package org.seadva.metadatagen.metagen.impl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.seadva.metadatagen.metagen.BaseMetadataGen;


public class OREMetadataGen extends BaseMetadataGen {


    public static String CREATOR = "Creator";
    public static String ABSTRACT = "Abstract";
    public static String TITLE = "Title";
    public static String DESCRIBES = "describes";

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
            if(!hasMinimalMetadata(oreString)){
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
            validated = false;
            System.out.println(OREMetadataGen.class.getName() + " : ORE is not a valid JSON object");
            this.errorMsg = "ORE is not a valid JSON object";
        }

        if(!oreObject.has(DESCRIBES)) {
            validated = false;
            System.out.println(OREMetadataGen.class.getName() + " : ORE Does not have '" + DESCRIBES + "'");
            this.errorMsg = "ORE Does not have '" + DESCRIBES + "'";
        }

        if(!(oreObject.get(DESCRIBES) instanceof JSONObject)) {
            validated = false;
            System.out.println(OREMetadataGen.class.getName() + " : ORE Does not have '" + DESCRIBES + "'");
            this.errorMsg = "ORE Does not have a valid JSON object for '" + DESCRIBES + "'";
        }

        JSONObject object = (JSONObject)oreObject.get(DESCRIBES);

        if(!object.has(CREATOR) || !nullCheck(object.get(CREATOR)) ) {
            validated = false;
            System.out.println(OREMetadataGen.class.getName() + " : ORE request does not contain value for '" + CREATOR + "' field.");
            this.errorMsg = "ORE request does not contain value for '" + CREATOR + "' field.";
        }
        if(!object.has(TITLE) || !nullCheck(object.get(TITLE)) ) {
            validated = false;
            System.out.println(OREMetadataGen.class.getName() + " : ORE request does not contain value for '" + TITLE + "' field.");
            this.errorMsg = "ORE request does not contain value for '" + TITLE + "' field.";
        }
        if(!object.has(ABSTRACT) || !nullCheck(object.get(ABSTRACT)) ) {
            validated = false;
            System.out.println(OREMetadataGen.class.getName() + " : ORE request does not contain value for '" + ABSTRACT + "' field.");
            this.errorMsg = "ORE request does not contain value for '" + ABSTRACT + "' field.";
        }

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

}
