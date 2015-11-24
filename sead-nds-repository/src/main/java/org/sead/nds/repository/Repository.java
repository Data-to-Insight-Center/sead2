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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import javax.activation.MimeType;
import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.zip.ParallelScatterZipCreator;
import org.apache.commons.compress.archivers.zip.ScatterZipOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntryRequest;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.parallel.InputStreamSupplier;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sead.nds.repository.util.ConsoleStatusReceiver;
import org.sead.nds.repository.util.StatusReceiver;

import edu.ucsb.nceas.ezid.EZIDException;
import edu.ucsb.nceas.ezid.EZIDService;
import edu.ucsb.nceas.ezid.profile.DataCiteProfile;
import edu.ucsb.nceas.ezid.profile.InternalProfile;

public class Repository {

	private static final Logger log = Logger.getLogger(Repository.class);
	private static String repoID = null;
	private static Properties props = new Properties();
	private static String dataPath=null;

	static {
		try {
			props.load(Repository.class
					.getResourceAsStream("repository.properties"));
			System.out.println(props.toString());
		} catch (IOException e) {
			log.warn("Could not read repositories.properties file");
		}
		repoID=props.getProperty("repo.ID", "bob");
		dataPath=props.getProperty("repo.datapath", "./test2");
		
	}
	
	public Repository() {
	}

	public static void main(String[] args) {
		PropertyConfigurator.configure("./log4j.properties");
		
		if (args.length == 1) {
			BagGenerator bg;
			bg = new BagGenerator(args[0]);
			//FixMe - use repo.ID from properties file (possibly in repo class
			bg.generateBag(new ConsoleStatusReceiver(repoID));
		} else {
			System.out.println("Usage: <oremap URL>");
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

}
