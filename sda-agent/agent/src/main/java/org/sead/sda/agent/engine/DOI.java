package org.sead.sda.agent.engine;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.seadva.services.EzidService;

public class DOI {

    private String target;
    private String metadata;
    private EzidService ezidService;


    public DOI(String target, JSONObject ore) {
        this.target = target;
        this.metadata = getMetaData(ore);
    }

    public String getDoi() {
        EzidService ezidService = new EzidService();
        System.out.println("DOI Metadata: " + this.metadata);
        System.out.println("DOI Target: " + this.target);
        return ezidService.createDOI(metadata, target);
    }


    public String updateDoi(String doi, String target) {
        ezidService = new EzidService();
        return ezidService.updateDOI(doi, target);
    }

    public void setPermanentDOI() {
        ezidService.setPermanentDOI(true);
    }

    public String getMetaData(JSONObject ore) {
        JSONObject newData = new JSONObject();
        JSONObject describes = (JSONObject) ore.get("describes");
        if (describes != null) {
            // get title
            if (describes.get("Title") != null) {
                newData.put("title", describes.get("Title").toString());
            }
            // get creator
            Object creator = describes.get("Creator");
            if (creator != null) {
                if (creator instanceof JSONArray) {
                    newData.put("creator", ((JSONArray) creator).get(0).toString());
                } else {
                    newData.put("creator", creator.toString());
                }
            }
            // get publication date
            if (describes.get("Publication Date") != null) {
                newData.put("pubDate", describes.get("Publication Date").toString());
            }
        }
        return newData.toJSONString();
    }

}
