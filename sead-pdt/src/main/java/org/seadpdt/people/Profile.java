/*
 *
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
 *
 *
 * @author myersjd@umich.edu
 */

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
