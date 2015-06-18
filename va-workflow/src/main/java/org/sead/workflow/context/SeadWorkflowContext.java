package org.sead.workflow.context;

import org.sead.workflow.model.PSInstance;

import java.util.HashMap;

/**
 * Holds runtime information of a workflow invocation. Each activity in the workflow
 * can use the context instance to retrieve or store shared information.
 */
public class SeadWorkflowContext {

    // registry collection id
    private String collectionId = null;

    // set of config level parameters
    private HashMap<String, String> properties = new HashMap<String, String>();

    // object that holds the project space configurations
    private PSInstance PSInstance = null;

    public PSInstance getPSInstance() {
        return PSInstance;
    }

    public void setPSInstance(PSInstance PSInstance) {
        this.PSInstance = PSInstance;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public void updateProperty(String key, String value) {
        properties.put(key, value);
    }

}
