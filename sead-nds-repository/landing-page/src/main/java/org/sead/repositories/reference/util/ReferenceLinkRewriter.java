package org.sead.repositories.reference.util;

import org.apache.log4j.Logger;
import org.sead.nds.repository.util.LinkRewriter;

public class ReferenceLinkRewriter implements LinkRewriter {

	private static final Logger log = Logger
			.getLogger(ReferenceLinkRewriter.class);

	private String base = null;

	public ReferenceLinkRewriter(String baseURL) {
		base = baseURL;
	}

	public String rewriteDataLink(String sourceURL, String id, String ro_id,
			String relPath) {
		// /researchobjects/{id}/data/{relpath}
		// relPath includes initial / which should not be encoded... so add
		// that / to the '/data/' constant and skip first char of relPath
		String start = encode(ro_id) + "/data/";
		return base
				+ start
				+ encode(relPath.substring(relPath.indexOf(start)
						+ (start.length() + 1)));
	}

	public String rewriteOREMapLink(String sourceURL, String ro_id) {
		return base + encode(ro_id) + "/meta/oremap.jsonld.txt";
	}

	public String rewriteAggregationLink(String sourceURL, String ro_id) {
		return base + encode(ro_id) + "/meta/oremap.jsonld.txt#aggregation";
	}

	// Need to percent encode bad chars that may be in file path names. Java
	// doesn't appear to have a built in encode appropriate for path segments
	// (URLEncoder encodes form data and doesn't do the right thing). For now,
	// we'll catch the most common ones.
	// See http://notes.richdougherty.com/2013/07/url-path-segment-encoding.html
	// for other options
	static String encode(String s) {

		return s.replaceAll("%", "%25").replaceAll("/", "%2F")
				.replaceAll(" ", "%20").replaceAll("\\?", "%3F")
				.replaceAll("#", "%23").replaceAll("\\[", "%5B")
				.replaceAll("\\]", "%5D");
	}

	static String decode(String s)  {

		return s.replaceAll("%5D", "\\]").replaceAll("%5B", "\\[")
				.replaceAll("%23", "#").replaceAll("%3F", "\\?")
				.replaceAll("%20", " ").replaceAll("%2F", "/")
				.replaceAll("%25", "%");
	}
}
