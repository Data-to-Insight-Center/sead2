/**
 * Modified copy of org.sead.acr.client.SEADAuthetnicator from SEADUploader
 * 
 * 
 *
 * Copyright 2016 University of Michigan
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

package org.sead.repositories.reference.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

/**
 * @author Jim
 * 
 */
public class SEADAuthenticator {

	private static Log log = LogFactory.getLog(SEADAuthenticator.class);

	private static long authTime;
	// Create a local instance of cookie store
	static CookieStore cookieStore = new BasicCookieStore();
	private static HttpClientContext localContext = HttpClientContext.create();

	// Create local HTTP context

	// Bind custom cookie store to the local context
	static {
		localContext.setCookieStore(cookieStore);
	}

	static public HttpClientContext authenticate(String server) {

		boolean authenticated = false;
		log.info("Authenticating");

		String accessToken = SEADGoogleLogin.getAccessToken();

		// Now login to server and create a session
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			// //DoLogin is the auth endpoint when using the AUthFilter, versus
			// the api/authenticate endpoint when connecting to the ACR directly
			HttpPost seadAuthenticate = new HttpPost(server + "/DoLogin");
			List<NameValuePair> nvpList = new ArrayList<NameValuePair>(1);
			nvpList.add(0, new BasicNameValuePair("googleAccessToken",
					accessToken));

			seadAuthenticate.setEntity(new UrlEncodedFormEntity(nvpList));

			CloseableHttpResponse response = httpclient.execute(
					seadAuthenticate, localContext);
			try {
				if (response.getStatusLine().getStatusCode() == 200) {
					HttpEntity resEntity = response.getEntity();
					if (resEntity != null) {
						// String seadSessionId =
						// EntityUtils.toString(resEntity);
						authenticated = true;
					}
				} else {
					// Seems to occur when google device id is not set on server
					// - with a Not Found response...
					log.error("Error response from " + server + " : "
							+ response.getStatusLine().getReasonPhrase());
				}
			} finally {
				response.close();
				httpclient.close();
			}
		} catch (IOException e) {
			log.error("Cannot read sead-google.json");
			log.error(e.getMessage());
		}

		// localContext should have the cookie with the SEAD session key, which
		// nominally is all that's needed.
		// FixMe: If there is no activity for more than 15 minutes, the session
		// may expire, in which case,
		// re-authentication using the refresh token to get a new google token
		// to allow SEAD login again may be required

		// also need to watch the 60 minutes google token timeout - project
		// spaces will invalidate the session at 60 minutes even if there is
		// activity
		authTime = System.currentTimeMillis();

		if (authenticated) {
			return localContext;
		}
		return null;
	}

	public static HttpClientContext reAuthenticateIfNeeded(String server,
			long startTime) {
		long curTime = System.currentTimeMillis();
		// If necessary, re-authenticate and return the result
		if (((curTime - startTime) / 1000l > 1700)
				|| ((curTime - authTime) / 1000l > 3500)) {
			return authenticate(server);
		}
		// If it's not time, just return the current value
		return localContext;
	}
}
