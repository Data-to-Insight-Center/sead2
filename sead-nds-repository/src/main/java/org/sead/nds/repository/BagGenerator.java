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
 */

package org.sead.nds.repository;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.zip.ParallelScatterZipCreator;
import org.apache.commons.compress.archivers.zip.ScatterZipOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntryRequest;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.parallel.InputStreamSupplier;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sead.nds.repository.util.StatusReceiver;

import edu.ucsb.nceas.ezid.EZIDException;
import edu.ucsb.nceas.ezid.EZIDService;
import edu.ucsb.nceas.ezid.profile.DataCiteProfile;
import edu.ucsb.nceas.ezid.profile.InternalProfile;

public class BagGenerator {

	private static final Logger log = Logger.getLogger(BagGenerator.class);
	Properties props = new Properties();

	private ParallelScatterZipCreator scatterZipCreator = new ParallelScatterZipCreator();
	private ScatterZipOutputStream dirs = null;
	BasicCookieStore cookieStore = new BasicCookieStore();
	private CloseableHttpClient client = HttpClientBuilder.create()
			.setDefaultCookieStore(cookieStore).build();

	private JSONArray aggregates = null;
	private ArrayList<String> resourceIndex = null;
	private Boolean[] resourceUsed = null;
	private HashMap<String, String> pidMap = new LinkedHashMap<String, String>();
	private HashMap<String, String> sha1Map = new LinkedHashMap<String, String>();

	private String bagPath = null;
	private String RO_id = null;
	private URL oreUrl = null;
	private String bearerToken = null;

	public BagGenerator(String RO_id) {
		this.bagPath = Repository.getDataPath();
		this.RO_id = RO_id;
		try {
			props.load(BagGenerator.class
					.getResourceAsStream("repository.properties"));
			System.out.println(props.toString());
		} catch (IOException e) {
			log.warn("Could not read repositories.properties file");
		}
	}

	/*
	 * Full workflow to generate new BagIt bag from ORE Map Url and Bag
	 * path/name on disk
	 * 
	 * @return success true/false
	 */
	public boolean generateBag(StatusReceiver sr) {
		if (getBagPath() == null || getRO_id() == null) {
			sr.sendStatusMessage("Failure",
					"Misconfiguration - missing ORE map URL or bag path/name");
			log.error("BagPath: " + getBagPath());
			log.error("ORE Url: " + getOreUrl());
			return false;
		}
		log.debug("Generating: Bag to the Future!");
		log.debug("BagPath: " + getBagPath());
		log.debug("RO ID: " + getRO_id());
		JSONObject pubRequest = getPublicationRequest(getRO_id());
		if (pubRequest.has("Bearer Token")) {
			bearerToken = pubRequest.getString("Bearer Token");
		} else if (props.containsKey("bearertoken.default")) {
			bearerToken = props.getProperty("bearertoken.default");
		}
		try {
			oreUrl = new URL(proxyIfNeeded(pubRequest.getJSONObject(
					"Aggregation").getString("@id")));

			File tmp = File.createTempFile("sead-scatter-dirs", "tmp");
			dirs = ScatterZipOutputStream.fileBased(tmp);

			JSONObject oremap = getOREMap(getOreUrl().toURI());
			JSONObject aggregation = oremap.getJSONObject("describes");
			String bagID = aggregation.getString("Identifier");
			String bagName = bagID;
			try {
				// Create valid filename from identifier and extend path with
				// two levels of hash-based subdirs to help distribute files
				bagName = getValidNameAndUpdatePath(bagName);
			} catch (Exception e) {
				log.error("Couldn't create valid filename: "
						+ e.getLocalizedMessage());
				return false;
			}
			// Create data dir in bag, also creates parent bagName dir
			String currentPath = bagName + "/data/";
			createDir(currentPath);

			aggregates = aggregation.getJSONArray("aggregates");

			if (aggregates != null) {
				// Add container and data entries
				// Setup global index of the aggregation and all aggregated
				// resources by Identifier
				resourceIndex = indexResources(bagID, aggregates);
				// Setup global list of succeed(true), fail(false), notused
				// (null) flags
				resourceUsed = new Boolean[aggregates.length() + 1];
				// Process current container (the aggregation itself) and its
				// children
				processContainer(aggregation, currentPath);
			}
			// Create mainifest files
			// pid-mapping.txt - a DataOne recommendation to connect ids and
			// in-bag path/names
			StringBuffer pidStringBuffer = new StringBuffer();
			boolean first = true;
			for (Entry<String, String> pidEntry : pidMap.entrySet()) {
				if (!first) {
					pidStringBuffer.append("\n");
				} else {
					first = false;
				}
				pidStringBuffer.append(pidEntry.getKey() + " "
						+ pidEntry.getValue());
			}
			createFileFromString(bagName + "/pid-mapping.txt",
					pidStringBuffer.toString());
			// SHA1 hash manifest - a hash manifest for md5 or sha1 is required
			// by Bagit spec
			StringBuffer sha1StringBuffer = new StringBuffer();
			first = true;
			for (Entry<String, String> sha1Entry : sha1Map.entrySet()) {
				if (!first) {
					sha1StringBuffer.append("\n");
				} else {
					first = false;
				}
				sha1StringBuffer.append(sha1Entry.getKey() + " "
						+ sha1Entry.getValue());
			}
			createFileFromString(bagName + "/manifest-sha1.txt",
					sha1StringBuffer.toString());
			// bagit.txt - Required by spec
			createFileFromString(bagName + "/bagit.txt",
					"BagIt-Version: 0.97\nTag-File-Character-Encoding: UTF-8");

			// Generate DOI:
			oremap.getJSONObject("describes").put("External Identifier",
					createDOIForRO(props, bagName, pubRequest, oremap));

			aggregation.put("Creators",
					expandPeople(normalizeValues(oremap.getJSONObject("describes").get("Creator"))));
			// Serialize oremap itself (pretty printed) - SEAD recommendation
			// (DataOne distributes metadata files within the bag
			// FixMe - add missing hash values if needed and update context
			// (read and cache files or read twice?)
			createFileFromString(bagName + "/oremap.jsonld.txt",
					oremap.toString(2));
			
			//Add a bag-info file
			createFileFromString(bagName + "/bag-info.txt",
					generateInfoFile(pubRequest, oremap));

			log.debug("Creating bag: " + getBagPath());
			// Create the bag file on disk
			File parent = new File(getBagPath());
			parent.mkdirs();
			File result = new File(getBagPath(), bagName + ".zip");
			// Create an output stream backed by the file
			ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(
					result);

			// Add all the waiting contents - dirs created first, then data
			// files
			// are retrieved via URLs in parallel (defaults to one thread per
			// processor)
			// directly to the zip file
			writeTo(zipArchiveOutputStream);
			// Finish
			zipArchiveOutputStream.close();

			ZipFile zf = new ZipFile(result);

			// Validate oremap - all entries are part of the collection
			for (int i = 0; i < resourceUsed.length; i++) {
				Boolean b = resourceUsed[i];
				if (b == null) {
					sr.sendStatusMessage("Problem",
							pidMap.get(resourceIndex.get(i)) + " was not used");
				} else if (b == false) {
					sr.sendStatusMessage("Problem",
							pidMap.get(resourceIndex.get(i))
									+ " was not included successfully");
				} else {
					// Successfully included - now check for hash value and
					// generate if needed
					if(i>0) { //Not root container
					if (!sha1Map
							.containsValue(pidMap.get(resourceIndex.get(i)))) {
						
						if(!isContainer(aggregates.getJSONObject(i-1)))
						log.debug("Missing sha1 hash for: "
								+ resourceIndex.get(i));
						// FixMe - actually generate it before adding the oremap
						// to the zip
					}
					}
				}

			}

			// Run a confirmation test - should verify all files and sha1s
			checkFiles(sha1Map, zf, sr);
			zf.close();
			return true;

		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			e.printStackTrace();
			sr.sendStatusMessage("Failure",
					"Processing failure during Bagit file creation");
			return false;
		}
	}

	private String proxyIfNeeded(String urlString) {

		if (props.containsKey("JSESSIONID")) {
			return props.getProperty("c3pr.address")
					+ urlString.substring(urlString.indexOf("api"));
		} else {
			return urlString;
		}
	}

	private String getValidNameAndUpdatePath(String bagName)
			throws NoSuchAlgorithmException, IOException {
		String pathString = DigestUtils.sha1Hex(bagName);

		// Two level hash-based distribution o files
		bagPath = Paths.get(bagPath, pathString.substring(0, 2),
				pathString.substring(2, 4)).toString();
		// Create known-good filename
		return bagName.replaceAll("\\W+", "_");
	}

	private void processContainer(JSONObject item, String currentPath) {
		JSONArray children = getChildren(item);
		currentPath = currentPath + item.getString("Title") + "/";
		try {
			createDir(currentPath);
			// Add containers to pid map and mark as 'used', but no sha1 hash
			// value
			resourceUsed[resourceIndex.indexOf(item.getString("Identifier"))] = true;
			pidMap.put(item.getString("Identifier"), currentPath);
		} catch (Exception e) {
			resourceUsed[resourceIndex.indexOf(item.getString("Identifier"))] = false;
			e.printStackTrace();
		}
		for (int i = 0; i < children.length(); i++) {

			// Find the ith child in the overall array of aggregated
			// resources
			String childId = children.getString(i);
			// Clowder Kludge
			if (childId.length() == 24) {
				childId = "urn:uuid:" + childId;
			}
			int index = resourceIndex.indexOf(childId);
			if (resourceUsed[index] != null) {
				System.out.println("Warning: reusing resource " + index);
			}
			resourceUsed[index] = true;
			// Aggregation is at index 0, so need to shift by 1 for aggregates
			// entries
			JSONObject child = aggregates.getJSONObject(index - 1);
			if (isContainer(child)) {
				// create dir and process children
				processContainer(child, currentPath);
			} else {
				// add item
				String dataUrl = (String) child.get("similarTo");
				String childPath = currentPath + (String) child.get("Title");
				try {
					log.debug("Requesting: " + childPath + " from " + dataUrl);
					createFileFromURL(childPath, dataUrl);
				} catch (Exception e) {
					resourceUsed[index] = false;
					e.printStackTrace();
				}
				// Check for nulls!
				pidMap.put(child.getString("Identifier"), childPath);
				// Check for nulls!
				if (child.has("SHA1 Hash")) {
					sha1Map.put(child.getString("SHA1 Hash"), childPath);
				}
			}
		}
	}

	// Logic to decide if this is a container -
	// first check for children, then check for source-specific type indicators
	private boolean isContainer(JSONObject item) {
		if (getChildren(item).length() != 0) {
			return true;
		}
		Object o = item.get("@type");
		if (o != null) {
			if (o instanceof JSONArray) {
				for (int i = 0; i < ((JSONArray) o).length(); i++) {
					if ("http://cet.ncsa.uiuc.edu/2007/Collection"
							.equals(((JSONArray) o).getString(i))) {
						return true;
					}
					// Check for Clowder type
				}
			} else if (o instanceof String) {
				if ("http://cet.ncsa.uiuc.edu/2007/Collection"
						.equals((String) o)) {
					return true;
				}
				// Check for Clowder type
			}
		}
		return false;
	}

	// Get's all "Has Part" children, standardized to send an array with 0,1, or
	// more elements
	private JSONArray getChildren(JSONObject aggregation) {
		Object o = null;
		try {
			o = aggregation.get("Has Part");
		} catch (JSONException e) {
			// Doesn't exist - that's OK
		}
		if (o == null) {
			return new JSONArray();
		} else {
			if (o instanceof JSONArray) {
				return (JSONArray) o;
			} else if (o instanceof String) {
				return new JSONArray("[	" + (String) o + " ]");
			}
			System.out.println("Error finding children: " + o.toString());
			return new JSONArray();
		}
	}

	private ArrayList<String> indexResources(String aggId, JSONArray aggregates) {
		ArrayList<String> l = new ArrayList<String>(aggregates.length() + 1);
		l.add(aggId);
		for (int i = 0; i < aggregates.length(); i++) {
			log.debug("Indexing : " + i + " " 
					+ aggregates.getJSONObject(i).getString("Identifier"));
			l.add(aggregates.getJSONObject(i).getString("Identifier"));
		}
		return l;
	}

	private JSONObject getPublicationRequest(String id) {
		JSONObject request = null;
		String c3prServer = props.getProperty("c3pr.address");
		HttpGet getPubRequest;
		try {
			log.debug("Retrieving: " + c3prServer + "api/researchobjects/"
					+ URLEncoder.encode(id, "UTF-8"));
			getPubRequest = new HttpGet(c3prServer + "api/researchobjects/"
					+ URLEncoder.encode(id, "UTF-8"));

			if (props.containsKey("JSESSIONID")) {
				// Proxy Mode
				log.debug("Adding: " + props.getProperty("JSESSIONID"));

				BasicClientCookie cookie = new BasicClientCookie("JSESSIONID",
						props.getProperty("JSESSIONID"));
				URL c3pr = new URL(c3prServer);

				cookie.setDomain(c3pr.getHost());
				cookie.setPath("/");
				cookie.setSecure(c3pr.getProtocol().equalsIgnoreCase("https") ? true
						: false);
				cookieStore.addCookie(cookie);
			}
			getPubRequest.addHeader("accept", "application/json");

			CloseableHttpResponse response = client.execute(getPubRequest);

			if (response.getStatusLine().getStatusCode() == 200) {
				System.out.println("200");
				String mapString = EntityUtils.toString(response.getEntity());
				log.debug(mapString);
				request = new JSONObject(mapString);

			}
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return request;
	}

	private String getRO_id() {
		return RO_id;
	}

	private JSONObject getOREMap(URI url) {
		JSONObject map = null;
		log.debug("Retreiving: " + url.toString());
		HttpGet getMap = createNewGetRequest(url, MediaType.APPLICATION_JSON);

		try {

			CloseableHttpResponse response = client.execute(getMap);

			if (response.getStatusLine().getStatusCode() == 200) {
				System.out.println("200");
				String mapString = EntityUtils.toString(response.getEntity());
				log.debug("OREMAP: " + mapString);
				map = new JSONObject(mapString);

			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}

	private HttpGet createNewGetRequest(URI url, String returnType) {

		HttpGet request = null;

		if (bearerToken != null) {
			// Clowder Kluge - don't add key once Auth header is accepted
			try {
				request = new HttpGet(new URI(url.toURL().toString() + "?key="
						+ bearerToken));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			request.addHeader("Authorization", "Bearer " + bearerToken);
		} else {
			request = new HttpGet(url);
		}
		if (returnType != null) {
			request.addHeader("accept", returnType);
		}
		return request;
	};

	private void createDir(final String name) throws IOException,
			ExecutionException, InterruptedException {

		ZipArchiveEntry archiveEntry = new ZipArchiveEntry(name);
		archiveEntry.setMethod(ZipEntry.DEFLATED);
		InputStreamSupplier supp = new InputStreamSupplier() {
			public InputStream get() {
				return new ByteArrayInputStream(("").getBytes());
			}
		};

		addEntry(archiveEntry, supp);
	}

	private void createFileFromString(final String name, final String content)
			throws IOException, ExecutionException, InterruptedException {

		ZipArchiveEntry archiveEntry = new ZipArchiveEntry(name);
		archiveEntry.setMethod(ZipEntry.DEFLATED);
		InputStreamSupplier supp = new InputStreamSupplier() {
			public InputStream get() {
				try {
					return new ByteArrayInputStream(content.getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
		};

		addEntry(archiveEntry, supp);
	}

	private void createFileFromURL(final String name, final String uri)
			throws IOException, ExecutionException, InterruptedException {

		ZipArchiveEntry archiveEntry = new ZipArchiveEntry(name);
		archiveEntry.setMethod(ZipEntry.DEFLATED);
		InputStreamSupplier supp = new InputStreamSupplier() {
			public InputStream get() {
				try {
					HttpGet getMap = createNewGetRequest(new URI(uri), null);
					System.out.println("request for: " + name);
					CloseableHttpResponse response;

					response = client.execute(getMap);

					if (response.getStatusLine().getStatusCode() == 200) {
						System.out.println("200: " + name);
						return response.getEntity().getContent();
					}
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
		};

		addEntry(archiveEntry, supp);
	}

	private void checkFiles(HashMap<String, String> sha1Map2, ZipFile zf,
			StatusReceiver sr) {
		for (Entry<String, String> entry : sha1Map2.entrySet()) {
			if (!hasValidFileHash(entry.getKey(), entry.getValue(), zf)) {
				sr.sendStatusMessage("Problem", "Hash for " + entry.getValue()
						+ " is incorrect.");
			}
		}
	}

	private boolean hasValidFileHash(String hash, String name, ZipFile zf) {

		boolean good = false;

		String realHash = generateFileHash(name, zf);
		if (hash.equals(realHash)) {
			good = true;
			log.debug("Valid hash for " + name);
		}
		return good;
	}

	private String generateFileHash(String name, ZipFile zf) {

		ZipArchiveEntry archiveEntry1 = zf.getEntry(name);
		long start = System.currentTimeMillis();
		InputStream inputStream;
		String realHash = null;
		try {
			inputStream = zf.getInputStream(archiveEntry1);

			realHash = DigestUtils.sha1Hex(inputStream);

		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.debug("Retrieve/compute time = "
				+ (System.currentTimeMillis() - start) + " ms");
		return realHash;
	}

	public void addEntry(ZipArchiveEntry zipArchiveEntry,
			InputStreamSupplier streamSupplier) throws IOException {
		if (zipArchiveEntry.isDirectory() && !zipArchiveEntry.isUnixSymlink())
			dirs.addArchiveEntry(ZipArchiveEntryRequest
					.createZipArchiveEntryRequest(zipArchiveEntry,
							streamSupplier));
		else
			scatterZipCreator.addArchiveEntry(zipArchiveEntry, streamSupplier);
	}

	public void writeTo(ZipArchiveOutputStream zipArchiveOutputStream)
			throws IOException, ExecutionException, InterruptedException {
		dirs.writeTo(zipArchiveOutputStream);
		dirs.close();
		scatterZipCreator.writeTo(zipArchiveOutputStream);
	}

	public String getBagPath() {
		return bagPath;
	}

	public URL getOreUrl() {
		return oreUrl;
	}

	public String createDOIForRO(Properties props, String bagName,
			JSONObject request, JSONObject map) throws EZIDException {
		String target = Repository.getLandingPage(bagName);
		log.debug("DOI Landing Page: " + target);
		String creators = getCreatorsString(normalizeValues(map.get("Creator")));

		HashMap<String, String> metadata = new LinkedHashMap<String, String>();
		metadata.put(InternalProfile.TARGET.toString(), target);
		metadata.put(DataCiteProfile.TITLE.toString(),
				((JSONObject) map.get("describes")).getString("Title"));
		metadata.put(DataCiteProfile.CREATOR.toString(), creators);
		String rightsholderString = "SEAD (http://sead-data.net";
		if (request.has("Rights Holder")) {
			rightsholderString = request.getString("Rights Holder") + ", "
					+ rightsholderString;
		} else {
			log.warn("Request has no Rights Holder");
		}
		metadata.put(DataCiteProfile.PUBLISHER.toString(), rightsholderString);
		metadata.put(DataCiteProfile.PUBLICATION_YEAR.toString(),
				String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));

		System.out.println(props.getProperty("ezid.url"));
		System.out.println(props.getProperty("doi.user"));
		System.out.println(props.getProperty("doi.pwd"));
		EZIDService ezid = new EZIDService(props.getProperty("ezid.url"));

		boolean permanent = props.get("doi.default").equals("temporary") ? false
				: true;
		if (((JSONObject) request.get("Preferences")).has("Purpose")) {
			String purpose = ((JSONObject) request.get("Preferences"))
					.getString("Purpose");
			if (purpose.equalsIgnoreCase("Testing-Only")) {
				permanent = false;
			}
		}
		ezid.login(props.getProperty("doi.user"), props.getProperty("doi.pwd"));
		String shoulder = (permanent) ? props.getProperty("doi.shoulder.prod")
				: props.getProperty("doi.shoulder.test");
		String doi = ezid.mintIdentifier(shoulder, metadata);
		return "http://dx.doi.org/" + doi;
	}

	private String getCreatorsString(String[] creators) {
		StringBuffer sBuffer = new StringBuffer();
		if ((creators != null) && (creators.length != 0)) {

			boolean first = true;
			for (int i = 0; i < creators.length; i++) {
				if (!first) {
					sBuffer.append(", ");
				} else {
					first = false;
				}
				sBuffer.append(creators[i]);
			}
		}
		return sBuffer.toString();
	}

	private String[] normalizeValues(Object cObject) {

		ArrayList<String> creatorList = new ArrayList<String>();
		if (cObject instanceof String) {
			creatorList.add((String) cObject);
		} else if (cObject instanceof JSONArray) {
			for (int i = 0; i < ((JSONArray) cObject).length(); i++) {
				creatorList.add(((JSONArray) cObject).getString(i));
			}
		} else {
			log.warn("\"Creator\" element is not a string or array of strings");
		}
		return creatorList.toArray(new String[creatorList.size()]);
	}

	private JSONArray expandPeople(String[] people) {
		
		JSONArray peopleArray = new JSONArray();
		if ((people != null) && (people.length != 0)) {
			for (int i = 0; i < people.length; i++) {
				log.debug("Expanding: " + people[i]);
				String c3prServer = props.getProperty("c3pr.address");
				HttpGet getPerson;
				try {
					getPerson = new HttpGet(c3prServer + "api/people/"
							+ URLEncoder.encode(people[i], "UTF-8"));

					getPerson.addHeader("accept", "application/json");

					CloseableHttpResponse response = client.execute(getPerson);

					if (response.getStatusLine().getStatusCode() == 200) {
						System.out.println("200");
						String mapString = EntityUtils.toString(response
								.getEntity());
						log.debug("Expanded: " + mapString);
						peopleArray.put(new JSONObject(mapString));

					} else {
						peopleArray.put(people[i]);
					}
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return peopleArray;

	}

	static final String CRLF = "\r\n";

	private String generateInfoFile(JSONObject request, JSONObject map) {
		log.debug("Generating info file");
		StringBuffer info = new StringBuffer();

		// SEAD and Rights Holder?
		if (map.getJSONObject("describes").has("Primary Source")) {
			info.append("Source-Organization: ");
			info.append(map.getJSONObject("describes").getString(
					"Primary Source"));
			info.append(CRLF);
		}
		JSONArray contactsArray = new JSONArray();
		if (map.getJSONObject("describes").has("Contact")) {
			contactsArray = expandPeople(normalizeValues(map.getJSONObject(
					"describes").get("Contact")));
		} else {
			if (request.has("Rights Holder")) {
				contactsArray = expandPeople(normalizeValues(request
						.get("Rights Holder")));
			}
		}
		for (int i = 0; i < contactsArray.length(); i++) {
			info.append("Contact-Name: ");
			Object person = contactsArray.get(i);
			if (person instanceof String) {
				info.append((String) person);
				info.append(CRLF);

			} else {
				info.append(((JSONObject) person).getString("givenName")
						+ ((JSONObject) person).getString("familyName"));
				info.append(CRLF);
				if (((JSONObject) person).has("email")) {
					info.append("Contact-Email: ");
					info.append(((JSONObject) person).getString("email"));
					info.append(CRLF);
				}
			}
		}

		info.append("Source-Organization: ");
		info.append("SEAD (http://sead-data.net/)");
		info.append(CRLF);

		info.append("Organization-Address: ");
		info.append("University of Michigan, Ann Arbor, MI 48106");
		info.append(CRLF);

		info.append("Contact-Email: ");
		info.append("SEADdatanet@umich.edu");
		info.append(CRLF);

		info.append("External-Description: ");

		info.append(WordUtils.wrap(
				map.getJSONObject("describes").getString("Abstract"), 78, CRLF
						+ " ", true));
		info.append(CRLF);

		info.append("Bagging-Date: ");
		info.append((new SimpleDateFormat("yyyy-MM-dd").format(Calendar
				.getInstance().getTime())));
		info.append(CRLF);
		info.append("External-Identifier: ");
		info.append(map.getJSONObject("describes").getString(
				"External Identifier"));
		info.append(CRLF);

		info.append("Bag-Size: ");
		info.append(byteCountToDisplaySize(Long.parseLong(request
				.getJSONObject("Aggregation Statistics")
				.getString("Total Size"))));
		info.append(CRLF);

		info.append("Payload-Oxum: ");
		info.append(request.getJSONObject("Aggregation Statistics").getString(
				"Total Size"));
		info.append(".");
		info.append(request.getJSONObject("Aggregation Statistics").getString(
				"Number of Datasets"));
		info.append(CRLF);

		info.append("Internal-Sender-Identifier: ");
		info.append(map.getJSONObject("describes").getString("Identifier"));
		info.append(CRLF);

		return info.toString();

	}
	
	/** Adapted from org/apache/commons/io/FileUtils.java
	*   change to SI - add 2 digits of precision
	*/ 
    /**
     * The number of bytes in a kilobyte.
     */
    public static final long ONE_KB = 1000;

    /**
     * The number of bytes in a megabyte.
     */
    public static final long ONE_MB = ONE_KB * ONE_KB;

    /**
     * The number of bytes in a gigabyte.
     */
    public static final long ONE_GB = ONE_KB * ONE_MB;

    
	/**
     * Returns a human-readable version of the file size, where the input
     * represents a specific number of bytes.
     *
     * @param size  the number of bytes
     * @return a human-readable display value (includes units)
     */
    public static String byteCountToDisplaySize(long size) {
        String displaySize;

        if (size / ONE_GB > 0) {
            displaySize = String.valueOf(Math.round(size / (ONE_GB/100.0d))/100.0) + " GB";
        } else if (size / ONE_MB > 0) {
            displaySize = String.valueOf(Math.round(size / (ONE_MB/100.0d))/100.0) + " MB";
        } else if (size / ONE_KB > 0) {
            displaySize = String.valueOf(Math.round(size / (ONE_KB/100.0d))/100.0) + " KB";
        } else {
            displaySize = String.valueOf(size) + " bytes";
        }
        return displaySize;
    }
}
