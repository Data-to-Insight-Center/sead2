package org.sead.workflow.activity.impl;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.sead.workflow.activity.AbstractWorkflowActivity;
import org.sead.workflow.activity.SeadWorkflowActivity;
import org.sead.workflow.config.SeadWorkflowConfig;
import org.sead.workflow.context.SeadWorkflowContext;
import org.sead.workflow.exception.SeadWorkflowException;
import org.sead.workflow.util.Constants;

import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Responsible for generating metadata standards like ORE, SIP etc.
 */
public class UpdateROStatusActivity extends AbstractWorkflowActivity {

    @Override
    public void execute(SeadWorkflowContext context, SeadWorkflowConfig config) {
        System.out.println("\n=====================================");
        System.out.println("Executing activity : " + activityName);
        System.out.println("-----------------------------------\n");

        if(context.getProperty(Constants.VALIDATED).equals(Constants.FALSE)){
            System.out.println(UpdateROStatusActivity.class.getName() + " : Not Updating RO status");
            return;
        }

        HashMap<String, String> activityParams = new HashMap<String, String>();
        for(SeadWorkflowActivity activity : config.getActivities()){
            AbstractWorkflowActivity abstractActivity = (AbstractWorkflowActivity)activity;
            if(abstractActivity.activityName.equals(activityName)){
                activityParams = abstractActivity.params;
                break;
            }
        }

        String roSystemUrl = activityParams.get("roSystemUrl");

        // Call RO Info System to update RO status
        WebResource webResource = Client.create()
                .resource(roSystemUrl + "/resource/updateROStatus/")
                .queryParam("entityId",URLEncoder.encode(context.getProperty(Constants.RO_ID)))
                .queryParam("status", "PublishedObject");

        ClientResponse response = webResource
                .post(ClientResponse.class);

        if(response.getStatus() == 200){
            System.out.println(UpdateROStatusActivity.class.getName() + " : Successfully updated RO status");
        } else {
            System.out.println(UpdateROStatusActivity.class.getName() + " : Failed to updated RO status");
            throw new SeadWorkflowException("Error occurred while updating status of the collection "
                    + context.getCollectionId() + " to PO");
        }

        System.out.println("=====================================\n");


    }
}
