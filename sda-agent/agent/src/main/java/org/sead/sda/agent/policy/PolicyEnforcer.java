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

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.json.simple.JSONObject;

public class PolicyEnforcer {

    private static final String PROJECT_ID = "projectIdentifier";
    private static final String ALLOCATED_SIZE = "allocatedSize";

    // better to read these from some config file??
    private static final long PER_PROJECT_LIMIT_BYTES = 2000000000000L;
    private static final long TOTAL_RO_SIZE_LIMIT_BYTES = 1000000000L;
    private static final int MAX_COLLECTION_DEPTH = 4;
    // list of minimal metadata fields
    private static final String[] MINIMAL_METADATA = new String[]{"Creator", "Date", "Title", "Publishing Project"};

    static public PolicyEnforcer policyEnforcerInstance = null;
    private MongoCollection<Document> projectsCollection = null;

    private PolicyEnforcer() {
        MongoDatabase db = MongoDB.getAgentDB();
        projectsCollection = db.getCollection(MongoDB.projects);
    }

    public static synchronized PolicyEnforcer getInstance() {
        if (policyEnforcerInstance == null) {
            try {
                policyEnforcerInstance = new PolicyEnforcer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return policyEnforcerInstance;
    }

    public EnforcementResult isROAllowed(JSONObject ro) {
        // TODO: use a json-ld library instead of using hardcoded labels
        JSONObject stats = (JSONObject) ro.get("Aggregation Statistics");
        JSONObject aggregation = (JSONObject) ro.get("Aggregation");
        if (stats != null && aggregation != null) {
            String project = (String) aggregation.get("Publishing Project");
            String totalSize  = (String) stats.get("Total Size");
            String collectionDepth  = (String) stats.get("Max Collection Depth");
            EnforcementResult result = enforceTotalSize(totalSize);
            if (!result.isROAllowed()) {
                return result;
            }
            result = enforceMaxCollectionDepth(collectionDepth);
            if (!result.isROAllowed()) {
                return result;
            }
            result = enforceMinimalMetadata(aggregation);
            if (!result.isROAllowed()) {
                return result;
            }
            result = enforcePerProjectLimit(project, totalSize);
            if (!result.isROAllowed()) {
                return result;
            }
            return new EnforcementResult(true, "All Good.");
        } else {
            return new EnforcementResult(false, "Aggregation Statistics or Aggregation element " +
                    "not found in the publication request.");
        }
    }

    private EnforcementResult enforcePerProjectLimit(String projectId, String roSizeString) {
        if (projectId == null || roSizeString == null) {
            return new EnforcementResult(false, "Publishing Project or Total Size not found in " +
                    "the publication request.");
        }
        long roSize = Long.parseLong(roSizeString);
        FindIterable<Document> iter = projectsCollection.find(new Document(PROJECT_ID, projectId));
        if (iter.iterator().hasNext()) {
            Document project = iter.first();
            // already allocated size
            long allocated = Long.parseLong((String) project.get(ALLOCATED_SIZE));
            // remaining space
            long remainder = PER_PROJECT_LIMIT_BYTES - allocated;
            if (roSize > remainder) {
                return new EnforcementResult(false, "Per-project limit rule violated. Space left in SDA for project " +
                        projectId + " is " + remainder + " bytes.");
            } else {
                projectsCollection.updateOne(new Document(PROJECT_ID, projectId),
                        new Document("$set", new Document(ALLOCATED_SIZE, String.valueOf(allocated + roSize))));
            }
        } else {
            projectsCollection.insertOne(Document.parse("{\"projectIdentifier\":\"" + projectId +
                    "\", \"allocatedSize\":\"" + roSize + "\"}"));
        }
        return new EnforcementResult(true, "Per-project limit not violated.");
    }

    private EnforcementResult enforceTotalSize(String totalSizeString) {
        if (totalSizeString == null) {
            return new EnforcementResult(false, "Total Size not found in the publication request.");
        }
        long totalROSize = Long.parseLong(totalSizeString);
        if (totalROSize <= TOTAL_RO_SIZE_LIMIT_BYTES) {
            return new EnforcementResult(true, "RO Size limit not violated.");
        } else {
            return new EnforcementResult(false, "RO Size limit violated. Max allowed size bytes: " +
                    TOTAL_RO_SIZE_LIMIT_BYTES);
        }
    }

    private EnforcementResult enforceMaxCollectionDepth(String depthString) {
        if (depthString == null) {
            return new EnforcementResult(false, "Max Collection Depth not found in the publication request.");
        }
        long roDepth = Long.parseLong(depthString);
        if (roDepth <= MAX_COLLECTION_DEPTH) {
            return new EnforcementResult(true, "Max Collection depth rule not violated.");
        } else {
            return new EnforcementResult(false, "Max Collection depth violated. Max allowed depth: " +
                    MAX_COLLECTION_DEPTH);
        }
    }

    private EnforcementResult enforceMinimalMetadata(JSONObject aggregation) {
        for (String meta: MINIMAL_METADATA) {
            if (aggregation.get(meta) == null) {
                return new EnforcementResult(false, "Minimal Metadata rule violated. " + meta + " not " +
                        "found in the publication request.");
            }
        }
        return new EnforcementResult(true, "Minimal Metadata rule not violated.");
    }

}
