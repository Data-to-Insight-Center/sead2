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
# Project: Matchmaker
# File:  MatchmakerOperations.java
# Description:  Definition of service operations
#
# -----------------------------------------------------------------
# 
*/

package edu.indiana.d2i.sead.matchmaker.service;

import com.fasterxml.jackson.databind.JsonNode;
import edu.indiana.d2i.sead.matchmaker.drivers.MetaDriver;
import edu.indiana.d2i.sead.matchmaker.drivers.Query;
import edu.indiana.d2i.sead.matchmaker.util.MatchmakerENV;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Yuan Luo
 */

public class MatchmakerOperations {
	public static final Log l = LogFactory.getLog(MatchmakerOperations.class);
	public static final String  ERROR_STRING = "SERVER ERROR";


	
	public enum OperationType {
		QUERY,
		BROKER
	}
	
	
	public String exec(MatchmakerENV env, JsonNode request, String responseID){
		//return "{success:true,response:\"Sample Response Message\"}";
		MetaDriver md = null;
		l.info(request.get("operation").asText());
		if(request.get("operation").asText().equals("query")){
			md =  new Query(env, request.get("message").toString(),responseID);
			return md.getResults();
		}/*else if(request.get("operation").asText().equals("deposit")){
			md =  new Deposit(env, request, responseID);
			return md.exec();
		}*/
		return "{success:false,response:\"Invalid Operation\"}";

	}

}
