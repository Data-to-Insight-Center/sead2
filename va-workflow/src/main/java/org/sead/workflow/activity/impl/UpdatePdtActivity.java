package org.sead.workflow.activity.impl;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sead.workflow.activity.AbstractWorkflowActivity;
import org.sead.workflow.activity.SeadWorkflowActivity;
import org.sead.workflow.config.SeadWorkflowConfig;
import org.sead.workflow.context.SeadWorkflowContext;
import org.sead.workflow.util.Constants;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import org.seadva.services.statusTracker.SeadStatusTracker;
import org.seadva.services.statusTracker.enums.SeadStatus;

/**
 * Responsible for updating PDT with the published RO details
 */
public class UpdatePdtActivity extends AbstractWorkflowActivity {
    
    @Override
    public void execute(SeadWorkflowContext context, SeadWorkflowConfig config) {
        System.out.println("\n=====================================");
        System.out.println("Executing activity : " + activityName);
        System.out.println("-----------------------------------\n");

        SeadStatusTracker.addStatus(context.getProperty(Constants.RO_ID), SeadStatus.WorkflowStatus.UPDATE_PDT_BEGIN.getValue());

        HashMap<String, String> activityParams = new HashMap<String, String>();
        for(SeadWorkflowActivity activity : config.getActivities()){
            AbstractWorkflowActivity abstractActivity = (AbstractWorkflowActivity)activity;
            if(abstractActivity.activityName.equals(activityName)){
                activityParams = abstractActivity.params;
                break;
            }
        }
        String pdtSystemUrl = activityParams.get("pdtSystemUrl");  

        SeadStatusTracker.addStatus(context.getProperty(Constants.RO_ID), SeadStatus.PDTStatus.START.getValue());

        String ros = null;
        JSONObject ro = null;
        JSONObject roObject = new JSONObject();
		try {
			ro = new JSONObject(context.getProperty(Constants.JSON_RO));
			
            roObject.put("@context", "http://schema.org/");
            roObject.put("@type", "DataDownload");
            if (ro.has("Identifier"))
            	roObject.put("Identifier", ro.get("Identifier"));
            if (ro.has("Uploaded By"))
            	roObject.put("Uploaded By", ro.get("Uploaded By"));
            if (ro.has("contentUrl"))
            	roObject.put("contentUrl", ro.get("contentUrl"));
            if (ro.has("Title"))
            	roObject.put("Title", ro.get("Title"));
            if (ro.has("Creator"))
            	roObject.put("Creator", ro.get("Creator"));
            if (ro.has("Published In"))
            	roObject.put("Published In", ro.get("Published In"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
            ros = roObject.toString(); 		    
	        WebResource webResource = Client.create().resource(pdtSystemUrl);
	        ClientResponse response = webResource.path("harvest")
	                .path("publishRO")
	                .accept("application/json")
	                .type("application/json")
	                .post(ClientResponse.class, ros);					
			
	        SeadStatusTracker.addStatus(context.getProperty(Constants.RO_ID), SeadStatus.PDTStatus.END.getValue());
			
        System.out.println("\n=====================================");
        System.out.println("return status : " + response);
        System.out.println("-----------------------------------\n");

        SeadStatusTracker.addStatus(context.getProperty(Constants.RO_ID), SeadStatus.WorkflowStatus.UPDATE_PDT_END.getValue());


    }
    
    private JSONArray getAuthors(JSONObject ro) throws JSONException {
        JSONArray authorArray = new JSONArray();

        if(ro.has("creator")) {
            Object creators = ro.get("creator");
            if(creators instanceof JSONArray){
                for(int i = 0 ; i < ((JSONArray) creators).length() ; i++){
                    Object creator = ((JSONArray) creators).get(i);
                    if(creator instanceof String){
                        String[] params = ((String) creator).split(":",2);

                        JSONObject creatorObject = new JSONObject();
                        creatorObject.put("@type", "Person");
                        creatorObject.put("name", params[0]);
                        if(params.length > 1){
                            creatorObject.put("@id", params[1]);
                        }
                        creatorObject.put("email", "");
                        authorArray.put(creatorObject);
                    }
                }
            }
        }

        return authorArray;
    }    
}
