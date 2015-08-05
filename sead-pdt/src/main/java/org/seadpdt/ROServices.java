package org.seadpdt;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

@Path("/RO")

public class ROServices {

	MongoClient mongoClient = new MongoClient();
	MongoDatabase db = mongoClient.getDatabase("sead");
		 
	 @GET
	 @Path("/byid")
	 @Produces(MediaType.APPLICATION_JSON)
	 
	 public FindIterable<Document> listMongo(@QueryParam("id") String repID)  {	
		 FindIterable<Document> iterable = db.getCollection("ro").find();
		 return iterable;
	 }	
	  
}
