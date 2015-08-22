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
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import org.apache.commons.io.IOUtils;
import org.dataconservancy.dcs.ingest.Events;
import org.junit.Test;
import org.seadva.data.lifecycle.support.model.Event;
import org.seadva.registry.client.RegistryClient;
import org.seadva.registry.database.model.obj.vaRegistry.Agent;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class ROServiceRESTTest extends JerseyTest {

    String roSubsystemUrl;
    String registryUrl;
    public ROServiceRESTTest() throws Exception {
        super(new WebAppDescriptor.Builder("org.seadva.data.lifecycle").
                contextParam("testPath","/src/main/webapp/").build());
        roSubsystemUrl = "http://localhost:8080/ro";
        registryUrl = "http://localhost:8080/registry/rest";
    }

    public WebResource resource(){
        return Client.create().resource(this.roSubsystemUrl);
    }

    /**
     * POST (Update) Test cases
     *
     */

    @Test
    public void testPostCollection() throws IOException {
        WebResource webResource = resource();

        File file = new File(
                getClass().getResource("./Vortex2_Visualization_oaiore_ingest.xml").getFile()
        );
        FileDataBodyPart fdp = new FileDataBodyPart("file", file,
                MediaType.APPLICATION_OCTET_STREAM_TYPE);

        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();

        formDataMultiPart.bodyPart(fdp);

        ClientResponse response = webResource.path("resource")
                .path("putro")
                .type(MediaType.MULTIPART_FORM_DATA)
                .post(ClientResponse.class, formDataMultiPart);

        assertEquals(200, response.getStatus());
    }

    @Test
    public void testTrackEvent() throws Exception {

        String agentId = "agent:" + UUID.randomUUID().toString();
        Agent agent = new Agent();
        agent.setFirstName("Kavitha");
        agent.setLastName("Chandrasekar");
        agent.setId(agentId);
        agent.setEntityName(agent.getLastName());
        agent.setEntityCreatedTime(new Date());
        agent.setEntityLastUpdatedTime(new Date());

        new RegistryClient(registryUrl).postAgent(agent, "Curator");
        WebResource webResource = resource();
        Event event = new Event();
        event.setEventIdentifier("testevent1");
        event.setEventDateTime(new Date());
        event.setLinkingAgentIdentifier(agentId);
        event.setWorkflowId("testwf1");
        event.setTargetId("http://seadva-test.d2i.indiana.edu/sead-wf/entity/766381");
        event.setEventType(Events.DEPOSIT);

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        List<String> values = new ArrayList<String>();
        values.add(new GsonBuilder().create().toJson(event));
        params.put("event", values);
        ClientResponse response = webResource.path("resource")
                .path("putEvent")
                .queryParams(params)
                .post(ClientResponse.class);
        System.out.println(response.getStatus());
    }

    /**
     * GET method tests
     */


    @Test
    public void testGetRO() throws IOException {
        String entityID = "http://localhost:8080/sead-wf/entity/2021";
        WebResource webResource = resource();
        ClientResponse response = webResource.path("resource")
                .path("ro")
                .path(
                        URLEncoder.encode(
                                entityID
                        )
                )
                .get(ClientResponse.class);

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        System.out.print(writer.toString());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testPutJsonLDRO() throws IOException {
        WebResource webResource = resource();

        File file = new File(
                getClass().getResource("./Demo_Collection.json").getFile()
        );
        FileInputStream roFile = new FileInputStream(file);
        String colRoString = IOUtils.toString(roFile, "UTF-8");

        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();

        formDataMultiPart.field("ro", colRoString);

        ClientResponse response = webResource.path("resource")
                .path("putjsonldro")
                .type(MediaType.MULTIPART_FORM_DATA)
                .post(ClientResponse.class, formDataMultiPart);

        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetJsonLDRO() throws IOException {
        String entityID = "http://sead_J8WCHZ12";
        WebResource webResource = resource();
        ClientResponse response = webResource.path("resource")
                .path("jsonldro")
                .path(
                        URLEncoder.encode(
                                entityID
                        )
                )
                .get(ClientResponse.class);

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        System.out.println(writer.toString());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testUpdateState() throws IOException {
        String entityID = "http://sead_J8WCHZ12";
        WebResource webResource = resource();
        ClientResponse response = webResource.path("resource")
                .path("updateROState")
                .queryParam("entityId", URLEncoder.encode(entityID))
                .queryParam("state", "PublishedObject")
                .post(ClientResponse.class);

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        System.out.println(writer.toString());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetState() throws IOException {
        String entityID = "http://sead_J8WCHZ12";
        WebResource webResource = resource();
        ClientResponse response = webResource.path("resource")
                .path("getState")
                .path(
                        URLEncoder.encode(
                                entityID
                        )
                )
                .get(ClientResponse.class);

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        System.out.println(writer.toString());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetLineageGraph() throws IOException {
        WebResource webResource = resource();


        ClientResponse response = webResource.path("resource")
                .path("lineage")
                .path(
                        URLEncoder.encode(
                                "http://seadva-test.d2i.indiana.edu/sead-wf/entity/166381"
                        )
                )
                .get(ClientResponse.class);

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        System.out.print(writer.toString());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetCO() throws IOException {
       WebResource webResource = resource();


        ClientResponse response = webResource.path("resource")
                .path("listRO")
                .queryParam("type", "CurationObject")
                .get(ClientResponse.class);

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        System.out.print(writer.toString());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetAgentGraph() throws IOException {
        WebResource webResource = resource();


        ClientResponse response = webResource.path("resource")
                .path("agentGraph")
                .path(
                        URLEncoder.encode(
                                "testagent1"
                        )
                )
                .get(ClientResponse.class);

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        System.out.print(writer.toString());
        assertEquals(200, response.getStatus());
    }


}
