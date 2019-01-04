
// CloudDetailsImplTest.java --
//
// CloudDetailsImplTest.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.service;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;

import ecplugins.EC_CloudManager.client.model.PlanDetails;

public class CloudDetailsImplGwtTest
    extends AbstractTestSupport
{

    //~ Methods ----------------------------------------------------------------

    public void testCreate()
    {
        PlanDetails cloud = (PlanDetails) JavaScriptObject.createObject();

        assertNull("active", cloud.getActive());
        assertNull("adjustPlugin", cloud.getAdjustPlugin());
        assertNull("costmax", cloud.getCostMax());
        assertNull("costperiod", cloud.getCostPeriod());
        assertNull("debug", cloud.getDebug());
        assertNull("description", cloud.getDescription());
        assertTrue("growConfig", cloud.getGrowConfig()
                                      .isEmpty());
        assertNull("killLimitMax", cloud.getKillLimitMax());
        assertNull("killLimitMin", cloud.getKillLimitMin());
        assertNull("killLimitPolicy", cloud.getKillLimitPolicy());
        assertNull("name", cloud.getName());
        assertNull("poolName", cloud.getPoolName());
        assertTrue("queryConfig", cloud.getQueryConfig()
                                       .isEmpty());
        assertNull("queryProcedure", cloud.getQueryProcedure());
        assertNull("queryProject", cloud.getQueryProject());
        assertTrue("shrinkConfig", cloud.getShrinkConfig()
                                        .isEmpty());

        for (int i = 0; i < 24; ++i) {
            assertNull("timeOfDayMin" + i, cloud.getTimeOfDayMin(i));
            assertNull("timeOfDayMax" + i, cloud.getTimeOfDayMax(i));
        }
    }

    public void testInitFromData()
    {
        PlanDetails cloud = (PlanDetails) JsonUtils.safeEval(TEST_PLAN);

        assertEquals("active", "0", cloud.getActive());
        assertEquals("adjustPlugin", "EC-PCE", cloud.getAdjustPlugin());
        assertEquals("costmax", "272", cloud.getCostMax());
        assertEquals("costperiod", "728", cloud.getCostPeriod());
        assertEquals("debug", "4", cloud.getDebug());
        assertEquals("description", "Default plan for testing",
            cloud.getDescription());

        Map<String, String> growConfig = cloud.getGrowConfig();

        assertEquals("growConfig", 3, growConfig.size());
        assertEquals("growParam1", "test", growConfig.get("pce_config"));
        assertEquals("growParam2", "testgroup",
            growConfig.get("pce_security_group"));
        assertEquals("growParam3", "ami-123456", growConfig.get("pce_image"));

        //
        assertEquals("killLimitMax", "58", cloud.getKillLimitMax());
        assertEquals("killLimitMin", "45", cloud.getKillLimitMin());
        assertEquals("killLimitPolicy", "never", cloud.getKillLimitPolicy());
        assertEquals("name", "three", cloud.getName());
        assertEquals("poolName", "CloudTest", cloud.getPoolName());

        Map<String, String> queryConfig = cloud.getQueryConfig();

        assertEquals("queryConfig", 2, queryConfig.size());
        assertEquals("queryParam1", "v1", queryConfig.get("n1"));
        assertEquals("queryParam2", "v2", queryConfig.get("n2"));
        assertEquals("queryProcedure", "DummyQuery", cloud.getQueryProcedure());
        assertEquals("queryProject", "EC-CloudManager-1.0.0.0",
            cloud.getQueryProject());

        Map<String, String> shrinkConfig = cloud.getShrinkConfig();

        assertEquals("shrinkConfig", 1, shrinkConfig.size());
        assertEquals("shrinkParam1", "test", shrinkConfig.get("pce_config"));

        for (int i = 0; i < 24; ++i) {
            assertEquals("timeOfDayMin" + i, "2", cloud.getTimeOfDayMin(i));
            assertEquals("timeOfDayMax" + i, "10", cloud.getTimeOfDayMax(i));
        }
    }

    public void testUpdate()
    {
        PlanDetails cloud = (PlanDetails) JsonUtils.safeEval(TEST_PLAN);

        // Update values
        cloud.setActive("9");
        cloud.setAdjustPlugin("16");
        cloud.setCostMax("1");
        cloud.setCostPeriod("2");
        cloud.setDebug("10");
        cloud.setDescription("6");

        Map<String, String> growMap = new HashMap<String, String>();

        growMap.put("grow1", "gv1");
        growMap.put("grow2", "gv2");
        cloud.setGrowConfig(growMap);
        cloud.setKillLimitMax("3");
        cloud.setKillLimitMin("4");
        cloud.setKillLimitPolicy("any");
        cloud.setName("7");
        cloud.setPoolName("8");

        Map<String, String> map = new HashMap<String, String>();

        map.put("arg1", "val1");
        map.put("arg2", "val2");
        map.put("arg3", "val3");
        cloud.setQueryConfig(map);
        cloud.setQueryProcedure("proc1");
        cloud.setQueryProject("proj1");

        Map<String, String> shrinkMap = new HashMap<String, String>();

        shrinkMap.put("shrink1", "sv1");
        shrinkMap.put("shrink2", "sv2");
        cloud.setShrinkConfig(shrinkMap);
        cloud.setTimeOfDayLimit(0, "11", "12");
        cloud.setTimeOfDayLimit(1, "13", "14");

        // Verify changes
        assertEquals("active", "9", cloud.getActive());
        assertEquals("adjustPlugin", "16", cloud.getAdjustPlugin());
        assertEquals("costmax", "1", cloud.getCostMax());
        assertEquals("costperiod", "2", cloud.getCostPeriod());
        assertEquals("debug", "10", cloud.getDebug());
        assertEquals("description", "6", cloud.getDescription());

        Map<String, String> growConfig = cloud.getGrowConfig();

        assertEquals("growConfig", 2, growConfig.size());
        assertEquals("growParam1", "gv1", growConfig.get("grow1"));
        assertEquals("growParam2", "gv2", growConfig.get("grow2"));
        assertEquals("killLimitMax", "3", cloud.getKillLimitMax());
        assertEquals("killLimitMin", "4", cloud.getKillLimitMin());
        assertEquals("killLimitPolicy", "any", cloud.getKillLimitPolicy());
        assertEquals("name", "7", cloud.getName());
        assertEquals("poolName", "8", cloud.getPoolName());
        assertEquals("queryConfig", 3, cloud.getQueryConfig()
                                            .size());
        assertEquals("queryParam1", "val1", cloud.getQueryConfig()
                                                 .get("arg1"));
        assertEquals("queryParam2", "val2", cloud.getQueryConfig()
                                                 .get("arg2"));
        assertEquals("queryParam3", "val3", cloud.getQueryConfig()
                                                 .get("arg3"));
        assertEquals("queryProcedure", "proc1", cloud.getQueryProcedure());
        assertEquals("queryProject", "proj1", cloud.getQueryProject());

        Map<String, String> shrinkConfig = cloud.getShrinkConfig();

        assertEquals("shrinkConfig", 2, shrinkConfig.size());
        assertEquals("shrinkParam1", "sv1", shrinkConfig.get("shrink1"));
        assertEquals("shrinkParam2", "sv2", shrinkConfig.get("shrink2"));
        assertEquals("timeOfDayMin0", "11", cloud.getTimeOfDayMin(0));
        assertEquals("timeOfDayMax0", "12", cloud.getTimeOfDayMax(0));
        assertEquals("timeOfDayMin1", "13", cloud.getTimeOfDayMin(1));
        assertEquals("timeOfDayMax1", "14", cloud.getTimeOfDayMax(1));

        for (int i = 2; i < 24; ++i) {
            assertEquals("timeOfDayMin" + i, "2", cloud.getTimeOfDayMin(i));
            assertEquals("timeOfDayMax" + i, "10", cloud.getTimeOfDayMax(i));
        }
    }
}
