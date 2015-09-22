/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sead.workflow.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Constants
 * */
public class Constants {

    public static Map<String, String> metadataPredicateMap;

    public static final String TRUE = "true";
    public static final String FALSE = "false";

    // constants in context
    public static final String JSON_RO = "jsonRO"; // JSONLD format of RO metadata
    public static final String REQUEST_URL = "requestUrl"; // JSONLD format of RO metadata
    public static final String VALIDATED = "validated";

    // constants in JSONLD of RO


    static{
        try {
            metadataPredicateMap = new Constants().loadAcrMetadataMapping();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private  Map<String, String> loadAcrMetadataMapping() throws IOException{
        Map<String, String> metadataPredicateMap = new HashMap<String, String>();

        InputStream inputStream =
                Constants.class.getResourceAsStream(
                        "./" +
                                "ACR_to_ORE_MappingConfig.properties");

        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer);

        String result = writer.toString();
        String[] pairs = result.trim().split(
                "\n|\\=");


        for (int i = 0; i + 1 < pairs.length;) {
            String name = pairs[i++].trim();
            String value = pairs[i++].trim();
            metadataPredicateMap.put(name,value);
        }
        return metadataPredicateMap;
    }
}
