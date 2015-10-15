package org.seadpdt.people;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;
import org.json.JSONObject;
import org.seadpdt.PeopleServices;

import com.mongodb.client.MongoCollection;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class LinkedInProvider extends Provider {

	private String SEADKey;

	public LinkedInProvider() {
		Properties props = new Properties();
		String path = "/linkedinprovider.properties"; //$NON-NLS-1$
		System.out.println("Loading LinkedIn Provider property file: " + path);

		// load properties
		InputStream input = null;
		try {
			input = LinkedInProvider.class.getResource(path).openStream();
			props.load(input);
			SEADKey = (String) props.get("linkedin.api_key");
		} catch (IOException exc) {
			System.out.println("Could not load linkedinprovider.properties:"
					+ exc.getLocalizedMessage());
		} finally {
			try {
				input.close();
			} catch (IOException exc) {
				System.out
						.println("Could not close linkedinprovider.properties."
								+ exc.getLocalizedMessage());
			}
		}
	}

	@Override
	public String getProviderName() {
		return "LinkedIn";
	}

	//LinkedIn will only allow retrieval when someone is Oauth2 logged in - will have to decide what info has to be passed in 
	//person Object to allow LinkedIn registration/updates
	@Override
	public Document getExternalProfile(JSONObject person)
			throws RuntimeException {
		throw(new RuntimeException("Not implemented, LinkedIn requires credentials to retrieve a profile"));
	}

	// Claim any String with linkedIn.com in it - just to stop other providers from grabbing it

	@Override
	public String getCanonicalId(String personID) {
		String canonical = null;
		if (personID.contains("linkedin.com")) {
				canonical = personID;
			}
		
		return canonical;
	}

}
