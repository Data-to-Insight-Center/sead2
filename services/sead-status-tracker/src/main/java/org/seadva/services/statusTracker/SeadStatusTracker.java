package org.seadva.services.statusTracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.seadva.services.statusTracker.common.DBConnectionPool;
import org.seadva.services.statusTracker.enums.SeadStatus;
import org.seadva.services.statusTracker.model.dao.CollectionStatusDao;
import org.seadva.services.statusTracker.model.dao.StatusDao;
import org.seadva.services.statusTracker.model.dao.impl.CollectionStatusDaoImpl;
import org.seadva.services.statusTracker.model.dao.impl.StatusDaoImpl;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import org.apache.commons.io.IOUtils;
import org.seadva.services.statusTracker.model.obj.impl.CollectionStatus;
import org.seadva.services.statusTracker.model.obj.impl.Status;


public class SeadStatusTracker {

    static StatusDao statusDao;
    static CollectionStatusDao collectionStatusDao;

    static String databaseUrl;
    static String databaseUser;
    static String databasePassword;
    static String tomcatDataPath;
    static String tomcatFilePath;

    static Map<Integer, Status> statusMap;
    static ArrayList<Integer>[] adj_list;
    static int beginIndex;

    static {

        InputStream inputStream =
                SeadStatusTracker.class.getResourceAsStream("Config.properties");

        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(inputStream, writer);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        String result = writer.toString();
        String[] pairs = result.trim().split(
                "\n|\\=");


        for (int i = 0; i + 1 < pairs.length; ) {
            String name = pairs[i++].trim();
            String value = pairs[i++].trim();
            if (name.equalsIgnoreCase("database.url"))
                databaseUrl = value;
            else if (name.equalsIgnoreCase("database.username"))
                databaseUser = value;
            else if (name.equalsIgnoreCase("database.password"))
                databasePassword = value;
            else if (name.equalsIgnoreCase("tomcat.path.to.data"))
                tomcatDataPath = value;
            else if (name.equalsIgnoreCase("tomcat.file.path"))
                tomcatFilePath = value;
        }
        try {
            DBConnectionPool.init(databaseUrl, databaseUser, databasePassword, 8, 30, 0);
            DBConnectionPool.launch();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        statusDao = new StatusDaoImpl();
        collectionStatusDao = new CollectionStatusDaoImpl();
        statusMap = new HashMap<Integer, Status>();

        populateStatuses();
        createAdjList();
    }

    public static boolean addStatus(String collectionId, String currentStatus) {

        System.out.println("Adding status to " + collectionId + " : " + currentStatus);
        CollectionStatus collectionStatus = new CollectionStatus();
        collectionStatus.setCollectionId(collectionId);
        collectionStatus.setCurrentStatus(currentStatus);
        collectionStatus.setUpdatedTime(System.nanoTime());
        collectionStatusDao.putCollectionStatus(collectionStatus);
        return true;

    }

    public static String getStatusByRo(String roId) {

        List<CollectionStatus> collectionStatusList = collectionStatusDao.getCollectionStatusById(roId);
        CollectionStatus lastCollectionStatus = collectionStatusList.get(0);
        for (CollectionStatus collectionStatus : collectionStatusList){
            if(lastCollectionStatus.getUpdatedTime() < collectionStatus.getUpdatedTime())
                lastCollectionStatus = collectionStatus;
        }
        Status lastStatus = statusDao.getStatusById(lastCollectionStatus.getCurrentStatus());
        return lastStatus.getStatusId().split(":")[0]+"-"+lastStatus.getDescription();
    }

    public static String getStatusGraphByRo(String roId) {

        List<CollectionStatus> collectionStatusList = collectionStatusDao.getCollectionStatusById(roId);
        ArrayList<String> visitedNodes = new ArrayList<String>();
        for (CollectionStatus collectionStatus : collectionStatusList)
            visitedNodes.add(collectionStatus.getCurrentStatus());
        drawGraph(visitedNodes);
        return tomcatFilePath;
    }

    private static void populateStatuses() {

        System.out.println("Populating status tables - START");


        for (SeadStatus.WorkflowStatus component : SeadStatus.WorkflowStatus.values()) {
            Status status = new Status();
            status.setComponent(SeadStatus.Components.Workflow.name());
            status.setStatusId(component.getValue());
            status.setDescription(component.name());
            statusDao.putStatus(status);
            statusMap.put(component.toIdx(), status);
        }

        for (SeadStatus.PDTStatus component : SeadStatus.PDTStatus.values()) {
            Status status = new Status();
            status.setComponent(SeadStatus.Components.PDT.name());
            status.setStatusId(component.getValue());
            status.setDescription(component.name());
            statusDao.putStatus(status);
            statusMap.put(component.toIdx(), status);
        }

        for (SeadStatus.MatchmakerStatus component : SeadStatus.MatchmakerStatus.values()) {
            Status status = new Status();
            status.setComponent(SeadStatus.Components.Matchmaker.name());
            status.setStatusId(component.getValue());
            status.setDescription(component.name());
            statusDao.putStatus(status);
            statusMap.put(component.toIdx(), status);
        }

        System.out.println("Populating status tables - END");

    }

    private static void createAdjList() {

        int max = 0;
        for (Integer integer : statusMap.keySet()) {
            if (max < integer)
                max = integer;
        }
        adj_list = (ArrayList<Integer>[]) new ArrayList[max++];

        beginIndex = SeadStatus.WorkflowStatus.START.toIdx();

        // Workflow Start
        ArrayList<Integer> edges = new ArrayList<Integer>();
        adj_list[SeadStatus.WorkflowStatus.START.toIdx()] = (ArrayList<Integer>) edges; // Start Workflow
        edges.add(SeadStatus.WorkflowStatus.CONVERT_RO_BEGIN.toIdx()); // Workflow_START -> CONVERT_RO_BEGIN

        edges = new ArrayList<Integer>();
        adj_list[SeadStatus.WorkflowStatus.CONVERT_RO_BEGIN.toIdx()] = edges;
        edges.add(SeadStatus.WorkflowStatus.CONVERT_RO_END.toIdx()); // CONVERT_RO_BEGIN -> CONVERT_RO_END

        edges = new ArrayList<Integer>();
        adj_list[SeadStatus.WorkflowStatus.CONVERT_RO_END.toIdx()] = edges;
        edges.add(SeadStatus.WorkflowStatus.PERSIST_RO_BEGIN.toIdx()); // CONVERT_RO_END -> PERSIST_RO_BEGIN

        edges = new ArrayList<Integer>();
        adj_list[SeadStatus.WorkflowStatus.PERSIST_RO_BEGIN.toIdx()] = edges;
        edges.add(SeadStatus.WorkflowStatus.PERSIST_RO_END.toIdx()); // PERSIST_RO_BEGIN -> PERSIST_RO_END

        edges = new ArrayList<Integer>();
        adj_list[SeadStatus.WorkflowStatus.PERSIST_RO_END.toIdx()] = edges;
        edges.add(SeadStatus.WorkflowStatus.VALIDATE_RO_BEGIN.toIdx()); // PERSIST_RO_END -> VALIDATE_RO_BEGIN

        edges = new ArrayList<Integer>();
        adj_list[SeadStatus.WorkflowStatus.VALIDATE_RO_BEGIN.toIdx()] = edges;
        edges.add(SeadStatus.WorkflowStatus.VALIDATE_RO_END.toIdx()); // VALIDATE_RO_BEGIN -> VALIDATE_RO_END

        edges = new ArrayList<Integer>();
        adj_list[SeadStatus.WorkflowStatus.VALIDATE_RO_END.toIdx()] = edges;
        edges.add(SeadStatus.WorkflowStatus.UPDATE_RO_STATE_BEGIN.toIdx()); // VALIDATE_RO_END -> UPDATE_RO_STATE_BEGIN

        edges = new ArrayList<Integer>();
        adj_list[SeadStatus.WorkflowStatus.UPDATE_RO_STATE_BEGIN.toIdx()] = edges;
        edges.add(SeadStatus.WorkflowStatus.UPDATE_RO_STATE_END.toIdx()); // UPDATE_RO_STATE_BEGIN -> UPDATE_RO_STATE_END

        edges = new ArrayList<Integer>();
        adj_list[SeadStatus.WorkflowStatus.UPDATE_RO_STATE_END.toIdx()] = edges;
        edges.add(SeadStatus.WorkflowStatus.UPDATE_PDT_BEGIN.toIdx()); // UPDATE_RO_STATE_END -> UPDATE_PDT_BEGIN

        // Workflow calling the PDT
        edges = new ArrayList<Integer>();
        adj_list[SeadStatus.WorkflowStatus.UPDATE_PDT_BEGIN.toIdx()] = edges;
        edges.add(SeadStatus.PDTStatus.START.toIdx()); // UPDATE_PDT_BEGIN -> PDT_START

        // Executing PDT Activities
        edges = new ArrayList<Integer>();
        adj_list[SeadStatus.PDTStatus.START.toIdx()] = edges;
        edges.add(SeadStatus.PDTStatus.END.toIdx()); // PDT_START -> PDT_END

        // PDT Finish and Workflow resume
        edges = new ArrayList<Integer>();
        adj_list[SeadStatus.PDTStatus.END.toIdx()] = edges;
        edges.add(SeadStatus.WorkflowStatus.UPDATE_PDT_END.toIdx()); // PDT_END -> UPDATE_PDT_END

        edges = new ArrayList<Integer>();
        adj_list[SeadStatus.WorkflowStatus.UPDATE_PDT_END.toIdx()] = edges;
        edges.add(SeadStatus.WorkflowStatus.PUBLISH_RO_BEGIN.toIdx()); // UPDATE_PDT_END -> PUBLISH_RO_BEGIN

        // Workflow hand-off RO to MM and finish UPDATE_PDT activity
        edges = new ArrayList<Integer>();
        adj_list[SeadStatus.WorkflowStatus.PUBLISH_RO_BEGIN.toIdx()] = edges;
        edges.add(SeadStatus.MatchmakerStatus.START.toIdx()); // PUBLISH_RO_BEGIN -> Matchmaker_START
        edges.add(SeadStatus.WorkflowStatus.PUBLISH_RO_END.toIdx()); // PUBLISH_RO_BEGIN -> PUBLISH_RO_END

        // End Workflow
        edges = new ArrayList<Integer>();
        adj_list[SeadStatus.WorkflowStatus.PUBLISH_RO_END.toIdx()] = edges;
        edges.add(SeadStatus.WorkflowStatus.END.toIdx()); // PUBLISH_RO_END -> Workflow_END

        // Executing MM activities
        edges = new ArrayList<Integer>();
        adj_list[SeadStatus.MatchmakerStatus.START.toIdx()] = edges;
        edges.add(SeadStatus.MatchmakerStatus.MM_ACTIVITY1_BEGIN.toIdx()); // Matchmaker_START -> MM_ACTIVITY1_BEGIN

        edges = new ArrayList<Integer>();
        adj_list[SeadStatus.MatchmakerStatus.MM_ACTIVITY1_BEGIN.toIdx()] = edges;
        edges.add(SeadStatus.MatchmakerStatus.MM_ACTIVITY1_END.toIdx()); // MM_ACTIVITY1_BEGIN -> MM_ACTIVITY1_END

        edges = new ArrayList<Integer>();
        adj_list[SeadStatus.MatchmakerStatus.MM_ACTIVITY1_END.toIdx()] = edges;
        edges.add(SeadStatus.MatchmakerStatus.END.toIdx()); // MM_ACTIVITY1_END -> MM_END

    }

    private static void drawGraph(ArrayList<String> visitedNodes) {

        try {
            JSONObject object = createNode(beginIndex, 0, 0, visitedNodes);
            FileOutputStream fileOutputStream = new FileOutputStream(new File(tomcatDataPath));
            IOUtils.write(object.toString(), fileOutputStream);
            fileOutputStream.close();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static JSONObject createNode(int startIndex, int x, int y, ArrayList<String> visitedNodes) throws JSONException {

        JSONObject object = new JSONObject();
        JSONArray nodes = new JSONArray();
        JSONArray edges = new JSONArray();
        object.put("nodes", nodes);
        object.put("edges", edges);

        int nextIndex = startIndex;

        while (nextIndex >= 0) {

            Status node = statusMap.get(nextIndex);
            JSONObject nodeObject = new JSONObject();
            nodeObject.put("id", "n" + nextIndex);
            nodeObject.put("label", node.getStatusId().split(":")[0] + ":" + node.getDescription());
            nodeObject.put("size", "10");
            nodeObject.put("x", x);
            nodeObject.put("y", y++);
            if (visitedNodes.contains(node.getStatusId()))
                nodeObject.put("color", "#00008B");
            else
                nodeObject.put("color", "#A9A9A9");
            nodes.put(nodeObject);

            if (nextIndex >= adj_list.length || adj_list[nextIndex] == null || adj_list[nextIndex].size() < 1) {
                nextIndex = -1;
                continue;
            }

            if (adj_list[nextIndex].size() > 1) {
                int x_indent = x;
                int size = adj_list[nextIndex].size();
                int increment = 8;
                x_indent = size % 2 == 0 ? x_indent - (size / 2 * increment - increment / 2) : x_indent - (size - 1) / 2 * increment;
                int y_indent = y++;

                for (int index : adj_list[nextIndex]) {

                    JSONObject branch = createNode(index, x_indent, y_indent, visitedNodes);
                    JSONArray branch_nodes = ((JSONArray) branch.get("nodes"));
                    for (int i = 0; i < branch_nodes.length(); i++) {
                        nodes.put(branch_nodes.get(i));
                    }

                    JSONArray branch_edges = ((JSONArray) branch.get("edges"));
                    for (int i = 0; i < branch_edges.length(); i++) {
                        edges.put(branch_edges.get(i));
                    }

                    JSONObject edge = new JSONObject();
                    edge.put("id", nextIndex + "-" + index);
                    edge.put("source", "n" + nextIndex);
                    edge.put("target", "n" + index);
                    edges.put(edge);

                    x_indent = x_indent + increment;
                }
                nextIndex = -1;
                continue;
            }

            int nextNode = adj_list[nextIndex].get(0);

            JSONObject edge = new JSONObject();
            edge.put("id", nextIndex + "-" + nextNode);
            edge.put("source", "n" + nextIndex);
            edge.put("target", "n" + nextNode);
            edges.put(edge);

            nextIndex = nextNode;
        }

        return object;
    }

    public static class UpdateGraph implements Runnable {
        private String collectionId;

        public UpdateGraph(String collection) {
            collectionId = collection;

        }

        public void run() {
            while (true) {
                try {
                    getStatusByRo(collectionId);
                    Thread.sleep(500);
                } catch (Exception e) {

                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        String collectionId = "http://sead_050WR3";

        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DBConnectionPool.getInstance().getEntry();
            statement = connection.prepareStatement("truncate collection_status");
            statement.executeUpdate();
        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {}
            }
            DBConnectionPool.getInstance().releaseEntry(connection);

        }

        SeadStatusTracker.addStatus(collectionId, SeadStatus.WorkflowStatus.START.getValue());

        SeadStatusTracker.addStatus(collectionId, SeadStatus.WorkflowStatus.CONVERT_RO_BEGIN.getValue());
        SeadStatusTracker.addStatus(collectionId, SeadStatus.WorkflowStatus.CONVERT_RO_END.getValue());

        SeadStatusTracker.addStatus(collectionId, SeadStatus.WorkflowStatus.PERSIST_RO_BEGIN.getValue());
        SeadStatusTracker.addStatus(collectionId, SeadStatus.WorkflowStatus.PERSIST_RO_END.getValue());

        SeadStatusTracker.addStatus(collectionId, SeadStatus.WorkflowStatus.VALIDATE_RO_BEGIN.getValue());
        SeadStatusTracker.addStatus(collectionId, SeadStatus.WorkflowStatus.VALIDATE_RO_END.getValue());

        SeadStatusTracker.addStatus(collectionId, SeadStatus.WorkflowStatus.UPDATE_RO_STATE_BEGIN.getValue());
        SeadStatusTracker.addStatus(collectionId, SeadStatus.WorkflowStatus.UPDATE_RO_STATE_END.getValue());

        SeadStatusTracker.addStatus(collectionId, SeadStatus.WorkflowStatus.UPDATE_PDT_BEGIN.getValue());
        SeadStatusTracker.addStatus(collectionId, SeadStatus.PDTStatus.START.getValue());
        SeadStatusTracker.addStatus(collectionId, SeadStatus.PDTStatus.END.getValue());
        SeadStatusTracker.addStatus(collectionId, SeadStatus.WorkflowStatus.UPDATE_PDT_END.getValue());

        SeadStatusTracker.addStatus(collectionId, SeadStatus.WorkflowStatus.PUBLISH_RO_BEGIN.getValue());
        SeadStatusTracker.addStatus(collectionId, SeadStatus.WorkflowStatus.PUBLISH_RO_END.getValue());
        SeadStatusTracker.addStatus(collectionId, SeadStatus.WorkflowStatus.END.getValue());

        SeadStatusTracker.addStatus(collectionId, SeadStatus.MatchmakerStatus.START.getValue());
        SeadStatusTracker.addStatus(collectionId, SeadStatus.MatchmakerStatus.MM_ACTIVITY1_BEGIN.getValue());
        SeadStatusTracker.addStatus(collectionId, SeadStatus.MatchmakerStatus.MM_ACTIVITY1_END.getValue());
        SeadStatusTracker.addStatus(collectionId, SeadStatus.MatchmakerStatus.END.getValue());

        //System.out.println(getStatusByRo(collectionId));

    }
}
