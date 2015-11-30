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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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

        String id = objectId;
        QueryResult<DcsEntity> result = null;
        try {
            result = SeadQueryService.queryService.query(SolrQueryUtil
                    .createLiteralQuery("resourceValue",
                            objectId), 0, -1); //sort by filename
        } catch (QueryServiceException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        List<QueryMatch<DcsEntity>> matches = result.getMatches();
        for(QueryMatch<DcsEntity> entity: matches){
            DcsFile dcsFile = (DcsFile)entity.getObject();
            String filePath = dcsFile.getSource().replace("file://","").replace("file:","").replace(":/","://").replace(":///","://");
            //String filePath = "http://bluespruce.pti.indiana.edu:8080/dcs-nced/datastream/"+  URLEncoder.encode(dcsFile.getId());


            //URL url = new URL(filePath);
            //HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            //InputStream is = urlConnection.getInputStream();
            InputStream is = new FileInputStream(filePath);

            String lastFormat = null;
            if(dcsFile.getFormats().size()>0){
                for(DcsFormat format:dcsFile.getFormats()){
                    if(SeadQueryService.sead2d1Format.get(format.getFormat())!=null) {
                        lastFormat = SeadQueryService.mimeMapping.get(SeadQueryService.sead2d1Format.get(format.getFormat()));
                        break;
                    }
                    lastFormat = SeadQueryService.mimeMapping.get(format.getFormat());
                }
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
            DcsEvent replicateEvent  = SeadQueryService.dataOneLogService.creatEvent(SeadQueryService.d1toSeadEventTypes.get(Event.REPLICATE.xmlValue()), userAgent, ip, entity.getObject());

            ResearchObject eventsSip = new ResearchObject();
            eventsSip.addEvent(replicateEvent);

            SeadQueryService.dataOneLogService.indexLog(eventsSip);

            return responseBuilder.build();
        }
        throw new NotFoundException(test);
    }
}
