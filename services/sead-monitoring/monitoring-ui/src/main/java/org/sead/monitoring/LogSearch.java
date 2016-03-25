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
 * @author charmadu@umail.iu.edu
 */

package org.sead.monitoring;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import com.sun.jersey.api.client.ClientResponse;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sead.monitoring.util.Constants;
import org.sead.monitoring.util.LogEvent;
import org.sead.monitoring.util.MongoDB;
import org.sead.monitoring.util.DateTimeUtil;

import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Path("/")
public class LogSearch {

    private MongoCollection<Document> publicationsCollection = null;
    private MongoCollection<Document> dataOneCollection = null;
    private CacheControl control = new CacheControl();

    public LogSearch() {
        MongoDatabase pdtDB = MongoDB.getPdtDB();
        MongoDatabase dataOneDB = MongoDB.getDataOneDB();
        publicationsCollection = pdtDB.getCollection(MongoDB.researchObjects);
        dataOneCollection = dataOneDB.getCollection(MongoDB.events);
        control.setNoCache(true);
    }

    @GET
    @Path("/dataone")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDataOneData(@QueryParam("start") int start,
                                   @QueryParam("count") String countStr,
                                   @QueryParam("event") String event,
                                   @QueryParam("pidFilter") String pidFilter,
                                   @QueryParam("fromDate") String fromDate,
                                   @QueryParam("toDate") String toDate) throws ParseException {
        JSONArray jsonArray = new JSONArray();

        BasicDBObject andQuery = new BasicDBObject();
        List<BasicDBObject> obj = new ArrayList<BasicDBObject>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        if (fromDate != null) {
            fromDate = fromDate.replace("+00:00", "Z");
            obj.add(new BasicDBObject("date", new BasicDBObject("$gte", fromDate)));
        }
        if (toDate != null) {
            toDate = toDate.replace("+00:00", "Z");
            obj.add(new BasicDBObject("date", new BasicDBObject("$lte", toDate)));
        }
        if(pidFilter != null){
            obj.add(new BasicDBObject("target", URLEncoder.encode(pidFilter)));
        }
        if(event != null){
            obj.add(new BasicDBObject("eventType", event));
        }
        if (obj.size() != 0) {
            andQuery.put("$and", obj);
        }

        List<LogEvent> result = queryLog(dataOneCollection, andQuery, countStr, start);
        Date startDate = simpleDateFormat.parse(result.get(0).getDate());
        Date endDate = simpleDateFormat.parse(result.get(result.size() - 1).getDate());
        long duration = Math.round((endDate.getTime() - startDate.getTime())*1.0/1000);
        String scale = DateTimeUtil.getTimeScale(duration);

        Map<Long, Integer> stats = new HashMap<Long, Integer>();

        for (LogEvent d1log : result) {
            if (d1log.getSubject() == null || d1log.getNodeIdentifier() == null) {
                continue;
            }
            Date date = simpleDateFormat.parse(d1log.getDate());
            long seconds = Math.round(date.getTime()*1.0 / 1000);
            long time = DateTimeUtil.getScaledTime(seconds, scale);

            if(stats.get(time) == null) {
                stats.put(time, 1);
            } else{
                int count = stats.get(time);
                stats.put(time, ++count);
            }
        }

        for(Long date: stats.keySet()){
            JSONObject dataObject = new JSONObject();
            dataObject.put("date", DateTimeUtil.getDateTime(date, scale));
            dataObject.put("count", stats.get(date));
            jsonArray.put(dataObject);
        }

        return Response.ok(jsonArray.toString()).build();
    }

    public List<LogEvent>  queryLog(MongoCollection collection, BasicDBObject query, String countStr, int start){

        int count = 80;
        if(countStr!=null && !countStr.equals(Constants.INFINITE))
            count = Integer.parseInt(countStr);
        start = start < 0 ? 0 : start;

        FindIterable<Document> iter;
        if(countStr!=null && countStr.equals(Constants.INFINITE)) {
            iter = collection.find(query)
                    .skip(start)
                    .sort(new BasicDBObject("date", 1));
        } else {
            iter = collection.find(query)
                    .limit(count)
                    .skip(start)
                    .sort(new BasicDBObject("date", 1));
        }
        List<LogEvent> logEvents = new ArrayList<LogEvent>();
        MongoCursor<Document> cursor = iter.iterator();
        try {
            while(cursor.hasNext()) {
                Document dbobj = cursor.next();
                //Converting BasicDBObject to a custom Class(LogEvent)
                LogEvent logEvent = (new Gson()).fromJson(dbobj.toJson(), LogEvent.class);
                logEvents.add(logEvent);
            }
        } finally {
            cursor.close();
        }
        return logEvents;
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllPublishedROs() {
        FindIterable<Document> iter = publicationsCollection.find(createPublishedFilter()
                .append("Repository", "sda"));
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

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFilteredListOfROs(String filterString) {
        // TODO: filter
        return getAllPublishedROs();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRODetails(@PathParam("id") String id) {
        FindIterable<Document> iter = publicationsCollection.find(createPublishedFilter()
                .append("Repository", "sda")
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
        Document stage = new Document("stage", "");
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
            /*if (Constants.successStage.equals(status.getString("stage"))) {
                doi = status.getString("message");
                pubDate = status.getString("date");
            }*/
        }
        doc.append("DOI", doi);
        doc.append("Publication Date", pubDate);
        doc.remove("Status");
    }

}
