/*
 *
 * Copyright 2015 University of Michigan
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
 *
 * @author myersjd@umich.edu
 */

package org.sead.matchmaker.matchers;

import org.bson.Document;
import org.bson.types.BasicBSONList;
import org.sead.matchmaker.Matcher;
import org.sead.matchmaker.RuleResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MinimalMetadataMatcher implements Matcher {

    public RuleResult runRule(Document aggregation, BasicBSONList affiliations,
                              Document preferences, Document statsDocument, Document profile,
                              Object context) {
        RuleResult result = new RuleResult();
        ArrayList preds = (ArrayList) profile.get("Metadata Terms");
        if (preds == null) {
            return result;
        }
        // Create map to look for metadata
        HashMap<String, String> labelsByPred = new HashMap<String, String>();
        if (context == null) {
            return result;
        }
        if (context instanceof List) {
            Iterator<Document> docIter = (Iterator<Document>) ((List) context)
                    .listIterator();
            while (docIter.hasNext()) {
                Object next = docIter.next();
                if (next != null) {
                    Document doc = (Document) next;
                    for (String k : doc.keySet()) {
                        labelsByPred.put(doc.getString(k), k);
                    }
                }
            }
        } else if (context instanceof Document) {
            Document doc = (Document) context;
            for (String k : doc.keySet()) {
                labelsByPred.put(doc.getString(k), k);
            }
        }
        StringBuffer missing = new StringBuffer();
        for (Object pred : preds) {
            String label = labelsByPred.get(pred);
            // No label for pred or no value for that label == missing
            if ((label == null) || (aggregation.get(label) == null)) {
                if (missing.length() != 0) {
                    missing.append(", ");
                }
                missing.append(pred);
            }
        }
        if (missing.length() == 0) {
            result.setResult(1, "All required metadata exists");
        } else {
            result.setResult(-1,
                    "Required metadata is missing: " + missing.toString());
        }

        return result;
    }

    public String getName() {
        return "Minimal Metadata";
    }

    public Document getDescription() {
        return new Document("Rule Name", getName())
                .append("Repository Trigger",
                        " \"Metadata Terms\": \"http://sead-data.net/terms/terms\" : JSON array of String predicates")
                .append("Publication Trigger",
                        " Predicates matching repository profile : existence of predicates in \"Aggregation\" in publication request");
    }

}