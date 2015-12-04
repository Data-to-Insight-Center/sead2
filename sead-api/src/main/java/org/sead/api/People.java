/*
 *
 * Copyright 2015 University of Michigan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 *
 * @author myersjd@umich.edu
 */

package org.sead.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/people")
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
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public abstract Response registerPerson(String personString);

	/**
	 * Return the list of people
	 * 
	 * @return [ {"name":<name>, "identifier":<id>}, ... ]
	 */
	@GET
	@Path("/")
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
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public abstract Response getPersonProfile(@Encoded @PathParam("id") String id);

    /**
     * Return the raw profile of a given person. For example, the complete
     * Orcid profile of the person.
     *
     * @param id
     *            the assigned person ID
     *
     * @return : json-ld profile document - as harvested
     */
    @GET
    @Path("/{id}/raw")
    @Produces(MediaType.APPLICATION_JSON)
    public abstract Response getRawPersonProfile(@Encoded @PathParam("id") String id);

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
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public abstract Response updatePersonProfile(@Encoded @PathParam("id") String id);

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
	@Path("/{id}")
	public abstract Response unregisterPerson(@Encoded @PathParam("id") String id);

}
