package org.seadva.metadatagen.metagen.impl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.seadva.metadatagen.metagen.BaseMetadataGen;
import org.seadva.metadatagen.util.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;


public class OREMetadataGen extends BaseMetadataGen {


    public static String CREATOR = "Creator";
    public static String ABSTRACT = "Abstract";
    public static String TITLE = "Title";
    public static String DESCRIBES = "describes";
    public static String AGGREGATES = "aggregates";
    public static String SIMILAR_TO = "similarTo";
    public static String HAS_PART = "Has Part";
    public static String IDENTIFIER = "Identifier";
    public static String SIZE = "Size";

    private String errorMsg = null;
    private boolean skipValidation;

    public OREMetadataGen() {
        errorMsg = "";
        skipValidation = false;
    }

    public boolean getSkipValidation() {
        return skipValidation;
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

            double size = partObject.has(SIZE) && partObject.get(SIZE) instanceof String ? Double.parseDouble((String) partObject.get(SIZE)) : -1;

            if(size > 0 && Constants.validateDownloadLinks && partObject.has(SIMILAR_TO)) {
                if(!validateDownloadLink(partObject.get(SIMILAR_TO))) {
                    System.out.println(OREMetadataGen.class.getName() + " : ORE has invalid download link to file with identifier : '" + identifier + "' ");
                    this.errorMsg = "ORE has invalid download link to file with identifier : '" + identifier + "' ";
                    return false;
                } else if(this.skipValidation == true) {
                    System.out.println(OREMetadataGen.class.getName() + " : ORE has a file download link that is not working at the moment : '" + partObject.get(SIMILAR_TO) + "' ");
                    this.errorMsg = "ORE has a file download link that is not working at the moment : '" + partObject.get(SIMILAR_TO) + "' ";
                    break;
                }
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
            URLConnection con = urlCon.openConnection();
            con.setConnectTimeout(5000);
            InputStream inputStream = con.getInputStream();

            // Read in the first byte from the url.
            int size = 1;
            byte[] data = new byte[size];
            int length = inputStream.read(data);
            inputStream.close();
            if (length == 1) {
                //System.out.println("Success");
                return true;
            } else {
                System.out.println("FAIL  : Cannot retrieve data from URL " + url);
                return false;
            }
        } catch (IOException e) {
            System.out.println("FAIL  : Exception thrown while calling the URL " + url + ", Error message : " + e.getMessage());
            if(e instanceof SocketTimeoutException){
                this.skipValidation = true;
                return true;
            }
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
