package Engine;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.seadva.services.EzidService;

public class DOI {
	
	private String target;
	private String metadata;
	private JSONObject ore;
	private EzidService ezidService;

	
	public DOI(String target, JSONObject ore){
		this.target = target;
		this.ore = ore;
		this.metadata = getMetaData(this.ore);
		
	}
	
	
	public String getDoi(){
		EzidService ezidService = new EzidService();
		this.target = "http://dummyUrlTest1.com";
		String doi_url = ezidService.createDOI(metadata, target);
		return doi_url;
	}
	
	
	public String updateDoi(String doi, String target){
		ezidService = new EzidService();
		String newdoi_url = ezidService.updateDOI(doi, target);
		return newdoi_url;
	}
	
	
	public void setPerPermanentDOI(){
		ezidService.setPermanentDOI(true);
	}
	
	public String getMetaData(JSONObject ore){
		
		
		JSONObject new_data = new JSONObject();
		JSONObject describe = (JSONObject) ore.get("describes");

		String title = describe.get("Title").toString();
		
		JSONArray creator = (JSONArray) describe.get("Creator");
		String who = creator.get(0).toString();
		
		JSONArray date = (JSONArray) describe.get("Publication Date");
		String pubdate = "";
		for (Object item : date.toArray()){
			pubdate = pubdate + ", " + item.toString();
		}
		
		new_data.put("title", title);
		new_data.put("creator", who);
		new_data.put("pubDate", pubdate);
		
		String data = new_data.toJSONString();
		
		return data;
	}
	
}
