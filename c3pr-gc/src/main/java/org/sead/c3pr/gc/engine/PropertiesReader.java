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
 * @author isuriara@indiana.edu
 */

package org.sead.c3pr.gc.engine;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReader {

    public static String allResearchObjects;
    public static String roDeletePath;
    public static String callDaemons;
    public static long gcIntervalHours;
    public static int gcBeforeDays;

    public static void init(String configPath) {
        try {
            loadConfigurations(configPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadConfigurations(String configPath) throws Exception {
        Properties properties = new Properties();
        InputStream inputStream = new FileInputStream(configPath);
        properties.load(inputStream);
        // read properties
        allResearchObjects = properties.getProperty("all.research.objects");
        roDeletePath = properties.getProperty("ro.delete.url");
        callDaemons = properties.getProperty("call.daemons");
        gcIntervalHours = Long.parseLong(properties.getProperty("gc.check.interval.hours"));
        gcBeforeDays = Integer.parseInt(properties.getProperty("garbage.collect.before.days"));
    }

}
