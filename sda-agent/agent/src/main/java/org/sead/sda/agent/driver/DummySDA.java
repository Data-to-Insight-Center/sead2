package org.sead.sda.agent.driver;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.sead.sda.agent.apicalls.NewOREmap;
import org.sead.sda.agent.engine.PropertiesReader;

public class DummySDA {
	
	private Properties property;
	private JSONObject ore;
	private String downloadPath;
	private String userAndpass;
	private ArrayList<String> errorLinks;
	private String rootPath = null;
	
	public DummySDA(PropertiesReader properties, JSONObject ore, JSONObject oldore){
		this.property = properties.getProperties();
		this.downloadPath = this.property.getProperty("dummySDA");
		this.ore = ore;
	
		
		this.userAndpass = property.getProperty("collection.user")+":"+property.getProperty("collection.pass");
		
		this.errorLinks = new ArrayList<String>();
		this.rootPath = createRootFolder(ore, this.downloadPath);
		
		writeOREmap(this.rootPath, oldore);
		
		JSONArray aggre = (JSONArray) ore.get("aggregates");
		
		download(aggre,this.rootPath);	
		
	}
	
	
	public String createRootFolder(JSONObject ore, String DummySDADownloadPath){
		String rootName = ore.get("Folder").toString();
		
		String path = DummySDADownloadPath + File.separator + rootName;
		
		createDirectory(path);
		
		return path;
		
	}
	
	
	public void createDirectory(String path){
		File newDir = new File(path);
		
		if (newDir.exists()){
			System.err.println("Duplicated Folder or not? "+path);
		}else{
			newDir.mkdirs();
		}
	}
	
	
	
	public void download(JSONArray object, String downloadPath){
		for (Object item : object.toArray()){
			JSONObject item_new = (JSONObject) item;
			if (item_new.containsKey("Folder")){
				String newFolderName = item_new.get("Folder").toString();
				String newDownloadPath = downloadPath + File.separator + newFolderName;
				createDirectory(newDownloadPath);
				download((JSONArray) item_new.get("content"), newDownloadPath);
			} else{
				HttpDownload httpDownload = new HttpDownload();	
				String label = item_new.get("Label").toString();
				String fileUrl = item_new.get("Link").toString();
				//System.out.println(fileUrl);
				String downloadPath_new = downloadPath + File.separator + label;
				httpDownload.connection(fileUrl, this.userAndpass, label);
				httpDownload.downloadFile(downloadPath_new);
				errorLinks.addAll(httpDownload.gerErrorLinks());
				httpDownload.disconnect();
									
			}	
		}
	}
	
	
	
	public ArrayList<String> getErrorLinks(){
		return this.errorLinks;
	}
	
	public String getRootPath(){
		return this.rootPath;
	}
	
	
	public void writeOREmap(String rootPath, JSONObject oldore){
		try{
			FileWriter outFile = new FileWriter(rootPath +File.separator+"OREmap.txt", true);
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
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args){
		/*
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = null;
        try {
 
            Object obj = parser.parse(new FileReader(
            		"/Users/yuluo/Desktop/ORE.txt"));
 
             jsonObject = (JSONObject) obj;
        }catch(Exception e){
        	e.printStackTrace();
        }
        
        NewOREmap oreMap = new NewOREmap(jsonObject);
		JSONObject newOREmap = oreMap.getNewOREmap();
		
        PropertiesReader read = new PropertiesReader("/Users/yuluo/Documents/workspace/New_Agent/config/config.properties");
        
        DummySDA sda = new DummySDA(read,newOREmap);
        */
		
	}
	
	
	
}
