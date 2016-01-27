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
 * @author charmadu@umail.iu.edu
 */

package org.sead.c3pr.gc.calls;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.sead.c3pr.gc.apicalls.Shimcalls;
import org.sead.c3pr.gc.engine.PropertiesReader;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SynchronizedReceiverRunnable implements Runnable {

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss a zzz");
    Date now = null;
    Date before = null;

    public void run() {

        while (true) {
            Shimcalls call = new Shimcalls();
            JSONArray allResearchObjects = call.getAllResearchObjects();
            now = new Date();
            before = new Date(now.getTime() - PropertiesReader.gcBeforeDays*24*60*60*1000);
            System.out.println("\nScanning Research Objects : " + dateFormat.format(now) + "\n");

            for (Object item : allResearchObjects.toArray()) {
                try {
                    JSONObject researchObject = (JSONObject) item;
                    call.getObjectID(researchObject, "Identifier");
                    String identifier = call.getID();

                    if (identifier == null) {
                        throw new Exception("C3PR GC : Cannot get Identifier of RO :" + identifier);
                    }

                    if (isExpired(researchObject)) {
                        System.out.println("Expired Research Object found, ID: " + identifier);
                        System.out.println("Starting to Delete Research Object...");
                        call.deleteCompleteRO(identifier);
                    }
                } catch (Exception e) {
                    System.out.println("ERROR: Error while publishing Research Object...");
                    e.printStackTrace();
                }
            }

            try {
                // wait between 2 garbage collecting jobs
                Thread.sleep(PropertiesReader.gcIntervalHours * 60*60*1000);
            } catch (InterruptedException e) {
                // ignore
            }

        }
    }

    private boolean isExpired(JSONObject researchObject) {
        Object statusObj = researchObject.get("Status");
        Date latest = null;
        if (statusObj != null) {
            JSONArray statusArray = (JSONArray) statusObj;
            for (Object status : statusArray) {
                if (status instanceof JSONObject) {
                    JSONObject statusJson = (JSONObject) status;
                    //String stage = statusJson.get("stage").toString();
                    //if ("Success".equals(stage)) {

                    String dateStr = statusJson.get("date").toString();
                    try {
                        Date date = DateFormat.getDateTimeInstance().parse(dateStr);
                        if(latest == null){
                            latest = date;
                        }
                        else if (date.after(latest)) {
                            latest = date;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    //}
                }
            }
        }

        if(latest.before(before)){
            return true;
        } else {
            return false;
        }
    }
}
