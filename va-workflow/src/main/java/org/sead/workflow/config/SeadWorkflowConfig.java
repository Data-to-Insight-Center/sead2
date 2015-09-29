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
 * @author isuriara@indiana.edu
 */

package org.sead.workflow.config;

import org.sead.workflow.activity.SeadWorkflowActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Holds the Workflow configuration. This class should be populated by reading the
 * sead-wf.xml config file
 */
public class SeadWorkflowConfig {

    // set of config level parameters
    private HashMap<String, String> workflowParams = new HashMap<String, String>();
    // list of activities
    private List<SeadWorkflowActivity> activities = new ArrayList<SeadWorkflowActivity>();

    public void addParam(String key, String value) {
        workflowParams.put(key, value);
    }

    public String getParamValue(String key) {
        return workflowParams.get(key);
    }

    public void addActivity(SeadWorkflowActivity activity) {
        activities.add(activity);
    }

    public List<SeadWorkflowActivity> getActivities() {
        return activities;
    }

}
