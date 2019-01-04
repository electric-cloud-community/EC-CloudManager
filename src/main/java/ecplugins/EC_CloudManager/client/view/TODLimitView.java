
// TODLimitView.java --
//
// TODLimitView.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.view;

import javax.inject.Inject;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import ecinternal.client.ui.InternalUIFactory;

import ecplugins.EC_CloudManager.client.Constants;
import ecplugins.EC_CloudManager.client.presenter.TODLimitPresenterWidget.MyView;

import static ecplugins.EC_CloudManager.client.presenter.PlanEditorPresenter.HOURS_PER_DAY;

public class TODLimitView
    extends PopupViewWithUiHandlers<DialogUiHandlers>
    implements MyView
{

    //~ Instance fields --------------------------------------------------------

    Widget           m_widget;
    @UiField Button  m_save;
    @UiField Button  m_cancel;
    @UiField(provided = true)
    ListBox          m_start;
    @UiField(provided = true)
    ListBox          m_end;
    @UiField TextBox m_max;
    @UiField TextBox m_min;

    //~ Constructors -----------------------------------------------------------

    @Inject public TODLimitView(
            Binder            uiBinder,
            Constants         constants,
            InternalUIFactory uiFactory,
            EventBus          eventBus)
    {
        super(eventBus);
        m_start  = (ListBox) uiFactory.createValuedListBox();
        m_end    = (ListBox) uiFactory.createValuedListBox();
        m_widget = uiBinder.createAndBindUi(this);

        for (int i = 0; i < HOURS_PER_DAY; ++i) {
            m_start.addItem(constants.time(i, 0), Integer.toString(i));
            m_end.addItem(constants.time(i, 59),
                Integer.toString((i + 1) % HOURS_PER_DAY));
        }

        // Set initial values of 9am - 5pm
        ((HasValue<String>) m_start).setValue("9");
        ((HasValue<String>) m_end).setValue("17");
    }

    //~ Methods ----------------------------------------------------------------

    @Override public Widget asWidget()
    {
        return m_widget;
    }

    @UiHandler("m_cancel")
    void handleCancel(ClickEvent event)
    {
        getUiHandlers().cancel();
    }

    @UiHandler("m_save")
    void handleSave(ClickEvent event)
    {
        getUiHandlers().save();
    }

    @Override public HasValue<String> getEnd()
    {
        return (HasValue<String>) m_end;
    }

    @Override public HasValue<String> getMax()
    {
        return m_max;
    }

    @Override public HasValue<String> getMin()
    {
        return m_min;
    }

    @Override public HasValue<String> getStart()
    {
        return (HasValue<String>) m_start;
    }

    //~ Inner Interfaces -------------------------------------------------------

    @SuppressWarnings(
        {
            "MarkerInterface",
            "InterfaceNeverImplemented",
            "PackageVisibleInnerClass"
        }
    )
    public interface Binder
        extends UiBinder<Widget, TODLimitView> { }
}
