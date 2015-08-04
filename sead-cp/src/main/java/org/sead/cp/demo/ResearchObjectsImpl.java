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

package org.sead.cp.demo;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bson.BasicBSONObject;
import org.bson.Document;
import org.bson.types.BasicBSONList;
import org.json.JSONArray;
import org.sead.cp.ResearchObjects;
import org.sead.cp.demo.matchers.DataTypeMatcher;
import org.sead.cp.demo.matchers.DepthMatcher;
import org.sead.cp.demo.matchers.MaxDatasetSizeMatcher;
import org.sead.cp.demo.matchers.MaxTotalSizeMatcher;
import org.sead.cp.demo.matchers.MinimalMetadataMatcher;
import org.sead.cp.demo.matchers.OrganizationMatcher;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.util.JSON;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * See abstract base class for documentation of the rest api. Note - path
 * annotations must match base class for documentation to be correct.
 */

@Path("/researchobjects")
public class ResearchObjectsImpl extends ResearchObjects {
	private MongoClient mongoClient = null;
	private MongoDatabase db = null;
	private MongoCollection<Document> publicationsCollection = null;
	private MongoCollection<Document> peopleCollection = null;
	private CacheControl control = new CacheControl();
	Set<Matcher> matchers = new HashSet<Matcher>();

	public ResearchObjectsImpl() {
		mongoClient = new MongoClient();
		db = mongoClient.getDatabase("seadcp");

		publicationsCollection = db.getCollection("researchobjects");
		peopleCollection = db.getCollection("people");
		
		//Build list of Matchers
		
		
		matchers.add(new MaxDatasetSizeMatcher());
		matchers.add(new MaxTotalSizeMatcher());
		matchers.add(new DataTypeMatcher());
		matchers.add(new OrganizationMatcher());
		matchers.add(new DepthMatcher());
		matchers.add(new MinimalMetadataMatcher());

		control.setNoCache(true);
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response startROPublicationProcess(String publicationRequestString) {
		String messageString = null;
		Document request = Document.parse(publicationRequestString);
		Document content = (Document) request.get("Content");
		if (content == null) {
			messageString += "Missing Content";
		}
		Document preferences = (Document) request.get("Preferences");
		if (preferences == null) {
			messageString += "Missing Preferences";
		}
		Object repository = request.get("Repository");
		if (repository == null) {
			messageString += "Missing Respository";
		}
		if (messageString == null) {
			// Get organization from profile(s)
			// Add to base document
			Object creatorObject = content.get("Creator");
			String ID = (String) content.get("Identifier");
			BasicBSONList affiliations = new BasicBSONList();
			if (creatorObject instanceof ArrayList) {
				Iterator<String> iter = ((ArrayList<String>) creatorObject)
						.iterator();

				while (iter.hasNext()) {
					String creator = iter.next();
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

			request.append("Affiliations", affiliations);

			//Add first status message
			
			List<DBObject> statusreports = new ArrayList<DBObject>();
			DBObject status = BasicDBObjectBuilder
					.start()
					.add("date", System.currentTimeMillis())
					.add("reporter", "SEAD-CP")
					.add("stage", "Receipt Ackowledged")
					.add("message",
							"request recorded and processing will begin").get();
			statusreports.add(status);
			request.append("Status", statusreports);
			// Create initial status message - add
			// Add timestamp
			// Generate ID - by calling Workflow?
			// Add doc, return 201

			publicationsCollection.insertOne(request);
			URI resource = null;
			try {
				resource = new URI("./" + ID);
			} catch (URISyntaxException e) {
				// Should not happen given simple ids
				e.printStackTrace();
			}
			return Response.created(resource)
					.entity(new Document("identifier", ID)).build();
		} else {
			return Response.status(Status.BAD_REQUEST)
					.entity(new BasicDBObject("Failure", messageString))
					.build();
		}
	}

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getROsList() {
		FindIterable<Document> iter = publicationsCollection.find();
		iter.projection(new Document("Status", 1).append("Repository", 1)
				.append("Content.Identifier", 1).append("_id", 0));
		MongoCursor<Document> cursor = iter.iterator();
		JSONArray array = new JSONArray();
		while (cursor.hasNext()) {
			array.put(JSON.parse(cursor.next().toJson()));
		}
		return Response.ok(array.toString()).cacheControl(control).build();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getROProfile(@PathParam("id") String id) {

		FindIterable<Document> iter = publicationsCollection.find(new Document(
				"Content.Identifier", id));
		Document document = iter.first();
		document.remove("_id");
		return Response.ok(document.toJson()).cacheControl(control).build();
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setROStatus(@PathParam("id") String id, String state) {
		UpdateResult ur = publicationsCollection.updateOne(new Document(
				"Content.Identifier", id), new BasicDBObject("$push",
				new BasicDBObject("Status", Document.parse(state))));
		if (ur.wasAcknowledged()) {
			return Response.status(Status.OK).build();

		} else {
			return Response.status(Status.NOT_FOUND).build();

		}
	}


	@GET
	@Path("/{id}/status")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getROStatus(@PathParam("id") String id) {

		FindIterable<Document> iter = publicationsCollection.find(new Document(
				"Content.Identifier", id));
		iter.projection(new Document("Status", 1).append("_id", 0));

		Document document = iter.first();
		document.remove("_id");
		return Response.ok(document.toJson()).cacheControl(control).build();
	}

	@DELETE
	@Path("/{id}")
	public Response rescindROPublicationRequest(@PathParam("id") String id) {
		DeleteResult dr = publicationsCollection.deleteOne(new Document(
				"Content.Identifier", id));
		if (dr.getDeletedCount() == 1) {
			return Response.status(Status.OK).build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	private Set<String> getOrganizationforPerson(String personID) {
		Set<String> orgs = new HashSet<String>();;
		if (personID.startsWith("orcid.org/")) {
			personID = personID.substring("orcid.org/".length());
			FindIterable<Document> iter = peopleCollection.find(new Document(
					"orcid-profile.orcid-identifier.path", personID));
			iter.projection(new Document(
					"orcid-profile.orcid-activities.affiliations.affiliation.organization.name", 1).append(
					"_id", 0));
			MongoCursor<Document> cursor = iter.iterator();
			if (cursor.hasNext()) {
				Document affilDocument = cursor.next();
				Document profile = (Document) affilDocument.get("orcid-profile");
				
				
				Document activitiesDocument =  (Document) profile.get("orcid-activities");
				
				
				Document affiliationsDocument = (Document) activitiesDocument.get("affiliations");
				
				ArrayList orgList = (ArrayList) affiliationsDocument.get("affiliation");
				System.out.println(orgList.size());
				for(Object entry: orgList) {
					Document org = (Document) ((Document)entry).get("organization");
					orgs.add((String)org.getString("name"));
				}
			}
			/*
			 * JSONArray array = new JSONArray(); while(cursor.hasNext()) {
			 * array.put(JSON.parse(cursor.next().toJson())); }
			 */

		}
		return orgs;

	}

	@POST
	@Path("/matchingrepositories")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response makeMatches(String matchRequest) {
		String messageString = null;
		Document request = Document.parse(matchRequest);
		Document content = (Document) request.get("Content");
		if (content == null) {
			messageString += "Missing Content";
		}
		Document preferences = (Document) request.get("Preferences");
		if (preferences == null) {
			messageString += "Missing Preferences";
		}
		String projectspace = (String) request.get("Project Space");
		if (projectspace == null) {
			messageString += "Missing Preferences";
		}
		if (messageString == null) {
			// Get organization from profile(s)
			// Add to base document
			Object creatorObject = content.get("Creator");
			String ID = (String) content.get("Identifier");

			BasicBSONList affiliations = new BasicBSONList();
			if (creatorObject instanceof ArrayList) {
				Iterator<String> iter = ((ArrayList<String>) creatorObject)
						.iterator();

				while (iter.hasNext()) {
					String creator = iter.next();
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

			
			//Get repository profiles
			FindIterable<Document> iter = db.getCollection("repositories").find();
			//iter.projection(new Document("_id", 0));
			

			
			//Create result lists per repository
			//Run matchers
			MongoCursor<Document> cursor = iter.iterator();

			BasicBSONList matches = new BasicBSONList();
			
			int j=0;
			while (cursor.hasNext()) {
				
				BasicBSONObject repoMatch = new BasicBSONObject();
				Document profile = cursor.next();
				
				repoMatch.put("orgidentifier", profile.get("orgidentifier"));
				
				BasicBSONList scores = new BasicBSONList();
				int total = 0;
				int i = 0;
				for(Matcher m: matchers) {
					BasicBSONObject individualScore = new BasicBSONObject();
					
					RuleResult result = m.runRule(content, projectspace, affiliations, preferences, profile);
					
					individualScore.put("Rule Name",m.getName());
					if(result.wasTriggered()) {
					individualScore.put("Score", result.getScore());
					total+=result.getScore();
					individualScore.put("Message",result.getMessage());
 					} else {
 						individualScore.put("Score", 0);
 						individualScore.put("Message","Not Used"); 							
 					}
					scores.put(i, individualScore);
					i++;
				}
				repoMatch.put("Per Rule Scores",  scores);
				repoMatch.put("Total Score", total);
				matches.put(j, repoMatch);
				j++;
			}
			//Assemble and send
			
			

			return Response.ok().entity(matches).build();
		} else {
			return Response.status(Status.BAD_REQUEST)
					.entity(new BasicDBObject("Failure", messageString))
					.build();
		}
	}

	@GET
	@Path("/matchingrepositories/rules")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRulesList() {
		ArrayList<Document> rulesArrayList = new ArrayList<Document>();
		for(Matcher m: matchers) {
			rulesArrayList.add(m.getDescription());
		}
		return Response.ok().entity(rulesArrayList).build();
	}

}
