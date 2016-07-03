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
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import com.mongodb.util.JSON;
import com.sun.jersey.api.client.ClientResponse;

import org.bson.Document;
import org.bson.types.BasicBSONList;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.seadpdt.people.Profile;
import org.seadpdt.people.Provider;
import org.json.JSONException;
import org.json.JSONObject;
import org.seadpdt.util.Constants;
import org.seadpdt.util.MongoDB;

import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Date;
import java.text.DateFormat;
import java.util.*;

@Path("/researchobjects")
public class ROServices {

	private MongoDatabase db = null;
	private MongoDatabase metaDb = null;
	private DB oreDb = null;
	private MongoCollection<Document> publicationsCollection = null;
	private MongoCollection<Document> repositoriesCollection = null;
	private MongoCollection<Document> oreMapCollection = null;
	private GridFS oreMapBucket = null;
	private MongoCollection<Document> fgdcCollection = null;
	private CacheControl control = new CacheControl();

	public ROServices() {
		db = MongoDB.getServicesDB();
		metaDb = MongoDB.geMetaGenDB();
		oreDb = MongoDB.geOreDB();
		publicationsCollection = db.getCollection(MongoDB.researchObjects);

		repositoriesCollection = db.getCollection(MongoDB.repositories);
		oreMapCollection = metaDb.getCollection(MongoDB.oreMap);
		oreMapBucket = new GridFS(oreDb, MongoDB.oreMap);
        fgdcCollection = metaDb.getCollection(MongoDB.fgdc);
		control.setNoCache(true);
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response startROPublicationProcess(String publicationRequestString,
			@QueryParam("requestUrl") String requestURL,
            @QueryParam("oreId") String oreId) throws URISyntaxException {
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
					.add("reporter", Constants.serviceName)
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
            URI uri = new URI(newMapURL);
            uri = uri.normalize();
			content.put("@id", uri.toString() + "#aggregation");
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
					.entity(messageString)
					.build();
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
	@Path("/new/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNewROsList() {
		//Find ROs that have a status not from the services and don't include them :-)
		Document reporterRule = new Document("$ne", Constants.serviceName);
		Document reporter = new Document("reporter", reporterRule);
		Document elem = new Document("$elemMatch", reporter);
		Document not = new Document("$not", elem);
		Document match= new Document("Status", not);

		FindIterable<Document> iter = publicationsCollection.find(match);
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
			return Response
                    .status(ClientResponse.Status.NOT_FOUND)
                    .entity(new JSONObject().put("Error", "Cannot find RO with id " + id).toString())
                    .build();
		}
		Document document = iter.first();
		if (document == null) {
			return Response
                    .status(ClientResponse.Status.NOT_FOUND)
                    .entity(new JSONObject().put("Error", "Cannot find RO with id " + id).toString())
                    .build();
		}
		// Internal meaning only - strip from exported doc
		document.remove("_id");

		Document aggDocument = (Document) document.get("Aggregation");
		aggDocument.remove("authoratativeMap");
		return Response.ok(document.toJson()).cacheControl(control).build();
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

		// First remove map
		FindIterable<Document> iter = publicationsCollection.find(new Document(
				"Aggregation.Identifier", id));
        Document document = iter.first();

        if(document != null) {
            boolean processing = false;
            ArrayList Statuses = (ArrayList) document.get("Status");
            for (Object status : Statuses) {
                Document statusObj = (Document) status;
                String stage = statusObj.get("stage").toString();
                if (stage.equalsIgnoreCase("Success") || stage.equalsIgnoreCase("Pending")) {
                    processing = true;
                    break;
                }
            }
            Document preferences = (Document)document.get("Preferences");
            String purpose = null;
            if(preferences != null){
                purpose = (String)preferences.get("Purpose");
            }
            if(Constants.deleteTestRO) {
                if(( purpose == null || !purpose.equalsIgnoreCase("Testing-Only")) && processing) {
                    // if server is configured to delete test ROs then still don't delete ROs that are not flagged as "Testing-Only" and in processing/deposited stage
                    return Response.status(ClientResponse.Status.BAD_REQUEST)
                            .entity("Cannot revoke the request since the repository is either processing or has deposited the requested RO")
                            .build();
                }
            } else {
                if (processing) {
                    // if server is configured not to delete test ROs then don't delete all ROs in processing/deposit stage
                    return Response.status(ClientResponse.Status.BAD_REQUEST)
                            .entity("Cannot revoke the request since the repository is either processing or has deposited the requested RO")
                            .build();
                }
            }
        }

		iter.projection(new Document("Aggregation", 1).append("_id", 0));

		document = iter.first();
		if (document == null) {
			return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND)
                    .entity("RO with ID " + id + " does not exist")
                    .build();
		}

        ObjectId mapId =  new ObjectId(((Document)document.get("Aggregation")).get("authoratativeMap").toString());
        oreMapBucket.remove(mapId);
		/*DeleteResult mapDeleteResult = oreMapCollection.deleteOne(new Document(
				"describes.Identifier", id));*/
		//if (mapDeleteResult.getDeletedCount() != 1) {
			// Report error
			//System.out.println("Could not find map corresponding to " + id);
		//}

		DeleteResult dr = publicationsCollection.deleteOne(new Document(
				"Aggregation.Identifier", id));
		if (dr.getDeletedCount() == 1) {
			return Response.status(ClientResponse.Status.OK)
                    .entity("RO Successfully Deleted")
                    .build();
		} else {
            return Response.status(ClientResponse.Status.NOT_FOUND)
                    .entity("RO with ID " + id + " does not exist")
                    .build();
		}
	}

    @DELETE
    @Path("/{id}/override")
    public Response DeleteOverrideRO(@PathParam("id") String id) {

        // First remove map
        FindIterable<Document> iter = publicationsCollection.find(new Document(
                "Aggregation.Identifier", id));
        iter.projection(new Document("Aggregation", 1).append("_id", 0));

        Document document = iter.first();
        if (document == null) {
            return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND)
                    .entity("RO with ID " + id + " does not exist")
                    .build();
        }

        ObjectId mapId =  new ObjectId(((Document)document.get("Aggregation")).get("authoratativeMap").toString());
        oreMapBucket.remove(mapId);
        DeleteResult dr = publicationsCollection.deleteOne(new Document(
                "Aggregation.Identifier", id));
        if (dr.getDeletedCount() == 1) {
            return Response.status(ClientResponse.Status.OK)
                    .entity("RO Successfully Deleted")
                    .build();
        } else {
            return Response.status(ClientResponse.Status.NOT_FOUND)
                    .entity("RO with ID " + id + " does not exist")
                    .build();
        }
    }

    @POST
    @Path("/oremap")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addOreMap(@QueryParam("objectId") String id, String oreMapString){
        ObjectId objectId = new ObjectId(id);
        String fileName = "ore-file";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(oreMapString.getBytes());
        GridFSInputFile gfsFile = oreMapBucket.createFile(inputStream, fileName, true);
        gfsFile.setId(objectId);
        gfsFile.save();
        return Response.ok().build();
    }

    @GET
    @Path("/{id}/oremap")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getROOREMap(@PathParam("id") String id) throws JSONException, IOException {

        FindIterable<Document> iter = publicationsCollection.find(new Document(
                "Aggregation.Identifier", id));

        if(iter == null || iter.first() == null) {
            return Response
                    .status(javax.ws.rs.core.Response.Status.NOT_FOUND)
                    .entity(new JSONObject().put("Error", "Cannot find RO with id " + id).toString())
                    .build();
        }

        Document document = iter.first();
        ObjectId mapId =  new ObjectId(((Document)document.get("Aggregation")).get("authoratativeMap").toString());

        //FindIterable<Document> oreIter = oreMapCollection.find(new Document("_id", mapId));
        //Document map = oreIter.first();
        GridFSDBFile dbFile = oreMapBucket.findOne(mapId);
        if(dbFile==null) {
            return Response
                    .status(javax.ws.rs.core.Response.Status.NOT_FOUND)
                    .entity(new JSONObject().put("Error", "Cannot find ORE with id " + id).toString())
                    .build();
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        dbFile.writeTo(bos);
        Document map = Document.parse(bos.toString());

        //Internal meaning only
        map.remove("_id");
        //document.remove("_id");

        return Response.ok(map.toJson()).build();
    }

    @DELETE
    @Path("/{id}/oremap")
    public Response deleteOreByDocumentId(@PathParam("id") String id) {
        oreMapBucket.remove(new ObjectId(id));
        return Response.status(ClientResponse.Status.OK).build();
    }

    @POST
    @Path("/{id}/fgdc")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response addFgdc(String fgdcString, @PathParam("id") String id){
        fgdcCollection.deleteMany(new Document("@id", id));
        Document document = new Document();
        document.put("@id", id);
        document.put("metadata", fgdcString);
        fgdcCollection.insertOne(document);
        return Response.ok().build();
    }

    @GET
    @Path("/{id}/fgdc")
    @Produces(MediaType.APPLICATION_XML)
    public Response getFgdc(@PathParam("id") String id) {
        FindIterable<Document> iter = fgdcCollection.find(new Document("@id", id));
        if(iter != null && iter.first() != null){
            return Response.ok(iter.first().get("metadata").toString()).build();
        } else {
            return Response.status(ClientResponse.Status.NOT_FOUND).build();
        }
    }

    //If pid resolves to a published research object, return that RO ID
    @GET
    @Path("/pid/{pid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoOfPID(@PathParam("pid") String pid) {

        BasicDBObject statusObj = new BasicDBObject();
        statusObj.put("stage", "Success");
        statusObj.put("message", pid);
        BasicDBObject elemMatch = new BasicDBObject();
        elemMatch.put("$elemMatch", statusObj);
        BasicDBObject query = new BasicDBObject("Status", elemMatch);

        FindIterable<Document> iter = publicationsCollection.find(query);
        iter.projection(new Document("Aggregation.Identifier", 1).append("_id", 0));
        if(iter != null && iter.first() != null){
            return Response.ok("{\"roId\" : \"" + ((Document)iter.first().get("Aggregation")).get("Identifier").toString() + "\" }").build();
        } else {
            return Response.status(ClientResponse.Status.NOT_FOUND).build();
        }
    }

    //Deprecate oldRO by newRO. Delete the old RO request and OREMap
    @GET
    @Path("/deprecate/{newRO}/{oldRO}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deprecateRO(@PathParam("newRO") String newRoId,
                                @PathParam("oldRO") String oldRoId) {

        try {
            BasicDBObject metaObject = new BasicDBObject(Constants.alternateOf, Constants.alternateOfIRI);
            UpdateResult urContext = publicationsCollection.updateOne(
                    new Document("Aggregation.Identifier", newRoId),
                    new BasicDBObject("$push", new BasicDBObject("@context", metaObject)));
            UpdateResult urAggregation = publicationsCollection.updateOne(
                    new Document("Aggregation.Identifier", newRoId),
                    new BasicDBObject("$set", new BasicDBObject("Aggregation." + Constants.alternateOf, oldRoId)));
            if (urContext.wasAcknowledged() && urAggregation.wasAcknowledged()) {
                DeleteOverrideRO(oldRoId);
                return Response.status(ClientResponse.Status.OK).build();
            } else {
                return Response.status(ClientResponse.Status.NOT_FOUND).build();

            }
        } catch (org.bson.BsonInvalidOperationException e) {
            return Response.status(ClientResponse.Status.BAD_REQUEST).build();
        }
    }

    //This is a management method used to copy oreMaps from main mongoDB to the GridFS DB
    @PUT
    @Path("/copyoremaps")
    public Response copyOreMaps(){
        FindIterable<Document> iter = oreMapCollection.find();
        MongoCursor<Document> cursor = iter.iterator();
        while (cursor.hasNext()) {
            Document document = cursor.next();
            ObjectId objectId = new ObjectId((String) document.get("_id").toString());
            document.remove("_id");
            String fileName = "ore-file";
            ByteArrayInputStream inputStream = new ByteArrayInputStream(document.toJson().getBytes());
            GridFSInputFile gfsFile = oreMapBucket.createFile(inputStream, fileName, true);
            gfsFile.setId(objectId);
            gfsFile.save();
        }
        return Response.ok().build();
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
                if(currentAffiliations != null) {
                    orgs = Arrays.asList(currentAffiliations.split("\\s*,\\s*"));
                }
			}
		}

		return orgs;
	}

}
