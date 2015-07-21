package org.seadcp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

@Path("")
public class CPServices {

	/**
	 * 
	 * @param publicationRequest {coID, &lt;coId&gt;, preferences, {&lt;preferences list&gt;}, destination, &lt;repositoryId&gt;}
	 * 
	 * <br>where coID is a unique ID for the entity the user wants to publish.
	 * <br>
	 * preferences is a json list of options chosen from those available (see api ____)
	 * <br>destination is the ID of the repository as defined within SEAD (see api _______)
	 * 
	 *  @see Example input file: _______
	 *  <br>Example output file: _______
	 *  
	 * @return 200: {response: "success", id : &lt;ID&gt;}
	 *  	   <br>400: {response: "failure", reason : &lt;string&gt;}
	 *  	   <br>401: {response: "failure", reason : &lt;string&gt;}
	 *  
	
	 */
	@POST
	@Path("/ro")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response startPublicationProcess(JSONObject publicationRequest) {
		return null;
	}

	
	@GET
	@Path("/status")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStatus() {
		return Response.ok().entity("{\"status\":\"up\"}" ).build();
	}

}
