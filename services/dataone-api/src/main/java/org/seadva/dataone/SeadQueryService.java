/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.seadva.dataone;

import org.apache.commons.io.IOUtils;
import org.dataconservancy.dcs.index.dcpsolr.SeadSolrService;
import org.dataconservancy.dcs.query.dcpsolr.SeadDataModelQueryService;
import org.dataone.service.types.v1.Event;
import org.jibx.runtime.*;
import org.seadva.ingest.Events;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * SEAD Query service with all constants
 */
public class SeadQueryService {

    static SeadSolrService solr;

    public static String NODE_IDENTIFIER;
    public static String SUBJECT;
    public static String BASE_URL;
    static Map<String,String> d1toSeadfixity;
    static Map<String,String> sead2d1fixity;
    static Map<String,String> d1toSeadEventTypes;
    static Map<String,String> sead2d1EventTypes;

    static Map<String,String> sead2d1Format;
    static Map<String,String> d12seadFormat;
    public static String solrPath;
    public static String datastreamURL;

    public static Map<String,String> mimeMapping ;
    public static SeadDataModelQueryService queryService;
    public static DataOneLogService dataOneLogService;

    static TransformerFactory factory;
    static Source xslt;
    static Transformer transformer;

    static {

           try {
               factory = TransformerFactory.newInstance();
               xslt = new StreamSource(SeadQueryService.class.getResourceAsStream("./DateFormat.xslt"));
               transformer = factory.newTransformer(xslt);

               StringWriter writer = new StringWriter();
               IOUtils.copy(SeadQueryService.class.getResourceAsStream("./Config.properties")
               , writer);

               String content = writer.toString();
               String[] pairs = content.trim().split(
                       "\n|\\=");


               for (int i = 0; i + 1 < pairs.length;) {
                   String name = pairs[i++].trim();
                   String value = pairs[i++].trim();
                   if (name.equals("datastream.url")) {
                       datastreamURL = value;
                   }
                   if (name.equals("solr.path")) {
                       SeadQueryService.solrPath = value;
                   }
                   if (name.equals("node.identifier")) {
                       NODE_IDENTIFIER = value;
                   }
                   if (name.equals("contact.subject")) {
                       SUBJECT = value.replace("-","=");
                   }
                   if (name.equals("base.url")) {
                       BASE_URL = value;
                   }
               }
            solr =
                    new SeadSolrService(new File(
                            solrPath
                    ));

            queryService = new SeadDataModelQueryService(solr);
            dataOneLogService = new DataOneLogService(solr);

            sead2d1Format = new HashMap<String, String>();
            sead2d1Format.put( "application/excel","application/vnd.ms-excel");
            sead2d1Format.put( "image/pjpeg","image/jpeg");
            sead2d1Format.put("http://www.fgdc.gov/schemas/metadata/fgdc-std-001-1998.xsd","FGDC-STD-001-1998");
            sead2d1Format.put("http://www.openarchives.org/ore/terms","http://www.openarchives.org/ore/terms");
            sead2d1Format.put( "application/mpeg","application/mp4");
            sead2d1Format.put( "http://ns.dataone.org/metadata/schema/onedcx/v1.0","http://ns.dataone.org/metadata/schema/onedcx/v1.0");

            d12seadFormat = new HashMap<String, String>();
            d12seadFormat.put( "application/vnd.ms-excel", "application/excel");
            d12seadFormat.put( "image/jpeg", "image/pjpeg");
            d12seadFormat.put("FGDC-STD-001-1998", "http://www.fgdc.gov/schemas/metadata/fgdc-std-001-1998.xsd");
            d12seadFormat.put("http://www.openarchives.org/ore/terms", "http://www.openarchives.org/ore/terms");
            d12seadFormat.put("application/mp4", "application/mpeg");
            d12seadFormat.put("http://ns.dataone.org/metadata/schema/onedcx/v1.0", "http://ns.dataone.org/metadata/schema/onedcx/v1.0");

            d1toSeadfixity = new HashMap<String, String>();
            d1toSeadfixity.put( "SHA-1", "SHA-1");
            d1toSeadfixity.put( "MD-5","MD5");

            sead2d1fixity = new HashMap<String, String>();
            sead2d1fixity.put( "SHA-1","SHA-1");
            sead2d1fixity.put( "MD5","MD-5");


            d1toSeadEventTypes = new HashMap<String, String>();
            d1toSeadEventTypes.put(Event.READ.xmlValue(), Events.FILE_D1READ);
            d1toSeadEventTypes.put(Event.READ.xmlValue(), Events.FILEMETADATA_D1READ);
            d1toSeadEventTypes.put(Event.REPLICATE.xmlValue(), Events.FILE_D1REPLICATE);


            sead2d1EventTypes = new HashMap<String, String>();
            sead2d1EventTypes.put(Events.FILE_D1READ, Event.READ.xmlValue());
            sead2d1EventTypes.put(Events.FILEMETADATA_D1READ, Event.READ.xmlValue());
            sead2d1EventTypes.put(Events.FILE_D1REPLICATE, Event.REPLICATE.xmlValue());

            mimeMapping = new HashMap<String,String>();

            loadMimeTypes();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ParserConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SAXException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (TransformerConfigurationException e) {
               e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
           }
    }

    private static void loadMimeTypes(){
        try{
            InputStream in =
                    SeadQueryService.class.getResourceAsStream("mime-mappings.csv");
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null)   {
                // Print the content on the console
                String[] arr = strLine.split(",");
                if(arr.length>1)
                    mimeMapping.put(arr[0],arr[1]);

            }
            //Close the input stream
            in.close();
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
        mimeMapping.put("FGDC-STD-001-1998","text/xml");
        mimeMapping.put("FGDC-STD-001.1-1999","text/xml");
        mimeMapping.put("FGDC-STD-001.2-1999","text/xml");
        mimeMapping.put("application/msword","application/msword,.doc");
        mimeMapping.put("application/pdf","application/pdf,.pdf");
        mimeMapping.put("application/octet-stream","application/octet-stream,.data");
        mimeMapping.put("application/postscript","application/postscript,.ps");
        mimeMapping.put("application/rtf","application/rtf,.rtf");
        mimeMapping.put("application/vnd.ms-excel","application/vnd.ms-excel,.xls");
        mimeMapping.put("application/zip","application/zip,.zip");
        mimeMapping.put("FGDC-STD-001-1998","text/xml,.xml");
        mimeMapping.put("FGDC-STD-001.1-1999","text/xml,.xml");
        mimeMapping.put("FGDC-STD-001.2-1999","text/xml,.xml");
        mimeMapping.put("http://www.openarchives.org/ore/terms","application/rdf+xml,.rdf");
        mimeMapping.put("image/bmp","image/bmp,.bmp");
        mimeMapping.put("image/gif","image/gif,.gif");
        mimeMapping.put("image/jp2","image/jp2,.jpg");
        mimeMapping.put("image/jpeg","image/jpeg,.jpg");
        mimeMapping.put("image/png","image/png,.png");
        mimeMapping.put("image/svg+xml","image/svg+xml,.svg");
        mimeMapping.put("image/tiff","image/tiff,.tiff");
        mimeMapping.put("INCITS 453-2009","text/xml,.xml");
        mimeMapping.put("netCDF-3","application/netcdf,.nc");
        mimeMapping.put("text/csv","text/csv,.csv");
        mimeMapping.put("text/html","text/html,.html");
        mimeMapping.put("text/n3","text/n3,.rdf");
        mimeMapping.put("text/plain","text/plain,.txt");
        mimeMapping.put("text/turtle","text/turtle,.ttl");
        mimeMapping.put("text/xml","text/xml,.xml");
        mimeMapping.put("video/quicktime","video/quicktime,.mov");
        mimeMapping.put("video/mpeg","video/mpeg,.mpg");

    }

    static String marshal(Serializable object) throws JiBXException, TransformerException {
        IBindingFactory bfact =
                BindingDirectory.getFactory(object.getClass());
        IMarshallingContext mctx = bfact.createMarshallingContext();

        StringWriter outputWriter = new StringWriter();

        mctx.marshalDocument(object, "UTF-8", true,
                outputWriter);

        StringWriter finaloutWriter = new StringWriter();
        transformer.transform(new StreamSource(new StringReader(outputWriter.toString())), new StreamResult(finaloutWriter));
        return finaloutWriter.toString();
    }

    static Serializable unmarshal(InputStream xml, Class className) throws JiBXException {
        IBindingFactory bfact =
                BindingDirectory.getFactory(className);

        IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
        Serializable obj = (Serializable)uctx.unmarshalDocument
                (xml, "UTF-8");
        return obj;
    }

}
