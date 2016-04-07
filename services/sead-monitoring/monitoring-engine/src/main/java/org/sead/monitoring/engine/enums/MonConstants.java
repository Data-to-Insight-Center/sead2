/*
 *
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
 *
 * @author charmadu@umail.iu.edu
 */

package org.sead.monitoring.engine.enums;

public class MonConstants {

    public enum Components {
        CURBEE("curbee"),
        LANDING_PAGE("landingPage"),
        IU_SEAD_CLOUD_SEARCH("iuSeadCloudSearch"),
        MATCHMAKER("matchmaker");

        Components(String value) {
            this.value = value;
        }

        private String value;

        public String getValue() {
            return this.value;
        }
    }

    public enum Status {
        SUCCESS("success"),
        FAILURE("failure");

        Status(String value) {
            this.value = value;
        }

        private String value;

        public String getValue() {
            return this.value;
        }
    }

    public enum EventType {
        ACCESS("access"),
        DOWNLOAD("download");

        EventType(String value) {
            this.value = value;
        }

        private String value;

        public String getValue() {
            return this.value;
        }
    }
}
