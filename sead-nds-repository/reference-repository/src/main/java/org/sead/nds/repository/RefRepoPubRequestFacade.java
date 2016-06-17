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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;

import javax.ws.rs.core.MediaType;

import org.apache.commons.compress.parallel.InputStreamSupplier;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class overrides the method to retrieve a pub request (allowing
 * reprocessing of an existing request with new Preferences and/or updated
 * oremap URL), e.g. to generate a new test DOI without re-retrieving all of the
 * data from the space.
 * 
 * @author Jim
 *
 */
public class RefRepoPubRequestFacade extends C3PRPubRequestFacade {

	private static final Logger log = Logger
			.getLogger(RefRepoPubRequestFacade.class);

	private String requestFilePath = null;

	public RefRepoPubRequestFacade(String RO_ID, String requestPath,
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

	private URI getOREMapURI() {
		try {
			return new URI(proxyIfNeeded(getPublicationRequest().getJSONObject("Aggregation")
					.getString("@id")));
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public JSONObject getOREMap() {
		if (oremap == null) {

			log.debug("Retrieving: " + getOREMapURI().toString());
			HttpGet getMap = createNewGetRequest(getOREMapURI(),
					MediaType.APPLICATION_OCTET_STREAM);
			try {
				CloseableHttpResponse response = client.execute(getMap);

				if (response.getStatusLine().getStatusCode() == 200) {
					String mapString = EntityUtils.toString(response
							.getEntity());
					log.trace("OREMAP: " + mapString);
					oremap = new JSONObject(mapString);
				}
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				log.error("Unable to retrieve OREMap", e);
				e.printStackTrace();
			}
		}
		return oremap;
	}

	private HttpGet createNewGetRequest(URI url, String returnType) {

		HttpGet request = null;

		if (bearerToken != null) {
			// Clowder Kluge - don't add key once Auth header is accepted
			try {
				request = new HttpGet(new URI(url.toURL().toString() + "?key="
						+ bearerToken));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			request.addHeader("Authorization", "Bearer " + bearerToken);
		} else {
			request = new HttpGet(url);
		}
		if (returnType != null) {
			request.addHeader("accept", returnType);
		}
		return request;
	}

	InputStreamSupplier getInputStreamSupplier(final String uri) {
		
		//FIXME - remove once done processing bad maps: Backward compat
		final String newuri=uri.replaceAll(" ", "%20");

		return new InputStreamSupplier() {
			public InputStream get() {
				int tries = 0;
				while (tries < 3) {
					try {
						HttpGet getMap = createNewGetRequest(new URI(newuri), null);
						log.trace("Retrieving: " + uri);
						CloseableHttpResponse response;
						response = client.execute(getMap);
						if (response.getStatusLine().getStatusCode() == 200) {
							log.trace("Retrieved: " + uri);
							return response.getEntity().getContent();
						}
						log.debug("Status: "
								+ response.getStatusLine().getStatusCode());
						tries++;
					} catch (ClientProtocolException e) {
						tries += 3;
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// Retry if this is a potentially temporary error such
						// as a timeout
						tries++;
						log.warn("Attempt# " + tries
								+ " : Unable to retrieve file: " + uri, e);
						if (tries == 3) {
							log.error("Final attempt failed for " + uri);
						}
						e.printStackTrace();
					} catch (URISyntaxException e) {
						tries += 3;
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return null;
			}
		};
	}

	JSONArray expandPeople(String[] people) {

		JSONArray peopleArray = new JSONArray();
		if ((people != null) && (people.length != 0)) {
			String c3prServer = props.getProperty("c3pr.address");
			for (int i = 0; i < people.length; i++) {
				log.debug("Expanding: " + people[i]);
				try {
					log.debug(URLEncoder.encode(people[i], "UTF-8"));
				} catch (UnsupportedEncodingException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				HttpGet getPerson;
				try {
					getPerson = new HttpGet(c3prServer + "api/people/"
							+ URLEncoder.encode(people[i], "UTF-8"));

					getPerson.addHeader("accept", "application/json");
					log.trace("getPerson created" + getPerson.getURI());
					CloseableHttpResponse response = client.execute(getPerson);

					if (response.getStatusLine().getStatusCode() == 200) {
						String mapString = EntityUtils.toString(response
								.getEntity());
						log.trace("Expanded: " + mapString);
						peopleArray.put(new JSONObject(mapString));

					} else {
						log.trace("Adding unexpanded person: " + people[i]);
						peopleArray.put(people[i]);
					}
					// Required to avoid some calls hanging in execute() even
					// though we do not reuse the HttpGet object
					getPerson.reset();

				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException ia) {
					ia.printStackTrace();
				}
			}
			log.debug("Expansion complete");
		}
		return peopleArray;
	}

	public void sendStatus(String stage, String message) {

		String c3prServer = props.getProperty("c3pr.address");
		try {
			String statusUrl = c3prServer + "api/researchobjects/"
					+ URLEncoder.encode(RO_ID, "UTF-8") + "/status";

			log.debug("Posting status to: " + statusUrl);
			HttpPost postStatus = new HttpPost(statusUrl);
			if (props.containsKey("JSESSIONID")) {
				// Proxy Mode
				log.debug("Adding: " + props.getProperty("JSESSIONID"));

				BasicClientCookie cookie = new BasicClientCookie("JSESSIONID",
						props.getProperty("JSESSIONID"));
				URL c3pr = new URL(c3prServer);

				cookie.setDomain(c3pr.getHost());
				cookie.setPath("/");
				cookie.setSecure(c3pr.getProtocol().equalsIgnoreCase("https") ? true
						: false);
				cookieStore.addCookie(cookie);
			}
			postStatus.addHeader("accept", MediaType.APPLICATION_JSON);
			String statusString = "{\"reporter\":\"" + Repository.getID()
					+ "\", \"stage\":\"" + stage + "\", \"message\":\""
					+ message + "\"}";
			StringEntity status = new StringEntity(statusString);
			log.trace("Status: " + statusString);
			postStatus.addHeader("content-type", MediaType.APPLICATION_JSON);
			postStatus.setEntity(status);

			CloseableHttpResponse response = client.execute(postStatus);

			if (response.getStatusLine().getStatusCode() == 200) {
				log.debug("Status Successfully posted");
			} else {
				log.warn("Failed to post status, response code: "
						+ response.getStatusLine().getStatusCode());
			}
			// Must consume entity to allow connection to be released
			// If this line is not here, the third try to send status will
			// result in a
			// org.apache.http.conn.ConnectionPoolTimeoutException: Timeout
			// waiting for connection from pool
			// (or a blocked call/hund program if timeouts weren't set
			EntityUtils.consumeQuietly(response.getEntity());

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			log.error("Error posting status.", e);

			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			log.error("Error posting status.", e);

			e.printStackTrace();
		} catch (IOException e) {
			log.error("Error posting status.", e);
			e.printStackTrace();
		} catch (Exception e) {
			log.error("Odd Error posting status.", e);
			e.printStackTrace();

		}

		if (echoToConsole) {
			System.out
					.println("*********************Status Message******************************");
			System.out.println("Reporter: " + Repository.getID() + ", Stage: "
					+ stage);
			System.out.println("Message Text: " + message);
			System.out
					.println("*****************************************************************");
		}
	}

}
