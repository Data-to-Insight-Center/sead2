package edu.indiana.d2i.sead.matchmaker.test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.node.ArrayNode;

import edu.indiana.d2i.sead.matchmaker.core.MatchMaker;
import edu.indiana.d2i.sead.matchmaker.core.MatchMakingList;
import edu.indiana.d2i.sead.matchmaker.core.POJOGenerator;
import edu.indiana.d2i.sead.matchmaker.custom.ruleset1.Ruleset1MatchMakingList;

public class TestCase1 {

	public static void main(String[] args) throws JsonParseException, JsonMappingException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		// TODO Auto-generated method stub
    	POJOGenerator reposGen = new POJOGenerator("edu.indiana.d2i.sead.matchmaker.pojo.Repository");
    	reposGen.fromPath("C:\\Users\\yuanluo\\WorkZone\\workspace\\MatchMaker\\profile\\repositories.json");
		POJOGenerator personGen = new POJOGenerator("edu.indiana.d2i.sead.matchmaker.pojo.Person");
		personGen.fromPath("C:\\Users\\yuanluo\\WorkZone\\workspace\\MatchMaker\\profile\\person.json");
		POJOGenerator researchObjectGen=new POJOGenerator("edu.indiana.d2i.sead.matchmaker.pojo.ResearchObject");
		researchObjectGen.fromPath("C:\\Users\\yuanluo\\WorkZone\\workspace\\MatchMaker\\profile\\research_object.json");
		
		Object[] repositories= (Object[]) reposGen.generate();
		Object person= (Object) personGen.generate();
		Object researchObject= (Object) researchObjectGen.generate();
/*		
		for(Object repo: repositories){
			Class<?>  classTypeRepository=Class.forName("edu.indiana.d2i.sead.matchmaker.pojo.Repository");
			Object maxSize=classTypeRepository.cast(repo).getClass().getMethod("getMaxSize", null).invoke(repo);
			Class<?>  classTypeMaxSize=Class.forName("edu.indiana.d2i.sead.matchmaker.pojo.MaxSize");
			Object value=classTypeMaxSize.cast(maxSize).getClass().getMethod("getValue", null).invoke(maxSize);
			System.out.println(value.toString());
		}
*/
		File[] rulefiles = new File[1];
		rulefiles[0]=new File("C:\\Users\\yuanluo\\WorkZone\\workspace\\MatchMaker\\plugins\\ruleset1\\target\\ruleset1-1.0.0.jar");
		MatchMakingList initList=(MatchMakingList) Class.forName("edu.indiana.d2i.sead.matchmaker.custom.ruleset1.Ruleset1MatchMakingList").getConstructor(ArrayNode.class).newInstance((ArrayNode)reposGen.getJsonTree());
		String[] classNames=new String[1];
		classNames[0]=new String("edu.indiana.d2i.sead.matchmaker.custom.ruleset1.Ruleset1Utility");
        new MatchMaker().basicGo(System.out, rulefiles, classNames, initList, repositories, person, researchObject);
        System.out.println("======================");
        System.out.println("Final Match:");
        initList.printCandidateList();
        System.out.println("======================");
    }



}
