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
 * @author charmadu@umail.iu.edu
 * @author myersjd@umich.edu
 */

package org.seadva.metadatagen.service;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.filter.ClientFilter;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.seadva.metadatagen.metagen.impl.FGDCMetadataGen;
import org.seadva.metadatagen.util.Constants;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Root resource (exposed at "rest" path)
 */
@Path("rest")
public class MetadataGenerator {

    static MongoCollection<Document> oreMapCollection;
    static MongoCollection<Document> fgdcCollection;
    static MongoClient mongoClient;
    static MongoDatabase db;

    static {
        mongoClient = new MongoClient();
        db = mongoClient.getDatabase(Constants.metagenDbName);
        oreMapCollection = db.getCollection(Constants.dbOreCollection);
        fgdcCollection = db.getCollection(Constants.dbFgdcCollection);
    }

    @POST
    @Path("/{id}/metadata/{type}")
    @Produces(MediaType.APPLICATION_XML)
    public Response putMetadata(@PathParam("id") String entityId,
                          @PathParam("type") String metadataType,
                          String doi) throws URISyntaxException {

        String errorMsg ="<error>\n" +
                "<description>EntityId and type(ORE/SIP/FGDC) are required path parameters. Please specify.</description>\n" +
                "<traceInformation>\n" +
                "method: metadata-gen/rest/{id}/metadata/{type} \n" +
                "</traceInformation>\n" +
                "</error>";

        if(entityId == null || metadataType == null){
            return Response.status(Response.Status.BAD_REQUEST).entity(errorMsg).type(MediaType.APPLICATION_XML).build();
        }

        if(metadataType.equalsIgnoreCase("FGDC")){
            if(doi == null || doi.equals("")){
                errorMsg ="<error><description>DOI is needed to create FGDC metadata.</description></error>";
                return Response.status(Response.Status.BAD_REQUEST).entity(errorMsg).type(MediaType.APPLICATION_XML).build();
            }
        }

        String response = null;

        if(metadataType.equalsIgnoreCase("FGDC")){
            FGDCMetadataGen fgdcMetadataGen = new FGDCMetadataGen(doi);
            response = fgdcMetadataGen.generateMetadata(entityId);
        }

        if(response == null){
            errorMsg ="<error><description>Metadata type " + metadataType + " is not supported.</description></error>";
            return Response.status(Response.Status.BAD_REQUEST).entity(errorMsg).type(MediaType.APPLICATION_XML).build();
        } else if(response.equals("")) {
            return Response.status(ClientResponse.Status.NOT_FOUND).build();
        } else {
            return Response.ok(response).build();
        }
    }

    
    @GET
    @Path("/{id}/metadata/{type}")
    @Produces(MediaType.APPLICATION_XML)
    public Response getMetadata(@PathParam("id") String entityId,
                          @PathParam("type") String metadataType) throws URISyntaxException {

        String errorMsg ="<error>\n" +
                "<description>EntityId and type(ORE/SIP/FGDC) are required path parameters. Please specify.</description>\n" +
                "<traceInformation>\n" +
                "method: metadata-gen/rest/{id}/metadata/{type} \n" +
                "</traceInformation>\n" +
                "</error>";

        if(entityId == null || metadataType == null){
            return Response.status(Response.Status.BAD_REQUEST).entity(errorMsg).type(MediaType.APPLICATION_XML).build();
        }

        FindIterable<Document> iter = null;

        if(metadataType.equalsIgnoreCase("FGDC")){
            iter = fgdcCollection.find(new Document("@id", entityId));
        }

        if(iter == null){
            errorMsg ="<error><description>Metadata type " + metadataType + " is not supported.</description></error>";
            return Response.status(Response.Status.BAD_REQUEST).entity(errorMsg).type(MediaType.APPLICATION_XML).build();
        } else if(iter.first() == null) {
            return Response.status(ClientResponse.Status.NOT_FOUND).build();
        } else {
            return Response.ok(iter.first().get("metadata").toString()).build();
        }
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

            //Update 'actionable' identifiers for map and aggregation:
            //Note these changes retain the tag-style identifier for the aggregation created by the space
            //These changes essentially work like ARKs/ARTs and represent the <aggId> moving from the custodianship of the space <SpaceURL>/<aggId>
            // to that of the CP services <servicesURL>/<aggId>
            String newMapURL = requestURL + "/" + ID + "/oremap";

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
        //iter.projection(new Document("describes", 1).append("_id", 0));

        Document document = iter.first();
        if(document==null) {
            return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).build();
        }
        //ObjectId mapId = (ObjectId) ((Document)document.get("Aggregation")).get("authoratativeMap");

        //iter = oreMapCollection.find(new Document("_id", mapId));
        //Document map = iter.first();
        //Internal meaning only
        //map.remove("_id");
        document.remove("_id");
        return Response.ok(document.toJson()).build();
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
