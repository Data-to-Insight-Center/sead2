package org.sead.matchmaker.service;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.sun.jersey.api.client.ClientResponse;
import org.bson.BasicBSONObject;
import org.bson.Document;
import org.bson.types.BasicBSONList;
import org.sead.matchmaker.Matcher;
import org.sead.matchmaker.RuleResult;
import org.sead.matchmaker.matchers.*;

import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Path("/ro")
public class MatchMaker {

    private MongoClient mongoClient = null;
    private MongoDatabase db = null;
    private MongoCollection<Document> peopleCollection = null;
    private CacheControl control = new CacheControl();
    Set<Matcher> matchers = new HashSet<Matcher>();

    public MatchMaker() {
        mongoClient = new MongoClient();
        db = mongoClient.getDatabase("seadcp");

        peopleCollection = db.getCollection("people");

        // Build list of Matchers
        matchers.add(new MaxDatasetSizeMatcher());
        matchers.add(new MaxTotalSizeMatcher());
        matchers.add(new DataTypeMatcher());
        matchers.add(new OrganizationMatcher());
        matchers.add(new DepthMatcher());
        matchers.add(new MinimalMetadataMatcher());

        control.setNoCache(true);
    }

    @POST
    @Path("/matchingrepositories")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response makeMatches(String matchRequest) {
        String messageString = null;
        Document request = Document.parse(matchRequest);
        Document content = (Document) request.get("Aggregation");
        if (content == null) {
            messageString += "Missing Aggregation";
        }
        Document preferences = (Document) request.get("Preferences");
        if (preferences == null) {
            messageString += "Missing Preferences";
        }
        Document stats = (Document) request.get("Aggregation Statistics");
        if (stats == null) {
            messageString += "Missing Statistics";
        }

        if (messageString == null) {
            // Get organization from profile(s)
            // Add to base document
            Object creatorObject = content.get("Creator");
            String ID = (String) content.get("Identifier");

            BasicBSONList affiliations = new BasicBSONList();
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

            // Get repository profiles
            // TODO Isuru: call PDT API for this
            FindIterable<Document> iter = db.getCollection("repositories").find();
            // iter.projection(new Document("_id", 0));

            // Create result lists per repository
            // Run matchers
            MongoCursor<Document> cursor = iter.iterator();

            BasicBSONList matches = new BasicBSONList();

            int j = 0;
            while (cursor.hasNext()) {

                BasicBSONObject repoMatch = new BasicBSONObject();
                Document profile = cursor.next();

                repoMatch.put("orgidentifier", profile.get("orgidentifier"));

                BasicBSONList scores = new BasicBSONList();
                int total = 0;
                int i = 0;
                for (Matcher m : matchers) {
                    BasicBSONObject individualScore = new BasicBSONObject();

                    RuleResult result = m.runRule(content, affiliations,
                            preferences, stats, profile);

                    individualScore.put("Rule Name", m.getName());
                    if (result.wasTriggered()) {
                        individualScore.put("Score", result.getScore());
                        total += result.getScore();
                        individualScore.put("Message", result.getMessage());
                    } else {
                        individualScore.put("Score", 0);
                        individualScore.put("Message", "Not Used");
                    }
                    scores.put(i, individualScore);
                    i++;
                }
                repoMatch.put("Per Rule Scores", scores);
                repoMatch.put("Total Score", total);
                matches.put(j, repoMatch);
                j++;
            }
            // Assemble and send

            return Response.ok().entity(matches).build();
        } else {
            return Response.status(ClientResponse.Status.BAD_REQUEST)
                    .entity(new BasicDBObject("Failure", messageString))
                    .build();
        }
    }

    @GET
    @Path("/matchingrepositories/rules")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRulesList() {
        ArrayList<Document> rulesArrayList = new ArrayList<Document>();
        for (Matcher m : matchers) {
            rulesArrayList.add(m.getDescription());
        }
        return Response.ok().entity(rulesArrayList).build();
    }

    private Set<String> getOrganizationforPerson(String personID) {
        Set<String> orgs = new HashSet<String>();
        if (personID.startsWith("orcid.org/")) {
            personID = personID.substring("orcid.org/".length());
            FindIterable<Document> iter = peopleCollection.find(new Document(
                    "orcid-profile.orcid-identifier.path", personID));
            // FixMe: NeverFail
            if (iter == null) {
                // TODO Isuru: call PDT to register person if he's not already there
//                new PeopleImpl().registerPerson(personID);
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

                Document activitiesDocument = (Document) profile
                        .get("orcid-activities");

                Document affiliationsDocument = (Document) activitiesDocument
                        .get("affiliations");

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
