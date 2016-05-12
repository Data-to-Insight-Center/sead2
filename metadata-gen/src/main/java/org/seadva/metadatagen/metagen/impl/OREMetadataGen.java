package org.seadva.metadatagen.metagen.impl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.seadva.metadatagen.metagen.BaseMetadataGen;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


public class OREMetadataGen extends BaseMetadataGen {


    public static String CREATOR = "Creator";
    public static String ABSTRACT = "Abstract";
    public static String TITLE = "Title";
    public static String DESCRIBES = "describes";
    public static String AGGREGATES = "aggregates";
    public static String SIMILAR_TO = "similarTo";
    public static String HAS_PART = "Has Part";
    public static String IDENTIFIER = "Identifier";

    private String errorMsg = null;

    public OREMetadataGen() {
        errorMsg = "";
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    @Override
    public String generateMetadata(String id){

        String result = "";
        return result;

    }

    public boolean hasValidOREMetadata(String oreString) {

        // Checking whether OREMap request contains metadata needed - check for independent submissions
        try {
            if(!hasMinimalMetadata(oreString) || !hasValidDownloadLinks(oreString)){
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println(OREMetadataGen.class.getName() + " : Error while validating ORE");
            this.errorMsg = "Error while validating ORE";
            return  false;
        }

        return true;
    }

    private boolean hasMinimalMetadata(String oreString) throws JSONException{
        boolean validated = true;

        // Checking whether ORE metadata includes Creator, Title and Abstract
        // These are the metadata required for SDA-Agent
        JSONObject oreObject = null;
        try {
            oreObject = new JSONObject(oreString);
        } catch (JSONException e) {
            System.out.println(OREMetadataGen.class.getName() + " : ORE is not a valid JSON object");
            this.errorMsg = "ORE is not a valid JSON object";
            return false;
        }

        if(!oreObject.has(DESCRIBES)) {
            System.out.println(OREMetadataGen.class.getName() + " : ORE Does not have '" + DESCRIBES + "'");
            this.errorMsg = "ORE Does not have '" + DESCRIBES + "'";
            return false;
        }

        if(!(oreObject.get(DESCRIBES) instanceof JSONObject)) {
            System.out.println(OREMetadataGen.class.getName() + " : ORE Does not have '" + DESCRIBES + "'");
            this.errorMsg = "ORE Does not have a valid JSON object for '" + DESCRIBES + "'";
            return false;
        }

        JSONObject object = (JSONObject)oreObject.get(DESCRIBES);

        if(!object.has(CREATOR) || !nullCheck(object.get(CREATOR)) ) {
            System.out.println(OREMetadataGen.class.getName() + " : ORE request does not contain value for '" + CREATOR + "' field.");
            this.errorMsg = "ORE request does not contain value for '" + CREATOR + "' field.";
            return false;
        }
        if(!object.has(TITLE) || !nullCheck(object.get(TITLE)) ) {
            System.out.println(OREMetadataGen.class.getName() + " : ORE request does not contain value for '" + TITLE + "' field.");
            this.errorMsg = "ORE request does not contain value for '" + TITLE + "' field.";
            return false;
        }
        if(!object.has(ABSTRACT) || !nullCheck(object.get(ABSTRACT)) ) {
            System.out.println(OREMetadataGen.class.getName() + " : ORE request does not contain value for '" + ABSTRACT + "' field.");
            this.errorMsg = "ORE request does not contain value for '" + ABSTRACT + "' field.";
            return false;
        }

        return validated;
    }

    private boolean hasValidDownloadLinks(String oreString) throws JSONException {
        boolean validated = true;
        JSONObject oreObject = new JSONObject(oreString);
        JSONObject describes = (JSONObject)oreObject.get(DESCRIBES);

        if(!describes.has(AGGREGATES)) {
            System.out.println(OREMetadataGen.class.getName() + " : ORE Does not have '" + AGGREGATES + "'");
            this.errorMsg = "ORE Does not have '" + AGGREGATES + "'";
            return false;
        }

        if(!(describes.get(AGGREGATES) instanceof JSONArray)) {
            System.out.println(OREMetadataGen.class.getName() + " : ORE Does not have a valid JSON Array object for '" + AGGREGATES + "'");
            this.errorMsg = "ORE Does not have a valid JSON Array object for '" + AGGREGATES + "'";
            return false;
        }

        JSONArray aggregates = (JSONArray)describes.get(AGGREGATES);

        for(int i=0 ; i < aggregates.length() ; i++) {
            Object part = aggregates.get(i);

            if(!(part instanceof JSONObject)) {
                System.out.println(OREMetadataGen.class.getName() + " : ORE '" + AGGREGATES + "' has invalid JSON Object");
                this.errorMsg = "ORE '" + AGGREGATES + "' has invalid JSON Object";
                return false;
            }

            JSONObject partObject = (JSONObject)part;

            if(!partObject.has(IDENTIFIER) || !(partObject.get(IDENTIFIER) instanceof String)) {
                System.out.println(OREMetadataGen.class.getName() + " : ORE '" + AGGREGATES + "' has Object with invalid '" + IDENTIFIER + "'");
                this.errorMsg = "ORE '" + AGGREGATES + "' has Object with invalid '" + IDENTIFIER + "'";
                return false;
            }

            String identifier = (String)partObject.get(IDENTIFIER);

            if(partObject.has(SIMILAR_TO) && !validateDownloadLink(partObject.get(SIMILAR_TO))) {
                System.out.println(OREMetadataGen.class.getName() + " : ORE has invalid download link to file with identifier : '" + identifier + "' ");
                this.errorMsg = "ORE has invalid download link to file with identifier : '" + identifier + "' ";
                return false;
            }
        }

        return validated;
    }

    private boolean validateDownloadLink(Object downloadLink) {
        boolean validated = true;

        if(!(downloadLink instanceof String))
            return false;

        if(!head((String)downloadLink))
            return false;

        return validated;
    }

    private boolean nullCheck(Object object) throws JSONException {
        boolean isNotNull = false;

        if(object instanceof String ){
            if(object != null || !object.equals("")) {
                isNotNull = true;
            }
        } else if(object instanceof JSONArray){
            JSONArray list = (JSONArray)object;
            for(int i = 0 ; i < list.length() ; i++){
                Object arrayItem = list.get(i);
                if(arrayItem != null && !arrayItem.equals("")){
                    isNotNull = true;
                    break;
                }
            }
        } else {
            System.out.println(OREMetadataGen.class.getName() + " : Unable to validate ORE since " + object + " is not either a String or an Array");
        }

        return isNotNull;
    }

    private boolean head(String url)  {
        try {
            URL urlCon = new URL(url);
            InputStream inputStream = urlCon.openStream();
            // Read in the first byte from the url.
            int size = 1;
            byte[] data = new byte[size];
            int length = inputStream.read(data);
            if (length == 1) {
                //System.out.println("Success");
                return true;
            } else {
                System.out.println("FAIL  : Cannot retrieve data from URL " + url);
                return false;
            }
        } catch (IOException e) {
            System.out.println("FAIL  : Exception thrown while calling the URL " + url + ", Error message : " + e.getMessage());
            return false;
        }
        /*try {
            URLConnection conn = new URL(url).openConnection();
            conn.connect();

            if (conn instanceof HttpURLConnection) {
                int code = ((HttpURLConnection)conn).getResponseCode();
                if (code < 200 || code >= 300) {
                    System.out.println("FAIL  : " + url + ", status : " + code);
                    return false;
                }
            } else {
                System.out.println("FAIL  : " + url + ", not a valid URL");
                return false;
            }
            System.out.println("FOUND : " + url);
            return true;
        } catch (Exception e) {
            System.out.println("FAIL  : " + url + ", exception thrown while calling the URL");
            return false;
        }*/
    }
}
