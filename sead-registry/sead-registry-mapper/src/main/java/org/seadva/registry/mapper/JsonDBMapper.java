package org.seadva.registry.mapper;

import com.github.jsonldjava.utils.JsonUtils;
import org.apache.commons.io.IOUtils;
import org.dspace.foresite.OREException;
import org.dspace.foresite.Predicate;
import org.dspace.foresite.Vocab;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.seadva.registry.client.RegistryClient;
import org.seadva.registry.database.model.obj.vaRegistry.*;
import org.seadva.registry.database.model.obj.vaRegistry.Collection;
import org.seadva.registry.mapper.util.Constants;
import org.seadva.registry.mapper.util.RO;
import org.seadva.registry.database.model.obj.vaRegistry.File;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Map Registry content to JSON-LD
 */
public class JsonDBMapper {

    RegistryClient client;

    private static Predicate DC_TERMS_IDENTIFIER = null;
    private static Predicate DC_TERMS_SOURCE = null;
    private static Predicate METS_LOCATION = null;
    private static Predicate REPLICA_LOCATION = null;
    private static Predicate DC_TERMS_TITLE = null;
    private static Predicate DC_TERMS_FORMAT = null;
    private static Predicate DC_TERMS_ABSTRACT = null;
    private static Predicate DC_REFERENCES = null;
    private static Predicate DC_TERMS_RIGHTS = null;
    private static Predicate DC_TERMS_SIZE = null;
    private static Predicate DC_TERMS_CONTRIBUTOR = null;

    private static Predicate CITO_IS_DOCUMENTED_BY = null;
    private static Predicate DC_TERMS_TYPE = null;

    private static Predicate CITO_DOCUMENTS = null;

    // Labels in metadata receiving from workflow
    private static final String IDENTIFIER = "Identifier";
    private static final String HAS_FILES = "Has Files";
    private static final String HAS_SUBCOLLECTIONS = "Has Subcollection";
    private static final String CONTEXT = "@context";
    private static final String CO_STATUS = "CurationObject";
    private static final String OO_STATUS = "OtherObject";

    private boolean isSubCollection;

    public JsonDBMapper(String registryUrl) throws URISyntaxException {
        client =  new RegistryClient(registryUrl);

        DC_TERMS_IDENTIFIER = new Predicate();
        DC_TERMS_IDENTIFIER.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_IDENTIFIER.setPrefix(Vocab.dcterms_Agent.schema());
        //DC_TERMS_IDENTIFIER.setName("identifier");
        DC_TERMS_IDENTIFIER.setURI(new URI(Constants.identifierTerm));

        DC_TERMS_TITLE = new Predicate();
        DC_TERMS_TITLE.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_TITLE.setPrefix(Vocab.dcterms_Agent.schema());
        //DC_TERMS_TITLE.setName("title");
        DC_TERMS_TITLE.setURI(new URI(Constants.titleTerm));


        DC_TERMS_FORMAT = new Predicate();
        DC_TERMS_FORMAT.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_FORMAT.setPrefix(Vocab.dcterms_Agent.schema());
        //DC_TERMS_FORMAT.setName("format");
        DC_TERMS_FORMAT.setURI(new URI(Constants.formatTerm));

        DC_TERMS_ABSTRACT = new Predicate();
        DC_TERMS_ABSTRACT.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_ABSTRACT.setPrefix(Vocab.dcterms_Agent.schema());
        //DC_TERMS_ABSTRACT.setName("abstract");
        DC_TERMS_ABSTRACT.setURI(new URI(Constants.abstractTerm));

        DC_TERMS_SOURCE = new Predicate();
        DC_TERMS_SOURCE.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_SOURCE.setPrefix(Vocab.dcterms_Agent.schema());
        //DC_TERMS_SOURCE.setName("source");
        DC_TERMS_SOURCE.setURI(new URI(Constants.sourceTerm));

        DC_TERMS_CONTRIBUTOR = new Predicate();
        DC_TERMS_CONTRIBUTOR.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_CONTRIBUTOR.setPrefix(Vocab.dcterms_Agent.schema());
        //DC_TERMS_CONTRIBUTOR.setName("contributor");
        DC_TERMS_CONTRIBUTOR.setURI(new URI(Constants.contributor));

        METS_LOCATION = new Predicate();
        METS_LOCATION.setNamespace("http://www.loc.gov/METS");
        METS_LOCATION.setPrefix("http://www.loc.gov/METS");
        //METS_LOCATION.setName("FLocat");
        METS_LOCATION.setURI(new URI("http://www.loc.gov/METS/FLocat"));

        REPLICA_LOCATION = new Predicate();
        REPLICA_LOCATION.setNamespace("http://seadva.org/terms/");
        REPLICA_LOCATION.setPrefix("http://seadva.org/terms/");
        //REPLICA_LOCATION.setName("replica");
        REPLICA_LOCATION.setURI(new URI("http://seadva.org/terms/replica"));

        // create the CITO:isDocumentedBy predicate
        CITO_IS_DOCUMENTED_BY = new Predicate();
        CITO_IS_DOCUMENTED_BY.setNamespace("http://purl.org/spar/cito/");
        CITO_IS_DOCUMENTED_BY.setPrefix("cito");
        //CITO_IS_DOCUMENTED_BY.setName("isDocumentedBy");
        CITO_IS_DOCUMENTED_BY.setURI(new URI(Constants.documentedBy));

        DC_TERMS_TYPE = new Predicate();
        DC_TERMS_TYPE.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_TYPE.setPrefix(Vocab.dcterms_Agent.schema());
        DC_TERMS_TYPE.setName("type");
        DC_TERMS_TYPE.setURI(new URI(Constants.typeTerm));

        DC_TERMS_RIGHTS = new Predicate();
        DC_TERMS_RIGHTS.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_RIGHTS.setPrefix(Vocab.dcterms_Agent.schema());
        //DC_TERMS_RIGHTS.setName("rights");
        DC_TERMS_RIGHTS.setURI(new URI(Constants.rightsTerm));

        DC_TERMS_SIZE = new Predicate();
        DC_TERMS_SIZE.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_SIZE.setPrefix(Vocab.dcterms_Agent.schema());
        //DC_TERMS_SIZE.setName("SizeOrDuration");
        DC_TERMS_SIZE.setURI(new URI(Constants.sizeTerm));

        DC_REFERENCES = new Predicate();
        DC_REFERENCES.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_REFERENCES.setPrefix(Vocab.dcterms_Agent.schema());
        //DC_REFERENCES.setName("references");
        DC_REFERENCES.setURI(new URI(Constants.referencesTerm));

        // create the CITO:documents predicate
        CITO_DOCUMENTS = new Predicate();
        CITO_DOCUMENTS.setNamespace(CITO_IS_DOCUMENTED_BY.getNamespace());
        CITO_DOCUMENTS.setPrefix(CITO_IS_DOCUMENTED_BY.getPrefix());
        //CITO_DOCUMENTS.setName("documents");
        CITO_DOCUMENTS.setURI(new URI(Constants.documentsTerm));

        isSubCollection = false;
    }

    public String toJSONLD(String collectionId)
            throws URISyntaxException, OREException, IOException, ClassNotFoundException, JSONException {

        List<AggregationWrapper> aggregationWrappers = client.getAggregation(collectionId);
        BaseEntity baseEntity;
        RO ro = new RO();
        if(aggregationWrappers!=null)
            for(AggregationWrapper aggregationWrapper: aggregationWrappers){
                baseEntity = client.getEntity(aggregationWrapper.getParent().getId(),
                        aggregationWrapper.getParentType());
                ro.setParent(baseEntity);
                baseEntity = client.getEntity(aggregationWrapper.getChild().getId(),
                        aggregationWrapper.getChildType());
                ro.appendChild(baseEntity);
            }

        return ro.toJSON();
    }

    public void mapFromJson(String roJsonString) throws IOException {

        JSONParser jsonParser = new JSONParser();
        ContainerFactory containerFactory = new ContainerFactory(){
            public List creatArrayContainer() {
                return new LinkedList();
            }

            public Map createObjectContainer() {
                return new LinkedHashMap();
            }

        };
        Map json = null;
        try {
            json = (Map) jsonParser.parse(roJsonString, containerFactory);
        } catch(Exception e){

        }

        String roId = (String) json.get(IDENTIFIER);
        LinkedList hasFiles = (LinkedList)json.get(HAS_FILES);
        LinkedList hasSubCollections = (LinkedList)json.get(HAS_SUBCOLLECTIONS);

        //json.remove(IDENTIFIER); collection identifier is in the properties table
        json.remove(HAS_FILES);
        json.remove(HAS_SUBCOLLECTIONS);

        Map<String,String> contextMap = ((Map<String,String>)json.get(CONTEXT));
        contextMap.remove(IDENTIFIER); // since ID and external ID has same namespace
        contextMap.remove(HAS_FILES);
        contextMap.remove(HAS_SUBCOLLECTIONS);
        if(contextMap.get(DC_TERMS_TYPE.getName()) == null) {
            contextMap.put(DC_TERMS_TYPE.getName(), DC_TERMS_TYPE.getURI().toString());
        }
        json.remove(CONTEXT);

        Map<String, String> reversedContextMap = new HashMap<String, String>();
        for (String key : contextMap.keySet()){
            reversedContextMap.put(contextMap.get(key), key);
        }
        contextMap.put(IDENTIFIER, DC_TERMS_IDENTIFIER.getURI().toString()); // put this back since file indentifier should go to properties table

        // Add entity_id_resource_map if needed

        populateNSNames(reversedContextMap);

        Collection collection = new Collection();
        collection.setId(roId);
        collection.setVersionNum("1");
        collection.setIsObsolete(0);
        collection.setEntityCreatedTime(new Date());
        collection.setEntityLastUpdatedTime(new Date());

        if(!isSubCollection) {
            collection.setState(client.getStateByName(CO_STATUS)); // CO when persisting RO initially
            if(json.get(DC_TERMS_TYPE.getName()) == null) {
                json.put(DC_TERMS_TYPE.getName(), CO_STATUS);
            } else {
                List<String> types = json.get(DC_TERMS_TYPE.getName()) instanceof List ?
                        (List<String>)json.get(DC_TERMS_TYPE.getName()) : Arrays.asList((String)json.get(DC_TERMS_TYPE.getName()));
                types.add(CO_STATUS);
                json.put(DC_TERMS_TYPE.getName(), types);
            }

        } else {
            collection.setState(client.getStateByName(OO_STATUS)); // OO(OtherObject) for sub collections
        }

        if(DC_TERMS_TITLE.getName() != null && json.get(DC_TERMS_TITLE.getName()) != null){
            String title = json.get(DC_TERMS_TITLE.getName()) instanceof List
                    ? (String)((List) json.get(DC_TERMS_TITLE.getName())).get(0) : (String)json.get(DC_TERMS_TITLE.getName());
            collection.setName(title);
            collection.setEntityName(title);
            json.remove(DC_TERMS_TITLE.getName());
        }


        // Repository location
        if(METS_LOCATION.getName() != null && json.get(METS_LOCATION.getName()) != null){
            List<String> locations = json.get(METS_LOCATION.getName()) instanceof List
                    ? (List<String>)json.get(METS_LOCATION.getName()) : Arrays.asList((String)json.get(METS_LOCATION.getName()));
            for(String location : locations){
                String[] locArr = location.split(";");
                DataLocation dataLocation = new DataLocation();
                DataLocationPK dataLocationPK = new DataLocationPK();
                Repository repository = client.getRepositoryByName(locArr[0]);
                dataLocationPK.setLocationType(repository);
                dataLocation.setId(dataLocationPK);
                dataLocation.setIsMasterCopy(1);
                dataLocation.setLocationValue(locArr[2]);
                collection.addDataLocation(dataLocation);
            }
            json.remove(METS_LOCATION.getName());
        }

        // Not yet defined for jsonld
        if(REPLICA_LOCATION.getName() != null && json.get(REPLICA_LOCATION.getName()) != null){
            List<String> locations = json.get(REPLICA_LOCATION.getName()) instanceof List
                    ? (List<String>)json.get(REPLICA_LOCATION.getName()) : Arrays.asList((String)json.get(REPLICA_LOCATION.getName()));
            for(String location : locations){
                if(location.contains(";")) {
                    String[] locArr = location.split(";");
                    DataLocation dataLocation = new DataLocation();
                    DataLocationPK dataLocationPK = new DataLocationPK();
                    Repository repository = client.getRepositoryByName(locArr[0]);
                    dataLocationPK.setLocationType(repository);
                    dataLocation.setId(dataLocationPK);
                    dataLocation.setIsMasterCopy(0);
                    dataLocation.setLocationValue(locArr[2]);
                    collection.addDataLocation(dataLocation);
                }
            }
            json.remove(REPLICA_LOCATION.getName());
        }

        //Insert properties
        Property property;
        MetadataType metadataType;
        List<Property> properties = new ArrayList<Property>();

        for(Object metadataName: json.keySet()){
            if(contextMap.get(metadataName) == null)
                continue;

            String namespace = contextMap.get(metadataName);

            metadataType = client.getMetadataByType(URLEncoder.encode(namespace)); //eventually Map ORE element to DB element if they are going to be different
            if(metadataType == null || metadataType.getId() == null)
                continue;
            if(metadataType != null && json.get(metadataName) != null){
                List<String> values = json.get(metadataName) instanceof List
                        ? (List<String>)json.get(metadataName) : Arrays.asList((String)json.get(metadataName));
                for(String value : values) {
                    property = new Property();
                    property.setMetadata(metadataType);
                    property.setValuestr(value);
                    property.setEntity(collection);
                    properties.add(property);
                }
            }

        }
        Set<Property> propertiesSet = new HashSet<Property>();

        for(Property property1:properties)
            propertiesSet.add(property1);

        collection.setProperties(propertiesSet);
        client.postCollection(collection);

        //post relation between collection and resource map if needed

        //call recursively for sub collections
        for(Object subCollection : hasSubCollections){
            isSubCollection = true;

            if(!(subCollection instanceof Map) && METS_LOCATION.getName() != null
            && ((Map)subCollection).get(METS_LOCATION.getName()) == null)
                continue;

            String collectionLoc = (String) ((Map)subCollection).get(METS_LOCATION.getName());
            FileInputStream roFile = new FileInputStream(new java.io.File(collectionLoc));
            String colRoString = IOUtils.toString(roFile, "UTF-8");
            mapFromJson(colRoString);

            //Add sub collection aggregation
            List<AggregationWrapper> aggregationWrappers = new ArrayList<AggregationWrapper>();
            AggregationWrapper aggregationWrapper = new AggregationWrapper();
            aggregationWrapper.setParentType(Collection.class.getName());

            aggregationWrapper.setChildType(Collection.class.getName());
            BaseEntity child = new BaseEntity();
            child.setId((String) ((Map)subCollection).get(IDENTIFIER));
            aggregationWrapper.setChild(child);
            BaseEntity parent = new BaseEntity();
            parent.setId(roId);
            aggregationWrapper.setParent(parent);
            aggregationWrappers.add(aggregationWrapper);
            client.postAggregation(aggregationWrappers, roId);
        }

        //handle file metadata
        for(Object object : hasFiles){

            if(!(object instanceof Map))
                continue;

            File file = new File();
            Map fileMetadata = (Map)object;

            String fileId = (String)fileMetadata.get(IDENTIFIER);
            //fileMetadata.remove(IDENTIFIER); - this should be in the table

            file.setId(fileId);
            file.setVersionNum("1");
            file.setIsObsolete(0);
            file.setEntityCreatedTime(new Date());
            file.setEntityLastUpdatedTime(new Date());

            if(DC_TERMS_TITLE.getName() != null && fileMetadata.get(DC_TERMS_TITLE.getName()) != null){
                String title = fileMetadata.get(DC_TERMS_TITLE.getName()) instanceof List
                        ? (String)((List) fileMetadata.get(DC_TERMS_TITLE.getName())).get(0) : (String)fileMetadata.get(DC_TERMS_TITLE.getName());
                file.setFileName(title);
                file.setEntityName(title);
                //fileMetadata.remove(DC_TERMS_TITLE.getName()); - was in 1.5 properties table
            }

            if(DC_TERMS_SIZE.getName() != null && fileMetadata.get(DC_TERMS_SIZE.getName()) != null){
                String size = fileMetadata.get(DC_TERMS_SIZE.getName()) instanceof List
                        ? (String)((List) fileMetadata.get(DC_TERMS_SIZE.getName())).get(0) : (String)fileMetadata.get(DC_TERMS_SIZE.getName());
                file.setSizeBytes(Long.valueOf(size));
                //json.remove(DC_TERMS_SIZE.getName()); - was in 1.5 properties table
            }

            properties = new ArrayList<Property>();

            for(Object metadataName: fileMetadata.keySet()){
                if(contextMap.get(metadataName) == null)
                    continue;

                String namespace = contextMap.get(metadataName);
                metadataType = client.getMetadataByType(URLEncoder.encode(namespace));
                if(metadataType == null || metadataType.getId() == null)
                    continue;
                if(metadataType != null && fileMetadata.get(metadataName) != null){
                    List<String> values = fileMetadata.get(metadataName) instanceof List
                            ? (List<String>)fileMetadata.get(metadataName) : Arrays.asList((String)fileMetadata.get(metadataName));
                    for(String value : values) {
                        property = new Property();
                        property.setMetadata(metadataType);
                        property.setValuestr(value);
                        property.setEntity(collection);
                        properties.add(property);
                    }
                }
            }
            for(Property property1:properties)
                file.addProperty(property1);

            client.postFile(file);

            //post fixity if needed

            //Add file aggregation
            List<AggregationWrapper> aggregationWrappers = new ArrayList<AggregationWrapper>();
            AggregationWrapper aggregationWrapper = new AggregationWrapper();
            aggregationWrapper.setParentType(Collection.class.getName());

            aggregationWrapper.setChildType(File.class.getName());
            BaseEntity child = new BaseEntity();
            child.setId(fileId);
            aggregationWrapper.setChild(child);
            BaseEntity parent = new BaseEntity();
            parent.setId(roId);
            aggregationWrapper.setParent(parent);
            aggregationWrappers.add(aggregationWrapper);
            client.postAggregation(aggregationWrappers, roId);
        }
    }

    private void populateNSNames(Map<String, String> reversedContextMap) {

        if(reversedContextMap.containsKey(Constants.identifierTerm))
            DC_TERMS_IDENTIFIER.setName(reversedContextMap.get(Constants.identifierTerm));
        if(reversedContextMap.containsKey(Constants.titleTerm))
            DC_TERMS_TITLE.setName(reversedContextMap.get(Constants.titleTerm));
        if(reversedContextMap.containsKey(Constants.formatTerm))
            DC_TERMS_FORMAT.setName(reversedContextMap.get(Constants.formatTerm));
        if(reversedContextMap.containsKey(Constants.abstractTerm))
            DC_TERMS_ABSTRACT.setName(reversedContextMap.get(Constants.abstractTerm));
        if(reversedContextMap.containsKey(Constants.contentSourceTerm))
            DC_TERMS_SOURCE.setName(reversedContextMap.get(Constants.contentSourceTerm));
        if(reversedContextMap.containsKey(Constants.contributor))
            DC_TERMS_CONTRIBUTOR.setName(reversedContextMap.get(Constants.contributor));
        if(reversedContextMap.containsKey(Constants.sourceTerm))
            METS_LOCATION.setName(reversedContextMap.get(Constants.sourceTerm));
        if(reversedContextMap.containsKey(Constants.replicaTerm))
            REPLICA_LOCATION.setName(reversedContextMap.get(Constants.replicaTerm));
        if(reversedContextMap.containsKey(Constants.documentedBy))
            CITO_IS_DOCUMENTED_BY.setName(reversedContextMap.get(Constants.documentedBy));
        if(reversedContextMap.containsKey(Constants.typeTerm))
            DC_TERMS_TYPE.setName(reversedContextMap.get(Constants.typeTerm));
        if(reversedContextMap.containsKey(Constants.rightsTerm))
            DC_TERMS_RIGHTS.setName(reversedContextMap.get(Constants.rightsTerm));
        if(reversedContextMap.containsKey(Constants.sizeTerm))
            DC_TERMS_SIZE.setName(reversedContextMap.get(Constants.sizeTerm));
        if(reversedContextMap.containsKey(Constants.referencesTerm))
            DC_REFERENCES.setName(reversedContextMap.get(Constants.referencesTerm));
        if(reversedContextMap.containsKey(Constants.documentsTerm))
            CITO_DOCUMENTS.setName(reversedContextMap.get(Constants.documentsTerm));

    }


    public void readJSONLD(java.io.File filename) throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        ContainerFactory containerFactory = new ContainerFactory(){
            public List creatArrayContainer() {
                return new LinkedList();
            }

            public Map createObjectContainer() {
                return new LinkedHashMap();
            }

        };
        try{
            Map json = (Map)jsonParser.parse(new FileReader(filename), containerFactory);
            Iterator iter = json.entrySet().iterator();
            System.out.println("==iterate result==");
            Object contextString = json.get("@context");
            System.out.println(contextString);


//            JSONObject jsonObject1 = (JSONObject)object1;
//            JSONArray context = (JSONArray)jsonObject1.get("@context");
//
//            JSONParser jsonParser1 = new JSONParser();
//            Object object = jsonParser1.parse(new FileReader(filename));
//            JSONObject jsonObject = (JSONObject)object;
//            for(Iterator iterator = jsonObject.keySet().iterator(); iterator.hasNext();) {
//                String key = (String) iterator.next();
//
//                System.out.println(jsonObject.get(key));
//            }
//
//            Object obj = jsonParser1.parse(new FileReader(filename));
//            JSONArray jsonArray = (JSONArray)obj;
//            for(int i=0; i<jsonArray.size(); i++){
//                System.out.println("The " + i + " element of the array: " + jsonArray.get(i));
//            }
//            // BaseEntity
//            Collection collection = new Collection();
//            collection.setId(new String("http://localhost:8080/entity/1001"));
//            collection.setName(json.get("Title").toString());
//            collection.setEntityCreatedTime(new Date());
//            collection.setEntityLastUpdatedTime(new Date());
//            collection.setVersionNum("1");
//            collection.setIsObsolete(0);
//            collection.setState(client.getStateByName("PO"));
//            //
//            Property property = new Property();
//            MetadataType metadataType = new MetadataType();
//            if(json.get("Abstract").toString() != null) {
//                metadataType = client.getMetadataByType("abstract");
//                if(metadataType != null){
//                    property.setMetadata(metadataType);
//                    property.setValuestr(json.get("Abstract").toString());
//                    property.setEntity(collection);
//                    collection.addProperty(property);
//                }
//            }
//
//            List<Property> properties = new ArrayList<Property>();
            while(iter.hasNext()){
                Map.Entry entry = (Map.Entry)iter.next();
                System.out.println(entry.getKey() + "=>" + entry.getValue());
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }
}
