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

package org.seadva.dataone;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataone.service.types.v1.*;
import org.jibx.runtime.JiBXException;
import org.seadva.model.SeadEvent;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.transform.TransformerException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/*
 * Retrieves logs from DataONE Reads
 */

@Path("/mn/v1/log")
public class LogService{
    public LogService() {
    }

    private final static int MAX_MATCHES = 80;

    @Produces(MediaType.APPLICATION_XML)
    @GET
    public String getLogRecords(@QueryParam("start") int start,
                             @QueryParam("count") String countStr,
                             @QueryParam("event") String event,
                             @QueryParam("pidFilter") String pidFilter,
                             @QueryParam("fromDate") String fromDate,
                             @QueryParam("toDate") String toDate) throws QueryServiceException, DatatypeConfigurationException, JiBXException, ParseException, TransformerException {


        Log log = new Log();
        Map<String,String> queryParams = new HashMap<String, String>();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        int count = MAX_MATCHES;
        boolean countZero = false;
        if(countStr!=null){
            count = Integer.parseInt(countStr);
            if(count <= 0)
                countZero = true;
        }

        DataOneLogService.Result tempResult = SeadQueryService.dataOneLogService.queryLog(0, "0", event, pidFilter, fromDate, toDate);
        int solrTotalCount = (int)tempResult.total;

        if(event != null)
            queryParams.put("event", event);
        if(pidFilter != null)
            queryParams.put("pidFilter", pidFilter);
        if(fromDate != null)
            queryParams.put("fromDate", fromDate);
        if(toDate != null)
            queryParams.put("toDate", toDate);
        int mongoTotalCount = getMongoTotal(queryParams);

        if(countZero){
            log.setCount(0);
            log.setTotal(solrTotalCount+mongoTotalCount);
            log.setStart(start);
            return SeadQueryService.marshal(log);
        }

        int solrCount = 0, mongoCount = 0, solrStart = 0, mongoStart = 0;
        if(start < solrTotalCount){
            solrStart = start;
            if(count + start - 1 < solrTotalCount){
                solrCount = count;
            } else {
                solrCount = solrTotalCount - start;
                mongoCount = count - solrCount;
            }
        } else {
            mongoStart = start - solrTotalCount;
            mongoCount = count;
        }


        DataOneLogService.Result result = null;
        if (solrCount > 0) {
            result = SeadQueryService.dataOneLogService.queryLog(solrStart,solrCount+"",event, pidFilter,fromDate, toDate);

            for(SeadEvent d1log: result.logs){

                if(d1log.getLogDetail().getSubject() == null || d1log.getLogDetail().getNodeIdentifier() == null) {
                    continue;
                }

                LogEntry logEntry = new LogEntry();

                logEntry.setEntryId(d1log.getId());
                Event eventType = Event.convert(
                        SeadQueryService.sead2d1EventTypes.get(d1log.getEventType()));
                if(eventType == Event.READ)
                    eventType = Event.READ;
                logEntry.setEvent(
                           eventType
                );

                Identifier identifier = new Identifier();
                identifier.setValue(d1log.getId()); //DcsEvent Identifier
                logEntry.setIdentifier(identifier);
                String ipaddress = d1log.getLogDetail().getIpAddress();

                if(ipaddress==null)
                    ipaddress="N/A";

                logEntry.setIpAddress(ipaddress);

                String date = d1log.getDate();
                logEntry.setDateLogged(simpleDateFormat.parse(date));

                String userAgent = d1log.getLogDetail().getUserAgent();
                if(userAgent==null)
                    userAgent= "N/A";
                logEntry.setUserAgent(userAgent);
                Subject subject = new Subject();
                subject.setValue(d1log.getLogDetail().getSubject());
                logEntry.setSubject(subject);
                NodeReference nodeReference = new NodeReference();
                nodeReference.setValue(d1log.getLogDetail().getNodeIdentifier());
                logEntry.setNodeIdentifier(nodeReference);
                log.getLogEntryList().add(logEntry);
            }
        }

        if(mongoCount > 0) {
            queryParams.put("start", mongoStart + "");
            queryParams.put("count", mongoCount + "");
            appendMongoNodes(log, queryParams);
        }

        log.setCount(log.getLogEntryList().size());
        log.setTotal(solrTotalCount+mongoTotalCount);
        log.setStart(start);
        return SeadQueryService.marshal(log);
    }

    private void appendMongoNodes(Log objectList, Map<String, String> queryParams) throws ParseException, JiBXException {

        WebResource webResource = Client.create().resource(SeadQueryService.SEAD_DATAONE_URL + "/log");
        webResource.accept("application/xml").type("application/xml");
        for(String param :queryParams.keySet()){
            webResource = webResource.queryParam(param, queryParams.get(param));
        }
        ClientResponse response = webResource.get(ClientResponse.class);
        Log mongoResults = (Log) SeadQueryService.unmarshal(response.getEntityInputStream(), Log.class);

        if(mongoResults.getLogEntryList() == null){
            return;
        }

        for(LogEntry logInfo: mongoResults.getLogEntryList()) {
            objectList.getLogEntryList().add(logInfo);
        }
    }

    private int getMongoTotal(Map<String,String> queryParams) {
        WebResource webResource = Client.create().resource(SeadQueryService.SEAD_DATAONE_URL + "/log/total");
        webResource.accept("application/xml").type("application/xml");
        for(String param :queryParams.keySet()){
            webResource = webResource.queryParam(param, queryParams.get(param));
        }
        ClientResponse response = webResource.get(ClientResponse.class);
        String total = response.getEntity(new GenericType<String>() {});
        return Integer.parseInt(total);
    }
}
