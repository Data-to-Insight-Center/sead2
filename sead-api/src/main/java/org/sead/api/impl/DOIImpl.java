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
 * @author charmadu@umail.iu.edu
 */

package org.sead.api.impl;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import org.sead.api.DOI;
import org.sead.api.util.Constants;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/doi")
public class DOIImpl extends DOI{

    private WebResource resource;
    private String serviceUrl;

    private WebResource resource(){
        return resource;
    }

    public DOIImpl() {
        this.serviceUrl = Constants.doiServiceUrl;
        resource = Client.create().resource(serviceUrl);
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateDoi(String doiInfo) {
        WebResource webResource = resource();

        ClientResponse response = webResource.path("doi")
                .accept("application/json")
                .type("application/json")
                .post(ClientResponse.class, doiInfo);

        return Response.status(response.getStatus())
                .entity(response.getEntity(new GenericType<String>() {}))
                .build();
    }
}
