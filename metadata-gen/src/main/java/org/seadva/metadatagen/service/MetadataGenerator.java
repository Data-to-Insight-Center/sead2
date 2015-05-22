package org.seadva.metadatagen.service;

import org.seadva.metadatagen.OREMetadataGen;
import org.seadva.metadatagen.SIPMetadataGen;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;

/**
 * Root resource (exposed at "rest" path)
 */
@Path("rest")
public class MetadataGenerator {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Path("/getMetadata")
    @Produces(MediaType.APPLICATION_XML)
    public Response getIt(@QueryParam("entityId") String entityId,
                          @QueryParam("type") String metadataType) throws URISyntaxException {

        String errorMsg ="<error>\n" +
                "<description>EntityId and type(ORE/SIP/FGDC) are required query parameters. Please specify.</description>\n" +
                "<traceInformation>\n" +
                "method: metadata-gen.getMetadata \n" +
                "</traceInformation>\n" +
                "</error>";

        if(entityId == null || metadataType == null){
            return Response.status(Response.Status.BAD_REQUEST).entity(errorMsg).type(MediaType.APPLICATION_XML).build();
        }

        String response = "";

        if(metadataType.equalsIgnoreCase("ORE")) {
            OREMetadataGen oreMetadataGen = new OREMetadataGen();
            response = oreMetadataGen.generateMetadata(entityId);
        } else if(metadataType.equalsIgnoreCase("SIP")){
            OREMetadataGen oreMetadataGen = new OREMetadataGen();
            oreMetadataGen.generateMetadata(entityId);
            SIPMetadataGen sipMetadataGen = new SIPMetadataGen();
            response = sipMetadataGen.generateMetadata(entityId);
        }

        return Response.ok(response
        ).build();
    }
}
