/*
 * Copyright 2015 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author luoyu@indiana.edu
 * @author isuriara@indiana.edu
 */

package org.sead.sda.agent.apicalls;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import javax.ws.rs.core.MediaType;

import org.json.JSONException;
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

        StringBuilder new_sb = getCalls(this.cp_researchobject + "/" + id);
        try {
            Object obj = parser.parse(new_sb.toString());
            object = (JSONObject) obj;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return object;
    }


    public JSONObject getResearchObjectORE(String ore_url) throws Exception {

		JSONParser parser = new JSONParser();

        StringBuilder newSb = getCalls(ore_url);
        Object obj = parser.parse(newSb.toString());
        JSONObject object = (JSONObject) obj;

		return object;
	}

    // TODO:
    // Using org.json library to pretty print the JSON. It's good to convert the entire SDA agent
    // code to org.json as all other SEAD components are using that library.
    public org.json.JSONObject getJsonORE(String oreUrl) throws JSONException {
        StringBuilder oreBuffer = getCalls(oreUrl);
        return new org.json.JSONObject(oreBuffer.toString());
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
                doiUrl.trim() + "\"}";
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
