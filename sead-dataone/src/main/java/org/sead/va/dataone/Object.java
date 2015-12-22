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


import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.codec.binary.Hex;
import org.bson.Document;
import org.dataone.service.types.v1.*;
import org.jibx.runtime.JiBXException;
import org.json.JSONObject;
import org.sead.va.dataone.util.Constants;
import org.sead.va.dataone.util.MongoDB;
import org.sead.va.dataone.util.SeadQueryService;
import org.xml.sax.SAXException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


/*
 * Returns list of objects and also datastream for individual objects
*/

@Path("/mn/v1/object")
public class Object {

    private final static int MAX_MATCHES = 10000;
    private MongoCollection<Document> fgdcCollection = null;
    private MongoDatabase metaDb = null;


    public Object() throws IOException, SAXException, ParserConfigurationException {
        metaDb = MongoDB.getServicesDB();
        fgdcCollection = metaDb.getCollection(MongoDB.fgdc);
    }

    @Context
    ServletContext context;

    @GET
    @Path("/{objectId}")
    @Produces(MediaType.APPLICATION_XML)
    public Response getObject(@Context HttpServletRequest request,
                              @HeaderParam("user-agent") String userAgent,
                              @PathParam("objectId") String objectId) throws IOException {


        String test ="<error name=\"NotFound\" errorCode=\"404\" detailCode=\"1020\" pid=\""+URLEncoder.encode(objectId)+"\" nodeId=\""+Constants.NODE_IDENTIFIER+"\">\n" +
                "<description>The specified object does not exist on this node.</description>\n" +
                "<traceInformation>\n" +
                "method: mn.get hint: http://cn.dataone.org/cn/resolve/"+URLEncoder.encode(objectId)+"\n" +
                "</traceInformation>\n" +
                "</error>";

        String id = URLEncoder.encode(objectId);

        FindIterable<Document> iter = fgdcCollection.find(new Document(Constants.META_INFO + "." + Constants.FGDC_ID, id));
        if(iter != null && iter.first() != null){
            JSONObject object = new JSONObject(iter.first());
            JSONObject metaInfo = (JSONObject) object.get(Constants.META_INFO);
            String fgdcMetadata = object.get(Constants.METADATA).toString();
            String metadataFormat = (String) metaInfo.get(Constants.META_FORMAT);

            String lastFormat = null;

            if (SeadQueryService.sead2d1Format.get(metadataFormat) != null) {
                lastFormat = SeadQueryService.mimeMapping.get(SeadQueryService.sead2d1Format.get(metadataFormat));
            } else {
                lastFormat = SeadQueryService.mimeMapping.get(metadataFormat);
            }

            Response.ResponseBuilder responseBuilder = Response.ok(new ByteArrayInputStream(fgdcMetadata.getBytes()));
            responseBuilder.header("DataONE-SerialVersion", "1");

            if (lastFormat != null) {
                String[] format = lastFormat.split(",");
                if (format.length > 1) {
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
        } else {
            return Response.status(ClientResponse.Status.NOT_FOUND).entity(test).build();
        }
    }


    @POST
    @Path("/{objectId}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response addObject(@Context HttpServletRequest request,
                              @PathParam("objectId") String id,
                              String fgdcString) throws UnsupportedEncodingException {

        Document metaInfo = new Document();
        metaInfo.put(Constants.META_FORMAT, "http://www.fgdc.gov/schemas/metadata/fgdc-std-001-1998.xsd");
        metaInfo.put(Constants.RO_ID, id);

        org.w3c.dom.Document doc = null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(new ByteArrayInputStream(fgdcString.getBytes()));
        } catch (ParserConfigurationException e) {
            System.out.println(e.getMessage());
        } catch (SAXException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        org.w3c.dom.NodeList origins = doc.getElementsByTagName("origin");
        String creator = "";
        for (int i = 0; i < origins.getLength(); i++) {
            org.w3c.dom.Node origin = origins.item(i);
            creator = origin.getChildNodes().item(0).getNodeValue();
            break;
        }
        creator = URLEncoder.encode(creator.split(":")[0].replace(" ","").replace(",",""));
        String fgdcId = "seadva-" + creator + "-" + UUID.randomUUID().toString();
        metaInfo.put(Constants.FGDC_ID, fgdcId);

        final byte[] utf8Bytes = fgdcString.getBytes("UTF-8");
        metaInfo.put(Constants.SIZE, utf8Bytes.length);

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        metaInfo.put(Constants.META_UPDATE_DATE, strDate);

        try {
            DigestInputStream digestStream =
                    new DigestInputStream(new ByteArrayInputStream(fgdcString.getBytes()), MessageDigest.getInstance("SHA-1"));
            if (digestStream.read() != -1) {
                byte[] buf = new byte[1024];
                while (digestStream.read(buf) != -1);
            }
            byte[] digest = digestStream.getMessageDigest().digest();
            metaInfo.put(Constants.FIXITY_FORMAT, "SHA-1");
            metaInfo.put(Constants.FIXITY_VAL, new String(Hex.encodeHex(digest)));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        fgdcCollection.deleteMany(new Document(Constants.META_INFO + "." + Constants.RO_ID, id));
        Document document = new Document();
        document.put(Constants.META_INFO, metaInfo);
        document.put(Constants.METADATA, fgdcString);
        fgdcCollection.insertOne(document);

        return Response.ok().build();
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_XML)
    public String listObjects(@Context HttpServletRequest request,
                                   @HeaderParam("user-agent") String userAgent,
                                   @QueryParam("start") int start,
                                   @QueryParam("count") String countStr,
                                   @QueryParam("formatId") String formatId,
                                   @QueryParam("fromDate") String fromDate,
                                   @QueryParam("toDate") String toDate)
            throws ParseException, TransformerException, JiBXException {

        int count = MAX_MATCHES;
        boolean countZero = false;
        if(countStr!=null){
            count = Integer.parseInt(countStr);
            if(count <= 0)
                countZero = true;
        }

        ObjectList objectList = new ObjectList();
        int totalMongoCount = (int)fgdcCollection.count();
        if(countZero){
            objectList.setCount(0);
            objectList.setTotal(totalMongoCount);
            objectList.setStart(start);
            return SeadQueryService.marshal(objectList);
        }

        BasicDBObject andQuery = new BasicDBObject();
        List<BasicDBObject> obj = new ArrayList<BasicDBObject>();
        if(formatId!=null) {
            String tempFormat = SeadQueryService.d12seadFormat.get(formatId);
            if(tempFormat ==null)
                tempFormat = formatId;
            obj.add(new BasicDBObject(Constants.META_INFO + "." + Constants.META_FORMAT, tempFormat));
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        if(fromDate!=null) {
            fromDate = fromDate.replace("+00:00","Z");
            obj.add(new BasicDBObject(Constants.META_INFO + "." + Constants.META_UPDATE_DATE,
                    new BasicDBObject("$gte", fromDate)));
        }
        if(toDate!=null) {
            toDate = toDate.replace("+00:00","Z");
            obj.add(new BasicDBObject(Constants.META_INFO + "." + Constants.META_UPDATE_DATE,
                    new BasicDBObject("$lte", toDate)));
        }

        if(obj.size() != 0) {
            andQuery.put("$and", obj);
        }

        FindIterable<Document> iter = fgdcCollection.find(andQuery)
                .limit(count)
                .skip(start)
                .sort(new Document(Constants.META_INFO + "." + Constants.META_UPDATE_DATE , 1));
        MongoCursor<Document> cursor = iter.iterator();
        int totalResutls = 0;


        while (cursor.hasNext()) {
            JSONObject object = new JSONObject(cursor.next().toJson().toString());
            JSONObject metaInfo = (JSONObject) object.get(Constants.META_INFO);
            String fgdcMetadata = object.get(Constants.METADATA).toString();

            String date = (String) metaInfo.get(Constants.META_UPDATE_DATE);
            ObjectInfo objectInfo =  new ObjectInfo();
            Identifier identifier = new Identifier();

            String id = (String) metaInfo.get(Constants.FGDC_ID);
            identifier.setValue(id);//URLEncoder.encode(id));
            objectInfo.setIdentifier(identifier);

            int size = Integer.parseInt(metaInfo.get(Constants.SIZE).toString());
            objectInfo.setSize(BigInteger.valueOf(size < 0 ? 10 : size));


            String lastFormat = "TestFormatId";
            if (SeadQueryService.sead2d1Format.get(metaInfo.get(Constants.META_FORMAT)) != null) {
                ObjectFormatIdentifier formatIdentifier = new ObjectFormatIdentifier();
                formatIdentifier.setValue(SeadQueryService.sead2d1Format.get(metaInfo.get(Constants.META_FORMAT)));
                objectInfo.setFormatId(formatIdentifier);
            }

            if(objectInfo.getFormatId()==null) {
                ObjectFormatIdentifier formatIdentifier = new ObjectFormatIdentifier();
                formatIdentifier.setValue(lastFormat);
                objectInfo.setFormatId(formatIdentifier);
            }

            objectInfo.setDateSysMetadataModified(simpleDateFormat.parse(date));

            Checksum checksum = new Checksum();
            checksum.setAlgorithm("MD5");
            checksum.setValue("testChecksum");

            String fixityFormat = (String) metaInfo.get(Constants.FIXITY_FORMAT);
            String fixityValue = (String) metaInfo.get(Constants.FIXITY_VAL);
            if (fixityFormat.equalsIgnoreCase("MD-5")) {
                checksum.setAlgorithm("MD5");
                checksum.setValue(fixityValue);
            }
            if (fixityFormat.equalsIgnoreCase("SHA-1")) {
                checksum.setAlgorithm("SHA-1");
                checksum.setValue(fixityValue);
            }

            objectInfo.setChecksum(checksum);
            objectList.getObjectInfoList().add(objectInfo);
            totalResutls++;
        }

        objectList.setCount(totalResutls);
        objectList.setTotal(totalMongoCount);
        objectList.setStart(start);
        return SeadQueryService.marshal(objectList);
    }

    @GET
    @Path("/total")
    @Produces(MediaType.APPLICATION_XML)
    public String listObjects(@Context HttpServletRequest request,
                              @HeaderParam("user-agent") String userAgent) {
        Long total = fgdcCollection.count();
        return String.format("%d", total);
    }
}
