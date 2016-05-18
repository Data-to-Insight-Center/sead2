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

package org.sead.va.dataone.util;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.json.JSONObject;
import org.sead.va.dataone.Metadata;
import org.xml.sax.SAXException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Monitor function
 */
//monitor/ping

@Path("/util")
public class DataOneUtil {

    private MongoCollection<Document> fgdcCollection = null;
    private MongoDatabase metaDb = null;

    public DataOneUtil() throws IOException, SAXException, ParserConfigurationException {
        super();
        metaDb = MongoDB.getServicesDB();
        fgdcCollection = metaDb.getCollection(MongoDB.fgdc);
    }

    @GET
    @Path("setObsolete/{newId}/{oldId}")
    @Produces(MediaType.APPLICATION_XML)
    public Response removeObject(@PathParam("newId") String newId, @PathParam("oldId") String oldId) {

        String errorMsg = "<error name=\"NotFound\" errorCode=\"404\" detailCode=\"1060\" pid=\"" +
                URLEncoder.encode(newId) + "|" + URLEncoder.encode(oldId) + " \" nodeId=\"" + Constants.NODE_IDENTIFIER + "\">\n" +
                "<description>The specified object(s) does not exist on this node.</description>\n" +
                "</error>";

        FindIterable<Document> newIter = fgdcCollection.find(new Document(Constants.META_INFO + "." + Constants.FGDC_ID, URLEncoder.encode(newId)));
        FindIterable<Document> oldIter = fgdcCollection.find(new Document(Constants.META_INFO + "." + Constants.FGDC_ID, URLEncoder.encode(oldId)));

        if (newIter != null && newIter.first() != null && oldIter != null && oldIter.first() != null) {

            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            Date now = new Date();
            String strDate = sdfDate.format(now);
            Metadata metadata = null;

            try {
                metadata = new Metadata();
            } catch (IOException e) {
                throw new ServerErrorException("<error>\n<description>" + e.getMessage() + "</description>\n</error>");
            } catch (SAXException e) {
                throw new ServerErrorException("<error>\n<description>" + e.getMessage() + "</description>\n</error>");
            } catch (ParserConfigurationException e) {
                throw new ServerErrorException("<error>\n<description>" + e.getMessage() + "</description>\n</error>");
            }

            JSONObject newMetaInfo = new JSONObject(((Document) newIter.first().get(Constants.META_INFO)).toJson());
            newMetaInfo.put(Constants.META_UPDATE_DATE, strDate);
            newMetaInfo.put(Constants.OBSOLETES, URLEncoder.encode(oldId));
            metadata.updateMetadata(null, null, newId, newMetaInfo.toString());

            JSONObject oldMetaInfo = new JSONObject(((Document) oldIter.first().get(Constants.META_INFO)).toJson());
            oldMetaInfo.put(Constants.META_UPDATE_DATE, strDate);
            oldMetaInfo.put(Constants.OBSOLETED_BY, URLEncoder.encode(newId));
            metadata.updateMetadata(null, null, oldId, oldMetaInfo.toString());

            return Response
                    .status(Response.Status.OK)
                    .entity("<message>Metadata updated successfully</message>")
                    .type(MediaType.APPLICATION_XML)
                    .build();

        } else {
            throw new NotFoundException(errorMsg);
        }

    }
}
