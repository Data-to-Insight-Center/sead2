package org.seadva.services.statusTracker.model.dao;

import org.seadva.services.statusTracker.model.obj.impl.CollectionStatus;

import java.util.List;

/**
 * Created by charmadu on 7/22/15.
 */
public interface CollectionStatusDao {

    public void putCollectionStatus(CollectionStatus collectionStatus);
    public List<CollectionStatus> getCollectionStatusById(String collectionId);
}
