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
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.sead.nds.repository.C3PRPubRequestFacade;

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

	public RefRepoLocalPubRequestFacade(String RO_ID, String requestPath,
			Properties props) {
		super(RO_ID, props);
		requestFilePath = requestPath;
	}

	// Currently, only the Preferences and Aggregation Statistics items are
	// used, so they are the only parts (along with an appropriate @context
	// needed in the request...
	public JSONObject getPublicationRequest() {
		File request = new File(requestFilePath);
		if (request.exists()) {

			try {
				log.debug("Retrieving local request file: " + request.getAbsolutePath());
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
}
