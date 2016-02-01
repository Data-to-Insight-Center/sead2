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

package org.sead.sda;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class Shimcalls {

    private String output = null;

    public StringBuilder getCalls(String url_string) {

        StringBuilder sb = new StringBuilder();

        try {
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

        } catch (Exception e) {
            e.printStackTrace();
        }


        return sb;
    }

    public JSONObject getResearchObject(String id) {

        JSONObject object = new JSONObject();
        JSONParser parser = new JSONParser();

        StringBuilder new_sb = getCalls(Constants.allResearchObjects + "/" + id);
        try {
            Object obj = parser.parse(new_sb.toString());
            object = (JSONObject) obj;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return object;
    }


    public JSONObject getResearchObjectORE(String ore_url) {

        JSONObject object = new JSONObject();
        JSONParser parser = new JSONParser();

        StringBuilder new_sb = getCalls(ore_url);
        try {
            Object obj = parser.parse(new_sb.toString());
            object = (JSONObject) obj;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return object;
    }


    public void getObjectID(JSONObject obj, String keyword) { //Identifier or @id
        Set keys = obj.keySet();
        Object[] keyList = keys.toArray();


        for (Object key : keyList) {
            if (key.toString().matches(keyword)) {
                this.output = obj.get(key).toString();
                break;
            } else if (obj.get(key) instanceof JSONObject) {
                getObjectID((JSONObject) obj.get(key), keyword);
            } else if (obj.get(key) instanceof JSONArray) {
                JSONArray insideArray = (JSONArray) obj.get(key);
                for (Object anInsideArray : insideArray) {
                    if (anInsideArray instanceof JSONObject) {
                        getObjectID((JSONObject) anInsideArray, keyword);
                    }
                }
            }
        }
    }

    public String getID() {
        return this.output;
    }
    

    public Boolean validUrl(String url_string){
    	
    	try {
            URL url = new URL(url_string);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : Live data links isn't existed : "
                        + conn.getResponseCode());
                
            }

            conn.disconnect();
            
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    	   	
    	
    }

}
