/*
 *
 * Copyright 2015 The Trustees of Indiana University,
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
 * @author charmadu@umail.iu.edu
 */

package org.sead.api.util;

import java.io.InputStream;
import java.util.Properties;

public class Constants {

    public static String pdtUrl;
    public static String curBeeUrl;
    public static String matchmakerUrl;
    public static String metadataGenUrl;
    public static String doiServiceUrl;
    public static String clowderUser;
    public static String clowderPassword;

    static {
        try {
            loadConfigurations();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadConfigurations() throws Exception {
        Properties properties = new Properties();
        InputStream inputStream = Constants.class.getResourceAsStream("default.properties");
        if (inputStream != null) {
            properties.load(inputStream);
        } else {
            throw new Exception("Error while reading Matchmaker properties");
        }
        pdtUrl = properties.getProperty("pdt.url");
        curBeeUrl = properties.getProperty("curBee.url");
        matchmakerUrl = properties.getProperty("matchmaker.url");
        metadataGenUrl = properties.getProperty("metadatagen.url");
        doiServiceUrl = properties.getProperty("doi.service.url");
        clowderUser = properties.getProperty("clowder.user");
        clowderPassword = properties.getProperty("clowder.pw");
    }
}
