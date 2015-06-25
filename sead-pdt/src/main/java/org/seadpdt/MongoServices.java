package org.seadpdt;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@Path("/mongo")
public class MongoServices {

	MongoClient mongoClient = new MongoClient();
	MongoDatabase db = mongoClient.getDatabase("sead");
	MongoCollection<Document> collection = db.getCollection("people");
	
	 @GET
	 @Path("/clear")
	 public void clearMongo()  {	
		// collection.deleteMany(gte("i", 100));
	 }
	 
	 @GET
	 @Path("/list")
	 @Produces(MediaType.APPLICATION_JSON)
	 public FindIterable<Document> listMongo()  {	
		 FindIterable<Document> iterable = db.getCollection("people").find();
		 return iterable;
	 }		 
	
}


