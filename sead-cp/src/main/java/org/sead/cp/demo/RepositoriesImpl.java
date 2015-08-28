package org.sead.cp.demo;

import java.net.URI;
import java.net.URISyntaxException;

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

import org.bson.Document;
import org.codehaus.enunciate.Facet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sead.cp.Repositories;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.util.JSON;
import com.sun.jersey.api.client.ClientResponse.Status;


/** See abstract base class for documentation of the rest api.
 *  Note - path annotations must match base class for documentation to be correct. */

@Path("/repositories")
public class RepositoriesImpl extends Repositories {
	private MongoClient mongoClient = null;
	private MongoDatabase db = null;
	private MongoCollection<Document> repositoriesCollection = null;
	private CacheControl control = new CacheControl();

	public RepositoriesImpl() {
		mongoClient = new MongoClient();
		db = mongoClient.getDatabase("seadcp");

		repositoriesCollection = db.getCollection("repositories");
		
		control.setNoCache(true);
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response registerRepository(String profileString) {
		BasicDBObject profile = (BasicDBObject) JSON.parse(profileString);
		String newID = (String) profile.get("orgidentifier");
		FindIterable<Document> iter = repositoriesCollection.find(new Document(
				"orgidentifier", newID));
		if (iter.iterator().hasNext()) {
			return Response.status(Status.CONFLICT).build();
		} else {
			repositoriesCollection
					.insertOne(Document.parse(profile.toString()));
			URI resource = null;
			try {
				resource = new URI("./" + newID);
			} catch (URISyntaxException e) {
				// Should not happen given simple ids
				e.printStackTrace();
			}
			return Response.created(resource).entity(new Document("orgidentifier", newID)).build();
		}
	}

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRepositoryList() {
		FindIterable<Document> iter = repositoriesCollection.find();
		iter.projection(new Document("orgidentifier", 1).append("repositoryURL", 1).append("_id", 0));
		MongoCursor<Document> cursor = iter.iterator();
		JSONArray array = new JSONArray();
		while(cursor.hasNext()) {
			array.put(cursor.next().toJson());
		}
		return Response.ok(array.toString()).cacheControl(control).build();

	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRepositoryProfile(@PathParam("id") String id) {
		FindIterable<Document> iter = repositoriesCollection.find(new Document(
				"orgidentifier", id));
		Document document = iter.first();
		document.remove("_id");
		return Response.ok(document.toJson()).cacheControl(control).build();
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setRepositoryProfile(@PathParam("id") String id,
			String profile) {
		FindIterable<Document> iter = repositoriesCollection.find(new Document(
				"orgidentifier", id));
		
		if (iter.iterator().hasNext()) {

		Document document = Document.parse(profile);
		UpdateResult ur = repositoriesCollection.replaceOne(new Document("orgidentifier", id),document);
		return Response.status(Status.OK).build();

		} else {
			return Response.status(Status.NOT_FOUND).build();
			
		}
	}

	@DELETE
	@Path("/{id}")
	public Response unregisterRepository(@PathParam("id") String id) {
		repositoriesCollection.deleteOne(new Document("orgidentifier", id));
		return Response.status(Status.OK).build();
	}

	@GET
	@Path("/researchobjects")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getROsByRepository() {
		return Response.status(Status.NOT_IMPLEMENTED).cacheControl(control).build();
	};

}