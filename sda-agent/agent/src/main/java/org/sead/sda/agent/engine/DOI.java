package org.sead.sda.agent.engine;

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
		//this.target = "http://dummyUrlTest1.com";
		System.out.println(this.metadata);
		String doi_url = ezidService.createDOI(metadata, target);
		return doi_url;
	}
	
	
	public String updateDoi(String doi, String target){
		ezidService = new EzidService();
		String newdoi_url = ezidService.updateDOI(doi, target);
		return newdoi_url;
	}
	
	
	public void setPermanentDOI(){
		ezidService.setPermanentDOI(true);
	}
	
	public String getMetaData(JSONObject ore){
		

		JSONObject new_data = new JSONObject();
		String title = null;
		
		JSONObject describe = new JSONObject();
		String pubdate = null;
		try{
			describe = (JSONObject) ore.get("describes");
			title = describe.get("Title").toString();
		}catch(Exception e){
			e.printStackTrace();
			System.err.println("no Title");
		}

			
			
		String who = null;
		
		try{
			JSONArray creator = (JSONArray) describe.get("Creator");
			who = creator.get(0).toString().split(":")[0];
		}catch(Exception e){
			who = describe.get("Creator").toString().split(":")[0];
		}
			
		try{
			pubdate = describe.get("Publication Date").toString();
		}catch(Exception e){
			e.printStackTrace();
			System.err.println("no Publish Date");
		}
			
		new_data.put("title", title);
		new_data.put("creator", who);
		new_data.put("pubDate", pubdate);
			
		String data = new_data.toJSONString();
	
		
		return data;
	}
	
}
