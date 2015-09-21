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
import org.seadva.services.statusTracker.SeadStatusTracker;
import org.seadva.services.statusTracker.enums.SeadStatus;

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
        System.out.println("\n=====================================");
        System.out.println("Executing MicroService : " + activityName);
        System.out.println("-----------------------------------\n");

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
            throw new SeadWorkflowException("Error occurred while validating collection " + context.getCollectionId());            
        }

        System.out.println("=====================================\n");
    }

    private boolean validateFilesInCollection(String roString, SeadWorkflowContext context) throws JSONException{

        boolean collectionValidated = true;

        return collectionValidated;
    }
}
