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
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class NewOREmap {
	
	private JSONObject new_ore; 
	private Map<String, String> keyMapList;
	
	LinkedHashMap<String, String> hierarchy;
	
	public NewOREmap(JSONObject ore, Map<String, String> keyMapList){
		this.keyMapList = keyMapList;
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
		JSONObject describe = (JSONObject) ore.get(keyMapList.get("describes".toLowerCase())) ;	
		JSONArray context = (JSONArray) ore.get(keyMapList.get("@context".toLowerCase()));		
		JSONArray aggregate = (JSONArray) describe.get(keyMapList.get("aggregates".toLowerCase()));
		
		List part = (List) describe.get(keyMapList.get("Has Part".toLowerCase()));
		
		JSONObject output = new JSONObject();

		output.put("Folder", describe.get(keyMapList.get("Title".toLowerCase())));			
		output.put("aggregates", hasPart(aggregate, part, 0, aggregate.size()-1));
		
		return output;
	}	
	

	
	public JSONArray hasPart(JSONArray agg, List part, int location, int stop){
		JSONArray example = new JSONArray();
		
		Object[] list = agg.toArray();
		for (int i = location; i <= stop; i++){
			JSONObject list_item = (JSONObject) list[i];
			if (list_item.containsKey(keyMapList.get("Has Part".toLowerCase())) && part.contains(list_item.get(keyMapList.get("Identifier".toLowerCase())))){
				int size = ((JSONArray) list_item.get(keyMapList.get("Has Part".toLowerCase()))).size();
				int location_new = i;
				int stop_new = i + size;
				List part_new = (List) list_item.get(keyMapList.get("Has Part".toLowerCase()));
				JSONObject oneItem = new JSONObject();
				oneItem.put("Folder", list_item.get(keyMapList.get("Title".toLowerCase())));
				oneItem.put("content", hasPart(agg, part_new,location_new, stop_new));
				example.add(oneItem);	
				i += size;
			}
			
			if (part.contains(list_item.get(keyMapList.get("Identifier".toLowerCase()))) && !list_item.containsKey(keyMapList.get("Has Part".toLowerCase()))){
				JSONObject oneItem = new JSONObject();
				oneItem.put("Title", list_item.get(keyMapList.get("Label".toLowerCase())));
				oneItem.put("Size", list_item.get(keyMapList.get("Size".toLowerCase())));
				example.add(oneItem);
				
			}
		}
		
		return example;
		
	}

}
