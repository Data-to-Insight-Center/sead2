package org.sead.repositories.reference.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.zip.ZipException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.log4j.Logger;
import org.sead.nds.repository.util.LocalContentProvider;
import org.sead.repositories.reference.RefRepository;

public class RefLocalContentProvider extends LocalContentProvider {

	private String oldID;
	// String dataPathString;
	String landingBaseString;

	private ZipFile zf = null;

	private static final Logger log = Logger
			.getLogger(RefLocalContentProvider.class);

	private String hashtype = null;
	private HashMap<String, String> hashToPathMap = new HashMap<String, String>();
	
	public RefLocalContentProvider(String id, Properties props) {
		log.info("Instantiating a local Content Provider based on RO: " + id);
		oldID = id;
		landingBaseString = props.getProperty("repo.landing.base");
		String path = RefRepository.getDataPathTo(id);

		String bagNameRoot = RefRepository.getBagNameRoot(id);
		File result = new File(path, bagNameRoot + ".zip");
		try {
			zf = new ZipFile(result);

			InputStream is = RefRepository.getFileInputStream(zf,
					RefRepository.getBagNameRoot(oldID) + "/manifest-sha1.txt");
			if (is != null) {
				hashtype = "SHA1 Hash";
			} else {
				is = RefRepository.getFileInputStream(zf,
						RefRepository.getBagNameRoot(oldID)
								+ "/manifest-sha512.txt");
				if (is != null) {
					hashtype = "SHA512 Hash";
				}
			}
			if (is != null) {
				readHashMap(is);
				IOUtils.closeQuietly(is);
			}

		} catch (IOException e) {
			log.warn("Can't find local content: ", e);
		}
		if (zf == null) {
			log.error("Could not open zipfile : " + result.getPath());
		}
		log.debug("Hashtype is : " + hashtype);
		log.debug("Found " + hashToPathMap.size() + " entries");
		log.info("Retrieved hash to path mappings for RO: " + id);
	}

	@Override
	protected void finalize() throws Throwable {
		log.debug("Finalizing");
		ZipFile.closeQuietly(zf);
		super.finalize();
	}

	public String getHashType() {
		return hashtype;
	}
	
	private void readHashMap(InputStream is) {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		try {
			line = br.readLine();
			while (line != null) {
				int firstSpace = line.indexOf(' ');
				String hash = line.substring(0, firstSpace);
				String path = line.substring(firstSpace + 1);
				// A couple warnings about duplicates - they don't affect the
				// ability to use the local content though.
				if (hashToPathMap.containsKey(hash)) {
					// Trusting the hash, having any one hash to path in the map
					// is OK - we'll get the right content
					log.warn("Multiple files with the same hash: "
							+ hashToPathMap.get(hash) + " and " + path);
				}
				if (hashToPathMap.containsValue(path)) {
					// Since we look up by hash, if there are files with the
					// same hash but different paths, we still get the right
					// bytes
					log.warn("Multiple files with tne same path: duplicate hash "
							+ hash);
				}
				hashToPathMap.put(hash, path);

				line = br.readLine();
			}
		} catch (IOException e) {
			log.warn("Error reading hashToPathMap to path info from manifest file: "
					+ e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	public boolean exists(String algorithm, String hash) {
		// If algorithm matches
		if (algorithm.equals(hashtype) && zf != null) {

			// Look up path based on hash
			if (hashToPathMap.containsKey(hash)) {

				String relPath = hashToPathMap.get(hash);
				log.debug("Checking existence of " + relPath);
				ZipArchiveEntry archiveEntry1 = zf.getEntry(relPath);
				if (archiveEntry1 != null) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public InputStream getInputStreamMatchingHash(String algorithm, String hash) {
		// If algorithm matches
		if (algorithm.equals(hashtype) && zf != null) {

			// Look up path based on hash
			if (hashToPathMap.containsKey(hash)) {

				String relPath = hashToPathMap.get(hash);
				log.trace("Retrieving stream for hash: " + hash + "from : "
						+ relPath);

				try {
					return RefRepository.getFileInputStream(zf, relPath);
				} catch (ZipException e) {
					log.error(relPath + " : " + e.getLocalizedMessage());
					e.printStackTrace();
				} catch (IOException e) {
					log.error(relPath + " : " + e.getLocalizedMessage());
					e.printStackTrace();
				}
			}
		}

		return null;
	}
}
