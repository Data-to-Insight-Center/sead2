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

package org.sead.sda;

import java.io.InputStream;
import java.util.Properties;

public class Constants {

    public static String sdaHost;
    public static String sdaUser;
    public static String sdaPassword;
    public static String sdaPath;
    public static String allResearchObjects;
    public static String landingPage;
    public static String pdtURL;

    static {
        try {
            loadConfigurations();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadConfigurations() throws Exception {
        Properties properties = new Properties();
        InputStream inputStream = Constants.class.getResourceAsStream("config.properties");
        if (inputStream != null) {
            properties.load(inputStream);
        } else {
            throw new Exception("Error while reading SDA landing page properties");
        }
        // read properties
        sdaHost = properties.getProperty("sftp.host");
        sdaUser = properties.getProperty("sftp.user");
        sdaPassword = properties.getProperty("sftp.pass");
        sdaPath = properties.getProperty("sda.path");
        allResearchObjects = properties.getProperty("all.research.objects");
        landingPage = properties.getProperty("landing.page");
        pdtURL = properties.getProperty("pdt.url");
    }

}
