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
 * @author charmadu@umail.iu.edu
 */

package org.sead.monitoring.ui;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sead.monitoring.engine.enums.MonConstants;
import org.sead.monitoring.engine.util.LogEvent;
import org.sead.monitoring.ui.util.Constants;
import org.sead.monitoring.ui.util.DataoneLogEvent;
import org.sead.monitoring.ui.util.DateTimeUtil;
import org.sead.monitoring.ui.util.MongoDB;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Path("/")
public class SeadMonService {

    private static SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private static MongoCollection<Document> publicationsCollection = null;
    private static MongoCollection<Document> dataOneCollection = null;
    private static CacheControl control = new CacheControl();

    private static MongoCollection<Document> curbeeCollection = null;
    private static MongoCollection<Document> matchmakerCollection = null;
    private static MongoCollection<Document> landingPageCollection = null;
    private static MongoCollection<Document> seadCloudSearchCollection = null;

    private static final Logger log = Logger.getLogger(SeadMonService.class);

    static {
        MongoDatabase monDB = MongoDB.getMonDB();
        MongoDatabase pdtDB = MongoDB.getPdtDB();
        MongoDatabase dataOneDB = MongoDB.getDataOneDB();

        //Collections for Components
        curbeeCollection = monDB.getCollection(MonConstants.Components.CURBEE.getValue());
        matchmakerCollection = monDB.getCollection(MonConstants.Components.MATCHMAKER.getValue());
        landingPageCollection = monDB.getCollection(MonConstants.Components.LANDING_PAGE.getValue());
        seadCloudSearchCollection = monDB.getCollection(MonConstants.Components.IU_SEAD_CLOUD_SEARCH.getValue());

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

        BasicDBObject andQuery = generateAndQuery(fromDate, toDate, pidFilter, event);

        List<DataoneLogEvent> result = queryDataoneLog(dataOneCollection, andQuery, countStr, start);
        Date startDate = sdfDate.parse(result.get(0).getDate());
        Date endDate = sdfDate.parse(result.get(result.size() - 1).getDate());
        long duration = Math.round((endDate.getTime() - startDate.getTime())*1.0/1000);
        String scale = DateTimeUtil.getTimeScale(duration);

        Map<Long, Integer> stats = new HashMap<Long, Integer>();

        for (DataoneLogEvent d1log : result) {
            if (d1log.getDate() == null) {
                continue;
            }
            Date date = sdfDate.parse(d1log.getDate());
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

    @GET
    @Path("/curbee")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCurbeeData(@QueryParam("start") int start,
                                   @QueryParam("count") String countStr,
                                   @QueryParam("event") String event,
                                   @QueryParam("pidFilter") String pidFilter,
                                   @QueryParam("fromDate") String fromDate,
                                   @QueryParam("toDate") String toDate) throws ParseException {
        JSONArray jsonArray = new JSONArray();

        BasicDBObject andQuery = generateAndQuery(fromDate, toDate, pidFilter, event);

        List<LogEvent> result = queryLog(curbeeCollection, andQuery, countStr, start);
        Date startDate = sdfDate.parse(result.get(0).getDate());
        Date endDate = sdfDate.parse(result.get(result.size() - 1).getDate());
        long duration = Math.round((endDate.getTime() - startDate.getTime())*1.0/1000);
        String scale = DateTimeUtil.getTimeScale(duration);

        Map<Long, Integer> stats = new HashMap<Long, Integer>();

        for (LogEvent d1log : result) {
            if (d1log.getDate() == null) {
                continue;
            }
            Date date = sdfDate.parse(d1log.getDate());
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

    @GET
    @Path("/matchmaker")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMatchmakerData(@QueryParam("start") int start,
                                  @QueryParam("count") String countStr,
                                  @QueryParam("event") String event,
                                  @QueryParam("pidFilter") String pidFilter,
                                  @QueryParam("fromDate") String fromDate,
                                  @QueryParam("toDate") String toDate) throws ParseException {
        JSONArray jsonArray = new JSONArray();

        BasicDBObject andQuery = generateAndQuery(fromDate, toDate, pidFilter, event);

        List<LogEvent> result = queryLog(matchmakerCollection, andQuery, countStr, start);
        Date startDate = sdfDate.parse(result.get(0).getDate());
        Date endDate = sdfDate.parse(result.get(result.size() - 1).getDate());
        long duration = Math.round((endDate.getTime() - startDate.getTime())*1.0/1000);
        String scale = DateTimeUtil.getTimeScale(duration);

        Map<Long, Integer> stats = new HashMap<Long, Integer>();

        for (LogEvent d1log : result) {
            if (d1log.getDate() == null) {
                continue;
            }
            Date date = sdfDate.parse(d1log.getDate());
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

    @GET
    @Path("/landingPage")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getIUseadData(@QueryParam("start") int start,
                                  @QueryParam("count") String countStr,
                                  @QueryParam("event") String event,
                                  @QueryParam("pidFilter") String pidFilter,
                                  @QueryParam("fromDate") String fromDate,
                                  @QueryParam("toDate") String toDate) throws ParseException {
        JSONArray jsonArray = new JSONArray();

        BasicDBObject andQuery = generateAndQuery(fromDate, toDate, pidFilter, event);

        List<LogEvent> result = queryLog(landingPageCollection, andQuery, countStr, start);
        Date startDate = sdfDate.parse(result.get(0).getDate());
        Date endDate = sdfDate.parse(result.get(result.size() - 1).getDate());
        long duration = Math.round((endDate.getTime() - startDate.getTime())*1.0/1000);
        String scale = DateTimeUtil.getTimeScale(duration);

        Map<Long, Integer> stats = new HashMap<Long, Integer>();

        for (LogEvent d1log : result) {
            if (d1log.getDate() == null) {
                continue;
            }
            Date date = sdfDate.parse(d1log.getDate());
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

    public List<LogEvent> queryLog(MongoCollection collection, BasicDBObject query, String countStr, int start){

        int count = 0;
        if(countStr!=null && !countStr.equals(Constants.INFINITE))
            count = Integer.parseInt(countStr);
        start = start < 0 ? 0 : start;

        FindIterable<Document> iter;
        if(countStr == null || (countStr!=null && countStr.equals(Constants.INFINITE))) {
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

    public List<DataoneLogEvent>  queryDataoneLog(MongoCollection collection, BasicDBObject query, String countStr, int start){

        int count = 0;
        if(countStr!=null && !countStr.equals(Constants.INFINITE))
            count = Integer.parseInt(countStr);
        start = start < 0 ? 0 : start;

        FindIterable<Document> iter;
        if(countStr == null || (countStr!=null && countStr.equals(Constants.INFINITE))) {
            iter = collection.find(query)
                    .skip(start)
                    .sort(new BasicDBObject("date", 1));
        } else {
            iter = collection.find(query)
                    .limit(count)
                    .skip(start)
                    .sort(new BasicDBObject("date", 1));
        }
        List<DataoneLogEvent> logEvents = new ArrayList<DataoneLogEvent>();
        MongoCursor<Document> cursor = iter.iterator();
        try {
            while(cursor.hasNext()) {
                Document dbobj = cursor.next();
                //Converting BasicDBObject to a custom Class(LogEvent)
                DataoneLogEvent logEvent = (new Gson()).fromJson(dbobj.toJson(), DataoneLogEvent.class);
                logEvents.add(logEvent);
            }
        } finally {
            cursor.close();
        }
        return logEvents;
    }

    private BasicDBObject generateAndQuery(String fromDate, String toDate, String pidFilter, String event) {
        BasicDBObject andQuery = new BasicDBObject();
        List<BasicDBObject> obj = new ArrayList<BasicDBObject>();

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

        return andQuery;
    }
}
