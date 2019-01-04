
// CloudManagerFactory.java --
//
// CloudManagerFactory.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client;

import com.google.gwt.core.client.GWT;

import com.gwtplatform.mvp.client.DelayedBindRegistry;

import ecinternal.client.InternalGinComponentBaseFactory;

import ecplugins.EC_CloudManager.client.gin.CloudManagerGinjector;

import com.electriccloud.commander.gwt.client.ComponentContext;

public class CloudManagerFactory
    extends InternalGinComponentBaseFactory<CloudManagerGinjector>
{

    //~ Constructors -----------------------------------------------------------

    CloudManagerFactory()
    {
        super(GWT.<CloudManagerGinjector>create(CloudManagerGinjector.class));
    }

    //~ Methods ----------------------------------------------------------------

    @Override public void onCommanderInit(ComponentContext context)
    {
        super.onCommanderInit(context);

        // Show the default place.
        getInjector().getPlaceManager()
                     .revealCurrentPlace();
    }

    @Override public void onModuleLoad()
    {

        // Finish binding GWTP objects
        DelayedBindRegistry.bind(getInjector());
        super.onModuleLoad();
    }
}
