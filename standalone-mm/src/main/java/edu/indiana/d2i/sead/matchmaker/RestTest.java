package edu.indiana.d2i.sead.matchmaker;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.jersey.api.client.ClientResponse;
import edu.indiana.d2i.sead.matchmaker.core.POJOGenerator;
import edu.indiana.d2i.sead.matchmaker.service.MatchmakerOperations;
import edu.indiana.d2i.sead.matchmaker.util.MatchmakerENV;
import edu.indiana.d2i.sead.matchmaker.util.PropertyReader;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by charmadu on 2/15/16.
 */

@Path("/rest")
public class RestTest {

    private static PropertyReader propertyReader = null;
    private static MatchmakerENV env = null;
    private static final Logger log = Logger.getLogger(RestTest.class);
    private MatchmakerOperations mmOperations;
    private POJOGenerator input;
    public static final String  PROCESSING_ERROR_STRING = "Processing Error";

    public RestTest() throws ClassNotFoundException {
        propertyReader = PropertyReader.getInstance("matchmaker.properties");
        PropertyConfigurator.configure(RestTest.class.getResourceAsStream(propertyReader.getProperty("log4j.properties.path")));

        if (log.isDebugEnabled()) log.debug("Matchmaker started");
        env = new MatchmakerENV(propertyReader);
        this.mmOperations = new MatchmakerOperations();
        input = new POJOGenerator(env.getMatchmakerInputSchemaClassName());
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRepositoryList() {


        return Response.status(200).entity("{\"test\" : \"test123\"}").build();

    }

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getMatchingRepositories(String roString) {
        this.input.fromString(roString);
        JsonNode requestMessageJsonNode=this.input.getJsonTree();
//String ResponseRoutingKey=requestMessageJsonNode.get("responseKey").asText();
        JsonNode request=requestMessageJsonNode.get("request");
        log.info("[Matchmaker server: Request] "+request);
        //log.info("[Matchmaker server: Message Response Routing Key] "+ResponseRoutingKey);

        //Perform Service Logic
        String result=null;
        try{
            result=this.mmOperations.exec(env, requestMessageJsonNode, null);
            log.info("[Matchmaker server: Async result] "+result);
            return Response.ok().entity(result).build();
        }catch(Exception e){
            result=PROCESSING_ERROR_STRING;
            log.info("[Matchmaker server: Processing Error] "+e.toString());
            return Response.status(ClientResponse.Status.INTERNAL_SERVER_ERROR).entity(result).build();
        }
    }
}
