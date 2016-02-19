/*
 * Copyright 2015 University of Michigan, 2015 The Trustees of Indiana University
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
 * @author myersjd@umich.edu
 * @author isuriara@indiana.edu
 */

package org.sead.matchmaker.service;

import com.mongodb.BasicDBObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import org.bson.BasicBSONObject;
import org.bson.Document;
import org.bson.types.BasicBSONList;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sead.matchmaker.Matcher;
import org.sead.matchmaker.MatchmakerConstants;
import org.sead.matchmaker.RuleResult;
import org.sead.matchmaker.matchers.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Path("/ro")
public class MatchMaker {
	// set of matchers
	Set<Matcher> matchers = new HashSet<Matcher>();
	// common resource for all PDT calls
	WebResource pdtResource;

	public MatchMaker() {
		// Build list of Matchers
		matchers.add(new MaxDatasetSizeMatcher());
		matchers.add(new MaxTotalSizeMatcher());
		matchers.add(new DataTypeMatcher());
		matchers.add(new OrganizationMatcher());
		matchers.add(new DepthMatcher());
		matchers.add(new MinimalMetadataMatcher());
		matchers.add(new RightsHolderIDsRequiredMatcher());

		pdtResource = Client.create().resource(MatchmakerConstants.pdtUrl);
	}

	@POST
	@Path("/matchingrepositories")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response makeMatches(String matchRequest) {
		String messageString = null;
        System.out.println("=================");
        System.out.println("MM request : " + matchRequest);
        System.out.println("=================");
        Document request = Document.parse(matchRequest);
		Document content = (Document) request.get("Aggregation");
		if (content == null) {
			messageString += "Missing Aggregation";
		}
		Document preferences = (Document) request.get("Preferences");
		if (preferences == null) {
			messageString += "Missing Preferences";
		}
		Document stats = (Document) request.get("Aggregation Statistics");
		if (stats == null) {
			messageString += "Missing Statistics";
		}
		// May be a string or array, not necessarily a document like the other
		// elements
		Object rightsHolders = request.get("Rights Holder");
		if (rightsHolders == null) {
			messageString += "Missing Rights Holder(s)";
		}
		Object context = request.get("@context");
		if (context == null) {
			messageString += "Missing @context";
		}
        if(content.get("Creator") == null) {
            messageString += "Missing Creator";
        }

		if (messageString == null) {
			// Get organization from profile(s)
			// Add to base document
			Object creatorObject = content.get("Creator");

			BasicBSONList affiliations = new BasicBSONList();
			if (creatorObject instanceof ArrayList) {
				for (String creator : ((ArrayList<String>) creatorObject)) {
					Set<String> orgs = getOrganizationforPerson(creator);
					if (!orgs.isEmpty()) {
						affiliations.addAll(orgs);
					}
				}
			} else {
				// BasicDBObject - single value
				Set<String> orgs = getOrganizationforPerson((String) creatorObject);
				if (!orgs.isEmpty()) {
					affiliations.addAll(orgs);
				}
			}

			// Get repository profiles
			JSONArray reposJson = new JSONArray(
					pdtGET(MatchmakerConstants.PDT_REPOSITORIES));
			BasicBSONList matches = new BasicBSONList();

			for (int j = 0; j < reposJson.length(); j++) {
				JSONObject repoJson = reposJson.getJSONObject(j);
				String orgId = repoJson.get("orgidentifier").toString();
				// call PDT to get profile
				Document profile = Document
						.parse(pdtGET(MatchmakerConstants.PDT_REPOSITORIES
								+ "/" + orgId));

				BasicBSONObject repoMatch = new BasicBSONObject();
				repoMatch.put("orgidentifier", profile.get("orgidentifier"));
				repoMatch.put("repositoryName", profile.get("repositoryName"));

				BasicBSONList scores = new BasicBSONList();
				int total = 0;
				int i = 0;
				for (Matcher m : matchers) {
					BasicBSONObject individualScore = new BasicBSONObject();

					RuleResult result = m.runRule(content, rightsHolders,
							affiliations, preferences, stats, profile, context);

					individualScore.put("Rule Name", m.getName());
					if (result.wasTriggered()) {
						individualScore.put("Score", result.getScore());
						total += result.getScore();
						individualScore.put("Message", result.getMessage());
					} else {
						individualScore.put("Score", 0);
						individualScore.put("Message", "Not Used");
					}
					scores.put(i, individualScore);
					i++;
				}
				repoMatch.put("Per Rule Scores", scores);
				repoMatch.put("Total Score", total);
				matches.put(j, repoMatch);
			}
			// Assemble and send
			return Response.ok().entity(matches).build();
		} else {
			return Response.status(ClientResponse.Status.BAD_REQUEST)
					.entity(new BasicDBObject("Failure", messageString))
					.build();
		}
	}

	@GET
	@Path("/matchingrepositories/rules")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRulesList() {
		ArrayList<Document> rulesArrayList = new ArrayList<Document>();
		for (Matcher m : matchers) {
			rulesArrayList.add(m.getDescription());
		}
		return Response.ok().entity(rulesArrayList).build();
	}

	private Set<String> getOrganizationforPerson(String personID) {
		Set<String> orgs = new HashSet<String>();
		String encodedID;
		String personProfile = null;
		try {
			encodedID = URLEncoder.encode(personID, "UTF-8");

			// call PDT to get the profile using the ID
			personProfile = pdtGET(MatchmakerConstants.PDT_PEOPLE + "/"
					+ encodedID);
			if (personProfile == null) {
				// if the person doesn't exist, add
				pdtPOST(MatchmakerConstants.PDT_PEOPLE, "{\"identifier\":\""
						+ encodedID + "\"}");
				// now try to get the profile
				personProfile = pdtGET(MatchmakerConstants.PDT_PEOPLE + "/"
						+ encodedID);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (RuntimeException e){
            System.out.println("Error ocurred while registering person with ID :" + personID);
        }



		if (personProfile == null) {
			System.out.println("Can't identify the person: " + personID);
		} else {
			// find organization names of the person
			Document profileDocument = Document.parse(personProfile);
			orgs.add(profileDocument.getString("affiliation"));
		}
		return orgs;
	}

	private String pdtGET(String path) {
		ClientResponse response = pdtResource.path(path)
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON).get(ClientResponse.class);
		if (response.getStatus() == 200) {
			return response.getEntity(String.class);
		} else {
			return null;
		}
	}

	private void pdtPOST(String path, String message) {
		ClientResponse response = pdtResource.path(path)
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON)
				.post(ClientResponse.class, message);
		if (response.getStatus() == 500) {
			throw new RuntimeException("Error while POSTing profile");
		}
	}

}
