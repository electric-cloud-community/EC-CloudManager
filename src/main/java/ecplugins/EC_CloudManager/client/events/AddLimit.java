
// AddLimit.java --
//
// AddLimit.java is part of ElectricCommander.
//
// Copyright (c) 2005-2012 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.events;

import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.dispatch.annotation.Order;

import ecplugins.EC_CloudManager.client.model.TimeOfDayLimit;

@GenEvent public class AddLimit
{

    //~ Instance fields --------------------------------------------------------

    @Order(1)
    TimeOfDayLimit m_limit;
}
