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
import org.sead.workflow.model.PSInstance;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Constants
 * */
public class Constants {

    public static Map<String, String> metadataPredicateMap;
    public static List<PSInstance> psInstances;

    public static final String TRUE = "true";
    public static final String FALSE = "false";

    // constants in context
    public static final String JSON_RO = "jsonRO";
    public static final String PS_ID = "psID";
    public static final String SIGNAL_PS = "signalPs";
    public static final String VALIDATED = "validated";
    public static final String RESPONSE = "response";
    public static final String RO_ID = "roId";

    // constants in JSONLD of RO
    public static final String HAS_FILES = "Has Files";
    public static final String HAS_SUBCOLLECTIONS = "Has Subcollection";
    public static final String REST_CONTEXT = "@context";
    public static final String REST_ID = "@id";
    public static final String IDENTIFIER = "Identifier";
    public static final String GEN_AT = "Source";
    public static final String GEN_AT_URL = "http://sead-data.net/terms/generatedAt";
    public static final String FLOCAT = "FLocat";
    public static final String FLOCAT_URL = "http://www.loc.gov/METS/FLocat";
    public static final String SIZE = "Size";

    static{
        try {
            metadataPredicateMap = new Constants().loadAcrMetadataMapping();
            psInstances = new Constants().loadPsInstances();
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

    private List<PSInstance> loadPsInstances() throws IOException {
        List<PSInstance> instances = new ArrayList<PSInstance>();

        InputStream inputStream =
                Constants.class.getResourceAsStream(
                        "./" +
                                "psInstances.xml");
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer);


        XmlPullParserFactory factory = null;
        try {
            factory = XmlPullParserFactory.newInstance();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        factory.setNamespaceAware(true);
        XmlPullParser xpp = null;
        try {
            xpp = factory.newPullParser();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        try {

            xpp.setInput(new StringReader(writer.toString()));
            int eventType = xpp.getEventType();
            int id = 0;
            int url = 0;
            int remoteAPI = 0;
            int title = 0;
            int type = 0;
            int user = 0;
            int pwd = 0;

            PSInstance instance = null;

            while (eventType != xpp.END_DOCUMENT) {
                if (eventType == xpp.START_TAG) {
                    if (xpp.getName().equals("instance"))
                        instance = new PSInstance();
                    if (xpp.getName().equals("id"))
                        id = 1;
                    if (xpp.getName().equals("url"))
                        url = 1;
                    if (xpp.getName().equals("remoteAPI"))
                        remoteAPI = 1;
                    if (xpp.getName().equals("title"))
                        title = 1;
                    if (xpp.getName().equals("type"))
                        type = 1;
                    if (xpp.getName().equals("user"))
                        user = 1;
                    if (xpp.getName().equals("password"))
                        pwd = 1;
                } else if (eventType == xpp.TEXT) {
                    if (id == 1) {
                        instance.setId(Integer.parseInt(xpp.getText()));
                        id = 0;
                    } else if (url == 1) {
                        instance.setUrl(xpp.getText());
                        url = 0;
                    } else if (remoteAPI == 1) {
                        instance.setRemoteAPI(xpp.getText());
                        remoteAPI = 0;
                    } else if (title == 1) {
                        instance.setTitle(xpp.getText());
                        title = 0;
                    } else if (type == 1) {
                        instance.setType(xpp.getText());
                        type = 0;
                    } else if (user == 1) {
                        instance.setUser(xpp.getText());
                        user = 0;
                    } else if (pwd == 1) {
                        instance.setPassword(xpp.getText());
                        pwd = 0;
                    }
                } else if (eventType == xpp.END_TAG) {
                    if (xpp.getName().equals("instance"))
                        instances.add(instance);
                }
                eventType = xpp.next();
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return instances;
    }
}
