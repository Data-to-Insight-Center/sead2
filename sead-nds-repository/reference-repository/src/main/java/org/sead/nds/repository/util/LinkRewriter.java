package org.sead.nds.repository.util;

public interface LinkRewriter {

	abstract public String rewriteDataLink(String sourceURL, String id, String bagID, String relPath); 
	abstract public String rewriteOREMapLink(String sourceURL, String ro_id);
	abstract public String rewriteAggregationLink(String sourceURL, String ro_id);
}
