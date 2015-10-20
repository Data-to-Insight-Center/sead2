/*
 *
 * Copyright 2015 University of Michigan
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
 *
 * @author myersjd@umich.edu
 */


package org.seadpdt.util;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public class MongoDB {

	static public MongoClient mongoClientInstance = null;
	public static String researchObjects = "ro";
	public static String people="people";
	public static String repositories = "repo";
    public static String oreMap = "oreMaps";
    public static String fgdc = "fgdc";

	public static synchronized MongoClient getMongoClientInstance() {
	    if (mongoClientInstance == null) {
	        try {
	            mongoClientInstance = new MongoClient();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	    return mongoClientInstance;
	}

	static public MongoDatabase getServicesDB() {
		MongoDatabase db = getMongoClientInstance().getDatabase(Constants.pdtDbName);
		return db;
	}

    static public MongoDatabase geMetaGenDB() {
        MongoDatabase db = getMongoClientInstance().getDatabase(Constants.metaDbName);
        return db;
    }
}
