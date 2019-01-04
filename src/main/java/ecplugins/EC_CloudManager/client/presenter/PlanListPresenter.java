
// PlanListPresenter.java --
//
// PlanListPresenter.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.presenter;

import java.util.logging.Logger;

import com.google.gwt.event.shared.EventBus;

import com.google.inject.Inject;

import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

import ecinternal.client.events.ChangeTitleEvent;
import ecinternal.client.events.ModelChangedEvent;
import ecinternal.client.events.ModelChangedEvent.ModelChangedHandler;

import ecplugins.EC_CloudManager.client.Constants;
import ecplugins.EC_CloudManager.client.events.DeletePlanEvent;
import ecplugins.EC_CloudManager.client.events.CopyPlanEvent;
import ecplugins.EC_CloudManager.client.events.DeletePlanEvent.DeletePlanHandler;
import ecplugins.EC_CloudManager.client.events.CopyPlanEvent.CopyPlanHandler;
import ecplugins.EC_CloudManager.client.model.ManagerState;
import ecplugins.EC_CloudManager.client.model.PlanDetails;
import ecplugins.EC_CloudManager.client.service.CloudServiceAsync;
import ecplugins.EC_CloudManager.client.view.PlanListUiHandlers;

import static ecplugins.EC_CloudManager.client.NameTokens.listPage;

public class PlanListPresenter
    extends Presenter<PlanListPresenter.MyView, PlanListPresenter.MyProxy>
    implements PlanListUiHandlers,
        DeletePlanHandler,
        CopyPlanHandler,
        ModelChangedHandler
{

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger log = Logger.getLogger(PlanListPresenter.class
                .getName());

    //~ Instance fields --------------------------------------------------------

    private final CloudServiceAsync         m_service;
    private final DeletePlanPresenterWidget m_deleteDialog;
    private final CopyPlanPresenterWidget   m_copyDialog;
    private final ConfigurePresenterWidget  m_configureDialog;
    private final ManagerState              m_state;
    private final Constants                 m_constants;

    //~ Constructors -----------------------------------------------------------

    @Inject public PlanListPresenter(
            EventBus                  eventBus,
            MyView                    view,
            MyProxy                   proxy,
            CloudServiceAsync         service,
            DeletePlanPresenterWidget deleteDialog,
            CopyPlanPresenterWidget   copyDialog,
            ConfigurePresenterWidget  configureDialog,
            Constants                 constants,
            ManagerState              state)
    {
        super(eventBus, view, proxy);
        m_service         = service;
        m_deleteDialog    = deleteDialog;
        m_copyDialog      = copyDialog;
        m_configureDialog = configureDialog;
        m_state           = state;
        m_constants       = constants;
    }

    //~ Methods ----------------------------------------------------------------

    @Override public void configure()
    {
        addToPopupSlot(m_configureDialog);
    }

    @Override public void onDeletePlan(DeletePlanEvent event)
    {

        if (m_state.isLoaded()) {
            m_state.getPlans()
                   .getList()
                   .remove(event.getPlan());
        }
    }
    
    @Override public void onCopyPlan(CopyPlanEvent event)
    {

        if (m_state.isLoaded()) {
            m_state.getPlans()
                   .getList()
                   .add(event.getPlan());
        }
        getView().showLoading(true);
        m_service.refreshState();
    }

    @Override public void onModelChanged(ModelChangedEvent event)
    {
        getView().showLoading(false);
        getView().sortData();
    }

    @Override public void refreshList()
    {

        // Fetch the contents of the list
        getView().showLoading(true);
        m_service.refreshState();
    }

    @Override public void showDeleteDialog(PlanDetails cloud)
    {
        m_deleteDialog.setCloud(cloud);
        addToPopupSlot(m_deleteDialog);
    }
    
    @Override public void showCopyDialog(PlanDetails cloud)
    {
        m_copyDialog.setCloud(cloud);
        addToPopupSlot(m_copyDialog);        
    }

    @Override protected void onBind()
    {
        super.onBind();
        addRegisteredHandler(DeletePlanEvent.getType(), this);
        addRegisteredHandler(CopyPlanEvent.getType(), this);
        addRegisteredHandler(ModelChangedEvent.getType(), this);
        getView().setUiHandlers(this);
    }

    @Override protected void onHide()
    {
        super.onHide();
        removeFromSlot(null, m_deleteDialog);
        removeFromSlot(null, m_copyDialog);
        removeFromSlot(null, m_configureDialog);
    }

    @Override protected void onReset()
    {
        super.onReset();

        // If we already have the data, make sure the list is sorted, otherwise
        // fetch the data.
        if (m_state.isLoaded()) {
            getView().sortData();
        }
        else {
            refreshList();
        }
    }

    @Override protected void onReveal()
    {
        super.onReveal();
        ChangeTitleEvent.fire(this, m_constants.cloudPlans(), null);
    }

    @Override protected void revealInParent()
    {
        RevealContentEvent.fire(this, CloudManagerPresenter.TYPE_SetMainContent,
            this);
    }

    //~ Inner Interfaces -------------------------------------------------------

    /**
     * {@link PlanListPresenter}'s proxy.
     */
    @NameToken(listPage)
    @ProxyCodeSplit public interface MyProxy
        extends ProxyPlace<PlanListPresenter>
    {
    }

    /**
     * {@link PlanListPresenter}'s view.
     */
    public interface MyView
        extends View,
        HasUiHandlers<PlanListUiHandlers>
    {

        //~ Methods ------------------------------------------------------------

        void showLoading(boolean loading);

        void sortData();
    }
}
