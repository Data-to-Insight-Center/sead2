package edu.indiana.d2i.sead.matchmaker.drivers;

import edu.indiana.d2i.sead.matchmaker.util.MatchmakerENV;

public class Query extends MetaDriver {

	public Query(MatchmakerENV env, String message,String responseID){
		super(env, message,responseID);
	}
	
	public String exec() {
		System.out.println("responseID="+responseID);
		return "{\n\"responseID\":\""+responseID+"\",\n\"sucess\":"+true+",\n\"response\":"+candidateList.CandidateList() +"\n}"; 
	}

    public String getResults() {
        return candidateList.CandidateList();
    }

	
}
