package org.seadva.metadatagen.metagen.impl;

import noNamespace.*;
import org.apache.xmlbeans.XmlOptions;
import org.seadva.metadatagen.metagen.BaseMetadataGen;
import org.seadva.metadatagen.model.AggregationType;
import org.seadva.metadatagen.model.MetadataObject;
import org.seadva.metadatagen.util.ROQueryUtil;
import org.seadva.metadatagen.util.SeadNCEDConstants;

import java.net.URISyntaxException;
import java.util.*;


public class FGDCMetadataGen extends BaseMetadataGen {

    Map<String,List<String>> aggregation;
    Map<String,Map<String,List<String>>> properties;
    Map<String,AggregationType> typeProperty;

    ROQueryUtil util = new ROQueryUtil();

    private static String CREATOR = "http://purl.org/dc/elements/1.1/creator";
    //private static String CREATOR = "http://purl.org/dc/terms/creator";
    private static String TITLE = "http://www.w3.org/2000/01/rdf-schema#label";
    private static String ABSTRACT = "http://purl.org/dc/terms/abstract";
    private static String CONTACT = "";
    private static String PUB_DATE = "";
    private static String ONLINK = "";



    public FGDCMetadataGen() throws URISyntaxException {
        aggregation = new HashMap<String , List<String>>();
        properties = new HashMap<String, Map<String, List<String>>>();
        typeProperty = new HashMap<String, AggregationType>();
    }

    @Override
    public String generateMetadata(String id){
        typeProperty.put(id,AggregationType.COLLECTION);
        populateAggregationREST(id, true);

        Map<String, List<String>> properties = this.properties.get(id);

        HashSet<String> creators = new HashSet<String>();
        HashSet<String> contacts = new HashSet<String>();

        Iterator i1 = null;
        if (properties.get(CREATOR) != null) {
            i1 = properties.get(CREATOR).iterator();
        }

        while(i1.hasNext()) {
            String creator = (String)i1.next();
            creators.add(creator);
        }

        String title = null;
        if (properties.get(TITLE) != null) {
            title = properties.get(TITLE).get(0);
        }
        String ro_abstract = null;
        if (properties.get(ABSTRACT) != null) {
            ro_abstract = properties.get(ABSTRACT).get(0);
        }


        String result = "";

        result = toFGDC(title, creators, contacts, ro_abstract, "", "");
        return result;

    }

    private void populateAggregationREST(String tagId, boolean isRootCollection) {

        MetadataObject metadataObject = util.readMetadata(tagId);
        if (metadataObject == null){
            return;
        }

        Map<String, List<String>> metadata = metadataObject.getMetadataMap();


        for (Map.Entry<String, List<String>> pair : metadata.entrySet()) {
            List<String> values = pair.getValue();
            Map<String, List<String>> existingProperties;
            if (properties.containsKey(tagId))
                existingProperties = properties.get(tagId);
            else {
                existingProperties = new HashMap<String, List<String>>();
                properties.put(tagId, existingProperties);
            }
            for (String result : values) {
                List<String> existingValues = existingProperties.get(pair.getKey());
                if (existingValues == null) {
                    existingValues = new ArrayList<String>();
                    existingProperties.put(pair.getKey(), existingValues);
                }
                existingValues.add(result);
            }
        }

        List<String> collections = metadataObject.getCollections();
        for(String child : collections){
            List<String> children = aggregation.get(tagId);
            if (children == null)
                children = new ArrayList<String>();
            children.add(child);

            aggregation.put(tagId, children);

            if (!typeProperty.containsKey(child)) {
                typeProperty.put(child, AggregationType.COLLECTION);
            }
            populateAggregationREST(child, false);
        }

        Map<String, Map<String, List<String>>> files = metadataObject.getFiles();
        for (Map.Entry<String, Map<String, List<String>>> pair : files.entrySet()) {
            String filename = pair.getKey();
            List<String> children = aggregation.get(tagId);
            if (children == null)
                children = new ArrayList<String>();
            children.add(filename);
            aggregation.put(tagId, children);

            if (!typeProperty.containsKey(filename)) {
                typeProperty.put(filename, AggregationType.FILE);
                Map<String, List<String>> file_metadata = pair.getValue();
                Map<String, List<String>> existingProperties;
                if (properties.containsKey(filename))
                    existingProperties = properties.get(filename);
                else {
                    existingProperties = new HashMap<String, List<String>>();
                    properties.put(filename, existingProperties);
                }

                for (Map.Entry<String, List<String>> fileMetadataPair : file_metadata.entrySet()) {
                    List<String> values = fileMetadataPair.getValue();
                    for (String result : values) {
                        List<String> existingValues = existingProperties.get(fileMetadataPair.getKey());
                        if (existingValues == null) {
                            existingValues = new ArrayList<String>();
                            existingProperties.put(fileMetadataPair.getKey(), existingValues);
                        }
                        existingValues.add(result);
                    }
                }
            }

        }

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
        rngdatesType.setBegdate(SeadNCEDConstants.DEFAULT_BEGINDATE);
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
        metainfoType.setMetd(SeadNCEDConstants.DEFAULT_METD);

        if(contacts!=null){
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
}
