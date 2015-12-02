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
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * SDA LandingPage
 */
public class LandingPage extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getParameter("tag") != null || request.getRequestURI().contains("/sda/list")) {       	
        	String tag = "";
        	
        	if (request.getParameter("tag") != null){
        		tag = request.getParameter("tag");
        	}else{
        		tag = request.getRequestURI().split("/sda/list=")[1];
        	}
        	
        	request.setAttribute("obTag", tag);
        	
            Shimcalls shim = new Shimcalls();
            JSONObject cp = shim.getResearchObject(tag);
            
            shim.getObjectID(cp, "@id");
            String oreUrl = shim.getID();
            JSONObject oreFile = shim.getResearchObjectORE(oreUrl);
            JSONObject describes = (JSONObject) oreFile.get("describes");
            Map<String, String> roProperties = new HashMap<String, String>();
            Map<String, String> downloadList = new HashMap<String, String>();

            // extract properties from ORE
            JSONArray status = (JSONArray) cp.get("Status");
            String doi = ((JSONObject) status.get(1)).get("message").toString();
            roProperties.put("DOI", doi);
            roProperties.put("Full Metadata", oreUrl);
            addROProperty("Creator", describes, roProperties);
            addROProperty("Publication Date", describes, roProperties);
            addROProperty("Label", describes, roProperties);
            addROProperty("Abstract", describes, roProperties);
           
            // set properties as an attribute
            request.setAttribute("roProperties", roProperties);
           
            String title = describes.get("Label").toString();
            
            // extract file names from tar archive in SDA
            String requestURI = request.getRequestURI();
            
            if (requestURI.contains("/sda/list")){
            	int c = 0;
            	String[] requestURIsda = requestURI.split("/");
            	for (String item : requestURIsda){
            		if (item.equals("sda")){
            			c++;
            		}
            	}
	            if (c % 2 != 0){
	            	downloadList = new HashMap<String, String>();
		            SFTP sftp = new SFTP();
		            String target = "/cos1/hpss/s/e/seadva/" + title + "/" + title + ".tar";
		            
		            InputStream inStream = sftp.downloadFile(target);
		            
		            TarArchiveInputStream myTarFile = new TarArchiveInputStream(inStream);
			        TarArchiveEntry entry = null;
		            String individualFiles;
		            
		            while ((entry = myTarFile.getNextTarEntry()) != null) {
		                
		                individualFiles = entry.getName();
		                byte[] content = new byte[(int) entry.getSize()];
		                String size = null;
		                                
		                int bytes = content.length;
		                int kb = bytes / 1024;
		                int mb = kb / 1024;
		                int gb = mb /1024;
		                if (bytes <= 1024){
		                	size = bytes + " Bytes";
		                }else if (kb <= 1024){
		                	size = kb + " KB";
		                }else if (mb <= 1024){
		                	size = mb + " MB";
		                }else{
		                	size = gb + " GB";
		                }
		                
		                downloadList.put(individualFiles, size);
		                
		            }
		            myTarFile.close();
		            sftp.disConnectSessionAndChannel();  
		            
		            
	            }
            // set download list as an attribute
            }
            request.setAttribute("downloadList", downloadList);
            // forward the user to get_id UI
            RequestDispatcher dispatcher = request.getRequestDispatcher("/ro.jsp");
            dispatcher.forward(request, response);

        } else if (!request.getRequestURI().contains("bootstrap")){

            // collection title is the last part of the request URI
            String requestURI = request.getRequestURI();          
            String newURL = requestURI.substring(requestURI.lastIndexOf("sda/")+4);            
            String title = null;
            String filename = null;
            
            if (!newURL.contains("/")){
            	title = newURL;            	
            }else{
            	title = newURL.split("/")[0];
            	filename = newURL.substring(newURL.indexOf("/")+1);
            }         
            title = URLDecoder.decode(title, "UTF-8");
            newURL = URLDecoder.decode(newURL, "UTF-8");            
            
            SFTP sftp = new SFTP();
            String target = "/cos1/hpss/s/e/seadva/" + title + "/" + title + ".tar";
                    
            System.out.println("title "+title);
            System.out.println("filename "+filename);
            
            if (!title.equals("*")){
            	InputStream inStream = sftp.downloadFile(target);
            	
            	String mimeType = "application/octet-stream";
                response.setContentType(mimeType);

                String headerKey = "Content-Disposition";
                
                String headerValue = null;
                if (filename != null){
                	if (filename.contains("/")){
                		filename = filename.substring(filename.lastIndexOf("/")+1);
                	}
                	headerValue = String.format("attachment; filename=\"%s\"", filename);}
                else{
                	headerValue = String.format("attachment; filename=\"%s\"", target.substring(target.lastIndexOf("/") + 1));
                }
                response.setHeader(headerKey, headerValue);
                
                
            	OutputStream outStream = response.getOutputStream();
	            if (newURL.equals(title)){
	            	//download tar file
	            	System.out.println("SDA download path: " + target);
		            byte[] buffer = new byte[4096];
		            int bytesRead;
		
		            while ((bytesRead = inStream.read(buffer)) != -1) {
		                outStream.write(buffer, 0, bytesRead);
		            }
	            }else{
	            	//download individual files
	            	System.out.println("SDA download path: " + "/cos1/hpss/s/e/seadva/" + newURL);
	            	TarArchiveInputStream myTarFile = new TarArchiveInputStream(inStream);
	    	        
	    	        TarArchiveEntry entry = null;
	                String individualFiles;
	                int offset;
	
		            while ((entry = myTarFile.getNextTarEntry()) != null) {		                
		                individualFiles = entry.getName();
		              
		                if (individualFiles.equals(newURL)){
		                    byte[] content = new byte[(int) entry.getSize()];
		                    offset=0;		
		                    myTarFile.read(content, offset, content.length - offset);		                    
		                    outStream.write(content);
		                }
			        }
		            myTarFile.close();
	            }
	            inStream.close();
	            outStream.close(); 
            }
            
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
