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

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.sead.nds.repository.BagGenerator;
import org.sead.nds.repository.C3PRPubRequestFacade;
import org.sead.sda.agent.apicalls.NewOREmap;
import org.sead.sda.agent.apicalls.Shimcalls;
import org.sead.sda.agent.driver.DummySDA;
import org.sead.sda.agent.driver.FileManager;
import org.sead.sda.agent.driver.ZipDirectory;
import org.sead.sda.agent.engine.DOI;
import org.sead.sda.agent.engine.PropertiesReader;
import org.sead.sda.agent.engine.SFTP;
import org.sead.sda.agent.policy.EnforcementResult;
import org.sead.sda.agent.policy.PolicyEnforcer;

import java.io.BufferedOutputStream;
import java.io.OutputStream;

public class SynchronizedReceiverRunnable implements Runnable {

    private static final Logger log = Logger.getLogger(SynchronizedReceiverRunnable.class);

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
                    // log.info("\nResearch Object found, ID: " + identifier);

                    if (!isAlreadyPublished(researchObject)) {
                        log.info("New Research Object found, ID: " + identifier);
                        log.info("Starting to publish Research Object...");

                        JSONObject publicationRequest = call.getResearchObject(identifier);
                        // check whether RO adheres to the SDA policy, if not reject
                        EnforcementResult enforcementResult = PolicyEnforcer.getInstance().isROAllowed(publicationRequest);
                        if (!enforcementResult.isROAllowed()) {
                            call.updateStatus(C3PRPubRequestFacade.FAILURE_STAGE, enforcementResult.getC3prUpdateMessage(), identifier);
                            log.info("Rejected RO: " + identifier + ", message: " + enforcementResult.getC3prUpdateMessage());
                            continue;
                        }
                        log.info("Policy validation passed, id: " + identifier);

                        if ("tar".equals(PropertiesReader.packageFormat.trim())) {
                            log.error("Tar format is not supported with the latest version of IU SEAD Cloud Agent");
                            //depositTar(publicationRequest, call, identifier);
                        } else {
                            log.info("Generating BagIt..");
                            depositBag(identifier);
                        }
                        log.info("Successfully published Research Object: " + identifier + "\n");
                    }
                } catch (Exception e) {
                    log.info("ERROR: Error while publishing Research Object...");
                    e.printStackTrace();
                }
                try {
                    // wait between 2 RO publishes
                    Thread.sleep(PropertiesReader.roPublishInterval * 1000);
                } catch (InterruptedException e) {
                    // ignore
                }
            }

            try {
                // wait between 2 fetches from API
                Thread.sleep(PropertiesReader.roFetchInterval * 1000);
            } catch (InterruptedException e) {
                // ignore
            }

        }
    }

    private void depositBag(String roId) throws Exception {
        // use the BagGenerator from reference repository code
        BagGenerator bg;
        C3PRPubRequestFacade ro = new C3PRPubRequestFacade(roId, PropertiesReader.properties);
        bg = new BagGenerator(ro);

        SFTP sftp = new SFTP();
        OutputStream ftpOS = sftp.openOutputStream(BagGenerator.getValidName(roId), ".zip");
        BufferedOutputStream bufferedOS = new BufferedOutputStream(ftpOS);

        log.info("Depositing RO into SDA as a BagIt Zip archive...");
        if (bg.generateBag(bufferedOS)) {
            String doi = ro.getOREMap().getJSONObject("describes").getString("External Identifier");
            log.info("Updating status in C3P-R with the DOI: " + doi);
            ro.sendStatus(C3PRPubRequestFacade.SUCCESS_STAGE, doi);
        } else {
            log.debug("RO deposit failed, roId: " + roId);
            ro.sendStatus(C3PRPubRequestFacade.FAILURE_STAGE, "Processing of this request has failed and no further " +
                    "attempts to process this request will be made. Please contact the repository for further information.");
        }

        // close output streams and disconnect sftp channel
        bufferedOS.close();
        ftpOS.close();
        sftp.disconnect();
    }

    private void depositTar(JSONObject pulishObject, Shimcalls call, String identifier) throws Exception {
        call.getObjectID(pulishObject, "@id");
        String oreUrl = call.getID();

        if (oreUrl == null) {
            throw new Exception("SDA Agent : Cannot get ORE map file of RO");
        }
        log.info("Fetching ORE from: " + oreUrl);
        JSONObject ore = call.getResearchObjectORE(oreUrl);
        NewOREmap oreMap = new NewOREmap(ore);
        JSONObject newOREmap = oreMap.getNewOREmap();

        log.info("Generating DOI...");
        String target = PropertiesReader.landingPage + "?tag=" + identifier;
        DOI doi = new DOI(target, ore);
        String doiUrl = doi.getDoi();
        if(doiUrl.startsWith("doi:")){
            doiUrl = doiUrl.replace("doi:", "http://dx.doi.org/");
        }
        log.info("DOI: " + doiUrl);


        log.info("Downloading data files...");
        JSONObject preferences = (JSONObject) pulishObject.get("Preferences");
        String license = null;
        if (preferences.containsKey("License")){
            license = preferences.get("License").toString();
        }
        DummySDA dummySDA = new DummySDA(newOREmap, call.getJsonORE(oreUrl), doiUrl, license);
        if (dummySDA.getErrorLinks().size() > 0) {
            throw new Exception("Error while downloading some/all files");
        }

        String rootPath = dummySDA.getRootPath();
        new ZipDirectory(rootPath);

        log.info("Depositing RO into SDA as a tar archive...");
        SFTP sftp = new SFTP();
        sftp.depositFile(rootPath + ".tar");
        sftp.disconnect();

        log.info("Updating status in C3P-R with the DOI...");
        call.updateStatus(C3PRPubRequestFacade.SUCCESS_STAGE, doiUrl, identifier);

        FileManager manager = new FileManager();
        manager.removeTempFile(rootPath + ".tar");
        manager.removeTempFolder(rootPath);
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
