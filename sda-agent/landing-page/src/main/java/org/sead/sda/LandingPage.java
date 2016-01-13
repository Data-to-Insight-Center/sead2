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
        	request.setAttribute("landingPageUrl", Constants.landingPage);

            Shimcalls shim = new Shimcalls();
            JSONObject cp = shim.getResearchObject(tag);
            
            shim.getObjectID(cp, "@id");
            String oreUrl = shim.getID();
            JSONObject oreFile = shim.getResearchObjectORE(oreUrl);
            JSONObject describes = (JSONObject) oreFile.get("describes");
            Map<String, List<String>> roProperties = new HashMap<String, List<String>>();
            Map<String, String> downloadList = new HashMap<String, String>();
            Map<String, String> linkedHashMap = new LinkedHashMap<String, String>();	

            // extract properties from ORE
            JSONArray status = (JSONArray) cp.get("Status");
            String doi = ((JSONObject) status.get(1)).get("message").toString();
            roProperties.put("DOI", Arrays.asList(doi));
            roProperties.put("Full Metadata", Arrays.asList(Constants.landingPage + "/metadata/" + tag + "/oremap"));
            addROProperty("Creator", describes, roProperties);
            addROProperty("Publication Date", describes, roProperties);
            addROProperty("Title", describes, roProperties);
            addROProperty("Abstract", describes, roProperties);

            //Map<String, String> properties = new HashMap<String, String>();
            //String Label = properties.get("Label");
           
            // set properties as an attribute
            request.setAttribute("roProperties", roProperties);
           
            String title = describes.get("Title").toString();
            
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
		            String target = Constants.sdaPath + title + "/" + title + ".tar";
		            
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
		            
		            // display folder hierarchy 
      	      		Set<String> namelist = downloadList.keySet();
	            
	            	int maxCount = 0;
	            	List<String[]> allNames = new ArrayList<String[]>();

	            	for (String name : namelist){
	            		String[] tem = name.split("/");
	            		if (tem.length >= maxCount){
	            			maxCount = tem.length;
	            		}
	            		allNames.add(tem);
	            	
	            	}	            
	           	
	            	List<String> namesAsc = new ArrayList<String>();
	            	for (int i = 2; i <= maxCount; i++){
	            		for (int j = 0; j < allNames.size(); j++){
	            			if (allNames.get(j).length == i){
	            				String tem = "";
	            				for (int k = 0; k < allNames.get(j).length; k++){
	            					tem+=allNames.get(j)[k]+"/";
	            				}
	            				namesAsc.add(tem);	            			
	            			}else if (allNames.get(j).length > i){
	            				String tem = "";
	            				for (int k = 0; k < i; k++){
	            					tem += allNames.get(j)[k]+"/";
	            				}
	            				if (!namesAsc.contains(tem)){
	            					namesAsc.add(tem);
	            				}
	            			}
	            		}
	            	}
	            	List<String[]> allNamesAsc = new ArrayList<String[]>();
	            	for (int i = 0; i< namesAsc.size();i++){
	            		allNamesAsc.add(namesAsc.get(i).split("/"));
	            	}
	            
	            	allNames = new ArrayList<String[]>();
	            	allNames.add(allNamesAsc.remove(0));
	            
	            	while(!allNamesAsc.isEmpty()){	 
	            		for (int loc = 0; loc < allNames.size(); loc++){
		            		for (int i = 0; i < allNamesAsc.size(); i++){
		            			if (allNames.get(loc).length == allNamesAsc.get(i).length && allNames.get(loc)[allNames.get(loc).length-2].equals(allNamesAsc.get(i)[allNamesAsc.get(i).length-2])){
		            				allNames.add(loc+1, allNamesAsc.remove(i));
		            			}
		            			else if (allNames.get(loc).length == allNamesAsc.get(i).length-1 && allNames.get(loc)[allNames.get(loc).length-1].equals(allNamesAsc.get(i)[allNamesAsc.get(i).length-2])){
		            				allNames.add(loc+1, allNamesAsc.remove(i));	
		            			}
		            		}
	            		}     		            	
	            	}
	            	            	            
	           
	            	List<String> newAllNames = new ArrayList<String>();
	            	for (int i = 0; i < allNames.size(); i++){
	            		String tem = "";
	            		for (int j = 0; j < allNames.get(i).length; j++){
	            			tem = tem + allNames.get(i)[j] + "/";
	            		}       
	            		newAllNames.add(tem.substring(0, tem.length()-1));
	            	}
	            
	            	linkedHashMap = new LinkedHashMap<String, String>();
	            	            
	            	for (int i = 0 ; i < newAllNames.size(); i++){
	            		String[] tem = newAllNames.get(i).split("/");
		            	String temp = "";
		            	if (tem.length <= 2){		            		
		            		temp = "<span style='padding-left:"+30*(tem.length-2)+"px'>"+tem[tem.length-1]+"</span>";
		            		linkedHashMap.put(newAllNames.get(i),temp);
		            	}else{		            		
		            		temp = "<span style='padding-left:"+30*(tem.length-2)+"px'>"+"|__"+tem[tem.length-1]+"</span>";
		            		linkedHashMap.put(newAllNames.get(i),temp);
		            	}
	            	}
		            
	            }
	            					            
	         
            // set download list as an attribute
            // set linkedHashMap as an attribute
            }
            request.setAttribute("downloadList", downloadList);
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
            
            SFTP sftp = new SFTP();
            String target = Constants.sdaPath + title + "/" + title + ".tar";
                    
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
    	Object valJSON = describes.get(propertyName);
        if (valJSON != null) {
            // some properties my come as arrays
            List<String> list = new ArrayList<String>();
            if (valJSON instanceof JSONArray) {
                for(int i=0; i < ((JSONArray) valJSON).size() ; i++) {
                    list.add(((JSONArray) valJSON).get(i).toString());
                }
                properties.put(propertyName,list);
            } else {
                list.add(valJSON.toString());
                properties.put(propertyName, list);
            }
        }    
    }

}
