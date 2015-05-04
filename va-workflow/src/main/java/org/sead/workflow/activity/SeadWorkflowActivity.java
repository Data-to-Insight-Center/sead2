package org.sead.workflow.activity;

import org.sead.workflow.config.SeadWorkflowConfig;
import org.sead.workflow.context.SeadWorkflowContext;

/**
 * Workflow Activity interface
 */
public interface SeadWorkflowActivity {

    /**
     * Executes the activity logic
     */
    public void execute(SeadWorkflowContext context, SeadWorkflowConfig config);

    public void addParam(String key, String value);

    public void setName(String name);

}
