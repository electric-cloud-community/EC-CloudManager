
// ScheduleDetails.java --
//
// ScheduleDetails.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.model;

public interface ScheduleDetails
{

    //~ Methods ----------------------------------------------------------------

    String getInterval();

    boolean isEnabled();

    void setEnabled(boolean enabled);

    void setInterval(String interval);
}
