/*
#
# Copyright 2015 The Trustees of Indiana University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------
#
# Project: Matchmaker Service
# File:  POJOGenerator.java
# Description:  Generate POJOs based json objects.
#
# -----------------------------------------------------------------
# 
*/
package edu.indiana.d2i.sead.matchmaker.core;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.*;
import java.lang.reflect.InvocationTargetException;


public class POJOGenerator {
	
	private JsonNode jsonTree=null;
	private Class<?> pojo=null;
	
	public POJOGenerator(){
		// empty object
	}
	
	public POJOGenerator(String className) throws ClassNotFoundException{
		ClassLoader cls =ClassLoader.getSystemClassLoader();
		//this.pojo=cls.loadClass(className);
		this.pojo=Class.forName(className);
	}
	
	public void fromPath(String filePath){
		ObjectMapper mapper = new ObjectMapper();
		 
		BufferedReader fileReader;
		try {
            //fileReader = new BufferedReader(
            //new FileReader(filePath));
            fileReader = new BufferedReader(new InputStreamReader(
                    this.getClass().getResourceAsStream(filePath)));
			JsonNode rootNode = mapper.readTree(fileReader);
			this.jsonTree=rootNode;	
	 
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void fromFile(File jsonFile){
		ObjectMapper mapper = new ObjectMapper();
		 
		try {
			JsonNode rootNode = mapper.readTree(jsonFile);
			this.jsonTree=rootNode;
				
	 
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void fromString(String jsonString){
		ObjectMapper mapper = new ObjectMapper();
		 
		try {
			JsonNode rootNode = mapper.readTree(jsonString);
			this.jsonTree=rootNode;
				
	 
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public JsonNode getJsonTree(){
		return this.jsonTree;
	}
	
	public Object generate() throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper mapper = new ObjectMapper();
		
		if(this.jsonTree.isArray()){
			ArrayNode jarray=(ArrayNode)this.jsonTree;
			Object[] objs=new Object[jarray.size()];
			for (int i=0;i<jarray.size();i++){
				objs[i]=mapper.readValue(jarray.get(i).toString(), this.pojo);
			}
			return objs;
		}else {
			Object obj=mapper.readValue(this.jsonTree.toString(), this.pojo);
			return obj;
		}
	}
	
	public static void main(String[] args) throws JsonProcessingException, IOException, ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
/*
		// TODO Auto-generated method stub
		POJOGenerator repositories = new POJOGenerator("edu.indiana.d2i.sead.matchmaker.pojo.Repository");
		repositories.fromPath("C:\\Users\\yuanluo\\WorkZone\\workspace\\MatchMaker\\profile\\repositories.json");
		POJOGenerator person = new POJOGenerator("edu.indiana.d2i.sead.matchmaker.pojo.Person");
		person.fromPath("C:\\Users\\yuanluo\\WorkZone\\workspace\\MatchMaker\\profile\\person.json");
		POJOGenerator researchObject=new POJOGenerator("edu.indiana.d2i.sead.matchmaker.pojo.ResearchObject");
		researchObject.fromPath("C:\\Users\\yuanluo\\WorkZone\\workspace\\MatchMaker\\profile\\research_object.json");
		
		Object[] repos=(Object[]) repositories.generate();
		for(Object repo: repos){
			Class<?> classTypeRepository=Class.forName("edu.indiana.d2i.sead.matchmaker.pojo.Repository");
			Object maxSize=classTypeRepository.cast(repo).getClass().getMethod("getMaxSize", null).invoke(repo);
			Class<?> classTypeMaxSize=Class.forName("edu.indiana.d2i.sead.matchmaker.pojo.MaxSize");
			Object value=classTypeMaxSize.cast(maxSize).getClass().getMethod("getValue", null).invoke(maxSize);
			System.out.println(value.toString());
		}
*/		
	}

}
