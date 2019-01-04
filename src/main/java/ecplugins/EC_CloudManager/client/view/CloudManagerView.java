
// CloudManagerView.java --
//
// CloudManagerView.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.view;

import com.electriccloud.commander.gwt.client.util.CommanderUrlBuilder;
import com.google.gwt.logging.client.HasWidgetsLogHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;
import ecinternal.client.ui.ShortcutStar;
import ecplugins.EC_CloudManager.client.presenter.CloudManagerPresenter;

import javax.inject.Inject;
import java.util.logging.Logger;

public class CloudManagerView
    extends ViewImpl
    implements CloudManagerPresenter.MyView
{

    //~ Instance fields --------------------------------------------------------

    public final Widget    m_widget;
    @UiField HasWidgets    m_mainContentPanel;
    @UiField FlowPanel     m_breadcrumbsPanel;
    @UiField FlowPanel     m_actionsList;
    @UiField ShortcutStar  m_shortcuts;
    @UiField Label         m_title;
    @UiField Label         m_subtitle;
    @UiField Label         m_separator;
    @UiField HasWidgets    m_iteratorPanel;
    @UiField VerticalPanel m_logPanel;

    //~ Constructors -----------------------------------------------------------

    @Inject public CloudManagerView(Binder uiBinder)
    {
        m_widget = uiBinder.createAndBindUi(this);

        Logger rootLogger = Logger.getLogger("");

        rootLogger.addHandler(new HasWidgetsLogHandler(m_logPanel));
    }

    //~ Methods ----------------------------------------------------------------

    @Override public Widget asWidget()
    {
        return m_widget;
    }

    @Override public void updateShortcut(String name)
    {
        m_shortcuts.setName(name);
        m_shortcuts.setUrl(CommanderUrlBuilder.createRedirectUrl()
                                              .buildString());
    }

    @Override public Label getSubtitle()
    {
        return m_subtitle;
    }

    @Override public Label getTitle()
    {
        return m_title;
    }

    private static void setContent(
            HasWidgets panel,
            Widget     content)
    {
        panel.clear();

        if (content != null) {
            panel.add(content);
        }
    }

    @Override public void setInSlot(
            Object slot,
            Widget content)
    {

        if (slot == CloudManagerPresenter.TYPE_SetMainContent) {
            setContent(m_mainContentPanel, content);
            m_logPanel.clear();
        }
        else if (slot == CloudManagerPresenter.TYPE_SetIterator) {
            setContent(m_iteratorPanel, content);
        }
        else {
            super.setInSlot(slot, content);
        }
    }

    @Override public void setSubtitleVisible(boolean visible)
    {
        m_subtitle.setVisible(visible);
        m_separator.setVisible(visible);
    }

    @Override public void setWindowTitle(String title)
    {
        Window.setTitle(title);
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
        extends UiBinder<Widget, CloudManagerView>
    {
    }
}
