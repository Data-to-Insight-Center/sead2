/*
 *
 * Copyright 2015 The Trustees of Indiana University, 2015 University of Michigan
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
 * @author myersjd@umich.edu
 * 
 * 
 * Ezid ID Management service - now using the https://github.com/NCEAS/ezid (DataOne) ezid library,
 *  with an apache http client update, rather than exec'ing a script.
 *  
 * derived from IU version - implements legacy interface, along with a minor update that provides 
 * a command line client (as before) and service/method interfaces that don't require use of the
 * Datacite terms. Update makes it possible to add more (arbitrary) metadata. 
 * New code may want to use the DataOne library directly.  
 */

package org.seadva.services;

import com.sun.jersey.api.client.ClientResponse;

import edu.ucsb.nceas.ezid.EZIDException;
import edu.ucsb.nceas.ezid.EZIDService;
import edu.ucsb.nceas.ezid.profile.DataCiteProfile;
import edu.ucsb.nceas.ezid.profile.DataCiteProfileResourceTypeValues;
import edu.ucsb.nceas.ezid.profile.ErcMissingValueCode;
import edu.ucsb.nceas.ezid.profile.InternalProfile;
import edu.ucsb.nceas.ezid.profile.InternalProfileValues;

import org.omg.CORBA.PRIVATE_MEMBER;
import org.seadva.services.util.Constants;
import org.json.*;

import java.io.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * EzidService is a helper class that enables to create and update DOIs using
 * the EZID service.
 *
 */

@Path("/doi")
public class EzidService {

	private boolean permanentDOI;

	public EzidService() {

		permanentDOI = false;
	}

	public static void main(String[] args) {

		EZIDService ezid = new EZIDService(Constants.ezid_url);
		Console c = System.console();
		if (c == null) {
			System.err.println("No console.");
			System.exit(1);
		}

		try {
			String u = c.readLine("Username : ");
			char[] p = c.readPassword("Password:", null);

			ezid.login(u, new String(p));

			String login = c
					.readLine("EZID DOI Mode - Create(C) or Update(U) : ");

			if (login.equalsIgnoreCase("U") || login.equalsIgnoreCase("update")) {

				String doi = c.readLine("DOI(ex: 10.5072/FK2S46KQ67) : ");
				if (doi == null || doi.equals("")) {
					System.out.println("Error : Input DOI should not be empty");
					return;
				}

				String target = c.readLine("New Target : ");
				if (target == null || target.equals("")) {
					System.out
							.println("Error : Input target should not be empty");
					return;
				}
				HashMap<String, String> metadataMap = new HashMap<String, String>();
				metadataMap.put("_target", target);
				try {
					ezid.setMetadata(doi, metadataMap);
				} catch (EZIDException e) {
					System.out.println("Error Updating DOI");
					return;
				}
				System.out
						.println("DOI Updated Successfully : http://dx.doi.org/"
								+ doi);
				System.out.println("DOI : " + Constants.ezid_url + "id/" + doi);

			} else if (login.equalsIgnoreCase("C")
					|| login.equalsIgnoreCase("create")) {
				HashMap<String, String> metadataMap = new HashMap<String, String>();

				String target = c.readLine("Target : ");
				if (target == null || target.equals("")) {
					System.out
							.println("Error : Input target should not be empty");
					return;
				}
				metadataMap.put("_target", target);

				String title = c.readLine(DataCiteProfile.TITLE.toString()
						+ " : ");
				setEntry(metadataMap, DataCiteProfile.TITLE.toString(), title);

				String creator = c.readLine(DataCiteProfile.CREATOR.toString()
						+ " : ");
				setEntry(metadataMap, DataCiteProfile.CREATOR.toString(),
						creator);

				String publisher = c.readLine(DataCiteProfile.PUBLISHER
						.toString() + " : ");
				setEntry(metadataMap, DataCiteProfile.PUBLISHER.toString(),
						publisher);

				String year = c.readLine(DataCiteProfile.PUBLICATION_YEAR
						.toString() + " : ");
				if ((year == null) || (year.length() == 0)) {
					year = String.valueOf(Calendar.getInstance().get(
							Calendar.YEAR));
				}
				metadataMap.put(DataCiteProfile.PUBLICATION_YEAR.toString(),
						year);

				// Provide a type
				metadataMap
						.put(DataCiteProfile.RESOURCE_TYPE.toString(),
								DataCiteProfileResourceTypeValues.COLLECTION
										.toString());

				String doi = null;
				try {
					doi = ezid.mintIdentifier(Constants.doi_shoulder_test,
							metadataMap);
				} catch (EZIDException e) {
					System.out.println("Error Creating DOI");
					System.out.println("Metadata was: "
							+ metadataMap.toString());
					return;
				}
				System.out
						.println("DOI Created Successfully : http://dx.doi.org/"
								+ doi);
				System.out.println("DOI : " + Constants.ezid_url + "id/" + doi);
				System.out.println("Retrieved Metadata for " + doi);
				Map<String, String> map = ezid.getMetadata(doi);
				for (Entry<String, String> e : map.entrySet()) {
					System.out.println(e.getKey() + " : " + e.getValue());
				}

			} else {
				System.out.println("Invalid Mode");
			}
		} catch (EZIDException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Create a new DOI
	 * 
	 * Target is used to set the landing page
	 * 
	 * @param target
	 *            Target URL, ex: http://dummyUrl - required These are required
	 *            fields in the datacite profile
	 * @param title
	 * @param creator
	 *            creator or creators separated by ';'
	 * @param publisher
	 *            publisher or publishers separated by ';'
	 * @param pubyear
	 *            null = set to current year Additional key/value pairs can be
	 *            sent and will be returned
	 * @param other
	 * 
	 * @return DOI URL, ex: http://dx.doi.org/10.5072/FK2S46KQ67
	 * @throws EZIDException
	 */
	public String createDOIForRO(String target, String title, String creator,
			String publisher, String pubyear, Map<String, String> other,
			boolean requestPermanent) throws EZIDException {

		HashMap<String, String> metadata = new HashMap<String, String>();
		// An RO is generically a datacite collection - allow default to be
		// overwritten by other
		metadata.put(DataCiteProfile.RESOURCE_TYPE.toString(),
				DataCiteProfileResourceTypeValues.COLLECTION.toString());

		if (other != null) {
			// FixMe - test that datacite terms aren't used? For now, they will
			// be overwritten if they exist
			metadata.putAll(other);
		}
		// API will default to an internal landing page at the provider
		if ((target == null) || (target.length() == 0)) {

			metadata.put(InternalProfile.TARGET.toString(), target);
		}
		// Set entries with a default of ErcMissingValueCode.UNAVAILABLE if
		// null/o length
		setEntry(metadata, DataCiteProfile.TITLE.toString(), title);
		setEntry(metadata, DataCiteProfile.CREATOR.toString(), creator);
		setEntry(metadata, DataCiteProfile.PUBLISHER.toString(), publisher);
		String realyear = String.valueOf(Calendar.getInstance().get(
				Calendar.YEAR));
		if ((pubyear != null) && (pubyear.length() != 0)) {
			realyear = pubyear;
		}
		metadata.put(DataCiteProfile.PUBLICATION_YEAR.toString(), realyear);

		EZIDService ezid = new EZIDService(Constants.ezid_url);

		ezid.login(Constants.doi_username, Constants.doi_password);
		String shoulder = (requestPermanent) ? Constants.doi_shoulder_prod
				: Constants.doi_shoulder_test;
		String doi = ezid.mintIdentifier(shoulder, metadata);
		return "http://dx.doi.org/" + doi;
	}

	private static void setEntry(Map<String, String> map, String term,
			JSONObject metadata_object, String key) {
		String value = null;
		if (metadata_object.has(key) && !metadata_object.isNull(key)) {
			try {
				value = metadata_object.getString(key);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		setEntry(map, term, value);
	}

	private static void setEntry(Map<String, String> map, String term,
			String value) {
		if (value == null || value.length() == 0) {
			map.put(term, ErcMissingValueCode.UNAVAILABLE.toString());
		} else {
			map.put(term, value);
		}

	}

	/**
	 * Create a new DOI using EZID service
	 * 
	 * @Deprecated - use createDOI(target, title, creator, publisher, pubdate,
	 *             othermetadata) which uses the real/final key values rather
	 *             than being translated within the service
	 *
	 * @param metadata_json
	 *            Metadata in JSON format, ex : {title : test_title, creator :
	 *            test_creator, pubDate : test_pubDate}
	 * @param target
	 *            Target URL, ex: http://dummyUrl
	 * @return DOI URL, ex: http://dx.doi.org/10.5072/FK2S46KQ67
	 */
	@Deprecated
	public String createDOI(String metadata_json, String target) {

		HashMap<String, String> metadata = new HashMap<String, String>();
		metadata.put(InternalProfile.TARGET.toString(), target);
		String doi = null;
		try {
			JSONObject metadata_object = null;

			metadata_object = new JSONObject(metadata_json);
			metadata.putAll(getTranslatedTerms(metadata_object));
			// An RO is generically a datacite collection
			metadata.put(DataCiteProfile.RESOURCE_TYPE.toString(),
					DataCiteProfileResourceTypeValues.COLLECTION.toString());

			EZIDService ezid = new EZIDService(Constants.ezid_url);

			ezid.login(Constants.doi_username, Constants.doi_password);

			String shoulder = (isPermanentDOI()) ? Constants.doi_shoulder_prod
					: Constants.doi_shoulder_test;
			doi = ezid.mintIdentifier(shoulder, metadata);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EZIDException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return "http://dx.doi.org/" + doi;
	}

	private Map<? extends String, ? extends String> getTranslatedTerms(
			JSONObject metadata_object) throws JSONException {
		HashMap<String, String> metadata = new HashMap<String, String>();
		setEntry(metadata, DataCiteProfile.TITLE.toString(), metadata_object,
				"title");
		setEntry(metadata, DataCiteProfile.CREATOR.toString(), metadata_object,
				"creator");
		setEntry(metadata, DataCiteProfile.PUBLISHER.toString(),
				metadata_object, "publisher");
		String realyear = String.valueOf(Calendar.getInstance().get(
				Calendar.YEAR));
		if (metadata_object.has("pubDate")) {
			String pubyear = metadata_object.getString("pubDate");
			if ((pubyear != null) && (pubyear.length() != 0)) {
				realyear = pubyear;
			}
			metadata.put(DataCiteProfile.PUBLICATION_YEAR.toString(), realyear);
		}
		return metadata;

	}

	/**
	 * Update a new target to existing DOI
	 *
	 * @param doi
	 *            DOI to be updated, ex: 10.5072/FK2S46KQ67)
	 * @param target
	 *            New target URL, ex: http://dummyUrl
	 * @return Updated URL of DOI, ex: http://dx.doi.org/10.5072/FK2S46KQ67
	 * @throws EZIDException
	 */
	public String updateDOI(String doi, String target) {
		EZIDService ezid = new EZIDService(Constants.ezid_url);
		try {
			ezid.login(Constants.doi_username, Constants.doi_password);

			HashMap<String, String> metadataMap = new HashMap<String, String>();
			metadataMap.put("_target", target);

			ezid.setMetadata(doi, metadataMap);
		} catch (EZIDException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return "http://dx.doi.org/" + doi;
	}

	public boolean setDOIUnavailable(String doi) throws IOException {
		EZIDService ezid = new EZIDService(Constants.ezid_url);
		try {
			ezid.login(Constants.doi_username, Constants.doi_password);

			HashMap<String, String> metadataMap = new HashMap<String, String>();
			metadataMap.put(InternalProfile.STATUS.toString(),
					InternalProfileValues.UNAVAILABLE.toString());

			ezid.setMetadata(doi, metadataMap);
		} catch (EZIDException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean isPermanentDOI() {
		return permanentDOI;
	}

	/**
	 * Set the permanentDOI check in EzidService
	 *
	 * @param permanentDOI
	 *            If this is set to true, EzidService create permanent DOIs If
	 *            this is set to false, EzidService create temporary DOIs
	 */
	public void setPermanentDOI(boolean permanentDOI) {
		this.permanentDOI = permanentDOI;
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setROStatus(String doiInfo) {
		boolean permanent = false;
		HashMap<String, String> metadataMap = new HashMap<String, String>();
		try {

			JSONObject doiInfoObj = new JSONObject(doiInfo);
			if (!doiInfoObj.has("target")) {
				return Response
						.status(ClientResponse.Status.BAD_REQUEST)
						.entity(new JSONObject().put("Failure",
								"target not specified").toString()).build();
			}

			String targetUrl = doiInfoObj.get("target").toString();
			metadataMap.put(InternalProfile.TARGET.toString(), targetUrl);

			if (doiInfoObj.has("permanent")
					&& doiInfoObj.get("permanent").toString().equals("true")) {
				permanent = true;
			}
			String metadata = doiInfoObj.has("metadata") ? doiInfoObj.get(
					"metadata").toString() : "";
			metadataMap.putAll(getTranslatedTerms(new JSONObject(metadata)));
			// An RO is generically a datacite collection
			metadataMap.put(DataCiteProfile.RESOURCE_TYPE.toString(),
					DataCiteProfileResourceTypeValues.COLLECTION.toString());

			EZIDService ezid = new EZIDService(Constants.ezid_url);

			String shoulder = (permanent) ? Constants.doi_shoulder_prod
					: Constants.doi_shoulder_test;
			String doi_url = null;
			try {
				ezid.login(Constants.doi_username, Constants.doi_password);

				doi_url = ezid.mintIdentifier(shoulder, metadataMap);
			} catch (EZIDException e) {
				// null value will trigger error message
				e.printStackTrace();
			}
			if (doi_url != null) {
				System.out.println(EzidService.class.getName()
						+ " : DOI created Successfully - " + doi_url);
				return Response.ok(
						new JSONObject().put("doi", doi_url).toString())
						.build();
			} else {
				System.out.println(EzidService.class.getName()
						+ " : Error creating DOI ");
				return Response
						.status(ClientResponse.Status.INTERNAL_SERVER_ERROR)
						.entity(new JSONObject().put("Failure",
								"Error occurred while generating DOI")
								.toString()).build();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return Response.status(ClientResponse.Status.BAD_REQUEST).build();
		}
	}
}
