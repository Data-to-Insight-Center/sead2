package org.seadpdt;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.seadpdt.util.Constants;

@Path("/researchobjects")

public class ROServices {

	// move these to an external file?
	String collectionName = "ro";
	String DBname = Constants.pdtDbName;
	
	MongoClient mongoClient = new MongoClient();
	MongoDatabase db = mongoClient.getDatabase(DBname);
	MongoCollection<Document> collection = db.getCollection(collectionName);	
		 	 	 
	 @GET
	 @Path("/list")
	 @Produces(MediaType.APPLICATION_JSON)
	 public FindIterable<Document> listRO() {	
		 FindIterable<Document> iterable = db.getCollection(collectionName).find();
		 return iterable;
	 }		 
	 
	@GET
	@Path("/byid")
	@Produces(MediaType.APPLICATION_JSON)
	 public FindIterable<Document> getRO(
		@QueryParam("id") String ROID) {
		FindIterable<Document> iterable = db.getCollection(collectionName).find();
		return iterable;		
	}	 	 
	  
}
