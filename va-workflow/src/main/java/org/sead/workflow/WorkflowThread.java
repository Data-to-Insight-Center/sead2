package org.sead.workflow;

import org.sead.workflow.activity.SeadWorkflowActivity;
import org.sead.workflow.context.SeadWorkflowContext;
import org.sead.workflow.exception.SeadWorkflowException;
import org.sead.workflow.model.PSInstance;
import org.sead.workflow.util.Constants;
import org.seadva.services.statusTracker.SeadStatusTracker;
import org.seadva.services.statusTracker.enums.SeadStatus;


public class WorkflowThread extends Thread {

    String roId = null;
    String psId = null;
    SeadWorkflowContext context = null;

    public WorkflowThread(String roId, String psId, SeadWorkflowContext context) {
        this.roId = roId;
        this.psId = psId;
        this.context = context;
    }

    @Override
    public void run() {

        SeadStatusTracker.addStatus(context.getProperty(Constants.RO_ID), SeadStatus.WorkflowStatus.START.getValue());

        context.setCollectionId(roId); // set RO ID in workflow context

        for(PSInstance PSInstance : Constants.psInstances){
            // select the project space that invoked the 'Intent to Publish' method and set that in context
            if(PSInstance.getId() == Integer.parseInt(psId)) {
                context.setPSInstance(PSInstance);
                break;
            }
        }

        for (SeadWorkflowActivity activity : SeadWorkflowService.config.getActivities()) {
            // execute activities
            try {
                activity.execute(context, SeadWorkflowService.config);
            } catch (SeadWorkflowException e) {
                System.out.println("*** WorkflowThread : exception... ***");
                e.printStackTrace();
                //TODO : add error handling
                System.out.println("*** WorkflowThread : Breaking the MicroService loop ***");
                break;
            }
        }

        SeadStatusTracker.addStatus(context.getProperty(Constants.RO_ID), SeadStatus.WorkflowStatus.END.getValue());

    }
}