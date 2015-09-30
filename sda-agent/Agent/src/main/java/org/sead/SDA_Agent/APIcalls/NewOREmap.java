package APIcalls;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class NewOREmap {
	
	private JSONObject new_ore; 
	
	public NewOREmap(JSONObject ore){
		new_ore = buildHierarchy(ore);		
	}
	
	
	public JSONObject getNewOREmap(){
		return this.new_ore;
	}
	
	public JSONObject buildHierarchy(JSONObject ore){
		JSONObject describe = (JSONObject) ore.get("describes") ;
		
		String label = (String) describe.get("Label");
		
		JSONArray aggregate = (JSONArray) describe.get("aggregates");
		
		List part = (List) describe.get("Has Part");
		
		JSONObject output = new JSONObject();
		
		output.put("Folder", describe.get("Label"));
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
				oneItem.put("Folder", list_item.get("Label"));
				oneItem.put("content", hasPart(agg, part_new,location_new, stop_new));
				example.add(oneItem);	
				i += size;
			}
			
			if (part.contains(list_item.get("Identifier")) && !list_item.containsKey("Has Part")){
				JSONObject oneItem = new JSONObject();
				oneItem.put("Label", list_item.get("Label"));
				oneItem.put("Link", list_item.get("similarTo"));
				example.add(oneItem);
				
			}
		}
		
		return example;
		
	}

}
