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

import org.dataone.service.types.v1.*;
import org.jibx.runtime.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.StringWriter;

/**
 * Node details adevrtised
 */
@Path("/mn")
public class NodeDetails {
    @Context
    private UriInfo context;
    public NodeDetails() {
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public String getMNDetails() throws JiBXException, TransformerException {
        return SeadQueryService.marshal(getBaseMNDetails());
    }

    @GET
    @Path("/v1/ping")
    public Response ping() {
        return Response.status(Response.Status.ACCEPTED)
                .entity("OK").type(MediaType.APPLICATION_XML).build();
    }



    @GET
    @Path("/v1")
    @Produces(MediaType.APPLICATION_XML)
    public String getDupMNDetails() throws JiBXException, FileNotFoundException, TransformerException {
        return SeadQueryService.marshal(getBaseMNDetails());
    }

    @GET
    @Path("/v1/node")
    @Produces(MediaType.APPLICATION_XML)
    public String getDup2MNDetails() throws JiBXException, TransformerException {
        return SeadQueryService.marshal(getBaseMNDetails());
    }

    private Node getBaseMNDetails() {
        Node node = new Node();
        node.setReplicate(false);
       // node.setSynchronize(false);
        node.setType(NodeType.MN);
        node.setState(NodeState.UP);
        Subject subject = new Subject();
        subject.setValue("CN="+SeadQueryService.NODE_IDENTIFIER+", DC=dataone, DC=org");
        node.addSubject(subject);

        NodeReference nodeReference = new NodeReference();
        nodeReference.setValue(SeadQueryService.NODE_IDENTIFIER);
        node.setIdentifier(nodeReference);
        node.setName("SEAD Virtual Archive");
        node.setDescription("SEAD Virtual Archive is part of the SEAD DataNet project [http://sead-data.net/]. "+
                "\n SEAD Virtual Archive is a thin virtualization layer on top of multiple university Institutional Repositories focusing on " +
                "preservation of long-tail scientific data.");
        node.setBaseURL(SeadQueryService.BASE_URL);
        Services services = new Services();

        Service service1 = new Service();
        service1.setName("MNRead");
        service1.setVersion("v1");
        service1.setAvailable(true);

        Service service2 = new Service();
        service2.setName("MNCore");
        service2.setVersion("v1");
        service2.setAvailable(true);
        Service service3 = new Service();
        service3.setName("MNAuthorization");
        service3.setVersion("v1");
        service3.setAvailable(false);
        Service service4 = new Service();
        service4.setName("MNStorage");
        service4.setVersion("v1");
        service4.setAvailable(false);
        Service service5 = new Service();
        service5.setName("MNReplication");
        service5.setVersion("v1");
        service5.setAvailable(false);

        services.getServiceList().add(service1);
        services.getServiceList().add(service2);
        services.getServiceList().add(service3);
        services.getServiceList().add(service4);
        services.getServiceList().add(service5);

        node.setServices(services);

        Synchronization synchronization = new Synchronization();
        Schedule schedule = new Schedule();
        //schedule.setHour("23");
        schedule.setHour("*");
        schedule.setMday("*");
        //schedule.setMin("00");
        schedule.setMin("0/3");
        schedule.setMon("*");
        //schedule.setSec("00");
        schedule.setSec("45");
        schedule.setWday("?");
        schedule.setYear("*");
        synchronization.setSchedule(schedule);

        node.setSynchronize(true);
        node.setReplicate(false);
        node.setSynchronization(synchronization);

        Subject contactSubject = new Subject();
        contactSubject.setValue(SeadQueryService.SUBJECT);

        node.addContactSubject(contactSubject);

        return node;
    }

}
