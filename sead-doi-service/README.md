SEAD DOI Service
================

SEAD DOI Service is created as a wrapper for the EZID REST service which can be used to create and update DOIs. 

Steps to build:
---------------

Move to the sead2/sead-doi-service directory and execute the following command.

mvn clean install -DskipTests


Steps to deploy on Tomcat:
--------------------------

* Copy the target/sead-doi-service-2.0.0-SNAPSHOT.war into TOMCAT_HOME/webapps.

	cp target/sead-doi-service-2.0.0-SNAPSHOT.war TOMCAT_HOME/webapps/sead-doi-service.war

* Include the EZID username and password in the configuration file.

	sead-doi-service/WEB-INFclasses/org/seadva/services/util/doi.properties

* Start the server.

* Now the DOI Service API should be accessible through the following URL.

	http://host:port/sead-doi-service/..


Create DOI using REST Service:
------------------------------

To create a DOI, a POST request should be sent to http://host:port/sead-doi-service/doi endpoint with the following request body;

{  
	"target":"http://localhost:8080/landing-page/foo.html",  // required  
	"metadata":  // optional  
		{   
			"title":"<title>",   
			"creator":"<creator>",   
			"pubDate":"<publication date>"  
		},  
	"permanent":"false"  // optional - set this value to true only if you need to create a permanent DOI  
}  


