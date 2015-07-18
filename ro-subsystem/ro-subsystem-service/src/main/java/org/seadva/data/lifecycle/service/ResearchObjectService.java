/*
 * Copyright 2014 The Trustees of Indiana University
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

package org.seadva.data.lifecycle.service;

import com.google.gson.GsonBuilder;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import edu.indiana.d2i.komadu.axis2.client.KomaduServiceStub;
import edu.indiana.d2i.komadu.query.*;
import org.apache.commons.io.IOUtils;
import org.dspace.foresite.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.seadva.data.lifecycle.support.KomaduIngester;
import org.seadva.data.lifecycle.support.model.Entity;
import org.seadva.data.lifecycle.support.model.Event;
import org.seadva.data.lifecycle.support.model.ROMetadata;
import org.seadva.data.lifecycle.service.util.Util;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.seadva.model.pack.ResearchObject;
import org.seadva.registry.client.RegistryClient;
import org.seadva.registry.database.model.obj.vaRegistry.*;
import org.seadva.registry.database.model.obj.vaRegistry.Agent;
import org.seadva.registry.database.model.obj.vaRegistry.Collection;
import org.seadva.registry.mapper.DcsDBMapper;
import org.seadva.registry.mapper.OreDBMapper;
import org.seadva.registry.mapper.JsonDBMapper;
import org.seadva.registry.service.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Required;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.*;

/**
 * REST interface for RO subsystem which is an abstraction on VA registry and Komadu Provenance system
 */

@Path("/resource")
public class ResearchObjectService {

    static {

        InputStream inputStream =
                ResearchObjectService.class.getResourceAsStream("./Config.properties");

        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(inputStream, writer);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        String result = writer.toString();
        String[] pairs = result.trim().split(
                "\n|\\=");


        for (int i = 0; i + 1 < pairs.length;) {
            String name = pairs[i++].trim();
            String value = pairs[i++].trim();
            if(name.equalsIgnoreCase("komadu.url"))
                komaduServiceUrl = value;
            else if(name.equalsIgnoreCase("registry.url"))
                registryServiceUrl = value;
        }
    }
    private static String komaduServiceUrl;
    private static String registryServiceUrl;

    @Required
    public void setKomaduServiceUrl(String komaduServiceUrl){
        this.komaduServiceUrl = komaduServiceUrl;
    }

    @Required
    public void setRegistryServiceUrl(String registryServiceUrl){
        this.registryServiceUrl = registryServiceUrl;
    }


    private static final String RESOURCE_MAP_SERIALIZATION_FORMAT = "RDF/XML";

    @GET
    @Path("/ro/{entityId}")
    public Response getResearchObject( @PathParam("entityId") String roIdentifier) throws Exception {

        String resourceMapXml = "";
        ORESerialiser serial = ORESerialiserFactory.getInstance(RESOURCE_MAP_SERIALIZATION_FORMAT);
        ResourceMapDocument doc = serial.serialise(new OreDBMapper(registryServiceUrl).toORE(roIdentifier));
        resourceMapXml = doc.toString();
        return Response.ok(  resourceMapXml
        ).build();
    }

    @GET
    @Path("/jsonldro/{entityId}")
    public Response getJSONLDResearchObject( @PathParam("entityId") String roIdentifier) throws Exception {

        String jsonlddoc = new JsonDBMapper(registryServiceUrl).toJSONLD(roIdentifier);
        System.out.println("RESEARCH OBJECT SERVICE: "+jsonlddoc);
        return Response.ok(jsonlddoc).build();
    }

    @GET
    @Path("/agentGraph/{agentId}")
    @Produces("application/json")
    public Response getAgentGraph(@PathParam("agentId") String agentId,
                                  @QueryParam("callback") String callback){
        try {
            GetAgentGraphRequestDocument agentGraphRequest = GetAgentGraphRequestDocument.Factory.newInstance();
            GetAgentGraphRequestType agentRequestType = GetAgentGraphRequestType.Factory.newInstance();
            agentRequestType.setAgentID(agentId);
            agentRequestType.setInformationDetailLevel(DetailEnumType.FINE);
            agentGraphRequest.setGetAgentGraphRequest(agentRequestType);
            KomaduServiceStub serviceStub = new KomaduServiceStub(
                    komaduServiceUrl
            );
            GetAgentGraphResponseDocument agentResponse = serviceStub.getAgentGraph(agentGraphRequest);
            JSONObject xmlJSONObj = XML.toJSONObject(agentResponse.getGetAgentGraphResponse().getDocument().toString());
            String jsonPrettyPrintString = xmlJSONObj.toString(4);
            return Response.ok(
                    //"__gwt_jsonp__.P0.onSuccess" +
                    callback+
                            "("+
                            jsonPrettyPrintString
                            +")"
            ).header("Content-Type", "application/javascript").build();
        } catch (RemoteException e) {
            return Response.serverError().entity(e.getMessage()).build();
        } catch (JSONException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/entityGraph/{entityId}")
    @Produces("application/json")
    public Response getEntityGraph(@PathParam("entityId") String entityId,
                                   @QueryParam("callback") String callback){
        try {
            GetEntityGraphRequestDocument entityGraphRequest = GetEntityGraphRequestDocument.Factory.newInstance();
            GetEntityGraphRequestType entityRequestType = GetEntityGraphRequestType.Factory.newInstance();
            entityRequestType.setInformationDetailLevel(DetailEnumType.FINE);
            entityRequestType.setEntityURI(entityId);
            entityRequestType.setEntityType(EntityEnumType.COLLECTION);
            entityGraphRequest.setGetEntityGraphRequest(entityRequestType);
            KomaduServiceStub serviceStub = new KomaduServiceStub(
                    komaduServiceUrl
            );

            GetEntityGraphResponseDocument entityResponse = serviceStub.getEntityGraph(entityGraphRequest);
            JSONObject xmlJSONObj = XML.toJSONObject(entityResponse.getGetEntityGraphResponse().getDocument().toString());
            String jsonPrettyPrintString = xmlJSONObj.toString(4);

            if(callback==null)
                callback = "";

            return Response.ok(
                    callback+
                            "("+
                            jsonPrettyPrintString
                            +")"
            ).header("Content-Type", "application/javascript").build();
        } catch (RemoteException e) {
            return Response.serverError().entity(e.getMessage()).build();
        } catch (JSONException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/lineage/{entityId}")
    @Produces("application/json")
    public Response getLineage( @PathParam("entityId") String roIdentifier) throws Exception {

        GetEntityGraphRequestDocument entityGraphRequest = GetEntityGraphRequestDocument.Factory.newInstance();
        GetEntityGraphRequestType entityRequestType = GetEntityGraphRequestType.Factory.newInstance();
        entityRequestType.setInformationDetailLevel(DetailEnumType.FINE);
        entityRequestType.setEntityURI(roIdentifier);
        entityRequestType.setEntityType(edu.indiana.d2i.komadu.query.EntityEnumType.COLLECTION);
        entityGraphRequest.setGetEntityGraphRequest(entityRequestType);

        KomaduServiceStub serviceStub = new KomaduServiceStub(
                komaduServiceUrl
        );
        GetEntityGraphResponseDocument entityResponse = serviceStub.getEntityGraph(entityGraphRequest);

        String graph = entityResponse.getGetEntityGraphResponse().getDocument().toString();


        Util util = new Util();
        util.pullParse(new ByteArrayInputStream(graph.getBytes(StandardCharsets.UTF_8)), "");
        Iterator iterator = util.getGenUsed().entrySet().iterator();
        Map<String, List<String>> genUsedUrl = new HashMap<String, List<String>>();
        while (iterator.hasNext()){
            Map.Entry<String,String> pair = (Map.Entry<String, String>) iterator.next();
            List<String> tempList = new ArrayList<String>();
            if(genUsedUrl.containsKey(util.getIdEntityMap().get(pair.getValue()).getUrl())) //parent
                tempList = genUsedUrl.get(util.getIdEntityMap().get(pair.getValue()).getUrl());


            if(!tempList.contains(util.getIdEntityMap().get(pair.getKey()).getUrl())) //child
                tempList.add( util.getIdEntityMap().get(pair.getKey()).getUrl());

            genUsedUrl.put(util.getIdEntityMap().get(pair.getValue()).getUrl(),
                    tempList);
        }


        Iterator iterator2 = genUsedUrl.entrySet().iterator();

        List<Entity> newList = new ArrayList<Entity>();


        while(iterator2.hasNext()){
            Map.Entry<String, List<String>> pair = (Map.Entry<String, List<String>>) iterator2.next();
            Entity entity = util.getUrlEntityMap().get(pair.getKey());

            List<Entity> temp = new ArrayList<Entity>();
            for(String child:pair.getValue()){
                if(util.getUrlEntityMap().containsKey(child)){
                    Entity childEntity = util.getUrlEntityMap().get(child);
                    temp.add(childEntity);
                }
            }
            entity.setChildren(temp);

            //   if(contains){
            //       newList = new ArrayList<Entity>();
            newList.add(entity);
            //    }

            iterator2.remove();
        }


        int i =0;
        int correctEntity = -1;
        int maxSize = -1;
        for(Entity entity:newList){
            size =0;
            contains = false;
            addSize(entity, roIdentifier);
            if(contains && size>maxSize){
                correctEntity = i;
                maxSize = size;
            }
            i++;
        }

        List<Entity> finalList = new ArrayList<Entity>();
        if(correctEntity>-1)
            finalList.add(newList.get(correctEntity));

        String json =  new GsonBuilder().create().toJson(finalList);
        return Response.ok(
                json
        ).build();
    }

    int size;
    boolean contains;
    void addSize(Entity entity, String roIdentifier){
        if(entity.getUrl().equalsIgnoreCase(roIdentifier))
            contains = true;
        if(entity.getChildren()!=null&&entity.getChildren().size()>0){
            size += entity.getChildren().size();
            for(Entity childEntity:entity.getChildren()){
                addSize(childEntity, roIdentifier);
            }
        }
    }

    @GET
    @Path("/listRO")
    public Response getResearchObjects(@QueryParam("type") String type,
                                       @QueryParam("submitterId") String submitterId, //Researcher who submitted Curation Object or Curator who submitted Published Object would be the submitters
                                       @QueryParam("creatorId") String creatorId,//Researcher who uploaded/created the data
                                       @QueryParam("repository") String repository, //Repository Name to which CurationObject is to be submitted or to which Published Object was already Published
                                       @QueryParam("fromDate") String fromDate,
                                       @QueryParam("toDate") String toDate) throws IOException, URISyntaxException, OREException, ClassNotFoundException {

        List<ROMetadata> roList = new ArrayList<ROMetadata>();
        List<CollectionWrapper> collections = new RegistryClient(registryServiceUrl).getCollectionList(type, repository, submitterId);//, creatorId);

        for(CollectionWrapper collection:collections){
            ROMetadata ro = new ROMetadata();
            ro.setIdentifier(collection.getCollection().getId());
            ro.setName(collection.getCollection().getEntityName());
            ro.setType(collection.getCollection().getState().getStateName());
            ro.setUpdatedDate(collection.getCollection().getEntityLastUpdatedTime().toString());
            ro.setIsObsolete(collection.getCollection().getIsObsolete());
            for(Relation relation:collection.getRelations()){
                if(relation.getId().getRelationType().getRelationElement().equalsIgnoreCase("curatedBy")){ //must be lazy init, doesn't seem to pick up now
                    ro.setAgentId(relation.getId().getEffect().getId());
                    break;
                }
            }//Add curator agentId to metadata of RO
            roList.add(ro);
        }
        return Response.ok(new GsonBuilder().create().toJson(roList)).build();
    }

    @GET
    @Path("/getsip/{entityId}")
    public Response getSip( @PathParam("entityId") String roIdentifier) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new SeadXstreamStaxModelBuilder().buildSip(
                new DcsDBMapper(registryServiceUrl).getSip(roIdentifier),baos
        );

        return Response.ok( new String(baos.toByteArray(), "UTF-8")).build();
    }

    @GET
    @Path("/getState/{entityId}")
    @Produces("application/json")
    public Response getState( @PathParam("entityId") String roIdentifier) throws Exception {

        Collection collection = new RegistryClient(registryServiceUrl).getCollection(roIdentifier);
        JSONObject response = new JSONObject();
        response.put("identifier", roIdentifier);
        response.put("state", collection.getState().getStateType());
        return Response.ok(response.toString()).build();
    }


    @POST
    @Path("/updateROState")
    public Response UpdateStateToPO(@QueryParam("entityId") String roIdentifier,
                                     @QueryParam("state") String state){

        try {
            new RegistryClient(registryServiceUrl).updateROState(roIdentifier, state);
            return Response.ok().build();
        } catch (NotFoundException e) {
            return Response.serverError().
                    status(Response.Status.NOT_FOUND).
                    entity(e.getResponse().getEntity()).
                    build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    /**
     * POST Methods
     */


    @POST
    @Path("/putro")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response putResearchObject(
            @FormDataParam("file") InputStream resourceMapStream,
            @FormDataParam("file") FormDataContentDisposition resourceMapDetail
    ) throws Exception {


        String directory =
                System.getProperty("java.io.tmpdir");
        String oreFilePath = directory+"/_"+ UUID.randomUUID().toString()+".xml";
        IOUtils.copy(resourceMapStream, new FileOutputStream(oreFilePath));

        InputStream input = new FileInputStream(oreFilePath);
        OREParser parser = OREParserFactory.getInstance("RDF/XML");
        ResourceMap resourceMap = parser.parse(input);

        new OreDBMapper(registryServiceUrl).mapfromOre(resourceMap);

        Map<String, List<String>> metadataMap = new ProvenanceAnalyzer().retrieveProv(resourceMap);

        Predicate DC_TERMS_TITLE = new Predicate();
        String titleTerm = "http://purl.org/dc/terms/title";
        DC_TERMS_TITLE.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_TITLE.setPrefix(Vocab.dcterms_Agent.schema());
        DC_TERMS_TITLE.setName("title");
        DC_TERMS_TITLE.setURI(new URI(titleTerm));

        Predicate DC_TERMS_TYPE = new Predicate();
        String typeTerm = "http://purl.org/dc/terms/type";
        DC_TERMS_TYPE.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_TYPE.setPrefix(Vocab.dcterms_Agent.schema());
        DC_TERMS_TYPE.setName("type");
        DC_TERMS_TYPE.setURI(new URI(typeTerm));

        String thisEntityId = resourceMap.getAggregation().getURI().toString();
        String title = null;
        TripleSelector titleSelector = new TripleSelector();
        titleSelector.setSubjectURI(resourceMap.getAggregation().getURI());
        titleSelector.setPredicate(DC_TERMS_TITLE);
        List<Triple> titleTriples = resourceMap.getAggregation().listAllTriples(titleSelector);

        if(titleTriples.size()>0){
            title = titleTriples.get(0).getObjectLiteral();
        }


        TripleSelector typeSelector = new TripleSelector();
        typeSelector.setSubjectURI(resourceMap.getAggregation().getURI());
        typeSelector.setPredicate(DC_TERMS_TYPE);
        List<Triple> typeTriples = resourceMap.getAggregation().listAllTriples(typeSelector);

        String type = "CurationObject";
        if(typeTriples.size()>0){
            type = typeTriples.get(0).getObjectLiteral();
        }

        if(type.equalsIgnoreCase("PublishedObject")){
            Iterator iterator = metadataMap.entrySet().iterator();
            while(iterator.hasNext()){
                Map.Entry<String,List<String>> pair = (Map.Entry<String, List<String>>) iterator.next();
                if(pair.getKey().contains("Revision")){
                    for(String relatedEntityId: pair.getValue()){
                        Entity relatedEntity = new Entity();
                        relatedEntity.setId(relatedEntityId);

                        Entity thisEntity = new Entity();
                        thisEntity.setId(thisEntityId);
                        thisEntity.setName(title);

                        for(AggregatedResource resource:resourceMap.getAggregatedResources())
                        {
                            Entity memberEntity = new Entity();
                            memberEntity.setId(resource.getURI().toString());
                            titleTriples = resource.listAllTriples(titleSelector);
                            if(titleTriples.size()>0)
                                memberEntity.setName(titleTriples.get(0).getObjectLiteral());
                            thisEntity.addChild(memberEntity);
                        }
                        new KomaduIngester(komaduServiceUrl).trackRevision(relatedEntity,
                                thisEntity
                        );
                    }
                }

                if(pair.getKey().contains("Derived")){
                    for(String relatedEntityId: pair.getValue()){
                        Entity relatedEntity = getCollection(relatedEntityId);

                        Entity thisEntity = new Entity();
                        thisEntity.setId(thisEntityId);
                        thisEntity.setName(title);

                        for(AggregatedResource resource:resourceMap.getAggregatedResources())
                        {
                            Entity memberEntity = new Entity();
                            memberEntity.setId(resource.getURI().toString());
                            titleTriples = resource.listAllTriples(titleSelector);
                            if(titleTriples.size()>0)
                                memberEntity.setName(titleTriples.get(0).getObjectLiteral());
                            thisEntity.addChild(memberEntity);
                        }
                        new KomaduIngester(komaduServiceUrl).trackDerivation(relatedEntity,
                                thisEntity
                        );
                    }
                }
                iterator.remove();
            }
        }
        return Response.ok().build();
    }

    @POST
    @Path("/putjsonldro")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response putJsonldResearchObject(@FormDataParam("ro") String roString) {

        try {
            new JsonDBMapper(registryServiceUrl).mapFromJson(roString);

            JSONObject roObject = new JSONObject(roString);
            JSONObject context = (JSONObject)roObject.get("@context");
            String wasDerivedFromName = null;
            String wasRevisionOfName = null;
            String identifierName = "Identifier";

            Iterator keys = context.keys();
            while(keys.hasNext()){
                String key = (String)keys.next();
                Object value = context.get(key);
                if(value instanceof String && value.equals("http://www.w3.org/ns/prov#wasRevisionOf"))
                    wasRevisionOfName = key;
                if(value instanceof String && value.equals("http://www.w3.org/ns/prov#wasDerivedFrom"))
                    wasDerivedFromName = key;
                //if(value instanceof String && value.equals("http://purl.org/dc/terms/identifier"))
                    //identifierName = key; // TODO : identify correct namespace for Identifier
            }

            String roId = (String)roObject.get(identifierName);
            String parentId = null;
            if(wasDerivedFromName != null && roObject.has(wasDerivedFromName)){
                parentId = (String)roObject.get(wasDerivedFromName);
                if(new RegistryClient(registryServiceUrl).getEntity(parentId, Collection.class.getName()) != null)
                    trackDerivation(parentId, roId);
            } else if(wasRevisionOfName != null && roObject.has(wasRevisionOfName)){
                parentId = (String)roObject.get(wasRevisionOfName);
                if(new RegistryClient(registryServiceUrl).getEntity(parentId, Collection.class.getName()) != null)
                    trackRevision(parentId, roId);
            }

            return Response.ok().build();
        } catch (IOException e) {
            return Response.serverError().build();
        } catch (URISyntaxException e) {
            return Response.serverError().build();
        } catch (ClassNotFoundException e) {
            return Response.serverError().build();
        } catch (JSONException e) {
            return Response.serverError().build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @POST
    @Path("/trackRevision")
    public Response trackRevision(@QueryParam("previous") String previousROId,
                                  @QueryParam("next") String nextROId) throws Exception {

        Entity previousRO = getCollection(previousROId);
        Entity nextRO = getCollection(nextROId);


        new KomaduIngester(komaduServiceUrl).trackRevision(previousRO,
                nextRO
        );
        return Response.ok().build();
    }

    @POST
    @Path("/trackDerivation")
    public Response trackDerivation(@QueryParam("previous") String previousROId,
                                  @QueryParam("next") String nextROId) throws Exception {

        Entity previousRO = getCollection(previousROId);
        Entity nextRO = getCollection(nextROId);


        new KomaduIngester(komaduServiceUrl).trackDerivation(previousRO,
                nextRO
        );
        return Response.ok().build();
    }

    @POST
    @Path("/putEvent")
    public Response trackEvent(@QueryParam("event") String eventStr){

        try {
            Event event = new GsonBuilder().create().fromJson(eventStr, Event.class);
            org.seadva.registry.database.model.obj.vaRegistry.Agent agent = getAgent(event.getLinkingAgentIdentifier());
            Entity collectionEntity  = getCollection(event.getTargetId());

            new KomaduIngester(komaduServiceUrl).trackEvent(event, agent, collectionEntity);
        } catch (Exception e) {
            e.printStackTrace(); // TODO : add logging
            return Response.serverError().entity(e.getMessage()).build();
        }

        return Response.ok().build();

    }

    private Agent getAgent(String agentId) throws IOException, ClassNotFoundException {
        return (Agent)new RegistryClient(registryServiceUrl).getEntity(agentId,
                Agent.class.getName());
    }

    private Entity getCollection(String collectionId) throws IOException, ClassNotFoundException {
        org.seadva.registry.database.model.obj.vaRegistry.Collection collection = (org.seadva.registry.database.model.obj.vaRegistry.Collection)new RegistryClient(registryServiceUrl).getEntity(collectionId,
                org.seadva.registry.database.model.obj.vaRegistry.Collection.class.getName());
        List<AggregationWrapper> aggregations =  new RegistryClient(registryServiceUrl).getAggregation(collectionId);

        Entity collectionEntity = new Entity();
        collectionEntity.setId(collectionId);
        collectionEntity.setName(collection.getName());

        for(AggregationWrapper aggregation:aggregations){
            Entity child = new Entity();
            child.setId(aggregation.getChild().getId());
            BaseEntity baseEntity = new RegistryClient(registryServiceUrl).getEntity(child.getId(),
                    aggregation.getChildType());
            child.setName(baseEntity.getEntityName());
            collectionEntity.addChild(child);
        }

        return collectionEntity;
    }

    @POST
    @Path("/putsip")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response putSip(
            @FormDataParam("file") InputStream sipStream,
            @FormDataParam("file") FormDataContentDisposition resourceMapDetail
    ) throws Exception {


        String directory =
                System.getProperty("java.io.tmpdir");
        String sipFilePath = directory+"/_"+ UUID.randomUUID().toString()+".xml";
        IOUtils.copy(sipStream, new FileOutputStream(sipFilePath));

        InputStream input = new FileInputStream(sipFilePath);
        ResearchObject sip = new SeadXstreamStaxModelBuilder().buildSip(input);

        new DcsDBMapper(registryServiceUrl).mapfromSip(sip);

        return Response.ok().build();
    }

    @POST
    @Path("/obsolete/{entityId}")
    public Response deleteRO( @PathParam("entityId") String roIdentifier) throws Exception {
        new RegistryClient(registryServiceUrl).makeObsolete(roIdentifier);
        return Response.ok().build();
    }

}