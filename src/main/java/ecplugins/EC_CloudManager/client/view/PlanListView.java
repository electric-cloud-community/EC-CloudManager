
// PlanListView.java --
//
// PlanListView.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.view;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;

import com.google.inject.Inject;
import com.google.inject.Provider;

import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

import ecinternal.client.ui.Action;
import ecinternal.client.ui.ActionCell;
import ecinternal.client.ui.DefaultAction;
import ecinternal.client.ui.InternalUIFactory;
import ecinternal.client.ui.Templates;

import ecplugins.EC_CloudManager.client.CloudManagerPlaceManager;
import ecplugins.EC_CloudManager.client.Constants;
import ecplugins.EC_CloudManager.client.model.ManagerState;
import ecplugins.EC_CloudManager.client.model.PlanDetails;
import ecplugins.EC_CloudManager.client.model.PlanDetailsNameComparator;
import ecplugins.EC_CloudManager.client.presenter.PlanListPresenter;

import java.util.Arrays;

import static ecplugins.EC_CloudManager.client.CloudManagerPlaceManager.PLAN_NAME;
import static ecplugins.EC_CloudManager.client.NameTokens.*;

public class PlanListView
    extends ViewWithUiHandlers<PlanListUiHandlers>
    implements PlanListPresenter.MyView
{

    //~ Instance fields --------------------------------------------------------

    private final Widget                   m_widget;
    private final CloudManagerPlaceManager m_placeManager;
    private final Templates                m_templates;

    // UiBinder fields
    @UiField(provided = true)
    CellTable<PlanDetails> m_table;
    @UiField(provided = true)
    SimplePager            m_pager;
    @UiField Hyperlink     m_addLink;
    @UiField Anchor        m_refreshLink;
    @UiField Anchor        m_configure;

    //~ Constructors -----------------------------------------------------------

    @Inject
    @SuppressWarnings({"unchecked"})
    PlanListView(
            CloudManagerPlaceManager          placeManager,
            InternalUIFactory                 uiFactory,
            Constants                         constants,
            Provider<ActionCell<PlanDetails>> actionCellProvider,
            Templates                         templates,
            ManagerState                      state,
            Binder                            uiBinder)
    {
        m_placeManager = placeManager;
        m_templates    = templates;

        // Explicitly create the table and pager ui elements before
        // calling the UiBinder.
        m_table = uiFactory.createCellTable(true);
        m_pager = uiFactory.createPager();

        // Create the UI
        m_widget = uiBinder.createAndBindUi(this);

        // Set the url for the add plan link
        m_addLink.setTargetHistoryToken(m_placeManager.buildHistoryToken(
                new PlaceRequest(editPage)));

        // Attach the pager and the list data provider to the table
        m_pager.setDisplay(m_table);
        state.getPlans()
             .addDataDisplay(m_table);

        // Create the name column and make it sortable
        Column<PlanDetails, String>              nameColumn =
            new PlanDetailsNameColumn();
        ColumnSortEvent.ListHandler<PlanDetails> handler    =
            new ColumnSortEvent.ListHandler<PlanDetails>(state.getPlans()
                                                              .getList());

        handler.setComparator(nameColumn, new PlanDetailsNameComparator());
        nameColumn.setSortable(true);
        m_table.addColumnSortHandler(handler);
        m_table.getColumnSortList()
               .push(nameColumn);
        m_table.addColumn(nameColumn, constants.cloudName());

        // Description column
        m_table.addColumn(new PlanDetailsDescriptionColumn(
                uiFactory.createHtmlOrTextCell()), constants.description());

        // Actions column 
        Action<PlanDetails> editAction   = new EditPlanAction(constants.edit());
        Action<PlanDetails> copyAction = new CopyPlanAction(constants.copy());
        Action<PlanDetails> deleteAction = new DeletePlanAction(constants.delete());

        m_table.addColumn(new IdentityColumn<PlanDetails>(
                actionCellProvider.get()
                                  .setActions(Arrays.asList(editAction, copyAction, deleteAction))),
            constants.actions());
    }

    //~ Methods ----------------------------------------------------------------

    @Override public Widget asWidget()
    {
        return m_widget;
    }

    @Override public void showLoading(boolean loading)
    {

        if (loading) {
            m_table.setRowCount(0, false);
        }
        else {
            m_table.setRowCount(m_table.getRowCount(), true);
        }
    }

    @Override public void sortData()
    {
        ColumnSortEvent.fire(m_table, m_table.getColumnSortList());
    }

    @UiHandler("m_configure")
    void handleConfigure(ClickEvent event)
    {
        getUiHandlers().configure();
    }

    @UiHandler("m_refreshLink")
    void handleRefresh(ClickEvent event)
    {
        getUiHandlers().refreshList();
    }

    //~ Inner Interfaces -------------------------------------------------------

    public interface Binder
        extends UiBinder<Widget, PlanListView> { }

    //~ Inner Classes ----------------------------------------------------------

    static class PlanDetailsDescriptionColumn
        extends Column<PlanDetails, String>
    {

        //~ Constructors -------------------------------------------------------

        PlanDetailsDescriptionColumn(Cell<String> cell)
        {
            super(cell);
        }

        //~ Methods ------------------------------------------------------------

        @Override public String getValue(PlanDetails object)
        {
            return object.getDescription();
        }
    }

    @SuppressWarnings({"PackageVisibleInnerClass"})
    class CopyPlanAction
        extends DefaultAction<PlanDetails>
    {

        //~ Constructors -------------------------------------------------------

        CopyPlanAction(String label)
        {
            super(label);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        @SuppressWarnings({"RefusedBequest"})
        public boolean onAction(PlanDetails value)
        {
            getUiHandlers().showCopyDialog(value);
              
            return false;
        }
    }
    
    @SuppressWarnings({"PackageVisibleInnerClass"})
    class DeletePlanAction
        extends DefaultAction<PlanDetails>
    {

        //~ Constructors -------------------------------------------------------

        DeletePlanAction(String label)
        {
            super(label);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        @SuppressWarnings({"RefusedBequest"})
        public boolean onAction(PlanDetails value)
        {
            getUiHandlers().showDeleteDialog(value);

            return false;
        }
    }

    @SuppressWarnings({"PackageVisibleInnerClass"})
    class EditPlanAction
        extends DefaultAction<PlanDetails>
    {

        //~ Constructors -------------------------------------------------------

        EditPlanAction(String label)
        {
            super(label);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        @SuppressWarnings({"RefusedBequest"})
        public SafeUri generateUrl(PlanDetails value)
        {
            return m_placeManager.generatePlanUrl(value, editPage);
        }
    }

    @SuppressWarnings({"PackageVisibleInnerClass"})
    class PlanDetailsNameColumn
        extends Column<PlanDetails, String>
        implements FieldUpdater<PlanDetails, String>
    {

        //~ Constructors -------------------------------------------------------

        PlanDetailsNameColumn()
        {
            super(new ClickableTextCell());
            setFieldUpdater(this);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        @SuppressWarnings({"RefusedBequest"})
        public void render(
                Cell.Context    context,
                PlanDetails     object,
                SafeHtmlBuilder sb)
        {
            String planName = object.getName();

            sb.append(m_templates.hrefLink(
                    m_placeManager.generatePlanUrl(object, detailsPage),
                    planName));
        }

        @Override public void update(
                int         index,
                PlanDetails object,
                String      value)
        {
            m_placeManager.revealPlace(new PlaceRequest(detailsPage).with(
                    PLAN_NAME, value));
        }

        @Override public String getValue(PlanDetails object)
        {
            return object.getName();
        }
    }
}
