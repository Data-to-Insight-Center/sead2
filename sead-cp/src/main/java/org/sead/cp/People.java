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

@Path("")
public abstract class People {

	/**
	 * 
	 * Register a new Person
	 * 
	 * @param { "provider": <provider>, "identifier", <id> }
	 * 
	 * 
	 * @return 200: {response: "success", id : &lt;ID&gt;} <br>
	 *         400 Bad Request: {response: "failure", reason : &lt;string&gt;} <br>
	 *         409 Conflict: {response: "failure", reason : &lt;string&gt;} 500:
	 *         Failure {response: "failure", reason : &lt;string&gt;}
	 * 
	 */
	@POST
	@Path("/people")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public abstract Response registerPerson(String personString);

	/**
	 * Return the list of people
	 * 
	 * @return [ {"name":<name>, "identifier":<id>}, ... ]
	 */
	@GET
	@Path("/people")
	@Produces(MediaType.APPLICATION_JSON)
	public abstract Response getPeopleList();

	/**
	 * Return the profile for a given person
	 * 
	 * @param id
	 *            the assigned person ID
	 * 
	 * @return : json-ld profile document - as harvested
	 */
	@GET
	@Path("/people/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public abstract Response getPersonProfile(@PathParam("id") String id);

	/**
	 * Request to update the profile for a given person
	 * 
	 * @param id
	 *            the assigned person ID
	 * 
	 * @return 200 OK: {response: "success", id : &lt;ID&gt;} <br>
	 *         400 Bad Request: {response: "failure", reason : &lt;string&gt;} <br>
	 */
	@PUT
	@Path("/people/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public abstract Response updatePersonProfile(@PathParam("id") String id);

	/**
	 * Unregister a person and remove their profile
	 * 
	 * @param id
	 *            the assigned person ID
	 * 
	 * @return 200 OK: {response: "success", id : &lt;ID&gt;} <br>
	 *         400 Bad Request: {response: "failure", reason : &lt;string&gt;} <br>
	 */
	@DELETE
	@Path("/people/{id}")
	public abstract Response unregisterPerson(@PathParam("id") String id);

}
