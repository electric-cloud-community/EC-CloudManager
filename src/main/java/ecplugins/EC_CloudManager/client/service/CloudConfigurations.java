
// CloudConfigurations.java --
//
// CloudConfigurations.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.service;

import java.util.List;

import ecplugins.EC_CloudManager.client.model.PlanDetails;
import ecplugins.EC_CloudManager.client.model.ProviderDetails;

public interface CloudConfigurations
{

    //~ Methods ----------------------------------------------------------------

    List<PlanDetails> getPlans();

    List<ProviderDetails> getProviders();
}
