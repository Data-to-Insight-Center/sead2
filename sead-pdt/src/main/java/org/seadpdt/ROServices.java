/*
 *
 * Copyright 2015 The Trustees of Indiana University, 2015 University of Michigan
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
 * @author myersjd@umich.edu
 * @author smccaula@indiana.edu
 */

package org.seadpdt;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.util.JSON;
import com.sun.jersey.api.client.ClientResponse;

import org.bson.Document;
import org.bson.types.BasicBSONList;
import org.bson.types.ObjectId;
import org.json.JSONArray;

import org.seadpdt.people.Profile;
import org.seadpdt.people.Provider;
import org.json.JSONObject;
import org.seadpdt.util.MongoDB;

import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Date;
import java.text.DateFormat;
import java.util.*;

@Path("/researchobjects")
public class ROServices {

	private MongoDatabase db = null;
	private MongoDatabase metaDb = null;
	private MongoCollection<Document> publicationsCollection = null;
	private MongoCollection<Document> repositoriesCollection = null;
	private MongoCollection<Document> oreMapCollection = null;
	private CacheControl control = new CacheControl();

	public ROServices() {
		db = MongoDB.getServicesDB();
		metaDb = MongoDB.geMetaGenDB();
		publicationsCollection = db.getCollection(MongoDB.researchObjects);
		db.getCollection(MongoDB.people);
		repositoriesCollection = db.getCollection(MongoDB.repositories);
		oreMapCollection = metaDb.getCollection(MongoDB.oreMap);
		control.setNoCache(true);
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response startROPublicationProcess(String publicationRequestString,
			@QueryParam("requestUrl") String requestURL,
            @QueryParam("oreId") String oreId) {
		String messageString = "";
		Document request = Document.parse(publicationRequestString);
		Document content = (Document) request.get("Aggregation");
		if (content == null) {
			messageString += "Missing Aggregation ";
		}
		Document preferences = (Document) request.get("Preferences");
		if (preferences == null) {
			messageString += "Missing Preferences ";
		}
		Object repository = request.get("Repository");
		if (repository == null) {
			messageString += "Missing Respository ";
		} else {
			FindIterable<Document> iter = repositoriesCollection
					.find(new Document("orgidentifier", repository));
			if (iter.first() == null) {
				messageString += "Unknown Repository: " + repository + " ";
			}

		}

		if (messageString.equals("")) {
			// Get organization from profile(s)
			// Add to base document
			Object creatorObject = content.get("Creator");
			String ID = (String) content.get("Identifier");
			BasicBSONList affiliations = new BasicBSONList();
			if (creatorObject != null) {
				if (creatorObject instanceof ArrayList) {
					Iterator<String> iter = ((ArrayList<String>) creatorObject)
							.iterator();

					while (iter.hasNext()) {
						String creator = iter.next();
						List<String> orgs = getOrganizationforPerson(creator);
						if (!orgs.isEmpty()) {
							affiliations.addAll(orgs);
						}
					}

				} else {
					// BasicDBObject - single value
					List<String> orgs = getOrganizationforPerson((String) creatorObject);
					if (!orgs.isEmpty()) {
						affiliations.addAll(orgs);
					}
				}
			}

			request.append("Affiliations", affiliations);

			// Add first status message

			List<DBObject> statusreports = new ArrayList<DBObject>();
			DBObject status = BasicDBObjectBuilder
					.start()
					.add("date",
							DateFormat.getDateTimeInstance().format(
									new Date(System.currentTimeMillis())))
					.add("reporter", "SEAD-CP")
					.add("stage", "Receipt Acknowledged")
					.add("message",
							"request recorded and processing will begin").get();
			statusreports.add(status);
			request.append("Status", statusreports);
			// Create initial status message - add
			// Add timestamp
			// Generate ID - by calling Workflow?
			// Add doc, return 201

			String newMapURL = requestURL + "/" + ID + "/oremap";
			content.put("@id", newMapURL + "#aggregation");
            content.put("authoratativeMap", oreId);

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
			return Response.status(ClientResponse.Status.BAD_REQUEST)
					.entity(messageString).build();
		}
	}

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getROsList() {
		FindIterable<Document> iter = publicationsCollection.find();
		iter.projection(new Document("Status", 1).append("Repository", 1)
				.append("Aggregation.Identifier", 1)
				.append("Aggregation.Title", 1).append("_id", 0));
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
				"Aggregation.Identifier", id));
		if (iter == null) {
			return Response.status(ClientResponse.Status.NOT_FOUND).build();
		}
		Document document = iter.first();
		if (document == null) {
			return Response.status(ClientResponse.Status.NOT_FOUND).build();
		}
		// Internal meaning only - strip from exported doc
		document.remove("_id");

		Document aggDocument = (Document) document.get("Aggregation");
		aggDocument.remove("authoratativeMap");
		return Response.ok(document.toJson()).cacheControl(control).build();
	}

    @GET
    @Path("/{id}/authoratativeMap")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAuthoratativeMap(@PathParam("id") String id) {

        FindIterable<Document> iter = publicationsCollection.find(new Document(
                "Aggregation.Identifier", id));
        if (iter == null) {
            return Response.status(ClientResponse.Status.NOT_FOUND).build();
        }
        Document document = iter.first();
        if (document == null) {
            return Response.status(ClientResponse.Status.NOT_FOUND).build();
        }
        // Internal meaning only - strip from exported doc
        String mapId = (String) ((Document)document.get("Aggregation")).get("authoratativeMap");
        return Response.ok(new JSONObject().put("mapId", mapId).toString()).cacheControl(control).build();
    }

	@POST
	@Path("/{id}/status")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setROStatus(@PathParam("id") String id, String state) {
		try {
			Document statusUpdateDocument = Document.parse(state);
			statusUpdateDocument.append(
					"date",
					DateFormat.getDateTimeInstance().format(
							new Date(System.currentTimeMillis())));
			UpdateResult ur = publicationsCollection.updateOne(new Document(
					"Aggregation.Identifier", id), new BasicDBObject("$push",
					new BasicDBObject("Status", statusUpdateDocument)));
			if (ur.wasAcknowledged()) {
				return Response.status(ClientResponse.Status.OK).build();

			} else {
				return Response.status(ClientResponse.Status.NOT_FOUND).build();

			}
		} catch (org.bson.BsonInvalidOperationException e) {
			return Response.status(ClientResponse.Status.BAD_REQUEST).build();
		}
	}

	@GET
	@Path("/{id}/status")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getROStatus(@PathParam("id") String id) {
		FindIterable<Document> iter = publicationsCollection.find(new Document(
				"Aggregation.Identifier", id));
		iter.projection(new Document("Status", 1).append("_id", 0));

		Document document = iter.first();
		document.remove("_id");
		return Response.ok(document.toJson()).cacheControl(control).build();
	}

	@DELETE
	@Path("/{id}")
	public Response rescindROPublicationRequest(@PathParam("id") String id) {
		// Is there ever a reason to preserve the map and not the pub request?
		// FixMe: Don't allow a delete after the request is complete?

		// First remove map
		FindIterable<Document> iter = publicationsCollection.find(new Document(
				"Aggregation.Identifier", id));
		iter.projection(new Document("Aggregation", 1).append("_id", 0));

		Document document = iter.first();
		if (document == null) {
			return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND)
					.build();
		}

        ObjectId mapId =  new ObjectId(((Document)document.get("Aggregation")).get("authoratativeMap").toString());
        DeleteResult mapDeleteResult = oreMapCollection.deleteOne(new Document("_id", mapId));
		/*DeleteResult mapDeleteResult = oreMapCollection.deleteOne(new Document(
				"describes.Identifier", id));*/
		if (mapDeleteResult.getDeletedCount() != 1) {
			// Report error
			System.out.println("Could not find map corresponding to " + id);
		}

		DeleteResult dr = publicationsCollection.deleteOne(new Document(
				"Aggregation.Identifier", id));
		if (dr.getDeletedCount() == 1) {
			return Response.status(ClientResponse.Status.OK).build();
		} else {
			return Response.status(ClientResponse.Status.NOT_FOUND).build();
		}
	}

	private List<String> getOrganizationforPerson(String pID) {

		List<String> orgs = new ArrayList<String>();

		Profile profile = Provider.findCanonicalId(pID);
		// If null, no chance that we have a profile...
		if (profile != null) {
			Document profileDoc = PeopleServices.retrieveProfile(profile
					.getIdentifier());

			// NeverFail
			if (profileDoc == null) {
				// Handle per provider
				PeopleServices.registerPerson("{\"provider\": \""
						+ profile.getProvider() + "\", \"identifier\":\""
						+ profile.getIdentifier() + "\" }");
			}
			profileDoc = PeopleServices.retrieveProfile(profile.getIdentifier());

			if (profileDoc != null) {
				String currentAffiliations = profileDoc
						.getString("affiliation");
				orgs = Arrays.asList(currentAffiliations.split("\\s*,\\s*"));
			}
		}

		return orgs;
	}

}
