package org.sead.sda;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class SDAObjectReaderTest extends TestCase {
    public SDAObjectReaderTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(SDAObjectReaderTest.class);
    }

    public void testReadCollection() {
        assertTrue(true);
    }

    public void readCollection() {

        Sftp sftp = new Sftp();
        String bgName = "A short name";
        String target = Constants.sdaPath + bgName ;
        if (sftp.doesFileExist(target)) {
            System.out.println("File exists!!!");
            Stat stat = new Stat();
            stat.setTab(0);
            sftp.listFiles(target, stat);

            System.out.println("\n\nResult : " + stat.toString());
        }

        sftp.disConnectSessionAndChannel();
    }


}


