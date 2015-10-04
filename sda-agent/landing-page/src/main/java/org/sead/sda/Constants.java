package org.sead.sda;

import java.io.InputStream;
import java.util.Properties;

public class Constants {

    public static String sdaHost;
    public static String sdaUser;
    public static String sdaPassword;
    public static String allResearchObjects;

    static {
        try {
            loadConfigurations();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadConfigurations() throws Exception {
        Properties properties = new Properties();
        InputStream inputStream = Constants.class.getResourceAsStream("config.properties");
        if (inputStream != null) {
            properties.load(inputStream);
        } else {
            throw new Exception("Error while reading SDA landing page properties");
        }
        // read properties
        sdaHost = properties.getProperty("sftp.host");
        sdaUser = properties.getProperty("sftp.user");
        sdaPassword = properties.getProperty("sftp.pass");
        allResearchObjects = properties.getProperty("all.research.objects");
    }

}
