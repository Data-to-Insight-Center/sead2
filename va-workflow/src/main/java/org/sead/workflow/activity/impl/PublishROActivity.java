package org.sead.workflow.activity.impl;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sead.workflow.activity.AbstractWorkflowActivity;
import org.sead.workflow.activity.SeadWorkflowActivity;
import org.sead.workflow.config.SeadWorkflowConfig;
import org.sead.workflow.context.SeadWorkflowContext;
import org.sead.workflow.exception.SeadWorkflowException;
import org.sead.workflow.util.Constants;
import org.seadva.services.statusTracker.SeadStatusTracker;
import org.seadva.services.statusTracker.enums.SeadStatus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

/**
 * Responsible for publishing the RO by calling Matchmaker
 */
public class PublishROActivity extends AbstractWorkflowActivity {

    @Override
    public void execute(SeadWorkflowContext context, SeadWorkflowConfig config) {

        System.out.println("\n=====================================");
        System.out.println("Executing activity : " + activityName);
        System.out.println("-----------------------------------\n");

        SeadStatusTracker.addStatus(context.getProperty(Constants.RO_ID), SeadStatus.WorkflowStatus.PUBLISH_RO_BEGIN.getValue());

        if(context.getProperty(Constants.VALIDATED).equals(Constants.FALSE)){
            System.out.println(PublishROActivity.class.getName() + " : Not publishing RO");
            return;
        }

        HashMap<String, String> activityParams = new HashMap<String, String>();
        for(SeadWorkflowActivity activity : config.getActivities()){
            AbstractWorkflowActivity abstractActivity = (AbstractWorkflowActivity)activity;
            if(abstractActivity.activityName.equals(activityName)){
                activityParams = abstractActivity.params;
                break;
            }
        }

        JSONObject rootObject = new JSONObject();
        try {
            JSONObject ro = new JSONObject(context.getProperty(Constants.JSON_RO));

            JSONObject messageObject = new JSONObject();
            messageObject.put("operation", "query");
            JSONObject roObject = new JSONObject();
            roObject.put("@context", "http://schema.org/");
            roObject.put("@type", "DataDownload");
            roObject.put("name", ro.has("Title") ? ro.get("Title"): "");
            roObject.put("ROID", context.getProperty(Constants.RO_ID));
            roObject.put("description", ro.has("Abstract") ? ro.get("Abstract"): "");
            //roObject.put("sourceOrganization", "");
            //roObject.put("fileSize", new JSONObject().put("value", 2000).put("unit", "MB"));
            roObject.put("fileSize", new JSONObject().put("value", calculateSize(ro)).put("unit", "b"));
            //roObject.put("contentUrl", context.getPSInstance().getUrl());

            ArrayList<String> downloadLinks = getDownloadLinks(ro, new ArrayList<String>());
            String downloadLinkList = "";
            for(String link : downloadLinks){
                downloadLinkList = downloadLinkList + " " + link;
            }
            roObject.put("contentUrl", downloadLinkList.trim());
            roObject.put("subject", ro.has("Topic") ? ro.get("Topic") : "");
            roObject.put("contentType", getFormats(ro, new ArrayList<String>()).get(0));
            roObject.put("author", getAuthors(ro).get(0));
            messageObject.put("message", roObject);

            rootObject.put("request", messageObject);
            rootObject.put("responseKey", "d4419434-25df-4f41-a28f-9f89f8b9dd59");
        } catch (JSONException e) {
            throw new SeadWorkflowException("Error creating the JSONLD message", e);
        } catch (IOException e) {
            throw new SeadWorkflowException("Error creating the JSONLD message", e);
        }

        String exchangeName = activityParams.get("messaging.exchangename");
        String queueName = activityParams.get("messaging.queuename");
        String routingKey = activityParams.get("messaging.routingkey");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(activityParams.get("messaging.username"));
        factory.setPassword(activityParams.get("messaging.password"));
        factory.setVirtualHost(activityParams.get("messaging.virtualhost"));
        factory.setHost(activityParams.get("messaging.hostname"));
        factory.setPort(Integer.parseInt(activityParams.get("messaging.hostport")));

        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            boolean durable = true;
            channel.queueDeclare(queueName, durable, false, false, null);
            byte[] messageBodyBytes = rootObject.toString().getBytes();
            channel.basicPublish(exchangeName, routingKey
                    , MessageProperties.PERSISTENT_TEXT_PLAIN, messageBodyBytes) ;
            channel.close();
            connection.close();
        } catch (IOException e) {
            throw new SeadWorkflowException("Error sending the message to MatchMaker", e);
        } catch (TimeoutException e) {
            throw new SeadWorkflowException("Error sending the message to MatchMaker", e);
        }

        System.out.println(PublishROActivity.class.getName() + " : Message successfully inserted to queue");
        System.out.println("Message : " + rootObject.toString());
        System.out.println("=====================================\n");

        SeadStatusTracker.addStatus(context.getProperty(Constants.RO_ID), SeadStatus.WorkflowStatus.PUBLISH_RO_END.getValue());


    }

    private JSONArray getAuthors(JSONObject ro) throws JSONException {
        JSONArray authorArray = new JSONArray();

        if(ro.has("creator")) {
            Object creators = ro.get("creator");
            if(creators instanceof JSONArray){
                for(int i = 0 ; i < ((JSONArray) creators).length() ; i++){
                    Object creator = ((JSONArray) creators).get(i);
                    if(creator instanceof String){
                        String[] params = ((String) creator).split(":",2);

                        JSONObject creatorObject = new JSONObject();
                        creatorObject.put("@type", "Person");
                        creatorObject.put("name", params[0]);
                        if(params.length > 1){
                            creatorObject.put("@id", params[1]);
                        }
                        creatorObject.put("email", "");
                        authorArray.put(creatorObject);
                    }
                }
            }
        }

        return authorArray;
    }

    private int calculateSize(JSONObject ro) throws JSONException, IOException {
        int size = 0;
        if(ro.has(Constants.HAS_FILES)){
            Object filesObject = ro.get(Constants.HAS_FILES);
            if(filesObject instanceof JSONArray){
                for(int i = 0 ; i < ((JSONArray) filesObject).length() ; i ++){
                    Object file = ((JSONArray) filesObject).get(i);
                    if(file instanceof JSONObject && ((JSONObject) file).has(Constants.SIZE)) {
                        size += Integer.parseInt((String)((JSONObject) file).get(Constants.SIZE));
                    }
                }
            }
        }

        if(ro.has(Constants.HAS_SUBCOLLECTIONS)){
            Object collectionsObject = ro.get(Constants.HAS_SUBCOLLECTIONS);
            if(collectionsObject instanceof JSONArray){
                for(int i = 0 ; i < ((JSONArray) collectionsObject).length() ; i ++){
                    Object collection = ((JSONArray) collectionsObject).get(i);
                    if(collection instanceof JSONObject && ((JSONObject) collection).has(Constants.FLOCAT)) {
                        String location = (String) ((JSONObject) collection).get(Constants.FLOCAT);
                        FileInputStream roFile = new FileInputStream(new File(location));
                        String colRoString = IOUtils.toString(roFile, "UTF-8");
                        size += calculateSize(new JSONObject(colRoString));
                    }
                }
            }
        }

        return size;
    }

    private ArrayList<String> getFormats(JSONObject ro, ArrayList<String> formats) throws JSONException, IOException {
        String format = "Mimetype";

        if(ro.has(Constants.HAS_FILES)){
            Object filesObject = ro.get(Constants.HAS_FILES);
            if(filesObject instanceof JSONArray){
                for(int i = 0 ; i < ((JSONArray) filesObject).length() ; i ++){
                    Object file = ((JSONArray) filesObject).get(i);
                    if(file instanceof JSONObject && ((JSONObject) file).has(format)) {
                        String fileFormat = (String)((JSONObject) file).get(format);
                        if(!formats.contains(fileFormat)){
                            formats.add(fileFormat);
                        }
                    }
                }
            }
        }

        if(ro.has(Constants.HAS_SUBCOLLECTIONS)){
            Object collectionsObject = ro.get(Constants.HAS_SUBCOLLECTIONS);
            if(collectionsObject instanceof JSONArray){
                for(int i = 0 ; i < ((JSONArray) collectionsObject).length() ; i ++){
                    Object collection = ((JSONArray) collectionsObject).get(i);
                    if(collection instanceof JSONObject && ((JSONObject) collection).has(Constants.FLOCAT)) {
                        String location = (String) ((JSONObject) collection).get(Constants.FLOCAT);
                        FileInputStream roFile = new FileInputStream(new File(location));
                        String colRoString = IOUtils.toString(roFile, "UTF-8");
                        getFormats(new JSONObject(colRoString), formats);
                    }
                }
            }
        }

        return formats;
    }

    private ArrayList<String> getDownloadLinks(JSONObject ro, ArrayList<String> downloadLinks) throws JSONException, IOException {
        String downloadLink = "FLocat";

        if(ro.has(Constants.HAS_FILES)){
            Object filesObject = ro.get(Constants.HAS_FILES);
            if(filesObject instanceof JSONArray){
                for(int i = 0 ; i < ((JSONArray) filesObject).length() ; i ++){
                    Object file = ((JSONArray) filesObject).get(i);
                    if(file instanceof JSONObject && ((JSONObject) file).has(downloadLink)) {
                        String link = (String)((JSONObject) file).get(downloadLink);
                        if(!downloadLinks.contains(link)){
                            downloadLinks.add(link);
                        }
                    }
                }
            }
        }

        if(ro.has(Constants.HAS_SUBCOLLECTIONS)){
            Object collectionsObject = ro.get(Constants.HAS_SUBCOLLECTIONS);
            if(collectionsObject instanceof JSONArray){
                for(int i = 0 ; i < ((JSONArray) collectionsObject).length() ; i ++){
                    Object collection = ((JSONArray) collectionsObject).get(i);
                    if(collection instanceof JSONObject && ((JSONObject) collection).has(Constants.FLOCAT)) {
                        String location = (String) ((JSONObject) collection).get(Constants.FLOCAT);
                        FileInputStream roFile = new FileInputStream(new File(location));
                        String colRoString = IOUtils.toString(roFile, "UTF-8");
                        getDownloadLinks(new JSONObject(colRoString), downloadLinks);
                    }
                }
            }
        }

        return downloadLinks;
    }

}
