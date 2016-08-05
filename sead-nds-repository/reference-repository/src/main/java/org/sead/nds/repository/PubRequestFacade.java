package org.sead.nds.repository;

import java.util.ArrayList;

import org.apache.commons.compress.parallel.InputStreamSupplier;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The PubRequestFacade class is an abstract class that provides all of the
 * request-related information needed to create a bag and create a data
 * publication. This class provides implementations of methods that do things
 * such as parse the OREmap and are, therefore, independent of how the oremap is
 * retrieved. Concrete implementations must add implementations of the abstract
 * methods that are appropriate for retrieving the Pub Request and OREmap and
 * sending status.
 * 
 * @author Jim
 *
 */
public abstract class PubRequestFacade {
	private static final Logger log = Logger.getLogger(PubRequestFacade.class);

	public static final String SUCCESS_STAGE = "Success";
	public static final String FAILURE_STAGE = "Failure";
	public static final String PENDING_STAGE = "Pending";
	public static final String PROBLEM_STAGE = "Problem";
	
	public static final String PREFERENCES = "Preferences";
	public static final String PURPOSE = "Purpose";
	public static final String TESTING = "Testing-Only";
	public static final String PRODUCTION = "Production";
	
	public static final String EXTERNAL_IDENTIFIER = "External Identifier";
	

	abstract public JSONObject getPublicationRequest();

	abstract public JSONObject getOREMap();

	// Logic to decide if this is a container -
	// first check for children, then check for source-specific type indicators
	boolean childIsContainer(int index) {
		JSONObject item = getOREMap().getJSONObject("describes")
				.getJSONArray("aggregates").getJSONObject(index);
		if (getChildren(item).length() != 0) {
			return true;
		}
		Object o = item.get("@type");
		if (o != null) {
			if (o instanceof JSONArray) {
				for (int i = 0; i < ((JSONArray) o).length(); i++) {
					String type = ((JSONArray) o).getString(i).trim();
					if ("http://cet.ncsa.uiuc.edu/2007/Collection".equals(type)
							|| "http://cet.ncsa.uiuc.edu/2016/Folder"
									.equals(type)) {
						return true;
					}
					// Check for Clowder type
				}
			} else if (o instanceof String) {
				String type = ((String) o).trim();
				if ("http://cet.ncsa.uiuc.edu/2007/Collection".equals(type)
						|| "http://cet.ncsa.uiuc.edu/2016/Folder".equals(type)) {
					return true;
				}
				// Check for Clowder type
			}
		}
		return false;
	}

	// Get's all "Has Part" children, standardized to send an array with 0,1, or
	// more elements
	JSONArray getChildren(JSONObject parent) {
		Object o = null;
		try {
			o = parent.get("Has Part");
		} catch (JSONException e) {
			// Doesn't exist - that's OK
		}
		if (o == null) {
			return new JSONArray();
		} else {
			if (o instanceof JSONArray) {
				return (JSONArray) o;
			} else if (o instanceof String) {
				return new JSONArray("[	" + (String) o + " ]");
			}
			log.error("Error finding children: " + o.toString());
			return new JSONArray();
		}
	}
	
	/**Retrieve a data file given it's URL. Nominally this just involves doing a GET, but implementing classes
	 * may use a proxy, implement custom retry strategies, or, if it can interpret the URL it may, for example, 
	 * bypass HTTP and get the relevant file from local storage or a known-good chache, etc.
	 *  
	 * @param uri
	 * @return a stream supplier from which the data file can be read when needed.
	 */
	abstract InputStreamSupplier getInputStreamSupplier(final String uri);

	static String getCreatorsString(String[] creators) {
		StringBuffer sBuffer = new StringBuffer();
		if ((creators != null) && (creators.length != 0)) {

			boolean first = true;
			for (int i = 0; i < creators.length; i++) {
				if (!first) {
					sBuffer.append(", ");
				} else {
					first = false;
				}
				sBuffer.append(creators[i]);
			}
		}
		return sBuffer.toString();
	}

	String[] normalizeValues(Object cObject) {

		ArrayList<String> valueList = new ArrayList<String>();
		if (cObject instanceof String) {
			valueList.add((String) cObject);
		} else if (cObject instanceof JSONArray) {
			for (int i = 0; i < ((JSONArray) cObject).length(); i++) {
				valueList.add(((JSONArray) cObject).getString(i));
			}
		} else {
			log.warn("Object for normalization is not a string or array of strings");
		}
		return valueList.toArray(new String[valueList.size()]);
	}

	abstract JSONArray expandPeople(String[] people);

	public static String[] flattenPeople(JSONArray creators) {

		ArrayList<String> creatorList = new ArrayList<String>();
		for (int i = 0; i < creators.length(); i++) {
			Object o = creators.get(i);
			if (o instanceof String) {
				creatorList.add((String) o);
			} else if (o instanceof JSONObject) {
				JSONObject jo = (JSONObject) o;
				creatorList.add((jo.getString("givenName")
						+ jo.getString("familyName") + "("
						+ jo.getString("@id") + ")"));
			}
		}
		return creatorList.toArray(new String[creatorList.size()]);
	}

	boolean echoToConsole = false;

	public void setEchoStatusToConsole(boolean val) {
		echoToConsole = val;
	}

	abstract public void sendStatus(String stage, String message);

}
