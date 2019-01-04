
// PlanListViewTest.java --
//
// PlanListViewTest.java is part of ElectricCommander.
//
// Copyright (c) 2005-2013 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.view;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.SimplePager;

import com.google.inject.Provider;

import com.googlecode.gwt.test.utils.GwtReflectionUtils;
import com.googlecode.gwt.test.utils.events.Browser;

import com.gwtplatform.mvp.client.proxy.ParameterTokenFormatter;

import ecplugins.EC_CloudManager.client.CloudManagerPlaceManager;
import ecplugins.EC_CloudManager.client.jtest.CMTestBase;
import ecplugins.EC_CloudManager.client.model.PlanDetails;
import ecplugins.EC_CloudManager.client.presenter.PlanListPresenter;
import ecplugins.EC_CloudManager.client.view.PlanListView.Binder;
import ecplugins.EC_CloudManager.client.view.PlanListView.DeletePlanAction;
import ecplugins.EC_CloudManager.client.view.PlanListView.EditPlanAction;
import ecplugins.EC_CloudManager.client.view.PlanListView.PlanDetailsNameColumn;

import ecinternal.client.ui.ActionCell;
import ecinternal.client.ui.InternalUIFactory;
import ecinternal.client.ui.Templates;
import ecinternal.client.ui.impl.HtmlOrTextCell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static com.googlecode.gwt.test.FinallyCommandTrigger.triggerCommands;

public class PlanListViewTest
    extends CMTestBase
{

    //~ Instance fields --------------------------------------------------------

    private Templates              m_templates;
    private CellTable<PlanDetails> m_cellTable;

    // Mocked
    @Mock private InternalUIFactory  m_uiFactory;
    @Mock private PlanListPresenter  m_presenter;
    @Mock private HtmlOrTextCell     m_htmlCell;
    @Mock private PlanListUiHandlers m_uiHandlers;

    // Captors
    @Captor private ArgumentCaptor<ValueChangeEvent<String>> m_valueChangeCaptor;
    @Captor private ArgumentCaptor<GwtEvent<?>>              m_eventArgumentCaptor;
    private PlanListView                                     m_view;

    //~ Methods ----------------------------------------------------------------

    @Test public void testDeletePlanAction()
        throws Exception
    {
        DeletePlanAction action = GwtReflectionUtils.instantiateClass(
                DeletePlanAction.class.getDeclaredConstructor(
                    PlanListView.class, String.class), m_view, "deleteLabel");

        assertEquals("label", "deleteLabel", action.getLabel(PLAN_1));
        assertFalse("suppress event", action.onAction(PLAN_1));
        assertNull("url", action.generateUrl(PLAN_1));
        verify(m_uiHandlers).showDeleteDialog(PLAN_1);
    }

    @Test public void testEditPlanAction()
        throws Exception
    {
        EditPlanAction action = GwtReflectionUtils.instantiateClass(
                EditPlanAction.class.getDeclaredConstructor(PlanListView.class,
                    String.class), m_view, "editLabel");

        assertEquals("label", "editLabel", action.getLabel(PLAN_1));
        assertTrue("propagate event", action.onAction(PLAN_1));
        assertEquals("url", "#edit;planName=cloud1",
            action.generateUrl(PLAN_1)
                  .asString());
    }

    @Test public void testPlanDetailsNameColumn()
        throws Exception
    {
        PlanDetailsNameColumn column = GwtReflectionUtils.instantiateClass(
                PlanDetailsNameColumn.class.getDeclaredConstructor(
                    PlanListView.class), m_view);
        SafeHtmlBuilder       sb     = new SafeHtmlBuilder();

        column.render(null, PLAN_1, sb);
        assertEquals("html", "<a href=\"#details;planName=cloud1\">cloud1</a>",
            sb.toSafeHtml()
              .asString());
        assertEquals("value", "cloud1", column.getValue(PLAN_1));
    }

    @Test public void testRefresh()
    {
        Browser.click(m_view.m_refreshLink);
        verify(m_uiHandlers).refreshList();
    }

    @Test public void testShowLoading()
        throws Exception
    {

        // Show the indicator
        m_view.showLoading(true);
        triggerCommands();
        assertFalse("empty table message is hidden",
            cellTableEmptyMessage(m_cellTable).isVisible());
        assertTrue("loading indicator is visible",
            cellTableLoading(m_cellTable).isVisible());
    }

    @Test public void testSortData()
        throws Exception
    {

        // Sort on the first column, ascending
        ColumnSortList sortList = m_cellTable.getColumnSortList();

        sortList.clear();
        sortList.push(m_cellTable.getColumn(0));
        m_state.setData(Arrays.<PlanDetails>asList(PLAN_2, PLAN_1),
            NO_PROVIDERS);
        m_view.sortData();
        triggerCommands();

        List<PlanDetails> list = m_state.getPlans()
                                        .getList();

        assertEquals("first is 1", PLAN_1.getName(), list.get(0)
                                                         .getName());
        assertEquals("second is 2", PLAN_2.getName(), list.get(1)
                                                          .getName());

        // Reverse the order and verify the update happens
        sortList.push(m_cellTable.getColumn(0));
        m_view.sortData();
        triggerCommands();
        assertEquals("first is 2", PLAN_2.getName(), list.get(0)
                                                         .getName());
        assertEquals("second is 1", PLAN_1.getName(), list.get(1)
                                                          .getName());
    }

    @Override
    @SuppressWarnings({"RawUseOfParameterizedType", "unchecked"})
    public void setUp()
    {
        super.setUp();

        CloudManagerPlaceManager placeManager = spy(
                new CloudManagerPlaceManager(m_eventBus,
                    new ParameterTokenFormatter()));

        m_templates = GWT.create(Templates.class);
        m_cellTable = spy(GWT.<CellTable<PlanDetails>>create(CellTable.class));

        m_cellTable.setPageSize(20);

        SimplePager pager = GWT.create(SimplePager.class);

        when(m_uiFactory.createCellTable(true)).thenReturn((CellTable)
            m_cellTable);
        when(m_uiFactory.createPager()).thenReturn(pager);
        when(m_uiFactory.createHtmlOrTextCell()).thenReturn(m_htmlCell);

        Binder                            binder             = GWT.create(
                Binder.class);
        Provider<ActionCell<PlanDetails>> actionCellProvider =
            new Provider<ActionCell<PlanDetails>>() {
                @Override public ActionCell<PlanDetails> get()
                {
                    return new ActionCell<PlanDetails>(m_templates);
                }
            };
        m_view = new PlanListView(placeManager, m_uiFactory, m_constants,
                actionCellProvider, m_templates, m_state, binder);

        m_view.setUiHandlers(m_uiHandlers);
        triggerCommands();
    }
}
