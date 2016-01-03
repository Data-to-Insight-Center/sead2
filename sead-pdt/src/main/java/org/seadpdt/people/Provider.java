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

import org.bson.Document;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


abstract public class Provider {

	static private Map<String, Provider>providers = new HashMap<String, Provider>();
	
	static {
	// Register Providers
	Provider.registerProvider(new OrcidProvider());
	Provider.registerProvider(new GooglePlusProvider());
	Provider.registerProvider(new LinkedInProvider());
    Provider.registerProvider(new ClowderProvider());
	// Clowder/SEAD2
	}
	
	static void registerProvider(Provider p) {
		providers.put(p.getProviderName(), p);
	}
	
//	public static Iterator<Provider> getProviders() {
		//return providers.values().iterator();
	//}
	
	public static Provider getProvider(String key) {
		return providers.get(key);
	}
	
	
	public abstract String getProviderName();
	
	//Retrieve the external profile
	public abstract Document getExternalProfile(JSONObject person) throws RuntimeException;
		

	
	//Return the canonical form for any identifier string sent in
	//Should return null if the identifier cannot be unambiguously 
	//recognized by this provider
	 public abstract String getCanonicalId(String personID);
	 
	 
	 
	 //Find canonical form for ID when provider is not known
	 //Loop through all and look for one and only one match
	 public static Profile findCanonicalId(String id) throws RuntimeException {
		 Profile profile=null;
		 //Try to find one and only one
		 for(Provider p: providers.values()) {
			 String newID = p.getCanonicalId(id);
			 if(newID!=null) {
				 if(profile==null) {
					 profile = new Profile();
					 profile.setIdentifier(newID);
					 profile.setProvider(p.getProviderName());
				 } else {
					 throw new RuntimeException("Ambiguous id");
				 }
			 }
		 } 
		 return profile;
	  }
}
