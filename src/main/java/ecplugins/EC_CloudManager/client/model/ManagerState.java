
// ManagerState.java --
//
// ManagerState.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.model;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.google.gwt.view.client.ListDataProvider;

public interface ManagerState
{

    //~ Methods ----------------------------------------------------------------

    void changePlan(
            @NotNull String oldPlanName,
            PlanDetails     plan);

    PlanDetails findPlanByName(String planName);

    ProviderDetails findProviderByName(String providerName);

    int getPlanIndex(String planName);

    ListDataProvider<PlanDetails> getPlans();

    List<ProviderDetails> getProviders();

    boolean isLoaded();

    void setData(
            List<PlanDetails>     plans,
            List<ProviderDetails> providers);
}
