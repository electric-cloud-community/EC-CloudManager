
// DetailsPresenter.java --
//
// DetailsPresenter.java is part of ElectricCommander.
//
// Copyright (c) 2005-2012 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.presenter;

import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.ListDataProvider;

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
import ecplugins.EC_CloudManager.client.NameTokens;
import ecplugins.EC_CloudManager.client.model.Deployment;
import ecplugins.EC_CloudManager.client.model.ManagerState;
import ecplugins.EC_CloudManager.client.model.PlanDetails;
import ecplugins.EC_CloudManager.client.model.TableData;
import ecplugins.EC_CloudManager.client.service.CloudServiceAsync;
import ecplugins.EC_CloudManager.client.view.DetailsUiHandlers;

import com.electriccloud.commander.client.util.StringUtil;

import ecinternal.client.events.ChangeIndexEvent;
import ecinternal.client.events.ChangeTitleEvent;
import ecinternal.client.events.ModelChangedEvent;
import ecinternal.client.events.UpdateIteratorEvent;
import ecinternal.client.request.ServiceException;

import static ecplugins.EC_CloudManager.client.CloudManagerPlaceManager.PLAN_NAME;
import static ecplugins.EC_CloudManager.client.NameTokens.detailsPage;

public class DetailsPresenter
    extends Presenter<DetailsPresenter.MyView, DetailsPresenter.MyProxy>
    implements DetailsUiHandlers,
        ChangeHandler,
        ModelChangedEvent.ModelChangedHandler,
        ChangeIndexEvent.ChangeIndexHandler
{

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger log = Logger.getLogger("DetailsPresenter");

    //~ Instance fields --------------------------------------------------------

    // Injected by Gin.
    private final Constants         m_constants;
    private final ManagerState      m_state;
    private final PlaceManager      m_placeManager;
    private final CloudServiceAsync m_service;

    /** The cloud that is being displayed. */
    @NonNls private String m_planName;

    //~ Constructors -----------------------------------------------------------

    @Inject public DetailsPresenter(
            EventBus          eventBus,
            MyView            view,
            MyProxy           proxy,
            PlaceManager      placeManager,
            Constants         constants,
            CloudServiceAsync service,
            ManagerState      state)
    {
        super(eventBus, view, proxy);
        m_constants    = constants;
        m_service      = service;
        m_placeManager = placeManager;
        m_state        = state;
    }

    //~ Methods ----------------------------------------------------------------

    @Override public void onChange(ChangeEvent event)
    {

        // Called if the user changes the chart range
        loadChartData();
    }

    @Override public void onChangeIndex(ChangeIndexEvent event)
    {

        // Ignore the event if this presenter isn't visible.
        if (!isVisible()) {
            return;
        }

        // The user used the object iterator to select a different plan
        int               newIndex = event.getIndex();
        List<PlanDetails> plans    = m_state.getPlans()
                                            .getList();

        if (newIndex >= 0 && newIndex < plans.size()) {
            PlanDetails details = plans.get(newIndex);

            if (!details.getName()
                        .equalsIgnoreCase(m_planName)) {
                m_placeManager.revealPlace(
                    new PlaceRequest(NameTokens.detailsPage).with(PLAN_NAME,
                        details.getName()));
            }
        }
    }

    @Override public void onModelChanged(ModelChangedEvent event)
    {
        updateIterator();
    }

    @Override public void prepareFromRequest(PlaceRequest request)
    {
        super.prepareFromRequest(request);

        String name = request.getParameter(PLAN_NAME, null);

        if (!name.equals(m_planName)) {
            m_planName = name;
            loadChartData();
            loadDeployments();
        }
    }

    @Override public void refreshChart()
    {
        loadChartData();
        loadDeployments();
    }

    @Override protected void onBind()
    {
        super.onBind();
        addRegisteredHandler(ModelChangedEvent.getType(), this);
        addRegisteredHandler(ChangeIndexEvent.getType(), this);
        getView().setUiHandlers(this);
        getView().getChartRange()
                 .addChangeHandler(this);
    }

    @Override protected void onHide()
    {
        super.onHide();
        UpdateIteratorEvent.fire(this, -1, 0);
    }

    @Override protected void onReset()
    {
        super.onReset();
        updateTitle();

        if (m_state.isLoaded()) {
            updateIterator();
        }
        else {
            m_service.refreshState();
        }
    }

    @Override protected void revealInParent()
    {
        RevealContentEvent.fire(this, CloudManagerPresenter.TYPE_SetMainContent,
            this);
    }

    private void loadChartData()
    {
        getView().showLoading(true);

        String range = getView().getChartRangeValue();

        m_service.loadChartData(m_planName, range,
            new AsyncCallback<TableData>() {
                @Override public void onFailure(Throwable caught)
                {

                    if (caught instanceof ServiceException) {
                        log.severe(((ServiceException) caught).getDetail());
                    }
                    else {
                        log.severe(caught.getMessage());
                    }
                    // TODO: Error handling
                }

                @Override public void onSuccess(TableData result)
                {
                    getView().updateChart(result);
                }
            });
    }

    private void loadDeployments()
    {
        m_service.loadDeployments(m_planName,
            new AsyncCallback<List<Deployment>>() {
                @Override public void onFailure(Throwable caught)
                {

                    if (caught instanceof ServiceException) {
                        log.severe(((ServiceException) caught).getDetail());
                    }
                    else {
                        log.severe(caught.getMessage());
                    }
                    // TODO: Error handling
                }

                @Override public void onSuccess(List<Deployment> result)
                {
                    ListDataProvider<Deployment> deployments = getView()
                            .getDeployments();

                    deployments.getList()
                               .clear();
                    deployments.getList()
                               .addAll(result);
                }
            });
    }

    private void updateIterator()
    {
        UpdateIteratorEvent.fire(this, m_state.getPlanIndex(m_planName),
            m_state.getPlans()
                   .getList()
                   .size());
    }

    private void updateTitle()
    {
        @Nullable String title;
        @Nullable String subtitle;

        if (StringUtil.isEmpty(m_planName)) {
            // error no planName argument
        }
        else {
            title    = m_constants.cloudDetails();
            subtitle = m_planName;

            ChangeTitleEvent.fire(this, title, subtitle);
        }
    }

    //~ Inner Interfaces -------------------------------------------------------

    @NameToken(detailsPage)
    @ProxyCodeSplit public interface MyProxy
        extends ProxyPlace<DetailsPresenter> { }

    public interface MyView
        extends View,
        HasUiHandlers<DetailsUiHandlers>
    {

        //~ Methods ------------------------------------------------------------

        void showLoading(boolean loading);

        void updateChart(TableData data);

        HasChangeHandlers getChartRange();

        String getChartRangeValue();

        ListDataProvider<Deployment> getDeployments();
    }
}
