/*
 *
 * Copyright 2015 University of Michigan
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
 */

package org.sead.api.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import org.bson.Document;
import org.json.JSONArray;
import org.sead.api.Repositories;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.util.JSON;
import com.sun.jersey.api.client.ClientResponse.Status;
import org.sead.api.util.Constants;

/**
 * See abstract base class for documentation of the rest api. Note - path
 * annotations must match base class for documentation to be correct.
 */

@Path("/repositories")
public class RepositoriesImpl extends Repositories {
    private WebResource resource;
    private String serviceUrl;

    private WebResource resource(){
        return resource;
    }

	public RepositoriesImpl() {
        this.serviceUrl = Constants.pdtUrl;
        resource = Client.create().resource(serviceUrl);
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response registerRepository(String profileString) {
        WebResource webResource = resource();

        ClientResponse response = webResource.path("repositories")
                .accept("application/json")
                .type("application/json")
                .post(ClientResponse.class, profileString);

        return Response.status(response.getStatus()).entity(response.getEntity(new GenericType<String>() {})).build();
	}

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRepositoryList() {
        WebResource webResource = resource();

        ClientResponse response = webResource.path("repositories")
                .accept("application/json")
                .type("application/json")
                .get(ClientResponse.class);

        return Response.status(response.getStatus()).entity(response.getEntity(new GenericType<String>() {})).build();

	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRepositoryProfile(@PathParam("id") String id) {
        WebResource webResource = resource();

        ClientResponse response = webResource.path("repositories")
                .path(id)
                .accept("application/json")
                .type("application/json")
                .get(ClientResponse.class);

        return Response.status(response.getStatus()).entity(response.getEntity(new GenericType<String>() {})).build();
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setRepositoryProfile(@PathParam("id") String id,
			String profile) {
        WebResource webResource = resource();

        ClientResponse response = webResource.path("repositories")
                .path(id)
                .accept("application/json")
                .type("application/json")
                .put(ClientResponse.class, profile);

        return Response.status(response.getStatus()).entity(response.getEntity(new GenericType<String>() {})).build();
	}

	@DELETE
	@Path("/{id}")
	public Response unregisterRepository(@PathParam("id") String id) {
        WebResource webResource = resource();

        ClientResponse response = webResource.path("repositories")
                .path(id)
                .accept("application/json")
                .type("application/json")
                .delete(ClientResponse.class);

        return Response.status(response.getStatus()).entity(response.getEntity(new GenericType<String>() {})).build();
	}

	@GET
	@Path("/{id}/researchobjects")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getROsByRepository(@PathParam("id") String id) {
        WebResource webResource = resource();

        ClientResponse response = webResource.path("repositories")
                .path(id + "/researchobjects")
                .accept("application/json")
                .type("application/json")
                .get(ClientResponse.class);

        return Response.status(response.getStatus()).entity(response.getEntity(new GenericType<String>() {})).build();
	}

}
