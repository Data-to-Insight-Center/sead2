package org.sead.workflow.util;

import java.util.Random;

public class IdGenerator {

    private static String PREFIX = "sead_";

    public static String generateRandomID(){

        String val = "";
        // char or numbers (5), random 0-9 A-Z
        for(int i = 0; i<6;){
            int ranAny = 48 + (new Random()).nextInt(90-48+1);

            if(!(57 < ranAny && ranAny<= 65)){
                char c = (char)ranAny;
                val += c;
                i++;
            }

        }

        return PREFIX + val;
    }
}
