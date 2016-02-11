/*
 * Copyright 2015 The Trustees of Indiana University
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
 * @author luoyu@indiana.edu
 * @author isuriara@indiana.edu
 */

package org.sead.sda;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class NewOREmap {
	
	private JSONObject new_ore; 
	
	LinkedHashMap<String, String> hierarchy;
	
	public NewOREmap(JSONObject ore){
		new_ore = buildHierarchy(ore);
		hierarchy = new LinkedHashMap<String, String>();
		String rootName = new_ore.get("Folder").toString();
		JSONArray aggregates = (JSONArray) new_ore.get("aggregates");
		//extract hierarchy
		extractHierarchy(aggregates, rootName);
		
	}
	
	
	
	
	public void extractHierarchy(JSONArray aggs, String root){
		
		for (Object agg : aggs.toArray()){
			
			JSONObject agg_item = (JSONObject) agg;
			
			if (agg_item.containsKey("Folder")){
				hierarchy.put(root+"/"+agg_item.get("Folder"), null);
				extractHierarchy((JSONArray) agg_item.get("content"), root+"/"+agg_item.get("Folder"));
			}else{
				hierarchy.put(root+"/"+agg_item.get("Title").toString(), agg_item.get("Size").toString());
			}
			
		}
		
		
	}
	
	
	public HashMap<String, String> getHierarchy(){
		return this.hierarchy;
	}
	
	public JSONObject buildHierarchy(JSONObject ore){
		JSONObject describe = (JSONObject) ore.get("describes") ;	
		JSONArray context = (JSONArray) ore.get("@context");		
		JSONArray aggregate = (JSONArray) describe.get("aggregates");
		
		List part = (List) describe.get("Has Part");
		
		JSONObject output = new JSONObject();

		output.put("Folder", describe.get("Title"));			
		output.put("aggregates", hasPart(aggregate, part, 0, aggregate.size()-1));
		
		return output;
	}	
	

	
	public JSONArray hasPart(JSONArray agg, List part, int location, int stop){
		JSONArray example = new JSONArray();
		
		Object[] list = agg.toArray();
		for (int i = location; i <= stop; i++){
			JSONObject list_item = (JSONObject) list[i];
			if (list_item.containsKey("Has Part") && part.contains(list_item.get("Identifier"))){
				int size = ((JSONArray) list_item.get("Has Part")).size();
				int location_new = i;
				int stop_new = i + size;
				List part_new = (List) list_item.get("Has Part");
				JSONObject oneItem = new JSONObject();
				//modify
				oneItem.put("Folder", list_item.get("Title"));
				oneItem.put("content", hasPart(agg, part_new,location_new, stop_new));
				example.add(oneItem);	
				i += size;
			}
			
			if (part.contains(list_item.get("Identifier")) && !list_item.containsKey("Has Part")){
				JSONObject oneItem = new JSONObject();
				oneItem.put("Title", list_item.get("Label"));
				oneItem.put("Size", list_item.get("Size"));
				example.add(oneItem);
				
			}
		}
		
		return example;
		
	}

}
