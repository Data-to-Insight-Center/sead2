/*
 *
 * Copyright 2015 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author isuriara@indiana.edu
 */

package org.iu.sead.cloud;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import com.sun.jersey.api.client.ClientResponse;

import org.bson.Document;
import org.iu.sead.cloud.util.Constants;
import org.iu.sead.cloud.util.MongoDB;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

@Path("/researchobjects")
public class ROSearch {

    private MongoCollection<Document> publicationsCollection = null;
    private CacheControl control = new CacheControl();

    public ROSearch() {
        MongoDatabase db = MongoDB.getServicesDB();
        publicationsCollection = db.getCollection(MongoDB.researchObjects);
        control.setNoCache(true);
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllPublishedROs() {
        FindIterable<Document> iter = publicationsCollection.find(createPublishedFilter()
                .append("Repository", Constants.repoName));
        setROProjection(iter);
        MongoCursor<Document> cursor = iter.iterator();
        JSONArray array = new JSONArray();
        while (cursor.hasNext()) {
            Document document = cursor.next();
            reArrangeDocument(document);
            array.put(JSON.parse(document.toJson()));
        }
        return Response.ok(array.toString()).cacheControl(control).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRODetails(@PathParam("id") String id) {
        FindIterable<Document> iter = publicationsCollection.find(createPublishedFilter()
                .append("Repository", Constants.repoName)
                .append("Aggregation.Identifier", id));
        if (iter == null) {
            return Response
                    .status(ClientResponse.Status.NOT_FOUND)
                    .entity(new JSONObject().put("Error", "Cannot find RO with id " + id).toString())
                    .build();
        }
        setROProjection(iter);
        Document document = iter.first();
        if (document == null) {
            return Response
                    .status(ClientResponse.Status.NOT_FOUND)
                    .entity(new JSONObject().put("Error", "Cannot find RO with id " + id).toString())
                    .build();
        }
        reArrangeDocument(document);
        return Response.ok(document.toJson()).cacheControl(control).build();
    }

    private Document createPublishedFilter() {
        // find only published ROs. there should be a Status with stage=Success
        Document stage = new Document("stage", Constants.successStage);
        Document elem = new Document("$elemMatch", stage);
        return new Document("Status", elem);
    }

    private void setROProjection(FindIterable<Document> iter) {
        iter.projection(new Document("Status", 1)
                .append("Repository", 1)
                .append("Aggregation.Identifier", 1)
                .append("Aggregation.Creator", 1)
                .append("Aggregation.Title", 1)
                .append("Aggregation.Contact", 1)
                .append("Aggregation.Abstract", 1)
                .append("_id", 0));
    }

    private void reArrangeDocument(Document doc) {
        // get elements inside Aggregation to top level
        Document agg = (Document) doc.get("Aggregation");
        for (String key : agg.keySet()) {
            doc.append(key, agg.getString(key));
        }
        doc.remove("Aggregation");
        // extract doi and remove Status
        ArrayList<Document> statusArray = (ArrayList<Document>) doc.get("Status");
        String doi = null;
        for (Document status : statusArray) {
            if (Constants.successStage.equals(status.getString("stage"))) {
                doi = status.getString("message");
            }
        }
        doc.append("DOI", doi);
        doc.remove("Status");
    }

}
