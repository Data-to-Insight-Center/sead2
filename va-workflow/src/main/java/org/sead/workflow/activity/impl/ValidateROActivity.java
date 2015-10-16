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
 * @author charmadu@umail.iu.edu
 */

package org.sead.workflow.activity.impl;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sead.workflow.activity.AbstractWorkflowActivity;
import org.sead.workflow.activity.SeadWorkflowActivity;
import org.sead.workflow.config.SeadWorkflowConfig;
import org.sead.workflow.context.SeadWorkflowContext;
import org.sead.workflow.exception.SeadWorkflowException;
import org.sead.workflow.util.Constants;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Responsible for validating the RO.
 * Checks the file size and verify that all the files in the main collection
 * and sub collections do exist in Project Space
 */
public class ValidateROActivity extends AbstractWorkflowActivity {


    @Override
    public void execute(SeadWorkflowContext context, SeadWorkflowConfig config) {
        // TODO: Remove system outs and add logging
        System.out.println("\n=====================================");
        System.out.println("Executing MicroService : " + activityName);
        System.out.println("-----------------------------------\n");

        HashMap<String, String> activityParams = new HashMap<String, String>();
        for(SeadWorkflowActivity activity : config.getActivities()){
            AbstractWorkflowActivity abstractActivity = (AbstractWorkflowActivity)activity;
            if(abstractActivity.activityName.equals(activityName)){
                activityParams = abstractActivity.params;
                break;
            }
        }

        String pdtUrl = activityParams.get("pdtUrl");

        // Call RO Info System to find whether RO already exists
        WebResource webResource = Client.create().resource(pdtUrl);
        ClientResponse response = webResource
                .path(context.getCollectionId())
                .accept("application/json")
                .type("application/json")
                .get(ClientResponse.class);

        if(response.getStatus() == 200){
            System.out.println(PersistROActivity.class.getName() + " : RO Validation Failed");
            throw new SeadWorkflowException("Error occurred while validating collection " + context.getCollectionId() +
             " - RO with same ID already exists in C3PR");
        }


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
        boolean validated = true;

        // Checking whether RO metadata includes Creator, Title and Abstract
        // These are the metadata required to generate FGDC
        JSONObject roObject = new JSONObject(roString);
        JSONObject object = (JSONObject)roObject.get(Constants.AGGREGATION);

        if(!object.has(Constants.CREATOR) || !nullCheck(object.get(Constants.CREATOR)) ||
                !object.has(Constants.TITLE) || !nullCheck(object.get(Constants.TITLE)) ||
                !object.has(Constants.ABSTRACT) || !nullCheck(object.get(Constants.ABSTRACT)) ) {
            validated = false;
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
            System.out.println(ValidateROActivity.class.getName() + " : Unable to validate RO since " + object + " is not either a String or an Array");
        }

        return isNotNull;
    }

    @Override
    public void rollback() {
    }
}
