package org.seadpdt;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.sun.jersey.api.client.ClientResponse.Status;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.seadpdt.util.Constants;
import org.seadpdt.util.MongoDB;

import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

@Path("/repositories")
public class RepoServices {

    private MongoDatabase db = null;
    private MongoCollection<Document> repositoriesCollection = null;
    private CacheControl control = new CacheControl();

    public RepoServices() {
        db = MongoDB.getServicesDB();
        repositoriesCollection = db.getCollection(MongoDB.repositories);
        control.setNoCache(true);
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerRepository(String profileString) {
        JSONObject profile = new JSONObject(profileString);

        if (!profile.has("orgidentifier")) {
            return Response.status(Status.BAD_REQUEST).entity(new BasicDBObject("Failure", "Invalid request format: " +
                    "Request must contain the field \"orgidentifier\"")).build();
        }

        String newID = (String) profile.get("orgidentifier");
        FindIterable<Document> iter = repositoriesCollection.find(new Document(
                "orgidentifier", newID));
        if (iter.iterator().hasNext()) {
            return Response.status(Status.CONFLICT).entity(new BasicDBObject("Failure", "Repository with Identifier "
                    + newID + " already exists")).build();
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
        iter.projection(new Document("orgidentifier", 1).append(
                "repositoryURL", 1).append(
                "repositoryName", 1).append(
                "lastUpdate", 1).append("_id", 0));
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
    public Response getRepositoryProfile(@PathParam("id") String id) {
        FindIterable<Document> iter = repositoriesCollection.find(new Document(
                "orgidentifier", id));
        if(iter.first() != null) {
            Document document = iter.first();
            document.remove("_id");
            return Response.ok(document.toJson()).cacheControl(control).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
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
            repositoriesCollection.replaceOne(new Document("orgidentifier", id), document);
            return Response.status(Status.OK).build();

        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response unregisterRepository(@PathParam("id") String id) {
        DeleteResult result = repositoriesCollection.deleteOne(new Document("orgidentifier", id));
        if(result.getDeletedCount() == 0 ){
            return Response.status(Status.NOT_FOUND).build();
        } else {
            return Response.status(Status.OK).build();
        }
    }

    @GET
    @Path("/{id}/researchobjects")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getROsByRepository(@PathParam("id") String id) {
        MongoCollection<Document> publicationsCollection = null;
        publicationsCollection = db.getCollection(MongoDB.researchObjects);
        FindIterable<Document> iter = publicationsCollection.find(new Document(
                "Repository", id));
        iter.projection(new Document("Aggregation.Identifier", 1).append("Aggregation.Title", 1)
                .append("Repository", 1).append("Status", 1).append("_id", 0));
        MongoCursor<Document> cursor = iter.iterator();
        Set<Document> array = new HashSet<Document>();
        while (cursor.hasNext()) {
            array.add(cursor.next());
        }
        return Response.ok(array).cacheControl(control).build();
    }

}
