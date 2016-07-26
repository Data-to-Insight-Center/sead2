/*
 *
 * Copyright 2015 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author kavchand@imail.iu.edu
 * @author charmadu@umail.iu.edu
 */

package org.seadva.metadatagen.metagen.impl;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import noNamespace.*;
import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.XmlOptions;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
import org.seadva.metadatagen.metagen.BaseMetadataGen;
import org.seadva.metadatagen.util.Constants;
import org.seadva.metadatagen.util.ROQueryUtil;
import org.seadva.metadatagen.util.SeadNCEDConstants;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;


public class FGDCMetadataGen extends BaseMetadataGen {


    static WebResource pdtWebService;

    private String doi;
    private List<String> creatorsList = new ArrayList<String>();
    private boolean production;

    static {
        pdtWebService = Client.create().resource(Constants.pdtURL);
    }

    public boolean isProduction() {
        return production;
    }

    public void setProduction(boolean production) {
        this.production = production;
    }

    public FGDCMetadataGen(String doi){
        this.doi = doi;
    }

    public List<String> getCreatorsList() {
        return creatorsList;
    }

    ROQueryUtil util = new ROQueryUtil();

    private static String CREATOR = "Creator";
    //private static String CREATOR = "http://purl.org/dc/terms/creator";
    private static String TITLE = "Title";
    private static String ABSTRACT = "Abstract";
    private static String CONTACT = "Contact";
    private static String PUB_DATE = "";
    private static String ONLINK = "";
    private static String PREFERENCES = "Preferences";
    private static String PURPOSE = "Purpose";
    private static String TESTING_ONLY = "Testing-Only";


    @Override
    public String generateMetadata(String id){

        ClientResponse roResponse = pdtWebService.path("researchobjects")
                .path(id)
                .accept("application/json")
                .type("application/json")
                .get(ClientResponse.class);
        Document roDoc = Document.parse(roResponse.getEntity(String.class));

        if(roDoc==null) {
            return "";
        }

        if(roDoc.get(PREFERENCES) != null
                && ((Document)roDoc.get(PREFERENCES)).get(PURPOSE) != null
                && ((Document)roDoc.get(PREFERENCES)).get(PURPOSE) instanceof String
                && ((String)((Document)roDoc.get(PREFERENCES)).get(PURPOSE)).equals(TESTING_ONLY)) {
            production = false;
        } else {
            production = true;
        }

        Document aggregation = (Document)roDoc.get("Aggregation");
        String title = "";
        if(aggregation.get(TITLE) instanceof String){
            title = aggregation.get(TITLE).toString();
        } else if(aggregation.get(TITLE) instanceof ArrayList) {
            title = StringUtils.join(((ArrayList)aggregation.get(TITLE)).toArray(), ",");
        }

        String ro_abstract = "";
        if(aggregation.get(ABSTRACT) instanceof String){
            ro_abstract = aggregation.get(ABSTRACT).toString();
        } else if(aggregation.get(ABSTRACT) instanceof ArrayList) {
            ro_abstract = StringUtils.join(((ArrayList)aggregation.get(ABSTRACT)).toArray(), ",");
        }

        Set<String>  creators = new HashSet<String>();
        if(aggregation.get(CREATOR) instanceof String){
            String creator = getPerson((String) aggregation.get(CREATOR));
            if(!creator.equals("")){
                creators.add(creator);
                creatorsList.add(creator);
            } else {
                creators.add((String) aggregation.get(CREATOR));
            }
        } else if(aggregation.get(CREATOR) instanceof ArrayList) {
            ArrayList list = (ArrayList)aggregation.get(CREATOR);
            for(Object creator : list){
                String creatorFullName = getPerson(creator.toString());
                if(!creatorFullName.equals("")){
                    creators.add(creatorFullName);
                    creatorsList.add(creatorFullName);
                } else {
                    creators.add(creator.toString());
                }
            }
        }

        Set<String> contacts = new HashSet<String>();
        if (aggregation.get(CONTACT) instanceof String) {
            String contact = getPerson((String) aggregation.get(CONTACT));
            if (!contact.equals("")) {
                contacts.add(contact);
            } else {
                contacts.add((String) aggregation.get(CONTACT));
            }
        } else if (aggregation.get(CONTACT) instanceof ArrayList) {
            ArrayList list = (ArrayList) aggregation.get(CONTACT);
            for (Object contactObject : list) {
                String contact = getPerson(contactObject.toString());
                if (!contact.equals("")) {
                    contacts.add(contact);
                } else {
                    contacts.add(contactObject.toString());
                }
            }
        }

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMdd");
        Date now = new Date();
        String pubDate = sdfDate.format(now);
        
        String result = toFGDC(title, creators, contacts, ro_abstract, pubDate, this.doi);
        String fgdcXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + result;

        fgdcXML = fgdcXML.replace("<metadata>","<metadata xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                " xsi:noNamespaceSchemaLocation=\"http://www.fgdc.gov/metadata/fgdc-std-001-1998.xsd\">");

        return fgdcXML;

    }

    /**
     * Creates an FGDC metadata
     */
    public String toFGDC(String title,
                         Set<String> creators,
                         Set<String> contacts,
                         String abstrct,
                         String publicationDate,
                         String onlink) //Arguments added by Kavitha
    {
        MetadataDocument metadataDoc = MetadataDocument.Factory.newInstance();
        MetadataType metadataType = metadataDoc.addNewMetadata();

        IdinfoType idinfoType = metadataType.addNewIdinfo();
        CitationType citationType = idinfoType.addNewCitation();
        CiteinfoType citeinfoType = citationType.addNewCiteinfo();



        if(creators!=null){
            Iterator<String> it = creators.iterator();
            while(it.hasNext()) {
                OriginType originType = citeinfoType.addNewOrigin();
                originType.setStringValue(it.next());
            }
        }
        else{
            OriginType originType = citeinfoType.addNewOrigin();
            originType.setStringValue(SeadNCEDConstants.DEFAULT_ORIGINATOR);
        }


        if(publicationDate!=null)
            citeinfoType.setPubdate(publicationDate);
        else
            citeinfoType.setPubdate(SeadNCEDConstants.DEFAULT_PUBDATE);

        if(title!=null)
            citeinfoType.setTitle(title);
        else
            citeinfoType.setTitle(SeadNCEDConstants.DEFAULT_UUID);

        if(onlink != null) {
            OnlinkType onlinkType = citeinfoType.addNewOnlink();
            onlinkType.setStringValue(onlink);    //commented by Kavitha
        }

        DescriptType descriptType = idinfoType.addNewDescript();
        if(abstrct!=null)
            descriptType.setAbstract(abstrct);
        else
            descriptType.setAbstract(SeadNCEDConstants.DEFAULT_ABSTRACT);

        descriptType.setPurpose(SeadNCEDConstants.DEFAULT_PURPOSE);

        TimeperdType timeperdType = idinfoType.addNewTimeperd();

        TimeinfoType timeinfoType = timeperdType.addNewTimeinfo();

        RngdatesType rngdatesType = timeinfoType.addNewRngdates();

        if(publicationDate!=null)
            rngdatesType.setBegdate(publicationDate);
        else
            rngdatesType.setBegdate(SeadNCEDConstants.DEFAULT_BEGINDATE);

        if(publicationDate!=null)
            rngdatesType.setEnddate(publicationDate);
        else
            rngdatesType.setEnddate(SeadNCEDConstants.DEFAULT_ENDDATE);

        timeperdType.setCurrent(SeadNCEDConstants.DEFAULT_CURRENTREF);

        StatusType statusType = idinfoType.addNewStatus();

        statusType.setProgress(SeadNCEDConstants.DEFAULT_PROGRESS);
        statusType.setUpdate(SeadNCEDConstants.DEFAULT_MAINTUPDATEFREQ);

        SpdomType spdomType = idinfoType.addNewSpdom();
        BoundingType boundingType = spdomType.addNewBounding();
        boundingType.setEastbc(SeadNCEDConstants.DEFAULT_EASTBOUND);
        boundingType.setWestbc(SeadNCEDConstants.DEFAULT_WESTBOUND);
        boundingType.setNorthbc(SeadNCEDConstants.DEFAULT_NORTHBOUND);
        boundingType.setSouthbc(SeadNCEDConstants.DEFAULT_SOUTHBOUND);

        KeywordsType keywordsType = idinfoType.addNewKeywords();
        ThemeType themeType = keywordsType.addNewTheme();
        themeType.setThemekt(SeadNCEDConstants.DEFAULT_THEMEKT);
        themeType.setThemekeyArray(SeadNCEDConstants.DEFAULT_THEMEKEYS);



        PlaceType placeType = keywordsType.addNewPlace();
        placeType.setPlacekt(SeadNCEDConstants.DEFAULT_PLACEKT);
        placeType.setPlacekeyArray(SeadNCEDConstants.DEFAULT_PLACEKEYS);

        TemporalType temporalType = keywordsType.addNewTemporal();
        temporalType.setTempkt(SeadNCEDConstants.DEFAULT_TEMPORALKT);
        temporalType.setTempkeyArray(SeadNCEDConstants.DEFAULT_TEMPORALKEYS);

        idinfoType.setAccconst(SeadNCEDConstants.DEFAULT_ACCESSCONSTRAINT);
        idinfoType.setUseconst(SeadNCEDConstants.DEFAULT_USECONSTRAINT);

        MetainfoType metainfoType = metadataType.addNewMetainfo();
        if(publicationDate!=null)
            metainfoType.setMetd(publicationDate);
        else
            metainfoType.setMetd(SeadNCEDConstants.DEFAULT_METD);

        if(contacts!=null && !contacts.isEmpty()){
            String contactsAppended = "";
            Iterator<String> it = contacts.iterator();
            int i=0;
            while(it.hasNext()) {
                contactsAppended+=it.next();
                if(i!=contacts.size()-1)
                    contactsAppended+=";";
                i++;
            }
            if(contactsAppended.length()>1)
            {
                MetcType metcType = metainfoType.addNewMetc();
                CntinfoType metadataContact = CntinfoType.Factory.newInstance();
                CntperpType cntperpType = metadataContact.addNewCntperp();
                cntperpType.setCntper(contactsAppended);
                CntaddrType cntaddrType = metadataContact.addNewCntaddr();
                cntaddrType.setAddrtype("Mailing");
                cntaddrType.setCity("Unknown");
                cntaddrType.setState("Unknown");
                cntaddrType.setPostal("00000");
                CntvoiceType cntvoiceType = metadataContact.addNewCntvoice();
                cntvoiceType.setStringValue("Unknown");
                metcType.setCntinfo(metadataContact);
            }
        }
        else{
            MetcType metcType = metainfoType.addNewMetc();
            metcType.setCntinfo(SeadNCEDConstants.DEFAULT_METADATACONTACT);
        }


        metainfoType.setMetstdn(SeadNCEDConstants.DEFAULT_METADATANAME);
        metainfoType.setMetstdv(SeadNCEDConstants.DEFAULT_METADATAVERS);

        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setSavePrettyPrint();
        return metadataDoc.xmlText(xmlOptions);

    }

    private String getPerson(String id) {
        String creator = "";
        ClientResponse response = pdtWebService.path("people/" + URLEncoder.encode(id))
                .accept("application/json")
                .type("application/json")
                .get(ClientResponse.class);
        if (response.getStatus() == 200) {
            try {
                JSONObject object = new JSONObject(response.getEntity(new GenericType<String>() {}));
                String name = "";
                if (object.has("givenName")) {
                    name += object.get("givenName");
                }
                if (object.has("familyName")) {
                    name += " " + object.get("familyName");
                }
                if (!name.equals("")) {
                    creator = name.trim();
                }
            } catch (JSONException e) {
                System.out.println(e.getMessage());
            }
        }
        return creator;
    }
}
