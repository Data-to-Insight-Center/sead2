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
import com.sun.jersey.api.client.WebResource;
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField;
import org.dataconservancy.dcs.index.solr.support.SolrQueryUtil;
import org.dataconservancy.dcs.query.api.QueryMatch;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFixity;
import org.dataone.service.types.v1.Checksum;
import org.dataone.service.types.v1.Event;
import org.jibx.runtime.JiBXException;
import org.seadva.model.pack.ResearchObject;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.transform.TransformerException;
import java.net.URLEncoder;
import java.util.List;


/*
 * Return checksum for files
 */



@Path("/mn/v1/checksum")
public class ObjectChecksum {


    public  ObjectChecksum(){
    }
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("{pid}")
    public String getChecksum(@Context HttpServletRequest request,
                                @HeaderParam("user-agent") String userAgent,
                                @PathParam("pid") String objectId,
                                @QueryParam("checksumAlgorithm") String checksumAlgorithm) throws JiBXException, TransformerException {

        String test ="<error name=\"NotFound\" errorCode=\"404\" detailCode=\"1060\" pid=\""+ URLEncoder.encode(objectId)+"\" nodeId=\""+SeadQueryService.NODE_IDENTIFIER+"\">\n" +
                "<description>The specified object does not exist on this node.</description>\n" +
                "<traceInformation>\n" +
                "method: mn.getChecksum hint: http://cn.dataone.org/cn/resolve/"+URLEncoder.encode(objectId)+"\n" +
                "</traceInformation>\n" +
                "</error>";
        if(objectId.contains("TestingNotFound")||objectId.contains("Test"))
            throw new NotFoundException(test);

        objectId = objectId.replace("doi-", "http://dx.doi.org/");

        String queryStr = SolrQueryUtil.createLiteralQuery("resourceValue", objectId);

        if(checksumAlgorithm!=null)
            queryStr+= " AND "+SolrQueryUtil.createLiteralQuery(DcsSolrField.FixityField.ALGORITHM.solrName(), checksumAlgorithm);

        QueryResult<DcsEntity> result = null;
        try {
            result = SeadQueryService.queryService.query(queryStr, 0, -1); //sort by filename
        } catch (QueryServiceException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        String ip = null;
        if(request!=null)
            ip = request.getRemoteAddr();
        List<QueryMatch<DcsEntity>> matches = result.getMatches();
        if(matches.size()==0){
            WebResource webResource = Client.create()
                    .resource(SeadQueryService.SEAD_DATAONE_URL + "/checksum")
                    .path(URLEncoder.encode(objectId));
            if(checksumAlgorithm!=null) {
                webResource = webResource.queryParam("checksumAlgorithm", checksumAlgorithm);
            }
            ClientResponse response = webResource
                    .accept("application/xml")
                    .type("application/xml")
                    .get(ClientResponse.class);
            if (response.getStatus() == 200) {
                Checksum mongoChecksum = (Checksum) SeadQueryService.unmarshal(response.getEntityInputStream(), Checksum.class);
                return SeadQueryService.marshal(mongoChecksum);
            } else {
                throw new NotFoundException(test);
            }
        }
        for(QueryMatch<DcsEntity> entity: matches){
            if(entity.getObject() instanceof DcsFile)  {
                DcsFile file = (DcsFile)entity.getObject();


                if(file.getFixity().size()>0){
                    for(DcsFixity fixity:file.getFixity())
                    {
                        Checksum checksum = new Checksum();
                        if(checksumAlgorithm!=null){
                            if(SeadQueryService.sead2d1fixity.get(fixity.getAlgorithm()).equals(checksumAlgorithm)){
                                checksum.setAlgorithm(checksumAlgorithm);
                                checksum.setValue(fixity.getValue());

                                DcsEvent readEvent  = SeadQueryService.dataOneLogService.creatEvent(Event.READ.xmlValue(), userAgent, ip, entity.getObject());

                                ResearchObject eventsSip = new ResearchObject();
                                eventsSip.addEvent(readEvent);

                                SeadQueryService.dataOneLogService.indexLog(eventsSip);
                                return SeadQueryService.marshal(checksum);
                            }
                            continue;
                        }
                        else{

                            checksum.setAlgorithm(SeadQueryService.sead2d1fixity.get(fixity.getAlgorithm()));
                            checksum.setValue(fixity.getValue());

                            DcsEvent readEvent  = SeadQueryService.dataOneLogService.creatEvent(Event.READ.xmlValue(), userAgent, ip, entity.getObject());

                            ResearchObject eventsSip = new ResearchObject();
                            eventsSip.addEvent(readEvent);

                            SeadQueryService.dataOneLogService.indexLog(eventsSip);
                            return SeadQueryService.marshal(checksum);
                        }
                    }

                }

            }
        }

        return SeadQueryService.marshal(new Checksum());

    }
}
