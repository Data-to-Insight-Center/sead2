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

package org.sead.repositories.reference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.sead.nds.repository.C3PRPubRequestFacade;
import org.sead.repositories.reference.util.SEADAuthenticator;

import com.sun.net.httpserver.HttpContext;

/**
 * This class overrides the method to retrieve a local pub request (allowing
 * reprocessing of an existing request with new Preferences and/or updated
 * oremap URL), e.g. to generate a new test DOI without re-retrieving all of the
 * data from the space.
 * 
 * @author Jim
 *
 */
public class RefRepoLocalPubRequestFacade extends C3PRPubRequestFacade {

	private static final Logger log = Logger
			.getLogger(RefRepoLocalPubRequestFacade.class);

	private String requestFilePath = null;

	private long starttime = 0l;
	private String proxyServer = null;
	
	public RefRepoLocalPubRequestFacade(String RO_ID, String requestPath,
			Properties props) {
		super(RO_ID, props);
		requestFilePath = requestPath;

		if (props.containsKey("proxyserver")) {
			// Proxy Mode
			proxyServer= props.getProperty("proxyserver");
			log.debug("Using Proxy: " + proxyServer);
			setLocalContext(SEADAuthenticator.authenticate(proxyServer));
			if (super.getLocalContext() == null) {
				log.error("Unable to authenticate - exiting");
				System.exit(0);
			}
		}
		starttime = System.currentTimeMillis();
	}

	// Currently, only the Preferences and Aggregation Statistics items are
	// used, so they are the only parts (along with an appropriate @context
	// needed in the request...
	public JSONObject getPublicationRequest() {
		if (requestFilePath == null) {
			return super.getPublicationRequest();
		}
		File request = new File(requestFilePath);
		if (request.exists()) {

			try {
				log.debug("Retrieving local request file: "
						+ request.getAbsolutePath());
				return new JSONObject(IOUtils.toString(new FileInputStream(
						request), "UTF-8"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public HttpClientContext getLocalContext() {
		//return the default or, if using a proxy, make sure we have updated credentials
		log.debug("Retrieiving context");
		if (proxyServer!=null) {
			log.debug("Using proxy: " + proxyServer);
			setLocalContext(SEADAuthenticator.reAuthenticateIfNeeded(proxyServer, starttime));
			starttime=System.currentTimeMillis();
		}
		if(super.getLocalContext()==null) {
			log.error("Unable to re-authenticate - exiting");
			System.exit(0);
		}
		return super.getLocalContext();
	}
}
