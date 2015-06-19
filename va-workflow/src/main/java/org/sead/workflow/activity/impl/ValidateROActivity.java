package org.sead.workflow.activity.impl;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sead.workflow.activity.AbstractWorkflowActivity;
import org.sead.workflow.config.SeadWorkflowConfig;
import org.sead.workflow.context.SeadWorkflowContext;
import org.sead.workflow.exception.SeadWorkflowException;
import org.sead.workflow.util.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Responsible for validating the RO.
 * Checks the file size and verify that all the files in the main collection
 * and sub collections do exist in Project Space
 */
public class ValidateROActivity extends AbstractWorkflowActivity {


    @Override
    public void execute(SeadWorkflowContext context, SeadWorkflowConfig config) {
        System.out.println("Executing activity : " + activityName);

        boolean validated = true;
        String roString = context.getProperty(Constants.JSON_RO);
        try {
            validated = validateFilesInCollection(roString, context);
        } catch (JSONException e) {
            throw new SeadWorkflowException( "Error occurred while validating the collection "
                    + context.getCollectionId() + " , Caused by: "  + e.getMessage() , e);
        }

        if(validated){
            System.out.println(ValidateROActivity.class.getName() + " : RO Validated");
            context.addProperty(Constants.VALIDATED, Constants.TRUE);
        } else {
            System.out.println(ValidateROActivity.class.getName() + " : RO Validation Failed");
            context.addProperty(Constants.VALIDATED, Constants.FALSE);
        }

    }

    private boolean validateFilesInCollection(String roString, SeadWorkflowContext context) throws JSONException{

        boolean collectionValidated = true;

        JSONObject jsonObject = new JSONObject(roString);

        // validate Files
        if(jsonObject.has(Constants.HAS_FILES)){
            String id = jsonObject.has(Constants.IDENTIFIER) ? jsonObject.get(Constants.IDENTIFIER).toString() : "";
            JSONArray filesArray = (JSONArray)jsonObject.get(Constants.HAS_FILES);
            for(int i = 0 ; i < filesArray.length(); i ++){
                JSONObject fileObject = (JSONObject)filesArray.get(i);
                String fileId = fileObject.has(Constants.IDENTIFIER) ? fileObject.get(Constants.IDENTIFIER).toString() : "";
                if(fileObject.has(Constants.SIZE)){
                    int size = Integer.parseInt((String)fileObject.get(Constants.SIZE));
                    if(size <= 0 ) {
                        collectionValidated = false;
                        System.out.println("Failed to Validated File : Size parameter missing in metadata of file " + fileId
                                + " in collection " + id);
                        context.addProperty(Constants.VALIDATION_FAILED_MSG, "Size is less than or equal to 0 of file " + fileId);
                        break;
                    }
                }else {
                    collectionValidated = false;
                    System.out.println("Failed to Validated File : Size parameter missing in metadata of file " + fileId
                            + " in collection " + id);
                    context.addProperty(Constants.VALIDATION_FAILED_MSG, "Size parameter missing in metadata of file " + fileId);
                    break;
                }
                System.out.println("Validated File : " + fileId + " in collection " + id);
            }
        }

        if(!collectionValidated){
            return collectionValidated;
        }

        // Recursively validate sub collection
        if(jsonObject.has(Constants.HAS_SUBCOLLECTIONS)){
            try {
                JSONArray subCollectionsArray = (JSONArray)jsonObject.get(Constants.HAS_SUBCOLLECTIONS);
                for(int i = 0 ; i < subCollectionsArray.length(); i ++){
                    JSONObject subCollection = (JSONObject)subCollectionsArray.get(i);
                    String FLocat = (String)subCollection.get(Constants.FLOCAT);

                    FileInputStream roFile = new FileInputStream(new File(FLocat));
                    String colRoString = IOUtils.toString(roFile, "UTF-8");
                    if(!validateFilesInCollection(colRoString, context)){
                        collectionValidated = false;
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return collectionValidated;
    }
}
