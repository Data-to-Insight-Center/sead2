package org.seadva.services;

import org.seadva.services.util.Constants;
import org.seadva.services.util.IdMetadata;

import org.json.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * EzidService is a helper class that enables to create and update DOIs using the
 * EZID service.
 *
 */
public class EzidService {

    private DataciteIdService dataciteIdService;
    private Map<IdMetadata.Metadata, String> metadata;
    private boolean permanentDOI;

    public EzidService() {
        dataciteIdService = new DataciteIdService();
        metadata = new HashMap<IdMetadata.Metadata, String>();
        dataciteIdService.setCredentials(Constants.doi_username, Constants.doi_password);
        permanentDOI = false;
    }

    public static void main(String[] args){

        EzidService ezidService = new EzidService();

        Console c = System.console();
        if (c == null) {
            System.err.println("No console.");
            System.exit(1);
        }

        String login = c.readLine("EZID DOI Mode - Create(C) or Update(U) : ");

        if (login.equalsIgnoreCase("U") || login.equalsIgnoreCase("update")) {

            String target = c.readLine("New Target : ");
            if (target == null || target.equals("")) {
                System.out.println("Error : Input target should not be empty");
                return;
            }
            String doi = c.readLine("DOI(ex: 10.5072/FK2S46KQ67) : ");
            if (doi == null || doi.equals("")) {
                System.out.println("Error : Input DOI should not be empty");
                return;
            }

            String doi_url = ezidService.updateDOI(doi, target);
            if (doi_url == null) {
                System.out.println("Error Updating DOI");
                return;
            } else {
                System.out.println("DOI Updated Successfully !");
                System.out.println("DOI : " + doi_url);
            }
        } else if (login.equalsIgnoreCase("C") || login.equalsIgnoreCase("create")) {

            String target = c.readLine("Target : ");
            if (target == null || target.equals("")) {
                System.out.println("Error : Input target should not be empty");
                return;
            }
            String metadata = c.readLine("Metadata(ex: {title : test_title, creator : test_creator, pubDate : test_pubDate}) : ");
            if (metadata == null || metadata.equals("")) {
                metadata = "{}";
            }

            String doi = ezidService.createDOI(metadata, target);
            if (doi == null) {
                System.out.println("Error Creating DOI");
                return;
            } else {
                System.out.println("DOI Created Successfully !");
                System.out.println("DOI : " + doi);
            }
        } else {
            System.out.println("Invalid Mode");
        }

        //System.out.println(System.getProperty("java.io.tmpdir"));
    }

    /**
     * Create a new DOI using EZID service
     *
     * @param metadata_json Metadata in JSON format, ex : {title : test_title, creator : test_creator, pubDate : test_pubDate}
     * @param target        Target URL, ex: http://dummyUrl
     * @return              DOI ULR, ex: http://dx.doi.org/10.5072/FK2S46KQ67
     */
    public String createDOI(String metadata_json, String target){
        if(!permanentDOI) {
            dataciteIdService.setService(Constants.ezid_url + "shoulder/" + Constants.doi_shoulder_test);
        } else {
            dataciteIdService.setService(Constants.ezid_url + "shoulder/" + Constants.doi_shoulder_prod);
        }
        metadata.put(IdMetadata.Metadata.TARGET, target);

        JSONObject metadata_object = null;
        try {
            metadata_object = new JSONObject(metadata_json);
            if (metadata_object.has("title") && metadata_object.getString("title") != null) {
                metadata.put(IdMetadata.Metadata.TITLE, metadata_object.getString("title"));
            }
            if (metadata_object.has("creator") && metadata_object.getString("creator") != null) {
                metadata.put(IdMetadata.Metadata.CREATOR, metadata_object.getString("creator"));
            }
            if (metadata_object.has("pubDate") && metadata_object.getString("pubDate") != null) {
                metadata.put(IdMetadata.Metadata.PUBDATE, metadata_object.getString("pubDate"));
            }
        } catch (JSONException e) {
            System.out.println("Error extracting metadata from input. Invalid JSON string.");
        }
        String result = null;
        try {
            result = dataciteIdService.createwithMd(metadata, false);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        String doi_url = "http://dx.doi.org/" + result.substring(result.indexOf("doi:") + 4, result.indexOf("|"));
        return doi_url;
    }

    /**
     * Update a new target to existing DOI
     *
     * @param doi       DOI to be updated, ex: 10.5072/FK2S46KQ67)
     * @param target    New target URL, ex: http://dummyUrl
     * @return          Updated URL of DOI, ex: http://dx.doi.org/10.5072/FK2S46KQ67
     */
    public String updateDOI(String doi, String target){

        dataciteIdService.setService(Constants.ezid_url + "id/doi:" + doi);
        metadata.put(IdMetadata.Metadata.TARGET, target);
        String result = null;
        try {
            result = dataciteIdService.createwithMd(metadata, true);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        if (result == null) {
            return result;
        }
        String doi_url = "http://dx.doi.org/" + result.split(":")[result.split(":").length - 1];
        return doi_url;
    }

    public boolean setDOIUnavailable(String doi) throws IOException {

        String serviceURL = Constants.ezid_url + "id/" + doi;
        String command = "curl -u "+ Constants.doi_username +":"+ Constants.doi_password +" -X POST -H \"Content-Type:text/plain\" " +
                "--data-binary " + "'_status:unavailable'" + " "+ serviceURL;

        ByteArrayOutputStream stdout = dataciteIdService.executeCommand(command);
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(stdout.toByteArray())));

        String output_line = null;
        String doiUrl = null;
        while((output_line = br.readLine()) != null)
        {
            if(output_line.contains("doi"))
                doiUrl = output_line;
        }

        System.out.println(doiUrl);
        if(doiUrl.split(":")[0].equals("success")){
            return true;
        } else {
            return false;
        }

    }


    public boolean isPermanentDOI() {
        return permanentDOI;
    }

    /**
     * Set the permanentDOI check in EzidService
     *
     * @param permanentDOI  If this is set to true, EzidService create permanent DOIs
     *                      If this is set to false, EzidService create temporary DOIs
     */
    public void setPermanentDOI(boolean permanentDOI) {
        this.permanentDOI = permanentDOI;
    }
}
