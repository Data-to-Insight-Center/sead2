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
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.sead.monitoring.engine.enums.MonConstants;
import org.sead.monitoring.engine.util.LogEvent;
import org.sead.monitoring.engine.util.MongoDB;

import javax.ws.rs.Path;
import javax.ws.rs.core.CacheControl;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        seadCloudCollection = monDB.getCollection(MonConstants.Components.IU_SEAD_CLOUD.getValue());

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

    public static void indexLog(MonConstants.Components component, LogEvent logEvent){
        try {
            Gson gson = new Gson();
            Document document = Document.parse(gson.toJson(logEvent));
            if(component.equals(MonConstants.Components.CURBEE)){
                curbeeCollection.insertOne(document);
            } else if(component.equals(MonConstants.Components.MATCHMAKER)){
                matchmakerCollection.insertOne(document);
            } else if(component.equals(MonConstants.Components.LANDING_PAGE)){
                landingPageCollection.insertOne(document);
            } else if(component.equals(MonConstants.Components.IU_SEAD_CLOUD)){
                seadCloudCollection.insertOne(document);
            }
        } catch (Exception e) {
            log.error("Error while saving event to MongoDB :" + e.getMessage());
        }
    }
}
