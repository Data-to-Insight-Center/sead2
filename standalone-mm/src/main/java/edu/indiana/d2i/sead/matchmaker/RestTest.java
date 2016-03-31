package edu.indiana.d2i.sead.matchmaker;

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
import org.drools.compiler.lang.descr.*;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public Response getRulesList() throws IOException, DroolsParserException, ClassNotFoundException, JSONException, URISyntaxException {

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

                                File file = new File(listOfFiles[m].getPath());
                                FileInputStream fis = new FileInputStream(file);
                                byte[] data = new byte[(int) file.length()];
                                fis.read(data);
                                fis.close();

                                String str = new String(data, "UTF-8");

                                String firstDelim = "rule " + '"' + rule.getName() + '"';
                                int p1 = str.indexOf(firstDelim);
                                String lastDelim = "end";
                                String desc = "";
                                int p2 = str.indexOf(lastDelim, p1);   // look after start delimiter
                                if (p1 >= 0 && p2 > p1) {
                                    String res = str.substring(p1,p2);
                                    Pattern pattern = Pattern.compile("// description:(.*?)//");
                                    Matcher matcher = pattern.matcher(res);
                                    while (matcher.find()) {
                                        String match_desc = matcher.group(1);
                                        desc += match_desc.trim();
                                    }
                                }

                                obj.put("name", rule.getName());
                                obj.put("desc", desc);
                                AndDescr lhs = rule.getLhs();
                                List<BaseDescr> val4 = lhs.getDescrs();

                                JSONArray lhsArray = new JSONArray();
                                obj.put("lhs", lhsArray);
                                JSONArray rhsArray = new JSONArray();
                                obj.put("rhs", rhsArray);
                                rulesArray.put(obj);

                                getLHS(val4, lhsArray);
                                String rhs = (String) (rules_val.get( i )).getConsequence();
                                List<String> rhsList = Arrays.asList(rhs.split(";"));

                                List<String> new_rhs_list = new ArrayList<String>();
                                for (int f=0; f < rhsList.size()-1; f++){
                                    new_rhs_list.add(f, rhsList.get(f) + ";");
                                }

                                JSONObject obj3 = new JSONObject();
                                obj3.put("rhs_val", new_rhs_list);
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

    public static void getLHS(List and_array, JSONArray lhs_array) throws JSONException {

        for (int j = 0; j < and_array.size(); j=j+1){
            JSONObject objj = new JSONObject();
            // Check jth time patterns
            Object arrayObject = and_array.get(j);
            if(arrayObject instanceof PatternDescr) {
                String lhs_const_val="";
                PatternDescr first = (PatternDescr) and_array.get( j );
                List<? extends BaseDescr> lhs_constraint = first.getConstraint().getDescrs();
                if (lhs_constraint.size() > 0) {
                    for (int q = 0; q < lhs_constraint.size(); q++) {
                        lhs_const_val += lhs_constraint.get(q).getText();
                    }
                }
                String lhs_identifier = first.getIdentifier();
                String lhs_objtype = first.getObjectType();

                objj.put("id", lhs_identifier);
                objj.put("objType", lhs_objtype +"("+lhs_const_val+")");
                if(lhs_identifier == null){
                    objj.put("lhsFull", lhs_objtype +"("+lhs_const_val+")");
                }else{
                    objj.put("lhsFull", lhs_identifier + ":" + lhs_objtype +"("+lhs_const_val+")");
                }
                lhs_array.put(objj);

            } else if (arrayObject instanceof OrDescr) {
                OrDescr first = (OrDescr) and_array.get( j );
                List<? extends BaseDescr> lhs_constraint = first.getDescrs();

                String lhs_identifier = "";
                String new_lhs_obj = "";
                if (lhs_constraint.size() > 0) {

                    for (int q = 0; q < lhs_constraint.size(); q++) {
                        PatternDescr col = (PatternDescr) first.getDescrs().get(q);

                        lhs_identifier = col.getIdentifier();
                        String lhs_objtype = col.getObjectType();
                        AndDescr lhs_const = (AndDescr) col.getConstraint();

                        String lhs_const_or_val = lhs_const.getDescrs().get(0).getText();
                        new_lhs_obj += lhs_objtype +"("+lhs_const_or_val+") or ";

                    }

                }
                if (lhs_identifier == null){
                    lhs_identifier="";
                }
                String last_new_lhs_obj =new_lhs_obj.substring(0, new_lhs_obj.lastIndexOf(" ")-2);
                objj.put("id", lhs_identifier);
                objj.put("objType", last_new_lhs_obj);
                if(lhs_identifier == null || lhs_identifier==""){
                    objj.put("lhsFull", last_new_lhs_obj);
                }else{
                    objj.put("lhsFull", lhs_identifier + ":" + last_new_lhs_obj);
                }
                lhs_array.put(objj);

            }
        }
    }

    @POST
    @Path("/rules")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String AddNewRule(final String input) throws JSONException, IOException {
        JSONObject req = new JSONObject(input);
        JSONArray rules = req.getJSONArray("rules");
        for (int v = rules.length()-1; v < rules.length(); ++v) {
            JSONObject rule = rules.getJSONObject(v);
            String name = rule.getString("name").trim();
            String desc = rule.getString("desc").trim();
            JSONArray lhs = rule.getJSONArray("lhs");
            JSONArray rhs = rule.getJSONArray("rhs");

            String new_rule = "\n\nrule " + '"' + name + '"' + "\n" + "// description: " + desc + " //\n" + "    when\n";
                for (int i = 0; i < lhs.length(); ++i) {
                    JSONObject lh = lhs.getJSONObject(i);
                    String lhsFull = lh.getString("lhsFull");
                    new_rule += "    " + "    " + lhsFull + "\n";
                }
            new_rule += "    then\n";

            for (int u = 0; u < rhs.length(); ++u) {
                JSONObject rh = rhs.getJSONObject(u);
                String rhs_val = rh.getString("rhs_val").trim();
                List<String> rhsList = Arrays.asList(rhs_val.split(";\n|;"));
                for(int o=0; o < rhsList.size(); ++o){
                    new_rule += "    " + "    " + rhsList.get(o) + ";\n";
            }}

        new_rule +=    "end\n";

        ClassLoader classLoader = getClass().getClassLoader();
        File folder = new File(classLoader.getResource("rules").getFile());
        File[] listOfFiles = folder.listFiles();

            for (int m = 0; m < listOfFiles.length; m++) {
                if (listOfFiles[m].isFile()) {
                    if (listOfFiles[m].getName().endsWith(".drl") && listOfFiles[m].getName().contentEquals("ruleset1.drl")) {
                        //open a bufferedReader to file
                        BufferedReader reader = new BufferedReader(new FileReader(listOfFiles[m].getPath()));
                        File file = new File(listOfFiles[m].getPath());
                        FileInputStream fis = new FileInputStream(file);
                        byte[] data = new byte[(int) file.length()];
                        fis.read(data);
                        fis.close();

                        String str = new String(data, "UTF-8");
                        BufferedWriter out = new BufferedWriter(new FileWriter(listOfFiles[m].getPath()));

                        if (str != null) {
                            out.write(str);
                        }
                        out.write(new_rule);
                        out.close();

                        File dest = new File("/Users/kunarath/Projects/sead2/standalone-mm/src/main/resources/rules/ruleset1.drl");

                        InputStream inStream = null;
                        OutputStream outStream = null;

                        inStream = new FileInputStream(file);
                        outStream = new FileOutputStream(dest);

                        byte[] buffer = new byte[1024];
                        int length;
                        //copy the file content in bytes
                        while ((length = inStream.read(buffer)) > 0){
                            outStream.write(buffer, 0, length);
                        }

                        inStream.close();
                        outStream.close();

                        KieContainer kContainer = KieServices.Factory.get().getKieClasspathContainer();
                        KieSession kSession = kContainer.newKieSession("ksession-rules");
                        kSession.fireAllRules();
                    }

                }
            }
        }
        return input;
}

    @DELETE
    @Path("/rules/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String DeleteRule(@PathParam("name") final String del_rule_name) throws JSONException, IOException {
        System.out.println(del_rule_name);

        ClassLoader classLoader = getClass().getClassLoader();
        File folder = new File(classLoader.getResource("rules").getFile());
        File[] listOfFiles = folder.listFiles();

        for (int m = 0; m < listOfFiles.length; m++) {
            if (listOfFiles[m].isFile())
                if (listOfFiles[m].getName().endsWith(".drl") && listOfFiles[m].getName().contentEquals("ruleset1.drl")) {
                    File file = new File(listOfFiles[m].getPath());
                    FileInputStream fis = new FileInputStream(file);
                    byte[] data = new byte[(int) file.length()];
                    fis.read(data);
                    fis.close();

                    String str = new String(data, "UTF-8");

                    if (str.contains(del_rule_name)){

                        String firstDelim = "rule " + '"' + del_rule_name + '"';
                        int p1 = str.indexOf(firstDelim);
                        String lastDelim = "end";
                        int p2 = str.indexOf(lastDelim, p1);   // look after start delimiter
                        String replacement = " ";
                        if (p1 >= 0 && p2 > p1) {
                            String res = str.substring(0, p1+firstDelim.length())
                                    + replacement
                                    + str.substring(p2);
                            String content = res.replace((firstDelim + " " + lastDelim), "").trim();
                            System.out.println(content);

                        BufferedWriter out = new BufferedWriter(new FileWriter(listOfFiles[m].getPath()));

                        out.write(content);
                        out.close();

                        File dest = new File("/Users/kunarath/Projects/sead2/standalone-mm/src/main/resources/rules/ruleset1.drl");
                        InputStream inStream = null;
                        OutputStream outStream = null;

                        inStream = new FileInputStream(file);
                        outStream = new FileOutputStream(dest);

                        byte[] buffer = new byte[1024];
                        int length;
                        //copy the file content in bytes
                        while ((length = inStream.read(buffer)) > 0){
                            outStream.write(buffer, 0, length);
                        }

                        inStream.close();
                        outStream.close();
                        }
                    }
                }
        }

        return del_rule_name;
    }

    public static void main( String args[]) throws Exception {
        RestTest resttest = new RestTest();
        resttest.getRulesList();
    }
}
