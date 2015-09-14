package org.sead.matchmaker;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.rabbitmq.client.ConnectionFactory;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;


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
    
    /**
     * return queue information
     * @throws IOException 
     */
    @GET
    @Path("/queue")
	@Produces(MediaType.APPLICATION_JSON)  
    
    public String queue() throws IOException 
    {
    	String output = "";

		String workingDir = System.getProperty("user.dir");
 	
		System.out.println("*****");    	
		System.out.println("SEAD 2 Matchmaker API _ Current working directory : " + workingDir);    	
		System.out.println("*****");    	
	   	
		ConnectionFactory factory = new ConnectionFactory();
		Properties prop = new Properties();
		
        InputStream input =
                APIServices.class.getResourceAsStream("config.properties");		
		
		prop.load(input);
		String buildResource = "";

	    factory.setUsername(prop.getProperty("messaging.username"));
	    factory.setPassword(prop.getProperty("messaging.password"));
	    factory.setVirtualHost(prop.getProperty("messaging.virtualhost"));
	    factory.setHost(prop.getProperty("messaging.hostname"));
	    factory.setPort(Integer.parseInt(prop.getProperty("messaging.hostport")));    	
		
	    buildResource = "http://" + factory.getHost() + ":" + factory.getPort() + "/api/queues/" + factory.getVirtualHost();
	    System.out.println("call: " + buildResource);
	    
	try {
		 				
		Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter(factory.getUsername(), factory.getPassword()));
 
		WebResource webResource = client
		   .resource(buildResource);
 
		ClientResponse response = webResource.accept("application/json")
                   .get(ClientResponse.class);
 
		if (response.getStatus() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
			+ response.getStatus());
		}
 
		output = response.getEntity(String.class);
		 
	  } catch (Exception e) {
 
		e.printStackTrace();
 
	  }    
    
        return output;
    }    
        
}
