package org.seadva.services.statusTracker.model.dao;

import org.seadva.services.statusTracker.model.obj.impl.Status;

/**
 * Created by charmadu on 7/22/15.
 */
public interface StatusDao {

    public void putStatus(Status status);
    public Status getStatusById(String id);
}
