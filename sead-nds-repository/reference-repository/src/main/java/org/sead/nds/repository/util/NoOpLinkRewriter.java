package org.sead.nds.repository.util;

public class NoOpLinkRewriter implements LinkRewriter {

	public String rewriteDataLink(String sourceURL, String id, String bagID, String relPath) {
		return sourceURL;
	}

	public String rewriteOREMapLink(String sourceURL, String ro_id) {
		return sourceURL;
	}

	public String rewriteAggregationLink(String sourceURL, String ro_id) {
		return sourceURL;
	}
}
