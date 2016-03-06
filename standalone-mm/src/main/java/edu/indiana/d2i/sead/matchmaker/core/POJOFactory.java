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
# File:  POJOFactory.java
# Description:  Create POJO source files based json objects/schemas.
#
# -----------------------------------------------------------------
# 
*/

package edu.indiana.d2i.sead.matchmaker.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.codemodel.JCodeModel;
import org.jsonschema2pojo.SchemaGenerator;
import org.jsonschema2pojo.SchemaMapper;

import java.io.File;
import java.io.IOException;

public class POJOFactory {

	public static void createClass(String className, String packageName, JsonNode json, File output) throws IOException{
		JCodeModel codeModel = new JCodeModel();
		JsonNode jsonSchema=new SchemaGenerator().schemaFromExample(json);
		String jsonString =jsonSchema.toString();
		new SchemaMapper().generate(codeModel, className, packageName,jsonString);
		codeModel.build(output);
		//Jsonschema2Pojo.generate(new DefaultGenerationConfig());;
	}

	public static void main(String[] args) throws JsonProcessingException, IOException, ClassNotFoundException {
		/*// TODO Auto-generated method stub
		
		ObjectMapper mapper = new ObjectMapper();

		POJOGenerator pojogen = new POJOGenerator();
		pojogen.fromPath(args[0]);
		if(pojogen.getJsonTree().isArray()){
			ArrayNode jarray=(ArrayNode)pojogen.getJsonTree();
			for (int i=0;i<jarray.size();i++){
				JsonNode conf = jarray.get(i);
				//System.out.println(conf.get("format").textValue());
				//System.out.println(conf.get("format").toString());
				//System.out.println(conf.get("format").asText());
				JsonNode rootNode = mapper.readTree(new File(conf.get("format").asText()));
				POJOFactory.createClass(conf.get("className").asText(),conf.get("packageName").asText(), rootNode, new File(conf.get("codeLocation").asText()));
			}

		}else {
			JsonNode conf = pojogen.getJsonTree();
			JsonNode rootNode = mapper.readTree(new File(conf.get("format").asText()));
			POJOFactory.createClass(conf.get("className").asText(),conf.get("packageName").asText(), rootNode, new File(conf.get("codeLocation").asText()));
		}
		*/

        ObjectMapper mapper = new ObjectMapper();

        if(args.length < 2) {
            System.out.println("Couldn't generate POJOs : Missing parameters");
            return;
        }
        String src = args[0];
        String resources = args[1];

		JsonNode rootNode = mapper.readTree(new File(resources + "profile/person.json"));
		POJOFactory.createClass("Person","edu.indiana.d2i.sead.matchmaker.pojo", rootNode, new File(src));

        rootNode = mapper.readTree(new File(resources + "profile/research_object.json"));
        POJOFactory.createClass("ResearchObject","edu.indiana.d2i.sead.matchmaker.pojo", rootNode, new File(src));

        rootNode = mapper.readTree(new File(resources + "profile/repositories.json"));
        POJOFactory.createClass("Repositories","edu.indiana.d2i.sead.matchmaker.pojo", rootNode, new File(src));

        rootNode = mapper.readTree(new File(resources + "profile/MatchmakerInputSchema.json"));
        POJOFactory.createClass("MatchmakerInputSchema","edu.indiana.d2i.sead.matchmaker.service", rootNode, new File(src));

    }
}
