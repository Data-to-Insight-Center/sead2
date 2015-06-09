package org.seadpdt;

import java.util.List;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class ORCIDcalls {


	  public static String getORCID() {
		  String output = "";
			try {
		 
				Client client = Client.create();
		 
				WebResource webResource = client
				   .resource("http://pub.orcid.org/v1.1/0000-0002-1778-6252/orcid-bio");
		 
				ClientResponse response = webResource.accept("application/xml")
		                   .get(ClientResponse.class);
		 
				if (response.getStatus() != 200) {
				   throw new RuntimeException("Failed : HTTP error code : "
					+ response.getStatus());
				}
		 
				output = response.getEntity(String.class);
		 
				System.out.println("Output from Server .... \n");
				System.out.println(output);
		 
			  } catch (Exception e) {
		 
				e.printStackTrace();
		 
			  }
			return output;
		 
			}	

	
	
}
