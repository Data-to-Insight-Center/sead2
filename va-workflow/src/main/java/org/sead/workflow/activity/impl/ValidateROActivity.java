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

import org.json.JSONException;
import org.sead.workflow.activity.AbstractWorkflowActivity;
import org.sead.workflow.config.SeadWorkflowConfig;
import org.sead.workflow.context.SeadWorkflowContext;
import org.sead.workflow.exception.SeadWorkflowException;
import org.sead.workflow.util.Constants;

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
        // TODO: Implement Validation
        return true;
    }
}
