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
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import org.json.JSONException;
import org.json.JSONObject;
import org.sead.workflow.activity.AbstractWorkflowActivity;
import org.sead.workflow.activity.SeadWorkflowActivity;
import org.sead.workflow.config.SeadWorkflowConfig;
import org.sead.workflow.context.SeadWorkflowContext;
import org.sead.workflow.exception.SeadWorkflowException;
import org.sead.workflow.util.Constants;

import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Responsible for persisting the RO in RO subsystem
 */
public class PersistOREActivity extends AbstractWorkflowActivity {

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

        String ro = context.getProperty(Constants.JSON_RO);
        String metadatagenUrl = activityParams.get("metadatagenUrl");

        // Call RO Info System to persist the JSONLD RO
        WebResource webResource = Client.create().resource(metadatagenUrl);
        ClientResponse response = webResource
                .path("rest/oremap")
                .queryParam("requestUrl", URLEncoder.encode(context.getProperty(Constants.REQUEST_URL)))
                .accept("application/json")
                .type("application/json")
                .post(ClientResponse.class, ro);

        if(response.getStatus() == 200 || response.getStatus() == 201){
            String oreId = null;
            try {
                oreId = new JSONObject(response.getEntity(String.class)).get("id").toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            context.addProperty(Constants.ORE_ID, oreId);
            System.out.println(PersistOREActivity.class.getName() + " : ORE successfully saved in DB, ORE ID - " + oreId);
        } else if(response.getStatus() == 400) {
            throw new SeadWorkflowException("Error occurred while persisting ORE " + context.getCollectionId()
                    + " - " + response.getEntity(new GenericType<String>() {}).toString());
        } else {
            throw new SeadWorkflowException("Error occurred while persisting ORE " + context.getCollectionId());
        }

        System.out.println("=====================================\n");

    }

    @Override
    public void rollback(SeadWorkflowContext context, SeadWorkflowConfig config) {
        HashMap<String, String> activityParams = new HashMap<String, String>();
        for(SeadWorkflowActivity activity : config.getActivities()){
            AbstractWorkflowActivity abstractActivity = (AbstractWorkflowActivity)activity;
            if(abstractActivity.activityName.equals(activityName)){
                activityParams = abstractActivity.params;
                break;
            }
        }

        String pdtUrl = activityParams.get("pdtUrl");
        WebResource webResource = Client.create().resource(pdtUrl);
        ClientResponse response = webResource
                .path(context.getProperty(Constants.ORE_ID)+ "/oremap")
                .accept("application/json")
                .type("application/json")
                .delete(ClientResponse.class);
    }

}
