
// ConfigureView.java --
//
// ConfigureView.java is part of ElectricCommander.
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
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import ecplugins.EC_CloudManager.client.presenter.ConfigurePresenterWidget.MyView;

public class ConfigureView
    extends PopupViewWithUiHandlers<DialogUiHandlers>
    implements MyView
{

    //~ Instance fields --------------------------------------------------------

    Widget             m_widget;
    @UiField Button    m_save;
    @UiField Button    m_cancel;
    @UiField TextBox   m_interval;
    @UiField CheckBox  m_enabled;
    @UiField DeckPanel m_deckPanel;

    //~ Constructors -----------------------------------------------------------

    @Inject public ConfigureView(
            Binder   uiBinder,
            EventBus eventBus)
    {
        super(eventBus);
        m_widget = uiBinder.createAndBindUi(this);
    }

    //~ Methods ----------------------------------------------------------------

    @Override public Widget asWidget()
    {
        return m_widget;
    }

    @Override public void showLoading(boolean loading)
    {
        m_deckPanel.showWidget(loading
                ? 0
                : 1);
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

    @Override public HasValue<Boolean> getEnabled()
    {
        return m_enabled;
    }

    @Override public HasValue<String> getInterval()
    {
        return m_interval;
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
        extends UiBinder<Widget, ConfigureView> { }
}
