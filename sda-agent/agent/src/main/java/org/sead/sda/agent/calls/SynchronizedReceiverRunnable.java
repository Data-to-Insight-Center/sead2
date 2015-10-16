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
 * @author luoyu@indiana.edu
 * @author isuriara@indiana.edu
 */

package org.sead.sda.agent.calls;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.sead.sda.agent.apicalls.NewOREmap;
import org.sead.sda.agent.apicalls.Shimcalls;
import org.sead.sda.agent.driver.DummySDA;
import org.sead.sda.agent.driver.FileManager;
import org.sead.sda.agent.driver.ZipDirectory;
import org.sead.sda.agent.engine.DOI;
import org.sead.sda.agent.engine.PropertiesReader;
import org.sead.sda.agent.engine.SFTP;

public class SynchronizedReceiverRunnable implements Runnable {

    public void run() {

        while (true) {
            Shimcalls call = new Shimcalls();
            JSONArray allResearchObjects = call.getResearchObjectsList();

            for (Object item : allResearchObjects.toArray()) {
                try {
                    JSONObject researchObject = (JSONObject) item;
                    call.getObjectID(researchObject, "Identifier");
                    String identifier = call.getID();

                    if (identifier == null) {
                        throw new Exception("SDA Agent : Cannot get Identifier of RO");
                    }
                    System.out.println("\nResearch Object found, ID: " + identifier);

                    if (isAlreadyPublished(researchObject)) {
                        System.out.println("Research Object has already been published, skipping...\n");
                    } else {
                        System.out.println("Starting to publish Research Object...");

                        JSONObject pulishObject = call.getResearchObject(identifier);
                        call.getObjectID(pulishObject, "@id");
                        String oreUrl = call.getID();

                        if (oreUrl == null) {
                            throw new Exception("SDA Agent : Cannot get ORE map file of RO");
                        }
                        System.out.println("Fetching ORE from: " + oreUrl);
                        JSONObject ore = call.getResearchObjectORE(oreUrl);
                        NewOREmap oreMap = new NewOREmap(ore);
                        JSONObject newOREmap = oreMap.getNewOREmap();

                        System.out.println("Downloading data files...");
                        DummySDA dummySDA = new DummySDA(newOREmap, call.getJsonORE(oreUrl));
                        if (dummySDA.getErrorLinks().size() > 0) {
                            throw new Exception("Error while downloading some/all files");
                        }

                        String rootPath = dummySDA.getRootPath();
                        new ZipDirectory(rootPath);

                        System.out.println("Depositing RO into SDA as a tar archive...");
                        new SFTP(rootPath + ".tar");

                        System.out.println("Generating DOI...");
                        String target = PropertiesReader.landingPage + "?tag=" + identifier;
                        DOI doi = new DOI(target, ore);
                        String doiUrl = doi.getDoi();
                        System.out.println("DOI: " + doiUrl);

                        System.out.println("Updating status in C3P-R with the DOI...");
                        call.updateStatus(doiUrl, identifier);

                        FileManager manager = new FileManager();
                        manager.removeTempFile(rootPath + ".tar");
                        manager.removeTempFolder(rootPath);
                        System.out.println("Successfully published Research Object: " + identifier + "\n");
                    }
                } catch (Exception e) {
                    System.out.println("ERROR: Error while publishing Research Object...");
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
    }

    private boolean isAlreadyPublished(JSONObject researchObject) {
        Object statusObj = researchObject.get("Status");
        if (statusObj != null) {
            JSONArray statusArray = (JSONArray) statusObj;
            for (Object status : statusArray) {
                if (status instanceof JSONObject) {
                    JSONObject statusJson = (JSONObject) status;
                    String stage = statusJson.get("stage").toString();
                    if ("Success".equals(stage)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
