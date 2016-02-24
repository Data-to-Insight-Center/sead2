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

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.*;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.BufferedInputStream;

/**
 * SDA LandingPage
 */
public class LandingPage extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static Map<String, String> keyMapList;
    private static final String RESTRICTED_ACCESS = "http://sead-data.net/terms/access/restricted";
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getParameter("tag") != null || request.getRequestURI().contains("/sda/list")) {       	
        	String tag = "";
        	
        	if (request.getParameter("tag") != null){
        		tag = request.getParameter("tag");
        	}else{
        		tag = request.getRequestURI().split("/sda/list=")[1];
        	}

            // here we check whether the BagIt zip file for this RO exists in SDA
            SFTP sftp = new SFTP();
            String bagName = getBagNameFromId(tag);
            if (sftp.doesFileExist(Constants.sdaPath + bagName + "/" + bagName + ".zip")) {
                System.out.println("Bag Exists in SDA...");
                request.setAttribute("bagExists", "true");
            }
            sftp.disConnectSessionAndChannel();

            request.setAttribute("obTag", tag);
        	request.setAttribute("landingPageUrl", Constants.landingPage);
   	
        	String keyList_cp = "@id|status|message|preferences";
        	String keyList_ore = "contact|creator|publication date|title|abstract|license|is version of|similarto|title|describes|@context|aggregates|has part|identifier|label|size";
        	
        	keyMapList = new HashMap<String, String>();
        	
            Shimcalls shim = new Shimcalls();
            // Fix: accessing RO from c3pr here is wrong. we have to access the ore map in the
            // published package and read properties from that.
            JSONObject cp = shim.getResearchObject(tag);
            keyMap(cp, keyList_cp);
            
            
            shim.getObjectID(cp, "@id");
            String oreUrl = shim.getID();
            JSONObject oreFile = shim.getResearchObjectORE(oreUrl);
            keyMap(oreFile, keyList_ore);
            
            JSONObject describes = (JSONObject) oreFile.get(keyMapList.get("describes"));
            Map<String, List<String>> roProperties = new HashMap<String, List<String>>();
            Map<String, String> downloadList = new HashMap<String, String>();
            Map<String, String> linkedHashMap = new LinkedHashMap<String, String>();
            Map<String, String> linkedHashMapTemp = new LinkedHashMap<String, String>();
            Map<String, String> newDownloadList = new LinkedHashMap<String, String>();	

            // extract properties from ORE
            JSONArray status = (JSONArray) cp.get(keyMapList.get("Status".toLowerCase()));
            String doi = "No DOI Found";             // handle this as an exception
            for (Object st : status) {
                JSONObject jsonStatus = (JSONObject) st;
                String stage = (String) jsonStatus.get("stage");
                if ("Success".equals(stage)) {
                    doi = (String) jsonStatus.get("message");
                }
            }
            roProperties.put("DOI", Arrays.asList(doi));
            roProperties.put("Full Metadata", Arrays.asList(Constants.landingPage + "/metadata/" + tag + "/oremap"));
            addROProperty("Creator", describes, roProperties);
            addROProperty("Publication Date", describes, roProperties);
            addROProperty("Title", describes, roProperties);
            addROProperty("Abstract", describes, roProperties);
            addROProperty("Contact", describes, roProperties);
            JSONObject preferences = (JSONObject) cp.get(keyMapList.get("Preferences".toLowerCase()).toLowerCase());
            addROProperty("License", preferences, roProperties);

            // check access rights
            if (isRORestricted(preferences)) {
                request.setAttribute("accessRestricted", "true");
                List<String> rights = new ArrayList<String>();
                rights.add("Restricted");
                roProperties.put("Access Rights", rights);
            }

            //Map<String, String> properties = new HashMap<String, String>();
            //String Label = properties.get("Label");
            
            // extract Live Data Links from ORE
            String liveCopy = null;
            if (describes.get(keyMapList.get("Is Version Of".toLowerCase())) != null) {
                String versionOf = describes.get(keyMapList.get("Is Version Of".toLowerCase())).toString();
                if (versionOf.startsWith("http")) {
                    liveCopy = versionOf;
                } else if (describes.get(keyMapList.get("similarTo".toLowerCase())) != null) {
                    String similar = describes.get(keyMapList.get("similarTo".toLowerCase())).toString();
                    similar = similar.substring(0, similar.indexOf("/resteasy") + 1);
                    liveCopy = similar + "#collection?uri=" + versionOf;
                }
            }
            if (liveCopy != null) {
                List<String> liveCopyList = new ArrayList<String>();
                if (shim.validUrl(liveCopy)){
                    liveCopyList.add(liveCopy);
                }else{
                    liveCopyList.add("Not Available");
                }
                roProperties.put("Live Data Links", liveCopyList);
            }
            
            // set properties as an attribute
            request.setAttribute("roProperties", roProperties);
           
           // String title = describes.get(keyMapList.get("Title".toLowerCase())).toString();
            
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
	            
					//extract RO hierarchy
	            	try{
		            	NewOREmap oreMap = new NewOREmap(oreFile, keyMapList);
		            	downloadList = oreMap.getHierarchy();
		            	
		            	Set<String> nameList = downloadList.keySet();
		            	
		            	for (String name : nameList){
		            		String[] name_split = name.split("/");
		            		String size = null;
		            		if (downloadList.get(name) != null){
			            		int bytes = Integer.parseInt(downloadList.get(name));
			            		
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
		            		}
			                
			                String temp = null;
			            	if (name_split.length <= 2 && size != null){
			            		
			            		temp = "<span style='padding-left:"+30*(name_split.length-2)+"px'>"+name_split[name_split.length-1]+"</span>";
			            		linkedHashMap.put(name,temp);
			            	}else{
			            		
			            		temp = "<span style='padding-left:"+30*(name_split.length-2)+"px'>"+"|__"+name_split[name_split.length-1]+"</span>";
			            		linkedHashMapTemp.put(name,temp);
			            	}
			                
			            	newDownloadList.put(name, size);
			                	                
		            	}
		            	
		            	for (String key : linkedHashMapTemp.keySet()){
		            		linkedHashMap.put(key, linkedHashMapTemp.get(key));
		            	}
	            	}catch(Exception e){
	            		System.err.println("Landing Page OREmap error: inaccurate keys");
	            	}
		            
	            }
	            					            
	         
            // set download list as an attribute
            // set linkedHashMap as an attribute
            }
            request.setAttribute("downloadList", newDownloadList);
            request.setAttribute("linkedHashMap", linkedHashMap);
           
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

            // don't allow downloads for restricted ROs
            // Fix: use ORE from package
            Shimcalls shim = new Shimcalls();
            JSONObject ro = shim.getResearchObject(title);
            
            String keyList_cp = "@id|status|message|preferences";
            keyMapList = new HashMap<String, String>();
            keyMap(ro, keyList_cp);
            
            if (isRORestricted((JSONObject) ro.get(keyMapList.get("Preferences".toLowerCase())))) {
                return;
            }
            
            SFTP sftp = new SFTP();
            String bgName = getBagNameFromId(title);
            String target = Constants.sdaPath + bgName + "/" + bgName + ".zip";
            if (!sftp.doesFileExist(target)) {
                target = Constants.sdaPath + title + "/" + title + ".tar";
            }

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
	            	if (target.contains(".tar")){
		            	System.out.println("SDA download path: " + Constants.sdaPath + newURL);
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
	            	}else{
	            		System.out.println("SDA download path: " + Constants.sdaPath + bgName+"/"+bgName+".zip/"+bgName+"/"+newURL.substring(newURL.indexOf("/")+1));
	            		BufferedInputStream bin = new BufferedInputStream(inStream);
	            		ZipInputStream myZipFile = new ZipInputStream(bin);
	            		
	            		ZipEntry ze = null;
	            		while ((ze = myZipFile.getNextEntry()) != null) {
	            		    if (ze.getName().equals(bgName+"/"+newURL.substring(newURL.indexOf("/")+1))) {
	            		        byte[] buffer = new byte[4096];
	            		        int len;
	            		        while ((len = myZipFile.read(buffer)) != -1) {
	            		            outStream.write(buffer, 0, len);
	            		        }
	            		        break;
	            		    }
	            		}
	            	}
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
                               Map<String, List<String>> properties) {
    	if (keyMapList.containsKey(propertyName.toLowerCase())){
	    	Object valJSON = describes.get(keyMapList.get(propertyName.toLowerCase()));
	        if (valJSON != null) {
	            // some properties my come as arrays
	            List<String> list = new ArrayList<String>();
	            if (valJSON instanceof JSONArray) {
	                for(int i=0; i < ((JSONArray) valJSON).size() ; i++) {
	                    if (((JSONArray) valJSON).get(i) != null) {
	                        list.add(((JSONArray) valJSON).get(i).toString());
	                    }
	                }
	                properties.put(propertyName,list);
	            } else {
	                list.add(valJSON.toString());
	                properties.put(propertyName, list);
	            }
	        } 
    	}else{
    		List<String> temp = new ArrayList<String>();
    		temp.add("No "+propertyName+" Found");
    		properties.put(propertyName, temp);
    	}
    }

    private static String getBagNameFromId(String bagId) {
        // Create known-good filename
        return bagId.replaceAll("\\W+", "_");
    }

    private boolean isRORestricted(JSONObject preferences) {
        if (preferences != null && preferences.get("Access Rights") != null) {
            String accessRights = (String) preferences.get("Access Rights");
            if (RESTRICTED_ACCESS.equals(accessRights.trim())) {
                return true;
            }
        }
        return false;
    }
    
    private static void keyMap(Object obj, String keyList){
    	
    	if (obj instanceof JSONArray){
    		for (Object item : ((JSONArray) obj).toArray()){
    			keyMap(item, keyList);
    		}
    	}else if (obj instanceof JSONObject){
    		for (Object key : ((JSONObject) obj).keySet()){
    			
    			if (key.toString().toLowerCase().matches(keyList)){
    				if (!keyMapList.containsKey(key.toString())){
    					keyMapList.put(key.toString().toLowerCase(), key.toString());
    				}
    				keyMap(((JSONObject) obj).get(key), keyList);
    			}else{
    				keyMap(((JSONObject) obj).get(key), keyList);
    			}
    		}
    	}

    } 

}
