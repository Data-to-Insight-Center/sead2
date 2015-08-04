package org.sead.cp.demo;


import org.bson.Document;
import org.bson.types.BasicBSONList;

public interface Matcher {
	public RuleResult runRule(Document Content, String projectspace, BasicBSONList affiliations, Document Preferences, Document profile);

	public String getName(); 
	
	public Document getDescription();

}
