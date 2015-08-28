package org.seadpdt;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.bson.Document;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@Path("/people")

public class PeopleServices {

	// move these to an external file?
	String collectionName = "people";
	String DBname = "sead";
	
	MongoClient mongoClient = new MongoClient();
	MongoDatabase db = mongoClient.getDatabase(DBname);
	MongoCollection<Document> collection = db.getCollection(collectionName);	
	
	 @POST
	 @Path("/add")
	 @Consumes(MediaType.APPLICATION_JSON)
	 @Produces(MediaType.APPLICATION_JSON)
	 public String addPerson(String personData)  {	 
		JSONObject xmlJSONObj = new JSONObject(personData);			
		Document doc = Document.parse(xmlJSONObj.toString());
		collection.drop(); // needs to drop by ID, not drop all
		collection.insertOne(doc);
		mongoClient.close();		
		return "success";
	 }	
	 	 
	 @GET
	 @Path("/list")
	 @Produces(MediaType.APPLICATION_JSON)
	 public FindIterable<Document> listPeople() {	
		 FindIterable<Document> iterable = db.getCollection(collectionName).find();
		 return iterable;
	 }		 
	 
	@GET
	@Path("/byid")
	@Produces(MediaType.APPLICATION_JSON)
	 public FindIterable<Document> getPeople(
		@QueryParam("id") String repID) {
		FindIterable<Document> iterable = db.getCollection(collectionName).find();
		return iterable;		
	}	 	 

	 @POST
	 @Path("/delete")
	 @Consumes(MediaType.APPLICATION_JSON)
	 @Produces(MediaType.APPLICATION_JSON)
	 public String deletePerson(String personData)  {	 
		JSONObject xmlJSONObj = new JSONObject(personData);			
		Document doc = Document.parse(xmlJSONObj.toString());
		collection.drop(); // needs to drop by ID, not drop all
		mongoClient.close();		
		return "success";
	 }		
	
	 @GET
	 @Path("/orcid")
	 @Produces(MediaType.APPLICATION_JSON)
	 public String listORCID(
				@QueryParam("id") String orcidID)  {	
		 return ORCIDcalls.getORCID(orcidID);
	 }			 
	
}
