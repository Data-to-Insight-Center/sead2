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

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sead.monitoring.engine.SeadMon;
import org.sead.monitoring.engine.util.DataoneLogEvent;
import org.sead.monitoring.engine.util.LogEvent;
import org.sead.monitoring.ui.util.DateTimeUtil;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/")
public class SeadMonService {

    private static SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static CacheControl control = new CacheControl();
    private static final Logger log = Logger.getLogger(SeadMonService.class);

    static {
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
        List<DataoneLogEvent> result = SeadMon.queryDataoneLogs(null, null);
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

        List<LogEvent> result = SeadMon.queryCurbeeLogs(null, null, null, null);
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

        List<LogEvent> result = SeadMon.queryMMLogs(null, null);
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

        List<LogEvent> result = SeadMon.queryLandingPageLogs(null, null, null, null);
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
    @Path("/iucloudsearch")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getIUseadCloudSearchData(@QueryParam("start") int start,
                                  @QueryParam("count") String countStr,
                                  @QueryParam("event") String event,
                                  @QueryParam("pidFilter") String pidFilter,
                                  @QueryParam("fromDate") String fromDate,
                                  @QueryParam("toDate") String toDate) throws ParseException {
        JSONArray jsonArray = new JSONArray();

        List<LogEvent> result = SeadMon.queryIUSeadCloudSearchLogs(null, null);
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
}
