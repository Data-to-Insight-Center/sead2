package org.seadpdt;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.seadpdt.util.Constants;

import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

@Path("/people")

public class PeopleServices {

    // move these to an external file?
    String collectionName = "people";
    String DBname = Constants.pdtDbName;

    MongoClient mongoClient = new MongoClient();
    MongoDatabase db = mongoClient.getDatabase(DBname);
    MongoCollection<Document> collection = db.getCollection(collectionName);
    CacheControl control = new CacheControl();

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerPerson(String personString) {

        JSONObject person = new JSONObject(personString);

        if (!person.has("identifier") || !person.has("provider")) {
            return Response.status(Status.BAD_REQUEST).entity(new BasicDBObject("Failure", "Invalid request format")).build();
        }

        String newID = (String) person.get("identifier");
        FindIterable<Document> iter = collection.find(new Document(
                "orcid-profile.orcid-identifier.path", newID));
        if (iter.iterator().hasNext()) {
            return Response.status(Status.CONFLICT).entity(new BasicDBObject("Failure", "Person with ORCID Identifier " + newID + " already exists")).build();
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
                collection.insertOne(Document.parse(profile));
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
        FindIterable<Document> iter = collection.find();
        iter.projection(new Document("orcid-profile.orcid-identifier.path", 1).append(
                "orcid-profile.orcid-bio.personal-details.given-names", 1).append("orcid-profile.orcid-bio.personal-details.family-name", 1).append("_id", 0));
        MongoCursor<Document> cursor = iter.iterator();
        JSONArray array = new JSONArray();
        while (cursor.hasNext()) {
            array.put(new JSONObject(cursor.next().toJson()));
        }
        return Response.ok(array.toString()).cacheControl(control).build();

    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPersonProfile(@PathParam("id") String id) {
        FindIterable<Document> iter = collection.find(new Document(
                "orcid-profile.orcid-identifier.path", id));
        if(iter.first() != null) {
            Document document = iter.first();
            document.remove("_id");
            return Response.ok(document.toJson()).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePersonProfile(@PathParam("id") String id) {
        FindIterable<Document> iter = collection.find(new Document(
                "orcid-profile.orcid-identifier.path", id));

        if (iter.iterator().hasNext()) {
            //String orcidID = (String) iter.first().get("orcid-profile.orcid-identifier.path");
            String profile = null;
            try {
                profile = getOrcidProfile(id);
            } catch (RuntimeException r) {
                return Response
                        .serverError()
                        .entity(new BasicDBObject("failure",
                                "Provider call failed with status: "
                                        + r.getMessage())).build();
            }


            collection.replaceOne(new Document(
                    "orcid-profile.orcid-identifier.path", id), Document.parse(profile));
            return Response.status(Status.OK).build();

        } else {
            return Response.status(Status.NOT_FOUND).build();

        }
    }

    @DELETE
    @Path("/{id}")
    public Response unregisterPerson(@PathParam("id") String id) {
        DeleteResult result = collection.deleteOne(new Document("orcid-profile.orcid-identifier.path", id));
        if(result.getDeletedCount() == 0 ){
            return Response.status(Status.NOT_FOUND).build();
        } else {
            return Response.status(Status.OK).build();
        }
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
