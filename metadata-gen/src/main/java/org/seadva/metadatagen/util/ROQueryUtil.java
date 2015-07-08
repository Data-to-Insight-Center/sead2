/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.seadva.metadatagen.util;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.seadva.metadatagen.model.MetadataObject;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.*;

public class ROQueryUtil {

    public static final String REST_CONTEXT = "@context";

    public MetadataObject readMetadata(String tagId) {

        MetadataObject metadataObject = new MetadataObject();

        String guid = null;

        if(tagId.contains("/"))
            guid = tagId.split("/")[tagId.split("/").length-1];
        else
            guid = tagId.split(":")[tagId.split(":").length-1];

        WebResource webResource = Client.create().resource(
                Constants.rosystemURL
        );
        webResource = webResource.path("resource")
                .path("jsonldro")
                .path(URLEncoder.encode(tagId));

        ClientResponse roResponse = webResource
                .get(ClientResponse.class);

        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(roResponse.getEntityInputStream(), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String json = writer.toString();

        //String json = Constants.json_map.get(guid);

        if(json == null) {
            return null;
        }

        try {
            JSONObject response = new JSONObject(json);
            JSONObject context = response.getJSONObject(REST_CONTEXT);
            // iterate through all children
            Iterator itr = response.keys();
            while (itr.hasNext()) {
                String child = (String) itr.next();
                // ignore context object
                if (child.equals(REST_CONTEXT)) {
                    continue;
                }
                // add predicate and value to the map
                if (child.equals("Has Subcollection")) {
                    addMetadata(response.get(child), metadataObject.getCollections(), context);
                } else if (child.equals("Has Files")) {
                    Object value = response.get(child);
                    if (value instanceof JSONArray) {
                        JSONArray array = (JSONArray) value;
                        for (int i = 0; i < array.length(); i++) {
                            Object arrayItem = array.get(i);
                            if(arrayItem instanceof JSONObject){
                                String id = ((JSONObject) arrayItem).getString("identifier");
                                metadataObject.getFiles().put(id, createMetadataObject((JSONObject) arrayItem, context));
                            }
                        }
                    }
                } else {
                    String predicate = context.get(child).toString();

                    List<String> list = metadataObject.getMetadataMap().get(predicate);
                    if (list == null) {
                        list = new ArrayList<String>();
                        metadataObject.getMetadataMap().put(predicate, list);
                    }
                    Object value = response.get(child);
                    addMetadata(value, list, context);
                }
            }
        } catch (JSONException e) {
            // TODO : Use logging and handle exceptions
            e.printStackTrace();
        }

        return metadataObject;
    }

    private Map<String,List<String>> createMetadataObject(JSONObject arrayItem, JSONObject context) throws JSONException {
        Iterator itr = arrayItem.keys();
        Map<String,List<String>> metadataMap = new HashMap<String,List<String>>();
        while (itr.hasNext()) {
            String child = (String) itr.next();

            //if(child.equalsIgnoreCase("identifier"))
                //continue;

            String predicate = context.get(child).toString();

            List<String> list = metadataMap.get(predicate);
            if (list == null) {
                list = new ArrayList<String>();
                metadataMap.put(predicate, list);
            }
            Object value = arrayItem.get(child);
            addMetadata(value, list, context);
        }
        return metadataMap;
    }

    private List<String> addMetadata(Object value, List<String> currentList, JSONObject context) throws JSONException {

            if (value instanceof String) {
                currentList.add(value.toString());
            } else if (value instanceof JSONArray) {
                JSONArray array = (JSONArray) value;
                for (int i = 0; i < array.length(); i++) {
                    Object arrayItem = array.get(i);
                    if (arrayItem instanceof String) {
                        currentList.add(arrayItem.toString());
                    }else if(arrayItem instanceof JSONObject) {
                        currentList.add(arrayItem.toString());
                    }
                }
            } else if (value instanceof JSONObject) {
                currentList.add(value.toString());
            }

        return currentList;
    }

}
