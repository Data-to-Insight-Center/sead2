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

package org.seadva.dataone;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import org.dataconservancy.dcs.index.solr.support.SolrQueryUtil;
import org.dataconservancy.dcs.query.api.QueryMatch;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataone.service.types.v1.Event;
import org.seadva.model.pack.ResearchObject;
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
import java.net.URLEncoder;
import java.util.List;

/**
 * Similar to returning object per id,  but used for replication
 */

@Path("/mn/v1/replica")
public class Replica {


    public Replica() throws IOException, SAXException, ParserConfigurationException {
    }

    @GET
    @Path("{objectId}")
    @Produces("*/*")
    public Response getObject(@Context HttpServletRequest request,
                              @HeaderParam("user-agent") String userAgent,
                              @PathParam("objectId") String objectId) throws IOException {


        String test ="<error name=\"NotFound\" errorCode=\"404\" detailCode=\"1020\" pid=\""+objectId+"\" nodeId=\""+SeadQueryService.NODE_IDENTIFIER+"\">\n" +
                "<description>The specified object does not exist on this node.</description>\n" +
                "<traceInformation>\n" +
                "method: mn.get hint: http://cn.dataone.org/cn/resolve/"+objectId+"\n" +
                "</traceInformation>\n" +
                "</error>";

        String id = URLEncoder.encode(objectId);
        QueryResult<DcsEntity> result = null;
        try {
            result = SeadQueryService.queryService.query(SolrQueryUtil
                    .createLiteralQuery("resourceValue",
                            objectId), 0, -1); //sort by filename
        } catch (QueryServiceException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        List<QueryMatch<DcsEntity>> matches = result.getMatches();
        InputStream is = null;
        String lastFormat = null;

        for(QueryMatch<DcsEntity> entity: matches) {
            DcsFile dcsFile = (DcsFile) entity.getObject();
            String filePath = dcsFile.getSource().replace("file://", "").replace("file:", "").replace(":/", "://").replace(":///", "://");
            //String filePath = "http://bluespruce.pti.indiana.edu:8080/dcs-nced/datastream/"+  URLEncoder.encode(dcsFile.getId());


            //URL url = new URL(filePath);
            //HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            //InputStream is = urlConnection.getInputStream();
            is = new FileInputStream(filePath);

            if (dcsFile.getFormats().size() > 0) {
                for (DcsFormat format : dcsFile.getFormats()) {
                    if (SeadQueryService.sead2d1Format.get(format.getFormat()) != null) {
                        lastFormat = SeadQueryService.mimeMapping.get(SeadQueryService.sead2d1Format.get(format.getFormat()));
                        break;
                    }
                    lastFormat = SeadQueryService.mimeMapping.get(format.getFormat());
                }
            }

            String ip = null;
            if(request!=null)
                ip = request.getRemoteAddr();
            DcsEvent replicateEvent  = SeadQueryService.dataOneLogService.creatEvent(SeadQueryService.d1toSeadEventTypes.get(Event.REPLICATE.xmlValue()), userAgent, ip, entity.getObject());

            ResearchObject eventsSip = new ResearchObject();
            eventsSip.addEvent(replicateEvent);

            SeadQueryService.dataOneLogService.indexLog(eventsSip);

            break;
        }
        if (matches.size() < 1) {
            WebResource webResource = Client.create().resource(SeadQueryService.SEAD_DATAONE_URL + "/replica");
            ClientResponse response = webResource.path(id)
                    .accept("application/xml")
                    .type("application/xml")
                    .get(ClientResponse.class);
            if (response.getStatus() == 200) {
                if(response.getHeaders().get("Content-Type") != null && response.getHeaders().get("Content-Disposition") != null) {
                    lastFormat = response.getHeaders().get("Content-Type").get(0) + ",";
                    lastFormat += response.getHeaders().get("Content-Disposition").get(0).split(id)[1];
                }
                is = new ByteArrayInputStream(response.getEntity(new GenericType<String>() {}).getBytes());
            } else {
                throw new NotFoundException(test);
            }
        }

        Response.ResponseBuilder responseBuilder = Response.ok(is);
        responseBuilder.header("DataONE-SerialVersion", "1");

        if (lastFormat != null) {
            String[] format = lastFormat.split(",");
            if (format.length > 0) {
                responseBuilder.header("Content-Type", format[0]);
                responseBuilder.header("Content-Disposition",
                        "inline; filename=" + id + format[1]);
            } else {
                responseBuilder.header("Content-Disposition",
                        "inline; filename=" + id);
            }
        } else {
            responseBuilder.header("Content-Disposition",
                    "inline; filename=" + id);
        }

        return responseBuilder.build();
    }
}
