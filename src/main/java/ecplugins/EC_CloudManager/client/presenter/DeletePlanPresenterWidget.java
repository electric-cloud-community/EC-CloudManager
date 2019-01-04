
// DeleteCloudPresenterWidget.java --
//
// DeleteCloudPresenterWidget.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.presenter;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;

import com.google.inject.Inject;

import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

import ecplugins.EC_CloudManager.client.Constants;
import ecplugins.EC_CloudManager.client.events.DeletePlanEvent;
import ecplugins.EC_CloudManager.client.model.PlanDetails;
import ecplugins.EC_CloudManager.client.service.CloudServiceAsync;
import ecplugins.EC_CloudManager.client.service.CommanderException;
import ecplugins.EC_CloudManager.client.view.DeletePlanUiHandlers;

public class DeletePlanPresenterWidget
    extends PresenterWidget<DeletePlanPresenterWidget.MyView>
    implements DeletePlanUiHandlers
{

    //~ Instance fields --------------------------------------------------------

    private CloudServiceAsync m_service;
    public PlanDetails m_cloud;
    private Constants         m_constants;

    //~ Constructors -----------------------------------------------------------

    @Inject public DeletePlanPresenterWidget(
            EventBus eventBus,
            MyView view,
            CloudServiceAsync service,
            Constants constants)
    {
        super(eventBus, view);
        m_service   = service;
        m_constants = constants;

        getView().setUiHandlers(this);
    }

    //~ Methods ----------------------------------------------------------------

    @Override public void cancel()
    {
        getView().hide();
    }

    @Override public void deleteCloud()
    {
        getView().setMessage(m_constants.deletingCloud(m_cloud.getName()));
        m_service.deletePlan(m_cloud.getName(), new AsyncCallback<Object>() {
            @Override
            public void onFailure(Throwable caught) {

                if (caught instanceof CommanderException) {
                    getView().hide();
                }
            }

            @Override
            public void onSuccess(Object result) {
                getView().hide();
                DeletePlanEvent.fire(DeletePlanPresenterWidget.this, m_cloud);
            }
        });
    }

    public void setCloud(PlanDetails cloud)
    {
        m_cloud = cloud;
        getView().setMessage(m_constants.deleteCloud(cloud.getName()));
    }

    //~ Inner Interfaces -------------------------------------------------------

    public interface MyView
        extends PopupView,
        HasUiHandlers<DeletePlanUiHandlers>
    {

        //~ Methods ------------------------------------------------------------

        void setMessage(String text);
    }
}
