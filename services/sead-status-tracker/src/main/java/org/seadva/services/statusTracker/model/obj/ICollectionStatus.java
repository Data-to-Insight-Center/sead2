package org.seadva.services.statusTracker.model.obj;

import java.util.Date;

/**
 * Created by charmadu on 7/22/15.
 */
public interface ICollectionStatus {

    public String getCollectionId();
    public void setCollectionId(String collectionId);
    public String getCurrentStatus();
    public void setCurrentStatus(String currentStatus);
    public Long getUpdatedTime();
    public void setUpdatedTime(Long updatedTime);

}
