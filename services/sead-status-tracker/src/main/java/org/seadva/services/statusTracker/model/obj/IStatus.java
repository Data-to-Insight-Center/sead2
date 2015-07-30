package org.seadva.services.statusTracker.model.obj;

import java.util.Date;

public interface IStatus {

    public String getStatusId();
    public void setStatusId(String statusId);
    public String getComponent();
    public void setComponent(String component);
    public String getDescription();
    public void setDescription(String description);

}
