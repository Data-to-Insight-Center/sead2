package org.sead.cp.demo.matchers;

import org.bson.Document;
import org.bson.types.BasicBSONList;
import org.sead.cp.demo.Matcher;
import org.sead.cp.demo.RuleResult;

public class MinimalMetadataMatcher implements Matcher {

	public RuleResult runRule(Document Content, String projectspace,
			BasicBSONList affiliations, Document Preferences, Document profile) {
		// TODO Auto-generated method stub
		return null;
	}

	public RuleResult runRule(Document Content, String projectspace,
			Document Preferences, Document profile) {

		return new RuleResult();
	}

	public String getName() {
		return "Minimal Metadata";
	}
	
	
	public Document getDescription() {
		return new Document("Rule Name", getName()).append("Repository Trigger", "\"Metadata Terms\": \"http://sead-data.net/terms/terms\" : JSON array of String predicates, Not yet implemented");
	}

}
