
// ProviderDetails.java --
//
// ProviderDetails.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.model;

import java.util.List;

public interface ProviderDetails
{

    //~ Methods ----------------------------------------------------------------

    List<Parameter> getGrowParameters();

    String getName();

    List<Parameter> getShrinkParameters();
}
