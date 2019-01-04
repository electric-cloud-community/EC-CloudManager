
// ActualParameterImpl.java --
//
// ActualParameterImpl.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.service;

import com.google.gwt.core.client.JavaScriptObject;

public class ActualParameterImpl
    extends JavaScriptObject
{

    //~ Constructors -----------------------------------------------------------

    protected ActualParameterImpl()
    {
    }

    //~ Methods ----------------------------------------------------------------

    public static native ActualParameterImpl create(
            String key,
            String value) /*-{
        return { "name" : key, "value": value}
    }-*/;

    public final native String getName() /*-{
        return this.name;
    }-*/;

    public final native String getValue() /*-{
        return this.value;
    }-*/;

    public final native void setName(String value) /*-{
        this.name = value;
    }-*/;

    public final native void setValue(String value) /*-{
        this.value = value;
    }-*/;
}
