
// CopyCloudView.java --
//
// CopyCloudView.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.view;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Widget;

import com.google.inject.Inject;

import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import ecinternal.client.ui.ConfirmDialog;

import ecplugins.EC_CloudManager.client.Constants;
import ecplugins.EC_CloudManager.client.presenter.CopyPlanPresenterWidget;


public class CopyPlanView
    extends PopupViewWithUiHandlers<CopyPlanUiHandlers>
    implements CopyPlanPresenterWidget.MyView,
        ConfirmDialog.ConfirmDialogCallback
{

    //~ Instance fields --------------------------------------------------------

    private final ConfirmDialog m_widget;

    //~ Constructors -----------------------------------------------------------

    @Inject public CopyPlanView(EventBus eventBus, Constants constants)
    {
        super(eventBus);
        m_widget = new ConfirmDialog(constants.copyDialogTitle(), "", this);
        m_widget.hide();
    }

    //~ Methods ----------------------------------------------------------------

    @Override public Widget asWidget()
    {
        return m_widget;
    }

    @Override public void onCancel()
    {
        getUiHandlers().cancel();
    }

    @Override public boolean onOK()
    {
        m_widget.setEnabled(false);
          
        getUiHandlers().copyCloud();

        return false;
    }

    @Override public void show()
    {
        super.show();
        m_widget.setEnabled(true);
    }

    @Override public void setMessage(String text)
    {
        m_widget.setMessage(text);
    }
}
