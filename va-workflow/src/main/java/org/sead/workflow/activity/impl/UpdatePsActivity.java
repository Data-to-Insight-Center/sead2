package org.sead.workflow.activity.impl;

import org.sead.workflow.activity.AbstractWorkflowActivity;
import org.sead.workflow.config.SeadWorkflowConfig;
import org.sead.workflow.context.SeadWorkflowContext;

/**
 * Responsible for updating Project Spaces after publishing an RO
 */
public class UpdatePsActivity extends AbstractWorkflowActivity {

    @Override
    public void execute(SeadWorkflowContext context, SeadWorkflowConfig config) {
        System.out.println("Executing activity : " + activityName);
    }
}
