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

package org.sead.nds.landingpage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.log4j.Logger;
import org.sead.nds.repository.Repository;

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
import java.util.zip.ZipException;

/**
 * RepoServices manages the RESTful interface to the published data packages stored as Zip files. It generates the landing page for a given DOI
 * and, based on the landing URL, finds the corresponding zip and extracts the relevant data/metadata. To help with this, it extracts a short 
 * description file that includes just the top-level description and top-level children from the oremap, and an index defining the offsets, 
 * within the oremap file, for the json description for each AggregatedResource (e.g. a collection or dataset (1.5) or Dataset/File (2.0). 
 *
 */

@Path("/")
public class RepoServices {

	private static final Logger log = Logger.getLogger(RepoServices.class);

	static ObjectMapper mapper = new ObjectMapper();

	public RepoServices() {
		//Reads config file from the same dir as this class
		Repository.init();
		log.debug("Repo Services Created");
	}

	/*
	 * @Path("/researchobjects") 
	 * 
	 * Returns the base landingpage html
	 * 
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
		return Response.temporaryRedirect(landingPage).build();

	}

	/*
	 * @Path("/researchobjects/{id}") 
	 * 
	 * Returns the description file for the Aggregation
	 * @deprecated - /researchobjects/{id}/metadata returns this plus the top level of children - that method is now used instead of this one
	 */

	
	@Path("/researchobjects/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public Response getAggregationSummary(@PathParam(value = "id") String id) {

		File descFile;
		try {
			//get or generate this file
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
	 * Returns the description for the Aggregation (the Aggregation metadata and the descriptions of the AggregatedResources at the 
	 * top-level(direct children listed in 'HasPart')
	 * 
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

		log.debug(bagNameRoot + "/oremap.jsonld.txt");
		ZipFile zf = null;
		try {
			// Check for index files
			File indexFile = getIndexFile(id);

			zf = new ZipFile(result);
			ZipArchiveEntry archiveEntry1 = zf.getEntry(bagNameRoot
					+ "/oremap.jsonld.txt");

			// Find/open base ORE map file
			// Note - limited to maxint size for oremap file size

			final CountingInputStream cis = new CountingInputStream(
					new BufferedInputStream(zf.getInputStream(archiveEntry1),
							Math.min((int) archiveEntry1.getSize(), 1000000)));

			JsonNode resultNode = getAggregation(id, indexFile, cis, true,
					archiveEntry1.getSize());
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
			if (zf != null) {
				try {
					zf.close();
				} catch (IOException io) {
					log.warn("IO error on zf close: " + io.getMessage());
				}
			}
		}
	}
	/*
	 * @Path("/researchobjects/{id}/metadata/{did}") 
	 * 
	 * Returns the description for the AggregationResource within the {id} Aggregation 
	 * (the AggregatedResource metadata and the descriptions of the AggregatedResources directly within it
	 * (direct children listed in 'HasPart'))
	 * 
	 */
	
	@Path("/researchobjects/{id}/metadata/{did}")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public Response getResourceSummary(@PathParam(value = "id") String id,
			@PathParam(value = "did") String dataID) {

		String path = getDataPathTo(id);
		String bagNameRoot = getBagNameRoot(id);

		File result = new File(path, bagNameRoot + ".zip");
		if (!result.exists()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		ZipFile zf = null;
		try {
			File indexFile = getIndexFile(id);

			zf = new ZipFile(result);
			ZipArchiveEntry archiveEntry1 = zf.getEntry(bagNameRoot
					+ "/oremap.jsonld.txt");

			// Find/open base ORE map file
			// Note - limited to maxint size for oremap file size

			final CountingInputStream cis = new CountingInputStream(
					new BufferedInputStream(zf.getInputStream(archiveEntry1),
							Math.min((int) archiveEntry1.getSize(), 1000000)));

			JsonNode resultNode = getItem(dataID, indexFile, cis, true,
					archiveEntry1.getSize());
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
			if (zf != null) {
				try {
					zf.close();
				} catch (IOException io) {
					log.warn("IO err on zf close: " + io.getMessage());
				}
			}
		}
	}

	/*
	 * @Path("/researchobjects/{id}/data/{relpath}") 
	 * 
	 * Returns the data file (any file within the /data directory) at the given path within the {id} publication
	 * 
	 */
	
	@Path("/researchobjects/{id}/data/{relpath}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public Response getDatafile(@PathParam(value = "id") String id,
			@PathParam(value = "relpath") String datapath) {

		String path = getDataPathTo(id);
		String bagNameRoot = getBagNameRoot(id);

		File result = new File(path, bagNameRoot + ".zip");
		try {
			final ZipFile zf = new ZipFile(result);

			log.debug(bagNameRoot + "/data/" + datapath);
			ZipArchiveEntry archiveEntry1 = zf.getEntry(bagNameRoot + "/data/"
					+ datapath);
			final InputStream inputStream = zf.getInputStream(archiveEntry1);

			StreamingOutput stream = new StreamingOutput() {
				public void write(OutputStream os) throws IOException,
						WebApplicationException {
					IOUtils.copy(inputStream, os);
					zf.close();
				}
			};

			return Response.ok(stream).build();
		} catch (IOException e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
	}

	/*
	 * @Path("/researchobjects/{id}/meta/{relpath}") 
	 * 
	 * Returns the metadata file (a file not in the /data dir) at the given path within the {id} publication
	 * 
	 */

	
	@Path("/researchobjects/{id}/meta/{relpath}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public Response getMetadatafile(@PathParam(value = "id") String id,
			@PathParam(value = "relpath") String metadatapath) {

		String path = getDataPathTo(id);
		String bagNameRoot = getBagNameRoot(id);

		File result = new File(path, bagNameRoot + ".zip");
		try {
			final ZipFile zf = new ZipFile(result);

			log.debug(bagNameRoot + "/" + metadatapath);
			//Don't let this call be used to get data from the data dir
			if(metadatapath.startsWith("data")|| metadatapath.startsWith("/data")) {
				return Response.status(Status.BAD_REQUEST).build();
			}
			ZipArchiveEntry archiveEntry1 = zf.getEntry(bagNameRoot + "/"
					+ metadatapath);
			final InputStream inputStream = zf.getInputStream(archiveEntry1);

			StreamingOutput stream = new StreamingOutput() {
				public void write(OutputStream os) throws IOException,
						WebApplicationException {
					IOUtils.copy(inputStream, os);
					zf.close();
				}
			};

			return Response.ok(stream).build();
		} catch (IOException e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
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

	//Calculate the path to the zip in the file system based in the base path and the 2 level hash subdurectory scheme
	private String getDataPathTo(String id) {
		String pathString = DigestUtils.sha1Hex(id);
		String path = Repository.getDataPath();
		// Two level hash-based distribution o files
		path = Paths.get(path, pathString.substring(0, 2),
				pathString.substring(2, 4)).toString();
		log.debug("Path:" + path);
		return path;
	}

	//Calculate the bagName by replacing non-chars with _ (e.g. the ,:/ chars in our normal tag ids) 
	private String getBagNameRoot(String id) {
		String bagNameRoot = id.replaceAll("\\W+", "_");
		log.debug(bagNameRoot);
		return bagNameRoot;
	}

	//Get the description file or trigger its generation
	private File getDescFile(String id) throws ZipException, IOException {
		String path = getDataPathTo(id);
		String bagNameRoot = getBagNameRoot(id);
		File descFile = new File(path, bagNameRoot + ".desc.json");
		if (!descFile.exists()) {
			File result = new File(path, bagNameRoot + ".zip");

			final ZipFile zf = new ZipFile(result);
			ZipArchiveEntry archiveEntry1 = zf.getEntry(bagNameRoot
					+ "/oremap.jsonld.txt");

			final InputStream roInputStream = zf.getInputStream(archiveEntry1);
			File indexFile = new File(path, bagNameRoot + ".index.json");

			generateIndex(roInputStream, descFile, indexFile);
			ZipFile.closeQuietly(zf);
		} else {
			log.debug("Desc and Index exist");
		}
		return descFile;

	}

	//Get the index file or trigger its generation
	private File getIndexFile(String id) throws ZipException, IOException {
		String path = getDataPathTo(id);
		String bagNameRoot = getBagNameRoot(id);
		File indexFile = new File(path, bagNameRoot + ".index.json");

		if (!indexFile.exists()) {
			File result = new File(path, bagNameRoot + ".zip");

			final ZipFile zf = new ZipFile(result);
			ZipArchiveEntry archiveEntry1 = zf.getEntry(bagNameRoot
					+ "/oremap.jsonld.txt");

			final InputStream roInputStream = zf.getInputStream(archiveEntry1);
			File descFile = new File(path, bagNameRoot + ".desc.json");

			generateIndex(roInputStream, descFile, indexFile);
			ZipFile.closeQuietly(zf);
		} else {
			log.debug("Desc and Index exist");
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
		try {
			fis.close();
		} catch (Exception e) {
			log.debug(e.getMessage());
		}

		File descFile = getDescFile(id);
		InputStream is = new FileInputStream(descFile);
		ObjectNode resultNode = (ObjectNode) mapper.readTree(is);
		try {
			is.close();
		} catch (Exception e) {
			log.debug(e.getMessage());
		}

		log.debug(resultNode.toString());
		if ((resultNode.has("Has Part")) && withChildren) {

			resultNode = getChildren(resultNode, indexFile, cis, oreFileSize,
					curPos, entries, offsets);
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
	}

	//Get the first item, before the entries and offsets lists are created (they are used to get children efficiently) 
	private JsonNode getItem(String item, File indexFile,
			CountingInputStream cis, boolean withChildren, long oreFileSize)
			throws JsonParseException, JsonMappingException, IOException {
		return getItem(item, indexFile, cis, withChildren, oreFileSize, 0,
				null, null);
	}
	
	//Get an item as a child using the existing (if not null) entries and offset lists
	private JsonNode getItem(String item, File indexFile,
			CountingInputStream cis, boolean withChildren, Long oreFileSize,
			long curOffset, ArrayList<String> entries, ArrayList<Long> offsets)
			throws JsonParseException, JsonMappingException, IOException {
		log.debug("Getting: " + item + " with starting offset: " + curOffset);

		long curPos = curOffset;

		if ((entries == null) || (offsets == null)) {
			entries = new ArrayList<String>();
			offsets = new ArrayList<Long>();

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
		log.debug("Current Pos updated to : " + curPos);
		b = new byte[estSize];
		bytesRead = cis.read(b);
		log.debug("Read " + bytesRead + " bytes");
		if (bytesRead == estSize) {
			log.debug("Read: " + new String(b));
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
			log.debug("curPos: " + curPos + " : count: " + cis.getByteCount());

			log.debug(resultNode.toString());
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

	//Get all direct child nodes
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
			log.debug("curPos updated to " + curPos + " after reading: " + name);

		}
		log.debug("Child Ids: " + childIds.toString());
		resultNode.set("aggregates", aggregates);
		return resultNode;

	}

	//Skip forward as needed through the oremap to find the next child
	//FixMe - it is not required that AgggegatedResources in the oremap are in the same relative order as they are listed in the dcterms:hasPart
	// list. If backwards skips are seen, we need to order the children according to their relative offsets before attempting to retrieve them.
	private static long skipTo(CountingInputStream cis, long curPos, Long long1)
			throws IOException {
		log.debug("Skipping to : " + long1.longValue());
		long offset = long1.longValue() - curPos;
		if (offset < 0) {
			log.error("Backwards jump attempted");
			throw new IOException("Backward Skip: failed");
		}
		log.debug("At: " + curPos + " going forward by " + offset);
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

	//Create the index file by parsing the oremap
	private static void generateIndex(InputStream ro, File descFile,
			File indexFile) throws JsonParseException, IOException {

		JsonFactory f = new MappingJsonFactory(); // reading
		JsonParser jp = f.createParser(ro);
		/*
		 * JsonNode node = jp.readValueAsTree(); if (node.isArray()) { int i =
		 * 0; while (node.has(i)) { JsonNode child = node.get(i);
		 * System.out.println(child.get("field1").asText());
		 * System.out.println(child.get("field2").asText()); i++; } }
		 * System.out.println(node.asText());
		 */

		JsonGenerator generator = new JsonFactory().createGenerator(descFile,
				JsonEncoding.UTF8);

		JsonToken current;

		current = jp.nextToken();

		report(jp, current);
		while ((current = jp.nextToken()) != null) {
			if (current.equals(JsonToken.FIELD_NAME)) {
				String fName = jp.getText();
				if (fName.equals("describes")) {
					log.debug("describes");
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
										log.debug("Writing: " + name);
										generator.writeFieldName(name);
										generator.writeTree(jp
												.readValueAsTree());
									} else {
										report(jp, current);
										log.debug("Skipping?");
										if (current.isStructStart()) {
											indexChildren(indexFile, jp);
											// jp.skipChildren();
										} else {
											log.warn("Was Not Struct start!");
										}
										log.debug("Hit aggregates");

									}
								}
							}

							generator.writeEndObject();

							generator.close();
						}

						/*
						 * if ((!(current.equals(JsonToken.FIELD_NAME))) ||
						 * (!(jp.getText().equals("aggregates")))) {
						 * 
						 * report(jp, current); } else { current =
						 * jp.nextToken(); report(jp, current);
						 * System.out.println("Skipping?"); if
						 * (current.isStructStart()) { jp.skipChildren(); }
						 * System.out.println("Hit aggregates"); }
						 */
						// report(jp, current);
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

	//debug output useful in testing parsing
	private static void report(JsonParser jp, JsonToken token) {
		boolean struct = token.isStructStart() || token.isStructEnd();
		try {
			String tag = struct ? token.asString() : jp.getText();
			log.debug("Tag: " + tag);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		long currentOffset = jp.getCurrentLocation().getByteOffset();
		long tokenOffset = jp.getTokenLocation().getByteOffset();
		log.debug("Cur: " + currentOffset + " tok: " + tokenOffset);
	}

}
