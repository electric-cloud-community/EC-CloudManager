
// RootComponentPresenter.java --
//
// RootComponentPresenter.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.presenter;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import com.google.inject.Inject;

import com.gwtplatform.mvp.client.RootPresenter;

import ecinternal.client.CommanderInitEvent;
import ecinternal.client.CommanderInitEventHandler;

import com.electriccloud.commander.gwt.client.ComponentContext;

public class RootComponentPresenter
    extends RootPresenter
    implements CommanderInitEventHandler
{

    //~ Static fields/initializers ---------------------------------------------

    static String s_divId;

    //~ Constructors -----------------------------------------------------------

    @Inject public RootComponentPresenter(
            EventBus          eventBus,
            RootComponentView view)
    {
        super(eventBus, view);
    }

    //~ Methods ----------------------------------------------------------------

    @Override public void onCommanderInit(ComponentContext context)
    {
        s_divId = context.getUniqueId();
    }

    @Override protected void onBind()
    {
        super.onBind();
        addRegisteredHandler(CommanderInitEvent.TYPE, this);
    }

    //~ Inner Classes ----------------------------------------------------------

    public static class RootComponentView
        extends RootView
    {

        //~ Methods ------------------------------------------------------------

        @Override public void setInSlot(
                Object slot,
                Widget content)
        {
            RootPanel.get(s_divId)
                     .add(content);
        }
    }
}
