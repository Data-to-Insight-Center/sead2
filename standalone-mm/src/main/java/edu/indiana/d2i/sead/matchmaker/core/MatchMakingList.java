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

import java.util.*;

public class MatchMakingList {
	
	protected int PRIORITY_DEFAULT;
	protected int PRIORITY_PREFERRED;
	protected int WEIGHT_DEFAULT;
	protected String PRIMARY_KEY;
	private Logger log;

    private static final String REPOSITORY_NAME = "repositoryName";
    private static final String REPOSITORY_ID = "orgidentifier";
    public static final String PER_RULE_SCORE = "Per Rule Scores";
    private static final String RULE_NAME = "Rule Name";
    public static final String RULE_SCORE = "Score";
    private static final String RULE_MESSAGE = "Message";

    public static final String ATTRIBUTE_TYPE = "Attribute Type";
    public static final String CRITICAL = "Critical";
    public static final String NONCRITICAL = "NonCritical";

	//String MatchmakingSchema ="{\"priority\":\"Integer\", \"weight\":\"Integer\"}";

    // map: "orgidentifier" --> [   "orgidentifier" -> "Org. Identifier",
    //                              "repositoryName" -> "Repository Name",
    //                              "Per Rule Scores" -> [{ Score -> 1,
    //                                                      Message -> Total size is acceptable (<= 1000000000),
    //                                                      Rule Name -> Maximum Total Size,
    //                                                      Attribute Type -> Critical} ],
    //                            ]
	private Map<String, HashMap<String, Object>> candidateList;
	
	/*
	 * Initiate candidateList, add all repositories.
	 * */
	public MatchMakingList(ArrayNode repositories){
		log = Logger.getLogger(MatchMakingList.class);
		init();
		this.candidateList = new HashMap<String, HashMap<String, Object>>();
		for (JsonNode repo : repositories ){
			HashMap<String, Object> params = new HashMap<String, Object>();
			//params.put("priority", PRIORITY_DEFAULT);
			//params.put("weight", WEIGHT_DEFAULT);
			//params.put("orgidentifier", repo.path(PRIMARY_KEY).asText());
			params.put(REPOSITORY_NAME, repo.path("repositoryName").asText());
			params.put(REPOSITORY_ID, repo.path(PRIMARY_KEY).asText());
			this.candidateList.put(repo.path(PRIMARY_KEY).asText(), params);
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
		//Set<String> newCandidateKeys=this.candidateList.keySet();
		//newCandidateKeys.retainAll(restrictCandidates);
	}
	
	/*
	 * Some rules may indicate a "not allowed" list. 
	 * Therefore, get the relative complement of notAllowedList in candidateList
	 * */
	public void notAllowed(Set<String> notAllowedList){
		/*Set<String> candidateKeySet=this.candidateList.keySet();
		Iterator iterator = candidateKeySet.iterator();
      	while (iterator.hasNext()){
      		if(notAllowedList.contains(iterator.next())){
      			iterator.remove();
      		}
      	}*/
		
	}
	
	/*
	 * Some rules may indicate preferred list. 
	 * */
	public void preferred(Set<String> PreferredList){
		/*for(String candidate : PreferredList){
			if(this.candidateList.containsKey(candidate)){
				HashMap<String, Integer> params= this.candidateList.get(candidate);
				params.replace("priority", PRIORITY_PREFERRED);
			}
		}*/
		
	}
	
	public void setWeight(String candidate, int weight){
		/*if (this.candidateList.containsKey(candidate)){
			//System.out.println(candidate+" "+weight);
			HashMap<String, Integer> params= this.candidateList.get(candidate);
			params.replace("weight", weight);
		}*/
	}
	
	public void addWeight(String candidate, int weight){
		/*if (this.candidateList.containsKey(candidate)){
			//System.out.println(candidate+" "+weight);
			HashMap<String, Integer> params= this.candidateList.get(candidate);
			params.replace("weight", params.get("weight").intValue() + weight);
		}*/
	}
	
	public void reduceWeight(String candidate, int weight){
		/*if (this.candidateList.containsKey(candidate)){
			//System.out.println(candidate+" "+weight);
			HashMap<String, Integer> params= this.candidateList.get(candidate);
			params.replace("weight", params.get("weight").intValue() - weight);
		}*/
	}

    public void ruleFired(String candidate, String rule, String message, String score) {
        log.info("RuleFired -- " + "Rule : " + rule + ", Repository : " + candidate + ", Message : " + message);
        if (this.candidateList.containsKey(candidate)){
            //System.out.println(candidate+" "+weight);
            HashMap<String, Object> repository= this.candidateList.get(candidate);
            HashMap<String, Object> ruleScore = new HashMap<String, Object>();
            ruleScore.put(RULE_NAME, rule);
            ruleScore.put(RULE_MESSAGE, message);
            ruleScore.put(RULE_SCORE, score);

            List<HashMap<String, Object>> ruleScoreList = new ArrayList<HashMap<String, Object>>();

            if(repository.containsKey(PER_RULE_SCORE)){
                ruleScoreList = (List<HashMap<String, Object>>)repository.get(PER_RULE_SCORE);
            } else {
                repository.put(PER_RULE_SCORE, ruleScoreList);
            }
            ruleScoreList.add(ruleScore);

        }
    }

    public void ruleFired(String candidate, String rule, String message, String score, String attributeType) {
        log.info("RuleFired -- " + "Rule : " + rule + ", Repository : " + candidate + ", Message : " + message + ", Attribute Type : " + attributeType);
        if (this.candidateList.containsKey(candidate)){
            //System.out.println(candidate+" "+weight);
            HashMap<String, Object> repository= this.candidateList.get(candidate);
            HashMap<String, Object> ruleScore = new HashMap<String, Object>();
            ruleScore.put(RULE_NAME, rule);
            ruleScore.put(RULE_MESSAGE, message);
            ruleScore.put(RULE_SCORE, score);
            ruleScore.put(ATTRIBUTE_TYPE, attributeType);

            List<HashMap<String, Object>> ruleScoreList = new ArrayList<HashMap<String, Object>>();

            if(repository.containsKey(PER_RULE_SCORE)){
                ruleScoreList = (List<HashMap<String, Object>>)repository.get(PER_RULE_SCORE);
            } else {
                repository.put(PER_RULE_SCORE, ruleScoreList);
            }
            ruleScoreList.add(ruleScore);

        }
    }

    public void addUnmatchedRules(Set<String> rules) {
        for (HashMap<String, Object> candidate : this.candidateList.values()) {

            List<HashMap<String, Object>> ruleScoreList = new ArrayList<HashMap<String, Object>>();
            if(candidate.containsKey(PER_RULE_SCORE)){
                ruleScoreList = (List<HashMap<String, Object>>)candidate.get(PER_RULE_SCORE);
            } else {
                candidate.put(PER_RULE_SCORE, ruleScoreList);
            }

            List currentRules = new ArrayList();
            for (Object rule : (ArrayList) candidate.get(PER_RULE_SCORE)) {
                currentRules.add(((HashMap) rule).get(RULE_NAME));
            }

            for (String availRule : rules) {
                if (!currentRules.contains(availRule)) {
                    HashMap<String, Object> ruleScore = new HashMap<String, Object>();
                    ruleScore.put(RULE_NAME, availRule);
                    ruleScore.put(RULE_MESSAGE, "Not Used");
                    ruleScore.put(RULE_SCORE, "0");
                    ruleScoreList.add(ruleScore);
                }
            }
        }

    }

    public List<HashMap<String, Object>> getCandidateList(){
		return new ArrayList<HashMap<String, Object>>(this.candidateList.values());
	}
	
	public void printCandidateList(){
		System.out.println(CandidateList());	
	}
	
	public String CandidateList(){
		ObjectMapper mapper = new ObjectMapper();
        String matchmaking = "";
		try {
			matchmaking = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(getCandidateList());
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
