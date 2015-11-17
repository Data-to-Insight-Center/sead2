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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.print.attribute.ResolutionSyntax;

import org.bson.Document;
import org.json.JSONObject;
import org.seadpdt.PeopleServices;

import com.mongodb.client.MongoCollection;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class GooglePlusProvider extends Provider {

	private String SEADKey;

	public GooglePlusProvider() {
		Properties props = new Properties();
		String path = "/googleplusprovider.properties"; //$NON-NLS-1$
		
		// load properties
		InputStream input = null;
		try {
			input = GooglePlusProvider.class.getResource(path).openStream();
			props.load(input);
			SEADKey = (String) props.get("google.api_key");
		} catch (IOException exc) {
			System.out.println("Could not load " + path + " : "
					+ exc.getLocalizedMessage());
		} finally {
			try {
				input.close();
			} catch (IOException exc) {
				System.out
						.println("Could not close googleplusprovider.properties."
								+ exc.getLocalizedMessage());
			}
		}
	}

	@Override
	public String getProviderName() {
		return "GooglePlus";
	}

	@Override
	public Document getExternalProfile(JSONObject person)
			throws RuntimeException {
		String id = person.getString(PeopleServices.identifier);
		Client client = Client.create();
		String rawID = id.substring("https://plus.google.com/".length());
		WebResource webResource = client
				.resource("https://www.googleapis.com/plus/v1/people/" + rawID
						+ "?key=" + SEADKey);

		ClientResponse response = webResource.accept("application/json").get(
				ClientResponse.class);
		if (response.getStatus() != 200) {
			throw new RuntimeException("" + response.getStatus());
		}
		String profile = response.getEntity(String.class);

		Document rawProfile = Document.parse(profile);
		Document personDocument = new Document();
		personDocument.put(PeopleServices.provider, getProviderName());
		personDocument.put("@id", rawProfile.getString("id"));
		Document nameDocument = (Document) rawProfile.get("name");
		personDocument.put("givenName", nameDocument.getString("givenName"));
		personDocument.put("familyName", nameDocument.getString("familyName"));
		ArrayList<Document> emails = ((ArrayList<Document>) rawProfile
				.get("emails"));
		String emailString = null;
		if ((emails != null) &&(!emails.isEmpty())) {
			for (Document email : emails) {
				if (emailString == null) {
					emailString = email.getString("value");
				} else if (email.getString("type").equals("work")) {
					emailString = email.getString("value");
				}
			}
		}
		if (emailString != null) {
			personDocument.put("email", emailString);
		}
		personDocument.put("PersonalProfileDocument", id + "/about");
		@SuppressWarnings("unchecked")
		ArrayList<Document> organizations = (ArrayList<Document>) rawProfile
				.get("organizations");
		StringBuffer affs = new StringBuffer();
		if ((organizations!=null) && (!organizations.isEmpty())) {
			for (Document org : organizations) {
				if (org.getString("endDate") == null) {
					if (affs.length() != 0) {
						affs.append(", ");
					}
					affs.append(org.getString("name"));
				}
			}
		}
		personDocument.append("affiliation", affs.toString());
		return personDocument;
	}

	// Claim any 21 digit number, if longer than 21 chars, make sure google.com
	// is included somewhere
	// Todo: Google also allows vanity names that start with a '+' - they do map
	// to a 21 number ID,
	// but dereferencing the name requires hitting the google api

	@Override
	public String getCanonicalId(String personID) {
		String canonical = null;
		if (personID.length() == 21 | personID.contains("google.com")) {
			Pattern p = Pattern.compile(".*(\\d{21}).*");
			Matcher matcher = p.matcher(personID);
			if (matcher.find()) {
				canonical = "https://plus.google.com/" + matcher.group(1);
			}
		}
		return canonical;
	}

}
