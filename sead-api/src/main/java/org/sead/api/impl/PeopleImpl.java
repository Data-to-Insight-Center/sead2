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
import org.sead.api.People;
import org.sead.api.util.Constants;

import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * See abstract base class for documentation of the rest api. Note - path
 * annotations must match base class for documentation to be correct.
 */

@Path("/people")
public class PeopleImpl extends People {
	private WebResource resource;
	private String serviceUrl;
    private CacheControl control = new CacheControl();

	private WebResource resource(){
        return resource;
    }
	

	public PeopleImpl() {
        this.serviceUrl = Constants.pdtUrl;
        resource = Client.create().resource(serviceUrl);
        control.setNoCache(true);
	}
	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response registerPerson(String personString) {

        WebResource webResource = resource();

        ClientResponse response = webResource.path("people")
                .accept("application/json")
                .type("application/json")
                .post(ClientResponse.class, personString);

        return Response.status(response.getStatus()).entity(response
                .getEntity(new GenericType<String>() {})).cacheControl(control).build();
	}

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPeopleList() {
        WebResource webResource = resource();

        ClientResponse response = webResource.path("people")
                .accept("application/json")
                .type("application/json")
                .get(ClientResponse.class);

        return Response.status(response.getStatus()).entity(response
                .getEntity(new GenericType<String>(){})).cacheControl(control).build();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPersonProfile(@Encoded @PathParam("id") String id) {
        WebResource webResource = resource();

        ClientResponse response = webResource.path("people")
                .path(id)
                .accept("application/json")
                .type("application/json")
                .get(ClientResponse.class);

        return Response.status(response.getStatus()).entity(response
                .getEntity(new GenericType<String>() {})).cacheControl(control).build();
    }

    @GET
    @Path("/{id}/raw")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRawPersonProfile(@PathParam("id") String id) {
        WebResource webResource = resource();

        ClientResponse response = webResource.path("people")
                .path(id+"/raw")
                .accept("application/json")
                .type("application/json")
                .get(ClientResponse.class);

        return Response.status(response.getStatus()).entity(response
                .getEntity(new GenericType<String>() {})).cacheControl(control).build();
    }

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updatePersonProfile(@PathParam("id") String id) {
        WebResource webResource = resource();

        ClientResponse response = webResource.path("people")
                .path(id)
                .accept("application/json")
                .type("application/json")
                .put(ClientResponse.class);

        return Response.status(response.getStatus()).entity(response
                .getEntity(new GenericType<String>() {})).cacheControl(control).build();
    }

	@DELETE
	@Path("/{id}")
	public Response unregisterPerson(@PathParam("id") String id) {
        WebResource webResource = resource();

        ClientResponse response = webResource.path("people")
                .path(id)
                .accept("application/json")
                .type("application/json")
                .delete(ClientResponse.class);

        return Response.status(response.getStatus()).entity(response
                .getEntity(new GenericType<String>() {})).cacheControl(control).build();
    }

}
