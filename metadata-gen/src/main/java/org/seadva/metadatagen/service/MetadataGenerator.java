package org.seadva.metadatagen.service;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.filter.ClientFilter;
import org.bson.Document;
import org.bson.types.BasicBSONList;
import org.bson.types.ObjectId;
import org.seadva.metadatagen.metagen.impl.FGDCMetadataGen;
import org.seadva.metadatagen.metagen.impl.OREMetadataGen;
import org.seadva.metadatagen.util.Constants;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Root resource (exposed at "rest" path)
 */
@Path("rest")
public class MetadataGenerator {

    static MongoCollection<Document> oreMapCollection;
    static MongoClient mongoClient;
    static MongoDatabase db;

    static {
        mongoClient = new MongoClient();
        db = mongoClient.getDatabase(Constants.metagenDbName);
        oreMapCollection = db.getCollection(Constants.dbOreCollection);
    }


    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Path("/getMetadata")
    @Produces(MediaType.APPLICATION_XML)
    public Response getIt(@QueryParam("entityId") String entityId,
                          @QueryParam("type") String metadataType) throws URISyntaxException {

        String errorMsg ="<error>\n" +
                "<description>EntityId and type(ORE/SIP/FGDC) are required query parameters. Please specify.</description>\n" +
                "<traceInformation>\n" +
                "method: metadata-gen.getMetadata \n" +
                "</traceInformation>\n" +
                "</error>";

        if(entityId == null || metadataType == null){
            return Response.status(Response.Status.BAD_REQUEST).entity(errorMsg).type(MediaType.APPLICATION_XML).build();
        }

        String response = "";

        if(metadataType.equalsIgnoreCase("ORE")) {
            OREMetadataGen oreMetadataGen = new OREMetadataGen();
            response = oreMetadataGen.generateMetadata(entityId);
        } else if(metadataType.equalsIgnoreCase("FGDC")){
            FGDCMetadataGen fgdcMetadataGen = new FGDCMetadataGen();
            response = fgdcMetadataGen.generateMetadata(entityId);
        }

        return Response.ok(response
        ).build();
    }

    @POST
    @Path("/putoremap")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response putOreMap(String publicationRequestString, @QueryParam("requestUrl") String requestURL) {
        String messageString = null;
        Document request = Document.parse(publicationRequestString);
        Document content = (Document) request.get("Aggregation");
        if (content == null) {
            messageString += "Missing Aggregation";
        }

        if (messageString == null) {
            // Get organization from profile(s)
            // Add to base document
            String ID = (String) content.get("Identifier");

            // retrieve OREMap
            Document aggregation = (Document) request.get("Aggregation");
            Client client = Client.create();
            WebResource webResource;

            webResource = client.resource(aggregation.get("@id").toString());
            webResource.addFilter(new RedirectFilter());

            ClientResponse response = webResource.accept("application/json")
                    .get(ClientResponse.class);

            if (response.getStatus() != 200) {
                throw new RuntimeException("" + response.getStatus());
            }

            Document oreMapDocument = Document.parse(response
                    .getEntity(String.class));
            ObjectId mapId = new ObjectId();
            oreMapDocument.put("_id", mapId);
            aggregation.put("authoratativeMap", mapId);

            //Update 'actionable' identifiers for map and aggregation:
            //Note these changes retain the tag-style identifier for the aggregation created by the space
            //These changes essentially work like ARKs/ARTs and represent the <aggId> moving from the custodianship of the space <SpaceURL>/<aggId>
            // to that of the CP services <servicesURL>/<aggId>
            String newMapURL = requestURL + "/" + ID + "/oremap";

            //Aggregation @id in the request

            aggregation.put("@id", newMapURL+ "#aggregation");

            //@id of the map in the map
            oreMapDocument.put("@id", newMapURL);

            //@id of describes object (the aggregation)  in map
            ((Document)oreMapDocument.get("describes")).put("@id", newMapURL + "#aggregation");
            oreMapCollection.insertOne(oreMapDocument);

            URI resource = null;
            try {
                resource = new URI("./" + ID);
            } catch (URISyntaxException e) {
                // Should not happen given simple ids
                e.printStackTrace();
            }
            return Response.ok().build();
        } else {
            return Response.status(ClientResponse.Status.BAD_REQUEST)
                    .entity(new BasicDBObject("Failure", messageString))
                    .build();
        }
    }

    @GET
    @Path("/{id}/oremap")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getROOREMap(@PathParam("id") String id) {

        FindIterable<Document> iter = oreMapCollection.find(new Document(
                "describes.Identifier", id));
        iter.projection(new Document("describes", 1).append("_id", 0));

        Document document = iter.first();
        if(document==null) {
            return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).build();
        }
        ObjectId mapId = (ObjectId) ((Document)document.get("Aggregation")).get("authoratativeMap");

        iter = oreMapCollection.find(new Document("_id", mapId));
        Document map = iter.first();
        //Internal meaning only
        map.remove("_id");
        return Response.ok(map.toJson()).build();
    }


    class RedirectFilter extends ClientFilter {

        @Override
        public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
            ClientHandler ch = getNext();
            ClientResponse resp = ch.handle(cr);

            if (resp.getClientResponseStatus().getFamily() != Response.Status.Family.REDIRECTION) {
                return resp;
            }
            else {
                // try location
                String redirectTarget = resp.getHeaders().getFirst("Location");
                cr.setURI(UriBuilder.fromUri(redirectTarget).build());
                return ch.handle(cr);
            }

        }

    }

}
