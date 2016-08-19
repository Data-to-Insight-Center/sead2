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
 * @author myersjd@umich.edu
 * 
 */

package org.sead.repositories.reference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Paths;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONObject;
import org.sead.nds.repository.BagGenerator;
import org.sead.nds.repository.C3PRPubRequestFacade;
import org.sead.nds.repository.PubRequestFacade;
import org.sead.nds.repository.Repository;
import org.sead.repositories.reference.util.RefLocalContentProvider;
import org.sead.repositories.reference.util.ReferenceLinkRewriter;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * RefRepository generates new data publications and manages the RESTful
 * interface to the published data packages stored as Zip files. It generates
 * the landing page for a given DOI and, based on the landing URL, finds the
 * corresponding zip and extracts the relevant data/metadata. To help with this,
 * it extracts the oremap and a short description file that includes just the
 * top-level description and top-level children from the oremap, and an index
 * defining the offsets, within the oremap file, for the json description for
 * each AggregatedResource (e.g. a collection or dataset (1.5) or Dataset/File
 * (2.0).
 *
 */

@Path("/")
public class RefRepository extends Repository {

	private static final Logger log = Logger.getLogger(RefRepository.class);

	static ObjectMapper mapper = new ObjectMapper();

	public RefRepository() {
	}

	/*
	 * BackwardCompatibility - Tomcat 6 seems to require that Repository be
	 * initialized in this class even though it is initialized in the
	 * RepoContextListener class in the same app It does not appear that this is
	 * needed in Tomcat 7 ...
	 */
	static {
		Repository.init(loadProperties());
	}

	private static String roId = null;
	private static String localRequest = null;
	private static String localContentSource = null;
	private static boolean validateOnly = false;

	public static void main(String[] args) {
		PropertyConfigurator.configure("./log4j.properties");
		init(loadProperties());
		if (args.length == 0) {
			printUsage();
		}
		roId = args[0];

		if (args.length > 1) {
			int i = 1;
			while (i < args.length) {
				if (args[i].startsWith("-")) {
					char flag = args[i].charAt(1);
					switch (flag) {
					case 'l':
						localRequest = args[i + 1];
						System.out
								.println("Local Pub Request: " + localRequest);
						i++;
						break;
					case 'r':
						localContentSource = args[i + 1];
						System.out.println("LocalContentSource: "
								+ localContentSource);
						i++;
						break;
					case 'v':
						validateOnly = true;
						break;
					default:
						printUsage();
						break;

					}
				}
				i += 1;
			}
		}
		/*
		 * At this point we have an RO ID and possibly a local pub request and
		 * possibly a local Content source.
		 */
		C3PRPubRequestFacade RO = null;
		RO = new RefRepoLocalPubRequestFacade(roId, localRequest, getProps());
		BagGenerator bg;
		bg = new BagGenerator(RO);
		if (validateOnly) {
			bg.validateBag(roId);
			log.info("Validation Complete.");
			System.exit(0);
			;
		}
		// Request human approval if needed - will send a fail status and
		// exit if request is denied
		localContentSource = handleRepub(RO, bg, localContentSource);
		bg.setLinkRewriter(new ReferenceLinkRewriter(getProps().getProperty(
				"repo.landing.base")));
		// FixMe - use repo.ID from properties file (possibly in repo class

		// If using local Content and it is the same RO ID as the new pub (just
		// reprocessing an existing RO, make the BagGenerator
		// use a temp file (and not overwrite the local RO with an empty version
		// at the start).
		boolean useTemp = (localContentSource != null)
				&& (localContentSource.equals(roId));
		if (bg.generateBag(roId, (useTemp))) {
			RO.sendStatus(PubRequestFacade.SUCCESS_STAGE, RO.getOREMap()
					.getJSONObject("describes")
					.getString("External Identifier"));
			System.out
					.println("Publication was successful. New publication is in: "
							+ RefRepository.getDataPathTo(roId));
			if (localContentSource != null) {
				System.out.println("New Publication was intended to replace "
						+ localContentSource);
				System.out.println("Old publication is in "
						+ RefRepository.getDataPathTo(localContentSource)
						+ " and could now be deleted.");
			}
		} else {
			RO.sendStatus(
					PubRequestFacade.FAILURE_STAGE,
					"Processing of this request has failed. Further attempts to process this request may or may not be made. Please contact the repository for further information.");
		}

		System.exit(0);
	}

	private static void printUsage() {
		System.out
				.println("Could not parse requuest: No processing will occur.");
		System.out
				.println("Usage:  <RO Identifier> <-l <optional local pubRequest file (path to JSON document)>> <-r <local Content Source RO ID>> <-v>");
		System.out
				.println("-v - validateOnly - assumes a zip file for this RO ID exists and will attempt to validate the stored files w.r.t. the hash values in the oremap.");
		System.out
				.println("Note: RO identifier is always sent and must match the identifier in any local pub Request file used.");
		System.out
				.println("Note: A local content source will override info sent as an alternateOf Preference.");

		System.exit(0);
	}

	private static String handleRepub(C3PRPubRequestFacade RO, BagGenerator bg,
			String localSource) {

		JSONObject request = RO.getPublicationRequest();
		JSONObject prefs = request.getJSONObject("Preferences");
		Scanner input = new Scanner(System.in);
		if (prefs.has("External Identifier")) {
			String extIdPref = prefs.getString("External Identifier");
			System.out.println("This publication is intended to replace "
					+ extIdPref);
			if (!((String) getProps().get("repo.allowupdates"))
					.equalsIgnoreCase("true")) {
				System.out
						.println("NOTE: Since updates are not allowed, a new DOI will be generated.");
			}
			System.out.println("Proceed (Y/N)?: ");

			if (!input.next().equalsIgnoreCase("y")) {
				input.close();
				RO.sendStatus(
						PubRequestFacade.FAILURE_STAGE,
						"This request has been denied as a replacement for the existing publication: "
								+ extIdPref
								+ ". Please contact the repository for further information.");
				System.exit(0);
			}
		}
		if (localSource == null && prefs.has("alternateOf")) {
			// Add a LocalContent class
			localSource = prefs.getString("alternateOf");
			System.out
					.println("Setting local content source to alternateOf value: "
							+ localSource);
		}
		if (localSource != null) {
			System.out.println("Looking at: " + localSource
					+ " for local content.");
			log.info("Looking at: " + localSource + " for local content.");
			RefLocalContentProvider ref = new RefLocalContentProvider(
					localSource, Repository.getProps());
			if (ref.getHashType() != null) {
				bg.setLocalContentProvider(ref);
				System.out.println("Proceeding with : " + localSource
						+ " for local content.");
			} else {

				System.out
						.println("Original RO not found/has no usable hash entries: "
								+ getDataPathTo(localSource));
				System.out.println("Proceed (using remote content)? {Y/N}: ");
				if (!input.next().equalsIgnoreCase("y")) {
					input.close();
					RO.sendStatus(
							PubRequestFacade.FAILURE_STAGE,
							"This request won't be processed due to a problem in finding local data copies: "
									+ localSource
									+ ". Please contact the repository for further information.");
					System.exit(0);
				}
				localSource = null;
			}
		}

		input.close();
		return localSource;
	}

	/**
	 * @Path("/researchobjects")
	 * 
	 *                           Returns the base landingpage html
	 */
	@Path("/repository")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public Response getRepositoryInfo() {
		String id = Repository.getID();
		String SEADServicesURL = Repository.getC3PRAddress();
		URL repoInfo = null;
		try {
			repoInfo = new URL(SEADServicesURL + "api/repositories/"
					+ URLEncoder.encode(id, "UTF-8"));
			// Make a connect to the server
			log.debug("Connecting to: " + repoInfo.toString());
			HttpURLConnection conn = null;
			conn = (HttpURLConnection) repoInfo.openConnection();

			conn.setDoInput(true);
			conn.setUseCaches(false);
			InputStream is = conn.getInputStream();

			return Response.ok(is).build();

		} catch (MalformedURLException e) {
			log.error("Bad Repo URL");
		} catch (IOException e) {
			log.warn("Could not contact c3pr: " + repoInfo.toString());
		}
		log.debug("Unable to refer to repository info @ c3pr");
		return Response
				.status(com.sun.jersey.api.client.ClientResponse.Status.INTERNAL_SERVER_ERROR)
				.build();
	}

	/*
	 * @Path("/researchobjects")
	 * 
	 * Returns the base landingpage html
	 */

	@Path("/researchobjects/{id}")
	@Produces(MediaType.TEXT_HTML)
	@GET
	public Response getLandingPage(@PathParam(value = "id") String id) {
		URI landingPage = null;
		try {
			landingPage = new URI("../landing.html#"
					+ URLEncoder.encode(id, "UTF-8"));

		} catch (URISyntaxException e) {
			log.warn(e.getMessage() + " id: " + id);
		} catch (UnsupportedEncodingException e) {
			log.warn("UTF-8 not supported");
		}
		log.debug("Referring to : " + landingPage.toString());
		// Fairly permanent, but using temporary to keep the permanent html and
		// json URLs for the RO the same...
		return Response.temporaryRedirect(landingPage).build();

	}

	/*
	 * @Path("/researchobjects/{id}")
	 * 
	 * Returns the description file for the Aggregation
	 * 
	 * /researchobjects/{id}/metadata returns this plus the top level of
	 * children
	 */

	@Path("/researchobjects/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public Response getAggregationSummary(@PathParam(value = "id") String id) {

		File descFile;
		try {
			// get or generate this file
			descFile = getDescFile(id);
		} catch (Exception e1) {
			log.error(e1.getLocalizedMessage(), e1);
			return Response.serverError().build();
		}
		log.debug("Ready to send desc file");
		try {
			final FileInputStream fis = new FileInputStream(descFile);

			StreamingOutput stream = new StreamingOutput() {
				public void write(OutputStream os) throws IOException,
						WebApplicationException {
					IOUtils.copy(fis, os);
					fis.close();
				}
			};

			return Response.ok(stream).build();
		} catch (IOException e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
	}

	/*
	 * @Path("/researchobjects/{id}/metadata")
	 * 
	 * Returns the description for the Aggregation (the Aggregation metadata and
	 * the descriptions of the AggregatedResources at the top-level(direct
	 * children listed in 'HasPart')
	 */

	@Path("/researchobjects/{id}/metadata")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public Response getResourceMetadata(@PathParam(value = "id") String id) {
		String path = getDataPathTo(id);
		String bagNameRoot = getBagNameRoot(id);

		File result = new File(path, bagNameRoot + ".zip");
		if (!result.exists()) {
			return Response.status(Status.NOT_FOUND).build();
		}

		log.debug(result.getAbsolutePath());
		CountingInputStream cis = null;
		try {
			// Check for index files
			File indexFile = getIndexFile(id);
			File oremap = getOREMapFile(id);
			// Find/open base ORE map file
			// Note - limited to maxint size for oremap file size
			cis = new CountingInputStream(new BufferedInputStream(
					new FileInputStream(oremap), Math.min(
							(int) oremap.length(), 1000000)));
			JsonNode resultNode = getAggregation(id, indexFile, cis, true,
					oremap.length());
			if (resultNode == null) {
				log.warn("Null item returned");
			}

			return Response.ok(resultNode.toString()).build();
		} catch (JsonParseException e) {
			log.error(e);
			e.printStackTrace();
			return Response.serverError().entity(e.getMessage()).build();
		} catch (IOException e) {
			log.error(e);
			e.printStackTrace();
			return Response.serverError().entity(e.getMessage()).build();
		} finally {
			IOUtils.closeQuietly(cis);
		}
	}

	/*
	 * @Path("/researchobjects/{id}/metadata/{did}")
	 * 
	 * Returns the description for the AggregationResource within the {id}
	 * Aggregation (the AggregatedResource metadata and the descriptions of the
	 * AggregatedResources directly within it (direct children listed in
	 * 'HasPart'))
	 */

	private File getOREMapFile(String id) {
		File map = null;
		String path = getDataPathTo(id);
		String bagNameRoot = getBagNameRoot(id);

		map = new File(path, bagNameRoot + ".oremap.jsonld.txt");
		if (!map.exists()) {
			createMap(map, path, bagNameRoot);

		}
		return map;
	}

	protected static void createMap(File map, String path, String bagNameRoot) {
		ZipFile zf = null;
		try {
			log.info("Caching oremap: " + map.getPath());
			// Note: This step can be VERY slow when something is being
			// published on the same disk - minutes for a large file
			// If you don't see the "Zipfile opened" message in the log,
			// look at disk I/O...
			File result = new File(path, bagNameRoot + ".zip");
			zf = new ZipFile(result);
			log.debug("Zipfile opened");
			ZipEntry archiveEntry1 = zf.getEntry(bagNameRoot
					+ "/oremap.jsonld.txt");
			InputStream source = zf.getInputStream(archiveEntry1);
			OutputStream sink = new FileOutputStream(map);
			IOUtils.copy(source, sink);
			IOUtils.closeQuietly(source);
			IOUtils.closeQuietly(sink);
			log.debug("ORE Map written: " + result.getCanonicalPath());
		} catch (Exception e) {
			log.error(
					"Cannot read zipfile to create cached oremap: "
							+ map.getPath(), e);
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(zf);
		}
	}

	@Path("/researchobjects/{id}/metadata/{did}")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public Response getResourceSummary(@PathParam(value = "id") String id,
			@PathParam(value = "did") String dataID) {
		log.debug("Getting " + dataID + " from " + id);
		String path = getDataPathTo(id);
		String bagNameRoot = getBagNameRoot(id);

		File result = new File(path, bagNameRoot + ".zip");
		if (!result.exists()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		CountingInputStream cis = null;
		try {
			File indexFile = getIndexFile(id);

			// Find/open base ORE map file
			// Note - limited to maxint size for oremap file size
			File map = getOREMapFile(id);
			cis = new CountingInputStream(new BufferedInputStream(
					new FileInputStream(map), Math.min((int) map.length(),
							1000000)));

			JsonNode resultNode = getItem(dataID, indexFile, cis, true,
					map.length());
			if (resultNode == null) {
				log.warn("Null item returned");
			}

			return Response.ok(resultNode.toString()).build();
		} catch (JsonParseException e) {
			log.error(e);
			e.printStackTrace();
			return Response.serverError().entity(e.getMessage()).build();
		} catch (IOException e) {
			log.error(e);
			e.printStackTrace();
			return Response.serverError().entity(e.getMessage()).build();
		} finally {
			IOUtils.closeQuietly(cis);
		}
	}

	/*
	 * @Path("/researchobjects/{id}/data/{relpath}")
	 * 
	 * Returns the data file (any file within the /data directory) at the given
	 * path within the {id} publication
	 * 
	 * Note: The original version using the apache compress ZiFile class used
	 * for generating the bags can be extremely slow when reading large files
	 * (e.g. 20+ minutes for a 600GB file), even when all we do is extract one
	 * file. The java.uti.zip.ZipFile class seems to work normally (<second).
	 */

	@Path("/researchobjects/{id}/data/{relpath}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public Response getDatafile(@PathParam(value = "id") String id,
			@PathParam(value = "relpath") String datapath) {
		String path = getDataPathTo(id);

		String bagNameRoot = getBagNameRoot(id);
		File result = new File(path, bagNameRoot + ".zip");
		StreamingOutput stream = null;

		try {
			final ZipFile zf = new ZipFile(result);
			ZipEntry archiveEntry1 = zf.getEntry(bagNameRoot + "/data/"
					+ datapath);
			if (archiveEntry1 != null) {
				final InputStream inputStream = new BufferedInputStream(
						zf.getInputStream(archiveEntry1));

				stream = new StreamingOutput() {
					public void write(OutputStream os) throws IOException,
							WebApplicationException {
						IOUtils.copy(inputStream, os);
						IOUtils.closeQuietly(os);
						IOUtils.closeQuietly(inputStream);
						IOUtils.closeQuietly(zf);
					}
				};
			}
		} catch (IOException e) {
			log.error(e.getLocalizedMessage());
			e.printStackTrace();
		}
		if (stream == null) {
			return Response.serverError().build();
		}

		return Response.ok(stream).build();
	}

	/*
	 * @Path("/researchobjects/{id}/meta/{relpath}")
	 * 
	 * Returns the metadata file (a file not in the /data dir) at the given path
	 * within the {id} publication
	 */

	@Path("/researchobjects/{id}/meta/{relpath}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public Response getMetadatafile(@PathParam(value = "id") String id,
			@PathParam(value = "relpath") String metadatapath) {
		String path = getDataPathTo(id);

		String bagNameRoot = getBagNameRoot(id);
		File result = new File(path, bagNameRoot + ".zip");

		// Don't let this call be used to get data from the data dir
		if (metadatapath.startsWith("data") || metadatapath.startsWith("/data")) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		StreamingOutput stream = null;
		try {
			final ZipFile zf = new ZipFile(result);
			ZipEntry archiveEntry1 = zf.getEntry(bagNameRoot + "/"
					+ metadatapath);
			if (archiveEntry1 != null) {
				final InputStream inputStream = new BufferedInputStream(
						zf.getInputStream(archiveEntry1));
				stream = new StreamingOutput() {
					public void write(OutputStream os) throws IOException,
							WebApplicationException {
						IOUtils.copy(inputStream, os);
						IOUtils.closeQuietly(inputStream);
						IOUtils.closeQuietly(os);
						IOUtils.closeQuietly(zf);
					}
				};
			}
		} catch (IOException e) {
			log.error(e.getLocalizedMessage());
			e.printStackTrace();
		}
		if (stream == null) {
			return Response.serverError().build();
		}

		return Response.ok(stream).build();

	}

	@Path("/researchobjects/{id}/bag")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public Response getBag(@PathParam(value = "id") String id) {

		String path = getDataPathTo(id);
		String bagNameRoot = getBagNameRoot(id);

		File result = new File(path, bagNameRoot + ".zip");
		try {
			final InputStream inputStream = FileUtils.openInputStream(result);

			StreamingOutput stream = new StreamingOutput() {
				public void write(OutputStream os) throws IOException,
						WebApplicationException {
					IOUtils.copy(inputStream, os);
					IOUtils.closeQuietly(inputStream);
				}
			};

			return Response.ok(stream).build();
		} catch (IOException e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
	}

	// Calculate the path to the zip in the file system based in the base path
	// and the 2 level hash subdirectory scheme
	public static String getDataPathTo(String id) {
		String pathString = DigestUtils.sha1Hex(id);
		String path = Repository.getDataPath();
		// Two level hash-based distribution o files
		path = Paths.get(path, pathString.substring(0, 2),
				pathString.substring(2, 4)).toString();
		log.debug("Path:" + path);
		return path;
	}

	// Calculate the bagName by replacing non-chars with _ (e.g. the ,:/ chars
	// in our normal tag ids)
	public static String getBagNameRoot(String id) {
		return BagGenerator.getValidName(id);
	}

	// Get the description file or trigger its generation
	private File getDescFile(String id) throws ZipException, IOException {
		String path = getDataPathTo(id);
		String bagNameRoot = getBagNameRoot(id);
		File descFile = new File(path, bagNameRoot + ".desc.json");
		if (!descFile.exists()) {
			final InputStream roInputStream = new FileInputStream(
					getOREMapFile(id));
			File indexFile = new File(path, bagNameRoot + ".index.json");
			generateIndex(roInputStream, descFile, indexFile);
			IOUtils.closeQuietly(roInputStream);
			log.debug("Created desc/index files");
		} else {
			log.trace("Desc and Index exist");
		}
		return descFile;

	}

	// Get the index file or trigger its generation
	private File getIndexFile(String id) throws ZipException, IOException {
		String path = getDataPathTo(id);
		String bagNameRoot = getBagNameRoot(id);
		File indexFile = new File(path, bagNameRoot + ".index.json");
		if (!indexFile.exists()) {
			final InputStream roInputStream = new FileInputStream(
					getOREMapFile(id));
			File descFile = new File(path, bagNameRoot + ".desc.json");
			generateIndex(roInputStream, descFile, indexFile);
			IOUtils.closeQuietly(roInputStream);
		} else {
			log.trace("Desc and Index exist");
		}
		return indexFile;

	}

	private JsonNode getAggregation(String id, File indexFile,
			CountingInputStream cis, boolean withChildren, Long oreFileSize)
			throws JsonParseException, JsonMappingException, IOException {
		log.debug("Getting Aggregation");

		long curPos = 0;

		// Always need to generate these
		ArrayList<String> entries = new ArrayList<String>();
		ArrayList<Long> offsets = new ArrayList<Long>();

		FileInputStream fis = new FileInputStream(indexFile);
		JsonFactory f = new MappingJsonFactory();
		JsonParser jp = f.createParser(fis);

		JsonToken current;
		log.debug("Reading Index file");
		current = jp.nextToken(); // Start object

		while ((current = jp.nextToken()) != null) {
			if (current.equals(JsonToken.FIELD_NAME)) {
				String fName = jp.getText();
				current = jp.nextToken(); // Get to start of
				// value
				long offset = jp.getLongValue();
				log.trace("Adding: " + fName + " : " + offset);
				entries.add(fName);
				offsets.add(offset);
			}
		}
		IOUtils.closeQuietly(fis);

		File descFile = getDescFile(id);
		InputStream is = new FileInputStream(descFile);
		ObjectNode resultNode = (ObjectNode) mapper.readTree(is);
		IOUtils.closeQuietly(is);

		log.trace(resultNode.toString());
		if ((resultNode.has("Has Part")) && withChildren) {

			resultNode = getChildren(resultNode, indexFile, cis, oreFileSize,
					curPos, entries, offsets);
		} else {
			resultNode.remove("aggregates");
		}
		log.debug("Aggregation retrieved");
		return resultNode;
	}

	// Get the first item, before the entries and offsets lists are created
	// (they are used to get children efficiently)
	private JsonNode getItem(String item, File indexFile,
			CountingInputStream cis, boolean withChildren, long oreFileSize)
			throws JsonParseException, JsonMappingException, IOException {
		return getItem(item, indexFile, cis, withChildren, oreFileSize, 0,
				null, null);
	}

	// Get an item as a child using the existing (if not null) entries and
	// offset lists
	private JsonNode getItem(String item, File indexFile,
			CountingInputStream cis, boolean withChildren, Long oreFileSize,
			long curOffset, ArrayList<String> entries, ArrayList<Long> offsets)
			throws JsonParseException, JsonMappingException, IOException {
		log.trace("Getting: " + item + " with starting offset: " + curOffset);

		long curPos = curOffset;

		if ((entries == null) || (offsets == null)) {
			entries = new ArrayList<String>();
			offsets = new ArrayList<Long>();

			FileInputStream fis = new FileInputStream(indexFile);
			JsonFactory f = new MappingJsonFactory();
			JsonParser jp = f.createParser(fis);

			JsonToken current;
			log.trace("Reading Index file");
			current = jp.nextToken(); // Start object

			while ((current = jp.nextToken()) != null) {
				if (current.equals(JsonToken.FIELD_NAME)) {
					String fName = jp.getText();
					current = jp.nextToken(); // Get to start of
					// value
					long offset = jp.getLongValue();
					log.trace("Adding: " + fName + " : " + offset);
					entries.add(fName);
					offsets.add(offset);
				}
			}
			try {
				fis.close();
			} catch (Exception e) {
				log.debug(e.getMessage());
			}

		}

		byte[] b = null;
		int bytesRead = 0;

		int index = entries.indexOf(item);
		if (index == -1) {
			log.warn(item + " not in index");
		}
		// getSizeEstimateFor(index)
		int estSize;
		if (index < offsets.size() - 1) {
			estSize = (int) (offsets.get(index + 1) - offsets.get(index));
		} else {
			estSize = (int) (oreFileSize - offsets.get(index));
		}
		curPos += skipTo(cis, curPos, offsets.get(index));
		log.trace("Current Pos updated to : " + curPos);
		b = new byte[estSize];
		bytesRead = cis.read(b);
		log.trace("Read " + bytesRead + " bytes");
		if (bytesRead == estSize) {
			log.trace("Read: " + new String(b));
			InputStream is = new ByteArrayInputStream(b);
			// mapper seems to be OK ignoring a last char such as a comma after
			// the object/tree
			ObjectNode resultNode = (ObjectNode) mapper.readTree(is);
			try {
				is.close();
			} catch (Exception e) {
				log.debug(e.getMessage());
			}

			curPos += bytesRead;
			log.trace("curPos: " + curPos + " : count: " + cis.getByteCount());

			log.trace(resultNode.toString());
			if ((resultNode.has("Has Part")) && withChildren) {
				resultNode = getChildren(resultNode, indexFile, cis,
						oreFileSize, curPos, entries, offsets);
			} else {
				resultNode.remove("aggregates");
			}
			/*
			 * if (args[2] != null) { long offset2 = Long.parseLong(args[2]);
			 * sbc.position(offset2); b.clear(); sbc.read(b);
			 * 
			 * InputStream is2 = new ByteArrayInputStream(b.array());
			 * 
			 * JsonNode node2 = mapper.readTree(is2);
			 * System.out.println(node2.toString()); is2.close(); }
			 */
			return resultNode;
		} else {
			return null;
		}

	}

	// Get all direct child nodes
	private ObjectNode getChildren(ObjectNode resultNode, File indexFile,
			CountingInputStream cis, Long oreFileSize, long curPos,
			ArrayList<String> entries, ArrayList<Long> offsets)
			throws JsonParseException, JsonMappingException, IOException {

		ArrayList<String> childIds = new ArrayList<String>();
		JsonNode children = resultNode.get("Has Part");
		if (children.isArray()) {
			for (JsonNode child : children) {
				childIds.add(child.textValue());
			}
		} else {
			System.out.println("Has Part not an array");
			childIds.add(children.textValue());
		}
		ArrayNode aggregates = mapper.createArrayNode();
		for (String name : childIds) {
			aggregates.add(getItem(name, indexFile, cis, false, oreFileSize,
					curPos, entries, offsets));
			curPos = cis.getByteCount();
			log.trace("curPos updated to " + curPos + " after reading: " + name);

		}
		log.trace("Child Ids: " + childIds.toString());
		resultNode.set("aggregates", aggregates);
		return resultNode;

	}

	// Skip forward as needed through the oremap to find the next child
	// FixMe - it is not required that AgggegatedResources in the oremap are in
	// the same relative order as they are listed in the dcterms:hasPart
	// list. If backwards skips are seen, we need to order the children
	// according to their relative offsets before attempting to retrieve them.
	private static long skipTo(CountingInputStream cis, long curPos, Long long1)
			throws IOException {
		log.trace("Skipping to : " + long1.longValue());
		long offset = long1.longValue() - curPos;
		if (offset < 0) {
			log.error("Backwards jump attempted");
			throw new IOException("Backward Skip: failed");
		}
		log.trace("At: " + curPos + " going forward by " + offset);
		long curskip = 0;
		while (curskip < offset) {
			long inc = cis.skip(offset - curskip);
			if (inc == -1) {
				log.error("End of Stream");
				throw new IOException("End of Stream");
			}
			curskip += inc;
		}
		return offset;
	}

	// Create the index file by parsing the oremap
	protected static void generateIndex(InputStream ro, File descFile,
			File indexFile) throws JsonParseException, IOException {

		log.debug("Generating desc and index files");
		JsonFactory f = new MappingJsonFactory(); // reading
		JsonParser jp = f.createParser(ro);

		JsonGenerator generator = new JsonFactory().createGenerator(descFile,
				JsonEncoding.UTF8);

		JsonToken current;

		current = jp.nextToken();

		report(jp, current);
		while ((current = jp.nextToken()) != null) {
			if (current.equals(JsonToken.FIELD_NAME)) {
				String fName = jp.getText();
				if (fName.equals("describes")) {
					log.trace("describes");
					while (((current = jp.nextToken()) != null)) {
						if (jp.isExpectedStartObjectToken()) {
							generator.setCodec(new ObjectMapper());
							generator.useDefaultPrettyPrinter();

							generator.writeStartObject();

							while (((current = jp.nextToken()) != JsonToken.END_OBJECT)) {
								if (current != JsonToken.FIELD_NAME) {
									log.warn("Unexpected Token!");
									report(jp, current);

								} else {
									report(jp, current);
									String name = jp.getText();
									current = jp.nextToken(); // Get to start of
																// value
									if (!name.equals("aggregates")) {
										log.trace("Writing: " + name);
										generator.writeFieldName(name);
										generator.writeTree(jp
												.readValueAsTree());
									} else {
										report(jp, current);
										log.trace("Skipping?");
										if (current.isStructStart()) {
											indexChildren(indexFile, jp);
											// jp.skipChildren();
										} else {
											log.warn("Was Not Struct start!");
										}
										log.trace("Hit aggregates");

									}
								}
							}

							generator.writeEndObject();

							generator.close();
						}
					}
				}
			}
		}
	}

	private static void indexChildren(File index, JsonParser jp)
			throws IOException {

		JsonGenerator generator = new JsonFactory().createGenerator(index,
				JsonEncoding.UTF8);
		generator.useDefaultPrettyPrinter();

		generator.writeStartObject();

		JsonToken cur = jp.nextToken();
		while (cur.equals(JsonToken.START_OBJECT)) {
			long start = jp.getTokenLocation().getByteOffset();
			int depth = 1;
			while (depth > 0) {
				cur = jp.nextToken();
				if (cur.equals(JsonToken.START_OBJECT)) {
					depth++;
				} else if (cur.equals(JsonToken.END_OBJECT)) {
					depth--;
				} else if (cur.equals(JsonToken.FIELD_NAME) && depth == 1) {
					if (jp.getText().equals("@id")) {
						cur = jp.nextToken();

						String vName = jp.getText();
						generator.writeNumberField(vName, start);
					} else {
						report(jp, cur);
					}
				}
			}
			cur = jp.nextToken();
		}
		generator.writeEndObject();
		generator.close();

	}

	// debug output useful in testing parsing
	private static void report(JsonParser jp, JsonToken token) {
		boolean struct = token.isStructStart() || token.isStructEnd();
		try {
			String tag = struct ? token.asString() : jp.getText();
			log.trace("Tag: " + tag);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		long currentOffset = jp.getCurrentLocation().getByteOffset();
		long tokenOffset = jp.getTokenLocation().getByteOffset();
		log.trace("Cur: " + currentOffset + " tok: " + tokenOffset);
	}

}
