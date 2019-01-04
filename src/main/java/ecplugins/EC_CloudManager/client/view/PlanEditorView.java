// PlanEditorView.java --
//
// PlanEditorView.java is part of ElectricCommander.
//
// Copyright (c) 2005-2012 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import ecinternal.client.ui.Action;
import org.jetbrains.annotations.NonNls;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

import com.google.inject.Inject;
import com.google.inject.Provider;

import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import ecplugins.EC_CloudManager.client.Constants;
import ecplugins.EC_CloudManager.client.model.TimeOfDayLimit;
import ecplugins.EC_CloudManager.client.presenter.PlanEditorPresenter;
import ecplugins.EC_CloudManager.client.ui.FilteredParameterPanelWidget;

import com.electriccloud.commander.client.CommanderRequestManager;
import com.electriccloud.commander.client.responses.CommanderError;
import com.electriccloud.commander.client.responses.CommanderErrorHandler;
import com.electriccloud.commander.gwt.client.ui.RenderableParameterPanel;
import com.electriccloud.commander.gwt.client.ui.ValuedListBox;
import com.electriccloud.commander.gwt.client.ui.impl.RadioButtonGroupImpl;

import ecinternal.client.LoggerLog;
import ecinternal.client.ui.ActionCell;
import ecinternal.client.ui.DefaultAction;
import ecinternal.client.ui.InternalUIFactory;
import ecinternal.client.ui.ParameterPanelWidget;
import ecinternal.client.ui.ProcedurePicker;
import ecinternal.client.ui.ProjectPicker;
import ecinternal.client.ui.RenderableParameterPanelWidget;
import ecinternal.client.ui.ResourcePoolPicker;
import ecinternal.client.ui.impl.ResourcePoolPickerImpl;

import static com.google.gwt.dom.client.Style.Unit.PCT;

import static ecplugins.EC_CloudManager.client.presenter.PlanEditorPresenter.HOURS_PER_DAY;

@SuppressWarnings({"PackageVisibleField"})
public class PlanEditorView
        extends ViewWithUiHandlers<PlanEditorUiHandlers>
        implements PlanEditorPresenter.MyView,
        CommanderErrorHandler {

    //~ Static fields/initializers ---------------------------------------------

    // ~ Static fields/initializers
    // ---------------------------------------------
    @NonNls
    private static final Logger log = Logger
            .getLogger("CloudEditorView");
    private static final char SAVE_ACCESS_KEY = 's';
    private static final char CANCEL_ACCESS_KEY = 'c';
    @NonNls
    private static final Set<String> EXCLUDED_PARAMETERS =
            new HashSet<String>(Arrays.asList("current", "poolName"));

    //~ Instance fields --------------------------------------------------------

    // ~ Instance fields
    // --------------------------------------------------------
    private final Constants m_constants;
    private final Widget m_widget;
    @UiField
    HasText m_description;
    @UiField(provided = true)
    ResourcePoolPickerImpl m_poolName;
    @UiField
    DeckPanel m_deckPanel;
    @UiField
    Button m_cancel;
    @UiField
    Button m_save;
    @UiField
    Button m_restartUsage;
    @UiField
    HasText m_name;
    @UiField
    HasText m_costMax;
    @UiField
    HasText m_costPeriod;
    @UiField
    HasText m_killLimitMax;
    @UiField
    HasText m_killLimitMin;
    @UiField(provided = true)
    RadioButtonGroupImpl m_policyGroup;
    @UiField
    SimplePanel m_queryParameterTable;
    @UiField
    SimplePanel m_queryProcedurePicker;
    @UiField
    SimplePanel m_queryProjectPicker;
    @UiField
    SimplePanel m_shrinkPanel;
    @UiField
    SimplePanel m_growPanel;
    @UiField
    SimplePanel m_providerPanel;
    @UiField(provided = true)
    CellTable<TimeOfDayLimit> m_todTable;
    @UiField
    CheckBox m_debug;
    @UiField
    CheckBox m_enabled;
    @UiField
    DeckPanel m_providerDeck;
    @UiField
    Anchor m_addLimit;
    private final ValuedListBox m_providerType;
    private final ProjectPicker m_queryProject;
    private final ProcedurePicker m_queryProcedure;
    private final ParameterPanelWidget m_parameterPanel;
    private final ListDataProvider<TimeOfDayLimit> m_timeOfDayLimits;
    private final RenderableParameterPanelWidget m_growParams;
    private final RenderableParameterPanelWidget m_shrinkParams;

    //~ Constructors -----------------------------------------------------------

    // ~ Constructors
    // -----------------------------------------------------------
    @Inject
    @SuppressWarnings({"OverlyLongMethod", "unchecked"})
    public PlanEditorView(
            CommanderRequestManager requestManager,
            InternalUIFactory uiFactory,
            Binder uiBinder,
            Constants constants,
            javax.inject.Provider<ActionCell<TimeOfDayLimit>> actionCellProvider,
            FilteredParameterPanelWidget filteredParameterPanelWidget) {
        m_constants = constants;
        m_timeOfDayLimits = new ListDataProvider<TimeOfDayLimit>();
        m_policyGroup = (RadioButtonGroupImpl)
                uiFactory.createRadioButtonGroup("policy");
        m_todTable = uiFactory.createCellTable(false);
        m_providerType = uiFactory.createValuedListBox();
        m_queryProject = uiFactory.createProjectPicker();
        m_queryProcedure = uiFactory.createProcedurePicker();
        m_poolName = (ResourcePoolPickerImpl)
                uiFactory.createResourcePoolPicker();
        m_parameterPanel = filteredParameterPanelWidget;

        filteredParameterPanelWidget.setExcludedParameters(EXCLUDED_PARAMETERS);
        m_growParams = uiFactory.createParameterPanel(requestManager,
                new LoggerLog(log), this, true, false, "growParams");
        m_shrinkParams = uiFactory.createParameterPanel(requestManager,
                new LoggerLog(log), this, true, false, "shrinkParams");

        // Create the uiBinder form
        m_widget = uiBinder.createAndBindUi(this);
        m_queryProcedure.setProjectBox(m_queryProject);
        m_queryProjectPicker.setWidget(m_queryProject);
        m_queryProcedurePicker.setWidget(m_queryProcedure);
        m_queryParameterTable.setWidget(m_parameterPanel.asWidget());
        m_providerPanel.setWidget(m_providerType.asWidget());
        m_growPanel.setWidget(m_growParams.asWidget());
        m_shrinkPanel.setWidget(m_shrinkParams);

        // Set access keys
        m_save.setAccessKey(SAVE_ACCESS_KEY);
        m_cancel.setAccessKey(CANCEL_ACCESS_KEY);

        // Set up time of day table
        m_todTable.setKeyboardSelectionPolicy(
                HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.ENABLED);

        Column hourCol = new TextColumn<TimeOfDayLimit>() {
            @Override
            public String getValue(TimeOfDayLimit object) {
                return m_constants.hourRange(object.getStart(),
                        (object.getEnd() - 1 + HOURS_PER_DAY) % HOURS_PER_DAY);
            }
        };
        m_todTable.addColumn(hourCol, "Hour");

        Column<TimeOfDayLimit, String> minCol = new Column<TimeOfDayLimit,
                String>(new ECEditTextCell("4")) {
            @Override
            public String getValue(TimeOfDayLimit object) {
                return object.getMin();
            }
        };
        minCol.setFieldUpdater(new FieldUpdater<TimeOfDayLimit, String>() {
            @Override
            public void update(
                    int index,
                    TimeOfDayLimit limit,
                    String value) {
                limit.setMin(value);
                getUiHandlers().changeLimit(limit);
            }
        });
        m_todTable.addColumn(minCol, "Min");

        Column<TimeOfDayLimit, String> maxCol = new Column<TimeOfDayLimit,
                String>(new ECEditTextCell("4")) {
            @Override
            public String getValue(TimeOfDayLimit object) {
                return object.getMax();
            }
        };
        maxCol.setFieldUpdater(new FieldUpdater<TimeOfDayLimit, String>() {
            @Override
            public void update(
                    int index,
                    TimeOfDayLimit limit,
                    String value) {
                limit.setMax(value);
                getUiHandlers().changeLimit(limit);
            }
        });
        m_todTable.addColumn(maxCol, "Max");

        DefaultAction<TimeOfDayLimit> defaultAction = new DefaultAction<TimeOfDayLimit>(
                "Delete") {
            @Override
            public boolean onAction(
                    TimeOfDayLimit value) {
                getUiHandlers()
                        .deleteLimit(
                                value);
                m_todTable
                        .redraw();

                return
                        true;
            }
        };
        List<ecinternal.client.ui.Action<TimeOfDayLimit>> defaultActions = new ArrayList<Action<TimeOfDayLimit>>();
        defaultActions.add(
                defaultAction);
        Column actionCol =
                new IdentityColumn<TimeOfDayLimit>(actionCellProvider.get()
                        .setActions(defaultActions));

        m_todTable.addColumn(actionCol, "Actions");
        m_todTable.setWidth("100%", true);
        m_todTable.setColumnWidth(hourCol, 30.0, PCT);
        m_todTable.setColumnWidth(minCol, 20.0, PCT);
        m_todTable.setColumnWidth(maxCol, 20.0, PCT);
        m_todTable.setColumnWidth(actionCol, 30.0, PCT);
        m_timeOfDayLimits.addDataDisplay(m_todTable);

        // Set up the policy radio button group
        m_policyGroup.setDefaultValue("never");
        m_policyGroup.addRadio(((Constants) GWT.create(Constants.class))
                .neverPolicy(), "never");
        m_policyGroup.addRadio(((Constants) GWT.create(Constants.class))
                .tryPolicy(), "try");
        m_policyGroup.addRadio(((Constants) GWT.create(Constants.class))
                .alwaysPolicy(), "always");
    }

    //~ Methods ----------------------------------------------------------------

    // ~ Methods
    // ----------------------------------------------------------------
    @Override
    public Widget asWidget() {
        return m_widget;
    }

    @Override
    public void handleError(CommanderError error) {
    }

    @Override
    public void showLoading(boolean showLoading) {
        m_deckPanel.showWidget(showLoading
                ? 0
                : 1);
    }

    @Override
    public void showProviderPanel(boolean showProvider) {
        m_providerDeck.showWidget(showProvider
                ? 1
                : 0);
    }

    @UiHandler("m_addLimit")
    void handleAddLimit(ClickEvent event) {
        getUiHandlers().handleAddLimit();
    }

    @Override
    public HasClickHandlers getCancel() {
        return m_cancel;
    }

    @Override
    public HasText getCostMax() {
        return m_costMax;
    }

    @Override
    public HasText getCostPeriod() {
        return m_costPeriod;
    }

    @Override
    public HasValue<Boolean> getDebug() {
        return m_debug;
    }

    @Override
    public HasText getDescription() {
        return m_description;
    }

    @Override
    public HasValue<Boolean> getEnabled() {
        return m_enabled;
    }

    @Override
    public RenderableParameterPanel getGrowParams() {
        return m_growParams;
    }

    @Override
    public HasText getKillLimitMax() {
        return m_killLimitMax;
    }

    @Override
    public HasText getKillLimitMin() {
        return m_killLimitMin;
    }

    @Override
    public HasValue<String> getKillPolicy() {
        return m_policyGroup;
    }

    @Override
    public HasText getName() {
        return m_name;
    }

    @Override
    public ResourcePoolPicker getPoolName() {
        return m_poolName;
    }

    @Override
    public ValuedListBox getProviderType() {
        return m_providerType;
    }

    @Override
    public ParameterPanelWidget getQueryParameters() {
        return m_parameterPanel;
    }

    @Override
    public HasValue<String> getQueryProcedure() {
        return m_queryProcedure;
    }

    @Override
    public HasValue<String> getQueryProject() {
        return m_queryProject;
    }

    @Override
    public HasClickHandlers getRestartUsage() {
        return m_restartUsage;
    }

    @Override
    public HasClickHandlers getSave() {
        return m_save;
    }

    @Override
    public RenderableParameterPanel getShrinkParams() {
        return m_shrinkParams;
    }

    @Override
    public ListDataProvider<TimeOfDayLimit> getTimeOfDayLimits() {
        return m_timeOfDayLimits;
    }

    //~ Inner Interfaces -------------------------------------------------------

    // ~ Inner Interfaces
    // -------------------------------------------------------
    public interface Binder
            extends UiBinder<Widget, PlanEditorView> {
    }
}
