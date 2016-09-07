MatchMaker
===============

Configuration and Installation (As a Web Application)
-----------------
1) Checkout the sead2 repository
~~~
git clone https://github.com/Data-to-Insight-Center/sead2.git
~~~
2) Add Research Object, Repository and People profile templates to the source 
~~~
sead2/standalone-mm/src/main/resources/edu/indiana/d2i/sead/matchmaker/profile
~~~
Sample profiles templates are already available in the source at https://github.com/Data-to-Insight-Center/sead2/tree/master/standalone-mm/src/main/resources/edu/indiana/d2i/sead/matchmaker/profile
</br>
</br>
3) Build the module
~~~
cd /sead2/standalone-mm
mvn clean install -DskipTests=true
~~~
4) Copy the .war file to tomcat/webapps folder
~~~
cp target/matchmaker-1.0.0.war CATALINA_HOME/webapps/mm.war
~~~
5) Unzip mm.war and add Repositories and People profiles to web application
~~~
unzip CATALINA_HOME/webapps/mm.war -d CATALINA_HOME/webapps/mm
cp repositories.json person.json CATALINA_HOME/webapps/mm/WEB-INF/classes/edu/indiana/d2i/sead/matchmaker/profile/
~~~
6) Start tomcat

Use matchmaker - REST API
-----------------
1) Send a POST request to http://&lt;host&gt;:&lt;port&gt;/mm/mm/rest with the following request as the POST body
~~~
{
  "operation": "query",
  "message": {
    "@context": [
      "https://w3id.org/ore/context"
    ],
    "Rights Holder": "https://sead2-beta.ncsa.illinois.edu/api/users/568acec7f26f437b16cafac4",
    "Aggregation": {
      "Uploaded By": "https://sead2-beta.ncsa.illinois.edu/api/users/568acec7f26f437b16cafac4",
      "Title": "test_dataset1 - new-cur-obj-test",
      "Creation Date": "04-03-2016",
      "similarTo": "https://sead2-beta.ncsa.illinois.edu/datasets/56966fc4e4b01d13f5ced4a5",
      "Creator": ["0000-0001-9351-557X"],
      "Abstract": ["Its a simple test"],
      "Identifier": "https://sead2-beta.ncsa.illinois.edu/spaces/curations/56da04d7e4b09d159f672258",
      "Publishing Project": "https://sead2-beta.ncsa.illinois.edu/spaces/56966fa6e4b01d13f5ced4a3",
      "@id": "https://sead2-beta.ncsa.illinois.edu/api/curations/56da04d7e4b09d159f672258/ore#aggregation"
    },
    "Preferences": {
      "Purpose": "Testing-Only",
      "Repository": null
    },
    "Aggregation Statistics": {
      "Data Mimetypes": [
        "image/png",
        "text/plain"
      ],
      "Number of Datasets": 2,
      "Number of Collections": 1,
      "Max Dataset Size": "18317",
      "Total Size": "11000000",
      "Max Collection Depth": "2"
    }
  }
}
~~~
The result would be;
~~~
[
  {
    "Per Rule Scores": [
      {
        "Score": "1",
        "Message": "Total size is acceptable (<= 10000000000)",
        "Rule Name": "Maximum Total Size"
      },
      {
        "Score": "-1",
        "Message": "Collection depth is not acceptable (<= 1)",
        "Rule Name": "Maximum Collection Depth"
      }
    ],
    "repositoryName": "Inter-university Consortium for Political and Social Research"
  },
  {
    "Per Rule Scores": [
      {
        "Score": "1",
        "Message": "Total size is acceptable (<= 10000000)",
        "Rule Name": "Maximum Total Size"
      },
      {
        "Score": "1",
        "Message": "Collection depth is acceptable (<= 10)",
        "Rule Name": "Maximum Collection Depth"
      }
    ],
    "repositoryName": "SEAD NDS Labs Publisher (Proof-of-Concept)"
  },
  {
    "Per Rule Scores": [
      {
        "Score": "1",
        "Message": "Total size is acceptable (<= 1000000000)",
        "Rule Name": "Maximum Total Size"
      },
      {
        "Score": "1",
        "Message": "Collection depth is acceptable (<= 4)",
        "Rule Name": "Maximum Collection Depth"
      }
    ],
    "repositoryName": "IU SEAD Cloud"
  },
  {
    "Per Rule Scores": [
      {
        "Score": "1",
        "Message": "Total size is acceptable (<= 10000000000)",
        "Rule Name": "Maximum Total Size"
      },
      {
        "Score": "-1",
        "Message": "Collection depth is not acceptable (<= 1)",
        "Rule Name": "Maximum Collection Depth"
      }
    ],
    "repositoryName": "IDEALS"
  }
]
~~~

Use matchmaker - Web UI
-----------------
Web UI of the Matchmaker is available at http://&lt;host&gt;:&lt;port&gt;/mm/crud-table.html. There you can view the rules, edit rules and send requests with Research Object profiles to get matched repositories. 
</br>
</br>
......................................................................................
</br>

Introduction
-----------------
Matchmaker is a stand-alone service that continuously reads all the preferences and constraints and candidate entities, matching and ranking candidate entities that satisfies the preferences and constraints.

Motivating Use Case
-----------------
The Matchmaker is used in the SEAD Virtual Archive to dynamically determine the destination preservation repository for a published Research Object (RO). The Matchmaker takes as input the preferences and constraints of data producers/creators/authors (e.g., people) and the data centers/digital repositories (JSON-LD format), as well as formal rules (Drools DRL format) that are executed by the JBoss Drools rule engine to find the optimum home for a deposited RO.  

Important Design Decisions
-----------------
* Matchmaker leverages Drools rule engine to make matchmaking decisions.
* Matchmaker has a plugin mechanism that allows new rules and optionally associated helper java classes to be added.
* Matchmaker has no hardcoded keywords of any kind, nor does it restrict to a particular JSON-LD schema. It generates on-the-fly POJO classes source code based on user input JSON-LD files, compile to customized jar file along with user defined rules and associated helper java classes, and instantiate POJOs (based on JSON-LD files) without pre-defined schema. However, if a JSON-LD schema is pre-defined, Matchmaker can generate POJO code and compile jar files offline, and only instantiate POJO objects (based on JSON-LD files) at runtime, making matchmaking process much faster.
* The Drools rules adopt "when-then" logic. In Matchmaker, each rule invokes one or more of the following Java methods in the "then" statement to update the candidate list. The logic behind this rule invocation process is that the initial candidate list is always a full list. By applying rules, the candidate list will be updated to a subset of the full candidate list. Therefore, the order of rules will have no impact to the final result so that it ease the burden of rule creation/verification. New rules can be added independently, without looking back to the existing rules. 
~~~
restrict() : Restrict candidate list to a given list.
notAllowed(): Remove selected candidates from the candidate list.
preferred(): Tag "preferred" to a list of candidates.
setWeight(): Set weight to a candidate.
addWeight(): Add weight to a candidate.
reduceWeight(): Reduce weight to a candidate.
~~~
Rules
-----------------
A matchmaker rule is essentially a Drools rule. A basic Drools rule is as simple as below:
~~~
rule "name"
    when
        Left Hand Side(LHS)
    then
        Right Hand Side(RHS)
end
~~~
LHS, operates on JavaBean(POJO) objects, is the conditional parts of the rule, which follows a certain syntax. RHS is basically a block that allows dialect specific semantic code to be executed, including java code.

### Sample 1: Java Method Test
~~~
Repositories: A, B, C, D, and E.
ROs: N/A
Persons: N/A
~~~
The matchmaker makes the following initial candidate list:
~~~
{"A":{"weight":0,"priority":0},"B":{"weight":0,"priority":0},"C":{"weight":0,"priority":0},"D":{"weight":0,"priority":0},"E":{"weight":0,"priority":0}}
~~~
6 sample rules are applied. The final result will not be effected by the order of rules.
~~~
1)	Rule: Add weight to B by 3
Output: {"A":{"weight":0,"priority":0},"B":{"weight":3,"priority":0},"C":{"weight":0,"priority":0},"D":{"weight":0,"priority":0},"E":{"weight":0,"priority":0}}
2) Rule: Reduce weight of B by 1
Output: {"A":{"weight":0,"priority":0},"B":{"weight":2,"priority":0},"C":{"weight":0,"priority":0},"D":{"weight":0,"priority":0},"E":{"weight":0,"priority":0}}
3) Rule: Set weight of C to 10
Output: {"A":{"weight":0,"priority":0},"B":{"weight":2,"priority":0},"C":{"weight":10,"priority":0},"D":{"weight":0,"priority":0},"E":{"weight":0,"priority":0}}
4) Rule: C not allowed
Output: {"A":{"weight":0,"priority":0},"B":{"weight":2,"priority":0},"D":{"weight":0,"priority":0},"E":{"weight":0,"priority":0}}
5) Rule: Restricted to A,B,D,F 
Output: {"A":{"weight":0,"priority":0},"B":{"weight":2,"priority":0},"D":{"weight":0,"priority":0}}
6) Rule: A, and B are preferred 
Output: {"A":{"weight":0,"priority":1},"B":{"weight":2,"priority":1},"D":{"weight":0,"priority":0}}
~~~

### Sample 2: Preliminary SEAD Test
* Repositories/Person/RO: 
~~~
* Repositories: 
[
{
  "@context": "http://re3data.org/",
  "@type": "repository",
  "orgidentifier": "http://doi.org/xxxxxxx",
  "repositoryName": "IDEALS",
  "repositoryURL": "https://www.ideals.illinois.edu/",
  "institution": "University of Illinois",
  "subject": "any",
  "versioning": "no",
  "dataAccessType": ["open","restricted"],
  "dataLicenseName": ["other"],
  "contentType": ["csv", "txt", "xml", "html", "tif", "jp2", "aif", "fla", "ogg", "wav", "avi", "mj2"],
  "/maxFileSize": {"unit":"MB", "value": 1000}
},

{
  "@context": "http://re3data.org/",
  "@type": "repository",
  "orgidentifier": "http://doi.org/xxxxxxx",
  "repositoryName": "ICPSR",
  "repositoryURL": "https://www.icpsr.umich.edu/icpsrweb/landing.jsp",
  "institution": "University of Michigan",
  "subject": "Social and Behavioral Sciences",
  "versioning": "yes",
  "dataAccessType": ["open","restricted"],
  "dataLicenseName": ["other"],
  "contentType": ["csv"],
  "/maxFileSize": {"unit":"MB", "value": 2000}
},

{
  "@context": "http://re3data.org/",
  "@type": "repository",
  "orgidentifier": "http://doi.org/xxxxxxx",
  "repositoryName": "IU SDA",
  "repositoryURL": "https://www.indiana.edu",
  "institution": "Indiana University",
  "subject": "any",
  "versioning": "yes",
  "dataAccessType": ["open","restricted"],
  "dataLicenseName": ["other"],
  "contentType": ["any"],
  "/maxFileSize": {"unit":"MB", "value": 3000}
},

{
  "@context": "http://re3data.org/",
  "@type": "repository",
  "orgidentifier": "http://doi.org/xxxxxxx",
  "repositoryName": "D2I",
  "repositoryURL": "https://www.d2i.indiana.edu",
  "institution": "Indiana University D2I",
  "subject": "any",
  "versioning": "yes",
  "dataAccessType": ["open","restricted"],
  "dataLicenseName": ["other"],
  "contentType": ["csv", "txt", "xml", "html", "tif"],
  "/maxFileSize": {"unit":"MB", "value": 3000}
}

]

* RO:

{				
  "@context": "http://schema.org/",				
  "@type": "DataDownload",				
  "name": "Debris Flow Flume",
  "ROID": "http://sample-roid",				
  "description": "A 4-meter diameter, 80-cm wide rotating debris flow flume was constructed at the University of California Richmond Field Station for studying large-scale granular flow phenomena. This dataset covers the experiments conducted in 2007 and 2008, where the primary goal was to study rates and mechanisms of bedrock erosion by debris flows.",				
  "sourceOrganization": "Columbia University",  				
  "author": {				
    "@type": "Person",				
    "name": "Hsu, Leslie",				
    "@id": "http://orcid.org/0000-0002-5353-807X",				
    "email": "lhsu@ldeo.columbia.edu"				
  },				
  "fileSize": {"unit":"MB", "value": 2000},				
  "contentUrl": "http://sead-vivo.d2i.indiana.edu:8080/sead-vivo/individual/n15603",				
  "/subject": "Geophysics",	
  "contentType"	: "tif"		
}		

* Person:

{			
  "@context": "http://schema.org/",			
  "@type": "Person",			
  "name": "Hsu, Leslie",			
  "@id": "http://orcid.org/0000-0002-5353-807X",			
  "affiliation": "Columbia University",			
  "jobTitle": "Associate Research Scientist",			
  "email": "lhsu@ldeo.columbia.edu",			
  "URL": "https://sites.google.com/site/lhsu000001/",			
  "codeRepository": "IDEALS"			
}
~~~
Java source code are generated based on above json files. These Java code are then compiled into .class files and are loaded and instantiated to POJOs, which are fed to the Drools rule engine.

* Rules:
~~~
rule "file size"
	when
		
		repo: Repository()
		researchObject : ResearchObject()
		Ruleset1Utility(computeBinaryUnitConverter(repo.MaxFileSize.unit)*repo.MaxFileSize.value < computeBinaryUnitConverter(researchObject.fileSize.unit)*researchObject.fileSize.value)
		mml: Ruleset1MatchMakingList()
	then
		System.out.println( " Repo " +repo.getRepositoryName()+" not allowed (File Size Restriction)");
		Set<String> notAllowedList= new HashSet<String>();
		notAllowedList.add(repo.getRepositoryName());
		mml.notAllowed(notAllowedList);
		mml.printCandidateList();
		
end

rule "file type"
	when
		repo: Repository()
		not ResearchObject(repo.contentType contains contentType ||repo.contentType contains "any")
		mml: Ruleset1MatchMakingList()
	then
		System.out.println( " Repo " +repo.getRepositoryName()+" not allowed (File Type Restriction)");
		Set<String> notAllowedList= new HashSet<String>();
		notAllowedList.add(repo.getRepositoryName());
		mml.notAllowed(notAllowedList);
		mml.printCandidateList();
end
~~~
The matchmaker makes the following initial candidate list:
~~~
{
  "IDEALS" : {
    "weight" : 0,
    "priority" : 0
  },
  "ICPSR" : {
    "weight" : 0,
    "priority" : 0
  },
  "IU SDA" : {
    "weight" : 0,
    "priority" : 0
  },
  "D2I" : {
    "weight" : 0,
    "priority" : 0
  }
}
~~~
Fire All Rules...
~~~
 Repo IDEALS not allowed (File Size Restriction)
{
  "ICPSR" : {
    "weight" : 0,
    "priority" : 0
  },
  "IU SDA" : {
    "weight" : 0,
    "priority" : 0
  },
  "D2I" : {
    "weight" : 0,
    "priority" : 0
  }
}
 Repo ICPSR not allowed (File Type Restriction)
{
  "IU SDA" : {
    "weight" : 0,
    "priority" : 0
  },
  "D2I" : {
    "weight" : 0,
    "priority" : 0
  }
}
======================
Final Match:
{
  "IU SDA" : {
    "weight" : 0,
    "priority" : 0
  },
  "D2I" : {
    "weight" : 0,
    "priority" : 0
  }
}
======================
~~~
Configuration and Installation (for administrators and users)
-----------------
0) Configuration: Setup RabbitMQ server, and modify self-explanatory configuration files properly.
~~~
vi config/matchmaker.properties
~~~
1) Build code generator that can generate java source code based on a json file that describes multiple json files.
~~~
./build-codegen.sh
~~~
2) Generate java source code for Matchmaker messaging input schema.
~~~
./bin/codegen.sh config/matchmaker_codegen.json
~~~
3) Build matchmaker
~~~
./build-standalone.sh
~~~
4) To add rule jar, generate java source code based on profile schemas(files), and build rule jar, and copy jar file to ./target/, and update config/rule_jars_properties.json
~~~
./bin/codegen.sh plugins/ruleset1/config/codegen.json
cd plugins/ruleset1/
mvn install
cd ../..
cp plugins/ruleset1/target/ruleset1-x.x.x.jar target/
vi config/rule_jars_properties.json
~~~
5) Build matchmaker client
~~~
./build-client.sh
~~~
Use matchmaker
-----------------
1) Start matchmaker service
~~~
nohup ./bin/Matchmaker.sh config/matchmaker.properties > log.txt &
~~~
2) Using matchmaker service
~~~
./bin/MatchmakerClient.sh sync config/matchmaker.properties test/data/query.json
or
./bin/MatchmakerClient.sh async config/matchmaker.properties test/data/query.json
~~~
where test/data/query.json is 
~~~
{
	"operation" : "query",
	"message" : {
		"@context": "http://schema.org/",
  		"@type": "DataDownload",
  		"name": "Debris Flow Flume",
  		"description": "Sample description",
  		"sourceOrganization": "Columbia University",
  		"author": {
    		"@type": "Person",
    		"name": "Hsu, Leslie",
    		"@id": "http://orcid.org/0000-0002-5353-807X",
    		"email": "lhsu@ldeo.columbia.edu"
  		},
  		"fileSize": {"unit":"MB", "value": 2000},
  		"contentUrl": "http://sead-vivo.d2i.indiana.edu:8080/sead-vivo/individual/n15603",
  		"/subject": "Geophysics",
  		"contentType" : "tif"
	}
}
~~~

Development Guideline For External Components (PDT and Repositories)
-----------------
Matchmaker sends request to external components with the following schema:
~~~
{
	"requestID":"String",
	"responseKey": "String",
	"request":{
			"operation" : "String", 
			"message" : "object"
			}
}
~~~
and expect to have response from external components with the following schema:
~~~
{
	"responseID":"String",
	"success": boolean,
	"message" : "object"
}
~~~
To be more specific, the matchmaker,
1) queries the PDT using "query" operation and expects the PDT to return an JSON-LD (described as an object in the schema) as a value to the message attribute.
and 2) deposit to repository using "deposit" operation and expects a repository to return in the message an JSON object with the following schema (under discussion):
~~~
"message":{"status" :"String"}
"message":{"status" :"String","doi" :"String"}
~~~ 
All the communication will be done using messaging system, change the following configuration accordingly to meet the security needs.
~~~
messaging.username=username
messaging.password=password
messaging.hostname=matchmaker-messaging-host
messaging.hostport=5672
messaging.virtualhost=/
messaging.exchangename=ExternalExchange
messaging.queuename=ExternalQueue
messaging.routingkey=ExternalKey
~~~

