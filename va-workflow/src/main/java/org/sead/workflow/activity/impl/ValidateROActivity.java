package org.sead.workflow.activity.impl;

import com.hp.hpl.jena.graph.query.SimpleQueryEngine;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sead.workflow.activity.AbstractWorkflowActivity;
import org.sead.workflow.config.SeadWorkflowConfig;
import org.sead.workflow.context.SeadWorkflowContext;
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
            validated = validateFilesInCollection(roString);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(validated){
            System.out.println(ValidateROActivity.class.getName() + " : RO Validated");
            context.addProperty(Constants.VALIDATED, Constants.TRUE);
        } else {
            System.out.println(ValidateROActivity.class.getName() + " : RO Validation Failed");
            context.addProperty(Constants.VALIDATED, Constants.FALSE);
        }

    }

    private boolean validateFilesInCollection(String roString) throws JSONException, IOException {

        boolean collectionValidated = true;

        JSONObject jsonObject = new JSONObject(roString);

        // validate Files
        if(jsonObject.has(Constants.HAS_FILES)){
            JSONArray filesArray = (JSONArray)jsonObject.get(Constants.HAS_FILES);
            for(int i = 0 ; i < filesArray.length(); i ++){
                JSONObject fileObject = (JSONObject)filesArray.get(i);
                if(fileObject.has(Constants.SIZE)){
                    int size = Integer.parseInt((String)fileObject.get(Constants.SIZE));
                    if(size <= 0 ) {
                        collectionValidated = false;
                        System.out.println("Failed to Validated File : " + fileObject.get(Constants.IDENTIFIER) + " in collection "
                                + jsonObject.get(Constants.IDENTIFIER));
                        break;
                    }
                }else {
                    collectionValidated = false;
                    System.out.println("Failed to Validated File : " + fileObject.get(Constants.IDENTIFIER) + " in collection "
                            + jsonObject.get(Constants.IDENTIFIER));
                    break;
                }
                System.out.println("Validated File : " + fileObject.get(Constants.IDENTIFIER) + " in collection "
                        + jsonObject.get(Constants.IDENTIFIER));
            }
        }

        // Recursively validate sub collection
        if(jsonObject.has(Constants.HAS_SUBCOLLECTIONS)){
            JSONArray subCollectionsArray = (JSONArray)jsonObject.get(Constants.HAS_SUBCOLLECTIONS);
            for(int i = 0 ; i < subCollectionsArray.length(); i ++){
                JSONObject subCollection = (JSONObject)subCollectionsArray.get(i);
                String FLocat = (String)subCollection.get(Constants.FLOCAT);

                FileInputStream roFile = new FileInputStream(new File(FLocat));
                String colRoString = IOUtils.toString(roFile, "UTF-8");
                if(!validateFilesInCollection(colRoString)){
                    collectionValidated = false;
                    break;
                }
            }
        }

        return collectionValidated;
    }
}
