/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.sead.va.dataone;


import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sun.jersey.api.client.ClientResponse;
import org.bson.Document;
import org.json.JSONObject;
import org.sead.va.dataone.util.Constants;
import org.sead.va.dataone.util.MongoDB;
import org.xml.sax.SAXException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;


/*
 * Returns list of objects and also datastream for individual objects
*/

@Path("/mn/v1/researchobjects")
public class ResearchObject {

    private MongoCollection<Document> fgdcCollection = null;
    private MongoDatabase metaDb = null;


    public ResearchObject() throws IOException, SAXException, ParserConfigurationException {
        metaDb = MongoDB.getServicesDB();
        fgdcCollection = metaDb.getCollection(MongoDB.fgdc);
    }

    @Context
    ServletContext context;

    @GET
    @Path("/{roId}/fgdc")
    @Produces(MediaType.APPLICATION_XML)
    public Response getObject(@Context HttpServletRequest request,
                              @PathParam("roId") String roId) throws IOException {

        String errorMsg = "<error name=\"NotFound\" errorCode=\"404\" pid=\"" + roId + "\" nodeId=\"" + Constants.NODE_IDENTIFIER + "\">\n" +
                "<description>The specified object does not exist on this node.</description>\n" +
                "</error>";

        FindIterable<Document> iter = fgdcCollection.find(new Document(Constants.META_INFO + "." + Constants.RO_ID, roId));
        if(iter != null && iter.first() != null){
            JSONObject object = new JSONObject(iter.first());
            String fgdcMetadata = object.get(Constants.METADATA).toString();
            return Response.ok().entity(fgdcMetadata).build();
        } else {
            return Response.status(ClientResponse.Status.NOT_FOUND).entity(errorMsg).build();
        }
    }



}
