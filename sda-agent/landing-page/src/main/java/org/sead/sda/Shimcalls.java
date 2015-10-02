package org.sead.sda;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class Shimcalls {
	
	private Properties property;
	private String sda_researchobjects;
	private String cp_researchobject;
	private String output = null;
	
	public Shimcalls(String propertyPath){
		this.property = new PropertiesReader(propertyPath).getProperties();
		this.sda_researchobjects = this.property.getProperty("sda.researchobjects");
		this.cp_researchobject = this.property.getProperty("cp.researchobject");
	}
	
	
	public StringBuilder getCalls(String url_string){
		
		StringBuilder sb = new StringBuilder();
		
		try{
			URL url = new URL(url_string);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
			
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}
			
			
			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));
			
			String output;
			while ((output = br.readLine()) != null) {
				sb.append(output);			
			}
			
			conn.disconnect();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		return sb;
	}
	
	
	public JSONArray getResearchObjectsList_repo(){
		
		JSONArray object = new JSONArray();
		JSONParser parser = new JSONParser();
		
		StringBuilder new_sb = getCalls(this.sda_researchobjects);
		
		try {
			Object obj = parser.parse(new_sb.toString());
			object = (JSONArray) obj;
		}catch(Exception e){
			e.printStackTrace();
		}
			
		return object;
	}
	
	
	public JSONObject getResearchObject_cp(String id){
		
		JSONObject object = new JSONObject();
		JSONParser parser = new JSONParser();
		
		StringBuilder new_sb = getCalls(this.cp_researchobject+File.separator+id);
		try {
			Object obj = parser.parse(new_sb.toString());
			object = (JSONObject) obj;
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return object;
	}
	
	

	public JSONObject getResearchObjectORE(String ore_url){
		
		JSONObject object = new JSONObject();
		JSONParser parser = new JSONParser();
		

		StringBuilder new_sb = null;
		if (ore_url.startsWith("https")){
			new_sb = getCalls(ore_url);
		}else{
			new_sb = getCalls("https"+ore_url.substring(ore_url.indexOf(":")));
		}
		try {
			Object obj = parser.parse(new_sb.toString());
			object = (JSONObject) obj;
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return object;
	}
	
	
	public void getObjectID(JSONObject obj, String keyword){ //Identifier or @id
		Set keys = obj.keySet();
		Object[] keyList = keys.toArray();
		
		
		for (Object key : keyList){
			if (key.toString().matches(keyword)){
				this.output =  obj.get(key).toString();
				break;
			}else if (obj.get(key) instanceof JSONObject){
				getObjectID((JSONObject) obj.get(key), keyword);
			} else if (obj.get(key) instanceof JSONArray){
				JSONArray insideArray = (JSONArray) obj.get(key);
				for (int i = 0 ; i< insideArray.size(); i++){
					if (insideArray.get(i) instanceof JSONObject){
						getObjectID((JSONObject) insideArray.get(i), keyword);
					}
				}
			}
		}
	}
	
	
	public void updateStatus(String doi_url, String id){
		try{
			URL url = new URL(this.cp_researchobject+File.separator+id+File.separator+"status");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);
			
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}
			
			
			OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
			out.write(doi_url);
			out.close();
			
			
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			 
			while (in.readLine() != null) {
				System.out.println(in.readLine());
			}
			System.out.println("\nCrunchify REST Service Invoked Successfully..");
			
			in.close();
			
			conn.disconnect();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	

	public String getID(){
		return this.output;
	}
	


}
