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

package org.sead.cp.demo.matchers;

import java.util.ArrayList;
import java.util.Iterator;

import javax.ws.rs.core.NewCookie;

import org.bson.BsonArray;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.types.BasicBSONList;
import org.sead.cp.demo.Matcher;
import org.sead.cp.demo.RuleResult;

public class OrganizationMatcher implements Matcher {

	@SuppressWarnings("unchecked")
	public RuleResult runRule(Document aggregation, BasicBSONList affiliations,
			Document preferences, Document statsDocument, Document profile) {
		RuleResult result = new RuleResult();
		try {
			//Get required affiliations from profile
			ArrayList<String> requiredAffiliations = (ArrayList<String>) profile
					.get("Affiliations");
			//Add asserted affiliations to the derived ones
			affiliations.addAll((ArrayList<String>) preferences.get("Affiliations"));
			boolean affiliated = false;
			String requiredOrgString = null;
			for (String org : affiliations.toArray(new String[0])) {
				if ((requiredAffiliations.contains(org))) {
					affiliated = true;
					requiredOrgString = org;
					break;
				}
			}
			if (!affiliated) {
				StringBuilder sBuilder = new StringBuilder();
				Iterator<String> iter = requiredAffiliations.iterator();
				sBuilder.append((String) iter.next());
				while (iter.hasNext()) {
					sBuilder.append(", " + (String) iter.next());
				}
				result.setResult(-1,
						"Collection does not have an affiliation with a required organization ("
								+ sBuilder.toString() + ").");
			} else {
				result.setResult(1, "Collection has required affiliation: "
						+ requiredOrgString);
			}
		} catch (NullPointerException npe) {
			// Just return untriggered result
			System.out.println("Missing info in Organization Match rule"
					+ npe.getLocalizedMessage());
		}
		return result;

	}

	public String getName() {
		return "Organization Match";
	}

	public Document getDescription() {
		BsonArray triggersArray = new BsonArray();
		triggersArray
				.add(new BsonString(
						"\"Affiliations\": \"http://sead-data.net/terms/affiliations\" : derived from profiles of the dcTerms creators"));
		triggersArray
				.add(new BsonString(
						"\"Affiliations\": \"http://sead-data.net/terms/affiliations\" : may also be provided as an assertion within the Preferences: \"http://sead-data.net/terms/publicationpreferences\" object"));
		return new Document("Rule Name", getName())
				.append("Repository Trigger",
						"\"Affiliations\": \"http://sead-data.net/terms/affiliations\" :"
								+ " JSON array of String organization names, at least one must match exactly")
				.append("Publication Trigger", triggersArray);

	}

}
