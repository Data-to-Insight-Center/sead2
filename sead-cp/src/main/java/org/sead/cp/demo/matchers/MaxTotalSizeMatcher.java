package org.sead.cp.demo.matchers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.bson.Document;
import org.bson.types.BasicBSONList;
import org.sead.cp.demo.Matcher;
import org.sead.cp.demo.RuleResult;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class MaxTotalSizeMatcher implements Matcher {

	public RuleResult runRule(Document content, String projectspace, BasicBSONList affiliations, 
			Document preferences, Document profile) {
		RuleResult result = new RuleResult();
		Client client = Client.create();
		WebResource webResource;
		try {
			webResource = client.resource(projectspace
					+ "/resteasy/collections/"
					+ URLEncoder.encode(content.getString("Identifier"),
							"UTF-8") + "/stats");

			ClientResponse response = webResource.accept("application/json")
					.get(ClientResponse.class);

			if (response.getStatus() != 200) {
				throw new RuntimeException("" + response.getStatus());
			}

			Document statsDocument = Document.parse(response
					.getEntity(String.class));

			long max = Long.parseLong(statsDocument.getString("Total Size"));
			long repoMax = Long.parseLong(profile.getString("Total Size"));
			if (max > repoMax) {
				result.setResult(-1, "Total size exceeds maximum allowed ("
						+ repoMax + ").");
			} else {
				result.setResult(1, "Total size is acceptable (<=" + repoMax
						+ ").");
			}
		} catch (NullPointerException npe) {
			// Just return untriggered result
			System.out.println("Missing info in TotalSize rule"
					+ npe.getLocalizedMessage());
		} catch (NumberFormatException nfe) {
			// Just return untriggered result
			System.out.println("Missing info in MaxDatasetSize rule for repo: "
					+ profile.getString("orgidentifier") + " : "
					+ nfe.getLocalizedMessage());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;

	}

	public String getName() {
		return "Maximum Total Size";
	}
	
	public Document getDescription() {
		return new Document("Rule Name", getName()).append("Repository Trigger", "\"Total Size\": \"tag:tupeloproject.org,2006:/2.0/files/length\" : long size (Bytes) as String");
	}
}
