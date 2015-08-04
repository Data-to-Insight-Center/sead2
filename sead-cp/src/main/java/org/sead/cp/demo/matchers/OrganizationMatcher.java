package org.sead.cp.demo.matchers;

import java.util.ArrayList;
import java.util.Iterator;

import org.bson.Document;
import org.bson.types.BasicBSONList;
import org.sead.cp.demo.Matcher;
import org.sead.cp.demo.RuleResult;

public class OrganizationMatcher implements Matcher {

	public RuleResult runRule(Document Content, String projectspace,  BasicBSONList affiliations, 
			Document Preferences, Document profile) {
		RuleResult result = new RuleResult();
		try {
			ArrayList<String> requiredAffiliations = (ArrayList<String>) profile.get("Affiliations");
			boolean affiliated = false;
			String requiredOrgString = null;
			for(String org: affiliations.toArray(new String[0])) {
				if((requiredAffiliations.contains(org))) {
					affiliated=true;
					requiredOrgString = org;
					break;
				}
			}
			if (!affiliated) {
				StringBuilder sBuilder = new StringBuilder();
				Iterator<String> iter = requiredAffiliations.iterator();
				sBuilder.append((String)iter.next());
				while(iter.hasNext()) {
					sBuilder.append(", " + (String)iter.next());
				}
				result.setResult(-1, "Collection does not have an affiliation with a required organization ("
						+ sBuilder.toString() + ").");
			} else {
				result.setResult(1, "Collection has required affiliation: " + requiredOrgString);
			}
		} catch (NullPointerException npe) {
			// Just return untriggered result
			System.out.println("Missing info in Organization Match rule"
					+ npe.getLocalizedMessage());
		} 
		return result;


	}

	public String getName() {
		return "Organization Match";
	}
	
	public Document getDescription() {
		return new Document("Rule Name", getName()).append("Repository Trigger", "\"Affiliations\": \"http://sead-data.net/terms/affiliations\" : JSON array of String organization names, at least one must match exactly");
	}

}
