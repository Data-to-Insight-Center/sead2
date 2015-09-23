package org.seadva.metadatagen.metagen.impl;

import org.dspace.foresite.*;
import org.dspace.foresite.jena.TripleJena;
import org.json.JSONException;
import org.seadva.metadatagen.metagen.BaseMetadataGen;
import org.seadva.metadatagen.model.AggregationType;
import org.seadva.metadatagen.model.MetadataObject;
import org.seadva.metadatagen.util.Constants;
import org.seadva.metadatagen.util.ROQueryUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.*;


public class OREMetadataGen extends BaseMetadataGen {

    Map<String,List<String>> aggregation;
    Map<String,Map<String,List<String>>> properties;
    Map<String,AggregationType> typeProperty;

    ROQueryUtil util = new ROQueryUtil();

    private static final String RESOURCE_MAP_SERIALIZATION_FORMAT = "RDF/XML";
    private static String DEFAULT_URL_PREFIX = "http://seadva.d2i.indiana.edu/";


    public OREMetadataGen() throws URISyntaxException {
        aggregation = new HashMap<String , List<String>>();
        properties = new HashMap<String, Map<String, List<String>>>();
        typeProperty = new HashMap<String, AggregationType>();
    }

    @Override
    public String generateMetadata(String id){
        typeProperty.put(id,AggregationType.COLLECTION);
        populateAggregationREST(id, true);

        String result = "";

        try {
            result = toOAIORE(null, null, id, Constants.bagPath);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (OREException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ORESerialiserException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
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
     * Creates an ORE for a given id  that is passed as argument
     * @param agg
     * @param rem
     * @param id
     * @param bagPath
     * @throws java.io.IOException
     * @throws org.json.JSONException
     * @throws java.net.URISyntaxException
     * @throws org.dspace.foresite.OREException
     * @throws org.dspace.foresite.ORESerialiserException
     */
    public String toOAIORE(Aggregation agg,
                           OREResource rem,
                           String id,
                           String bagPath)
            throws IOException, JSONException, URISyntaxException, OREException, ORESerialiserException {

        String guid = null;

        if(id.contains("/"))
            guid = id.split("/")[id.split("/").length-1];
        else
            guid = id.split(":")[id.split(":").length-1];
        AggregationType type = typeProperty.get(id);


        if(type == AggregationType.COLLECTION){

            String aggId = id;

            if(!aggId.startsWith("http:"))
                aggId = DEFAULT_URL_PREFIX + URLEncoder.encode(id);
            agg = OREFactory.createAggregation(new URI(
                    aggId
            ));

            rem = agg.createResourceMap(
                    new URI(
                            aggId + "_ResourceMap"
                    ));
        }


        String title = "";


        List<String> results = aggregation.get(id);

        if(results!=null)           //This part checks for child entities (files or sub-collections) so their properties can also be populated in the ORE.
            for(String child:results){

                /*
                If current entity has child ids, then it checks whether they are collection or file  and recursively calls the method to populate child properties in the ORE
                 */
                if(typeProperty.get(child) == AggregationType.COLLECTION){
                    //If collection, a  new ORE file is generated for the aggregation and the toOAIORE is called recursively to
                    // populate the properties of the collection


                    String uri = child;
                    /*try {
                        new URI(child);
                    } catch (URISyntaxException x) {*/
                    if(!child.startsWith("http:"))
                        uri = DEFAULT_URL_PREFIX + URLEncoder.encode(child);
                    //}

                    //Based on http://www.openarchives.org/ore/0.1/datamodel#nestedAggregations
                    AggregatedResource subAggResource = agg.createAggregatedResource(
                            new URI(
                                    uri
                            )

                    );
                    subAggResource.addType(new URI(agg.getTypes().get(0).toString()));//type aggregation

                    Triple resourceMapSource = new TripleJena();
                    resourceMapSource.initialise(subAggResource);

                    String childguid = null;
                    if(child.contains("/"))
                        childguid = child.split("/")[child.split("/").length-1];
                    else
                        childguid = child.split(":")[child.split(":").length-1];

                    Predicate DC_TERMS_SOURCE =  new Predicate();
                    DC_TERMS_SOURCE.setNamespace(Vocab.dcterms_Agent.ns().toString());
                    DC_TERMS_SOURCE.setPrefix(Vocab.dcterms_Agent.schema());
                    URI sourceUri = new URI(Constants.sourceTerm);
                    DC_TERMS_SOURCE.setName("FLocat");
                    DC_TERMS_SOURCE.setURI(sourceUri);
                    resourceMapSource.relate(DC_TERMS_SOURCE,
                            bagPath + "/" + childguid + "_oaiore.xml");
                    rem.addTriple(resourceMapSource);

                    agg.addAggregatedResource(subAggResource);

                    String remChildId = child;
                    /*try {
                        new URI(child);
                    } catch (URISyntaxException x) {*/
                    if(!child.startsWith("http:"))
                        remChildId = DEFAULT_URL_PREFIX + URLEncoder.encode(child);
                    //}

                    Aggregation newAgg = OREFactory.createAggregation(new URI(
                            remChildId+"_Aggregation"
                    ));
                    ResourceMap newRem = newAgg.createResourceMap(new URI(
                            remChildId
                    ));


                    toOAIORE(newAgg, newRem, child, bagPath);  //the new aggregation and  resourceMap is passed recursively, so that the sub-collection(sub-aggregation) properties are populated
                }
                else{
                    /*
                    If child is a file, the toOAIORE method is recursively called to populate the properties of the file.
                     */
                    String uri = child;
                    if(!uri.startsWith("http:"))
                        uri = DEFAULT_URL_PREFIX + URLEncoder.encode(child);

                    AggregatedResource dataResource = agg.createAggregatedResource(new URI(uri));
                    agg.addAggregatedResource(dataResource);   // The dataResource for the file is added to the current aggregation i.e connection is made between the aggregation and file resource

                    toOAIORE(agg, dataResource, child, bagPath); //the file resource is passed recursively, so that the file properties are populated in the recursive call
                }
            }

        //This section gets properties of the current entity (collection/file) and puts them in the ORE map. The properties data structure should be populated beforehand by querying ACR for metadata by ACRQueryHandler

        if (properties.get(id) != null) {
            Iterator props = properties.get(id).entrySet().iterator();
            while(props.hasNext()) {
                Map.Entry<String,List<String>> pair = (Map.Entry) props.next();

                for(String value: pair.getValue()){
                    if(value==null)
                        continue;
                    Triple triple = new TripleJena();
                    if(typeProperty.get(id)== AggregationType.COLLECTION)
                        triple.initialise(agg);
                    else if(typeProperty.get(id)== AggregationType.FILE)
                        triple.initialise(rem);

                    Predicate ORE_TERM_PREDICATE =  new Predicate();
                    ORE_TERM_PREDICATE.setNamespace(Vocab.dcterms_Agent.ns().toString());
                    ORE_TERM_PREDICATE.setPrefix(Vocab.dcterms_Agent.schema());
                    URI uri = new URI((String)pair.getKey());
                    //  System.out.println("------------------"+pair.getKey()+"-------------------");
                    ORE_TERM_PREDICATE.setName(uri.toString().substring(uri.toString().lastIndexOf("/")));
                    ORE_TERM_PREDICATE.setURI(uri);
                    triple.relate(ORE_TERM_PREDICATE, value);
                    if(typeProperty.get(id)== AggregationType.COLLECTION)
                        agg.addTriple(triple);
                    else if(typeProperty.get(id)== AggregationType.FILE)
                        rem.addTriple(triple);
                }
            }
        }

        //Finally, a check is made to see if the entity is a Collection (if file, do nothing). If it is a collection, then the ORE strcuture in memory is serialized as a file.
        if(typeProperty.get(id)== AggregationType.COLLECTION){

            FileWriter oreStream = new FileWriter(bagPath +"/" + guid + "_oaiore.xml");
            BufferedWriter ore = new BufferedWriter(oreStream);

            String resourceMapXml = "";
            ORESerialiser serial = ORESerialiserFactory.getInstance(RESOURCE_MAP_SERIALIZATION_FORMAT);
            ResourceMapDocument doc = serial.serialise((ResourceMap)rem);
            resourceMapXml = doc.toString();

            ore.write(resourceMapXml);
            ore.close();

            return resourceMapXml;
        }

        return "";

    }
}
