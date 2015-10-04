package org.sead.sda.agent.apicalls;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import javax.ws.rs.core.MediaType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.sead.sda.agent.engine.PropertiesReader;

public class Shimcalls {
	
	private String sda_researchobjects;
	private String cp_researchobject;
	private String output = null;
	
	public Shimcalls(){
		this.sda_researchobjects = PropertiesReader.sdaResearchObjects;
		this.cp_researchobject = PropertiesReader.allResearchObjects;
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


    public JSONArray getResearchObjectsList() {

        JSONArray object = new JSONArray();
        JSONParser parser = new JSONParser();

        StringBuilder new_sb = getCalls(sda_researchobjects);

        try {
            Object obj = parser.parse(new_sb.toString());
            object = (JSONArray) obj;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return object;
    }


    public JSONObject getResearchObject(String id) {

        JSONObject object = new JSONObject();
        JSONParser parser = new JSONParser();

        StringBuilder new_sb = getCalls(this.cp_researchobject + File.separator + id);
        try {
            Object obj = parser.parse(new_sb.toString());
            object = (JSONObject) obj;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return object;
    }


    public JSONObject getResearchObjectORE(String ore_url){
		
		JSONObject object = new JSONObject();
		JSONParser parser = new JSONParser();
		

		StringBuilder new_sb = getCalls(ore_url);
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
                for (Object anInsideArray : insideArray) {
                    if (anInsideArray instanceof JSONObject) {
                        getObjectID((JSONObject) anInsideArray, keyword);
                    }
                }
			}
		}
	}

    public void updateStatus(String doiUrl, String id) {
        WebResource webResource = Client.create().resource(this.cp_researchobject);
        String status = "{\"reporter\":\"sda\", \"stage\":\"Success\", \"message\":\"" +
                doiUrl + "\"}";
        System.out.println("Status update JSON: " + status);
        ClientResponse response = webResource.path(id)
                .path("status")
                .accept("application/json")
                .type("application/json")
                .post(ClientResponse.class, status);
        if (response.getStatus() != 200) {
            System.out.println("ERROR: Status update in C3P-R services filed.");
        }
    }

    public String getID(){
		return this.output;
	}
	


}
