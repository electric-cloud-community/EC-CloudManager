
// CloudServiceImpl.java --
//
// CloudServiceImpl.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.jetbrains.annotations.NonNls;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.rpc.AsyncCallback;

import com.google.inject.Inject;

import ecinternal.client.events.ModelChangedEvent;

import ecinternal.client.request.DefaultRequestCallback;
import ecinternal.client.request.JSONResponseCallback;

import ecplugins.EC_CloudManager.client.model.Deployment;
import ecplugins.EC_CloudManager.client.model.ManagerState;
import ecplugins.EC_CloudManager.client.model.PlanDetails;
import ecplugins.EC_CloudManager.client.model.ScheduleDetails;
import ecplugins.EC_CloudManager.client.model.TableData;

import com.electriccloud.commander.gwt.client.requests.CgiRequestProxy;
import com.electriccloud.commander.client.util.StringUtil;

public class CloudServiceImpl
    implements CloudServiceAsync
{

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger log = Logger.getLogger("CloudServiceImpl");

    // Get parameters
    @NonNls private static final String PARAM_ACTION   = "action";
    @NonNls private static final String PARAM_NAME     = "name";
    @NonNls private static final String PARAM_RANGE    = "range";
    @NonNls private static final String PARAM_DISABLED = "disabled";
    @NonNls private static final String PARAM_MINUTES  = "minutes";

    //~ Instance fields --------------------------------------------------------

    private final CgiRequestProxy m_requestProxy;
    private final EventBus        m_eventBus;
    private final ManagerState    m_state;

    //~ Constructors -----------------------------------------------------------

    @Inject
    CloudServiceImpl(
            CgiRequestProxy requestProxy,
            EventBus        eventBus,
            ManagerState    state)
    {
        m_requestProxy = requestProxy;
        m_eventBus     = eventBus;
        m_state        = state;
    }

    //~ Methods ----------------------------------------------------------------

    @Override public void createPlan(
            PlanDetails           details,
            AsyncCallback<Object> callback)
    {
        Map<String, String> params = new HashMap<String, String>();

        params.put(PARAM_ACTION, "createPlan");
        params.put(PARAM_NAME, details.getName());

        try {
            m_requestProxy.issuePostRequest(params, serialize(details),
                new DefaultRequestCallback<Object>(callback));
        }
        catch (RequestException e) {
            callback.onFailure(e);
        }
    }

    @Override public void deletePlan(
            String                name,
            AsyncCallback<Object> callback)
    {
        Map<String, String> params = new HashMap<String, String>();

        params.put(PARAM_ACTION, "deletePlan");
        params.put(PARAM_NAME, name);

        try {
            m_requestProxy.issueGetRequest(params,
                new JSONResponseCallback<Object>(callback));
        }
        catch (RequestException e) {
            callback.onFailure(e);
        }
    }
    
    @Override public void copyPlan(
            PlanDetails                details,
            AsyncCallback<Object> callback)
    {
        Map<String, String> params = new HashMap<String, String>();

        params.put(PARAM_ACTION, "copyPlan");
        params.put(PARAM_NAME, details.getName());

        try {
            m_requestProxy.issuePostRequest(params, serialize(details),
                new DefaultRequestCallback<Object>(callback));
        }
        catch (RequestException e) {
            callback.onFailure(e);
        }
    }

    @Override public void loadChartData(
            String                   name,
            String                   range,
            AsyncCallback<TableData> callback)
    {
        Map<String, String> params = new HashMap<String, String>();

        params.put(PARAM_ACTION, "limitReportData");
        params.put(PARAM_NAME, name);
        params.put(PARAM_RANGE, range);

        try {
            m_requestProxy.issueGetRequest(params,
                new JSONResponseCallback<TableData>(callback));
        }
        catch (RequestException e) {
            callback.onFailure(e);
        }
    }

    @Override public void loadDeployments(
            String                          name,
            AsyncCallback<List<Deployment>> callback)
    {
        Map<String, String> params = new HashMap<String, String>();

        params.put(PARAM_ACTION, "getDeps");
        params.put(PARAM_NAME, name);

        try {
            m_requestProxy.issueGetRequest(params,
                new JSONResponseCallback<List<Deployment>>(callback) {
                    @Override
                    @SuppressWarnings({"RefusedBequest"})
                    protected void onResponse(JavaScriptObject jso)
                    {
                        DeploymentResponse      response = (DeploymentResponse)
                            jso;
                        List<Deployment>        result   =
                            new ArrayList<Deployment>();
                        JsArray<DeploymentImpl> list     = response.getDeps();

                        for (int i = 0; i < list.length(); i++) {
                            result.add(list.get(i));
                        }

                        getCallback().onSuccess(result);
                    }
                });
        }
        catch (RequestException e) {
            callback.onFailure(e);
        }
    }

    @Override public void modifyPlan(
            String                name,
            PlanDetails           details,
            AsyncCallback<Object> callback)
    {
        Map<String, String> params = new HashMap<String, String>();

        params.put(PARAM_ACTION, "modifyPlan");
        params.put(PARAM_NAME, name);

        try {
            m_requestProxy.issuePostRequest(params, serialize(details),
                new JSONResponseCallback<Object>(callback));
        }
        catch (RequestException e) {
            callback.onFailure(e);
        }
    }

    @Override public PlanDetails newPlan()
    {
        return (PlanDetails) JavaScriptObject.createObject();
    }

    @Override public void refreshState()
    {
        Map<String, String> params = new HashMap<String, String>();

        params.put(PARAM_ACTION, "getPlans");

        try {
            m_requestProxy.issueGetRequest(params, new RefreshStateCallback());
        }
        catch (RequestException e) {
            log.severe(e.getMessage());
        }
    }

    @Override public void restartUsage(
            String                planName,
            AsyncCallback<Object> callback)
    {
        Map<String, String> params = new HashMap<String, String>();

        params.put(PARAM_ACTION, "restartUsage");
        params.put(PARAM_NAME, planName);

        try {
            m_requestProxy.issueGetRequest(params,
                new JSONResponseCallback<Object>(callback));
        }
        catch (RequestException e) {
            callback.onFailure(e);
        }
    }

    private String serialize(PlanDetails details)
    {

        if (details instanceof PlanDetailsImpl) {
            return new JSONObject((JavaScriptObject) details).toString();
        }
        else {

            // For testing purpose
            return details.toString();
        }
    }

    @Override public void getSchedule(AsyncCallback<ScheduleDetails> callback)
    {
        Map<String, String> params = new HashMap<String, String>();

        params.put(PARAM_ACTION, "getSchedule");

        try {
            m_requestProxy.issueGetRequest(params,
                new JSONResponseCallback<ScheduleDetails>(callback));
        }
        catch (RequestException e) {
            callback.onFailure(e);
        }
    }

    @Override public void setSchedule(
            boolean               enabled,
            String                interval,
            AsyncCallback<Object> callback)
    {
        Map<String, String> params = new HashMap<String, String>();

        params.put(PARAM_ACTION, "setSchedule");
        params.put(PARAM_DISABLED, enabled
                ? "0"
                : "1");
        params.put(PARAM_MINUTES, interval);

        try {
            m_requestProxy.issueGetRequest(params,
                new JSONResponseCallback<Object>(callback));
        }
        catch (RequestException e) {
            callback.onFailure(e);
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    private static class DeploymentImpl
        extends JavaScriptObject
        implements Deployment
    {

        //~ Constructors -------------------------------------------------------

        protected DeploymentImpl() { }

        //~ Methods ------------------------------------------------------------

        @Override public final native String getHandle() /*-{
            return this.handle;
        }-*/;

        @Override public final native String getResource() /*-{
            return this.resource;
        }-*/;

        @Override
        @SuppressWarnings({"NumericCastThatLosesPrecision"})
        public final long getStart()
        {
            return (long) getStartDouble();
        }

        public final native double getStartDouble() /*-{
            return this.start ;
        }-*/;
    }

    private static class DeploymentResponse
        extends JavaScriptObject
    {

        //~ Constructors -------------------------------------------------------

        protected DeploymentResponse() { }

        //~ Methods ------------------------------------------------------------

        public final native JsArray<DeploymentImpl> getDeps() /*-{
            return typeof(this.deps) == 'undefined' ? new Array() : this.deps ;
        }-*/;
    }

    private class RefreshStateCallback
        implements RequestCallback
    {

        //~ Methods ------------------------------------------------------------

        @Override public void onError(
                Request   request,
                Throwable exception)
        {
            log.severe(exception.getMessage());
        }

        @Override public void onResponseReceived(
                Request  request,
                Response response)
        {

            if (response.getStatusCode() == Response.SC_OK) {
                String text = response.getText();

                if (!StringUtil.isEmpty(text)) {
                    CloudConfigurations config = (CloudConfigurations) JsonUtils
                            .safeEval(text);

                    m_state.setData(config.getPlans(), config.getProviders());
                    ModelChangedEvent.fire(m_eventBus);

                    return;
                }
            }

            log.severe(response.getStatusText() + ": " + response.getText());
        }
    }
}
