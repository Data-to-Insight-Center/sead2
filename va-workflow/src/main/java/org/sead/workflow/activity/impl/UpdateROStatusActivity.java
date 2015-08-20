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
import org.seadva.services.statusTracker.SeadStatusTracker;
import org.seadva.services.statusTracker.enums.SeadStatus;

import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Responsible for generating metadata standards like ORE, SIP etc.
 */
public class UpdateROStatusActivity extends AbstractWorkflowActivity {

    @Override
    public void execute(SeadWorkflowContext context, SeadWorkflowConfig config) {

        if(context.getProperty(Constants.VALIDATED).equals(Constants.FALSE)){
            System.out.println(UpdateROStatusActivity.class.getName() + " : Not Updating RO state");
            return;
        }

        System.out.println("\n=====================================");
        System.out.println("Executing MicroService : " + activityName);
        System.out.println("-----------------------------------\n");

        SeadStatusTracker.addStatus(context.getProperty(Constants.RO_ID), SeadStatus.WorkflowStatus.UPDATE_RO_STATE_BEGIN.getValue());

        HashMap<String, String> activityParams = new HashMap<String, String>();
        for(SeadWorkflowActivity activity : config.getActivities()){
            AbstractWorkflowActivity abstractActivity = (AbstractWorkflowActivity)activity;
            if(abstractActivity.activityName.equals(activityName)){
                activityParams = abstractActivity.params;
                break;
            }
        }

        String roSystemUrl = activityParams.get("roSystemUrl");

        // Call RO Info System to update RO state
        WebResource webResource = Client.create()
                .resource(roSystemUrl + "/resource/updateROState/")
                .queryParam("entityId",URLEncoder.encode(context.getProperty(Constants.RO_ID)))
                .queryParam("state", "PublishedObject");

        ClientResponse response = webResource
                .post(ClientResponse.class);

        if(response.getStatus() == 200){
            System.out.println(UpdateROStatusActivity.class.getName() + " : Successfully updated RO state");
        } else {
            System.out.println(UpdateROStatusActivity.class.getName() + " : Failed to updated RO state");
            throw new SeadWorkflowException("Error occurred while updating state of the collection "
                    + context.getCollectionId() + " to PO");
        }

        SeadStatusTracker.addStatus(context.getProperty(Constants.RO_ID), SeadStatus.WorkflowStatus.UPDATE_RO_STATE_END.getValue());

        System.out.println("=====================================\n");


    }
}
