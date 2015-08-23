package org.sead.matchmaker;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/MM")

public class APIServices {

    /**
     * Ping method to check whether the PDT service is up
     */
    @GET
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    public String ping() {
        return "SEAD Matchmaker API Service is available!";
    }
				  
}
