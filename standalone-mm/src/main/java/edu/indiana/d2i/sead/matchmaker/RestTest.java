package edu.indiana.d2i.sead.matchmaker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.jersey.api.client.ClientResponse;
import edu.indiana.d2i.sead.matchmaker.core.POJOGenerator;
import edu.indiana.d2i.sead.matchmaker.service.MatchmakerOperations;
import edu.indiana.d2i.sead.matchmaker.util.MatchmakerENV;
import edu.indiana.d2i.sead.matchmaker.util.PropertyReader;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.drools.compiler.compiler.DrlParser;
import org.drools.compiler.compiler.DroolsParserException;
import org.drools.compiler.lang.descr.AndDescr;
import org.drools.compiler.lang.descr.BaseDescr;
import org.drools.compiler.lang.descr.PackageDescr;
import org.drools.compiler.lang.descr.PatternDescr;
import org.drools.compiler.lang.descr.RuleDescr;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kie.api.KieServices;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

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
	
	@GET
    @Path("/rules")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRulesList() throws IOException, DroolsParserException, JsonProcessingException, ClassNotFoundException, JSONException, URISyntaxException {

        JSONObject root = new JSONObject();
        JSONArray rulesArray = new JSONArray();
        root.put("rules", rulesArray);

        ClassLoader classLoader = getClass().getClassLoader();
        File folder = new File(classLoader.getResource("rules").getFile());
        File[] listOfFiles = folder.listFiles();

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        KieServices ks = KieServices.Factory.get();
        KieContainer kContainer = ks.getKieClasspathContainer();
        KieSession kSession = kContainer.newKieSession("ksession-rules");
        kSession.fireAllRules();

        for (int m = 0; m < listOfFiles.length; m++) {
            if (listOfFiles[m].isFile()) {
                if (listOfFiles[m].getName().endsWith(".drl")) {
                    String ruleFile = "rules/" + listOfFiles[m].getName();
                    Resource resource = ResourceFactory.newClassPathResource(ruleFile, getClass());
                    kbuilder.add(resource, ResourceType.DRL);

                        DrlParser parser = new DrlParser();
                        PackageDescr packageDescr=parser.parse(resource);
                        List<RuleDescr> rules_val = packageDescr.getRules();
                        int val2 = packageDescr.getRules().size();

                            for (int i = 0; i < val2; i = i + 1) {
                                JSONObject obj = new JSONObject();
                                RuleDescr rule = rules_val.get( i );
                                obj.put("name", rule.getName());
                                AndDescr lhs = rule.getLhs();
                                List<BaseDescr> val4 = rule.getLhs().getDescrs();

                                JSONArray lhsArray = new JSONArray();
                                obj.put("lhs", lhsArray);
                                JSONArray rhsArray = new JSONArray();
                                obj.put("rhs", rhsArray);
                                rulesArray.put(obj);

                                for (int j = 0; j < val4.size(); j=j+1){
                                    JSONObject objj = new JSONObject();
                                    // Check jth time patterns
                                    PatternDescr first = (PatternDescr) lhs.getDescrs().get( j );
                                    String lhs_identifier = first.getIdentifier();
                                    String lhs_objtype = first.getObjectType();

                                    objj.put("id", lhs_identifier);
                                    objj.put("objType", lhs_objtype);
                                    lhsArray.put(objj);
                                }
                                String rhs = (String) (rules_val.get( i )).getConsequence();
                                List<String> rhsList = Arrays.asList(rhs.split(";"));

                                JSONObject obj3 = new JSONObject();
                                obj3.put("rhs_val", rhsList);
                                rhsArray.put(obj3);
                            }
                        }if ( kbuilder.hasErrors() ) {
                            System.out.println( kbuilder.getErrors() );
                            }
            }
        }
        System.out.println(root.toString());
        return Response.ok().entity(root.toString()).build();
    }
	
	public static void main( String args[]) throws Exception {
        RestTest resttest = new RestTest();
        resttest.getRulesList();
    }
}
