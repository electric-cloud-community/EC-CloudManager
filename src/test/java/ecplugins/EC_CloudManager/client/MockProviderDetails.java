
// MockProviderDetails.java --
//
// MockProviderDetails.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client;

import java.util.List;

import ecplugins.EC_CloudManager.client.model.Parameter;
import ecplugins.EC_CloudManager.client.model.ProviderDetails;

public class MockProviderDetails
    implements ProviderDetails
{

    //~ Instance fields --------------------------------------------------------

    String m_name;

    //~ Constructors -----------------------------------------------------------

    public MockProviderDetails(String name)
    {
        m_name = name;
    }

    //~ Methods ----------------------------------------------------------------

    @Override public List<Parameter> getGrowParameters()
    {
        return null;
    }

    @Override public String getName()
    {
        return m_name;
    }

    @Override public List<Parameter> getShrinkParameters()
    {
        return null;
    }
}
