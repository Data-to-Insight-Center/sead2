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
import com.sun.research.ws.wadl.Resource;
import com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import org.bson.Document;
import org.json.JSONObject;
import org.seadpdt.people.GooglePlusProvider;
import org.seadpdt.people.LinkedInProvider;
import org.seadpdt.people.OrcidProvider;
import org.seadpdt.people.Profile;
import org.seadpdt.people.Provider;
import org.seadpdt.util.MongoDB;

import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Providers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;

@Path("/people")
public class PeopleServices {

	public static final String identifier = "identifier";
	public static final String provider = "provider";
	private static MongoDatabase db = MongoDB.getServicesDB();
	private static MongoCollection<Document> peopleCollection = db
			.getCollection(MongoDB.people);
	private CacheControl control = new CacheControl();

	public PeopleServices() {
		control.setNoCache(true);
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public static Response registerPerson(String personString) {

		JSONObject person = new JSONObject(personString);
		Provider p = null;

		if (person.has(provider)) {
			p = Provider.getProvider((String) person.get(provider));

		}
		if (!person.has(identifier) || p == null) {
			return Response
					.status(Status.BAD_REQUEST)
					.entity(new BasicDBObject("Failure",
							"Invalid request format")).build();
		}

		String newID = p.getCanonicalId((String) person.get(identifier));
		person.put(identifier, newID);

		FindIterable<Document> iter = peopleCollection.find(new Document("@id",
				newID));
		if (iter.iterator().hasNext()) {
			return Response
					.status(Status.CONFLICT)
					.entity(new BasicDBObject("Failure",
							"Person with Identifier " + newID
									+ " already exists")).build();
		} else {
			URI resource = null;
			try {

				Document profileDocument = p.getExternalProfile(person);
				peopleCollection.insertOne(profileDocument);
				resource = new URI("./" + profileDocument.getString("@id"));
			} catch (Exception r) {
				return Response
						.serverError()
						.entity(new BasicDBObject("failure",
								"Provider call failed with status: "
										+ r.getMessage())).build();
			}

			try {
				resource = new URI("./" + newID);
			} catch (URISyntaxException e) {
				// Should not happen given simple ids
				e.printStackTrace();
			}
			return Response.created(resource)
					.entity(new Document("identifier", newID)).build();
		}
	}

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPeopleList() {
		FindIterable<Document> iter = peopleCollection.find();
		iter.projection(getBasicPersonProjection());

		MongoCursor<Document> cursor = iter.iterator();
		ArrayList<Object> array = new ArrayList<Object>();
		while (cursor.hasNext()) {
			Document next = cursor.next();
			array.add(next);
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
		Document document = retrieveProfile(id);
		if (document != null) {
			return Response.ok(document.toJson()).cacheControl(control).build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updatePersonProfile(@PathParam("id") String id) {
		Profile profile = null;
		try {
			profile = Provider.findCanonicalId(id);
		} catch (Exception e) {
			return Response
					.status(javax.ws.rs.core.Response.Status.CONFLICT)
					.entity(new BasicDBObject("failure", "Ambiguous identifier"))
					.build();
		}
		if (profile == null) {
			return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND)
					.build();
		}
		id = profile.getIdentifier();
		Document profileDoc = retrieveProfile(id);
		if (profileDoc != null) {
			String providerName = profileDoc.getString(provider);
			try {
				profileDoc = Provider.getProvider(providerName)
						.getExternalProfile(profile.asJson());
			} catch (RuntimeException r) {
				return Response
						.serverError()
						.entity(new BasicDBObject("failure",
								"Provider call failed with status: "
										+ r.getMessage())).build();
			}

			peopleCollection.replaceOne(new Document("@id", id), profileDoc);
			return Response.status(Status.OK).build();

		} else {
			return Response.status(Status.NOT_FOUND).build();

		}
	}

	@DELETE
	@Path("/{id}")
	public Response unregisterPerson(@PathParam("id") String id) {
		DeleteResult result = peopleCollection
				.deleteOne(new Document("@id", id));
		if (result.getDeletedCount() == 0) {
			return Response.status(Status.NOT_FOUND).build();
		} else {
			return Response.status(Status.OK).build();
		}
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

	static protected Document getBasicPersonProjection() {
		return new Document("givenName", 1).append("familyName", 1)
				.append("@id", 1).append("email", 1).append("affiliation", 1)
				.append("PersonalProfileDocument", 1).append("_id", 0);
	}

	static Document retrieveProfile(String id) {
		Document document = null;
		FindIterable<Document> iter = peopleCollection.find(new Document("@id",
				id));
		iter.projection(getBasicPersonProjection());
		if (iter.first() != null) {
			document = iter.first();
			document.put("@context", getPersonContext());
		}
		return document;
	}

}
