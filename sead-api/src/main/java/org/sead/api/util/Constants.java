package org.sead.api.util;

import java.io.InputStream;
import java.util.Properties;

public class Constants {

    public static String pdtUrl;
    public static String curBeeUrl;
    public static String matchmakerUrl;

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
    }
}
