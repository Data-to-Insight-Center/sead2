//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.sead.va.dataone.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class UidGenerator {
    static String lastUIDFilePath = "lastGeneratedUID.txt";

    public UidGenerator() {
    }

    public static Integer generateNextUID() {
        Integer lastUID = getLastGeneratedUID();
        Integer newUID;
        if(lastUID != null) {
            newUID = Integer.valueOf(lastUID.intValue() + 1);
        } else {
            newUID = Integer.valueOf(0);
        }

        setLastGeneratedUID(newUID);
        return newUID;
    }

    private static Integer getLastGeneratedUID() {
        try {
            Scanner e = new Scanner(new File(lastUIDFilePath));
            String lastUIDString = e.next();
            Integer lastUID = Integer.valueOf(lastUIDString);
            e.close();
            return lastUID;
        } catch (FileNotFoundException var3) {
            return Integer.valueOf(0);
        }
    }

    private static void setLastGeneratedUID(Integer uidValue) {
        try {
            FileWriter e = new FileWriter(lastUIDFilePath, false);
            e.write(uidValue.toString());
            e.close();
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }
}
