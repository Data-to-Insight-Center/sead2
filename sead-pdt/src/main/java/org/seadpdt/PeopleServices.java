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
import org.seadpdt.RepoJSON;

@Path("/people")

public class PeopleServices {

	 @GET
	 @Path("/list")
	 @Produces(MediaType.APPLICATION_JSON)
	 public byte[] listRepos()  {	
		 
		 java.nio.file.Path path = Paths.get("../../sead-json/people.json");
		 byte[] data = new byte[] {'*'};
		try {
			data = Files.readAllBytes(path);
		} catch (IOException e) {

		}		 
		 return data;
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
	
}
