package edu.indiana.d2i.sead.matchmaker.custom.ruleset1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiFunction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.util.JSONPObject;

import edu.indiana.d2i.sead.matchmaker.core.MatchMakingList;
import edu.indiana.d2i.sead.matchmaker.core.POJOGenerator;

public class Ruleset1MatchMakingList extends MatchMakingList{

	public Ruleset1MatchMakingList(ArrayNode repositories) {
		super(repositories);
		// TODO Auto-generated constructor stub
	}
	public void init(){
		super.PRIMARY_KEY="repositoryName";
	}

}
