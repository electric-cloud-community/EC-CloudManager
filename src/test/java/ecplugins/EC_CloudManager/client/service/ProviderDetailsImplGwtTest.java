
// ProviderDetailsImplTest.java --
//
// ProviderDetailsImplTest.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.service;

import java.util.List;

import com.google.gwt.core.client.JsonUtils;

import ecplugins.EC_CloudManager.client.model.Parameter;

public class ProviderDetailsImplGwtTest
    extends AbstractTestSupport
{

    //~ Methods ----------------------------------------------------------------

    public void testEmpty()
    {
        ProviderDetailsImpl provider = JsonUtils.safeEval("{}");

        assertNull("name", provider.getName());
        assertTrue("empty grow params", provider.getGrowParameters()
                                                .isEmpty());
        assertTrue("empty shrink params",
            provider.getShrinkParameters()
                    .isEmpty());
    }

    public void testWithData()
    {
        ProviderDetailsImpl provider = JsonUtils.safeEval(TEST_PLUGIN);

        assertEquals("name", TEST_PLUGIN_NAME, provider.getName());

        List<Parameter> grow = provider.getGrowParameters();

        assertEquals("grow size", 3, grow.size());
        assertParameter("param 1", "0", "param1", "", "descr1", grow.get(0));
        assertParameter("param 2", "0", "param2", "", "descr2", grow.get(1));
        assertParameter("param 3", "1", "param3", "default3", "descr3",
            grow.get(2));

        List<Parameter> shrink = provider.getShrinkParameters();

        assertEquals("shrink size", 1, shrink.size());
        assertParameter("shrink 1", "1", "param4", "default4", "descr4",
            shrink.get(0));
    }

    private void assertParameter(
            String    msg,
            String    required,
            String    name,
            String    defaultValue,
            String    description,
            Parameter parameter)
    {
        assertNotNull(msg, parameter);
        assertEquals(msg + " required", required, parameter.getRequired());
        assertEquals(msg + " name", name, parameter.getParameterName());
        assertEquals(msg + " defaultValue", defaultValue,
            parameter.getDefault());
        assertEquals(msg + " description", description,
            parameter.getDescription());
    }
}
