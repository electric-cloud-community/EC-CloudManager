
// CloudManagerGinjector.java --
//
// CloudManagerGinjector.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.gin;

import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.inject.client.GinModules;

import com.google.inject.Provider;

import com.gwtplatform.mvp.client.proxy.PlaceManager;

import ecinternal.client.gin.CommanderGinjector;
import ecinternal.client.gin.CommanderModule;

import ecplugins.EC_CloudManager.client.presenter.CloudManagerPresenter;
import ecplugins.EC_CloudManager.client.presenter.DetailsPresenter;
import ecplugins.EC_CloudManager.client.presenter.PlanEditorPresenter;
import ecplugins.EC_CloudManager.client.presenter.PlanListPresenter;
import ecplugins.EC_CloudManager.client.presenter.RootComponentPresenter;

@GinModules({CloudManagerModule.class, CommanderModule.class})
@SuppressWarnings({"InterfaceNeverImplemented"})
public interface CloudManagerGinjector
    extends CommanderGinjector
{

    //~ Methods ----------------------------------------------------------------

    Provider<CloudManagerPresenter> getCloudManagerPresenter();

    AsyncProvider<DetailsPresenter> getDetailsPresenter();

    PlaceManager getPlaceManager();

    AsyncProvider<PlanEditorPresenter> getPlanEditorPresenter();

    AsyncProvider<PlanListPresenter> getPlanListPresenter();

    RootComponentPresenter getRootComponentPresenter();
}
