package org.sead.matchmaker;

import java.io.InputStream;
import java.util.Properties;

public class MatchmakerConstants {

    public static final String JSON_CONTENT_TYPE = "application/json";
    public static final String PDT_REPOSITORIES = "repositories";
    public static final String PDT_PEOPLE = "people";

    public static String pdtUrl;

    static {
        try {
            loadConfigurations();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadConfigurations() throws Exception {
        Properties properties = new Properties();
        InputStream inputStream = MatchmakerConstants.class.getResourceAsStream("default.properties");
        if (inputStream != null) {
            properties.load(inputStream);
        } else {
            throw new Exception("Error while reading Matchmaker properties");
        }
        pdtUrl = properties.getProperty("pdt.url");
    }

}
