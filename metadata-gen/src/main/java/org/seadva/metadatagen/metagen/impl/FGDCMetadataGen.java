package org.seadva.metadatagen.metagen.impl;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import noNamespace.*;
import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.XmlOptions;
import org.bson.Document;
import org.seadva.metadatagen.metagen.BaseMetadataGen;
import org.seadva.metadatagen.model.AggregationType;
import org.seadva.metadatagen.model.MetadataObject;
import org.seadva.metadatagen.util.Constants;
import org.seadva.metadatagen.util.ROQueryUtil;
import org.seadva.metadatagen.util.SeadNCEDConstants;

import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;


public class FGDCMetadataGen extends BaseMetadataGen {


    static MongoCollection<Document> fgdcCollection;
    static MongoCollection<Document> roCollection;
    static MongoClient mongoClient;
    static MongoDatabase db;
    static MongoDatabase pdtDb;

    static {
        mongoClient = new MongoClient();
        db = mongoClient.getDatabase(Constants.metagenDbName);
        fgdcCollection = db.getCollection(Constants.dbFgdcCollection);
        pdtDb = mongoClient.getDatabase(Constants.pdtDbName);
        roCollection = pdtDb.getCollection(Constants.pdtRoCollection);
    }

    ROQueryUtil util = new ROQueryUtil();

    private static String CREATOR = "Creator";
    //private static String CREATOR = "http://purl.org/dc/terms/creator";
    private static String TITLE = "Title";
    private static String ABSTRACT = "Abstract";
    private static String CONTACT = "Contact";
    private static String PUB_DATE = "";
    private static String ONLINK = "";


    @Override
    public String generateMetadata(String id){

        FindIterable<Document> iter = roCollection.find(new Document(
                "Aggregation.Identifier", id));

        Document document = iter.first();
        if(document==null) {
            return "";
        }

        Document aggregation = (Document)document.get("Aggregation");
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
            creators.add(aggregation.get(CREATOR).toString());
        } else if(aggregation.get(CREATOR) instanceof ArrayList) {
            ArrayList list = (ArrayList)aggregation.get(CREATOR);
            for(Object creator : list){
                creators.add(creator.toString());
            }
        }

        Set<String>  contacts = new HashSet<String>();
        if(aggregation.get(CONTACT) instanceof String){
            contacts.add(aggregation.get(CONTACT).toString());
        } else if(aggregation.get(CONTACT) instanceof ArrayList) {
            ArrayList list = (ArrayList)aggregation.get(CONTACT);
            for(Object contact : list){
                contacts.add(contact.toString());
            }
        }

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMdd");
        Date now = new Date();
        String pubDate = sdfDate.format(now);

        String result = "";

        result = toFGDC(title, creators, contacts, ro_abstract, pubDate, "");
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
