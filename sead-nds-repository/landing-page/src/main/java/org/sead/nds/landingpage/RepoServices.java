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
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.sead.nds.repository.Repository;

/**
 * EzidService is a helper class that enables to create and update DOIs using
 * the EZID service.
 *
 */

@Path("/")
public class RepoServices {

	private static final Logger log = Logger.getLogger(RepoServices.class);
	
	public RepoServices() {
		log.debug("Repo Services Created");
	}

	/*
	 * @Path("/researchobjects") GET ?filter= {published, failed, pending}, PUT
	 * 
	 * @Path("/requests") GET
	 */

	@Path("/researchobjects/{id}")
	@Produces(MediaType.TEXT_HTML)
	@GET
	public Response getLandingPage(@PathParam(value = "id") String id) {
		URI landingPage=null;
		try {
			landingPage = new URI("../landing.html#" + URLEncoder.encode(id, "UTF-8"));
			
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.debug("Referring to : " + landingPage.toString());
		return Response.temporaryRedirect(landingPage).build();

	}

	@Path("/researchobjects/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public Response getAggregation(@PathParam(value = "id") String id) {

		String path = Repository.getDataPath();

		String pathString = DigestUtils.sha1Hex(id);

		// Two level hash-based distribution o files
		path = Paths.get(path, pathString.substring(0, 2),
				pathString.substring(2, 4)).toString();
		log.debug("Path:" + path);
		
		String bagNameRoot = id.replaceAll("\\W+", "_");
		log.debug(bagNameRoot);
		File result = new File(path, bagNameRoot + ".zip");
		try {
			final ZipFile zf = new ZipFile(result);
			log.debug(bagNameRoot
					+ "/oremap.jsonld.txt");
			ZipArchiveEntry archiveEntry1 = zf.getEntry(bagNameRoot
					+ "/oremap.jsonld.txt");
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

	@Path("/researchobjects/{id}/data/{relpath}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public Response getDatafile(@PathParam(value = "id") String id, @PathParam (value="relpath") String datapath) {

		String path = Repository.getDataPath();

		String pathString = DigestUtils.sha1Hex(id);

		// Two level hash-based distribution o files
		path = Paths.get(path, pathString.substring(0, 2),
				pathString.substring(2, 4)).toString();
		log.debug("Path:" + path);
		
		String bagNameRoot = id.replaceAll("\\W+", "_");
		log.debug(bagNameRoot);
		File result = new File(path, bagNameRoot + ".zip");
		try {
			final ZipFile zf = new ZipFile(result);
			
			
			log.debug(bagNameRoot
					+ "/data/" + datapath);
			ZipArchiveEntry archiveEntry1 = zf.getEntry(bagNameRoot
					+ "/data/" + datapath);
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

	@Path("/researchobjects/{id}/meta/{relpath}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public Response getMetadatafile(@PathParam(value = "id") String id, @PathParam (value="relpath") String metadatapath) {

		String path = Repository.getDataPath();

		String pathString = DigestUtils.sha1Hex(id);

		// Two level hash-based distribution o files
		path = Paths.get(path, pathString.substring(0, 2),
				pathString.substring(2, 4)).toString();
		log.debug("Path:" + path);
		
		String bagNameRoot = id.replaceAll("\\W+", "_");
		log.debug(bagNameRoot);
		File result = new File(path, bagNameRoot + ".zip");
		try {
			final ZipFile zf = new ZipFile(result);
			
			
			log.debug(bagNameRoot
					+ "/" + metadatapath);
			ZipArchiveEntry archiveEntry1 = zf.getEntry(bagNameRoot
					+ "/" + metadatapath);
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

		String path = Repository.getDataPath();

		String pathString = DigestUtils.sha1Hex(id);

		// Two level hash-based distribution o files
		path = Paths.get(path, pathString.substring(0, 2),
				pathString.substring(2, 4)).toString();
		log.debug("Path:" + path);
		
		String bagNameRoot = id.replaceAll("\\W+", "_");
		log.debug(bagNameRoot);
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

	/*
	 * @PUT
	 * 
	 * @Path("/")
	 * 
	 * @Consumes(MediaType.APPLICATION_JSON)
	 * 
	 * @Produces(MediaType.APPLICATION_JSON) public Response setROStatus(String
	 * doiInfo) { boolean permanent = false; HashMap<String, String> metadataMap
	 * = new HashMap<String, String>(); try {
	 * 
	 * JSONObject doiInfoObj = new JSONObject(doiInfo); if
	 * (!doiInfoObj.has("target")) { return Response
	 * .status(ClientResponse.Status.BAD_REQUEST) .entity(new
	 * JSONObject().put("Failure", "target not specified").toString()).build();
	 * }
	 * 
	 * String targetUrl = doiInfoObj.get("target").toString();
	 * metadataMap.put(InternalProfile.TARGET.toString(), targetUrl);
	 * 
	 * if (doiInfoObj.has("permanent") &&
	 * doiInfoObj.get("permanent").toString().equals("true")) { permanent =
	 * true; } String metadata = doiInfoObj.has("metadata") ? doiInfoObj.get(
	 * "metadata").toString() : ""; metadataMap.putAll(getTranslatedTerms(new
	 * JSONObject(metadata))); // An RO is generically a datacite collection
	 * metadataMap.put(DataCiteProfile.RESOURCE_TYPE.toString(),
	 * DataCiteProfileResourceTypeValues.COLLECTION.toString());
	 * 
	 * EZIDService ezid = new EZIDService(Constants.ezid_url);
	 * 
	 * String shoulder = (permanent) ? Constants.doi_shoulder_prod :
	 * Constants.doi_shoulder_test; String doi_url = null; try {
	 * ezid.login(Constants.doi_username, Constants.doi_password);
	 * 
	 * doi_url = ezid.mintIdentifier(shoulder, metadataMap); } catch
	 * (EZIDException e) { // null value will trigger error message
	 * e.printStackTrace(); } if (doi_url != null) {
	 * System.out.println(RepoServices.class.getName() +
	 * " : DOI created Successfully - " + doi_url); return Response.ok( new
	 * JSONObject().put("doi", doi_url).toString()) .build(); } else {
	 * System.out.println(RepoServices.class.getName() +
	 * " : Error creating DOI "); return Response
	 * .status(ClientResponse.Status.INTERNAL_SERVER_ERROR) .entity(new
	 * JSONObject().put("Failure", "Error occurred while generating DOI")
	 * .toString()).build(); } } catch (JSONException e) { e.printStackTrace();
	 * return Response.status(ClientResponse.Status.BAD_REQUEST).build(); } }
	 */
}
