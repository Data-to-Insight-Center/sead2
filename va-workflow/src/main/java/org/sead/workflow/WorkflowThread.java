package org.sead.workflow;

import org.sead.workflow.activity.SeadWorkflowActivity;
import org.sead.workflow.context.SeadWorkflowContext;
import org.sead.workflow.model.PSInstance;
import org.sead.workflow.util.Constants;

import java.util.concurrent.Semaphore;


public class WorkflowThread extends Thread {

    Semaphore semaphore = null;
    String roId = null;
    String psId = null;
    SeadWorkflowContext context = null;
    boolean signalPS; // This flag is set to true after WorkflowThread signals the main thread

    public WorkflowThread(Semaphore semaphore, String roId, String psId, SeadWorkflowContext context) {
        this.semaphore = semaphore;
        this.roId = roId;
        this.psId = psId;
        this.context = context;
        signalPS = false;
    }

    @Override
    public void run() {

        context.setCollectionId(roId); // set RO ID in workflow context
        context.addProperty(Constants.SIGNAL_PS, Constants.FALSE); // set SIGNAL_PS flag to false

        for(PSInstance PSInstance : Constants.psInstances){
            // select the project space that invoked the 'Intent to Publish' method and set that in context
            if(PSInstance.getId() == Integer.parseInt(psId)) {
                context.setPSInstance(PSInstance);
                break;
            }
        }

        for (SeadWorkflowActivity activity : SeadWorkflowService.config.getActivities()) {
            // execute activities
            activity.execute(context, SeadWorkflowService.config);

            if(context.getProperty(Constants.SIGNAL_PS).equals(Constants.TRUE) && !signalPS){
                // if SIGNAL_PS is set to true by any activity, signal the main thread and set signalPS to true
                signalPS = true;
                System.out.println("Thread: release semaphore");
                semaphore.release();
            }
        }


    }
}