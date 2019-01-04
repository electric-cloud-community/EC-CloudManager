
// ProviderDetailsImpl.java --
//
// ProviderDetailsImpl.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.service;

import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import ecplugins.EC_CloudManager.client.model.Parameter;
import ecplugins.EC_CloudManager.client.model.ProviderDetails;

import static ecinternal.client.request.JsUtils.toList;

public class ProviderDetailsImpl
    extends JavaScriptObject
    implements ProviderDetails
{

    //~ Constructors -----------------------------------------------------------

    protected ProviderDetailsImpl()
    {
    }

    //~ Methods ----------------------------------------------------------------

    @Override public final List<Parameter> getGrowParameters()
    {
        return toList(getGrowParams());
    }

    private native JsArray<ParameterImpl> getGrowParams() /*-{
        if (typeof(this.growParams) == "undefined") {
        this.growParams = new Array();
        }
        return this.growParams;
    }-*/;

    @Override public final native String getName() /*-{
        return this.name;
    }-*/;

    @Override public final List<Parameter> getShrinkParameters()
    {
        return toList(getShrinkParams());
    }

    private native JsArray<ParameterImpl> getShrinkParams() /*-{
        if (typeof(this.shrinkParams) == "undefined") {
        this.shrinkParams = new Array();
        }
        return this.shrinkParams;
    }-*/;
}
