package org.seadpdt;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.json.JSONObject;
import org.json.XML;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class ORCIDcalls {
	public static int PRETTY_PRINT_INDENT_FACTOR = 4;
	
	public static String getORCID(String orcidID) {
		  String output = "";
		  String jsonPrettyPrintString = "";
			MongoClient mongoClient = new MongoClient();
			MongoDatabase db = mongoClient.getDatabase("sead");
			MongoCollection<Document> collection = db.getCollection("people");
			
			try {
		 
				Client client = Client.create();
		 
				WebResource webResource = client
				   .resource("http://pub.orcid.org/v1.1/" + orcidID + "/orcid-bio");
		 
				ClientResponse response = webResource.accept("application/xml")
		                   .get(ClientResponse.class);
		 
				if (response.getStatus() != 200) {
					mongoClient.close();
					throw new RuntimeException("Failed : HTTP error code : "
					+ response.getStatus());
				}
		 
				output = response.getEntity(String.class);
				
				JSONObject xmlJSONObj = XML.toJSONObject(output);
				
//				Document doc = new Document("name", "MongoDB")
//	               .append("type", "database")
//	               .append("count", 1)
//	               .append("info", new Document("x", 203).append("y", 102));
				
				Document doc = Document.parse(xmlJSONObj.toString());
				
				collection.insertOne(doc);
								
				mongoClient.close();
				
				jsonPrettyPrintString = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
	            System.out.println(jsonPrettyPrintString);
		 
				System.out.println("Output from Server .... \n");
				System.out.println(output);
		 
			  } catch (Exception e) {
		 
				e.printStackTrace();
		 
			  }
			return jsonPrettyPrintString;
		 
			}
	
	
}
