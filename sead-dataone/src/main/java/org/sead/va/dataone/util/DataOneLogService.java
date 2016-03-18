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
 *
 * @author charmadu@umail.iu.edu
 */

package org.sead.va.dataone.util;

import com.google.gson.Gson;
import com.mongodb.*;
import com.mongodb.util.JSON;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataOneLogService {

    private DBCollection eventCollection = null;
    private DB metaDb = null;
    static UidGenerator uidGenerator = new UidGenerator();
    static SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    public DataOneLogService() {
        metaDb = MongoDB.getDB();
        eventCollection = metaDb.getCollection(MongoDB.event);
    }

    public LogEvent creatEvent(String eventType, String userAgent, String ip, String entityId){
        LogEvent logEvent = new LogEvent();
        logEvent.setEntityId(Constants.BASE_URL + "/event/" + uidGenerator.generateNextUID());
        logEvent.setId(entityId);
        logEvent.setEventType(eventType);
        logEvent.setDate(sdfDate.format(new Date()));
        logEvent.setIp(ip);
        logEvent.setUserAgent(userAgent);
        logEvent.setSubject("DC=dataone, DC=org");
        logEvent.setNodeIdentifier(Constants.NODE_IDENTIFIER);
        logEvent.setTarget(entityId);
        return logEvent;
    }

    public void indexLog(LogEvent logEvent){
        Gson gson = new Gson();
        BasicDBObject basicDBObject = (BasicDBObject) JSON.parse(gson.toJson(logEvent));
        eventCollection.insert(basicDBObject);
    }

    public List<LogEvent>  queryLog(BasicDBObject query, String countStr, int start){

        int count = 80;
        if(countStr!=null && !countStr.equals(Constants.INFINITE))
            count = Integer.parseInt(countStr);
        start = start < 0 ? 0 : start;

        DBCursor cursor;
        if(countStr!=null && countStr.equals(Constants.INFINITE)) {
            cursor = eventCollection.find(query)
                            .skip(start)
                            .sort(new BasicDBObject("id", 1));
        } else {
            cursor = eventCollection.find(query)
                    .limit(count)
                    .skip(start)
                    .sort(new BasicDBObject("id", 1));
        }
        List<LogEvent> logEvents = new ArrayList<LogEvent>();
        try {
            while(cursor.hasNext()) {
                DBObject dbobj = cursor.next();
                //Converting BasicDBObject to a custom Class(LogEvent)
                LogEvent logEvent = (new Gson()).fromJson(dbobj.toString(), LogEvent.class);
                logEvents.add(logEvent);
            }
        } finally {
            cursor.close();
        }
        return logEvents;
    }

}
