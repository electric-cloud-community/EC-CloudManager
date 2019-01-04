
// CloudConfigurationsImplTest.java --
//
// CloudConfigurationsImplTest.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.service;

import java.util.List;

import ecplugins.EC_CloudManager.client.model.PlanDetails;
import ecplugins.EC_CloudManager.client.model.ProviderDetails;
import com.google.gwt.core.client.JsonUtils;

public class CloudConfigurationsImplGwtTest
    extends AbstractTestSupport
{

    //~ Methods ----------------------------------------------------------------

    public void testConfig_empty()
        throws Exception
    {
        CloudConfigurationsImpl config = JsonUtils.safeEval("{}");

        assertTrue("empty", config.getPlans()
                                  .isEmpty());
        assertTrue("empty", config.getProviders()
                                  .isEmpty());
    }

    public void testConfig_nonEmpty()
        throws Exception
    {
        CloudConfigurationsImpl config = JsonUtils.safeEval(TEST_CONFIG);
        List<PlanDetails>      clouds = config.getPlans();

        assertEquals("one details", 1, clouds.size());
        assertEquals("name", TEST_PLAN_NAME, clouds.get(0)
                                                   .getName());

        List<ProviderDetails> providers = config.getProviders();

        assertEquals("one provider", 1, providers.size());
        assertEquals("name", TEST_PLUGIN_NAME, providers.get(0)
                                                   .getName());
    }
}
