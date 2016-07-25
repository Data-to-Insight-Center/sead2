/*
 *
 * Copyright 2016 University of Michigan
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

import java.util.ArrayList;

import org.bson.Document;
import org.bson.types.BasicBSONList;
import org.sead.matchmaker.Matcher;
import org.sead.matchmaker.RuleResult;

public class PurposeMatcher implements Matcher {

	@SuppressWarnings("unchecked")
	public RuleResult runRule(Document aggregation, Object rightsHolders,
			BasicBSONList affiliations, Document preferences,
			Document statsDocument, Document profile, Object context) {
		RuleResult result = new RuleResult();
		try {
			// Get required affiliations from profile
			ArrayList<String> requiredPurposes = (ArrayList<String>) profile
					.get("Purpose");
			// Add asserted purpose
			String purpose = preferences.getString("Purpose");
			//Backward compatibility
			if(purpose==null) {
				purpose = "Production";
			}
			boolean matchedPurpose = false;

			if ((requiredPurposes.contains(purpose))) {
				matchedPurpose = true;
			}
			if (!matchedPurpose) {
				result.setResult(-1, "Repository does not support " + purpose
						+ " publications.");
			} else {
				result.setResult(1, "Repository supports " + purpose
						+ " publication.");
			}
		} catch (NullPointerException npe) {
			// Just return untriggered result
			System.out.println("Missing info in Purpose Match rule"
					+ npe.getLocalizedMessage());
		}
		result.setMandatory(true);
		return result;
	}

	public String getName() {
		return "Purpose Match";
	}

	public Document getDescription() {
		ArrayList<String> triggersArray = new ArrayList<String>();
		triggersArray
				.add(" \"Purpose\": \"http://sead-data.net/vocab/publishing#Purpose\" : a Preference that can be \"Testing-Only\" or \"Production\"");
		return new Document("Rule Name", getName())
				.append("Repository Trigger",
						" \"Purpose\": \"http://sead-data.net/vocab/publishing#Purpose\" : \"Testing-Only\", \"Production\", or an array with both, at the top level. Rule is mandatory.")
				.append("Publication Trigger", triggersArray);
	}
}
