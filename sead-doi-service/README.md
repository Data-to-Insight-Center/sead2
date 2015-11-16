SEAD DOI Service
================

SEAD DOI Service is created as a wrapper for the EZID REST service which can be used to create and update DOIs. Uses the DataOne EZID library (https://github.com/NCEAS/ezid) with an update to a newer http client library required to use ezid.lib.purdue.edu. (No longer requires CURL).

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

* Also proxied through sead-c3pr at /api/doi if sead-c3pr is deployed.


Create DOI using REST Service:
------------------------------

To create a DOI, a POST request should be sent to http://host:port/sead-doi-service/doi endpoint with the following request body;

{"target":"http://localhost:8080/landing-page/foo.html", "metadata":{"title":"<title>","creator":"<creator>","pubDate":"<publication date>"}, "permanent":"false"}

"target" is a required field and set "permanent" to 'true' only if you need to create a permanent DOI. 
