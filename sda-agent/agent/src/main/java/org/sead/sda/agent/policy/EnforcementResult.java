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

package org.sead.sda.agent.policy;

public class EnforcementResult {

    private boolean isROAllowed;
    private String c3prUpdateMessage;

    public EnforcementResult(boolean isROAllowed, String c3prUpdateMessage) {
        this.isROAllowed = isROAllowed;
        this.c3prUpdateMessage = c3prUpdateMessage;
    }

    public boolean isROAllowed() {
        return isROAllowed;
    }

    public String getC3prUpdateMessage() {
        return c3prUpdateMessage;
    }

}
