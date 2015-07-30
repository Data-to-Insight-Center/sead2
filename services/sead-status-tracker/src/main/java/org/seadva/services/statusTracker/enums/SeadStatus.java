package org.seadva.services.statusTracker.enums;

public class SeadStatus {

    static int globalID = 0;

    static int fetchAddID(){
        return globalID++;
    }

    public enum Components {
        Workflow,
        PDT,
        Matchmaker;
    }

    public enum WorkflowStatus {
        START("WF:1", fetchAddID()),
        CONVERT_RO_BEGIN("WF:2:BEGIN", fetchAddID()),
        CONVERT_RO_END("WF:2:END", fetchAddID()),
        PERSIST_RO_BEGIN("WF:3:BEGIN", fetchAddID()),
        PERSIST_RO_END("WF:3:END", fetchAddID()),
        VALIDATE_RO_BEGIN("WF:4:BEGIN", fetchAddID()),
        VALIDATE_RO_END("WF:4:END", fetchAddID()),
        UPDATE_RO_STATE_BEGIN("WF:5:BEGIN", fetchAddID()),
        UPDATE_RO_STATE_END("WF:5:END", fetchAddID()),
        UPDATE_PDT_BEGIN("WF:6:BEGIN", fetchAddID()),
        UPDATE_PDT_END("WF:6:END", fetchAddID()),
        PUBLISH_RO_BEGIN("WF:7:BEGIN", fetchAddID()),
        PUBLISH_RO_END("WF:7:END", fetchAddID()),
        END("WF:8", fetchAddID());

        private String value;
        private int id;

        WorkflowStatus(String value, int id) {
            this.value = value;
            this.id = id;
        }

        public int toIdx(){
            return id;
        }

        public String getValue() {
            return this.value;
        }
    }

    public enum PDTStatus {
        START("PDT:1", fetchAddID()),
        END("PDT:2", fetchAddID());

        private String value;
        private int id;

        PDTStatus(String value, int id) {
            this.value = value;
            this.id = id;
        }

        public int toIdx(){
            return id;
        }

        public String getValue() {
            return this.value;
        }
    }

    public enum MatchmakerStatus {
        START("MM:1", fetchAddID()),
        MM_ACTIVITY1_BEGIN("MM:2:BEGIN", fetchAddID()),
        MM_ACTIVITY1_END("MM:2:END", fetchAddID()),
        END("MM:3", fetchAddID());

        private String value;
        private int id;

        MatchmakerStatus(String value, int id) {
            this.value = value;
            this.id = id;
        }

        public int toIdx(){
            return id;
        }

        public String getValue() {
            return this.value;
        }
    }
}
