package org.seadva.services.statusTracker.model.obj.impl;

import org.seadva.services.statusTracker.model.obj.ICollectionStatus;

import java.util.Date;

/**
 * Created by charmadu on 7/22/15.
 */
public class CollectionStatus implements ICollectionStatus {

    private String collectionId;
    private String currentStatus;
    private Long statusUpdatedTime;

    @Override
    public String getCollectionId() {
        return collectionId;
    }

    @Override
    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    @Override
    public String getCurrentStatus() {
        return currentStatus;
    }

    @Override
    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    @Override
    public Long getUpdatedTime() {
        return statusUpdatedTime;
    }

    @Override
    public void setUpdatedTime(Long updatedTime) {
        this.statusUpdatedTime = updatedTime;
    }
}
