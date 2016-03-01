/*
#
# Copyright 2015 The Trustees of Indiana University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------
#
# Project: Matchmaker Service
# File:  MatchMakingList.java
# Description:  Key matchmaking operations. 
#            The Drools rules adopt "when-then" logic. In Matchmaker, 
#            each rule invokes one or more of the following Java methods
#            in the "then" statement to update the candidate list. 
#            The logic behind this rule invocation process is that the 
#            initial candidate list is always a full list. By applying rules, 
#            the candidate list will be updated to a subset of the full 
#            candidate list. Therefore, the order of rules will have no impact
#            to the final result so that it ease the burden of rule 
#            creation/verification. New rules can be added independently, 
#            without looking back to the existing rules.
#                 restrict() : Restrict candidate list to a given list.
#                 notAllowed(): Remove selected candidates from the candidate list.
#                 preferred(): Tag "preferred" to a list of candidates.
#                 setWeight(): Set weight to a candidate.
#                 addWeight(): Add weight to a candidate.
#                 reduceWeight(): Reduce weight to a candidate.
# -----------------------------------------------------------------
# 
*/

package edu.indiana.d2i.sead.matchmaker.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class MatchMakingList {
	
	protected int PRIORITY_DEFAULT;
	protected int PRIORITY_PREFERRED;
	protected int WEIGHT_DEFAULT;
	protected String PRIMARY_KEY;
	private Logger log;
	
	//String MatchmakingSchema ="{\"priority\":\"Integer\", \"weight\":\"Integer\"}";
	
	private HashMap<String, HashMap<String, Integer>> candidateList;
	
	/*
	 * Initiate candidateList, add all repositories.
	 * */
	public MatchMakingList(ArrayNode repositories){
		log = Logger.getLogger(MatchMakingList.class);
		init();
		this.candidateList = new HashMap<String, HashMap<String, Integer>>();
		for (JsonNode repo : repositories ){
			HashMap<String, Integer> params = new HashMap<String, Integer>();
			params.put("priority", PRIORITY_DEFAULT);
			params.put("weight", WEIGHT_DEFAULT);
			this.candidateList.put(repo.path(PRIMARY_KEY).asText(),params);
			//System.out.println(repo.toString());
		}
	}
	
	public void init(){
		this.PRIMARY_KEY="name";
		this.PRIORITY_DEFAULT=0;
		this.PRIORITY_PREFERRED=1;
		this.WEIGHT_DEFAULT=0;
		
	}
	
	/*
	 * Some rules may restrict candidates to a smaller set, "restrictCandidates". 
	 * Therefore, get intersections of two sets: candidateList and restrictCandidates.
	 * */
	public void restricted(Set<String> restrictCandidates){
		Set<String> newCandidateKeys=this.candidateList.keySet();
		newCandidateKeys.retainAll(restrictCandidates);
	}
	
	/*
	 * Some rules may indicate a "not allowed" list. 
	 * Therefore, get the relative complement of notAllowedList in candidateList
	 * */
	public void notAllowed(Set<String> notAllowedList){
		Set<String> candidateKeySet=this.candidateList.keySet();
		Iterator iterator = candidateKeySet.iterator();
      	while (iterator.hasNext()){
      		if(notAllowedList.contains(iterator.next())){
      			iterator.remove();
      		}
      	}
		
	}
	
	/*
	 * Some rules may indicate preferred list. 
	 * */
	public void preferred(Set<String> PreferredList){
		for(String candidate : PreferredList){
			if(this.candidateList.containsKey(candidate)){
				HashMap<String, Integer> params= this.candidateList.get(candidate);
				params.replace("priority", PRIORITY_PREFERRED);
			}
		}
		
	}
	
	public void setWeight(String candidate, int weight){
		if (this.candidateList.containsKey(candidate)){
			//System.out.println(candidate+" "+weight);
			HashMap<String, Integer> params= this.candidateList.get(candidate);
			params.replace("weight", weight);
		}
	}
	
	public void addWeight(String candidate, int weight){
		if (this.candidateList.containsKey(candidate)){
			//System.out.println(candidate+" "+weight);
			HashMap<String, Integer> params= this.candidateList.get(candidate);
			params.replace("weight", params.get("weight").intValue() + weight);
		}
	}
	
	public void reduceWeight(String candidate, int weight){
		if (this.candidateList.containsKey(candidate)){
			//System.out.println(candidate+" "+weight);
			HashMap<String, Integer> params= this.candidateList.get(candidate);
			params.replace("weight", params.get("weight").intValue() - weight);
		}
	}
	
	public HashMap<String, HashMap<String, Integer>> getCandidateList(){
		return this.candidateList;
	}
	
	public void printCandidateList(){
		System.out.println(CandidateList());	
	}
	
	public String CandidateList(){
		ObjectMapper mapper = new ObjectMapper();
        String matchmaking = "";
		try {
			matchmaking = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this.candidateList);
			//System.out.println(matchmaking);
			log.info(matchmaking);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return matchmaking;
	}

	public static void main(String[] args) throws JsonProcessingException, ClassNotFoundException {
		// TODO Auto-generated method stub
		
		/*POJOGenerator reposGen = new POJOGenerator("edu.indiana.d2i.sead.matchmaker.pojo.Repository");
    	reposGen.fromPath("/Users/charmadu/repo/git/git2/sead2/standalone-mm/profile/repositories.json");
    	MatchMakingList mml=new MatchMakingList((ArrayNode)reposGen.getJsonTree());
        mml.addWeight("B",3);
		mml.printCandidateList();
		mml.reduceWeight("B",1);
		mml.printCandidateList();
		mml.setWeight("C",10);
		mml.printCandidateList();
		Set<String> notAllowedList= new HashSet<String>();
		notAllowedList.add("C");
		mml.notAllowed(notAllowedList);
		mml.printCandidateList();
		Set<String> restrictedList= new HashSet<String>();
		restrictedList.add("A");
		restrictedList.add("B");
		restrictedList.add("D");
		restrictedList.add("F");
		mml.restricted(restrictedList);
		mml.printCandidateList();
		Set<String> preferredList= new HashSet<String>();
		preferredList.add("A");
		preferredList.add("B");
		mml.preferred(preferredList);
		mml.printCandidateList();*/
	
	}

}
