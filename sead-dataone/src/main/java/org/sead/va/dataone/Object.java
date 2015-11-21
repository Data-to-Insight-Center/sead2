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
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.dataone.service.types.v1.*;
import org.jibx.runtime.JiBXException;
import org.json.JSONObject;
import org.sead.va.dataone.util.MongoDB;
import org.sead.va.dataone.util.SeadQueryService;
import org.xml.sax.SAXException;
import org.apache.commons.codec.binary.Hex;


import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
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


    @POST
    @Path("{objectId}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response addObject(@Context HttpServletRequest request,
                              String fgdcString,
                              @PathParam("id") String id) throws UnsupportedEncodingException {

        Document metaInfo = new Document();
        metaInfo.put("metaFormat", "http://www.fgdc.gov/schemas/metadata/fgdc-std-001-1998.xsd");
        metaInfo.put("@id", id);

        final byte[] utf8Bytes = fgdcString.getBytes("UTF-8");
        metaInfo.put("size", utf8Bytes.length);

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        metaInfo.put("metadataUpdateDate", strDate);

        try {
            DigestInputStream digestStream =
                    new DigestInputStream(new ByteArrayInputStream(fgdcString.getBytes()), MessageDigest.getInstance("SHA-1"));
            if (digestStream.read() != -1) {
                byte[] buf = new byte[1024];
                while (digestStream.read(buf) != -1);
            }
            byte[] digest = digestStream.getMessageDigest().digest();
            metaInfo.put("fixityAlgo", "SHA-1");
            metaInfo.put("fixityVal", new String(Hex.encodeHex(digest)));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        fgdcCollection.deleteMany(new Document("@id", id));
        Document document = new Document();
        document.put("metaInfo", metaInfo);
        document.put("metadata", fgdcString);
        fgdcCollection.insertOne(document);

        return Response.ok().build();
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
            throws ParseException, TransformerException, JiBXException {

        int count = 0;
        boolean countZero = false;
        if(countStr!=null){
            count = Integer.parseInt(countStr);
            if(count <= 0)
                countZero = true;
        }

        FindIterable<Document> iter = fgdcCollection.find();
        MongoCursor<Document> cursor = iter.iterator();
        int totalResutls = 0;
        ObjectList objectList = new ObjectList();


        while (cursor.hasNext()) {
            JSONObject object = new JSONObject(cursor.next().toJson().toString());
            JSONObject metaInfo = (JSONObject) object.get("metaInfo");
            String fgdcMetadata = object.get("meatadata").toString();

            String date = (String) metaInfo.get("metadataUpdateDate");
            ObjectInfo objectInfo =  new ObjectInfo();
            Identifier identifier = new Identifier();

            String id = (String) metaInfo.get("@id");//TODO get DOI
            /*String id = null;
            Collection<DcsResourceIdentifier> altIds = dcsFile.getAlternateIds();
            for(DcsResourceIdentifier altId: altIds){
                if(altId.getTypeId().equalsIgnoreCase("dataone")){
                    id = altId.getIdValue().replace("http://dx.doi.org/","doi-");
                    int index;
                    if(doiCount.containsKey(id)){
                        index = doiCount.get(id);
                        index ++;
                    }
                    else
                        index = 1;

                    doiCount.put(id, index);
                    //  id += "_" + index;
                    break;
                }
            }
            if(id==null)
                id = dcsFile.getId();*/
            identifier.setValue(id);//URLEncoder.encode(id));
            objectInfo.setIdentifier(identifier);

            int size = Integer.parseInt(metaInfo.get("size").toString());
            objectInfo.setSize(BigInteger.valueOf(size < 0 ? 10 : size));


            String lastFormat = "TestFormatId";
            if (SeadQueryService.sead2d1Format.get(metaInfo.get("metaFormat")) != null) {
                ObjectFormatIdentifier formatIdentifier = new ObjectFormatIdentifier();
                formatIdentifier.setValue(SeadQueryService.sead2d1Format.get(metaInfo.get("metaFormat")));
                objectInfo.setFormatId(formatIdentifier);
            }

            if(objectInfo.getFormatId()==null) {
                ObjectFormatIdentifier formatIdentifier = new ObjectFormatIdentifier();
                formatIdentifier.setValue(lastFormat);
                objectInfo.setFormatId(formatIdentifier);
            }

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            objectInfo.setDateSysMetadataModified(simpleDateFormat.parse(date));

            Checksum checksum = new Checksum();
            checksum.setAlgorithm("MD5");
            checksum.setValue("testChecksum");

            if (size > 0) {
                String fixityFormat = (String) metaInfo.get("fixityAlgo");
                String fixityValue = (String) metaInfo.get("fixityVal");
                if (fixityFormat.equalsIgnoreCase("MD-5")) {
                    checksum.setAlgorithm("MD5");
                    checksum.setValue(fixityValue);
                }
                if (fixityFormat.equalsIgnoreCase("SHA-1")) {
                    checksum.setAlgorithm("SHA-1");
                    checksum.setValue(fixityValue);
                }
            }
            objectInfo.setChecksum(checksum);
            objectList.getObjectInfoList().add(objectInfo);
            totalResutls++;
        }

        if(countZero){
            objectList.setCount(0);
            objectList.setTotal((int)totalResutls);
            objectList.setStart(start);
            return SeadQueryService.marshal(objectList);
        }

        objectList.setCount(totalResutls);//TODO:check this count and total
        objectList.setTotal((int)totalResutls);
        objectList.setStart(start);
        return SeadQueryService.marshal(objectList);

    }
}
