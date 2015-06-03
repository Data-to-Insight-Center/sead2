package org.seadpdt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

@Path("/repo")
public class RepoServices {
 
	MongoClient mongoClient = new MongoClient();
	MongoDatabase db = mongoClient.getDatabase("sead");
	
	 @GET
	 @Path("/mongo")
	 @Produces(MediaType.APPLICATION_JSON)
	 public FindIterable<Document> listMongo()  {	
		 FindIterable<Document> iterable = db.getCollection("repo").find();
		 return iterable;
	 }	
	 
	 @GET
	 @Path("/list")
	 @Produces(MediaType.APPLICATION_JSON)
	 public byte[] listRepos()  {	
		 
		 java.nio.file.Path path = Paths.get("../../sead-json/repo.json");
		 byte[] data = new byte[] {'*'};
		try {
			data = Files.readAllBytes(path);
		} catch (IOException e) {

		}
		 
		 return data;
	 }
	  
	 @GET
	 @Path("/test")
	 @Produces(MediaType.APPLICATION_JSON)
	 public RepoJSON testRepos()  {	
		 RepoJSON repository = new RepoJSON();
		 repository.setContext("http://re3data.org/");
		 repository.setType("repository"); 
		 repository.setOrgIdentifier("https://www.ideals.illinois.edu");
		 repository.setRepositoryName("IDEALS"); 
		 return repository;
	 }	 
	 
	@GET
	@Path("/byid")
	public byte[] getRepoID(
		@QueryParam("id") String repID)  {
		 String repPath = "../../sead-json/" + repID + ".json";
		 java.nio.file.Path path = Paths.get(repPath);
		 byte[] data = new byte[] {'*'};
		try {
			data = Files.readAllBytes(path);
		} catch (IOException e) {

		}	 
		 return data;
	}
	
	@GET
	@Path("/params")
	public Response getRepoValue(
		@QueryParam("from") int from,
		@QueryParam("to") int to,
		@QueryParam("orderBy") List<String> orderBy) {
		return Response
		   .status(200)
		   .entity("getUsers is called, from : " + from + ", to : " + to
			+ ", orderBy" + orderBy.toString()).build();
	}
	
}
