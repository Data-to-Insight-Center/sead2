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

package org.sead.workflow.activity;

import org.sead.workflow.config.SeadWorkflowConfig;
import org.sead.workflow.context.SeadWorkflowContext;

import java.util.HashMap;

public abstract class AbstractWorkflowActivity implements SeadWorkflowActivity {

    // name of the activity
    public String activityName = null;
    // parameter map for the activity
    public HashMap<String, String> params = new HashMap<String, String>();

    @Override
    public abstract void execute(SeadWorkflowContext context, SeadWorkflowConfig config);

    public void addParam(String key, String value) {
        params.put(key, value);
    }

    public void setName(String name) {
        activityName = name;
    }
}
