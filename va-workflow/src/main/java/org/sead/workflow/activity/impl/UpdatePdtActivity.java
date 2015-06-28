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

/**
 * Responsible for updating PDT with the published RO details
 */
public class UpdatePdtActivity extends AbstractWorkflowActivity {
    
    @Override
    public void execute(SeadWorkflowContext context, SeadWorkflowConfig config) {
        System.out.println("\n=====================================");
        System.out.println("Executing activity : " + activityName);
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
        String pdtSystemUrl = activityParams.get("pdtSystemUrl");        
        
		Client client = Client.create();
		 
		// need to pull some key value pairs from ro to pass
		
		WebResource webResource = client
		   .resource(pdtSystemUrl + "/harvest/publishRO?" + "{\"response\": \"success\", \"message\" : \"ID_1234\"}");
 
		ClientResponse response = webResource.accept("application/xml")
                   .get(ClientResponse.class);        
        
        System.out.println("\n=====================================");
        System.out.println("return status : " + response);
        System.out.println("-----------------------------------\n");           
        
    }
}
