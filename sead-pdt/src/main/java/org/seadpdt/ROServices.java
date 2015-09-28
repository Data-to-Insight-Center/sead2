/*
 *
 * Copyright 2015 The Trustees of Indiana University, 2015 University of Michigan
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
 * @author myersjd@umich.edu
 * @author smccaula@indiana.edu
 */

package org.seadpdt;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.util.JSON;
import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.filter.ClientFilter;
import org.bson.Document;
import org.bson.types.BasicBSONList;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.seadpdt.util.Constants;
import org.seadpdt.util.MongoDB;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Date;
import java.text.DateFormat;
import java.util.*;

@Path("/researchobjects")

public class ROServices {

    private MongoClient mongoClient = null;
    private MongoDatabase db = null;
    private MongoCollection<Document> publicationsCollection = null;
    private MongoCollection<Document> peopleCollection = null;
    private CacheControl control = new CacheControl();

    public ROServices() {
        db = MongoDB.getServicesDB();
        publicationsCollection = db.getCollection(MongoDB.researchObjects);
        peopleCollection = db.getCollection(MongoDB.people);
        control.setNoCache(true);
    }


    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response startROPublicationProcess(String publicationRequestString, @QueryParam("requestUrl") String requestURL) {
        String messageString = null;
        Document request = Document.parse(publicationRequestString);
        Document content = (Document) request.get("Aggregation");
        if (content == null) {
            messageString += "Missing Aggregation";
        }
        Document preferences = (Document) request.get("Preferences");
        if (preferences == null) {
            messageString += "Missing Preferences";
        }
        Object repository = request.get("Repository");
        if (repository == null) {
            messageString += "Missing Respository";
        }
        //Document stats = (Document) request.get("Aggregation Statistics");
        //if (stats == null) {
        //    messageString += "Missing Statistics";
        //}
        if (messageString == null) {
            // Get organization from profile(s)
            // Add to base document
            Object creatorObject = content.get("Creator");
            String ID = (String) content.get("Identifier");
            BasicBSONList affiliations = new BasicBSONList();
            if (creatorObject != null) {
                if (creatorObject instanceof ArrayList) {
                    Iterator<String> iter = ((ArrayList<String>) creatorObject)
                            .iterator();

                    while (iter.hasNext()) {
                        String creator = iter.next();
                        Set<String> orgs = getOrganizationforPerson(creator);
                        if (!orgs.isEmpty()) {
                            affiliations.addAll(orgs);
                        }
                    }

                } else {
                    // BasicDBObject - single value
                    Set<String> orgs = getOrganizationforPerson((String) creatorObject);
                    if (!orgs.isEmpty()) {
                        affiliations.addAll(orgs);
                    }
                }
            }

            request.append("Affiliations", affiliations);

            // Add first status message

            List<DBObject> statusreports = new ArrayList<DBObject>();
            DBObject status = BasicDBObjectBuilder
                    .start()
                    .add("date",
                            DateFormat.getDateTimeInstance().format(
                                    new Date(System.currentTimeMillis())))
                    .add("reporter", "SEAD-CP")
                    .add("stage", "Receipt Acknowledged")
                    .add("message",
                            "request recorded and processing will begin").get();
            statusreports.add(status);
            request.append("Status", statusreports);
            // Create initial status message - add
            // Add timestamp
            // Generate ID - by calling Workflow?
            // Add doc, return 201

            String newMapURL = requestURL + "/" + ID + "/oremap";
            content.put("@id", newMapURL+ "#aggregation");

            publicationsCollection.insertOne(request);
            URI resource = null;
            try {
                resource = new URI("./" + ID);
            } catch (URISyntaxException e) {
                // Should not happen given simple ids
                e.printStackTrace();
            }
            return Response.created(resource)
                    .entity(new Document("identifier", ID)).build();
        } else {
            return Response.status(ClientResponse.Status.BAD_REQUEST)
                    .entity(new BasicDBObject("Failure", messageString))
                    .build();
        }
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getROsList() {
        FindIterable<Document> iter = publicationsCollection.find();
        iter.projection(new Document("Status", 1).append("Repository", 1)
                .append("Aggregation.Identifier", 1)
                .append("Aggregation.Title", 1).append("_id", 0));
        MongoCursor<Document> cursor = iter.iterator();
        JSONArray array = new JSONArray();
        while (cursor.hasNext()) {
            array.put(JSON.parse(cursor.next().toJson()));
        }
        return Response.ok(array.toString()).cacheControl(control).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getROProfile(@PathParam("id") String id) {

        FindIterable<Document> iter = publicationsCollection.find(new Document(
                "Aggregation.Identifier", id));
        if (iter == null) {
            return Response.status(ClientResponse.Status.NOT_FOUND).build();
        }
        Document document = iter.first();
        if (document == null) {
            return Response.status(ClientResponse.Status.NOT_FOUND).build();
        }
        //Internal meaning only - strip from exported doc
        document.remove("_id");
        Document aggDocument = (Document) document.get("Aggregation");
        aggDocument.remove("authoratativeMap");
        return Response.ok(document.toJson()).cacheControl(control).build();
    }

    @POST
    @Path("/{id}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setROStatus(@PathParam("id") String id, String state) {
        try {
            Document statusUpdateDocument = Document.parse(state);
            statusUpdateDocument.append(
                    "date",
                    DateFormat.getDateTimeInstance().format(
                            new Date(System.currentTimeMillis())));
            UpdateResult ur = publicationsCollection.updateOne(new Document(
                    "Aggregation.Identifier", id), new BasicDBObject("$push",
                    new BasicDBObject("Status", statusUpdateDocument)));
            if (ur.wasAcknowledged()) {
                return Response.status(ClientResponse.Status.OK).build();

            } else {
                return Response.status(ClientResponse.Status.NOT_FOUND).build();

            }
        } catch (org.bson.BsonInvalidOperationException e) {
            return Response.status(ClientResponse.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("/{id}/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getROStatus(@PathParam("id") String id) {
        FindIterable<Document> iter = publicationsCollection.find(new Document(
                "Aggregation.Identifier", id));
        iter.projection(new Document("Status", 1).append("_id", 0));

        Document document = iter.first();
        document.remove("_id");
                return Response.ok(document.toJson()).cacheControl(control).build();
    }

    private Set<String> getOrganizationforPerson(String personID) {
        Set<String> orgs = new HashSet<String>();
        ;
        if (personID.startsWith("orcid.org/")) {
            personID = personID.substring("orcid.org/".length());
            FindIterable<Document> iter = peopleCollection.find(new Document(
                    "orcid-profile.orcid-identifier.path", personID));
            // FixMe: NeverFail
            if (iter.first() == null) {
                new PeopleServices().registerPerson("{\"provider\": \"ORCID\", \"identifier\":\"" + personID + "\" }");
                iter = peopleCollection.find(new Document(
                        "orcid-profile.orcid-identifier.path", personID));
            }

            iter.projection(new Document(
                    "orcid-profile.orcid-activities.affiliations.affiliation.organization.name",
                    1).append("_id", 0));
            MongoCursor<Document> cursor = iter.iterator();
            if (cursor.hasNext()) {
                Document affilDocument = cursor.next();
                Document profile = (Document) affilDocument
                        .get("orcid-profile");

                if (profile == null)  return  orgs;
                Document activitiesDocument = (Document) profile
                        .get("orcid-activities");

                if (activitiesDocument == null)  return  orgs;
                Document affiliationsDocument = (Document) activitiesDocument
                        .get("affiliations");

                if (affiliationsDocument == null)  return  orgs;
                ArrayList orgList = (ArrayList) affiliationsDocument
                        .get("affiliation");
                System.out.println(orgList.size());
                for (Object entry : orgList) {
                    Document org = (Document) ((Document) entry)
                            .get("organization");
                    orgs.add((String) org.getString("name"));
                }
            }
			/*
			 * JSONArray array = new JSONArray(); while(cursor.hasNext()) {
			 * array.put(JSON.parse(cursor.next().toJson())); }
			 */

        }
        return orgs;

    }
	  
}
