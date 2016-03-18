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
 *
 * @author charmadu@umail.iu.edu
*/

package org.sead.va.dataone;

import com.mongodb.BasicDBObject;
import org.dataone.service.types.v1.*;
import org.jibx.runtime.JiBXException;
import org.sead.va.dataone.util.Constants;
import org.sead.va.dataone.util.LogEvent;
import org.sead.va.dataone.util.SeadQueryService;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.transform.TransformerException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@Path("/mn/v1/event")
public class EventService {

    public EventService() {
    }


    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("{eventId}")
    public String getLogEvent(@Context HttpServletRequest request,
                                @PathParam("eventId") String eventId) throws JiBXException, ParseException, TransformerException {


        String errorMsg = "<error name=\"NotFound\" errorCode=\"404\" detailCode=\"1060\" pid=\"" + eventId + "\" nodeId=\"" + Constants.NODE_IDENTIFIER + "\">\n" +
                "<description>The specified object does not exist on this node.</description>\n" +
                "<traceInformation>\n" +
                "method: mn.getSystemMetadata hint: http://cn.dataone.org/cn/resolve/" + eventId + "\n" +
                "</traceInformation>\n" +
                "</error>";


        Log log = new Log();

        BasicDBObject query = new BasicDBObject("entityId", Constants.BASE_URL + "/event/" + eventId);
        List<LogEvent> result = SeadQueryService.dataOneLogService.queryLog(query, null , 0);

        if (result.size() > 0) {

            LogEvent d1log = result.get(0);
            LogEntry logEntry = new LogEntry();

            logEntry.setEntryId(d1log.getEntityId());
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
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
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
            return SeadQueryService.marshal(logEntry);
        } else {
            throw new NotFoundException(errorMsg);
        }
    }

}
