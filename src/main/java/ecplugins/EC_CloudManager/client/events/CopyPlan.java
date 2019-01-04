// CopyPlan.java --
//
// CopyPlan.java is part of ElectricCommander.
//
// Copyright (c) 2005-2012 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.events;

import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.dispatch.annotation.Order;

import ecplugins.EC_CloudManager.client.model.PlanDetails;

@GenEvent public class CopyPlan
{

    //~ Instance fields --------------------------------------------------------

    @Order(1)
    PlanDetails plan;
}
