package org.seadva.registry.mapper.util;

import org.json.JSONException;
import org.seadva.registry.database.model.obj.vaRegistry.BaseEntity;
import org.seadva.registry.database.model.obj.vaRegistry.Collection;
import org.seadva.registry.database.model.obj.vaRegistry.File;
import org.seadva.registry.database.model.obj.vaRegistry.Property;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.*;

/**
 * The RO object is used to represent a collection with its parent
 * and children (sub-collection)
 */
public class RO {
    // parent points to parent's entity id
    // children contains a list of child entity ids
    BaseEntity parent;
    List<BaseEntity> children;

    public RO(){
        parent = null;
        children = new ArrayList<BaseEntity>();
    }

    public void setParent(BaseEntity entity){
        parent = entity;
    }

    public BaseEntity getParent(){
        return parent;
    }

    public List<BaseEntity> getChildren(){
        return children;
    }

    public void appendChild(BaseEntity entity){
        children.add(entity);
    }

    public String toJSON() throws JSONException {
        BaseEntity parent = getParent();
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();

        // Create the context section first
        if(parent != null){
            for(Property property:parent.getProperties()){
                objectBuilder.add(property.getMetadata().getMetadataElement(),
                        property.getMetadata().getMetadataSchema()+property.getMetadata().getMetadataElement());
            }
        }
        objectBuilder.add("Has Subcollection","http://purl.org/dc/terms/hasPart");
        objectBuilder.add("Has Files","http://purl.org/dc/terms/hasPart");

        // Context build done

        // Now start constructing the main object starting with the context
        // and then with properties of the parent
        JsonObjectBuilder mainBuilder = Json.createObjectBuilder();
        if(parent != null){
            Map<String, Object> propertyMap = createPropertyMap(parent.getProperties());
            for(String key: propertyMap.keySet()){
                if(propertyMap.get(key) instanceof String) {
                    mainBuilder.add(key, (String)propertyMap.get(key));
                }else {
                    mainBuilder.add(key, (JsonArrayBuilder)propertyMap.get(key));
                }
            }
        }

        // Now start adding the details of the children starting with
        // list of entity id of the sub collection

        JsonArrayBuilder subCollection = Json.createArrayBuilder();
        JsonObjectBuilder fileBuilder = Json.createObjectBuilder();
        JsonArrayBuilder filesList = Json.createArrayBuilder();

        // Get the list of children
        List<BaseEntity> list = getChildren();

        // For each entity, if the entity is the collection
        // add the collection id to the ArrayList - subCollList
        // else if the entity is a file, get the details of
        // the file into the fileHolder
        for(BaseEntity entity:list){
            if(entity instanceof Collection)
                subCollection.add(entity.getId());
            else if(entity instanceof File){
                Map<String, Object> filePropertyMap = createPropertyMap(entity.getProperties());
                for(String key:filePropertyMap.keySet()) {

                    if (filePropertyMap.get(key) instanceof String) {
                        fileBuilder.add(key, (String) filePropertyMap.get(key));
                    } else {
                        fileBuilder.add(key, (JsonArrayBuilder) filePropertyMap.get(key));
                    }
                }

//                    if(property.getMetadata().getId().equalsIgnoreCase("md:6")){
//                        if(!property.getValuestr().equalsIgnoreCase("-1")){
//                            fileBuilder.add(property.getMetadata().getMetadataElement(), property.getValuestr());
//                        }
//                    }else
                        //fileBuilder.add(property.getMetadata().getMetadataElement(), property.getValuestr());
                for(Property property:entity.getProperties()) {
                    objectBuilder.add(property.getMetadata().getMetadataElement(),
                            property.getMetadata().getMetadataSchema()+property.getMetadata().getMetadataElement());
                }
                filesList.add(fileBuilder.build());
            }
        }
        mainBuilder.add("Has Subcollection", subCollection);
        mainBuilder.add("Has Files", filesList);

        JsonObject context = objectBuilder.build();
        mainBuilder.add("@context", context);

        JsonObject finalObject = mainBuilder.build();
        return finalObject.toString();
    }

    private Map<String, Object> createPropertyMap(Set<Property> propertySet){

        Map<String, Object> map = new HashMap<String, Object>();

        for(Property property : propertySet){
            String key = property.getMetadata().getMetadataElement();
            String value = property.getValuestr();
            if(map.containsKey(key)) {
                if(map.get(key) instanceof String) {
                    JsonArrayBuilder array = Json.createArrayBuilder();
                    array.add((String)map.get(key));
                    array.add(value);
                    map.put(key, array);
                } else {
                    ( (JsonArrayBuilder)map.get(key) ).add(value);
                }
            }else {
                map.put(key, value);
            }
        }
        return map;
    }
}

