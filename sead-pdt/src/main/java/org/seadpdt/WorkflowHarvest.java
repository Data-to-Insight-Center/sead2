package org.seadpdt;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;


@Path("/workflow")
public class WorkflowHarvest {

	MongoClient mongoClient = new MongoClient();
	MongoDatabase db = mongoClient.getDatabase("sead");
	
	 @GET
	 @Path("/harvest")
	 @Produces(MediaType.APPLICATION_JSON)
	 public String workflowHarvest(
				@QueryParam("id") String workflowID)  {	
		 return workflowID;
	 }	
	
}
