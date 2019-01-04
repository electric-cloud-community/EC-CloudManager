
// ConfigurePresenterWidget.java --
//
// ConfigurePresenterWidget.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.presenter;

import java.util.logging.Logger;

import org.jetbrains.annotations.NonNls;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasValue;

import com.google.inject.Inject;

import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

import com.gwtplatform.mvp.client.proxy.LockInteractionEvent;
import ecplugins.EC_CloudManager.client.model.ScheduleDetails;
import ecplugins.EC_CloudManager.client.service.CloudServiceAsync;
import ecplugins.EC_CloudManager.client.view.DialogUiHandlers;

public class ConfigurePresenterWidget
    extends PresenterWidget<ConfigurePresenterWidget.MyView>
    implements DialogUiHandlers
{

    //~ Static fields/initializers ---------------------------------------------

    @NonNls private static final Logger log = Logger.getLogger(
            "ConfigurePresenterWidget");

    //~ Instance fields --------------------------------------------------------

    private final CloudServiceAsync m_service;

    //~ Constructors -----------------------------------------------------------

    @Inject public ConfigurePresenterWidget(
            EventBus          eventBus,
            MyView            view,
            CloudServiceAsync service)
    {
        super(eventBus, view);
        m_service = service;
        getView().setUiHandlers(this);
    }

    //~ Methods ----------------------------------------------------------------

    @Override public void cancel()
    {
        getView().hide();
    }

    @Override public void save()
    {
        final MyView view = getView();

        m_service.setSchedule(view.getEnabled()
                                  .getValue(), view.getInterval()
                                                   .getValue(),
            new AsyncCallback<Object>() {
                @Override public void onFailure(Throwable caught)
                {
                    log.severe(caught.getMessage());
                }

                @Override public void onSuccess(Object result)
                {
                    view.hide();
                }
            });
    }

    @Override protected void onReveal()
    {
        super.onReveal();
        getView().showLoading(true);
        m_service.getSchedule(new AsyncCallback<ScheduleDetails>() {
                @Override public void onFailure(Throwable caught)
                {
                    log.severe("Unable to get schedule: " + caught);
                    LockInteractionEvent.fire(ConfigurePresenterWidget.this, false);

                    cancel();
                }

                @Override public void onSuccess(ScheduleDetails result)
                {
                    MyView view = getView();

                    view.getEnabled()
                        .setValue(result.isEnabled());
                    view.getInterval()
                        .setValue(result.getInterval());
                    getView().showLoading(false);
                }
            });
    }

    //~ Inner Interfaces -------------------------------------------------------

    @SuppressWarnings({"PublicInnerClass"})
    public interface MyView
        extends PopupView,
        HasUiHandlers<DialogUiHandlers>
    {

        //~ Methods ------------------------------------------------------------

        HasValue<Boolean> getEnabled();

        HasValue<String> getInterval();

        void showLoading(boolean loading);
    }
}
