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
import org.dataone.service.types.v1.*;
import org.jibx.runtime.JiBXException;
import org.sead.va.dataone.util.Constants;
import org.sead.va.dataone.util.LogEvent;
import org.sead.va.dataone.util.SeadQueryService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.transform.TransformerException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/*
 * Retrieves logs from DataONE Reads
 */

@Path("/mn/v1/log")
public class LogService {

    public LogService() {
    }

    @Produces(MediaType.APPLICATION_XML)
    @GET
    public String getLogRecords(@QueryParam("start") int start,
            @QueryParam("count") String countStr,
            @QueryParam("event") String event,
            @QueryParam("pidFilter") String pidFilter,
            @QueryParam("fromDate") String fromDate,
            @QueryParam("toDate") String toDate) throws DatatypeConfigurationException, JiBXException, ParseException, TransformerException {


        Log log = new Log();

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

        List<LogEvent> result = SeadQueryService.dataOneLogService.queryLog(andQuery, countStr, start);

        for (LogEvent d1log : result) {

            if (d1log.getSubject() == null || d1log.getNodeIdentifier() == null) {
                continue;
            }

            LogEntry logEntry = new LogEntry();

            logEntry.setEntryId(d1log.getId());
            Event eventType = Event.convert(d1log.getEventType());
            if (eventType == Event.READ)
                eventType = Event.READ;
            logEntry.setEvent(eventType);

            Identifier identifier = new Identifier();
            identifier.setValue(d1log.getId()); //DcsEvent Identifier
            logEntry.setIdentifier(identifier);
            String ipaddress = d1log.getIp();

            if (ipaddress == null)
                ipaddress = "N/A";

            logEntry.setIpAddress(ipaddress);

            String date = d1log.getDate();
            logEntry.setDateLogged(simpleDateFormat.parse(date));

            String userAgent = d1log.getUserAgent();
            if (userAgent == null)
                userAgent = "N/A";
            logEntry.setUserAgent(userAgent);
            Subject subject = new Subject();
            subject.setValue(d1log.getSubject());
            logEntry.setSubject(subject);
            NodeReference nodeReference = new NodeReference();
            nodeReference.setValue(d1log.getNodeIdentifier());
            logEntry.setNodeIdentifier(nodeReference);
            log.getLogEntryList().add(logEntry);
        }

        log.setCount(result.size());
        log.setTotal(Integer.parseInt(countEvents(event, pidFilter, fromDate, toDate)));
        log.setStart(start);
        return SeadQueryService.marshal(log);
    }

    @GET
    @Path("/total")
    @Produces(MediaType.APPLICATION_XML)
    public String countEvents(@QueryParam("event") String event,
                              @QueryParam("pidFilter") String pidFilter,
                              @QueryParam("fromDate") String fromDate,
                              @QueryParam("toDate") String toDate) {

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

        List<LogEvent> result = SeadQueryService.dataOneLogService.queryLog(andQuery, Constants.INFINITE, 0);

        return result.size() + "";
    }
}
