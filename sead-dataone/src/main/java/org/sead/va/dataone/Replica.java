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
import org.dataone.service.types.v1.Event;
import org.json.JSONObject;
import org.sead.va.dataone.util.Constants;
import org.sead.va.dataone.util.MongoDB;
import org.sead.va.dataone.util.SeadQueryService;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Similar to returning object per id,  but used for replication
 */

@Path("/mn/v1/replica")
public class Replica {

    private MongoCollection<Document> fgdcCollection = null;
    private MongoDatabase metaDb = null;

    public Replica() throws IOException, SAXException, ParserConfigurationException {
        metaDb = MongoDB.getServicesDB();
        fgdcCollection = metaDb.getCollection(MongoDB.fgdc);
    }

    @GET
    @Path("{objectId}")
    @Produces("*/*")
    public Response getObject(@Context HttpServletRequest request,
                              @HeaderParam("user-agent") String userAgent,
                              @PathParam("objectId") String objectId) throws IOException {


        String test ="<error name=\"NotFound\" errorCode=\"404\" detailCode=\"1020\" pid=\""+objectId+"\" nodeId=\""+ Constants.NODE_IDENTIFIER+"\">\n" +
                "<description>The specified object does not exist on this node.</description>\n" +
                "<traceInformation>\n" +
                "method: mn.get hint: http://cn.dataone.org/cn/resolve/"+objectId+"\n" +
                "</traceInformation>\n" +
                "</error>";

        String id = objectId;
        FindIterable<Document> iter = fgdcCollection.find(new Document(Constants.META_INFO + "." + Constants.FGDC_ID, id));
        if(iter != null && iter.first() != null){

            JSONObject object = new JSONObject(iter.first().toJson().toString());
            JSONObject metaInfo = (JSONObject) object.get(Constants.META_INFO);
            String fgdcMetadata = object.get(Constants.METADATA).toString();
            String metadataFormat = (String) metaInfo.get(Constants.META_FORMAT);

            //String filePath = "http://bluespruce.pti.indiana.edu:8080/dcs-nced/datastream/"+  URLEncoder.encode(dcsFile.getId());

            //URL url = new URL(filePath);
            //HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            //InputStream is = urlConnection.getInputStream();
            InputStream is = new ByteArrayInputStream(fgdcMetadata.getBytes());

            String lastFormat = SeadQueryService.mimeMapping.get(metadataFormat);

            if (SeadQueryService.sead2d1Format.get(metadataFormat) != null) {
                lastFormat = SeadQueryService.mimeMapping.get(SeadQueryService.sead2d1Format.get(metadataFormat));
            }

            Response.ResponseBuilder responseBuilder = Response.ok(is);

            responseBuilder.header("DataONE-SerialVersion","1");

            if(lastFormat!=null){
                String[] format = lastFormat.split(",");
                if(format.length>0)
                {
                    responseBuilder.header("Content-Type", format[0]);
                    responseBuilder.header("Content-Disposition",
                            "inline; filename=" + id+format[1]);
                }
                else{
                    responseBuilder.header("Content-Disposition",
                            "inline; filename=" + id);
                }
            }
            else{
                responseBuilder.header("Content-Disposition",
                        "inline; filename=" + id);
            }


            String ip = null;
            if(request!=null)
                ip = request.getRemoteAddr();
            //DcsEvent replicateEvent  = SeadQueryService.dataOneLogService.creatEvent(SeadQueryService.d1toSeadEventTypes.get(Event.REPLICATE.xmlValue()), userAgent, ip, entity.getObject());

            //ResearchObject eventsSip = new ResearchObject();
            //eventsSip.addEvent(replicateEvent);

            //SeadQueryService.dataOneLogService.indexLog(eventsSip);

            return responseBuilder.build();
        } else {
            return Response.status(ClientResponse.Status.NOT_FOUND).build();
        }
    }
}
