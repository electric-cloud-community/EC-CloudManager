
// PlanListPresenterTest.java --
//
// PlanListPresenterTest.java is part of ElectricCommander.
//
// Copyright (c) 2005-2013 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.presenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

import com.google.web.bindery.event.shared.Event;

import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

import ecplugins.EC_CloudManager.client.MockPlanDetails;
import ecplugins.EC_CloudManager.client.events.CopyPlanEvent;
import ecplugins.EC_CloudManager.client.events.DeletePlanEvent;
import ecplugins.EC_CloudManager.client.jtest.CMTestBase;
import ecplugins.EC_CloudManager.client.model.ManagerStateImpl;
import ecplugins.EC_CloudManager.client.model.PlanDetails;
import ecplugins.EC_CloudManager.client.model.ProviderDetails;
import ecplugins.EC_CloudManager.client.service.CloudServiceAsync;

import ecinternal.client.events.ChangeTitleEvent;
import ecinternal.client.events.ModelChangedEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static com.googlecode.gwt.test.FinallyCommandTrigger.triggerCommands;

public class PlanListPresenterTest
    extends CMTestBase
{

    //~ Instance fields --------------------------------------------------------

    @Mock private HasData<PlanDetails>                 m_display;
    @Mock private PlanListPresenter.MyView             m_view;
    @Mock private PlanListPresenter.MyProxy            m_proxy;
    @Mock private CloudServiceAsync                    m_service;
    @Mock private ConfigurePresenterWidget             m_configure;
    @Mock private DeletePlanPresenterWidget            m_delete;
    @Mock private DeletePlanPresenterWidget.MyView     m_deleteView;
    @Mock private CopyPlanPresenterWidget              m_copy;
    @Mock private CopyPlanPresenterWidget.MyView       m_copyView;
    @Captor private ArgumentCaptor<List<PlanDetails>>  m_listCaptor;
    @Captor private ArgumentCaptor<Event<?>>           m_eventCaptor;
    @Captor private ArgumentCaptor<Event.Type<Object>> m_typeCaptor;
    private PlanListPresenter                          m_presenter;

    //~ Methods ----------------------------------------------------------------

    @SuppressWarnings("SuspiciousMethodCalls")
    @Test public void testBind()
        throws Exception
    {
        m_presenter.bind();
        verify(m_eventBus, times(3)).addHandler(m_typeCaptor.capture(),
            eq(m_presenter));
        verify(m_view).setUiHandlers(m_presenter);

        List<Event.Type<Object>> values = m_typeCaptor.getAllValues();

        assertTrue("delete plan registered",
            values.contains(DeletePlanEvent.getType()));
        assertTrue("copy plan registered",
            values.contains(CopyPlanEvent.getType()));
        assertTrue("model change registered",
            values.contains(ModelChangedEvent.getType()));
    }

    @Test public void testOnDeletePlan_deleteOne()
        throws Exception
    {

        // Setup
        initState(Arrays.<PlanDetails>asList(PLAN_1, PLAN_2), NO_PROVIDERS);

        // test
        m_presenter.onDeletePlan(new DeletePlanEvent(PLAN_1));
        triggerCommands();

        // Verify
        List<PlanDetails> list = m_state.getPlans()
                                        .getList();

        verify(m_display).setRowCount(1, true);
        verify(m_display).setRowData(eq(0), m_listCaptor.capture());
        assertViewTable(PLAN_2);
        assertEquals("one plan", 1, list.size());
        assertEquals("plan", PLAN_2, list.get(0));
    }

    @Test public void testOnDeletePlan_noPlan()
        throws Exception
    {

        // Setup
        initState(NO_PLANS, NO_PROVIDERS);

        // test
        m_presenter.onDeletePlan(new DeletePlanEvent(PLAN_1));
        triggerCommands();

        // Verify
        List<PlanDetails> list = m_state.getPlans()
                                        .getList();

        assertTrue("no items", list.isEmpty());
    }

    @Test public void testOnDeletePlan_notLoaded()
        throws Exception
    {
        // Setup

        // test
        m_presenter.onDeletePlan(new DeletePlanEvent(PLAN_1));
        triggerCommands();

        // Verify
        List<PlanDetails> list = m_state.getPlans()
                                        .getList();

        assertTrue("no items", list.isEmpty());
    }

    @Test public void testOnHide()
    {
        when(m_delete.getWidget()).thenReturn(null);
        m_presenter.onHide();
        verify(m_view, times(3)).removeFromSlot(null, null);
    }

    @Test public void testOnHide_withDialog()
    {
        when(m_delete.getView()).thenReturn(m_deleteView);
        m_presenter.showDeleteDialog(PLAN_1);
        m_presenter.onHide();
        verify(m_view, times(3)).removeFromSlot(null, null);
    }

    @Test public void testOnModelChanged()
    {
        initState(NO_PLANS, NO_PROVIDERS);
        m_presenter.onModelChanged(new ModelChangedEvent());
        verify(m_view).sortData();
    }

    @Test public void testOnReset_initial()
    {
        m_presenter.onReset();
        verify(m_view).showLoading(true);
        verify(m_service).refreshState();
    }

    @Test public void testOnReset_loaded()
    {
        initState(NO_PLANS, NO_PROVIDERS);
        m_presenter.onReset();
        verify(m_view, never()).showLoading(true);
        verify(m_service, never()).refreshState();
    }

    @Test public void testOnReveal()
    {

        // setup
        when(m_constants.cloudPlans()).thenReturn("pass");

        // test
        m_presenter.onReveal();

        // verify
        verify(m_eventBus).fireEventFromSource(m_eventCaptor.capture(),
            eq(m_presenter));

        ChangeTitleEvent event = (ChangeTitleEvent) m_eventCaptor.getValue();

        assertEquals("title", "pass", event.getTitle());
        assertNull("subtitle", event.getSubtitle());
    }

    @Test public void testRevealInParent()
    {
        m_presenter.revealInParent();
        verify(m_eventBus).fireEventFromSource(m_eventCaptor.capture(),
            eq(m_presenter));

        RevealContentEvent event = (RevealContentEvent)
            m_eventCaptor.getValue();

        assertEquals("slot", CloudManagerPresenter.TYPE_SetMainContent,
            event.getAssociatedType());
        assertEquals("content", m_presenter, event.getContent());
    }

    @Test public void testShowCopyDialog()
    {
        when(m_copy.getView()).thenReturn(m_copyView);
        m_presenter.showCopyDialog(PLAN_1);
        verify(m_copy).setCloud(PLAN_1);
    }

    @Test public void testShowDeleteDialog()
    {
        when(m_delete.getView()).thenReturn(m_deleteView);
        m_presenter.showDeleteDialog(PLAN_1);
        verify(m_delete).setCloud(PLAN_1);
    }

    private void assertViewTable(MockPlanDetails... expected)
    {
        Collection<MockPlanDetails> list = new ArrayList<MockPlanDetails>();

        list.addAll(Arrays.asList(expected));
        list.removeAll(m_listCaptor.getValue());
        assertTrue("table has same contents", list.isEmpty());
    }

    @SuppressWarnings({"unchecked"})
    private void initState(
            List<PlanDetails>     plans,
            List<ProviderDetails> providers)
    {
        when(m_display.getVisibleRange()).thenReturn(new Range(0, 10));
        m_state.setData(plans, providers);
        m_state.getPlans()
               .addDataDisplay(m_display);
        triggerCommands();
        reset(m_display);
        when(m_display.getVisibleRange()).thenReturn(new Range(0, 10));
    }

    @Before @Override public void setUp()
    {
        super.setUp();
        m_state     = new ManagerStateImpl();
        m_presenter = spy(new PlanListPresenter(m_eventBus, m_view, m_proxy,
                    m_service, m_delete, m_copy, m_configure, m_constants,
                    m_state));
    }
}
