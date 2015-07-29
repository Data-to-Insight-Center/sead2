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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bson.Document;
import org.codehaus.enunciate.Facet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.sead.cp.People;
import org.sead.cp.Repositories;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.util.JSON;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * See abstract base class for documentation of the rest api. Note - path
 * annotations must match base class for documentation to be correct.
 */

@Path("/people")
public class PeopleImpl extends People {
	private MongoClient mongoClient = null;
	private MongoDatabase db = null;
	private MongoCollection<Document> peopleCollection = null;

	public PeopleImpl() {
		mongoClient = new MongoClient();
		db = mongoClient.getDatabase("seadcp");

		peopleCollection = db.getCollection("people");
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response registerPerson(String personString) {

		BasicDBObject person = (BasicDBObject) JSON.parse(personString);

		String newID = (String) person.get("identifier");
		FindIterable<Document> iter = peopleCollection.find(new Document(
				"identifier", newID));
		if (iter.iterator().hasNext()) {
			return Response.status(Status.CONFLICT).build();
		} else {
			if (person.get("provider").equals("ORCID")) {
				String orcidID = (String) person.get("identifier");
				String profile = null;
				try {
					profile = getOrcidProfile(orcidID);
				} catch (RuntimeException r) {
					return Response
							.serverError()
							.entity(new BasicDBObject("failure",
									"Provider call failed with status: "
											+ r.getMessage())).build();
				}
				peopleCollection.insertOne(Document.parse(profile));
				URI resource = null;
				try {
					resource = new URI("./" + newID);
				} catch (URISyntaxException e) {
					// Should not happen given simple ids
					e.printStackTrace();
				}
				return Response.created(resource)
						.entity(new Document("identifier", newID)).build();
			} else {
				return Response.status(Status.BAD_REQUEST).entity(new BasicDBObject("Failure", "Provider " + person.get("provider") + " not supported")).build();
			}
		}
	}

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPeopleList() {
		FindIterable<Document> iter = peopleCollection.find();
		iter.projection(new Document("orcid-profile.orcid-identifier.path", 1).append(
				"orcid-profile.orcid-bio.personal-details.given-names", 1).append("orcid-profile.orcid-bio.personal-details.family-name", 1).append("_id", 0));
		MongoCursor<Document> cursor = iter.iterator();
		JSONArray array = new JSONArray();
		while (cursor.hasNext()) {
			array.put(cursor.next().toJson());
		}
		return Response.ok(array.toString()).build();

	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPersonProfile(@PathParam("id") String id) {
		FindIterable<Document> iter = peopleCollection.find(new Document(
				"orcid-profile.orcid-identifier.path", id));
		Document document = iter.first();
		document.remove("_id");
		return Response.ok(document.toJson()).build();
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updatePersonProfile(@PathParam("id") String id) {
		FindIterable<Document> iter = peopleCollection.find(new Document(
				"orcid-profile.orcid-identifier.path", id));

		if (iter.iterator().hasNext()) {
			String orcidID = (String) iter.first().get("orcid-profile.orcid-identifier.path");
			String profile = null;
			try {
				profile = getOrcidProfile(orcidID);
			} catch (RuntimeException r) {
				return Response
						.serverError()
						.entity(new BasicDBObject("failure",
								"Provider call failed with status: "
										+ r.getMessage())).build();
			}
		
			
			UpdateResult ur = peopleCollection.replaceOne(new Document(
					"orcid-profile.orcid-identifier.path", id), Document.parse(profile));
			return Response.status(Status.OK).build();

		} else {
			return Response.status(Status.NOT_FOUND).build();

		}
	}

	@DELETE
	@Path("/{id}")
	public Response unregisterPerson(@PathParam("id") String id) {
		peopleCollection.deleteOne(new Document("orcid-profile.orcid-identifier.path", id));
		return Response.status(Status.OK).build();
	}



	private String getOrcidProfile(String id) {

		Client client = Client.create();
		WebResource webResource = client.resource("http://pub.orcid.org/v1.1/"
				+ id + "/orcid-profile");

		ClientResponse response = webResource.accept("application/orcid+json")
				.get(ClientResponse.class);

		if (response.getStatus() != 200) {
			throw new RuntimeException("" + response.getStatus());
		}

		return response.getEntity(String.class);
	}

}
