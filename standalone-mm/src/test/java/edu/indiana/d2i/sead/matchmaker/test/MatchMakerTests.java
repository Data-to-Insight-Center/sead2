package edu.indiana.d2i.sead.matchmaker.test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import edu.indiana.d2i.sead.matchmaker.core.MatchMaker;
import edu.indiana.d2i.sead.matchmaker.core.MatchMakingList;
import edu.indiana.d2i.sead.matchmaker.core.POJOGenerator;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class MatchMakerTests extends TestCase {

    public MatchMakerTests(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(MatchMakerTests.class);
    }

    public void testMM() throws ClassNotFoundException, IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {

        // TODO Auto-generated method stub
        POJOGenerator reposGen = new POJOGenerator("edu.indiana.d2i.sead.matchmaker.pojo.Repository");
        reposGen.fromPath("../profile/repositories.json");
        POJOGenerator personGen = new POJOGenerator("edu.indiana.d2i.sead.matchmaker.pojo.Person");
        personGen.fromPath("../profile/person.json");
        POJOGenerator researchObjectGen = new POJOGenerator("edu.indiana.d2i.sead.matchmaker.pojo.ResearchObject");
        researchObjectGen.fromPath("../profile/research_object.json");

        Object[] repositories = (Object[]) reposGen.generate();
        Object person = (Object) personGen.generate();
        Object researchObject = (Object) researchObjectGen.generate();

        File[] rulefiles = new File[1];
        //rulefiles[0] = new File("../ruleset1/target/ruleset1-1.0.0.jar");
        MatchMakingList initList = (MatchMakingList) Class.forName("edu.indiana.d2i.sead.matchmaker.custom.ruleset1.Ruleset1MatchMakingList").getConstructor(ArrayNode.class).newInstance((ArrayNode) reposGen.getJsonTree());
        String[] classNames = new String[1];
        classNames[0] = new String("edu.indiana.d2i.sead.matchmaker.custom.ruleset1.Ruleset1Utility");
        new MatchMaker().basicGo(System.out, rulefiles, classNames, initList, repositories, person, researchObject);
        System.out.println("======================");
        System.out.println("Final Match:");
        initList.printCandidateList();
        System.out.println("======================");
        assertTrue(true);
    }


}
