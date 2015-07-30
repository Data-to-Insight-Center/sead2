package org.seadva.services.statusTracker.model.obj;

import java.util.Date;

public interface ICollectionStatus {

    public String getCollectionId();
    public void setCollectionId(String collectionId);
    public String getCurrentStatus();
    public void setCurrentStatus(String currentStatus);
    public Long getUpdatedTime();
    public void setUpdatedTime(Long updatedTime);

}
