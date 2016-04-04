package edu.indiana.d2i.sead.matchmaker.custom.ruleset1;

import com.fasterxml.jackson.databind.node.ArrayNode;
import edu.indiana.d2i.sead.matchmaker.core.MatchMakingList;

public class Ruleset1MatchMakingList extends MatchMakingList{

	public Ruleset1MatchMakingList(ArrayNode repositories) {
		super(repositories);
		// TODO Auto-generated constructor stub
	}
	public void init(){
		super.PRIMARY_KEY="orgidentifier";
	}

}
