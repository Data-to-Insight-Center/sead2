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

package org.seadpdt.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Constants {

	public static String mongoHost;
	public static int mongoPort;
	public static String mongoOreHost;
	public static int mongoOrePort;

	public static String pdtDbName;
	public static String metaDbName;
	public static String oreDbName;
	
	public static String serviceName = "SEAD-C3PR";

	static {
		try {
			loadConfigurations();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void loadConfigurations() throws IOException {
		InputStream inputStream = Constants.class
				.getResourceAsStream("./default.properties");
		Properties props = new Properties();
		props.load(inputStream);
		mongoHost = props.getProperty("mongo.host", "localhost");
		mongoPort = Integer.parseInt(props.getProperty("mongo.port", "27017"));
		pdtDbName = props.getProperty("pdt.db.name", "sead-pdt");
		metaDbName = props.getProperty("metadata.db.name", "sead-metadata");
		mongoOreHost = props.getProperty("mongo.ore.host", "localhost");
		mongoOrePort = Integer.parseInt(props.getProperty("mongo.ore.port",
				"27018"));
		oreDbName = props.getProperty("ore.db.name", "sead-ore");
	}
}
