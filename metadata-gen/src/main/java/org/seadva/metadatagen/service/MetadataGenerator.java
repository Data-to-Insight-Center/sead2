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

import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.filter.ClientFilter;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.seadva.metadatagen.metagen.impl.FGDCMetadataGen;
import org.seadva.metadatagen.metagen.impl.OREMetadataGen;
import org.seadva.metadatagen.util.Constants;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URISyntaxException;

/**
 * Root resource (exposed at "rest" path)
 */
@Path("rest")
public class MetadataGenerator {

    static WebResource pdtWebService;
    static WebResource dataoneWebService;

    static {
        pdtWebService = Client.create().resource(Constants.pdtURL);
        dataoneWebService = Client.create().resource(Constants.dataoneURL);
    }

    @POST
    @Path("/{id}/fgdc")
    @Produces(MediaType.APPLICATION_XML)
    public Response addFgdc(@PathParam("id") String id,
                          String doi) throws URISyntaxException {
        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_XML).build();
        }
        if (doi == null || doi.equals("")) {
            String errorMsg = "<error><description>DOI is needed to create FGDC metadata.</description></error>";
            return Response.status(Response.Status.BAD_REQUEST).entity(errorMsg).type(MediaType.APPLICATION_XML).build();
        }

        FGDCMetadataGen fgdcMetadataGen = new FGDCMetadataGen(doi);
        String response = fgdcMetadataGen.generateMetadata(id);

        if (response.equals("")) {
            return Response.status(ClientResponse.Status.NOT_FOUND).build();
        } else {
            ClientResponse postResponse = dataoneWebService
                    .path(id)
                    .queryParam("creators", !fgdcMetadataGen.getCreatorsList().isEmpty()
                            ? StringUtils.join(fgdcMetadataGen.getCreatorsList().toArray(), "|")
                            : "")
                    .accept("application/xml")
                    .type("application/xml")
                    .post(ClientResponse.class, response);
            if (postResponse.getStatus() == 200) {
                return Response.ok(response).build();
            } else {
                return Response.serverError().build();
            }
        }
    }

    @POST
    @Path("/oremap")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response putOreMap(String publicationRequestString, @QueryParam("requestUrl") String requestURL) throws JSONException {
        String messageString = "";
        Document request = Document.parse(publicationRequestString);
        Document content = (Document) request.get("Aggregation");
        if (content == null) {
            messageString += "Missing Aggregation";
        }

        if (messageString.equals("")) {
            // Get organization from profile(s)
            // Add to base document
            String ID = (String) content.get("Identifier");

            // retrieve OREMap
            Document aggregation = (Document) request.get("Aggregation");
            Client client = Client.create();
            WebResource webResource;

            webResource = client.resource(aggregation.get("@id").toString());
            webResource.addFilter(new RedirectFilter());

            ClientResponse response = null;
            try {
                response = webResource.accept("application/json")
                        .get(ClientResponse.class);
                if (response.getStatus() != 200) {
                    String message = "Error while retrieving OREMap from Project Space - Response : " + response.getStatus();
                    System.out.println(MetadataGenerator.class.getName() + " - " + message);
                    return Response.status(ClientResponse.Status.BAD_REQUEST)
                            .entity(message)
                            .build();
                }
            } catch (RuntimeException e) {
                String message = "Error while retrieving OREMap from Project Space - Response : " + e.getMessage();
                System.out.println(MetadataGenerator.class.getName() + " - " + message);
                return Response.status(ClientResponse.Status.BAD_REQUEST)
                        .entity(message)
                        .build();
            }

            String oreString = response.getEntity(String.class);

            OREMetadataGen oreMetadataGen = new OREMetadataGen();
            if(!oreMetadataGen.hasValidOREMetadata(oreString)){
                String message = "Error occurred while validating OREMap : " + oreMetadataGen.getErrorMsg();
                System.out.println(MetadataGenerator.class.getName() + " - " + message);
                return Response.status(ClientResponse.Status.BAD_REQUEST)
                        .entity(message)
                        .build();
            }

            Document oreMapDocument = Document.parse(oreString);
            ObjectId mapId = new ObjectId();
            //oreMapDocument.put("_id", mapId);

            //Update 'actionable' identifiers for map and aggregation:
            //Note these changes retain the tag-style identifier for the aggregation created by the space
            //These changes essentially work like ARKs/ARTs and represent the <aggId> moving from the custodianship of the space <SpaceURL>/<aggId>
            // to that of the CP services <servicesURL>/<aggId>
            String newMapURL = requestURL + "/" + ID + "/oremap";

            //@id of the map in the map
            oreMapDocument.put("@id", newMapURL);

            //@id of describes object (the aggregation)  in map
            ((Document)oreMapDocument.get("describes")).put("@id", newMapURL + "#aggregation");

            ClientResponse postResponse = pdtWebService.path("researchobjects")
                    .path("/oremap")
                    .queryParam("objectId", mapId.toString())
                    .accept("application/json")
                    .type("application/json")
                    .post(ClientResponse.class, oreMapDocument.toJson().toString());

            if(postResponse.getStatus() == 200) {
                return Response.ok(new JSONObject().put("id", mapId).toString()).build();
            } else {
                System.out.println(MetadataGenerator.class.getName() + ": Error while persisting OREMap in PDT - Response : " + postResponse.getStatus());
                return Response.serverError().build();
            }
        } else {
            System.out.println(MetadataGenerator.class.getName() + ": Bad Request : " + messageString);
            return Response.status(ClientResponse.Status.BAD_REQUEST)
                    .entity(messageString)
                    .build();
        }
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
