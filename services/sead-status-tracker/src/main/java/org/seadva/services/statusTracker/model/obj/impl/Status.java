package org.seadva.services.statusTracker.model.obj.impl;

import org.seadva.services.statusTracker.model.obj.IStatus;

/**
 * Created by charmadu on 7/22/15.
 */
public class Status implements IStatus {

    private String statusId;
    private String component;
    private String description;

    @Override
    public String getStatusId() {
        return statusId;
    }

    @Override
    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    @Override
    public String getComponent() {
        return component;
    }

    @Override
    public void setComponent(String component) {
        this.component = component;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }
}
