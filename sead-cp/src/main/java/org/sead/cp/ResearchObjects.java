package org.sead.cp;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

@Path("/researchobjects")
public abstract class ResearchObjects {

	
	/**
	 * Request publication of a new research object
	 * 
	 * @param publicationRequest
	 *            {coID, &lt;coId&gt;, preferences, {&lt;preferences list&gt;},
	 *            destination, &lt;repositoryId&gt;}
	 * 
	 * <br>
	 *            where coID is a unique ID for the entity the user wants to
	 *            publish. <br>
	 *            preferences is a json list of options chosen from those
	 *            available (see api ____) <br>
	 *            destination is the ID of the repository as defined within SEAD
	 *            (see api _______)
	 * 
	 * @see Example input file: _______ <br>
	 *      Example output file: _______
	 * 
	 * @return 200: {response: "success", id : &lt;ID&gt;} <br>
	 *         400: {response: "failure", reason : &lt;string&gt;} <br>
	 *         401: {response: "failure", reason : &lt;string&gt;}
	 *         409 Conflict: {response: "failure", reason : &lt;string&gt;}
	 * 
	 */
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	abstract public Response startROPublicationProcess(JSONObject publicationRequest);

	/**
	 * Return the list of requests
	 * 
	 * @param - optional param to filter by status?
	 * 
	 * @return [
	 * 			{"identifier":<id>, "latest status":status},
	 * 			...
	 * 		   ]
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public abstract Response getROsList();

	/**
	 * Return the profile and status for a given publication
	 * 
	 * @param id
	 *            the assigned ro/publication ID
	 * 
	 * @return : json-ld profile document 
	 */
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public abstract Response getROProfile(@PathParam("id") String id);

	/**
	 * Update the status for a given publication / ro
	 * 
	 * Body : {
	 * 			"status": <status>,
	 * 			"message":<message>
	 * 		  } 
	 * 
	 * 
	 * @param id
	 *            the assigned repository ID
	 * 
	 * @return 200 OK: {response: "success", id : &lt;ID&gt;} <br>
	 *         400 Bad Request: {response: "failure", reason : &lt;string&gt;} <br>
	 */
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public abstract Response setROStatus(@PathParam("id") String id, String status);

	/**
	 * Rescind a publication request and mark it as obsolete
	 * 
	 * @param id
	 *            the assigned publication/ro ID
	 * 
	 * @return 200 OK: {response: "success", id : &lt;ID&gt;} <br>
	 *         400 Bad Request: {response: "failure", reason : &lt;string&gt;} <br>
	 */
	@DELETE
	@Path("/{id}")
	public abstract Response rescindROPublicationRequest(@PathParam("id") String id);

}
