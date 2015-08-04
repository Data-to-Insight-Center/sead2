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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.bson.types.BasicBSONList;
import org.sead.cp.demo.Matcher;
import org.sead.cp.demo.RuleResult;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class DataTypeMatcher implements Matcher {

	public RuleResult runRule(Document content, String projectspace,
			BasicBSONList affiliations, Document preferences, Document profile) {
		RuleResult result = new RuleResult();
		Client client = Client.create();
		WebResource webResource;
		try {
			webResource = client.resource(projectspace
					+ "/resteasy/collections/"
					+ URLEncoder.encode(content.getString("Identifier"),
							"UTF-8") + "/stats");

			ClientResponse response = webResource.accept("application/json")
					.get(ClientResponse.class);

			if (response.getStatus() != 200) {
				throw new RuntimeException("" + response.getStatus());
			}

			Document statsDocument = Document.parse(response
					.getEntity(String.class));

			ArrayList<String> existingTypes = (ArrayList<String>) statsDocument
					.get("Data Mimetypes");
			ArrayList<String> requiredTypes = (ArrayList<String>) profile
					.get("Data Mimetypes");
			Set<String> forbiddenTypes = new HashSet<String>();
			for (String type : existingTypes) {
				if (!(requiredTypes.contains(type))) {
					forbiddenTypes.add(type);
				}
			}
			if (!forbiddenTypes.isEmpty()) {
				StringBuilder sBuilder = new StringBuilder();
				Iterator<String> iter = forbiddenTypes.iterator();
				sBuilder.append(iter.next());
				while (iter.hasNext()) {
					sBuilder.append(", " + iter.next());
				}
				result.setResult(-1, "Collection contains forbidden types ("
						+ sBuilder.toString() + ").");
			} else {
				result.setResult(1, "Collection only contains acceptable types");
			}
		} catch (NullPointerException npe) {
			// Just return untriggered result
			System.out.println("Missing info in MaximumDepth rule"
					+ npe.getLocalizedMessage());
		} catch (NumberFormatException nfe) {
			// Just return untriggered result
			System.out.println("Missing info in MaxDepth rule for repo: "
					+ profile.getString("orgidentifier") + " : "
					+ nfe.getLocalizedMessage());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;

	}

	public String getName() {
		return "Acceptable Data Types";
	}

	public Document getDescription() {
		return new Document("Rule Name", getName())
				.append("Repository Trigger",
						"\"Data Mimetypes\": \"http://purl.org/dc/elements/1.1/format\" : JSON array of String mimetypes, collection must be limited to this set");
	}
}
