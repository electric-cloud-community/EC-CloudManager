
// CloudConfigurationsImpl.java --
//
// CloudConfigurationsImpl.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.service;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import ecplugins.EC_CloudManager.client.model.PlanDetails;
import ecplugins.EC_CloudManager.client.model.ProviderDetails;

import static ecinternal.client.request.JsUtils.toList;

public class CloudConfigurationsImpl
    extends JavaScriptObject
    implements CloudConfigurations
{

    //~ Constructors -----------------------------------------------------------

    protected CloudConfigurationsImpl()
    {
    }

    //~ Methods ----------------------------------------------------------------

    @NotNull private native JsArray<PlanDetailsImpl> getPlanDetails() /*-{
        if (typeof(this.cfg) == "undefined") {
        this.cfg = new Array();
        }
        return this.cfg;
    }-*/;

    @NotNull @Override public final List<PlanDetails> getPlans()
    {
        return toList(getPlanDetails());
    }

    @NotNull private native JsArray<ProviderDetailsImpl> getProviderDetails() /*-{
        if (typeof(this.plugins) == "undefined") {
        this.plugins = new Array();
        }
        return this.plugins;
    }-*/;

    @NotNull @Override public final List<ProviderDetails> getProviders()
    {
        return toList(getProviderDetails());
    }
}
