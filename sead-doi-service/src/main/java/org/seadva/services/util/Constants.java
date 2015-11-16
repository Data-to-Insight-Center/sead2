package org.seadva.services.util;

import java.io.IOException;
import java.util.Properties;

public class Constants {

    public static String ezid_url;
    public static String doi_shoulder_prod;
    public static String doi_shoulder_test;
    public static String doi_username;
    public static String doi_password;

    static {
            Properties props= new Properties();
            try {
				props.load(Constants.class.getResourceAsStream("doi.properties"));
			} catch (IOException e) {
				System.err.println("Unable to load doi.properties");
				e.printStackTrace();
			}
            ezid_url = props.getProperty("ezid.url", "http://ezid.lib.purdue.edu"); 
            doi_shoulder_prod=props.getProperty("doi.shoulder.prod","doi:10.5967/M0" );
            doi_shoulder_test=props.getProperty("doi.shoulder.test","doi:10.5072/FK2" );
            doi_username=props.getProperty("doi.user", "apitest");
            doi_password=props.getProperty("doi.pwd", "apitest");
    }
}
