package org.sead.workflow.activity.impl;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sead.workflow.activity.AbstractWorkflowActivity;
import org.sead.workflow.activity.SeadWorkflowActivity;
import org.sead.workflow.config.SeadWorkflowConfig;
import org.sead.workflow.context.SeadWorkflowContext;
import org.sead.workflow.util.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Responsible for converting the JSONLD format of metadata received from PS to the format
 * that is accepted by RO Info system.
 * Converts the namespaces of metadata in a collection and add information about files and
 * pointers to sub-collections.
 */
public class ConvertROActivity extends AbstractWorkflowActivity {

    public static final String REST_CONTEXT = "@context";
    public static final String REST_ID = "@id";
    public static final String IDENTIFIER = "Identifier";
    public static final String GEN_AT = "Source";
    public static final String GEN_AT_URL = "http://sead-data.net/terms/generatedAt";
    public static final String FLOCAT = "FLocat";
    public static final String FLOCAT_URL = "http://www.loc.gov/METS/FLocat";

    @Override
    public void execute(SeadWorkflowContext context, SeadWorkflowConfig config) {
        System.out.println("Executing activity : " + activityName);

        String roId = context.getCollectionId();

        HashMap<String, String> activityParams = new HashMap<String, String>();
        for(SeadWorkflowActivity activity : config.getActivities()){
            AbstractWorkflowActivity abstractActivity = (AbstractWorkflowActivity)activity;
            if(abstractActivity.activityName.equals(activityName)){
                activityParams = abstractActivity.params;
                break;
            }
        }

        // generate JSONLD for the collection identified by roId
        String roJsonString = getRO(roId, activityParams, context);
        context.addProperty(Constants.JSON_RO, roJsonString);

    }

    private String getRO(String roId, HashMap<String, String> activityParams, SeadWorkflowContext context) {
        String psUrl = context.getPSInstance().getUrl();
        String tempPath = activityParams.get("tempPath");

        // get collection level metadata
        WebResource webResource = Client.create().resource(
                psUrl + "/resteasy/"
        );
        webResource = webResource.path("collections")
                .path(URLEncoder.encode(roId))
                .path("unique");

        ClientResponse response = webResource
                .get(ClientResponse.class);

        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(response.getEntityInputStream(), writer);

        } catch (IOException e) {
            e.printStackTrace();
        }
        // convert metatada namespaces
        String json = convertRO(writer.toString());

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);

            // add File metadata
            addFilesMetadata(roId, jsonObject, context);

            // add pointers to sub-collections
            JSONArray subCollectionArray = addSubCollectionMetadata(roId, context, tempPath);
            ((JSONObject)jsonObject.get(REST_CONTEXT)).put("Has Subcollection", "http://purl.org/dc/terms/hasPart");
            jsonObject.put("Has Subcollection", subCollectionArray);

            for (int i = 0; i < subCollectionArray.length(); i++) {
                JSONObject arrayItem = (JSONObject)subCollectionArray.get(i);
                // generate JSONLD for each sub-collection. This is a recursive call
                getRO((String)arrayItem.get(IDENTIFIER), activityParams, context);
            }

            // Write JSONLD to a file
            FileOutputStream fileOutputStream = new FileOutputStream(new File(tempPath + "ro_"+ getROFileName(roId) + ".json"));
            IOUtils.write(jsonObject.toString(), fileOutputStream);
            fileOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
    }

    private String convertRO(String roString){
        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(roString);
            Iterator rootIterator = jsonObject.keys();
            while (rootIterator.hasNext()) {
                String rootKey = (String)rootIterator.next();
                if (rootKey.equals(REST_CONTEXT)) {
                    JSONObject object = (JSONObject)jsonObject.get(rootKey);
                    Iterator iterator = object.keys();
                    while(iterator.hasNext()){
                        String key = (String)iterator.next();
                        Object value = object.get(key);
                        if(value instanceof String && Constants.metadataPredicateMap.get(value) != null){
                            object.put(key, Constants.metadataPredicateMap.get(value));
                        } else if(value instanceof JSONObject &&
                                Constants.metadataPredicateMap.get((String)((JSONObject)value).get(REST_ID)) != null){
                            object.put(key, Constants.metadataPredicateMap.get(
                                    Constants.metadataPredicateMap.get((String)((JSONObject)value).get(REST_ID))));

                        } else {
                            //object.put(key, "test");
                        }
                    }

                    if(!object.has(FLOCAT)) {
                        object.put(FLOCAT, FLOCAT_URL);
                    }
                } else {
                    //TODO flatten the objects
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    private void addFilesMetadata(String roId, JSONObject jsonObject, SeadWorkflowContext context) throws JSONException {

        String psUrl = context.getPSInstance().getUrl();
        String ps_un = context.getPSInstance().getUser();
        String ps_pw = context.getPSInstance().getPassword();

        JSONArray hasFilesArray = new JSONArray();
        ((JSONObject)jsonObject.get(REST_CONTEXT)).put("Has Files", "http://purl.org/dc/terms/hasPart");
        jsonObject.put("Has Files", hasFilesArray);

        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter(ps_un, ps_pw));
        WebResource webResource = client.resource(
                psUrl + "/resteasy/"
        );
        webResource = webResource.path("collections")
                .path(URLEncoder.encode(roId))
                .path("datasets");

        ClientResponse response = webResource
                .get(ClientResponse.class);

        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(response.getEntityInputStream(), writer);

        } catch (IOException e) {
            e.printStackTrace();
        }
        String fileArray = writer.toString();
        JSONObject fileArrayObject = new JSONObject(fileArray);
        fileArrayObject.remove(REST_CONTEXT);
        Iterator iterator = fileArrayObject.keys();

        while (iterator.hasNext()){
            String key = (String)iterator.next();

            Client fileClient = Client.create();
            fileClient.addFilter(new HTTPBasicAuthFilter(ps_un, ps_pw));
            WebResource fileWebResource = fileClient.resource(
                    psUrl + "/resteasy/"
            );
            fileWebResource = fileWebResource.path("datasets")
                    .path(URLEncoder.encode(key))
                    .path("unique");

            ClientResponse fileResponse = fileWebResource
                    .get(ClientResponse.class);

            StringWriter fileWriter = new StringWriter();
            try {
                IOUtils.copy(fileResponse.getEntityInputStream(), fileWriter);

            } catch (IOException e) {
                e.printStackTrace();
            }
            JSONObject fileObject = new JSONObject(fileWriter.toString());
            fileObject.remove(REST_CONTEXT);
            hasFilesArray.put(fileObject);
        }
    }

    private JSONArray addSubCollectionMetadata(String roId, SeadWorkflowContext context, String tempPath) throws JSONException {

        String psUrl = context.getPSInstance().getUrl();
        String ps_un = context.getPSInstance().getUser();
        String ps_pw = context.getPSInstance().getPassword();

        JSONArray hasFilesArray = new JSONArray();

        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter(ps_un, ps_pw));
        WebResource webResource = client.resource(
                psUrl + "/resteasy/"
        );
        webResource = webResource.path("collections")
                .path(URLEncoder.encode(roId))
                .path("collections");

        ClientResponse response = webResource
                .get(ClientResponse.class);

        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(response.getEntityInputStream(), writer);

        } catch (IOException e) {
            e.printStackTrace();
        }
        String subCollectionArray = writer.toString();
        JSONObject subCollectionArrayObject = new JSONObject(subCollectionArray);
        subCollectionArrayObject.remove(REST_CONTEXT);
        Iterator iterator = subCollectionArrayObject.keys();

        while (iterator.hasNext()){
            String key = (String)iterator.next();
            String subCollection = "{\""+IDENTIFIER+"\" : \""+key+"\" ," +
                                    "\""+FLOCAT+"\" : \""+tempPath + "ro_"+ getROFileName(key) + ".json\" }";
            JSONObject fileObject = new JSONObject(subCollection);
            hasFilesArray.put(fileObject);
        }

        return hasFilesArray;
    }

    private String getROFileName(String roId){
        if(roId.contains("/"))
            return roId.split("/")[roId.split("/").length-1];
        else if(roId.contains(":"))
            return roId.split(":")[roId.split(":").length-1];
        else
            return roId;
    }
}
