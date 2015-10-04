package org.sead.sda.agent.engine;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReader {

    public static String sdaHost;
    public static String sdaUser;
    public static String sdaPassword;

    public static String sdaResearchObjects;
    public static String allResearchObjects;
    public static String landingPage;
    public static String callDaemons;
    public static String dummySDA;
    public static String clowderUser;
    public static String clowderPassword;
    public static String sdaPath;

    public static String doiEndpoint;
    public static String isDoiPermanent;

    public static void init(String configPath) {
        try {
            loadConfigurations(configPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadConfigurations(String configPath) throws Exception {
        Properties properties = new Properties();
        InputStream inputStream = new FileInputStream(configPath);
        properties.load(inputStream);
        // read properties
        sdaHost = properties.getProperty("sftp.host");
        sdaUser = properties.getProperty("sftp.user");
        sdaPassword = properties.getProperty("sftp.pass");
        sdaResearchObjects = properties.getProperty("sda.research.objects");
        allResearchObjects = properties.getProperty("all.research.objects");
        landingPage = properties.getProperty("landing.page.url");
        callDaemons = properties.getProperty("call.daemons");
        dummySDA = properties.getProperty("dummy.sda");
        clowderUser = properties.getProperty("clowder.user");
        clowderPassword = properties.getProperty("clowder.pass");
        sdaPath = properties.getProperty("sda.path");
        doiEndpoint = properties.getProperty("doi.service.url");
        isDoiPermanent = properties.getProperty("doi.permanent");
    }

}
