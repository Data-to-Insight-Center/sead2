package org.seadpdt;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.seadpdt.Repository;

@Path("/PDT")
public class SeadPDT {
 
	 @GET
	 @Path("/repo")
	 @Produces(MediaType.APPLICATION_JSON)
	 public Repository getRepo(@PathParam("varX") String varX,
			    @PathParam("varY") String varY) {	 
		 Repository repository = new Repository();
		 repository.setContext("http://re3data.org/");
		 repository.setType("repository"); 
		 repository.setOrgIdentifier("https://www.ideals.illinois.edu");
		 repository.setRepositoryName("IDEALS"); 
		 return repository;
	 }
 
}