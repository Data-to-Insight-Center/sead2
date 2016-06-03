package org.sead.nds.repository.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import javax.ws.rs.Encoded;

import org.apache.log4j.Logger;

public class ReferenceLinkRewriter implements LinkRewriter {

	private static final Logger log = Logger
			.getLogger(ReferenceLinkRewriter.class);

	private String base = null;

	public ReferenceLinkRewriter(String baseURL) {
		base = baseURL;
	}

	public String rewriteDataLink(String sourceURL, String id, String ro_id,
			String relPath) {
		try {
			// /researchobjects/{id}/data/{relpath}
			// relPath includes initial / which should not be encoded... so add
			// that / to the '/data/' constant and skip first char of relPath
			String start = encode(ro_id)+ "/data/"; 
			return base + start
					+ encode(relPath.substring(relPath.indexOf(start)+ (start.length()+1)));
		} catch (URISyntaxException e) {
			log.error(e.getLocalizedMessage());
			return (sourceURL);
		}
	}

	public String rewriteOREMapLink(String sourceURL, String ro_id) {
		try {
			return base + encode(ro_id)
					+ "/meta/oremap.jsonld.txt";
		} catch (URISyntaxException e) {
			log.error(e.getLocalizedMessage());
			return (sourceURL);
		}
	}

	public String rewriteAggregationLink(String sourceURL, String ro_id) {
		try {
			return base + encode(ro_id)
					+ "/meta/oremap.jsonld.txt#aggregation";
		} catch (URISyntaxException e) {
			log.error(e.getLocalizedMessage());
			return (sourceURL);
		}
	}
	
	private String encode(String s) throws URISyntaxException {
		return s.replaceAll("/",  "%2F");
	}
}
