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
package org.sead.api.impl.matchers;

import org.bson.Document;
import org.bson.types.BasicBSONList;
import org.sead.api.impl.Matcher;
import org.sead.api.impl.RuleResult;

public class MinimalMetadataMatcher implements Matcher {

	public RuleResult runRule(Document aggregation, 
			BasicBSONList affiliations, Document preferences, Document statsDocument, Document profile) {

		return new RuleResult();
	}

	public String getName() {
		return "Minimal Metadata";
	}

	public Document getDescription() {
		return new Document("Rule Name", getName())
				.append("Repository Trigger",
						"\"Metadata Terms\": \"http://sead-data.net/terms/terms\" : JSON array of String predicates, Not yet implemented");
	}

}
