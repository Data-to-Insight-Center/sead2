/*
 *
 * Copyright 2015 University of Michigan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 *
 * @author myersjd@umich.edu
 */

package org.seadpdt.people;

import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.core.Response;

import org.bson.Document;
import org.json.JSONObject;
import org.seadpdt.PeopleServices;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class OrcidProvider extends Provider {

	@Override
	public Document getExternalProfile(JSONObject person) throws RuntimeException {
		String id = person.getString(PeopleServices.identifier);
		Document profile = null;
		if (id != null) {
			// id should be canonical already
			id = id.substring("http://orcid.org/".length());

			profile = generateProfile(id);
		}
		return profile;
	}

	// Retrieve external profile and create the standard internal form. Input is
	// id in canonical form
	private Document generateProfile(String id) {

		Document personDocument = new Document();
		personDocument.put(PeopleServices.provider, getProviderName());

		Document rawDocument = getRawProfile(id);
		personDocument.put("@id",
				getCanonicalId(((Document) ((Document) rawDocument
						.get("orcid-profile")).get("orcid-identifier"))
						.getString("path")));
		Document detailsDocument = (Document) ((Document) ((Document) rawDocument
				.get("orcid-profile")).get("orcid-bio"))
				.get("personal-details");
		personDocument.put("givenName",
				((Document) detailsDocument.get("given-names")).get("value"));
		personDocument.put("familyName",
				((Document) detailsDocument.get("family-name")).get("value"));
		ArrayList emails = ((ArrayList<?>) ((Document) ((Document) ((Document) rawDocument
				.get("orcid-profile")).get("orcid-bio")).get("contact-details"))
				.get("email"));
		if (!emails.isEmpty()) {
			personDocument
					.put("email", ((Document) emails.get(0)).get("value"));
		}
		personDocument.put("PersonalProfileDocument",
				((Document) ((Document) rawDocument.get("orcid-profile"))
						.get("orcid-identifier")).get("uri"));
		@SuppressWarnings("unchecked")
		ArrayList<Document> affiliationsList = (ArrayList<Document>) ((Document) ((Document) ((Document) rawDocument
				.get("orcid-profile")).get("orcid-activities"))
				.get("affiliations")).get("affiliation");
		StringBuffer affs = new StringBuffer();
		for (Document affiliationDocument : affiliationsList) {
			if (affiliationDocument.getString("type").equals("EMPLOYMENT")
					&& (affiliationDocument.get("end-date") == null)) {
				if (affs.length() != 0) {
					affs.append(", ");
				}
				affs.append(((Document) affiliationDocument.get("organization"))
						.getString("name"));

			}
			personDocument.append("affiliation", affs.toString());
		}

		return personDocument;

	};

	public static Document getRawProfile(String id) {
		Client client = Client.create();
		String rawID = id.substring("http://orcid.org/".length());
		WebResource webResource = client.resource("http://pub.orcid.org/v1.2/"
				+ rawID + "/orcid-profile");

		ClientResponse response = webResource.accept("application/orcid+json")
				.get(ClientResponse.class);

		if (response.getStatus() != 200) {
			throw new RuntimeException("" + response.getStatus());
		}

		return Document.parse(response.getEntity(String.class));
	}

	// Parse the string to get the internal ID that the profile would have been
	// stored under.
	// If the value is a plain string name, or a 1.5 style name:VIVO URL, return
	// null since we
	// have no profiles
	public String getCanonicalId(String personID) {
		// ORCID

		if (personID
				.matches("^http://orcid.org/\\d\\d\\d\\d-\\d\\d\\d\\d-\\d\\d\\d\\d-\\d\\d\\d[\\d|X]$")) {
			if (validCheckDigit(personID.substring(17))) {
				return personID;
			}

		} else if (personID
				.matches("^orcid.org/\\d\\d\\d\\d-\\d\\d\\d\\d-\\d\\d\\d\\d-\\d\\d\\d[\\d|X]$")) {
			if (validCheckDigit(personID.substring(10))) {
				return "http://" + personID;
			}
		} else if (personID
				.matches("^\\d\\d\\d\\d-\\d\\d\\d\\d-\\d\\d\\d\\d-\\d\\d\\d[\\d|X]$")) {
			if (validCheckDigit(personID)) {
				return "http://orcid.org/" + personID;
			}
		}
		return null; // Unrecognized/no id/profile in system

	}

	/**
	 * Generates check digit as per ISO 7064 11,2.
	 * 
	 */
	public static boolean validCheckDigit(String id) {
		String baseDigits = id.replaceAll("-", "");
		String checkDigit = baseDigits.substring(baseDigits.length() - 1);
		baseDigits = baseDigits.substring(0, baseDigits.length());

		int total = 0;
		for (int i = 0; i < baseDigits.length(); i++) {
			int digit = Character.getNumericValue(baseDigits.charAt(i));
			total = (total + digit) * 2;
		}
		int remainder = total % 11;
		int result = (12 - remainder) % 11;
		String resultString = result == 10 ? "X" : String.valueOf(result);
		return (checkDigit.equals(resultString));

	}

	@Override
	public String getProviderName() {
		return "ORCID";
	}

}
