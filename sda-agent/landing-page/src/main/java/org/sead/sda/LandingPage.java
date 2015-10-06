/*
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
 * @author luoyu@indiana.edu
 * @author isuriara@indiana.edu
 */

package org.sead.sda;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * SDA LandingPage
 */
public class LandingPage extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (request.getParameter("tag") != null) {
            String tag = request.getParameter("tag");
            Shimcalls shim = new Shimcalls();

            JSONObject cp = shim.getResearchObject(tag);
            shim.getObjectID(cp, "@id");
            String oreUrl = shim.getID();
            JSONObject oreFile = shim.getResearchObjectORE(oreUrl);

            JSONObject describes = (JSONObject) oreFile.get("describes");
            Map<String, String> roProperties = new HashMap<String, String>();
            roProperties.put("ORE Location", oreUrl);

            // extract properties from ORE
            addROProperty("Identifier", describes, roProperties);
            addROProperty("Title", describes, roProperties);
            addROProperty("Creator", describes, roProperties);
            addROProperty("Publication Date", describes, roProperties);
            addROProperty("Creation Date", describes, roProperties);
            addROProperty("Label", describes, roProperties);
            addROProperty("Uploaded By", describes, roProperties);
            addROProperty("Abstract", describes, roProperties);

            // set properties as an attribute
            request.setAttribute("roProperties", roProperties);
            // forward the user to get_id UI
            RequestDispatcher dispatcher = request.getRequestDispatcher("/ro.jsp");
            dispatcher.forward(request, response);

        } else {

            // collection title is the last part of the request URI
            String requestURI = request.getRequestURI();
            String title = requestURI.substring(requestURI.lastIndexOf('/') + 1);

            SFTP sftp = new SFTP();
            String target = "/cos1/hpss/s/e/seadva/" + title + "/" + title + ".tar";
            System.out.println("SDA download path: " + target);
            InputStream inStream = sftp.downloadFile(target);

            String mimeType = "application/octet-stream";
            response.setContentType(mimeType);

            String headerKey = "Content-Disposition";
            String headerValue = String.format("attachment; filename=\"%s\"", target.substring(target.lastIndexOf("/") + 1));
            response.setHeader(headerKey, headerValue);

            OutputStream outStream = response.getOutputStream();

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }

            inStream.close();
            outStream.close();
            sftp.disConnectSessionAndChannel();
        }

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // no posts yet
    }

    private void addROProperty(String propertyName, JSONObject describes,
                               Map<String, String> properties) {
        Object valJSON = describes.get(propertyName);
        if (valJSON != null) {
            // some properties my come as arrays
            if (valJSON instanceof JSONArray) {
                properties.put(propertyName, ((JSONArray) valJSON).get(0).toString());
            } else {
                properties.put(propertyName, valJSON.toString());
            }
        }
    }

}
