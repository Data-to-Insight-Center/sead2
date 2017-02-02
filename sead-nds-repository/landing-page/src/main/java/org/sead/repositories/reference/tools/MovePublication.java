/*
 *
 * Copyright 2017 University of Michigan
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

package org.sead.repositories.reference.tools;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sead.nds.repository.Repository;
import org.sead.repositories.reference.RefRepository;

public class MovePublication {

	private static final Logger log = Logger.getLogger(MovePublication.class);

	private static String similarTo = "similarTo";
	private static Map<String, String> pidMap = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: MovePublication <id> <new base URL>");
			System.out
					.println("<id> should correspond to the name of a local zip file");
			System.exit(0);

		}

		String id = args[0];
		String base = args[1];
		log.info("Moving Publication: " + id + " to server " + base);
		System.out.println("Moving Publication: " + id + " to server " + base);

		JSONObject oremap = null;

		String bagNameRoot = RefRepository.getBagNameRoot(id);
		File result = new File(bagNameRoot + ".zip");
		ZipFile zf = null;
		try {
			zf = new ZipFile(result);
			InputStream oreIS = null;
			ZipEntry archiveEntry1 = zf.getEntry(bagNameRoot
					+ "/oremap.jsonld.txt");

			if (archiveEntry1 != null) {
				oreIS = new BufferedInputStream(
						zf.getInputStream(archiveEntry1));
				oremap = new JSONObject(IOUtils.toString(oreIS, "UTF-8"));
			}
			InputStream pidIS = null;
			ZipEntry archiveEntry2 = zf.getEntry(bagNameRoot
					+ "/pid-mapping.txt");
			if (archiveEntry2 != null) {
				pidIS = new BufferedInputStream(
						zf.getInputStream(archiveEntry2));
				pidMap = readPidMap(pidIS);
			}

			IOUtils.closeQuietly(oreIS);
			IOUtils.closeQuietly(pidIS);

		} catch (IOException e) {
			log.warn("Can't find entries: ", e);
			IOUtils.closeQuietly(zf);
		}

		/*
		 * Now, scan oremap for similarTo entries and map them to new URLs For
		 * collections - leave as is For datasets (type
		 * http://cet.ncsa.uiuc.edu/2007/Dataset or
		 * http://cet.ncsa.uiuc.edu/2015/File ver):
		 */

		JSONArray aggregates = oremap.getJSONObject("describes").getJSONArray(
				"aggregates");
		for (int i = 0; i < aggregates.length(); i++) {
			JSONObject resource = aggregates.getJSONObject(i);
			if (isData(resource)) {
				resource.put(similarTo, newLocation(resource, base));
			}
		}

		// Now write result back to zip
		/*
		 * File newORE = new File("test.ore.txt"); FileWriter fw = null; try {
		 * fw = new FileWriter(newORE); fw.write(oremap.toString(2)); } catch
		 * (JSONException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } catch (IOException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); } IOUtils.closeQuietly(fw);
		 */

		File dir = unzipTree("./temp", result);
		String oreFilePath = "./temp/" + bagNameRoot + "/oremap.jsonld.txt";
		File map = new File(oreFilePath);
		map.delete();
		File newORE = new File(oreFilePath);
		FileWriter fw = null;
		try {
			fw = new FileWriter(newORE);
			fw.write(oremap.toString(2));
		} catch (JSONException e) { // TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		IOUtils.closeQuietly(fw);
		Properties p = new Properties();
		p.put("repo.datapath", "./final/");
		Repository.init(p);
		String path = RefRepository.getDataPathTo(id);
		new File(path).mkdirs();

		try {
			zipTree(path, dir);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File temp = new File("./temp/" + result);
		try {
			FileUtils.deleteDirectory(temp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void zipTree(String outPath, File dir) throws Exception {

		File topDir = dir.listFiles()[0];
		File outFile = new File(outPath, topDir.getName() + ".zip");
		ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(
				outFile);
		addDir(topDir, "", zipOutputStream);

		zipOutputStream.finish();
		zipOutputStream.close();

	}

	private static void addDir(File dir, String relPath,
			ZipArchiveOutputStream zipOutputStream) throws IOException {
		File[] files = dir.listFiles();
		if (relPath.length() != 0) {
			relPath = relPath + "/" + dir.getName();
		} else {
			relPath = dir.getName();
		}

		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				addDir(files[i], relPath, zipOutputStream);
				continue;
			}
			System.out
					.println(" Adding: " + relPath + "/" + files[i].getName());
			ZipArchiveEntry entry = new ZipArchiveEntry(files[i], relPath + "/"
					+ files[i].getName());

			zipOutputStream.putArchiveEntry(entry);
			IOUtils.copy(new FileInputStream(files[i]), zipOutputStream);
			zipOutputStream.closeArchiveEntry();
		}

	}

	static File unzipTree(String outPath, File file) {
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(file);

			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				File entryDestination = new File(outPath, entry.getName());
				if (entry.isDirectory()) {
					entryDestination.mkdirs();
				} else {
					entryDestination.getParentFile().mkdirs();
					InputStream in = zipFile.getInputStream(entry);
					OutputStream out = new FileOutputStream(entryDestination);
					IOUtils.copy(in, out);
					IOUtils.closeQuietly(in);
					out.close();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(zipFile);
		}
		return new File(outPath);
	}

	private static String newLocation(JSONObject resource, String base) {
		/*
		 * SDA-style: URLs contain /resteasy/researchobjects/<RO
		 * ID>/files/<URLencoded live ID>?pubtoken=<key> Map to
		 * base/api/researchobjecs/<RO ID>/<path starting at data/...>
		 * 
		 * RefRepoStyle - just replace base before the /api/researchobjects
		 * ...// TODO Auto-generated method stub
		 */
		String location = resource.getString(similarTo);
		String newLocation = location; // Default - leave as is.
		if (location.contains("/resteasy/researchobjects/")) {
			// SDA style
			String ro_id = location.substring(location
					.indexOf("/resteasy/researchobjects/")
					+ "/resteasy/researchobjects/".length());
			String id = ro_id;
			ro_id = ro_id.substring(0, ro_id.indexOf("/"));
			id = id.substring(id.indexOf("/files/") + "/files/".length());
			id = id.substring(0, id.indexOf("?pubtoken"));
			try {
				id = URLDecoder.decode(id, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(ro_id);
			System.out.println(id);
			String path = pidMap.get(id);
			path = path.substring(path.indexOf("/data"));
			newLocation = (base + "/api/researchobjects/" + ro_id + path);
		} else if (location.contains("/api/researchobjects/")) {
			// Ref style
			newLocation = base + location.substring(location.indexOf("/api/researchobjects/"));
		}
		return newLocation;
	}

	static String seadDataType = "http://cet.ncsa.uiuc.edu/2007/Dataset";
	static String sead2DataType = "http://cet.ncsa.uiuc.edu/2015/File";

	private static boolean isData(JSONObject resource) {
		boolean isData = false;
		Object type = resource.get("@type");
		if (type != null) {
			if (type instanceof JSONArray) {
				for (int j = 0; j < ((JSONArray) type).length(); j++) {
					String theType = ((JSONArray) type).getString(j);
					if (theType.equals(sead2DataType)
							|| theType.equals(seadDataType)) {
						isData = true;
					}
				}
			}
		}
		return isData;
	}

	private static Map<String, String> readPidMap(InputStream is) {
		Map<String, String> pidMap = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		try {
			line = br.readLine();
			while (line != null) {
				int firstSpace = line.indexOf(' ');
				String id = line.substring(0, firstSpace);
				if (id.contains("/")) {
					id = id.substring(0, id.lastIndexOf("/"));
				}
				String path = line.substring(firstSpace + 1);

				pidMap.put(id, path);

				line = br.readLine();
			}
		} catch (IOException e) {
			log.warn("Error reading ID to path info from pid-mapping.txt file: "
					+ e.getLocalizedMessage());
			e.printStackTrace();
		}
		return pidMap;
	}

}
