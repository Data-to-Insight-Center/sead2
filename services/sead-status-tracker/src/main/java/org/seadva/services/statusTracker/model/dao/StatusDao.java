package org.seadva.services.statusTracker.model.dao;

import org.seadva.services.statusTracker.model.obj.impl.Status;

public interface StatusDao {

    public void putStatus(Status status);
    public Status getStatusById(String id);
}
