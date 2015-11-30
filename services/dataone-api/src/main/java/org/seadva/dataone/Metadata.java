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
import org.dataconservancy.model.dcs.DcsFixity;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataone.service.types.v1.*;
import org.jibx.runtime.JiBXException;
import org.seadva.ingest.Events;
import org.seadva.model.SeadEvent;
import org.seadva.model.SeadFile;
import org.seadva.model.pack.ResearchObject;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

/*
 * List all system metadata and system metadata per object
 */

@Path("/mn/v1/meta")
public class Metadata{




    public Metadata() throws IOException, SAXException, ParserConfigurationException {
        super();
    }

    @GET
    public void testmeta() {
        return;
    }
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("{objectId}")
    public String getMetadata(@Context HttpServletRequest request,
                                      @HeaderParam("user-agent") String userAgent,
                                      @PathParam("objectId") String objectId) throws JiBXException, ParseException, TransformerException {


        String test ="<error name=\"NotFound\" errorCode=\"404\" detailCode=\"1060\" pid=\""+ URLEncoder.encode(objectId)+"\" nodeId=\""+SeadQueryService.NODE_IDENTIFIER+"\">\n" +
                "<description>The specified object does not exist on this node.</description>\n" +
                "<traceInformation>\n" +
                "method: mn.getSystemMetadata hint: http://cn.dataone.org/cn/resolve/"+URLEncoder.encode(objectId)+"\n" +
                "</traceInformation>\n" +
                "</error>";

        //get the file metadata
        SystemMetadata metadata = new SystemMetadata();

        metadata.setSerialVersion(BigInteger.ONE);
        Identifier identifier = new Identifier();
        identifier.setValue(objectId);
        metadata.setIdentifier(identifier);

        QueryResult<DcsEntity> result = null;
        try {
            result = SeadQueryService.queryService.query(SolrQueryUtil
                    .createLiteralQuery("resourceValue",
                            objectId), 0, -1); //sort by filename
        } catch (QueryServiceException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        String date = null;
        SeadFile file = null;
        String depositDate = null;
        List<QueryMatch<DcsEntity>> matches = result.getMatches();
        if(matches.size()==0)
            throw new NotFoundException(test);
        for(QueryMatch<DcsEntity> entity: matches){
            if(entity.getObject() instanceof SeadFile)  {
                file = (SeadFile)entity.getObject();
                date =  file.getMetadataUpdateDate();
                depositDate = file.getDepositDate();

                metadata.setSize(BigInteger.valueOf(file.getSizeBytes()<0?10:file.getSizeBytes()));


                String lastFormat = "TestFormatId";
                if(file.getFormats().size()>0){
                    for(DcsFormat format:file.getFormats()){
                        if(SeadQueryService.sead2d1Format.get(format.getFormat())!=null) {
                            ObjectFormatIdentifier formatIdentifier = new ObjectFormatIdentifier();
                            formatIdentifier.setValue(SeadQueryService.sead2d1Format.get(format.getFormat()));
                            metadata.setFormatId(formatIdentifier);
                            break;
                        }
                        lastFormat = format.getFormat();
                    }
                }

                if(metadata.getFormatId()==null){
                    ObjectFormatIdentifier formatIdentifier = new ObjectFormatIdentifier();
                    formatIdentifier.setValue(lastFormat);
                    metadata.setFormatId(formatIdentifier);
                }


                if(file.getFixity().size()>0){
                    DcsFixity[] fileFixity = file.getFixity().toArray(new DcsFixity[file.getFixity().size()]);
                    Checksum checksum = new Checksum();
                    for(int j=0;j<file.getFixity().size();j++){
                        if(fileFixity[j].getAlgorithm().equalsIgnoreCase("MD-5"))
                        {
                            checksum.setAlgorithm("MD5");
                            checksum.setValue(fileFixity[j].getValue());
                            metadata.setChecksum(checksum);
                        }
                        if(fileFixity[j].getAlgorithm().equalsIgnoreCase("SHA-1"))
                        {
                            checksum.setAlgorithm("SHA-1");
                            checksum.setValue(fileFixity[j].getValue());
                            metadata.setChecksum(checksum);
                            break;
                        }
                    }
                }

            }
            else
                return null;
            break;
        }


        //

        if(metadata.getChecksum()==null){
            Checksum chcksum = new Checksum();
            chcksum.setAlgorithm("MD5");
            chcksum.setValue("testChecksum");
            metadata.setChecksum(chcksum);
        }

        AccessPolicy accessPolicy = new AccessPolicy();
        AccessRule rule = new AccessRule();

        Subject subject = new Subject();
        subject.setValue("public");
        rule.getPermissionList().add(Permission.READ);
        rule.getSubjectList().add(subject);
        accessPolicy.getAllowList().add(rule);
        metadata.setAccessPolicy(accessPolicy);

        Subject subject1 = new Subject();
        subject1.setValue("SEAD");
        metadata.setSubmitter(subject1);

        Subject rightsHolder = new Subject();
        rightsHolder.setValue("NCED");
        metadata.setRightsHolder(rightsHolder);

        ReplicationPolicy replicationPolicy = new ReplicationPolicy();
        replicationPolicy.setReplicationAllowed(false);
        metadata.setReplicationPolicy(replicationPolicy);


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        metadata.setDateSysMetadataModified(simpleDateFormat.parse(date));
        metadata.setDateUploaded(simpleDateFormat.parse(depositDate));


        NodeReference nodeReference = new NodeReference();
        nodeReference.setValue(SeadQueryService.NODE_IDENTIFIER);
        metadata.setOriginMemberNode(nodeReference);
        metadata.setAuthoritativeMemberNode(nodeReference);

        String ip = null;
        if(request!=null)
            ip = request.getRemoteAddr();

        SeadEvent readEvent  = SeadQueryService.dataOneLogService.creatEvent( Events.FILEMETADATA_D1READ, userAgent, ip, file);

        ResearchObject eventsSip = new ResearchObject();
        eventsSip.addEvent(readEvent);

        SeadQueryService.dataOneLogService.indexLog(eventsSip);

        return SeadQueryService.marshal(metadata);
    }

}
