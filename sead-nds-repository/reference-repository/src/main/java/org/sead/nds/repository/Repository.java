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
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONObject;

import edu.ucsb.nceas.ezid.EZIDException;
import edu.ucsb.nceas.ezid.EZIDService;
import edu.ucsb.nceas.ezid.profile.DataCiteProfile;
import edu.ucsb.nceas.ezid.profile.InternalProfile;

public class Repository {

	private static final Logger log = Logger.getLogger(Repository.class);
	private static String repoID = null;
	private static Properties props;
	private static String dataPath = null;
	private static boolean allowUpdates = false;

	public Repository() {
	}

	public static void init(Properties properties) {
		props = properties;
		repoID = props.getProperty("repo.ID", "bob");
		dataPath = props.getProperty("repo.datapath", "./test2");
		allowUpdates = (props.getProperty("repo.allowupdates"))
				.equalsIgnoreCase("true") ? true : false;

	}

	private static Properties loadProperties() {
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

	public static void main(String[] args) {
		PropertyConfigurator.configure("./log4j.properties");
		init(loadProperties());

		if (args.length == 1) {

			BagGenerator bg;
			C3PRPubRequestFacade RO = new C3PRPubRequestFacade(args[0], props);
			bg = new BagGenerator(RO);
			// FixMe - use repo.ID from properties file (possibly in repo class
			if (bg.generateBag(args[0])) {
				RO.sendStatus(
						C3PRPubRequestFacade.SUCCESS_STAGE,
						RO.getOREMap().getJSONObject("describes")
								.getString("External Identifier"));
			} else {
				RO.sendStatus(
						C3PRPubRequestFacade.FAILURE_STAGE,
						"Processing of this request has failed and no further attempts to process this request will be made. Please contact the repository for further information.");
			}
		} else {
			System.out.println("Usage: <RO Identifier>");
		}
		System.exit(0);
	}

	static public String getLandingPage(String bagName) {
		return props.getProperty("repo.landing.base",
				"http://bobdiscountdatashack.com/howabout/") + bagName;
	}

	static public String getDataPath() {
		return dataPath;
	}

	public static String createDOIForRO(String bagID, C3PRPubRequestFacade RO)
			throws EZIDException {
		String target = Repository.getLandingPage(bagID);
		log.debug("DOI Landing Page: " + target);
		String existingID = null;
		if (RO.getPublicationRequest().getJSONObject("Preferences")
				.has("External Identifier")) {
			existingID = RO.getPublicationRequest()
					.getJSONObject("Preferences")
					.getString("External Identifier");
			if (existingID.startsWith("http://dx.doi.org/")) {
				existingID = existingID
						.substring("http://dx.doi.org/".length());
			} else if (existingID.startsWith("doi:")) {
				existingID = existingID.substring("doi:".length());
			}
			if (existingID != null && !allowUpdates) {
				// FixMe - should we fail instead of going forward with a new
				// ID?
				log.warn("User requested an update to an existing ID, which is not allowed: Ingoring update request.");
			}
		}

		String creators = RO.getCreatorsString(RO.normalizeValues(RO
				.getOREMap().getJSONObject("describes").get("Creator")));

		HashMap<String, String> metadata = new LinkedHashMap<String, String>();
		metadata.put(InternalProfile.TARGET.toString(), target);
		metadata.put(DataCiteProfile.TITLE.toString(), ((JSONObject) RO
				.getOREMap().get("describes")).getString("Title"));
		metadata.put(DataCiteProfile.CREATOR.toString(), creators);
		String rightsholderString = "SEAD (http://sead-data.net";
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
		existingID = "doi:" + existingID;
		if ((existingID != null) && (existingID.contains(shoulder))
				&& allowUpdates) {
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
}
