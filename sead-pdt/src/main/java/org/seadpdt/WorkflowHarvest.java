package org.seadpdt;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.bson.Document;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@Path("/harvest")
public class WorkflowHarvest {

	MongoClient mongoClient = new MongoClient();
	MongoDatabase db = mongoClient.getDatabase("sead");
	MongoCollection<Document> collection = db.getCollection("ro");	
			
    /**
     * Ping method to check whether the PDT service is up
     */
    @GET
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    public String ping() {
        return "SEAD PDT Service is up!";
    }
		
	 @GET
	 @Path("/harvest")
	 @Produces(MediaType.APPLICATION_JSON)
	 public String workflowHarvest(
				@QueryParam("data") String workflowID)  {	
		 return workflowID;
	 }	
	 	 
	 @GET
	 @Path("/publishRO")
	 @Produces(MediaType.APPLICATION_JSON)
	 public String getWorkflowRO(
				@QueryParam("data") String workflowData)  {	 
		 
			System.out.println("data : " + workflowData);
	 		 
			JSONObject xmlJSONObj = new JSONObject(workflowData);
			
			System.out.println("json : " + xmlJSONObj);
			
			Document doc = Document.parse(xmlJSONObj.toString());

			System.out.println("doc : " + doc);
			
			collection.drop();
			collection.insertOne(doc);
							
			mongoClient.close();		
			
		 return "success";
	 }		 
	 	
}
