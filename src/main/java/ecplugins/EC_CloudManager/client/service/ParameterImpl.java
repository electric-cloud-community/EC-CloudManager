
// ParameterImpl.java --
//
// ParameterImpl.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.service;

import com.google.gwt.core.client.JavaScriptObject;

import ecplugins.EC_CloudManager.client.model.Parameter;

public class ParameterImpl
    extends JavaScriptObject
    implements Parameter
{

    //~ Constructors -----------------------------------------------------------

    protected ParameterImpl()
    {
    }

    //~ Methods ----------------------------------------------------------------

    @Override public final native String getDefault() /*-{
        return this["default"];
    }-*/;

    @Override public final native String getDescription() /*-{
        return this.description;
    }-*/;

    @Override public final native String getParameterName() /*-{
        return this.parameterName;
    }-*/;

    @Override public final native String getRequired() /*-{
        return this.required;
    }-*/;
}
