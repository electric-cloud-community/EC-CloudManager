
// CloudManagerModule.java --
//
// CloudManagerModule.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.gin;

import java.util.logging.Logger;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import ecinternal.client.ComponentController;
import ecinternal.client.LoggerLog;

import ecinternal.client.ui.ActionCell;

import ecplugins.EC_CloudManager.client.CloudManagerPlaceManager;
import ecplugins.EC_CloudManager.client.model.ManagerState;
import ecplugins.EC_CloudManager.client.model.ManagerStateImpl;
import ecplugins.EC_CloudManager.client.presenter.CloudManagerPresenter;
import ecplugins.EC_CloudManager.client.presenter.ConfigurePresenterWidget;
import ecplugins.EC_CloudManager.client.presenter.DeletePlanPresenterWidget;
import ecplugins.EC_CloudManager.client.presenter.CopyPlanPresenterWidget;
import ecplugins.EC_CloudManager.client.presenter.DetailsPresenter;
import ecplugins.EC_CloudManager.client.presenter.PlanEditorPresenter;
import ecplugins.EC_CloudManager.client.presenter.PlanListPresenter;
import ecplugins.EC_CloudManager.client.presenter.TODLimitPresenterWidget;
import ecplugins.EC_CloudManager.client.service.CloudServiceAsync;
import ecplugins.EC_CloudManager.client.service.CloudServiceImpl;
import ecplugins.EC_CloudManager.client.view.CloudManagerView;
import ecplugins.EC_CloudManager.client.view.ConfigureView;
import ecplugins.EC_CloudManager.client.view.DeletePlanView;
import ecplugins.EC_CloudManager.client.view.CopyPlanView;
import ecplugins.EC_CloudManager.client.view.DetailsView;
import ecplugins.EC_CloudManager.client.view.PlanEditorView;
import ecplugins.EC_CloudManager.client.view.PlanListView;
import ecplugins.EC_CloudManager.client.view.TODLimitView;

import com.electriccloud.commander.gwt.client.requests.CgiRequestProxy;

@SuppressWarnings({"OverlyCoupledMethod", "OverlyCoupledClass"})
public class CloudManagerModule
    extends AbstractPresenterModule
{

    //~ Methods ----------------------------------------------------------------

    @Override protected void configure()
    {
        bind(CloudManagerPlaceManager.class).in(Singleton.class);
        bind(PlaceManager.class).to(CloudManagerPlaceManager.class);
        bind(CloudServiceAsync.class).to(CloudServiceImpl.class)
                                     .in(Singleton.class);
        bind(ActionCell.class);
        bind(ManagerState.class).to(ManagerStateImpl.class)
                                .asEagerSingleton();

        // Presenters
        bindPresenter(CloudManagerPresenter.class,
            CloudManagerPresenter.MyView.class, CloudManagerView.class,
            CloudManagerPresenter.MyProxy.class);
        bindPresenter(PlanListPresenter.class, PlanListPresenter.MyView.class,
            PlanListView.class, PlanListPresenter.MyProxy.class);
        bindPresenter(PlanEditorPresenter.class,
            PlanEditorPresenter.MyView.class, PlanEditorView.class,
            PlanEditorPresenter.MyProxy.class);
        bindPresenter(DetailsPresenter.class, DetailsPresenter.MyView.class,
            DetailsView.class, DetailsPresenter.MyProxy.class);
        bindSingletonPresenterWidget(DeletePlanPresenterWidget.class,
            DeletePlanPresenterWidget.MyView.class, DeletePlanView.class);
        bindSingletonPresenterWidget(CopyPlanPresenterWidget.class,
            CopyPlanPresenterWidget.MyView.class, CopyPlanView.class);
        bindSingletonPresenterWidget(ConfigurePresenterWidget.class,
            ConfigurePresenterWidget.MyView.class, ConfigureView.class);
        bindSingletonPresenterWidget(TODLimitPresenterWidget.class,
            TODLimitPresenterWidget.MyView.class, TODLimitView.class);
    }

    @Provides @Singleton CgiRequestProxy provideCgiRequestProxy(
            ComponentController controller)
    {
        return new CgiRequestProxy(controller.getPluginName(),
            "CloudService.cgi");
    }

    @Provides LoggerLog providerLoggerLog()
    {
        return new LoggerLog(Logger.getLogger("ecplugins.EC_CloudManager"));
    }
}
