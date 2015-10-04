package org.sead.sda.agent.engine;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class DOI {

    private String target;
    private String metadata;

    public DOI(String target, JSONObject ore) {
        this.target = target;
        this.metadata = getMetaData(ore);
    }

    public String getDoi() throws Exception {
        System.out.println("DOI Metadata: " + metadata);
        System.out.println("DOI Target: " + target);

        WebResource webResource = Client.create().resource(PropertiesReader.doiEndpoint);
        String request = "{\"target\":\"" + target + "\", \"permanent\":\"" +
                PropertiesReader.isDoiPermanent + "\", \"metadata\":" + metadata + "}";
        ClientResponse response = webResource.path("doi")
                .accept("application/json")
                .type("application/json")
                .post(ClientResponse.class, request);
        if (response.getStatus() != 200) {
            throw new Exception("DOI generation filed..");
        }

        JSONParser parser = new JSONParser();
        String entity = response.getEntity(String.class);
        JSONObject doiJSON = (JSONObject) parser.parse(entity);

        return doiJSON.get("doi").toString();
    }

    public String getMetaData(JSONObject ore) {
        JSONObject newData = new JSONObject();
        JSONObject describes = (JSONObject) ore.get("describes");
        if (describes != null) {
            // get title
            if (describes.get("Title") != null) {
                newData.put("title", describes.get("Title").toString());
            }
            // get creator
            Object creator = describes.get("Creator");
            if (creator != null) {
                if (creator instanceof JSONArray) {
                    newData.put("creator", ((JSONArray) creator).get(0).toString());
                } else {
                    newData.put("creator", creator.toString());
                }
            }
            // get publication date
            if (describes.get("Publication Date") != null) {
                newData.put("pubDate", describes.get("Publication Date").toString());
            }
        }
        return newData.toJSONString();
    }

}
