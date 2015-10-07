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
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import org.bson.Document;
import org.json.JSONObject;
import org.seadpdt.util.MongoDB;

import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

@Path("/people")

public class PeopleServices {

    private MongoDatabase db = null;
    private MongoCollection<Document> peopleCollection = null;
    private CacheControl control = new CacheControl();

    public PeopleServices() {
        db = MongoDB.getServicesDB();
        peopleCollection = db.getCollection(MongoDB.people);
        control.setNoCache(true);
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerPerson(String personString) {

        JSONObject person = new JSONObject(personString);

        if (!person.has("identifier") || !person.has("provider")) {
            return Response.status(Status.BAD_REQUEST).entity(new BasicDBObject("Failure", "Invalid request format")).build();
        }

        String newID = (String) person.get("identifier");
        FindIterable<Document> iter = peopleCollection.find(new Document(
                "orcid-profile.orcid-identifier.path", newID));
        if (iter.iterator().hasNext()) {
            return Response.status(Status.CONFLICT).entity(new BasicDBObject("Failure", "Person with ORCID Identifier " + newID + " already exists")).build();
        } else {
            if (person.get("provider").equals("ORCID")) {
                String orcidID = (String) person.get("identifier");
                String profile = null;
                try {
                    profile = getOrcidProfile(orcidID);
                } catch (RuntimeException r) {
                    return Response
                            .serverError()
                            .entity(new BasicDBObject("failure",
                                    "Provider call failed with status: "
                                            + r.getMessage())).build();
                }
                peopleCollection.insertOne(Document.parse(profile));
                URI resource = null;
                try {
                    resource = new URI("./" + newID);
                } catch (URISyntaxException e) {
                    // Should not happen given simple ids
                    e.printStackTrace();
                }
                return Response.created(resource)
                        .entity(new Document("identifier", newID)).build();
            } else {
                return Response.status(Status.BAD_REQUEST).entity(new BasicDBObject("Failure", "Provider " + person.get("provider") + " not supported")).build();
            }
        }
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPeopleList() {
        FindIterable<Document> iter = peopleCollection.find();
        iter.projection(getOrcidPersonProjection());

        MongoCursor<Document> cursor = iter.iterator();
        ArrayList<Object> array = new ArrayList<Object>();
        while (cursor.hasNext()) {
            Document next = cursor.next();
            array.add(getPersonInfo(next));
        }
        Document peopleDocument = new Document();
        peopleDocument.put("persons", array);
        peopleDocument.put("@context", getPersonContext());
        return Response.ok(peopleDocument.toJson()).cacheControl(control)
                .build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPersonProfile(@PathParam("id") String id) {
        FindIterable<Document> iter = peopleCollection.find(new Document(
                "orcid-profile.orcid-identifier.path", id));
        if(iter.first() != null) {
            iter.projection(getOrcidPersonProjection());
            Document document = getPersonInfo(iter.first());
            document.put("@context", getPersonContext());
            return Response.ok(document.toJson()).cacheControl(control).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/{id}/raw")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRawPersonProfile(@PathParam("id") String id) {
        FindIterable<Document> iter = peopleCollection.find(new Document(
                "orcid-profile.orcid-identifier.path", id));

        if(iter.first() != null) {
            Document document = iter.first();
            return Response.ok(document.toJson()).cacheControl(control).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePersonProfile(@PathParam("id") String id) {
        FindIterable<Document> iter = peopleCollection.find(new Document(
                "orcid-profile.orcid-identifier.path", id));

        if (iter.iterator().hasNext()) {
            String profile = null;
            try {
                profile = getOrcidProfile(id);
            } catch (RuntimeException r) {
                return Response
                        .serverError()
                        .entity(new BasicDBObject("failure",
                                "Provider call failed with status: "
                                        + r.getMessage())).build();
            }

            peopleCollection.replaceOne(new Document(
                    "orcid-profile.orcid-identifier.path", id), Document.parse(profile));
            return Response.status(Status.OK).build();

        } else {
            return Response.status(Status.NOT_FOUND).build();

        }
    }

    @DELETE
    @Path("/{id}")
    public Response unregisterPerson(@PathParam("id") String id) {
        DeleteResult result = peopleCollection.deleteOne(new Document("orcid-profile.orcid-identifier.path", id));
        if(result.getDeletedCount() == 0 ){
            return Response.status(Status.NOT_FOUND).build();
        } else {
            return Response.status(Status.OK).build();
        }
    }

    static protected Document getPersonInfo(Document next) {
        Document standardForm = new Document();

        String id = next.get("orcid-profile") != null
                && ((Document) next.get("orcid-profile")).get("orcid-identifier") != null
                && ((Document) ((Document) next.get("orcid-profile")).get("orcid-identifier")).get("path") != null
                ? ((Document) ((Document) next.get("orcid-profile")).get("orcid-identifier")).get("path").toString() : "";
        standardForm.put("@id", id);

        Document detailsDocument = next.get("orcid-profile") != null
                && ((Document) next.get("orcid-profile")).get("orcid-bio") != null
                && ((Document) ((Document) next.get("orcid-profile")).get("orcid-bio")).get("personal-details") != null
                ? ((Document) ((Document) ((Document) next.get("orcid-profile")).get("orcid-bio")).get("personal-details")) : null;
        if (detailsDocument != null) {
            String givenName = detailsDocument.get("given-names") != null
                    && ((Document) detailsDocument.get("given-names")).get("value") != null
                    ? ((Document) detailsDocument.get("given-names")).get("value").toString() : "";
            String familyName = detailsDocument.get("family-name") != null
                    && ((Document) detailsDocument.get("family-name")).get("value") != null
                    ? ((Document) detailsDocument.get("family-name")).get("value").toString() : "";

            standardForm.put("givenName", givenName);
            standardForm.put("familyName", familyName);
        }

        ArrayList emails = next.get("orcid-profile") != null
                && ((Document) next.get("orcid-profile")).get("orcid-bio") != null
                && ((Document) ((Document) next.get("orcid-profile")).get("orcid-bio")).get("contact-details") != null
                && ((Document) ((Document) ((Document) next.get("orcid-profile")).get("orcid-bio")).get("contact-details")).get("email") != null
                ? ((ArrayList<?>) ((Document) ((Document) ((Document) next.get("orcid-profile")).get("orcid-bio")).get("contact-details")).get("email"))
                : new ArrayList();

        if (!emails.isEmpty()) {
            standardForm.put("email", ((Document) emails.get(0)).get("value"));
        }

        String uri = next.get("orcid-profile") != null
                && ((Document) next.get("orcid-profile")).get("orcid-identifier") != null
                && ((Document) ((Document) next.get("orcid-profile")).get("orcid-identifier")).get("uri") != null
                ? ((Document) ((Document) next.get("orcid-profile")).get("orcid-identifier")).get("uri").toString() : "";
        standardForm.put("PersonalProfileDocument", uri);

        try {
            ArrayList<Document> affiliationsList = (ArrayList<Document>) ((Document) ((Document) ((Document) next
                    .get("orcid-profile")).get("orcid-activities"))
                    .get("affiliations")).get("affiliation");
            StringBuffer affs = new StringBuffer();
            for (Document affiliationDocument : affiliationsList) {
                if (affiliationDocument.getString("type").equals("EMPLOYMENT")
                        && (affiliationDocument.get("end-date") == null)) {
                    if (affs.length() != 0) {
                        affs.append(", ");
                    }
                    affs.append(((Document) affiliationDocument.get("organization"))
                            .getString("name"));

                }
                standardForm.append("affiliation", affs.toString());
            }
        } catch (NullPointerException e) {
            System.out.println("Exception while retrieving Affiliations from ORCID profile");
            e.printStackTrace();
        }

        return standardForm;
    }

    static protected Document getOrcidPersonProjection() {
        return new Document("orcid-profile.orcid-identifier.uri", 1)
                .append("orcid-profile.orcid-identifier.path", 1)
                .append("orcid-profile.orcid-bio.personal-details.given-names",
                        1)
                .append("orcid-profile.orcid-bio.personal-details.family-name",
                        1)
                .append("orcid-profile.orcid-bio.contact-details.email", 1)
                .append("orcid-profile.orcid-activities.affiliations.affiliation",
                        1).append("_id", 0);
    }

    static private Document getPersonContext() {
        Document contextDocument = new Document();
        contextDocument.put("givenName", "http://schema.org/Person/givenName");
        contextDocument
                .put("familyName", "http://schema.org/Person/familyName");
        contextDocument.put("email", "http://schema.org/Person/email");
        contextDocument.put("affiliation",
                "http://schema.org/Person/affiliation");
        contextDocument.put("PersonalProfileDocument",
                "http://schema.org/Thing/mainEntityOfPage");
        return contextDocument;
    }

    private String getOrcidProfile(String id) {

        Client client = Client.create();
        WebResource webResource = client.resource("http://pub.orcid.org/v1.2/"
                + id + "/orcid-profile");

        ClientResponse response = webResource.accept("application/orcid+json")
                .get(ClientResponse.class);

        if (response.getStatus() != 200) {
            throw new RuntimeException("" + response.getStatus());
        }

        return response.getEntity(String.class);
    }


}
