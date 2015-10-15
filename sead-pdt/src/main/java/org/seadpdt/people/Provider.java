package org.seadpdt.people;

import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.json.JSONObject;


abstract public class Provider {

	static private Map<String, Provider>providers = new HashMap<String, Provider>();
	
	public static void registerProvider(Provider p) {
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
					 profile.setId(newID);
					 profile.setProvider(p.getProviderName());
				 } else {
					 throw new RuntimeException("Ambiguous id");
				 }
			 }
		 } 
		 return profile;
	  }
}
