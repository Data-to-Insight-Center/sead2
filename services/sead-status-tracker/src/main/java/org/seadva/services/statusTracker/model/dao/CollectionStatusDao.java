package org.seadva.services.statusTracker.model.dao;

import org.seadva.services.statusTracker.model.obj.impl.CollectionStatus;

import java.util.List;

public interface CollectionStatusDao {

    public void putCollectionStatus(CollectionStatus collectionStatus);
    public List<CollectionStatus> getCollectionStatusById(String collectionId);
}
