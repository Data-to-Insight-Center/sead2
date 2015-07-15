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

package org.seadva.registry.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.commons.io.IOUtils;
import org.seadva.registry.database.model.obj.vaRegistry.*;
import org.seadva.registry.database.model.obj.vaRegistry.CollectionWrapper;
import org.seadva.registry.service.exception.NotFoundException;
import org.seadva.registry.service.util.QueryAttributeType;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.ws.http.HTTPException;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class RegistryClient {

    static WebResource resource;
    static String serviceUrl;
    static Gson gson;
    private static WebResource resource(){
        return resource;
    }

    public RegistryClient(String url){
        this.serviceUrl = url;
        resource = Client.create().resource(serviceUrl);
        gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
    }

    /**
     * GET methods
     *
     */

    public BaseEntity getEntity(String entityId, String type) throws IOException, ClassNotFoundException {
        if(type.equalsIgnoreCase(Collection.class.getName()))
            return  getCollection(entityId);
        else if(type.equalsIgnoreCase(File.class.getName()))
            return getFile(entityId);

        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        ClientResponse response = webResource.path("resource")
                .path("entity")
                .path(
                        URLEncoder.encode(
                                entityId
                        )
                )
                .queryParams(params)
                .get(ClientResponse.class);

        if(response.getStatus()!=200)
            throw new HTTPException(response.getStatus());

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        return (BaseEntity) gson.fromJson(writer.toString(), Class.forName(type));
    }

    public Collection getCollection(String collectionId) throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        ClientResponse response = webResource.path("resource")
                .path("collection")
                .path(
                        URLEncoder.encode(
                                collectionId
                        )
                )
                .queryParams(params)
                .get(ClientResponse.class);

        if(response.getStatus()!=200)
            throw new HTTPException(response.getStatus());

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        return gson.fromJson(writer.toString(), Collection.class);
    }


    public File getFile(String fileId) throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        ClientResponse response = webResource.path("resource")
                .path("file")
                .path(
                        URLEncoder.encode(
                                fileId
                        )
                )
                .queryParams(params)
                .get(ClientResponse.class);

        if(response.getStatus()!=200)
            throw new HTTPException(response.getStatus());

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        return gson.fromJson(writer.toString(), File.class);
    }

    public List<Fixity> getFixity(String fileId) throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        ClientResponse response = webResource.path("resource")
                .path("fixity")
                .path(
                        URLEncoder.encode(
                                fileId
                        )
                )
                .queryParams(params)
                .get(ClientResponse.class);

        if(response.getStatus()!=200)
            throw new HTTPException(response.getStatus());

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        Type listType = new TypeToken<ArrayList<Fixity>>() {
        }.getType();
        return gson.fromJson(writer.toString(), listType);
    }

    public List<CollectionWrapper> getCollectionList(String type, String repository, String submitterId) throws IOException {
        WebResource webResource = resource();
        webResource = webResource.path("resource")
                .path("listCollections")
                .path(type);

        if(repository!=null)
            webResource = webResource.queryParam("repository", repository);

        if(submitterId!=null)
            webResource = webResource.queryParam("submitterId", submitterId);

        ClientResponse response = webResource
                .get(ClientResponse.class);

        if(response.getStatus()!=200)
            throw new HTTPException(response.getStatus());

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        Type listType = new TypeToken<ArrayList<CollectionWrapper>>() {
        }.getType();
        return gson.fromJson(writer.toString(), listType);
    }


    public List<AggregationWrapper> getAggregation(String parentId) throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        ClientResponse response = webResource.path("resource")
                .path("aggregation")
                .path(
                        URLEncoder.encode(
                                parentId
                        )
                )
                .queryParams(params)
                .get(ClientResponse.class);

        if(response.getStatus()!=200)
            throw new HTTPException(response.getStatus());

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        Type listType = new TypeToken<ArrayList<AggregationWrapper>>() {
        }.getType();
        return gson.fromJson(writer.toString(), listType);
    }

    public List<Relation> getRelation(String causeId) throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        ClientResponse response = webResource.path("resource")
                .path("relation")
                .path(
                        URLEncoder.encode(
                                causeId
                        )
                )
                .queryParams(params)
                .get(ClientResponse.class);

        if(response.getStatus()!=200)
            throw new HTTPException(response.getStatus());

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        Type listType = new TypeToken<ArrayList<Relation>>() {
        }.getType();
        return gson.fromJson(writer.toString(), listType);
    }

    public Repository getRepositoryByName(String repoName) throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        ClientResponse response = webResource.path("resource")
                .path("repository")
                .path(
                        repoName
                )
                .queryParams(params)
                .get(ClientResponse.class);

        if(response.getStatus()!=200)
            throw new HTTPException(response.getStatus());

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        return gson.fromJson(writer.toString(), Repository.class);
    }

    public DataIdentifierType getDataIdentifierType(String typeName) throws IOException, ClassNotFoundException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        ClientResponse response = webResource.path("resource")
                .path("identifiertype")
                .path(
                        typeName
                )
                .queryParams(params)
                .get(ClientResponse.class);

        if(response.getStatus()!=200)
            throw new HTTPException(response.getStatus());

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        return (DataIdentifierType) gson.fromJson(writer.toString(), DataIdentifierType.class);
    }

    public MetadataType getMetadataType(String entityId) throws IOException, ClassNotFoundException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        ClientResponse response = webResource.path("resource")
                .path("metadata")
                .path(
                        URLEncoder.encode(
                                entityId
                        )
                )
                .queryParams(params)
                .get(ClientResponse.class);

        if(response.getStatus()!=200)
            throw new HTTPException(response.getStatus());

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        return (MetadataType) gson.fromJson(writer.toString(), MetadataType.class);
    }

    public MetadataType getMetadataByType(String element) throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        ClientResponse response = webResource.path("resource")
                .path("metadataType")
                .path(
                        element
                )
                .queryParams(params)
                .get(ClientResponse.class);

        if(response.getStatus()!=200)
        {    if(response.getStatus()==404)
            return null;
        else
            throw new HTTPException(response.getStatus());
        }

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        MetadataType metadataType = (MetadataType) gson.fromJson(writer.toString(), MetadataType.class);

        return metadataType;
    }

    public RelationType getRelationByType(String element) throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        ClientResponse response = webResource.path("resource")
                .path("relationType")
                .path(
                        element
                )
                .queryParams(params)
                .get(ClientResponse.class);

        if(response.getStatus()!=200)
        {
            if(response.getStatus()==404)
                return null;
            else
                throw new HTTPException(response.getStatus());
        }

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        RelationType relationType = (RelationType) gson.fromJson(writer.toString(), RelationType.class);

        return relationType;
    }


    public List<BaseEntity> queryByProperty(String key, String value, QueryAttributeType attributeType) throws IOException {
        WebResource webResource = resource();

         webResource = webResource.path("resource")
                .path("query")
                .queryParam("value", value)
                .queryParam("type", attributeType.getName());

        if(key!=null)
                webResource = webResource.queryParam("key", key);


        ClientResponse response  = webResource.get(ClientResponse.class);

        if(response.getStatus()!=200)
            throw new HTTPException(response.getStatus());
        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        Type listType = new TypeToken<ArrayList<BaseEntity>>() {
        }.getType();
        return (List<BaseEntity>) gson.fromJson(writer.toString(), listType);
    }

    /**
     * POST (Create)  Test cases
     *
     */

    public void postCollection(Collection collection) throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        List<String> values = new ArrayList<String>();
        values.add(gson.toJson(collection));
        params.put("entity",values);

        List<String> types = new ArrayList<String>();
        types.add("org.seadva.registry.database.model.obj.vaRegistry.Collection");

        params.put("type", types);
        ClientResponse response = webResource.path("resource")
                .path(
                        URLEncoder.encode(
                                collection.getId()
                        )
                )
                .queryParams(params)
                .post(ClientResponse.class);

        if(response.getStatus()!=200)
            throw new HTTPException(response.getStatus());
    }


    public void postFixity(List<Fixity> fixities) throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        List<String> values = new ArrayList<String>();
        values.add(gson.toJson(fixities));
        params.put("fixityList",values);

        ClientResponse response = webResource.path("resource")
                .path("fixity")
                .queryParams(params)
                .post(ClientResponse.class);
        if(response.getStatus()!=200)
            throw new HTTPException(response.getStatus());
    }


    public void postFile(File file) throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        List<String> values = new ArrayList<String>();
        values.add(gson.toJson(file));
        params.put("entity",values);

        List<String> types = new ArrayList<String>();
        types.add(File.class.getName());

        params.put("type", types);
        ClientResponse response = webResource.path("resource")
                .path(
                        URLEncoder.encode(
                                file.getId()
                        )
                )
                .queryParams(params)
                .post(ClientResponse.class);
        if(response.getStatus()!=200)
            throw new HTTPException(response.getStatus());
    }


    /**
     * Base entity post
     */

    public void postEntity(BaseEntity entity) throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        List<String> values = new ArrayList<String>();
        values.add(gson.toJson(entity));
        params.put("entity",values);

        List<String> types = new ArrayList<String>();
        types.add(BaseEntity.class.getName());

        params.put("type", types);
        ClientResponse response = webResource.path("resource")
                .path(
                        URLEncoder.encode(
                                entity.getId()
                        )
                )
                .queryParams(params)
                .post(ClientResponse.class);
        if(response.getStatus()!=200)
            throw new HTTPException(response.getStatus());
    }

    private static RoleType getRoleByName(String role) throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        ClientResponse response = webResource.path("resource")
                .path("roleType")
                .path(
                        role
                )
                .queryParams(params)
                .get(ClientResponse.class);

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        return gson.fromJson(writer.toString(), RoleType.class);
    }


    public State getStateByName(String stateName) throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        ClientResponse response = webResource.path("resource")
                .path("state")
                .path(
                        stateName
                )
                .queryParams(params)
                .get(ClientResponse.class);

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        return gson.fromJson(writer.toString(), State.class);
    }

    public void postAgent(Agent agent, String roleName) throws IOException {
        WebResource webResource = resource();

        AgentRole role = new AgentRole();
        AgentRolePK agentRolePK = new AgentRolePK();
        agentRolePK.setAgent(agent);
        agentRolePK.setRoleType(getRoleByName(roleName));
        role.setId(agentRolePK);
        agent.addAgentRole(role);

        String json = gson.toJson(agent);
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        List<String> values = new ArrayList<String>();
        values.add(json);
        params.put("entity",values);

        List<String> types = new ArrayList<String>();
        types.add("org.seadva.registry.database.model.obj.vaRegistry.Agent");

        params.put("type", types);
        ClientResponse response = webResource.path("resource")
                .path("agent")
                .path(
                        URLEncoder.encode(
                                agent.getId()
                        )
                )
                .queryParams(params)
                .post(ClientResponse.class);

        if(response.getStatus()!=200)
            throw new HTTPException(response.getStatus());
    }

    /**
     *
     * @param aggregationWrappers
     * @param parentId
     * @return
     * @throws java.io.IOException
     */
    public void postAggregation(List<AggregationWrapper> aggregationWrappers, String parentId) throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        List<String> values = new ArrayList<String>();
        values.add(gson.toJson(aggregationWrappers));
        params.put("aggList",values);

        ClientResponse response = webResource.path("resource")
                .path("aggregation")
                .path(
                        URLEncoder.encode(
                                parentId
                        )
                )
                .queryParams(params)
                .post(ClientResponse.class);
        if(response.getStatus()!=200)
            throw new HTTPException(response.getStatus());
    }

    public void postRelation(List<Relation> relationList) throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        List<String> values = new ArrayList<String>();
        values.add(gson.toJson(relationList));
        params.put("relList",values);

        ClientResponse response = webResource.path("resource")
                .path("relation")
                .queryParams(params)
                .post(ClientResponse.class);
        if(response.getStatus()!=200)
            throw new HTTPException(response.getStatus());
    }

    public void deleteRelation(List<Relation> relationList) throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        List<String> values = new ArrayList<String>();
        values.add(gson.toJson(relationList));
        params.put("relList",values);

        ClientResponse response = webResource.path("resource")
                .path("delrelation")
                .queryParams(params)
                .post(ClientResponse.class);
        if(response.getStatus()!=200)
            throw new HTTPException(response.getStatus());
    }

    public void makeObsolete(String entityId) throws IOException {
        WebResource webResource = resource();


        ClientResponse response = webResource.path("resource")
                .path("obsolete")
                .path(
                        URLEncoder.encode(
                                entityId
                        )
                )
                .post(ClientResponse.class);

        if(response.getStatus()!=200)
            throw new HTTPException(response.getStatus());
    }

    public void updateROStatus(String entityId, String status) throws IOException {

        WebResource webResource = resource();
        ClientResponse response = webResource.path("resource")
                .path("updateStatus")
                .queryParam("entityId", URLEncoder.encode(entityId))
                .queryParam("state",status)
                .post(ClientResponse.class);

        if(response.getStatus()!=200) {
            StringWriter writer = new StringWriter();
            IOUtils.copy(response.getEntityInputStream(), writer);
            throw new NotFoundException(writer.toString());
        }

    }
}
