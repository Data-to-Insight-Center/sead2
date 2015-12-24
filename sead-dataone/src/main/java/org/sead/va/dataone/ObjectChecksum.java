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
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.dataone.service.types.v1.Checksum;
import org.dataone.service.types.v1.Event;
import org.jibx.runtime.JiBXException;
import org.json.JSONObject;
import org.sead.va.dataone.util.Constants;
import org.sead.va.dataone.util.LogEvent;
import org.sead.va.dataone.util.MongoDB;
import org.sead.va.dataone.util.SeadQueryService;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.transform.TransformerException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


/*
 * Return checksum for files
 */


@Path("/mn/v1/checksum")
public class ObjectChecksum {

    private MongoCollection<Document> fgdcCollection = null;
    private MongoDatabase metaDb = null;

    public ObjectChecksum() {
        metaDb = MongoDB.getServicesDB();
        fgdcCollection = metaDb.getCollection(MongoDB.fgdc);
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("{objectId}")
    public Response getChecksum(@Context HttpServletRequest request,
                                @HeaderParam("user-agent") String userAgent,
                                @PathParam("objectId") String objectId,
                                @QueryParam("checksumAlgorithm") String checksumAlgorithm) throws JiBXException, TransformerException {

        String errorMsg = "<error name=\"NotFound\" errorCode=\"404\" detailCode=\"1060\" pid=\"" + URLEncoder.encode(objectId) + "\" nodeId=\"" + Constants.NODE_IDENTIFIER + "\">\n" +
                "<description>The specified object does not exist on this node.</description>\n" +
                "<traceInformation>\n" +
                "method: mn.getChecksum hint: http://cn.dataone.org/cn/resolve/" + URLEncoder.encode(objectId) + "\n" +
                "</traceInformation>\n" +
                "</error>";
        if (objectId.contains("TestingNotFound") || objectId.contains("Test"))
            throw new NotFoundException(errorMsg);

        objectId = URLEncoder.encode(objectId);

        BasicDBObject andQuery = new BasicDBObject();
        List<BasicDBObject> obj = new ArrayList<BasicDBObject>();
        obj.add(new BasicDBObject(Constants.META_INFO + "." + Constants.FGDC_ID, objectId));

        if(checksumAlgorithm!=null)
            obj.add(new BasicDBObject(Constants.META_INFO + "." + Constants.FIXITY_FORMAT, checksumAlgorithm));

        andQuery.put("$and", obj);

        String ip = null;
        if (request != null)
            ip = request.getRemoteAddr();

        FindIterable<Document> iter = fgdcCollection.find(andQuery);
        if (iter != null && iter.first() != null) {

            JSONObject object = new JSONObject(iter.first().toJson().toString());
            JSONObject metaInfo = (JSONObject) object.get(Constants.META_INFO);
            String fixityAlgo = (String) metaInfo.get(Constants.FIXITY_FORMAT);
            String fixityVal = (String) metaInfo.get(Constants.FIXITY_VAL);

            Checksum checksum = new Checksum();

            if (checksumAlgorithm != null) {
                if (SeadQueryService.sead2d1fixity.get(fixityAlgo).equals(checksumAlgorithm)) {
                    checksum.setAlgorithm(checksumAlgorithm);
                    checksum.setValue(fixityVal);

                    LogEvent readEvent = SeadQueryService.dataOneLogService.creatEvent(Event.READ.xmlValue(), userAgent, ip, objectId);
                    SeadQueryService.dataOneLogService.indexLog(readEvent);

                    return Response.ok(SeadQueryService.marshal(checksum)).build();
                }
            } else {
                checksum.setAlgorithm(SeadQueryService.sead2d1fixity.get(fixityAlgo));
                checksum.setValue(fixityVal);

                LogEvent readEvent = SeadQueryService.dataOneLogService.creatEvent(Event.READ.xmlValue(), userAgent, ip, objectId);
                SeadQueryService.dataOneLogService.indexLog(readEvent);

                return Response.ok(SeadQueryService.marshal(checksum)).build();
            }
            return Response.ok(SeadQueryService.marshal(new Checksum())).build();
        } else {
            throw new NotFoundException(errorMsg);
        }

    }
}
