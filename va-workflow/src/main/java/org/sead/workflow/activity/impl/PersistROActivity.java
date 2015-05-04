package org.sead.workflow.activity.impl;

import org.sead.workflow.activity.AbstractWorkflowActivity;
import org.sead.workflow.config.SeadWorkflowConfig;
import org.sead.workflow.context.SeadWorkflowContext;

/**
 * Responsible for persisting the RO in RO subsystem
 */
public class PersistROActivity extends AbstractWorkflowActivity {

    @Override
    public void execute(SeadWorkflowContext context, SeadWorkflowConfig config) {
        System.out.println("Executing activity : " + activityName);
    }

}
