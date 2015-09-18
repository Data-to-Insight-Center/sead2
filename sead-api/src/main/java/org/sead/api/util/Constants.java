package org.sead.api.util;


import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

public class Constants {

    public static String pdtUrl;

    static {
        try {
            loadConfigurations();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadConfigurations() throws IOException {
        InputStream inputStream =
                Constants.class.getResourceAsStream("./default.properties");

        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer);

        String result = writer.toString();
        String[] pairs = result.trim().split(
                "\n|\\=");


        for (int i = 0; i + 1 < pairs.length;) {
            String name = pairs[i++].trim();
            String value = pairs[i++].trim();

            if(name.equals("pdt.url")){
                pdtUrl = value;
            }
        }
    }
}
