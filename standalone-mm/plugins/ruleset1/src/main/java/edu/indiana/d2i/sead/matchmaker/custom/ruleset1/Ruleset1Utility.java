package edu.indiana.d2i.sead.matchmaker.custom.ruleset1;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.indiana.d2i.sead.matchmaker.core.BasicRuleUtility;
import edu.indiana.d2i.sead.matchmaker.core.POJOFactory;

public class Ruleset1Utility extends BasicRuleUtility {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode;
		try {
			rootNode = mapper.readTree(new File("C:\\Users\\yuanluo\\WorkZone\\workspace\\MatchMaker\\profile\\person.json"));
			POJOFactory.createClass("Person","edu.indiana.d2i.sead.matchmaker.pojo", rootNode, new File("C:\\Users\\yuanluo\\WorkZone\\workspace\\MatchMaker\\plugins\\ruleset1\\src\\main\\java"));
			
			rootNode = mapper.readTree(new File("C:\\Users\\yuanluo\\WorkZone\\workspace\\MatchMaker\\profile\\research_object.json"));
			POJOFactory.createClass("ResearchObject","edu.indiana.d2i.sead.matchmaker.pojo", rootNode, new File("C:\\Users\\yuanluo\\WorkZone\\workspace\\MatchMaker\\plugins\\ruleset1\\src\\main\\java"));
			
			rootNode = mapper.readTree(new File("C:\\Users\\yuanluo\\WorkZone\\workspace\\MatchMaker\\profile\\repositories.json"));
			POJOFactory.createClass("Repositories","edu.indiana.d2i.sead.matchmaker.pojo", rootNode, new File("C:\\Users\\yuanluo\\WorkZone\\workspace\\MatchMaker\\plugins\\ruleset1\\src\\main\\java"));
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
