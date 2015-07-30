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

@Path("/repositories")
public abstract class Repositories {


	/**
	 * 
	 * Register a new Repository
	 * 
	 * @param registerRepository
	 *            {initialProfile, &lt;initialProfile&gt;}
	 * 
	 * <br>
	 *            where initialProfile is a json profile document for the new
	 *            repository.
	 * 
	 * @see Example input file: < a href = "./demo/ideals.json">./demo/ideals.json</a>
	 * 
	 * @return 200: {response: "success", id : &lt;ID&gt;} <br>
	 *         400 Bad Request: {response: "failure", reason : &lt;string&gt;} <br>
	 *         409 Conflict: {response: "failure", reason : &lt;string&gt;}
	 * 
	 */
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public abstract Response registerRepository(String profileString);

	/**
	 * Return the list of repositories
	 * 
	 * @return [
	 * 			{"orgidentifier":<id>, "repositoryUrl":<url>},
	 * 			...
	 * 		   ]
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public abstract Response getRepositoryList();

	/**
	 * Return the profile for a given repository
	 * 
	 * @param id
	 *            the assigned repository ID
	 * 
	 * @return : json-ld profile document - as submitted
	 */
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public abstract Response getRepositoryProfile(@PathParam("id") String id);

	/**
	 * Update the profile for a given repository
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
	public abstract Response setRepositoryProfile(@PathParam("id") String id, String profile);

	/**
	 * Unregister a repository and remove its profile
	 * 
	 * @param id
	 *            the assigned repository ID
	 * 
	 * @return 200 OK: {response: "success", id : &lt;ID&gt;} <br>
	 *         400 Bad Request: {response: "failure", reason : &lt;string&gt;} <br>
	 */
	@DELETE
	@Path("/{id}")
	public abstract Response unregisterRepository(@PathParam("id") String id);

	/**
	 * Return the set or ROs for a given repository
	 * 
	 * @param  - filter by status?
	 * 
	 * @return :[ 
	 * 				{"identifier":<id>, "latest status":status},
	 * 				...
	 * 		   	]
	 */
	@GET
	@Path("/{id}/researchobjects")
	@Produces(MediaType.APPLICATION_JSON)
	public abstract Response getROsByRepository(@PathParam("id") String id);
	
}
