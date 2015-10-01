/*
 *
 * Copyright 2015 University of Michigan, 2015 The Trustees of Indiana University,
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
 * @author charmadu@umail.iu.edu
 */

package org.sead.api.impl;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.bson.Document;
import org.json.JSONObject;
import org.sead.api.ResearchObjects;
import org.sead.api.util.Constants;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * See abstract base class for documentation of the rest api. Note - path
 * annotations must match base class for documentation to be correct.
 */

@Path("/researchobjects")
public class ResearchObjectsImpl extends ResearchObjects {

    private WebResource pdtWebService;
    private WebResource curBeeWebService;
    private WebResource mmWebService;
    private WebResource metadataGenWebService;

	public ResearchObjectsImpl() {
        pdtWebService = Client.create().resource(Constants.pdtUrl);
        curBeeWebService = Client.create().resource(Constants.curBeeUrl);
        mmWebService = Client.create().resource(Constants.matchmakerUrl);
        metadataGenWebService = Client.create().resource(Constants.metadataGenUrl);
	}

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response startROPublicationProcess(String publicationRequestString, @Context HttpServletRequest servletRequest) {
        WebResource webResource = curBeeWebService;

        String requestUrl = servletRequest.getRequestURL().toString();

        ClientResponse response = webResource.path("service/publishRO")
                .queryParam("requestUrl", requestUrl)
                .accept("application/json")
                .type("application/json")
                .post(ClientResponse.class, publicationRequestString);

        return Response.status(response.getStatus()).entity(response.getEntity(new GenericType<String>() {})).build();
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getROsList() {
        WebResource webResource = pdtWebService;

        ClientResponse response = webResource.path("researchobjects")
                .accept("application/json")
                .type("application/json")
                .get(ClientResponse.class);

        return Response.status(response.getStatus()).entity(response.getEntity(new GenericType<String>() {})).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getROProfile(@PathParam("id") String id) {
        WebResource webResource = pdtWebService;

        ClientResponse response = webResource.path("researchobjects")
                .path(id)
                .accept("application/json")
                .type("application/json")
                .get(ClientResponse.class);

        return Response.status(response.getStatus()).entity(response.getEntity(new GenericType<String>() {})).build();
    }

    @POST
    @Path("/{id}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setROStatus(@PathParam("id") String id, String state) {
        JSONObject stateJson = new JSONObject(state);
        // read stage and message from status
        String stage = stateJson.get("stage").toString();
        String message = stateJson.get("message").toString();

        // update PDT with the status
        ClientResponse response = pdtWebService.path("researchobjects")
                .path(id + "/status")
                .accept("application/json")
                .type("application/json")
                .post(ClientResponse.class, state);

        if (!"Success".equals(stage) || response.getStatus() != 200) {
            // if the status update in PDT is not successful, return the error
            return Response.status(response.getStatus()).entity(response.getEntity(new GenericType<String>() {})).build();
        } else {
            // Calling MetadataGenerator to generate FGDC metadata for the RO
            ClientResponse metagenResponse = metadataGenWebService.path("rest")
                    .path(id + "/metadata/fgdc")
                    .accept("application/json")
                    .type("application/json")
                    .post(ClientResponse.class, message);
            if(metagenResponse.getStatus() != 200){
                System.out.println("Failed to generate FGDC metadata for " + id);
            }

            // if the status update in PDT is successful, we have to send to DOI to Clowder
            // first get the RO JSON to find the callback URL
            ClientResponse roResponse = pdtWebService.path("researchobjects")
                    .path(id)
                    .accept("application/json")
                    .type("application/json")
                    .get(ClientResponse.class);
            Document roDoc = Document.parse(roResponse.getEntity(String.class));
            String callbackUrl = roDoc.getString("Publication Callback");

            if (callbackUrl != null) {
                // now we POST to callback URL to update Clowder with the DOI
                Client client = Client.create();
                // set credentials
                client.addFilter(new HTTPBasicAuthFilter(Constants.clowderUser, Constants.clowderPassword));
                WebResource clowderResource = client.resource(callbackUrl);
                // DOI is in message when the stage is Success
                String body = "{\"uri\":\"" + message + "\", \"@context\": " +
                        "{\"uri\": \"http://purl.org/dc/terms/identifier\"}}";
                ClientResponse clowderResponse = clowderResource
                        .accept("application/json")
                        .type("application/json")
                        .post(ClientResponse.class, body);
                // TODO log
                System.out.println("Clowder Updated, Response : " + clowderResponse.getEntity(String.class));
            }
            return Response.status(ClientResponse.Status.OK).build();
        }
    }

    @GET
    @Path("/{id}/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getROStatus(@PathParam("id") String id) {
        WebResource webResource = pdtWebService;

        ClientResponse response = webResource.path("researchobjects")
                .path(id + "/status")
                .accept("application/json")
                .type("application/json")
                .get(ClientResponse.class);

        return Response.status(response.getStatus()).entity(response.getEntity(new GenericType<String>() {})).build();
    }

	@DELETE
	@Path("/{id}")
	public Response rescindROPublicationRequest(@PathParam("id") String id) {
        WebResource webResource = pdtWebService;

        ClientResponse response = webResource.path("researchobjects")
                .path(id)
                .accept("application/json")
                .type("application/json")
                .delete(ClientResponse.class);

        return Response.status(response.getStatus()).entity(response.getEntity(new GenericType<String>() {})).build();
	}

	@POST
    @Path("/matchingrepositories")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response makeMatches(String matchRequest) {
        ClientResponse response = mmWebService.path("ro")
                .path("matchingrepositories")
                .accept("application/json")
                .type("application/json")
                .post(ClientResponse.class, matchRequest);

        return Response.status(response.getStatus()).entity(
                response.getEntity(new GenericType<String>() {})).build();
    }

    @GET
    @Path("/matchingrepositories/rules")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRulesList() {
        ClientResponse response = mmWebService.path("ro")
                .path("matchingrepositories")
                .path("rules")
                .accept("application/json")
                .type("application/json")
                .get(ClientResponse.class);

        return Response.status(response.getStatus()).entity(
                response.getEntity(new GenericType<String>() {})).build();
    }

	@GET
	@Path("/{id}/oremap")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getROOREMap(@PathParam("id") String id) {
        WebResource webResource = metadataGenWebService;

        ClientResponse response = webResource.path("rest")
                .path(id + "/oremap")
                .accept("application/json")
                .type("application/json")
                .get(ClientResponse.class);

        return Response.status(response.getStatus()).entity(response.getEntity(new GenericType<String>() {
        })).build();
	}

    @GET
    @Path("/{id}/fgdc")
    @Produces(MediaType.APPLICATION_XML)
    public Response getFgdc(@PathParam("id") String id) {
        WebResource webResource = metadataGenWebService;

        ClientResponse response = webResource.path("rest")
                .path(id + "/metadata/fgdc")
                .accept("application/xml")
                .type("application/xml")
                .get(ClientResponse.class);

        return Response.status(response.getStatus()).entity(response.getEntity(new GenericType<String>() {})).build();
    }

}
