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
import java.net.URLEncoder;

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
    private WebResource seadDataoneService;
    private CacheControl control = new CacheControl();

	public ResearchObjectsImpl() {
        pdtWebService = Client.create().resource(Constants.pdtUrl);
        curBeeWebService = Client.create().resource(Constants.curBeeUrl);
        mmWebService = Client.create().resource(Constants.matchmakerUrl);
        metadataGenWebService = Client.create().resource(Constants.metadataGenUrl);
        seadDataoneService = Client.create().resource(Constants.seadDataOneUrl);
        control.setNoCache(true);
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

        return Response.status(response.getStatus()).entity(response
                .getEntity(new GenericType<String>() {})).cacheControl(control).build();
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

        return Response.status(response.getStatus()).entity(response
                .getEntity(new GenericType<String>() {})).cacheControl(control).build();
    }
    
    @GET
    @Path("/new/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNewROsList() {
        WebResource webResource = pdtWebService;

        ClientResponse response = webResource.path("researchobjects/new/")
                .accept("application/json")
                .type("application/json")
                .get(ClientResponse.class);

        return Response.status(response.getStatus()).entity(response
                .getEntity(new GenericType<String>() {})).cacheControl(control).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getROProfile(@Context HttpServletRequest servletRequest,
                                 @PathParam("id") String id) {
        WebResource webResource = pdtWebService;

        ClientResponse response = webResource.path("researchobjects")
                .path(id)
                .accept("application/json")
                .type("application/json")
                .get(ClientResponse.class);

        if(response.getStatus() == ClientResponse.Status.MOVED_PERMANENTLY.getStatusCode()) {
            String movedTo = response.getHeaders().getFirst("Location");
            String requestURL = servletRequest.getRequestURL().toString();
            String movedToURL = requestURL.replace(id, movedTo);
            return Response
                    .status(response.getStatus())
                    .header("Location", movedToURL)
                    .entity(new JSONObject().put("Error", "RO has been replaced by " + movedTo).toString())
                    .cacheControl(control).build();
        }

        return Response.status(response.getStatus()).entity(response
                .getEntity(new GenericType<String>() {})).cacheControl(control).build();
    }

    @POST
    @Path("/{id}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setROStatus(@Context HttpServletRequest request, @PathParam("id") String id, String state) {
        JSONObject stateJson = new JSONObject(state);
        // read stage and message from status
        String stage = stateJson.get("stage").toString();
        String message = stateJson.get("message").toString();

        if(message.startsWith("doi:")){
            message = message.replace("doi:", "http://dx.doi.org/");
        }

        // Check whether the RO has an alternate RO which was published before
        Response getRoProfileResponse = getROProfile(request, id);
        if(getRoProfileResponse.getStatus() != 200) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(getRoProfileResponse.getEntity().toString())
                    .build();
        }

        JSONObject roObject = new JSONObject((String)getRoProfileResponse.getEntity());
        String callbackUrl = roObject.getString("Publication Callback");
        boolean republishRO = false;
        String alternateOf = null;
        if("Success".equals(stage) && roObject.has("Preferences") &&
                roObject.get("Preferences") instanceof  JSONObject &&
                roObject.getJSONObject("Preferences").has("External Identifier")) {
            Object republishROIdObject = roObject.getJSONObject("Preferences").get("External Identifier");
            if(republishROIdObject != null && republishROIdObject instanceof String){
                String republishROPID = (String)republishROIdObject;

                ClientResponse pidResponse = pdtWebService.path("researchobjects/pid")
                        .path(URLEncoder.encode(republishROPID))
                        .accept("application/json")
                        .type("application/json")
                        .get(ClientResponse.class);
                if(pidResponse.getStatus() == 200) {
                    Document roDoc = Document.parse(pidResponse.getEntity(String.class));
                    alternateOf = roDoc.getString("roId");
                    republishRO = true;
                    System.out.println("RO with ID " + id + " is an alternate of RO with ID " + alternateOf);
                }
            }
        }

        // update PDT with the status
        ClientResponse statusUpdateResponse = pdtWebService.path("researchobjects")
                .path(id + "/status")
                .accept("application/json")
                .type("application/json")
                .post(ClientResponse.class, state);

        if (!"Success".equals(stage) || statusUpdateResponse.getStatus() != 200) {
            // if the status update in PDT is not successful, return the error
            return Response.status(statusUpdateResponse.getStatus()).entity(statusUpdateResponse
                    .getEntity(new GenericType<String>() {})).cacheControl(control).build();
        } else {

            // If this RO has an alternate RO, delete the old RO request/OREMap and add oldRO ID to the new RO request
            if (republishRO && !id.equals(alternateOf)) {
                ClientResponse deprecateRoResponse = pdtWebService.path("researchobjects/deprecate")
                        .path(id)
                        .path(alternateOf)
                        .accept("application/json")
                        .type("application/json")
                        .get(ClientResponse.class);
                if(deprecateRoResponse.getStatus() != 200) {
                    System.out.println("Deprecation of RO Failed : Error occurred while deprecating " + alternateOf +
                            " by " + id + ", response status : " + deprecateRoResponse.getStatus());
                }
            }

            // Calling MetadataGenerator to generate FGDC metadata for the RO
            // If this RO has an alternate RO, obsolete the FGDC of previous RO if it is different
            ClientResponse metagenResponse = metadataGenWebService.path("rest")
                    .path(id + "/fgdc")
                    .queryParam("deprecateFgdc", republishRO ? alternateOf : "")
                    .accept("application/xml")
                    .type("application/xml")
                    .post(ClientResponse.class, message);
            if(metagenResponse.getStatus() != 200){
                System.out.println("Failed to generate FGDC metadata for " + id);
            }

            // if the status update in PDT is successful, we have to send to DOI to project space/data source
            if (callbackUrl != null) {
                // now we POST to callback URL to update Clowder with the DOI
                Client client = Client.create();
                // set credentials
                // TODO use keys when those are available
                client.addFilter(new HTTPBasicAuthFilter(Constants.clowderUser, Constants.clowderPassword));
                WebResource callbackResource = client.resource(callbackUrl);
                // DOI is in message when the stage is Success
                String body = "{\"uri\":\"" + message + "\", \"@context\": " +
                        "{\"uri\": \"http://purl.org/dc/terms/identifier\"}}";
                ClientResponse pubRequestorResponse = callbackResource
                        .accept("application/json")
                        .type("application/json")
                        .post(ClientResponse.class, body);
                // TODO log
                System.out.println("Project Space/Data Source Updated, Response : " + pubRequestorResponse.getEntity(String.class));
            }
            return Response.status(ClientResponse.Status.OK).cacheControl(control).build();
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

        return Response.status(response.getStatus()).entity(response
                .getEntity(new GenericType<String>() {})).cacheControl(control).build();
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

        return Response.status(response.getStatus()).entity(response
                .getEntity(new GenericType<String>() {})).cacheControl(control).build();
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

        return Response.status(response.getStatus()).entity(response
                .getEntity(new GenericType<String>() {})).cacheControl(control).build();
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

        return Response.status(response.getStatus()).entity(response
                .getEntity(new GenericType<String>() {})).cacheControl(control).build();
    }

	@GET
	@Path("/{id}/oremap")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getROOREMap(@PathParam("id") String id) {
        WebResource webResource = pdtWebService;

        ClientResponse response = webResource.path("researchobjects")
                .path(id + "/oremap")
                .accept("application/json")
                .type("application/json")
                .get(ClientResponse.class);

        return Response.status(response.getStatus()).entity(response
                .getEntity(new GenericType<String>() {})).cacheControl(control).build();
	}

    @GET
    @Path("/{id}/fgdc")
    @Produces(MediaType.APPLICATION_XML)
    public Response getFgdc(@PathParam("id") String id) {
        WebResource webResource = seadDataoneService;

        ClientResponse response = webResource.path("researchobjects")
                .path(id + "/fgdc")
                .accept("application/xml")
                .type("application/xml")
                .get(ClientResponse.class);

        return Response.status(response.getStatus()).entity(response
                .getEntity(new GenericType<String>() {})).cacheControl(control).build();
    }

}
