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

package org.seadva.registry.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.IOUtils;
import org.seadva.registry.database.common.DBConnectionPool;
import org.seadva.registry.database.model.dao.vaRegistry.*;
import org.seadva.registry.database.model.dao.vaRegistry.impl.*;
import org.seadva.registry.database.model.obj.vaRegistry.*;
import org.seadva.registry.service.exception.NotFoundException;
import org.seadva.registry.service.util.QueryAttributeType;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;
import org.springframework.test.context.transaction.TransactionConfiguration;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//import com.sun.jersey.multipart.FormDataParam;

/**
 * REST interface
 */

@Component
@Configurable
@Path("/resource")
@TransactionConfiguration(defaultRollback=false)
public class ResourceService {

    static Gson gson;
    //  static DataLayerVaRegistry dataLayerVaRegistry;
    static BaseEntityDao baseEntityDao;
    static CollectionDao collectionEntityDao;
    static FileDao fileDao;
    static MetadataTypeDao metadataTypeDao;
    static DataIdentifierTypeDao dataIdentifierTypeDao;
    static DataIdentifierDao dataIdentifierDao;
    static RelationTypeDao relationTypeDao;
    static RoleTypeDao roleTypeDao;
    static RepositoryDao repositoryDao;
    static ProfileTypeDao profileTypeDao;
    static PropertyDao propertyDao;
    static StateDao stateDao;
    static FixityDao fixityDao;
    static AggregationDao aggregationDao;
    static RelationDao relationDao;
    static AgentDao agentDao;
    static String databaseUrl;
    static String databaseUser;
    static String databasePassword;

    static {

        InputStream inputStream =
                ResourceService.class.getResourceAsStream("./Config.properties");

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
            if(name.equalsIgnoreCase("database.url"))
                databaseUrl = value;
            else if(name.equalsIgnoreCase("database.username"))
                databaseUser = value;
            else if(name.equalsIgnoreCase("database.password"))
                databasePassword = value;
        }
        try {
            DBConnectionPool.init(databaseUrl, databaseUser, databasePassword,8,30,0);
            DBConnectionPool.launch();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        baseEntityDao = new BaseEntityDaoImpl();
        collectionEntityDao = new CollectionDaoImpl();
        metadataTypeDao = new MetadataTypeDaoImpl();
        dataIdentifierTypeDao = new DataIdentifierTypeDaoImpl();
        dataIdentifierDao = new DataIdentifierDaoImpl();
        relationTypeDao = new RelationTypeDaoImpl();
        roleTypeDao = new RoleTypeDaoImpl();
        repositoryDao = new RepositoryDaoImpl();
        profileTypeDao = new ProfileTypeDaoImpl();
        propertyDao = new PropertyDaoImpl();
        stateDao = new StateDaoImpl();
        fixityDao = new FixityDaoImpl();
        aggregationDao = new AggregationDaoImpl();
        relationDao = new RelationDaoImpl();
        fileDao = new FileDaoImpl();
        agentDao = new AgentDaoImpl();
    }

    
    /**
     * Ping method to check whether the registry service is up
     *
     * @return ACK
     */
    @GET
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    public String ping() {
        return "SEAD Registry Service is up!";
    }    
    
    /* GET types */

    @GET
    @Path("/metadataType/{typeId}")
    @Produces("application/json")
    public Response getType( @PathParam("typeId") String typeId) throws Exception {
        MetadataType metadataType = metadataTypeDao.getMetadataType(typeId);
        if(metadataType==null)
            throw new NotFoundException("Role type not found in role type registry");
        return Response.ok(gson.toJson(metadataType)).build();
    }



    @GET
    @Path("/identifiertype/{typename}")
    @Produces("application/json")
    public Response getIdentifierType( @PathParam("typename") String typeName) throws Exception {
        DataIdentifierType dataIdentifierType = dataIdentifierTypeDao.getDataIdentifierType(typeName);
        if(dataIdentifierType==null)
            throw new NotFoundException("DataIdentifer type not found in DataIdentifer type registry");
        return Response.ok(gson.toJson(dataIdentifierType)).build();
    }


    @GET
    @Path("/relationType/{element}")
    @Produces("application/json")
    public Response getRelationByType( @PathParam("element") String element) throws Exception {

        RelationType relationType = relationTypeDao.getRelationTypeByName(element);
        if(relationType == null)
            throw new NotFoundException("Relation type not found in relation type registry");
        return Response.ok(gson.toJson(relationType)).build();
    }

    @GET
    @Path("/roleType/{element}")
    @Produces("application/json")
    public Response getRoleByType( @PathParam("element") String element) throws Exception {

        RoleType roleType = roleTypeDao.getRoleType(element);
        if(roleType == null)
            throw new NotFoundException("Role type not found in role type registry");
        return Response.ok(gson.toJson(roleType)).build();
    }

    @GET
    @Path("/profileType/{element}")
    @Produces("application/json")
    public Response getProfileByType( @PathParam("element") String element) throws Exception {
        ProfileType profileType = profileTypeDao.getProfileType(element);
        if(profileType==null)
            throw new NotFoundException("Profile type not found in role type registry");
        return Response.ok(gson.toJson(profileType)).build();
    }

    @GET
    @Path("/repository/{name}")
    @Produces("application/json")
    public Response getRepositoryByName( @PathParam("name") String repoName) throws Exception {
        Repository repository = repositoryDao.getRepository(repoName);
        if(repository==null)
            throw new NotFoundException("No repository found matching the given name "+repoName+" in registry");
        return Response.ok(gson.toJson(repository)).build();
    }

    @GET
    @Path("/state/{name}")
    @Produces("application/json")
    public Response getStateByName( @PathParam("name") String stateName) throws Exception {
        State state = stateDao.getState(stateName);
        if(state == null)
            throw new NotFoundException("No state found matching the given name "+stateName+" in registry");
        return Response.ok(gson.toJson(state)).build();
    }

    /* GET by ID methods */

    @GET
    @Path("/entity/{entityId}")
    @Produces("application/json")
    public Response getEntity( @PathParam("entityId") String entityId) throws Exception {

        BaseEntity entity = baseEntityDao.getBaseEntity(entityId);
        String json = gson.toJson(entity);
        return Response.ok(json).build();
    }

    @GET
    @Path("/collection/{entityId}")
    @Produces("application/json")
    public Response getCollection( @PathParam("entityId") String entityId) throws Exception {

        Collection entity = collectionEntityDao.getCollection(entityId);
        String json = gson.toJson(entity);
        return Response.ok(json).build();
    }

    @GET
    @Path("/file/{entityId}")
    @Produces("application/json")
    public Response getFile( @PathParam("entityId") String entityId) throws Exception {

        File entity = fileDao.getFile(entityId);
        String json = gson.toJson(entity);
        return Response.ok(json).build();
    }

    @GET
    @Path("/aggregation/{entityId}")
    @Produces("application/json")
    public Response getAggregations( @PathParam("entityId") String entityId) throws Exception {

        List<AggregationWrapper> aggregationList = aggregationDao.getAggregations(entityId);
        return Response.ok(gson.toJson(aggregationList)).build();
    }


    @GET
    @Path("/altId")
    @Produces("application/json")
    public Response getByIdentifier(@QueryParam("alternateId") String alternateId) throws Exception {

        List<DataIdentifier> identifiers = new ArrayList<DataIdentifier>();

        if(alternateId==null)
            return Response.ok(gson.toJson(identifiers)).build();

        identifiers = dataIdentifierDao.getDataIdentifiersByValue(alternateId);

        return Response.ok(gson.toJson(identifiers)).build();
    }


    @GET
    @Path("/fixity/{entityId}")
    @Produces("application/json")
    public Response getFixities( @PathParam("entityId") String entityId) throws Exception {
        List<Fixity> fixityList = fixityDao.getFixities(entityId);
        return Response.ok(gson.toJson(fixityList)).build();
    }

    @GET
    @Path("/relation/{entityId}")
    @Produces("application/json")
    public Response getRelations( @PathParam("entityId") String entityId) throws Exception {

        List<Relation> relationList =  relationDao.getRelations(entityId);
        Set<Relation> newRelationList = new HashSet<Relation>();

        for(Relation relation:relationList){
            Relation newRelation = new Relation();
            RelationPK newRelationPK = new RelationPK();

            RelationPK relationPK = relation.getId();

            RelationType relationType = new RelationType();
            relationType.setId(relationPK.getRelationType().getId());
            relationType.setRelationSchema(relationPK.getRelationType().getRelationSchema());
            relationType.setRelationElement(relationPK.getRelationType().getRelationElement());
            newRelationPK.setRelationType(relationType);

            BaseEntity effectEntity = new BaseEntity();
            effectEntity.setId(relationPK.getEffect().getId());
            effectEntity.setEntityName(relationPK.getEffect().getEntityName());
            effectEntity.setEntityCreatedTime(relationPK.getEffect().getEntityCreatedTime());
            effectEntity.setEntityLastUpdatedTime(relationPK.getEffect().getEntityLastUpdatedTime());
            newRelationPK.setEffect(effectEntity);

            BaseEntity causeEntity = new BaseEntity();
            causeEntity.setId(relationPK.getCause().getId());
            causeEntity.setEntityName(relationPK.getCause().getEntityName());
            causeEntity.setEntityCreatedTime(relationPK.getCause().getEntityCreatedTime());
            causeEntity.setEntityLastUpdatedTime(relationPK.getCause().getEntityLastUpdatedTime());
            newRelationPK.setCause(causeEntity);
            RelationType rlType = relationPK.getRelationType();
            newRelationPK.setRelationType(new RelationType(rlType.getId(), rlType.getRelationElement(), rlType.getRelationSchema()));
            newRelation.setId(newRelationPK);

            newRelationList.add(newRelation);
        }

        String json = gson.toJson(newRelationList);
        return Response.ok(json).build();
    }

     /* GET methods - for querying by properties */

    @GET
    @Path("/listCollections/{type}")
    @Produces("application/json")
    public Response getAllCollections(@PathParam("type") String type,
                                      @QueryParam("submitterId") String submitterId, //Researcher who submitted Curation Object or Curator who submitted Published Object would be the submitters
                                      @QueryParam("repository") String repository, //Repository Name to which CurationObject is to be submitted or to which Published Object was already Published
                                      @QueryParam("fromDate") String fromDate,
                                      @QueryParam("toDate") String toDate) throws Exception {

        List<CollectionWrapper> finalCollectionWrappers = new ArrayList<CollectionWrapper>();
        List<Collection> collections = collectionEntityDao.listCollections(submitterId, repository, type);
        for(Collection collection:collections){

            List<Relation> relationList =  relationDao.getRelations(collection.getId());
            Set<Relation> newRelationList = new HashSet<Relation>();
            CollectionWrapper newCollectionWrapper = new CollectionWrapper(collection);
            newCollectionWrapper.setRelations(new HashSet<Relation>());

            for(Relation relation:relationList){
                Relation newRelation = new Relation();
                RelationPK newRelationPK = new RelationPK();

                RelationPK relationPK = relation.getId();

                RelationType relationType = new RelationType();
                relationType.setId(relationPK.getRelationType().getId());
                relationType.setRelationSchema(relationPK.getRelationType().getRelationSchema());
                relationType.setRelationElement(relationPK.getRelationType().getRelationElement());
                newRelationPK.setRelationType(relationType);

                BaseEntity effectEntity = new BaseEntity();
                effectEntity.setId(relationPK.getEffect().getId());
                effectEntity.setEntityName(relationPK.getEffect().getEntityName());
                effectEntity.setEntityCreatedTime(relationPK.getEffect().getEntityCreatedTime());
                effectEntity.setEntityLastUpdatedTime(relationPK.getEffect().getEntityLastUpdatedTime());
                newRelationPK.setEffect(effectEntity);

                BaseEntity causeEntity = new BaseEntity();
                causeEntity.setId(relationPK.getCause().getId());
                causeEntity.setEntityName(relationPK.getCause().getEntityName());
                causeEntity.setEntityCreatedTime(relationPK.getCause().getEntityCreatedTime());
                causeEntity.setEntityLastUpdatedTime(relationPK.getCause().getEntityLastUpdatedTime());
                newRelationPK.setCause(causeEntity);
                RelationType rlType = relationPK.getRelationType();
                newRelationPK.setRelationType(new RelationType(rlType.getId(), rlType.getRelationElement(), rlType.getRelationSchema()));
                newRelation.setId(newRelationPK);

                newRelationList.add(newRelation);
            }

            newCollectionWrapper.setRelations(newRelationList);
            finalCollectionWrappers.add(newCollectionWrapper);
        }
        return Response.ok(gson.toJson(finalCollectionWrappers)).build();
    }


    @GET
    @Path("/query")
    @Produces("application/json")
    public Response queryCollections(@QueryParam("key") String propertyKey,
                                     @QueryParam("value") String propertyValue,
                                     @QueryParam("type") String type) throws Exception {


        List<Collection> collections = new ArrayList<Collection>();

        if(type==null||propertyValue==null)
            return Response.ok(gson.toJson(collections)).build();

        QueryAttributeType queryAttributeType = QueryAttributeType.fromString(type);

        if(queryAttributeType ==null)
            return Response.ok(gson.toJson(collections)).build();

        if(queryAttributeType == QueryAttributeType.PROPERTY){
            if(propertyKey==null)
                return Response.ok(gson.toJson(collections)).build();
            collections = collectionEntityDao.queryByProperty(propertyKey, propertyValue);
        }
        else if(queryAttributeType == QueryAttributeType.DATA_IDENTIFIER){
            List<DataIdentifier> dataIdentifiers = dataIdentifierDao.getDataIdentifiersByValue(propertyValue);
            for(DataIdentifier identifier: dataIdentifiers){
                collections.add(collectionEntityDao.getCollection(identifier.getId().getEntity().getId()));
            }
        }  //Todo for Data Location

        return Response.ok(gson.toJson(collections)).build();
    }




    /*POST methods */

    @POST
    @Path("/{entityId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putResource(
            @QueryParam("entity") String entityJson,
            @QueryParam("type") String type
            //, @DefaultValue("false") @QueryParam("update") boolean update
    ) throws IOException, ClassNotFoundException

    {
        BaseEntity baseEntity = (BaseEntity) gson.fromJson(entityJson, Class.forName(type));

        Set<Property> tempProperties =  new HashSet<Property>(baseEntity.getProperties());
        baseEntity.setProperties(new HashSet<Property>());
        Set<Property> newProperties =  new HashSet<Property>();

        for(Property property: tempProperties){
            property.setEntity(baseEntity);
            newProperties.add(property);
        }

        BaseEntity existingEntity = baseEntityDao.getBaseEntity(baseEntity.getId());
        if(existingEntity!=null)
        {
//            if(update==true)
//            {
                propertyDao.deleteProperties(baseEntity.getId());
                baseEntity.setProperties(newProperties);
//            }
//            else
//                baseEntity.setProperties(existingEntity.getProperties(), newProperties);
        }
        else
            baseEntity.setProperties(newProperties);

        Set<DataLocation> tempDataLocations =  new HashSet<DataLocation>(baseEntity.getDataLocations());
        baseEntity.setDataLocations(new HashSet<DataLocation>());
        Set<DataLocation> newDataLocations =  new HashSet<DataLocation>();

        for(DataLocation dataLocation: tempDataLocations){
            DataLocationPK dataLocationPK = dataLocation.getId();
            dataLocationPK.setEntity(baseEntity);
            dataLocation.setId(dataLocationPK);
            newDataLocations.add(dataLocation);
        }
        baseEntity.setDataLocations(newDataLocations);

        Set<DataIdentifier> tempDataIdentifiers =  new HashSet<DataIdentifier>(baseEntity.getDataIdentifiers());
        baseEntity.setDataIdentifiers(new HashSet<DataIdentifier>());
        Set<DataIdentifier> newDataIdentifiers =  new HashSet<DataIdentifier>();

        for(DataIdentifier dataIdentifier: tempDataIdentifiers){
            DataIdentifierPK dataIdentifierPK = dataIdentifier.getId();
            dataIdentifierPK.setEntity(baseEntity);
            dataIdentifier.setId(dataIdentifierPK);
            newDataIdentifiers.add(dataIdentifier);
        }
        baseEntity.setDataIdentifiers(newDataIdentifiers);



        //Todo
        if(baseEntity instanceof File){
            Set<Format> tempFormats =  new HashSet<Format>(((File) baseEntity).getFormats());
            ((File) baseEntity).setFormats(new HashSet<Format>());
            Set<Format> newFormats =  new HashSet<Format>();

            for(Format format: tempFormats){
                format.setEntity((File) baseEntity);
                newFormats.add(format);
            }
            ((File) baseEntity).setFormats(newFormats);
        }
        if(baseEntity instanceof Collection)
            collectionEntityDao.insertCollection((Collection)baseEntity);
        else if(baseEntity instanceof File)
            fileDao.insertFile((File)baseEntity);
        else
            baseEntityDao.insertEntity(baseEntity);

        return Response.ok().build();
    }


    @POST
    @Path("/agent/{agentId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putAgent(
            @QueryParam("entity") String entityJson
    ) throws IOException, ClassNotFoundException

    {
        Agent agent = gson.fromJson(entityJson, Agent.class);
        agentDao.putAgent(agent);
        return Response.ok().build();
    }

    @POST
    @Path("/aggregation/{entityId}")
    @Consumes("application/json")
    public Response postAggregations( @QueryParam("aggList") String aggregationJson
    ) throws IOException, ClassNotFoundException

    {
        Type listType = new TypeToken<ArrayList<AggregationWrapper>>() {
        }.getType();
        List<AggregationWrapper> aggregationWrapperList = gson.fromJson(aggregationJson, listType);
        for(AggregationWrapper aggregationWrapper: aggregationWrapperList){
            Aggregation aggregation = new Aggregation();
            AggregationPK aggregationPK = new AggregationPK();
            aggregationPK.setParent(aggregationWrapper.getParent());
            aggregationPK.setChild(aggregationWrapper.getChild());
            aggregation.setId(aggregationPK);
            aggregationDao.putAggregation(aggregation);
        }

        return Response.ok().build();
    }


    @POST
    @Path("/fixity")
    @Consumes("application/json")
    public Response postFixity( @QueryParam("fixityList") String fixityJson
    ) throws IOException, ClassNotFoundException

    {
        Type listType = new TypeToken<ArrayList<Fixity>>() {
        }.getType();
        List<Fixity> fixityList = gson.fromJson(fixityJson, listType);
        fixityDao.putFixities(fixityList);
        return Response.ok().build();
    }

    @POST
    @Path("/relation")
    @Consumes("application/json")
    public Response postRelations( @QueryParam("relList") String relationListJson
    ) throws IOException, ClassNotFoundException

    {
        Type listType = new TypeToken<ArrayList<Relation>>() {
        }.getType();
        List<Relation> relationList =  gson.fromJson(relationListJson, listType);
        for(Relation relation: relationList){
            relationDao.putRelation(relation);
        }
        return Response.ok().build();
    }

    @POST
    @Path("/delrelation")
    @Consumes("application/json")
    public Response deleteRelations( @QueryParam("relList") String relationListJson
    ) throws IOException, ClassNotFoundException
    {
        Type listType = new TypeToken<ArrayList<Relation>>() {
        }.getType();
        List<Relation> relationList =  gson.fromJson(relationListJson, listType);
        for(Relation relation: relationList){
            relationDao.deleteRelation(relation);
        }

        return Response.ok().build();
    }

    @POST
    @Path("/obsolete/{entityId}")
    public Response makeObsolete( @PathParam("entityId") String entityId) throws IOException, ClassNotFoundException

    {
        baseEntityDao.updateEntity(entityId, 1);
        return Response.ok().build();
    }

    @POST
    @Path("/updateState")
    public Response updateState(@QueryParam("entityId") String entityId,
                                 @QueryParam("state") String stateName){

        State state = stateDao.getState(stateName);
        if(state.getId() == null)
            throw new NotFoundException("No state found matching the given name "+stateName+" in registry");
        boolean entityUpdated = collectionEntityDao.updateState(entityId, state.getId());
        if(entityUpdated)
            return Response.ok(gson.toJson(state)).build();
        else
            throw new NotFoundException("No collection found matching the given name "+entityId+" in registry");

    }
}
