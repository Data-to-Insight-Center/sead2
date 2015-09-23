package org.seadva.metadatagen.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Constants {

    public static String rosystemURL;
    public static String dcsBaseURL;
    public static String bagPath;
    public static String metagenDbName;
    public static String dbOreCollection = "oreMaps";

    public static String FORMAT_IANA_SCHEME = "http://www.iana.org/assignments/media-types/";
    public static String titleTerm = "http://purl.org/dc/terms/title";
    public static String typeTerm = "http://purl.org/dc/terms/type";
    public static String identifierTerm = "http://purl.org/dc/terms/identifier";
    public static String sizeTerm = "http://purl.org/dc/terms/SizeOrDuration";
    public static String rightsTerm = "http://purl.org/dc/terms/rights";
    public static String sourceTerm = "http://www.loc.gov/METS/FLocat";
    public static String formatTerm = "http://purl.org/dc/elements/1.1/format";
    public static String creatorTerm = "http://purl.org/dc/terms/creator";
    public static String issuedTerm = "http://purl.org/dc/terms/issued";
    public static String contactTerm = "http://purl.org/dc/terms/mediator";
    public static String locationTerm = "http://purl.org/dc/terms/Location";
    public static String abstractTerm = "http://purl.org/dc/terms/abstract";
    public static String contentSourceTerm = "http://purl.org/dc/terms/source";
    public static String secondaryLocation = "http://seadva.org/terms/replica";
    public static String contributor = "http://purl.org/dc/terms/contributor";
    public static String documentedBy  = "http://purl.org/spar/cito/isDocumentedBy";

    public static Map<String, String> json_map = new HashMap<String, String>();

    static {

        try {
            StringWriter writer = new StringWriter();
            IOUtils.copy(Constants.class.getResourceAsStream("./Config.properties")
                    , writer);

            String content = writer.toString();
            String[] pairs = content.trim().split(
                    "\n|\\=");

            for (int i = 0; i + 1 < pairs.length;) {
                String name = pairs[i++].trim();
                String value = pairs[i++].trim();
                if (name.equals("rosystem.url")) {
                    rosystemURL = value;
                }
                if (name.equals("dcs.baseurl")) {
                    dcsBaseURL = value;
                }
                if (name.equals("bag.path")) {
                    bagPath = value;
                }
                if (name.equals("metagen.db.name")) {
                    metagenDbName = value;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
