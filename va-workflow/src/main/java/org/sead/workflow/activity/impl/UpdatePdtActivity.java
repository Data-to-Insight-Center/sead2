package org.sead.workflow.activity.impl;

import java.util.HashMap;

import org.sead.workflow.activity.AbstractWorkflowActivity;
import org.sead.workflow.activity.SeadWorkflowActivity;
import org.sead.workflow.config.SeadWorkflowConfig;
import org.sead.workflow.context.SeadWorkflowContext;
import org.sead.workflow.util.Constants;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.seadva.services.statusTracker.SeadStatusTracker;
import org.seadva.services.statusTracker.enums.SeadStatus;

/**
 * Responsible for updating PDT with the published RO details
 */
public class UpdatePdtActivity extends AbstractWorkflowActivity {
    
    @Override
    public void execute(SeadWorkflowContext context, SeadWorkflowConfig config) {
        System.out.println("\n=====================================");
        System.out.println("Executing activity : " + activityName);
        System.out.println("-----------------------------------\n");

        SeadStatusTracker.addStatus(context.getProperty(Constants.RO_ID), SeadStatus.WorkflowStatus.UPDATE_PDT_BEGIN.getValue());

        HashMap<String, String> activityParams = new HashMap<String, String>();
        for(SeadWorkflowActivity activity : config.getActivities()){
            AbstractWorkflowActivity abstractActivity = (AbstractWorkflowActivity)activity;
            if(abstractActivity.activityName.equals(activityName)){
                activityParams = abstractActivity.params;
                break;
            }
        }

        String ro = context.getProperty(Constants.JSON_RO);
        String pdtSystemUrl = activityParams.get("pdtSystemUrl");  
        
        // for testing, one hard coded piece from ro, needs to be parsed properly
        String testJSON = 
        		"{\"@context\": {\"Identifier\": \"http://purl.org/dc/terms/identifier\"},\"Identifier\": \"tag:medici@uiuc.edu,2009:col_SqW5XUYSfhZ627ghxBfaMQ\"}";
              		    
	        WebResource webResource = Client.create().resource(pdtSystemUrl);
	        ClientResponse response = webResource.path("harvest")
	                .path("publishRO")
	                .accept("application/json")
	                .type("application/json")
//	                .post(ClientResponse.class, ro);			
	                .post(ClientResponse.class, testJSON);			
			
			
        System.out.println("\n=====================================");
        System.out.println("return status : " + response);
        System.out.println("-----------------------------------\n");

        SeadStatusTracker.addStatus(context.getProperty(Constants.RO_ID), SeadStatus.WorkflowStatus.UPDATE_PDT_END.getValue());


    }
}
