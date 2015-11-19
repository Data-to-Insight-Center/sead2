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
import org.bson.Document;
import org.sead.va.dataone.util.MongoDB;
import org.xml.sax.SAXException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import com.sun.jersey.api.client.ClientResponse;


/*
 * Returns list of objects and also datastream for individual objects
*/

@Path("/mn/v1/object")
public class Object {

    private MongoCollection<Document> fgdcCollection = null;
    private MongoDatabase metaDb = null;


    public Object() throws IOException, SAXException, ParserConfigurationException {
        metaDb = MongoDB.getServicesDB();
        fgdcCollection = metaDb.getCollection(MongoDB.fgdc);
    }

    @Context
    ServletContext context;

    @GET
    @Path("{objectId}")
    @Produces("*/*")
    public Response getObject(@Context HttpServletRequest request,
                              @HeaderParam("user-agent") String userAgent,
                              @PathParam("objectId") String objectId) throws IOException {


        String test ="<error name=\"NotFound\" errorCode=\"404\" detailCode=\"1020\" pid=\""+URLEncoder.encode(objectId)+"\" nodeId=\""+"TODO-NodeIdentifier"+"\">\n" +
                "<description>The specified object does not exist on this node.</description>\n" +
                "<traceInformation>\n" +
                "method: mn.get hint: http://cn.dataone.org/cn/resolve/"+URLEncoder.encode(objectId)+"\n" +
                "</traceInformation>\n" +
                "</error>";

        String id = objectId;

        FindIterable<Document> iter = fgdcCollection.find(new Document("@id", id));
        if(iter != null && iter.first() != null){
            return Response.ok(iter.first().get("metadata").toString()).build();
        } else {
            return Response.status(ClientResponse.Status.NOT_FOUND).build();
        }
    }



    @GET
    @Produces(MediaType.APPLICATION_XML)
    public String listObjects(@Context HttpServletRequest request,
                                   @HeaderParam("user-agent") String userAgent,
                                   @QueryParam("start") int start,
                                   @QueryParam("count") String countStr,
                                   @QueryParam("formatId") String formatId,
                                   @QueryParam("fromDate") String fromDate,
                                   @QueryParam("toDate") String toDate)
            throws ParseException, TransformerException {



        Map<String,Integer> doiCount = new HashMap<String, Integer>();


        return "";
    }
}
