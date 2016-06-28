package org.sead.repositories.reference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.log4j.Logger;
import org.sead.nds.repository.Repository;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

//WebListener works in tomcat7/servlets3 but not in tomcat6/servlets2.5
@WebListener
public class RepoContextListener implements ServletContextListener {

	private static final Logger log = Logger
			.getLogger(RepoContextListener.class);

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// Reads config file from the same dir as this class
		try {
		Repository.init(Repository.loadProperties());
		log.debug("Repo Context Initializing");
		checkPublications();
		log.info("Reference Repository Context Initialized");
		} catch (Exception e) {
			log.error("Exception during Context initiaization: " + e.getLocalizedMessage() );
			e.printStackTrace();
		}
	}

	private void checkPublications() {

		FilenameFilter zipFilter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(".zip")) {

					return true;
				}
				return false;

			}
		};
		String path = Repository.getDataPath();
		File pubRoot = new File(path);
		if (!pubRoot.exists()) {
			log.error("Could not find publication root directory: "
					+ pubRoot.getPath());
		}
		File[] level1 = pubRoot.listFiles();

		for (File f : level1) {
			File[] level2 = f.listFiles();
			if (level2 != null) {
				for (File f2 : level2) {
					String[] zips = f2.list(zipFilter);
					if (zips != null) {
						for (String zip : zips) {
							String bagNameRoot = zip.substring(0,
									zip.length() - 4); // 4 =
														// ".zip".length();
							String bagPathRoot = f2.getPath() + "/"
									+ bagNameRoot;
							File descFile = new File(bagPathRoot + ".desc.json");
							File indexFile = new File(bagPathRoot
									+ ".index.json");
							File mapFile = new File(bagPathRoot
									+ ".oremap.jsonld.txt");
							if (!mapFile.exists()) {
								RefRepository.createMap(mapFile, f2.getPath(),
										bagNameRoot);
							}
							if (!descFile.exists() || !indexFile.exists()) {
								log.info("Creating desc and/or index files: "
										+ bagPathRoot);
								InputStream mapStream = null;
								try {
									mapStream = new FileInputStream(mapFile);
									RefRepository.generateIndex(mapStream,
											descFile, indexFile);
								} catch (FileNotFoundException e) {
									log.error(bagNameRoot + ": "
											+ e.getMessage());
									e.printStackTrace();
								} catch (JsonParseException e) {
									log.error(bagNameRoot + ": "
											+ e.getMessage());
									e.printStackTrace();
								} catch (IOException e) {
									log.error(bagNameRoot + ": "
											+ e.getMessage());
									e.printStackTrace();
								} finally {
									IOUtils.closeQuietly(mapStream);
								}
							}
							if (mapFile.exists() && descFile.exists()
									&& indexFile.exists()) {
								log.info("All cached files for : "
										+ bagPathRoot + " exist");
							}

							ObjectMapper mapper = new ObjectMapper();

							ObjectNode rootNode;
							try {
								rootNode = (ObjectNode) mapper.readValue(
										descFile, JsonNode.class);
								String idString = rootNode.get("Identifier")
										.textValue();
								String correctPath = RefRepository
										.getDataPathTo(idString);
								if (!f2.equals(new File(correctPath))) {
									log.warn("Publication id: " + idString
											+ " is not located in "
											+ correctPath);
								}
							} catch (Exception e) {
								log.error(bagNameRoot + ": " + e.getMessage());
								e.printStackTrace();
							}
						}
					} else {
						log.warn("Extra file: " + f.getPath());
					}
				}
			} else {
				log.warn("Extra file: " + f.getPath());
			}
		}
	}
}
