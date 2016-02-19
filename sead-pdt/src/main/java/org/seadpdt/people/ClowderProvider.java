/*
 *
 * Copyright 2015 The Trustees of Indiana University
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
 * @author charmadu@umail.iu.edu
 */

package org.seadpdt.people;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.bson.Document;
import org.json.JSONObject;
import org.seadpdt.PeopleServices;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClowderProvider extends Provider {

    @Override
    public Document getExternalProfile(JSONObject person)
            throws RuntimeException {
        String id = person.getString(PeopleServices.identifier);
        Document profile = null;
        if (id != null) {
            // id should be canonical already
            id = id.substring("http://sead2-beta.ncsa.illinois.edu/api/users/".length());

            profile = generateProfile(id);
        }
        return profile;
    }

    // Retrieve external profile and create the standard internal form. Input is
    // id in canonical form
    private Document generateProfile(String id) {

        Document personDocument = new Document();
        personDocument.put(PeopleServices.provider, getProviderName());

        Document rawDocument = getRawProfile(id);
        personDocument.put("@id", getCanonicalId(rawDocument.get("id").toString()));
        if (rawDocument.get("firstName") != null) {
            personDocument.put("givenName", rawDocument.get("firstName"));
        }
        if (rawDocument.get("lastName") != null) {
            personDocument.put("familyName", rawDocument.get("lastName"));
        }
        if (rawDocument.get("email") != null) {
            personDocument.put("email", rawDocument.get("email"));
        }
        if (rawDocument.get("affiliation") != null) {
            personDocument.append("affiliation", rawDocument.get("affiliation"));
        }
        personDocument.put("PersonalProfileDocument",getCanonicalId(rawDocument.get("id").toString()));

        return personDocument;
    }

    public static Document getRawProfile(String rawID) {
        Client client = Client.create();

        WebResource webResource = client.resource("http://sead2-beta.ncsa.illinois.edu/api/users/"
                + rawID);

        ClientResponse response = webResource.accept("application/json")
                .get(ClientResponse.class);

        if (response.getStatus() != 200) {
            throw new RuntimeException("" + response.getStatus());
        }

        return Document.parse(response.getEntity(String.class));
    }

    // Parse the string to get the internal ID that the profile would have been
    // stored under.
    // Check whether ID contains /api/users and then alpha-numeric ID
    public String getCanonicalId(String personID) {
        String canonical = null;

        if (personID.matches("^.*/api/users/[a-zA-Z0-9]+$")) {
            Matcher m = Pattern.compile("^.*/api/users/([a-zA-Z0-9]+)$").matcher(personID);
            if (m.find( )) {
                return "http://sead2-beta.ncsa.illinois.edu/api/users/" + m.group(1);
            }
        } else if (personID.matches("^[0-9]+$")) {
            return "http://sead2-beta.ncsa.illinois.edu/api/users/" + personID;
        }

        return canonical;
    }

    @Override
    public String getProviderName() {
        return "Clowder";
    }
}
