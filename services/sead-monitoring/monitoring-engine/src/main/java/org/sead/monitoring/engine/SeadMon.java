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

package org.sead.monitoring.engine;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.sead.monitoring.engine.enums.MonConstants;
import org.sead.monitoring.engine.util.Constants;
import org.sead.monitoring.engine.util.DataoneLogEvent;
import org.sead.monitoring.engine.util.LogEvent;
import org.sead.monitoring.engine.util.MongoDB;

import javax.ws.rs.Path;
import javax.ws.rs.core.CacheControl;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Path("/")
public class SeadMon {

    private static SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private static MongoCollection<Document> publicationsCollection = null;
    private static MongoCollection<Document> dataOneCollection = null;
    private static CacheControl control = new CacheControl();

    private static MongoCollection<Document> curbeeCollection = null;
    private static MongoCollection<Document> matchmakerCollection = null;
    private static MongoCollection<Document> landingPageCollection = null;
    private static MongoCollection<Document> seadCloudCollection = null;

    private static final Logger log = Logger.getLogger(SeadMon.class);

    static {
        MongoDatabase monDB = MongoDB.getMonDB();
        MongoDatabase pdtDB = MongoDB.getPdtDB();
        MongoDatabase dataOneDB = MongoDB.getDataOneDB();

        //Collections for Components
        curbeeCollection = monDB.getCollection(MonConstants.Components.CURBEE.getValue());
        matchmakerCollection = monDB.getCollection(MonConstants.Components.MATCHMAKER.getValue());
        landingPageCollection = monDB.getCollection(MonConstants.Components.LANDING_PAGE.getValue());
        seadCloudCollection = monDB.getCollection(MonConstants.Components.IU_SEAD_CLOUD_SEARCH.getValue());

        publicationsCollection = pdtDB.getCollection(MongoDB.researchObjects);
        dataOneCollection = dataOneDB.getCollection(MongoDB.events);
        control.setNoCache(true);
    }

    public static void addLog(MonConstants.Components component, String roId) {
        LogEvent logEvent = new LogEvent();
        logEvent.setId(roId);
        logEvent.setDate(sdfDate.format(new Date()));
        indexLog(component, logEvent);
    }

    public static void addLog(MonConstants.Components component, String roId, MonConstants.EventType event) {
        LogEvent logEvent = new LogEvent();
        logEvent.setId(roId);
        logEvent.setDate(sdfDate.format(new Date()));
        logEvent.setEventType(event.getValue());
        indexLog(component, logEvent);
    }

    public static void addLog(MonConstants.Components component, String roId, MonConstants.Status status) {
        LogEvent logEvent = new LogEvent();
        logEvent.setId(roId);
        logEvent.setDate(sdfDate.format(new Date()));
        logEvent.setStatus(status.getValue());
        indexLog(component, logEvent);
    }

    public static void addLog(MonConstants.Components component, String roId, MonConstants.Status status,
                              MonConstants.EventType event) {
        LogEvent logEvent = new LogEvent();
        logEvent.setId(roId);
        logEvent.setDate(sdfDate.format(new Date()));
        logEvent.setStatus(status.getValue());
        logEvent.setEventType(event.getValue());
        indexLog(component, logEvent);
    }

    private static void indexLog(MonConstants.Components component, LogEvent logEvent){
        try {
            Gson gson = new Gson();
            Document document = Document.parse(gson.toJson(logEvent));
            if(component.equals(MonConstants.Components.CURBEE)){
                curbeeCollection.insertOne(document);
            } else if(component.equals(MonConstants.Components.MATCHMAKER)){
                matchmakerCollection.insertOne(document);
            } else if(component.equals(MonConstants.Components.LANDING_PAGE)){
                landingPageCollection.insertOne(document);
            } else if(component.equals(MonConstants.Components.IU_SEAD_CLOUD_SEARCH)){
                seadCloudCollection.insertOne(document);
            }
        } catch (Exception e) {
            log.error("Error while saving event to MongoDB :" + e.getMessage());
        }
    }

    public static List<LogEvent> queryCurbeeLogs(MonConstants.Status status, String id, Date fromDate, Date toDate) {
        BasicDBObject andQuery = generateAndQuery(fromDate, toDate, id, null, status);
        return queryLog(curbeeCollection, andQuery, null, 0);
    }

    public static List<LogEvent> queryMMLogs(Date fromDate, Date toDate) {
        BasicDBObject andQuery = generateAndQuery(fromDate, toDate, null, null, null);
        return queryLog(matchmakerCollection, andQuery, null, 0);
    }

    public static List<LogEvent> queryLandingPageLogs(MonConstants.EventType event, String id, Date fromDate, Date toDate) {
        BasicDBObject andQuery = generateAndQuery(fromDate, toDate, id, event, null);
        return queryLog(landingPageCollection, andQuery, null, 0);
    }

    public static List<LogEvent> queryIUSeadCloudSearchLogs(Date fromDate, Date toDate) {
        BasicDBObject andQuery = generateAndQuery(fromDate, toDate, null, null, null);
        return queryLog(seadCloudCollection, andQuery, null, 0);
    }

    public static List<DataoneLogEvent> queryDataoneLogs(Date fromDate, Date toDate) {
        BasicDBObject andQuery = generateAndQuery(fromDate, toDate, null, null, null);
        return queryDataoneLog(dataOneCollection, andQuery, null, 0);
    }

    private static List<LogEvent> queryLog(MongoCollection collection, BasicDBObject query, String countStr, int start){

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

    private static List<DataoneLogEvent> queryDataoneLog(MongoCollection collection, BasicDBObject query, String countStr, int start){

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

    private static BasicDBObject generateAndQuery(Date fromDate, Date toDate, String id, MonConstants.EventType event, MonConstants.Status status) {
        BasicDBObject andQuery = new BasicDBObject();
        List<BasicDBObject> obj = new ArrayList<BasicDBObject>();

        if (fromDate != null) {
            obj.add(new BasicDBObject("date", new BasicDBObject("$gte", sdfDate.format(fromDate))));
        }
        if (toDate != null) {
            obj.add(new BasicDBObject("date", new BasicDBObject("$lte", sdfDate.format(toDate))));
        }
        if(id != null){
            obj.add(new BasicDBObject("id", id));
        }
        if(event != null){
            obj.add(new BasicDBObject("eventType", event.getValue()));
        }
        if(status != null){
            obj.add(new BasicDBObject("status", status.getValue()));
        }
        if (obj.size() != 0) {
            andQuery.put("$and", obj);
        }

        return andQuery;
    }
}
