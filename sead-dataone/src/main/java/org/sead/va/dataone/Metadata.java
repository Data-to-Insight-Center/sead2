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
import org.dataone.service.types.v1.*;
import org.jibx.runtime.JiBXException;
import org.json.JSONObject;
import org.sead.va.dataone.util.Constants;
import org.sead.va.dataone.util.MongoDB;
import org.sead.va.dataone.util.SeadQueryService;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/*
 * List all system metadata and system metadata per object
 */

@Path("/mn/v1/meta")
public class Metadata {

    private MongoCollection<Document> fgdcCollection = null;
    private MongoDatabase metaDb = null;

    public Metadata() throws IOException, SAXException, ParserConfigurationException {
        super();
        metaDb = MongoDB.getServicesDB();
        fgdcCollection = metaDb.getCollection(MongoDB.fgdc);
    }

    @GET
    public void testmeta() {
        return;
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("{objectId}")
    public Response getMetadata(@Context HttpServletRequest request,
                                @HeaderParam("user-agent") String userAgent,
                                @PathParam("objectId") String objectId) throws JiBXException, ParseException, TransformerException {


        String test = "<error name=\"NotFound\" errorCode=\"404\" detailCode=\"1060\" pid=\"" + URLEncoder.encode(objectId) + "\" nodeId=\"" + Constants.NODE_IDENTIFIER + "\">\n" +
                "<description>The specified object does not exist on this node.</description>\n" +
                "<traceInformation>\n" +
                "method: mn.getSystemMetadata hint: http://cn.dataone.org/cn/resolve/" + URLEncoder.encode(objectId) + "\n" +
                "</traceInformation>\n" +
                "</error>";

        //get the file metadata
        SystemMetadata metadata = new SystemMetadata();

        metadata.setSerialVersion(BigInteger.ONE);
        Identifier identifier = new Identifier();
        identifier.setValue(objectId);
        metadata.setIdentifier(identifier);


        FindIterable<Document> iter = fgdcCollection.find(new Document(Constants.META_INFO + "." + Constants.FGDC_ID, objectId));
        if (iter != null && iter.first() != null) {
            JSONObject object = new JSONObject(iter.first().get(Constants.METADATA).toString());
            JSONObject metaInfo = (JSONObject) object.get(Constants.META_INFO);

            String date = (String) metaInfo.get(Constants.META_UPDATE_DATE);
            int size = Integer.parseInt(metaInfo.get(Constants.SIZE).toString());
            String metadataFormat = (String) metaInfo.get(Constants.META_FORMAT);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            metadata.setDateSysMetadataModified(simpleDateFormat.parse(date));
            //metadata.setDateUploaded(simpleDateFormat.parse(depositDate));//TODO add deposit date

            metadata.setSize(BigInteger.valueOf(size < 0 ? 10 : size));


            String lastFormat = metadataFormat != null ? metadataFormat : "TestFormatId";

            if (SeadQueryService.sead2d1Format.get(metadataFormat) != null) {
                ObjectFormatIdentifier formatIdentifier = new ObjectFormatIdentifier();
                formatIdentifier.setValue(SeadQueryService.sead2d1Format.get(metadataFormat));
                metadata.setFormatId(formatIdentifier);
            }


            if (metadata.getFormatId() == null) {
                ObjectFormatIdentifier formatIdentifier = new ObjectFormatIdentifier();
                formatIdentifier.setValue(lastFormat);
                metadata.setFormatId(formatIdentifier);
            }

            String fixityAlgo = (String) metaInfo.get(Constants.FIXITY_FORMAT);
            String fixityVal = (String) metaInfo.get(Constants.FIXITY_VAL);

            Checksum checksum = new Checksum();
            checksum.setAlgorithm("MD5");
            checksum.setValue("testChecksum");

            if (fixityAlgo.equalsIgnoreCase("MD-5")) {
                checksum.setAlgorithm("MD5");
                checksum.setValue(fixityVal);
                metadata.setChecksum(checksum);
            }
            if (fixityAlgo.equalsIgnoreCase("SHA-1")) {
                checksum.setAlgorithm("SHA-1");
                checksum.setValue(fixityVal);
                metadata.setChecksum(checksum);
            }

        } else {
            return Response.status(ClientResponse.Status.NOT_FOUND).build();
        }

        if (metadata.getChecksum() == null) {
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

        NodeReference nodeReference = new NodeReference();
        nodeReference.setValue(Constants.NODE_IDENTIFIER);
        metadata.setOriginMemberNode(nodeReference);
        metadata.setAuthoritativeMemberNode(nodeReference);

        String ip = null;
        if (request != null)
            ip = request.getRemoteAddr();

        //SeadEvent readEvent  = SeadQueryService.dataOneLogService.creatEvent( Events.FILEMETADATA_D1READ, userAgent, ip, file);

        //ResearchObject eventsSip = new ResearchObject();
        //eventsSip.addEvent(readEvent);

        //SeadQueryService.dataOneLogService.indexLog(eventsSip);

        return Response.ok(SeadQueryService.marshal(metadata)).build();
    }

}
