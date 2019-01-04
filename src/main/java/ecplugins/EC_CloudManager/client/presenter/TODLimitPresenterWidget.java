
// TODLimitPresenterWidget.java --
//
// TODLimitPresenterWidget.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.presenter;

import java.util.logging.Logger;

import org.jetbrains.annotations.NonNls;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.HasValue;

import com.google.inject.Inject;

import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

import ecplugins.EC_CloudManager.client.events.AddLimit;
import ecplugins.EC_CloudManager.client.events.AddLimitEvent;
import ecplugins.EC_CloudManager.client.model.TimeOfDayLimit;
import ecplugins.EC_CloudManager.client.service.CloudServiceAsync;
import ecplugins.EC_CloudManager.client.view.DialogUiHandlers;

import static ecplugins.EC_CloudManager.client.presenter.PlanEditorPresenter.HOURS_PER_DAY;

public class TODLimitPresenterWidget
    extends PresenterWidget<TODLimitPresenterWidget.MyView>
    implements DialogUiHandlers
{

    //~ Static fields/initializers ---------------------------------------------

    @NonNls private static final Logger log = Logger.getLogger(
            "ConfigurePresenterWidget");

    //~ Constructors -----------------------------------------------------------

    @Inject public TODLimitPresenterWidget(
            EventBus          eventBus,
            MyView            view)
    {
        super(eventBus, view);
        getView().setUiHandlers(this);
    }

    //~ Methods ----------------------------------------------------------------

    @Override public void cancel()
    {
        getView().hide();
    }

    @Override public void save()
    {
        MyView view = getView();
        int start = Integer.valueOf(view.getStart().getValue());
        int end = Integer.valueOf(view.getEnd().getValue());
        if (end == start) {
            end += HOURS_PER_DAY;
        }
        String max = view.getMax().getValue();
        String min = view.getMin().getValue();

        AddLimitEvent.fire(this, new TimeOfDayLimit(start, end, min, max));

        view.hide();
    }

    @Override protected void onReveal()
    {
        super.onReveal();
    }

    //~ Inner Interfaces -------------------------------------------------------

    @SuppressWarnings({"PublicInnerClass"})
    public interface MyView
        extends PopupView,
        HasUiHandlers<DialogUiHandlers>
    {

        //~ Methods ------------------------------------------------------------

        HasValue<String> getEnd();

        HasValue<String> getMax();

        HasValue<String> getMin();

        HasValue<String> getStart();
    }
}
