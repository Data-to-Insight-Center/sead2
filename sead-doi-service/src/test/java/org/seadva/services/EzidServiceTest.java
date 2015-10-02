package org.seadva.services;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit test for simple EzidService.
 */
public class EzidServiceTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public EzidServiceTest(String testName)
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( EzidServiceTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }

    public void testSetDOIUnavailable() throws IOException, InterruptedException {

       /* List<String> list = new ArrayList<String>(Arrays.asList("doi:10.5967/M0028PG8"));

        EzidService ezidService = new EzidService();

        System.out.println("length :" + list.size());
        for(String doi : list) {
            System.out.println("\nDOI : " + doi);
            boolean success = ezidService.setDOIUnavailable(doi);
            System.out.println("\t status : " + success);
            if(success == false){
                System.out.println("Breaking the loop");
                break;
            }
            Thread.sleep(1000);
        }*/
    }

    public void testCreateDOI() {

        /*EzidService ezidService = new EzidService();
        String target = "http://localhost:8080/Landing_Page/LandingPage?tag=?tag=tag:sead-data.net,2015:RO_6ogsujSI4r8Ken5CS98l-A";
        String metadata = "{\"creator\":\"Essawy, Bakinam \",\"title\":\"WSO_OutputViz\",\"pubDate\":\"Aug 24, 2015 9:29:41 AM\"}";
//ezidService.setPermanentDOI(true); // Set this only if a permanent DOI is needed
        String doi_url = ezidService.createDOI(metadata, target);
        System.out.println(doi_url);*/
    }
}
