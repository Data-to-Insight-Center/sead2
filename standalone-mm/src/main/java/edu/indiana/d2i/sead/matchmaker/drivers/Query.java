package edu.indiana.d2i.sead.matchmaker.drivers;

import edu.indiana.d2i.sead.matchmaker.core.MatchMakingList;
import edu.indiana.d2i.sead.matchmaker.util.MatchmakerENV;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Query extends MetaDriver {

	public Query(MatchmakerENV env, String message,String responseID){
		super(env, message,responseID);
	}
	
	public String exec() {
		System.out.println("responseID="+responseID);
		return "{\n\"responseID\":\""+responseID+"\",\n\"sucess\":"+true+",\n\"response\":"+candidateList.CandidateList() +"\n}"; 
	}

    public String getResults() {

        JSONArray repos = null;
        try {
            repos = new JSONArray(candidateList.CandidateList());
            for (int i = 0; i < repos.length(); i++) {
                JSONObject repo = (JSONObject) repos.get(i);
                JSONArray scores = (JSONArray) repo.get(MatchMakingList.PER_RULE_SCORE);
                int total = 0;
                int totalMatched = 0;
                int criticalMatched = 0;
                int totalCritical = 0;
                int totalAttributes = 0;

                for (int j = 0; j < scores.length(); j++) {
                    JSONObject score = (JSONObject) scores.get(j);
                    int value = Integer.parseInt(score.get(MatchMakingList.RULE_SCORE).toString());
                    score.put(MatchMakingList.RULE_SCORE, value);
                    total += value;
                    if(value == 1)
                        totalMatched++;
                    if(score.has(MatchMakingList.ATTRIBUTE_TYPE)){
                        if(score.get(MatchMakingList.ATTRIBUTE_TYPE).equals(MatchMakingList.CRITICAL)) {
                            if (value == 1) {
                                criticalMatched++;
                            }
                            totalCritical++;
                        }
                    }
                    totalAttributes++;
                }

                repo.put("Total", total);
                repo.put("Result", "Percentage of total attributes matched : "
                                    + String.format("%.2f", totalMatched*1.0/totalAttributes*100) + "% | " +
                                    "Percentage of critical attributes matched : "
                                    + String.format("%.2f", criticalMatched*1.0/totalCritical*100) + "%" );
            }
        } catch (JSONException e) {
            return "\"Response\" : \"Error occurred while formatting the response.\"";
        }

        return repos.toString();
    }
}
