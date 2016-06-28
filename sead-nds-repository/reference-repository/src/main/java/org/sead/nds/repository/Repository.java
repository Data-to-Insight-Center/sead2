/*
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
 * @author myersjd@umich.edu
 * @author isuriara@indiana.edu
 */

package org.sead.nds.repository;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.ucsb.nceas.ezid.EZIDException;
import edu.ucsb.nceas.ezid.EZIDService;
import edu.ucsb.nceas.ezid.profile.DataCiteProfile;
import edu.ucsb.nceas.ezid.profile.DataCiteProfileResourceTypeValues;
import edu.ucsb.nceas.ezid.profile.InternalProfile;

public class Repository {

	static final Logger log = Logger.getLogger(Repository.class);
	private static String repoID = null;
	private static Properties props;
	private static String dataPath = null;
	private static boolean allowUpdates = false;

	static int numThreads;

	public Repository() {
	}

	// Important: SDA Agent also uses this Repository class and passes it's own
	// properties.
	// therefore don't load properties inside the init method.
	public static void init(Properties properties) {
		props = properties;
		repoID = props.getProperty("repo.ID");
		dataPath = props.getProperty("repo.datapath");

		if ((repoID == null) || (dataPath == null)) {
			log.error("Unable to find repoId and dataPath in proporties file");
		}
		allowUpdates = (props.getProperty("repo.allowupdates", "false"))
				.equalsIgnoreCase("true") ? true : false;
		numThreads = Runtime.getRuntime().availableProcessors();
		if (props.getProperty("repo.numthreads") != null) {
			numThreads = Integer.parseInt(props.getProperty("repo.numthreads"));
		}
		log.debug("Using " + numThreads + " threads");
	}

	public static int getNumThreads() {
		return numThreads;
	}

	public static Properties getProps() {
		return props;
	}

	public static boolean getAllowUpdates() {
		return allowUpdates;
	}

	public static String getRepoID() {
		return repoID;
	}

	static public String getDataPath() {
		return dataPath;
	}
	
	public static Properties loadProperties() {
		Properties props = new Properties();
		try {
			props.load(Repository.class
					.getResourceAsStream("repository.properties"));
			log.trace(props.toString());
		} catch (IOException e) {
			log.warn("Could not read repositories.properties file");
		}
		return props;
	}

	static String getLandingPageUri(String bagName) {
		return props.getProperty("repo.landing.base") + bagName;
	}



	public static String createDOIForRO(String bagID, C3PRPubRequestFacade RO)
			throws EZIDException {
		String target = Repository.getLandingPageUri(bagID);
		log.debug("DOI Landing Page: " + target);
		String existingID = null;
		if (RO.getPublicationRequest().getJSONObject("Preferences")
				.has("External Identifier")) {
			existingID = RO.getPublicationRequest()
					.getJSONObject("Preferences")
					.getString("External Identifier");
			if (existingID.startsWith("http://dx.doi.org/")) {
				existingID = "doi:"
						+ existingID.substring("http://dx.doi.org/".length());
			}
			if (existingID != null && !allowUpdates) {
				// FixMe - should we fail instead of going forward with a new
				// ID?
				log.warn("User requested an update to an existing ID, which is not allowed: Ingoring update request.");
			}
		}

		// After people expansion, we know that "Creator" is a JSONArray
		String creators = C3PRPubRequestFacade
				.getCreatorsString(C3PRPubRequestFacade
						.flattenPeople((JSONArray) RO.getOREMap()
								.getJSONObject("describes").get("Creator")));

		log.debug("Creators for DOI: " + creators);

		HashMap<String, String> metadata = new LinkedHashMap<String, String>();
		metadata.put(InternalProfile.TARGET.toString(), target);
		metadata.put(DataCiteProfile.TITLE.toString(), ((JSONObject) RO
				.getOREMap().get("describes")).getString("Title"));
		metadata.put(DataCiteProfile.CREATOR.toString(), creators);
		String rightsholderString = "SEAD (http://sead-data.net)";
		if (RO.getPublicationRequest().has("Rights Holder")) {
			rightsholderString = RO.getPublicationRequest().getString(
					"Rights Holder")
					+ ", " + rightsholderString;
		} else {
			log.warn("Request has no Rights Holder");
		}
		metadata.put(DataCiteProfile.PUBLISHER.toString(), rightsholderString);
		metadata.put(DataCiteProfile.PUBLICATION_YEAR.toString(),
				String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
		// An RO is generically a DataCite Collection, but based on their
		// definition and example,
		// using Dataset, even when the RO has data + documentation is
		// OK/preferred.
		metadata.put(DataCiteProfile.RESOURCE_TYPE.toString(),
				DataCiteProfileResourceTypeValues.DATASET.toString());

		EZIDService ezid = new EZIDService(props.getProperty("ezid.url"));

		boolean permanent = props.get("doi.default").equals("temporary") ? false
				: true;
		if (((JSONObject) RO.getPublicationRequest().get("Preferences"))
				.has("Purpose")) {
			String purpose = ((JSONObject) RO.getPublicationRequest().get(
					"Preferences")).getString("Purpose");
			if (purpose.equalsIgnoreCase("Testing-Only")) {
				permanent = false;
			}
		}

		ezid.login(props.getProperty("doi.user"), props.getProperty("doi.pwd"));
		String shoulder = (permanent) ? props.getProperty("doi.shoulder.prod")
				: props.getProperty("doi.shoulder.test");
		String doi = null;

		if ((existingID != null) && (existingID.contains(shoulder))
				&& allowUpdates) {

			// Enhancement: Retrieve metadata first and find current landing
			// page for this DOI - can then
			// decide what to do, e.g. to move/remove the old version, do
			// something other than a 404 for the old landigng URL, etc.

			log.debug("Updating metadata for: " + existingID);
			ezid.setMetadata(existingID, metadata);
			doi = existingID;
		} else if ((existingID == null) || !allowUpdates) {
			log.debug("Generating new ID with shoulder: " + shoulder);
			doi = ezid.mintIdentifier(shoulder, metadata);
		} else {
			log.warn("Request to update an existing DOI that does not match requested shoulder: "
					+ existingID + " : " + shoulder);
			throw new EZIDException(
					"Cannot update doi due to shoulder conflict");
		}
		// Should be true
		if (doi.startsWith("doi:")) {
			doi = doi.substring(4);
		}
		log.debug("Generated/Updated DOI: http://dx.doi.org/" + doi);

		return "http://dx.doi.org/" + doi;
	}

	public static String getID() {
		return repoID;
	}

	public static String getC3PRAddress() {
		return props.getProperty("c3pr.address");
	}
}
