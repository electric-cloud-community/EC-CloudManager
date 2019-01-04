
// CloudServiceAsync.java --
//
// CloudServiceAsync.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.service;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ecplugins.EC_CloudManager.client.model.Deployment;
import ecplugins.EC_CloudManager.client.model.PlanDetails;
import ecplugins.EC_CloudManager.client.model.ScheduleDetails;
import ecplugins.EC_CloudManager.client.model.TableData;

public interface CloudServiceAsync
{

    //~ Methods ----------------------------------------------------------------

    void createPlan(
            PlanDetails           details,
            AsyncCallback<Object> callback);

    void deletePlan(
            String                name,
            AsyncCallback<Object> callback);
            
    void copyPlan(
            PlanDetails                details,
            AsyncCallback<Object> callback);        

    void loadChartData(
            String                   name,
            String                   range,
            AsyncCallback<TableData> callback);

    void loadDeployments(
            String                          name,
            AsyncCallback<List<Deployment>> callback);

    void modifyPlan(
            String                name,
            PlanDetails           details,
            AsyncCallback<Object> callback);

    PlanDetails newPlan();

    /**
     * Refresh ManagerState object. Fires a ModelChanged event when done.
     */
    void refreshState();

    void restartUsage(
            String                planName,
            AsyncCallback<Object> callback);

    void getSchedule(AsyncCallback<ScheduleDetails> callback);

    void setSchedule(
            boolean               enabled,
            String                interval,
            AsyncCallback<Object> callback);
}
