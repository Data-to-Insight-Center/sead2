package org.seadpdt.people;

import org.json.JSONObject;
import org.seadpdt.PeopleServices;

public class Profile {
	private String provider;
	private String identifier;
	public String getProvider() {
		return provider;
	}
	public void setProvider(String provider) {
		this.provider = provider;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String id) {
		this.identifier = id;
	}

	public JSONObject asJson() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(PeopleServices.provider, getProvider());
		jsonObject.put(PeopleServices.identifier, getIdentifier());
		return jsonObject;
	}
}
