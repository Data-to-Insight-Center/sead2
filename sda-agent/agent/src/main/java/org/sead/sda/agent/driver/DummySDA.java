package org.sead.sda.agent.driver;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.sead.sda.agent.engine.PropertiesReader;

public class DummySDA {

    private String userAndpass;
    private ArrayList<String> errorLinks;
    private String rootPath = null;

    public DummySDA(JSONObject ore, JSONObject oldore) {
        this.userAndpass = PropertiesReader.clowderUser + ":" + PropertiesReader.clowderPassword;

        this.errorLinks = new ArrayList<String>();
        this.rootPath = createRootFolder(ore, PropertiesReader.dummySDA);

        writeOREmap(this.rootPath, oldore);

        JSONArray aggre = (JSONArray) ore.get("aggregates");

        download(aggre, this.rootPath);
    }


    public String createRootFolder(JSONObject ore, String DummySDADownloadPath) {
        String rootName = ore.get("Folder").toString();

        String path = DummySDADownloadPath + File.separator + rootName;

        createDirectory(path);

        return path;

    }


    public void createDirectory(String path) {
        File newDir = new File(path);

        if (newDir.exists()) {
            System.err.println("Duplicated Folder or not? " + path);
        } else {
            newDir.mkdirs();
        }
    }


    public void download(JSONArray object, String downloadPath) {
        for (Object item : object.toArray()) {
            JSONObject itemNew = (JSONObject) item;
            if (itemNew.containsKey("Folder")) {
                String newFolderName = itemNew.get("Folder").toString();
                String newDownloadPath = downloadPath + File.separator + newFolderName;
                createDirectory(newDownloadPath);
                download((JSONArray) itemNew.get("content"), newDownloadPath);
            } else {
                HttpDownload httpDownload = new HttpDownload();
                String label = itemNew.get("Label").toString();
                String fileUrl = itemNew.get("Link").toString();
                String downloadPath_new = downloadPath + File.separator + label;
                httpDownload.connection(fileUrl, this.userAndpass, label);
                httpDownload.downloadFile(downloadPath_new);
                errorLinks.addAll(httpDownload.gerErrorLinks());
                httpDownload.disconnect();
            }
        }
    }


    public ArrayList<String> getErrorLinks() {
        return this.errorLinks;
    }

    public String getRootPath() {
        return this.rootPath;
    }


    public void writeOREmap(String rootPath, JSONObject oldore) {
        try {
            FileWriter outFile = new FileWriter(rootPath + File.separator + "OREmap.json", true);
            try {
                PrintWriter out1 = new PrintWriter(outFile);
                try {
                    out1.append(oldore.toJSONString());
                } finally {
                    out1.close();
                }
            } finally {
                outFile.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
