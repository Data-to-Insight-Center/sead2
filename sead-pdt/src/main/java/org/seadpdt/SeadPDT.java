package org.seadpdt;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.seadpdt.Repository;

@Path("/repo")
public class SeadPDT {
 
	 @GET
	 @Path("/list")
	 @Produces(MediaType.APPLICATION_JSON)
	 public byte[] listRepos() throws IOException {	
		 
		 java.nio.file.Path path = Paths.get("../../sead-json/list.json");
		 byte[] data = Files.readAllBytes(path);
		 
		 return data;
	 }
 
	 @GET
	 @Path("/test")
	 @Produces(MediaType.APPLICATION_JSON)
	 public Repository testRepos()  {	
		 Repository repository = new Repository();
		 repository.setContext("http://re3data.org/");
		 repository.setType("repository"); 
		 repository.setOrgIdentifier("https://www.ideals.illinois.edu");
		 repository.setRepositoryName("IDEALS"); 
		 return repository;
	 }	 
	 
	@GET
	@Path("/byid")
	public Response getRepoID(
		@QueryParam("from") int from,
		@QueryParam("to") int to,
		@QueryParam("orderBy") List<String> orderBy) {
		return Response
		   .status(200)
		   .entity("getUsers is called, from : " + from + ", to : " + to
			+ ", orderBy" + orderBy.toString()).build();
	}
	
	@GET
	@Path("/byvalue")
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
