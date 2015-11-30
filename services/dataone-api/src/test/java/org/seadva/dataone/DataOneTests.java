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

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;
import org.dataconservancy.dcs.index.api.BatchIndexer;
import org.dataconservancy.dcs.index.api.IndexServiceException;
import org.dataconservancy.dcs.index.dcpsolr.ROBatchIndexer;
import org.dataconservancy.dcs.index.dcpsolr.SeadSolrService;
import org.dataconservancy.model.dcs.DcsFixity;
import org.dataconservancy.model.dcs.DcsResourceIdentifier;
import org.dataone.service.types.v1.*;
import org.jibx.runtime.JiBXException;
import org.junit.Before;
import org.junit.Test;
import org.seadva.model.SeadFile;
import org.seadva.model.pack.ResearchObject;
import org.xml.sax.SAXException;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * DataONE service tests
 */
public class DataOneTests extends JerseyTest {


    public DataOneTests() {
        super("org.seadva.dataone");
    }

    String dataOneId = "sead-bode-0bf5a0a7-831d-4e11-bf5b-27f9c6022c85";
    String checksum = "934f520e12ca5cbeb54c819088b0a76d3fa5d44a";

    @Before
    public void init() throws ClassNotFoundException, IndexServiceException, IOException, SAXException, ParserConfigurationException, IllegalAccessException, InstantiationException {
        SeadQueryService.class.newInstance();
        //set up Solr default
        install();
        index();
    }

    @Test
    public void testGetNodeDetails() throws IOException, JiBXException, IllegalAccessException, InstantiationException, ClassNotFoundException {


        WebResource webResource = resource();

        ClientResponse response = webResource.path("mn")
                .path("v1")
                .type(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);

        Node actual = (Node) SeadQueryService.unmarshal(response.getEntityInputStream(), Node.class);

        Node expected = (Node) SeadQueryService.unmarshal(getClass().getResourceAsStream("/nodeDetails.xml"), Node.class);
        assertEquals(200, response.getStatus());
        assertEquals(actual.getIdentifier().getValue(),expected.getIdentifier().getValue());
        assertEquals(actual.getName(),expected.getName());
    }

    @Test
    public void testGetObjectList() throws IOException, JiBXException, IllegalAccessException, InstantiationException, ClassNotFoundException {

        WebResource webResource = resource();

        ClientResponse response = webResource.path("mn")
                .path("v1/object")
                .type(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);

        ObjectList actual = (ObjectList) SeadQueryService.unmarshal(response.getEntityInputStream(), ObjectList.class);

        assertEquals(200, response.getStatus());
        assertEquals(1,actual.sizeObjectInfoList());
    }

    @Test
    public void testGetObject() throws IOException, JiBXException, IllegalAccessException, InstantiationException, ClassNotFoundException {


        WebResource webResource = resource();

        ClientResponse response = webResource.path("mn")
                .path("v1/object/"+dataOneId)
                .type(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);
        assertNotNull(response.getEntityInputStream());
    }

    @Test
    public void testGetSystemMetadata() throws IOException, JiBXException, IllegalAccessException, InstantiationException, ClassNotFoundException {


        WebResource webResource = resource();

        ClientResponse response = webResource.path("mn")
                .path("v1/meta/"+dataOneId)
                .type(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);

        SystemMetadata actual = (SystemMetadata) SeadQueryService.unmarshal(response.getEntityInputStream(), SystemMetadata.class);

        assertEquals(200, response.getStatus());
        assertNotNull(actual);
        assertEquals(actual.getChecksum().getValue(),checksum);
    }

    @Test
    public void testGetLogs() throws IOException, JiBXException, IllegalAccessException, InstantiationException, ClassNotFoundException {


        testGetSystemMetadata();

        WebResource webResource = resource();

        ClientResponse response = webResource.path("mn")
                .path("v1/log")
                .type(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);

        Log actual = (Log) SeadQueryService.unmarshal(response.getEntityInputStream(), Log.class);

        assertEquals(200, response.getStatus());
        assertEquals(actual.getLogEntryList().size(),1);
    }
    @Test
    public void testGetChecksum() throws IOException, JiBXException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        WebResource webResource = resource();

        ClientResponse response = webResource.path("mn")
                .path("v1/checksum/"+dataOneId)
                .type(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);

        Checksum actual = (Checksum) SeadQueryService.unmarshal(response.getEntityInputStream(), Checksum.class);

        assertEquals(200, response.getStatus());
        assertEquals(actual.getValue(), checksum);
    }

    void index() throws IOException, SAXException, ParserConfigurationException, IndexServiceException {
        ResearchObject researchObject = new ResearchObject();
        SeadFile file = new SeadFile();
        file.setId("id");
        file.setName("sample file");
        DcsFixity fixity = new DcsFixity();
        fixity.setAlgorithm("SHA-1");
        fixity.setValue(checksum);
        file.addFixity(fixity);
        DcsResourceIdentifier identifier = new DcsResourceIdentifier();
        identifier.setIdValue(dataOneId);
        identifier.setTypeId("dataone");
        file.addAlternateId(identifier);
        researchObject.addFile(file);
        BatchIndexer<ResearchObject> indexer = new ROBatchIndexer(SeadQueryService.solr, null);
        indexer.add(researchObject);
        indexer.close();
    }

    void install() throws IOException {
        SeadSolrService.createSolrInstall(new File(SeadQueryService.solrPath));
    }
}
