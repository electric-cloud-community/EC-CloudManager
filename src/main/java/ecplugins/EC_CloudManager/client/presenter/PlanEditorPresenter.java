
// PlanEditorPresenter.java --
//
// PlanEditorPresenter.java is part of ElectricCommander.
//
// Copyright (c) 2005-2012 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.presenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.view.client.ListDataProvider;

import com.google.inject.Inject;

import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

import ecplugins.EC_CloudManager.client.Constants;
import ecplugins.EC_CloudManager.client.events.AddLimitEvent;
import ecplugins.EC_CloudManager.client.events.AddLimitEvent.AddLimitHandler;
import ecplugins.EC_CloudManager.client.model.ManagerState;
import ecplugins.EC_CloudManager.client.model.Parameter;
import ecplugins.EC_CloudManager.client.model.PlanDetails;
import ecplugins.EC_CloudManager.client.model.ProviderDetails;
import ecplugins.EC_CloudManager.client.model.TimeOfDayLimit;
import ecplugins.EC_CloudManager.client.service.CloudServiceAsync;
import ecplugins.EC_CloudManager.client.view.PlanEditorUiHandlers;

import com.electriccloud.commander.client.domain.ActualParameter;
import com.electriccloud.commander.client.domain.FormalParameter;
import com.electriccloud.commander.client.responses.CommanderError;
import com.electriccloud.commander.client.responses.CommanderErrorHandler;
import com.electriccloud.commander.client.util.StringUtil;
import com.electriccloud.commander.gwt.client.SimpleActualParameter;
import com.electriccloud.commander.gwt.client.SimpleFormalParameter;
import com.electriccloud.commander.gwt.client.ui.RenderableParameterPanel;
import com.electriccloud.commander.gwt.client.ui.ValuedListBox;

import ecinternal.client.LoggerLog;
import ecinternal.client.events.ChangeTitleEvent;
import ecinternal.client.events.ModelChangedEvent;
import ecinternal.client.events.ModelChangedEvent.ModelChangedHandler;
import ecinternal.client.ui.ParameterPanelWidget;
import ecinternal.client.ui.ResourcePoolPicker;

import static java.util.logging.Level.SEVERE;

import static ecplugins.EC_CloudManager.client.CloudManagerPlaceManager.PLAN_NAME;
import static ecplugins.EC_CloudManager.client.NameTokens.editPage;

public class PlanEditorPresenter
    extends Presenter<PlanEditorPresenter.MyView, PlanEditorPresenter.MyProxy>
    implements AddLimitHandler,
        ClickHandler,
        CommanderErrorHandler,
        PlanEditorUiHandlers,
        ValueChangeHandler<String>,
        ModelChangedHandler
{

    //~ Static fields/initializers ---------------------------------------------

    // ~ Static fields/initializers
    // ---------------------------------------------
    private static final Logger log = Logger.getLogger("PlanEditorPresenter");

    // Constants
    public static final int                      HOURS_PER_DAY  = 24;
    @NonNls @NotNull private static final String DEFAULT_POLICY = "never";

    //~ Instance fields --------------------------------------------------------

    // ~ Instance fields
    // --------------------------------------------------------
    // Injected by Gin.
    private final Constants               m_constants;
    private final ManagerState            m_state;
    private final PlaceManager            m_placeManager;
    private final CloudServiceAsync       m_service;
    private final TODLimitPresenterWidget m_limitDialog;

    /** The name of the plan that is being edited. */
    @Nullable private String m_planName;

    /** The details of the plan being edited. */
    @Nullable private PlanDetails m_plan;

    //~ Constructors -----------------------------------------------------------

    // ~ Constructors
    // -----------------------------------------------------------
    @Inject public PlanEditorPresenter(
            com.google.web.bindery.event.shared.EventBus eventBus,
            MyView                  view,
            MyProxy                 proxy,
            PlaceManager            placeManager,
            TODLimitPresenterWidget limitDialog,
            Constants               constants,
            CloudServiceAsync       service,
            ManagerState            state)
    {
        super(eventBus, view, proxy);
        m_constants    = constants;
        m_service      = service;
        m_placeManager = placeManager;
        m_state        = state;
        m_limitDialog  = limitDialog;
    }

    //~ Methods ----------------------------------------------------------------

    // ~ Methods
    // ----------------------------------------------------------------
    @Override public void changeLimit(TimeOfDayLimit value)
    {
        int end   = value.getEnd();
        int start = value.getStart();
        int min   = Integer.parseInt(safeLimit(value.getMin()));
        int max   = Integer.parseInt(safeLimit(value.getMax()));

        if (min <= max) {

            // in case there is only one record in the grid
            if (end == 0 && start == 0) {

                assert m_plan != null;
                for (int i = 0; i < 24; i++) {
                    m_plan.setTimeOfDayLimit(i, safeLimit(value.getMin()),
                        safeLimit(value.getMax()));
                }
            }

            // in case there are several records in the grid
            assert m_plan != null;
            for (int i = value.getStart(); i != end;
                    i = (i + 1) % HOURS_PER_DAY) {
                m_plan.setTimeOfDayLimit(i, safeLimit(value.getMin()),
                    safeLimit(value.getMax()));
            }
        }
        else {
            Window.alert("The minimum value(" + min
                    + ") cannot exceed the maximum value(" + max + ").");
        }

        buildLimitsTable();
    }

    @Override public void deleteLimit(TimeOfDayLimit value)
    {
        String newMax   = "0";
        String newMin   = "0";
        int    curStart = value.getStart();
        int    curEnd   = value.getEnd();

        // if this is not the first entry, set
        // limit of our time range to that of previous
        if (curStart != 0) {
            assert m_plan != null;
            newMax = m_plan.getTimeOfDayMax(curStart - 1);
            newMin = m_plan.getTimeOfDayMin(curStart - 1);
            // if it is the first entry but there are more, copy from the follow
            // on entry
        }
        else if (curEnd != 24) {
            assert m_plan != null;
            newMax = m_plan.getTimeOfDayMax(curEnd + 1);
            newMin = m_plan.getTimeOfDayMin(curEnd + 1);
        }

        value.setMin(newMin);
        value.setMax(newMax);
        changeLimit(value);
    }

    @Override public void handleAddLimit()
    {
        addToPopupSlot(m_limitDialog);
    }

    @Override public void handleError(CommanderError error)
    {

        if (log.isLoggable(SEVERE)) {
            log.severe(LoggerLog.formatCommanderError(error));
        }
    }

    @Override public void onAddLimit(AddLimitEvent event)
    {
        changeLimit(event.getLimit());
    }

    @Override public void onClick(@NotNull ClickEvent event)
    {
        Object source = event.getSource();
        MyView view   = getView();

        if (source.equals(view.getCancel())) {
            m_placeManager.navigateBack();
        }
        else if (source.equals(view.getSave())) {
            save();
        }
        else if (source.equals(view.getRestartUsage())) {
            restartUsage();
        }
    }

    @Override public void onModelChanged(ModelChangedEvent event)
    {
        updateForm();
    }

    @Override public void onValueChange(
            @NotNull ValueChangeEvent<String> event)
    {
        Object source = event.getSource();

        if (source.equals(getView().getProviderType())) {
            onProviderTypeChange(event.getValue());
        }
        else {
            onQueryProcedureChange();
        }
    }

    /**
     * Extract the parameters from the request. If 'planName' is present then we
     * are trying to edit an existing cloud. If the argument is not present,
     * then we are adding a new cloud.
     *
     * @param  request  the request with parameters
     */
    @Override public void prepareFromRequest(@NotNull PlaceRequest request)
    {
        super.prepareFromRequest(request);
        m_planName = request.getParameter(PLAN_NAME, null);
    }

    @Override protected void onBind()
    {
        super.onBind();

        MyView view = getView();

        view.getCancel()
            .addClickHandler(this);
        view.getSave()
            .addClickHandler(this);
        view.getRestartUsage()
            .addClickHandler(this);
        view.getQueryProcedure()
            .addValueChangeHandler(this);
        view.getProviderType()
            .addValueChangeHandler(this);
        view.setUiHandlers(this);
        addRegisteredHandler(AddLimitEvent.getType(), this);
        addRegisteredHandler(ModelChangedEvent.getType(), this);
    }

    @Override protected void onReset()
    {
        super.onReset();
        getView().getPoolName()
                 .refreshOptions();

        if (m_state.isLoaded()) {
            updateForm();
        }
        else {
            getView().showLoading(true);
            m_service.refreshState();
        }
    }

    @Override protected void onReveal()
    {
        super.onReveal();
        updateTitle();
    }

    @Override protected void revealInParent()
    {
        RevealContentEvent.fire(this, CloudManagerPresenter.TYPE_SetMainContent,
            this);
    }

    private void buildLimitsTable()
    {
        Collection<TimeOfDayLimit> limits = getView().getTimeOfDayLimits()
                                                     .getList();

        limits.clear();
        assert m_plan != null;
        String oldMin = safeLimit(m_plan.getTimeOfDayMin(0));
        String oldMax = safeLimit(m_plan.getTimeOfDayMax(0));
        int    start  = 0;

        for (int i = 1; i < HOURS_PER_DAY; ++i) {
            @NonNls String min = safeLimit(m_plan.getTimeOfDayMin(i));
            @NonNls String max = safeLimit(m_plan.getTimeOfDayMax(i));

            if (!min.equals(oldMin) || !max.equals(oldMax)) {
                limits.add(new TimeOfDayLimit(start, i, oldMin, oldMax));
                start = i;
            }

            oldMin = min;
            oldMax = max;
        }

        limits.add(new TimeOfDayLimit(start, 0, oldMin, oldMax));
    }

    @Nullable private PlanDetails findPlan()
    {
        PlanDetails details = null; // Attempt to find the requested plan by
                                    // name in the list of plans.

        // If there is no name or the plan is not found, create a new one.
        if (!StringUtil.isEmpty(m_planName)) {
            details = m_state.findPlanByName(m_planName);
        }

        if (details == null) {
            details = m_service.newPlan();

            // Set some reasonable defaults
            details.setName(m_planName);

            List<ProviderDetails> providers = m_state.getProviders();

            if (providers.size() == 1) {
                details.setAdjustPlugin(providers.get(0)
                                                 .getName());
            }

            // Reset the given name so we know to create rather than update.
            m_planName = null;
        }

        return details;
    }

    private void onProviderTypeChange(String value)
    {
        ProviderDetails       provider      = m_state.findProviderByName(value);
        List<FormalParameter> growFormals   = null;
        List<FormalParameter> shrinkFormals = null;

        if (provider != null) {
            growFormals   = makeFormals(provider.getGrowParameters());
            shrinkFormals = makeFormals(provider.getShrinkParameters());
        }

        MyView                   view       = getView();
        RenderableParameterPanel growParams = view.getGrowParams();

        growParams.setFormalParameters(growFormals);
        growParams.render();

        RenderableParameterPanel shrinkParams = view.getShrinkParams();

        shrinkParams.setFormalParameters(shrinkFormals);
        shrinkParams.render();
    }

    private void onQueryProcedureChange()
    {

        // Procedure changed
        MyView view      = getView();
        String project   = view.getQueryProject()
                               .getValue();
        String procedure = view.getQueryProcedure()
                               .getValue();

        if (project.isEmpty() || procedure.isEmpty()) {
            // Hide the parameter panel
        }
        else {
            view.getQueryParameters()
                .setContext(project, procedure);
        }
    }

    private void restartUsage()
    {
        m_service.restartUsage(m_planName, new AsyncCallback<Object>() {
                @Override public void onFailure(@NotNull Throwable caught)
                {
                    log.severe(
                        "Unable to restart usage counters: "
                            + caught.getMessage());
                }

                @Override public void onSuccess(Object result)
                {
                    // refresh counters
                }
            });
    }

    private static String safeLimit(String s)
    {

        if (s == null) {
            return "0";
        }

        if (s.isEmpty()) {
            return "0";
        }
        //noinspection DynamicRegexReplaceableByCompiledPattern
        if (s.matches("[0-9]*")) {
            return s;
        }

        return "0";
    }

    private void save()
    {
        boolean create = StringUtil.isEmpty(m_planName);

        if (create) {
            m_plan = m_service.newPlan();
        }

        savePlan();
        getView().showLoading(true);
        assert m_plan != null;

        final String          oldPlanName = StringUtil.nullToEmpty(create
                    ? m_plan.getName()
                    : m_planName);
        AsyncCallback<Object> callback    = new AsyncCallback<Object>() {
            @Override public void onFailure(Throwable caught) { }

            @Override public void onSuccess(Object result)
            {
                updateModel(oldPlanName);
                m_placeManager.navigateBack();
            }
        };

        if (create) {
            m_service.createPlan(m_plan, callback);
        }
        else {
            m_service.modifyPlan(oldPlanName, m_plan, callback);
        }
    }

    private void savePlan()
    {
        MyView view = getView();

        m_plan.setActive(view.getEnabled()
                             .getValue()
                ? "1"
                : "0");
        m_plan.setDebug(view.getDebug()
                            .getValue()
                ? "4"
                : "0");
        m_plan.setName(view.getName()
                           .getText());
        m_plan.setDescription(view.getDescription()
                                  .getText());
        m_plan.setPoolName(view.getPoolName()
                               .getValue());
        m_plan.setCostMax(view.getCostMax()
                              .getText());

        try {
            Integer periodHours = Math.round(Float.valueOf(
                        view.getCostPeriod()
                            .getText()) * HOURS_PER_DAY);

            m_plan.setCostPeriod(Integer.toString(periodHours));
        }
        catch (NumberFormatException e) {

            // bad numeric value, report error and bail out
            m_plan.setCostPeriod("");
        }

        m_plan.setKillLimitMin(view.getKillLimitMin()
                                   .getText());
        m_plan.setKillLimitMax(view.getKillLimitMax()
                                   .getText());
        m_plan.setKillLimitPolicy(view.getKillPolicy()
                                      .getValue());
        m_plan.setQueryProject(view.getQueryProject()
                                   .getValue());
        m_plan.setQueryProcedure(view.getQueryProcedure()
                                     .getValue());
        m_plan.setQueryConfig(view.getQueryParameters()
                                  .getValues());

        // Provider configuration
        m_plan.setAdjustPlugin(view.getProviderType()
                                   .getValue());
        m_plan.setGrowConfig(view.getGrowParams()
                                 .getValues());
        m_plan.setShrinkConfig(view.getShrinkParams()
                                   .getValues());

        // Time of Day limits
        List<TimeOfDayLimit> limits = view.getTimeOfDayLimits()
                                          .getList();

        for (TimeOfDayLimit limit : limits) {
            int start = limit.getStart();
            int end   = limit.getEnd();
            int min   = Integer.parseInt(safeLimit(limit.getMin()));
            int max   = Integer.parseInt(safeLimit(limit.getMax()));

            if (min <= max) {

                if (start == 0 && end == 0) {

                    for (int i = 0; i < 24; i++) {
                        m_plan.setTimeOfDayLimit(i, safeLimit(limit.getMin()),
                            safeLimit(limit.getMax()));
                    }
                }

                for (int i = start; i < end; ++i) {
                    m_plan.setTimeOfDayLimit(i, limit.getMin(), limit.getMax());
                }
            }
            else {
                Window.alert("The minimum value(" + min
                        + ") cannot exceed the maximum value(" + max + ").");
            }
        }
    }

    private void updateForm()
    {
        MyView view = getView();

        m_plan = findPlan();

        view.getDebug()
            .setValue(asBoolean(m_plan.getDebug()));
        view.getEnabled()
            .setValue(asBoolean(m_plan.getActive()));
        view.getDescription()
            .setText(m_plan.getDescription());
        view.getPoolName()
            .setValue(m_plan.getPoolName());
        view.getName()
            .setText(m_plan.getName());
        view.getCostMax()
            .setText(m_plan.getCostMax());

        ValuedListBox providerType = view.getProviderType();

        providerType.clear();

        List<ProviderDetails> providers = m_state.getProviders();

        if (providers.isEmpty()) {
            view.showProviderPanel(false);
        }
        else {
            view.showProviderPanel(true);

            for (ProviderDetails provider : providers) {
                providerType.addItem(provider.getName());
            }

            String pluginName = StringUtil.nullToEmpty(
                    m_plan.getAdjustPlugin());

            view.getGrowParams()
                .setActualParameters(makeActuals(m_plan.getGrowConfig()));
            view.getShrinkParams()
                .setActualParameters(makeActuals(m_plan.getShrinkConfig()));
            providerType.setValue(pluginName, true);
            view.getGrowParams()
                .render();
            view.getShrinkParams()
                .render();
        }

        String period = m_plan.getCostPeriod();

        if (!StringUtil.isEmpty(period)) {
            Integer costPeriodDays = Math.round(Float.valueOf(
                        m_plan.getCostPeriod()) / HOURS_PER_DAY);

            // noinspection CallToNumericToString
            period = costPeriodDays.toString();
        }

        view.getCostPeriod()
            .setText(period);

        String policy = StringUtil.nullToEmpty(m_plan.getKillLimitPolicy());

        if (policy.isEmpty()) {
            policy = DEFAULT_POLICY;
        }

        view.getKillPolicy()
            .setValue(policy);
        view.getKillLimitMin()
            .setText(m_plan.getKillLimitMin());
        view.getKillLimitMax()
            .setText(m_plan.getKillLimitMax());

        String queryProject   = StringUtil.nullToEmpty(
                m_plan.getQueryProject());
        String queryProcedure = StringUtil.nullToEmpty(
                m_plan.getQueryProcedure());

        view.getQueryProject()
            .setValue(queryProject);
        view.getQueryProcedure()
            .setValue(queryProcedure);
        view.getQueryParameters()
            .setInitialContext(queryProject, queryProcedure,
                m_plan.getQueryConfig());

        // Time of day limits
        buildLimitsTable();
        updateTitle();
        view.showLoading(false);
    }

    @SuppressWarnings({"MethodOnlyUsedFromInnerClass"})
    private void updateModel(@NotNull String oldPlanName)
    {
        m_state.changePlan(oldPlanName, m_plan);
    }

    private void updateTitle()
    {
        @Nullable String title;
        @Nullable String subtitle;

        if (StringUtil.isEmpty(m_planName)) {
            title    = m_constants.createCloud();
            subtitle = null;
        }
        else {
            title    = m_constants.editCloud();
            subtitle = m_planName;
        }

        ChangeTitleEvent.fire(this, title, subtitle);
    }

    //~ Methods ----------------------------------------------------------------

    private static boolean asBoolean(String value)
    {
        boolean debug;

        try {
            debug = Integer.valueOf(StringUtil.nullToEmpty(value)) > 0;
        }
        catch (NumberFormatException ignore) {
            debug = false;
        }

        return debug;
    }

    @NotNull private static Collection<ActualParameter> makeActuals(
            @NotNull Map<String, String> actuals)
    {
        List<ActualParameter> params = new ArrayList<ActualParameter>();

        for (Map.Entry<String, String> entry : actuals.entrySet()) {
            params.add(new SimpleActualParameter(entry.getKey(),
                    entry.getValue()));
        }

        return params;
    }

    @NotNull private static List<FormalParameter> makeFormals(
            @NotNull Iterable<Parameter> formals)
    {
        List<FormalParameter> params = new ArrayList<FormalParameter>();

        for (Parameter formal : formals) {
            params.add(new SimpleFormalParameter(formal.getParameterName(),
                    formal.getDescription(), "", formal.getDefault(),
                    asBoolean(formal.getRequired())));
        }

        Collections.sort(params, new Comparator<FormalParameter>() {
                @Override public int compare(
                        @NotNull FormalParameter o1,
                        @NotNull FormalParameter o2)
                {
                    return o1.getName()
                             .compareToIgnoreCase(o2.getName());
                }
            });

        return params;
    }

    //~ Inner Interfaces -------------------------------------------------------

    // ~ Inner Interfaces
    // -------------------------------------------------------
    @NameToken(editPage)
    @ProxyCodeSplit
    @SuppressWarnings(
        {"InterfaceNeverImplemented", "MarkerInterface", "PublicInnerClass"}
    )
    public interface MyProxy
        extends ProxyPlace<PlanEditorPresenter> { }

    @SuppressWarnings({"PublicInnerClass"})
    public interface MyView
        extends View,
        HasUiHandlers<PlanEditorUiHandlers>
    {

        //~ Methods ------------------------------------------------------------

        // ~ Methods
        // ------------------------------------------------------------
        void showLoading(boolean showLoading);

        void showProviderPanel(boolean showProvider);

        HasClickHandlers getCancel();

        HasText getCostMax();

        HasText getCostPeriod();

        HasValue<Boolean> getDebug();

        HasText getDescription();

        HasValue<Boolean> getEnabled();

        RenderableParameterPanel getGrowParams();

        HasText getKillLimitMax();

        HasText getKillLimitMin();

        HasValue<String> getKillPolicy();

        HasText getName();

        ResourcePoolPicker getPoolName();

        ValuedListBox getProviderType();

        ParameterPanelWidget getQueryParameters();

        HasValue<String> getQueryProcedure();

        HasValue<String> getQueryProject();

        HasClickHandlers getRestartUsage();

        HasClickHandlers getSave();

        RenderableParameterPanel getShrinkParams();

        ListDataProvider<TimeOfDayLimit> getTimeOfDayLimits();
    }
}
