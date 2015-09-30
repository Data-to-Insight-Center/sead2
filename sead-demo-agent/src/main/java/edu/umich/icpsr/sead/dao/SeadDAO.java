package edu.umich.icpsr.sead.dao;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
import org.apache.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;

import edu.umich.icpsr.sead.auth.SEADGoogleLogin;
import edu.umich.icpsr.sead.model.ResearchObjectOreMap;
import edu.umich.icpsr.sead.model.ResearchObjectRequest;
import edu.umich.icpsr.sead.model.ResearchObjectRequest.Status;

@Component
public class SeadDAO {
	private static final Logger LOG = Logger.getLogger(SeadDAO.class);
	RestTemplate restTemplate = new RestTemplate();

	public List<ResearchObjectRequest> getResearchObjectsRequests(String restUrl) {
		try {
			ResearchObjectRequest[] res = restTemplate.getForObject(restUrl, ResearchObjectRequest[].class);
			if (res != null) {
				return Arrays.asList(res);
			}
		} catch (Exception e) {
			LOG.error("", e);
			throw new RuntimeException("", e);
		}
		return null;
	}

	public ResearchObjectRequest getResearchObjectsRequest(String restUrl) {
		try {
			ResearchObjectRequest res = restTemplate.getForObject(restUrl, ResearchObjectRequest.class);
			if (res != null) {
				return res;
			}
		} catch (Exception e) {
			LOG.error("", e);
			throw new RuntimeException("", e);
		}
		return null;
	}

	public void postStatus(String restUrl, Status status) {
		try {
			restTemplate.postForObject(restUrl, status, Status.class);
		} catch (Exception e) {
			LOG.error("", e);
			throw new RuntimeException("", e);
		}
	}

	public ResearchObjectOreMap getResearchObjectOreMap(ResearchObjectRequest ro) {
		try {
			String roIdUrl = ro.getAggregation().get("@id").toString();
			return restTemplate.execute(roIdUrl, HttpMethod.GET, new RequestCallback() {
				public void doWithRequest(ClientHttpRequest request) throws IOException {
					// Do nothing
				}
			}, new ResponseExtractor<ResearchObjectOreMap>() {
				public ResearchObjectOreMap extractData(ClientHttpResponse response) throws IOException {
					InputStream inputStream = response.getBody();
					Object jsonObject = JsonUtils.fromInputStream(inputStream);
					JsonLdOptions options = new JsonLdOptions();
					Object compact;
					try {
						compact = JsonLdProcessor.compact(jsonObject, new HashMap(), options);
						ObjectMapper mapper = new ObjectMapper();
						ResearchObjectOreMap oreMap = mapper.readValue(JsonUtils.toPrettyString(compact), ResearchObjectOreMap.class);
						return oreMap;
					} catch (JsonLdError e) {
						throw new RuntimeException("", e);
					}
				}
			});
		} catch (Exception e) {
			throw new RuntimeException("", e);
		}
	}

	public boolean authenticate(HttpClientContext localContext) {
		// Create a local instance of cookie store
		CookieStore cookieStore = new BasicCookieStore();
		// Create local HTTP context
		// Bind custom cookie store to the local context
		localContext.setCookieStore(cookieStore);

		boolean authenticated = false;
		LOG.info("Authenticating");

		String accessToken = SEADGoogleLogin.getAccessToken();

		// Now login to server and create a session
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			HttpPost seadAuthenticate = new HttpPost("https://sead-test.ncsa.illinois.edu/acr/api/authenticate");
			List<NameValuePair> nvpList = new ArrayList<NameValuePair>(1);
			nvpList.add(0, new BasicNameValuePair("googleAccessToken", accessToken));

			seadAuthenticate.setEntity(new UrlEncodedFormEntity(nvpList));

			CloseableHttpResponse response = httpclient.execute(seadAuthenticate, localContext);
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
					LOG.error("Error response from  : " + response.getStatusLine().getReasonPhrase());
				}
			} finally {
				response.close();
				httpclient.close();
			}
		} catch (IOException e) {
			LOG.error("Cannot read sead-google.json");
			LOG.error(e.getMessage(), e);
		}
		return authenticated;
	}
}
