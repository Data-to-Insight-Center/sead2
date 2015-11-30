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

import org.dataconservancy.dcs.id.impl.UidGenerator;
import org.dataconservancy.dcs.index.api.IndexServiceException;
import org.dataconservancy.dcs.index.dcpsolr.DcpIndexService;
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField;
import org.dataconservancy.dcs.index.dcpsolr.ROIndexService;
import org.dataconservancy.dcs.index.dcpsolr.SolrService;
import org.dataconservancy.dcs.index.solr.support.SolrQueryUtil;
import org.dataconservancy.dcs.query.api.QueryMatch;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataone.service.types.v1.Event;
import org.seadva.ingest.Events;
import org.seadva.model.SeadEvent;
import org.seadva.model.SeadFile;
import org.seadva.model.SeadLogDetail;
import org.seadva.model.pack.ResearchObject;

import java.util.ArrayList;
import java.util.List;

import static org.dataconservancy.dcs.util.DateUtility.now;
import static org.dataconservancy.dcs.util.DateUtility.toIso8601;

/**
 * Logging service in DataONE
 */
public class DataOneLogService {

    ROIndexService dcpIndexService;

    public DataOneLogService(SolrService solr) {
        dcpIndexService = new ROIndexService(
                solr
                );
    }
    static UidGenerator uidGenerator = new UidGenerator();
    public SeadEvent creatEvent(String eventType, String userAgent, String ip, DcsEntity entity){
        SeadEvent readEvent = new SeadEvent();
        readEvent.setId(
                SeadQueryService.datastreamURL + uidGenerator.generateNextUID() //this a unique id generator that reads from a file and not a UUID.randomUUID().toString()
        );
        readEvent.setEventType(eventType);
        readEvent.setDate(toIso8601(now()));
        SeadLogDetail logDetail = new SeadLogDetail();
        logDetail.setIpAddress(ip);
        logDetail.setUserAgent(userAgent);
        logDetail.setSubject("DC=dataone, DC=org");
        logDetail.setNodeIdentifier(SeadQueryService.NODE_IDENTIFIER);
        readEvent.setDate(toIso8601(now()));
        readEvent.setLogDetail(logDetail);

        if(entity instanceof SeadFile)
            readEvent.setDetail(eventType + " event was initiated by an anonymous DataONE user for object "+((SeadFile)entity).getName());
        DcsEntityReference reference = new DcsEntityReference();
        reference.setRef(entity.getId());
        readEvent.addTargets(reference);
        return readEvent;

    }
    public void indexLog(ResearchObject eventsSip){
        try {
            dcpIndexService.index().add(eventsSip);
            dcpIndexService.index().close();
        } catch (IndexServiceException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    public Result queryLog(int start, String countStr, String event, String pidFilter, String fromDate, String toDate){


        String queryStr  = SolrQueryUtil.createLiteralQuery(DcsSolrField.EntityField.TYPE.solrName(), DcsSolrField.EntityTypeValue.EVENT.solrValue());

        if(event!=null){
            queryStr+= " AND ("+ DcsSolrField.EventField.TYPE.solrName() + ":" + SeadQueryService.d1toSeadEventTypes.get(event) ;
            if(event.equals(Event.READ.xmlValue()))
                queryStr+= " OR "+ DcsSolrField.EventField.TYPE.solrName() + ":" +
                        SeadQueryService.d1toSeadEventTypes.get(Event.READ.xmlValue()) +" )" ;
        }
        else {
            queryStr+= " AND ("+ DcsSolrField.EventField.TYPE.solrName() + ":" + SeadQueryService.d1toSeadEventTypes.get(Event.READ.xmlValue()) ;
            queryStr+= " OR "+ DcsSolrField.EventField.TYPE.solrName() + ":" + Events.FILEMETADATA_D1READ ;  //other type of read
            queryStr+= " OR "+ DcsSolrField.EventField.TYPE.solrName() + ":" + SeadQueryService.d1toSeadEventTypes.get(Event.REPLICATE.xmlValue()) +" )";
        }
        if(pidFilter!=null){
            pidFilter = pidFilter.replace(":","\\:");
            queryStr+= " AND "+ DcsSolrField.EntityField.ID.solrName() + ":" + pidFilter;
        }
        if(fromDate!=null&&toDate!=null) {
            fromDate = fromDate.replace("+00:00","Z");
            toDate = toDate.replace("+00:00","Z");
            queryStr+= " AND "+ DcsSolrField.EventField.DATE.solrName() + ":" + "[" + fromDate+" TO " +toDate+ "]";
        }
        else if(fromDate!=null) {
            fromDate = fromDate.replace("+00:00","Z");
            queryStr+= " AND "+ DcsSolrField.EventField.DATE.solrName() + ":" + "[" + fromDate+" TO NOW" + "]";
        }
        else if(toDate!=null) {
            toDate = toDate.replace("+00:00","Z");
            queryStr+= " AND "+ DcsSolrField.EventField.DATE.solrName() + ":" + "[ NOW-365DAY TO " + toDate + "]";
        }

        int count = 80;

        if(countStr!=null)
            count = Integer.parseInt(countStr);

        QueryResult<DcsEntity> result = null;
        try {
            result = SeadQueryService.queryService.query(queryStr, start, count); //sort by filename
        } catch (QueryServiceException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        List<SeadEvent> events = new ArrayList<SeadEvent>();
        Result resultObject = new Result();
        if(result!=null){
            List<QueryMatch<DcsEntity>> matches = result.getMatches();
            for(QueryMatch<DcsEntity> entity: matches){
                SeadEvent dcsEvent = (SeadEvent)entity.getObject();
                events.add(dcsEvent);
            }

            resultObject.total = result.getTotal();
        }
        else
            resultObject.total = 0;
        resultObject.logs = events;
        return resultObject;
    }

    public class Result{
        public long total;
        public List<SeadEvent> logs;
    }
}