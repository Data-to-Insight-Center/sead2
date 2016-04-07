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
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.bson.Document;
import org.iu.sead.cloud.util.Constants;
import org.iu.sead.cloud.util.MongoDB;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sead.monitoring.engine.SeadMon;
import org.sead.monitoring.engine.enums.MonConstants;

import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@Path("/researchobjects")
public class ROSearch {

    private MongoCollection<Document> publicationsCollection = null;
    private CacheControl control = new CacheControl();
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
    WebResource pdtResource;

    public ROSearch() {
        MongoDatabase db = MongoDB.getServicesDB();
        publicationsCollection = db.getCollection(MongoDB.researchObjects);
        control.setNoCache(true);
        pdtResource = Client.create().resource(Constants.pdtURL);
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllPublishedROs() {
        Document query = createPublishedFilter().append("Repository", Constants.repoName);
        return getAllPublishedROs(query, null, null, null);
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFilteredListOfROs(String filterString) {

        SeadMon.addLog(MonConstants.Components.IU_SEAD_CLOUD_SEARCH, "ID", MonConstants.EventType.ACCESS);

        JSONObject filter = new JSONObject(filterString);
        String creator = filter.getString("Creator");
        String startDate = filter.getString("Start Date");
        String endDate = filter.getString("End Date");
        String searchString = filter.getString("Search String");
        String title = filter.getString("Title");

        // query
        Document query = createPublishedFilter().append("Repository", Constants.repoName);
        if (searchString != null && !"".equals(searchString)) {
            // doing a text search in entire RO using the given search string
            query = query.append("$text", new Document("$search", searchString));
        }

        if (title != null && !"".equals(title)) {
            // regex for title
            query = query.append("Aggregation.Title", new Document("$regex", title).append("$options", "i"));
        }

        Date start = null;
        Date end = null;
        try {
            if (startDate != null && !"".equals(startDate)) {
                start = simpleDateFormat.parse(startDate);
            }
            if (endDate != null && !"".equals(endDate)) {
                end = simpleDateFormat.parse(endDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // ignore
        }

        return getAllPublishedROs(query, start, end, "(?i)(.*)" + creator + "(.*)");
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

    private Response getAllPublishedROs(Document filter, Date start, Date end, String creatorRegex) {
        FindIterable<Document> iter = publicationsCollection.find(filter);
        setROProjection(iter);
        MongoCursor<Document> cursor = iter.iterator();
        JSONArray array = new JSONArray();
        while (cursor.hasNext()) {
            Document document = cursor.next();
            reArrangeDocument(document);
            if (withinDateRange(document.getString("Publication Date"), start, end) &&
                    creatorMatch(document.get("Creator"), creatorRegex)) {
                array.put(JSON.parse(document.toJson()));
            }
        }
        return Response.ok(array.toString()).cacheControl(control).build();
    }

    private boolean creatorMatch(Object creator, String creatorRegex) {
        if (creator == null || creatorRegex == null) {
            return true;
        }
        if (creator instanceof String) {
            return ((String) creator).matches(creatorRegex);
        } else if (creator instanceof ArrayList) {
            ArrayList<String> creators = (ArrayList<String>) creator;
            for (String cr : creators) {
                if (cr.matches(creatorRegex)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private boolean withinDateRange(String pubDateString, Date start, Date end) {
        try {
            Date pubDate = DateFormat.getDateTimeInstance().parse(pubDateString);
            if ((start != null && start.after(pubDate)) || (end != null && end.before(pubDate))) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            // ignore
        }
        return true;
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
                .append("Aggregation.Creator", 1)
                .append("Aggregation.Title", 1)
                .append("Aggregation.Contact", 1)
                .append("Aggregation.Abstract", 1)
                .append("Aggregation.Creation Date", 1)
                .append("_id", 0));
    }

    private void reArrangeDocument(Document doc) {
        // get elements inside Aggregation to top level
        Document agg = (Document) doc.get("Aggregation");
        for (String key : agg.keySet()) {
            doc.append(key, agg.get(key));
        }
        doc.remove("Aggregation");
        // extract doi and remove Status
        ArrayList<Document> statusArray = (ArrayList<Document>) doc.get("Status");
        String doi = "Not Found";
        String pubDate = "Not Found";
        for (Document status : statusArray) {
            if (Constants.successStage.equals(status.getString("stage"))) {
                doi = status.getString("message");
                pubDate = status.getString("date");
            }
        }
        // fixing DOIs which are not URLs
        if (!doi.startsWith("http")) {
            doi = "http://dx.doi.org/" + doi.substring(doi.indexOf(':') + 1);
        }
        doc.append("DOI", doi);
        doc.append("Publication Date", pubDate);

        // resolve creator if ID is provided
        Object creator = doc.get("Creator");
        if (creator instanceof String) {
            String creatorId = doc.getString("Creator");
            doc.put("Creator", getPersonName(creatorId));
        } else if (creator instanceof ArrayList) {
            ArrayList<String> creatorIds = (ArrayList<String>) creator;
            ArrayList<String> creatorNames = new ArrayList<String>();
            for (String id : creatorIds) {
                creatorNames.add(getPersonName(id));
            }
            doc.remove("Creator");
            doc.put("Creator", creatorNames);
        }

        doc.remove("Status");
    }

    private String getPersonName(String creator) {
        String personProfile = getPersonProfile(creator);
        if (personProfile == null) {
            return creator;
        } else {
            JSONObject profile = new JSONObject(personProfile);
            String givenName = profile.getString("givenName");
            String familyName = profile.getString("familyName");
            if (givenName == null && familyName == null) {
                return creator;
            }
            String fullName = (givenName == null ? "" : givenName) + " " + (familyName == null ? "" : familyName);
            return fullName.trim();
        }
    }

    private String getPersonProfile(String personID) {
        String encodedID;
        try {
            encodedID = URLEncoder.encode(personID, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        ClientResponse response = pdtResource.path("people/" + encodedID)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        if (response.getStatus() == 200) {
            return response.getEntity(String.class);
        } else {
            return null;
        }
    }

}
