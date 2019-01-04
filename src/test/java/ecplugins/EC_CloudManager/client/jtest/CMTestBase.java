
// CMTestBase.java --
//
// CMTestBase.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.jtest;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NonNls;

import org.junit.After;
import org.junit.Before;

import org.mockito.Mock;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.junit.GWTMockUtilities;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.UIObject;

import com.googlecode.gwt.test.GwtTestWithMockito;
import com.googlecode.gwt.test.utils.GwtReflectionUtils;

import ecplugins.EC_CloudManager.client.Constants;
import ecplugins.EC_CloudManager.client.MockPlanDetails;
import ecplugins.EC_CloudManager.client.model.ManagerStateImpl;
import ecplugins.EC_CloudManager.client.model.PlanDetails;
import ecplugins.EC_CloudManager.client.model.ProviderDetails;


import org.junit.Test;
import com.googlecode.gwt.test.uibinder.UiBinderCreateHandler;

public abstract class CMTestBase
    extends GwtTestWithMockito
{

    //~ Static fields/initializers ---------------------------------------------

    @NonNls protected static final MockPlanDetails PLAN_1                            =
        new MockPlanDetails("cloud1", "descr");
    @NonNls protected static final MockPlanDetails PLAN_2                            =
        new MockPlanDetails("cloud2", "descr");
    protected static final List<PlanDetails>       NO_PLANS                          =
        Collections.emptyList();
    protected static final List<ProviderDetails>   NO_PROVIDERS                      =
        Collections.emptyList();
    @NonNls private static final String            CELLTABLE_LOADING_INDICATOR_FIELD =
        "loadingIndicatorContainer";
    @NonNls private static final String            CELLTABLE_EMPTY_MESSAGE_FIELD     =
        "emptyTableWidgetContainer";

    //~ Instance fields --------------------------------------------------------

    @Mock protected EventBus   m_eventBus;
    @Mock protected Constants m_constants;
    protected ManagerStateImpl m_state;

    //~ Methods ----------------------------------------------------------------

    @After public void tearDown()
    {
        GWTMockUtilities.restore();
    }

    protected static UIObject cellTableEmptyMessage(
            CellTable<PlanDetails> cellTable)
    {
        return (UIObject) GwtReflectionUtils.getPrivateFieldValue(cellTable,
            CELLTABLE_EMPTY_MESSAGE_FIELD);
    }

    protected static UIObject cellTableLoading(
            CellTable<PlanDetails> cellTable)
    {
        return (UIObject) GwtReflectionUtils.getPrivateFieldValue(cellTable,
            CELLTABLE_LOADING_INDICATOR_FIELD);
    }

    @Override public String getModuleName()
    {
        return "ecinternal.ECInternal";
    }

    @Before public void setUp()
    {
        addGwtCreateHandler(UiBinderCreateHandler.get());
        GWTMockUtilities.disarm();
        m_state = new ManagerStateImpl();
    }
}
