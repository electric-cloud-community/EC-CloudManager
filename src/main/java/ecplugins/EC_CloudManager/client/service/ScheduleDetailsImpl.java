
// ScheduleDetailsImpl.java --
//
// ScheduleDetailsImpl.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.service;

import com.google.gwt.core.client.JavaScriptObject;

import ecplugins.EC_CloudManager.client.model.ScheduleDetails;

public class ScheduleDetailsImpl
    extends JavaScriptObject
    implements ScheduleDetails
{

    //~ Constructors -----------------------------------------------------------

    protected ScheduleDetailsImpl() { }

    //~ Methods ----------------------------------------------------------------

    @Override public final native String getInterval() /*-{
        return this.minutes;
    }-*/;

    @Override public final native boolean isEnabled() /*-{
        return this.disabled != "1";
    }-*/;

    public final native void setEnabled(boolean enabled) /*-{
        this.disabled = enabled ? "0" : "1";
    }-*/;

    @Override public final native void setInterval(String interval) /*-{
        this.minutes = interval;
    }-*/;
}
