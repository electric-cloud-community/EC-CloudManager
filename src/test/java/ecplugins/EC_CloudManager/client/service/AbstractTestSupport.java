
// AbstractTestSupport.java --
//
// AbstractTestSupport.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.service;

import org.jetbrains.annotations.NonNls;

import com.google.gwt.junit.client.GWTTestCase;

public abstract class AbstractTestSupport
    extends GWTTestCase
{

    //~ Static fields/initializers ---------------------------------------------

    @NonNls static final String TEST_PLAN_NAME   = "three";
    @NonNls static final String TEST_PLAN        =
        "{\"cost\":{\"COST_START\":\"0\",\"COST_CONSUMED\":\"0\","
            + "\"COST_PERIOD\":\"728\",\"COST_MAX\":\"272\"},"
            + "\"tod\":{\"18-min\":\"2\",\"15-max\":\"10\",\"13-min\":"
            + "\"2\",\"14-min\":\"2\",\"6-min\":\"2\",\"2-min\":\"2\","
            + "\"0-min\":\"2\",\"5-min\":\"2\",\"17-min\":\"2\","
            + "\"0-max\":\"10\",\"11-min\":\"2\",\"19-max\":\"10\","
            + "\"6-max\":\"10\",\"9-max\":\"10\",\"17-max\":\"10\","
            + "\"7-min\":\"2\",\"20-min\":\"2\",\"3-max\":\"10\","
            + "\"11-max\":\"10\",\"16-min\":\"2\",\"10-max\":\"10\","
            + "\"15-min\":\"2\",\"8-min\":\"2\",\"22-max\":\"10\","
            + "\"2-max\":\"10\",\"23-max\":\"10\",\"13-max\":\"10\","
            + "\"7-max\":\"10\",\"21-min\":\"2\",\"5-max\":\"10\","
            + "\"14-max\":\"10\",\"9-min\":\"2\",\"16-max\":\"10\","
            + "\"1-max\":\"10\",\"22-min\":\"2\",\"18-max\":\"10\","
            + "\"4-min\":\"2\",\"12-min\":\"2\",\"20-max\":\"10\","
            + "\"19-min\":\"2\",\"8-max\":\"10\",\"10-min\":\"2\","
            + "\"12-max\":\"10\",\"4-max\":\"10\",\"23-min\":\"2\","
            + "\"21-max\":\"10\",\"1-min\":\"2\",\"3-min\":\"2\"},"
            + "\"query\":{\"queryCfg\":[{\"value\":\"v2\","
            + "\"name\":\"n2\"},{\"value\":\"v1\",\"name\":\"n1\"}],"
            + "\"Proj\":\"EC-CloudManager-1.0.0.0\",\"Proc\":\"DummyQuery\"},"
            + "\"name\":\"" + TEST_PLAN_NAME
            + "\",\"active\":\"0\",\"poolName\":\"CloudTest\","
            + "\"target\":\"0\",\"desc\":\"Default plan for testing\","
            + "\"killLimits\":{\"KillLimitMin\":\"45\",\"KillLimitMax\":\"58\","
            + "\"KillLimitPolicy\":\"never\"},\"debug\":\"4\","
            + "\"adjust\":{\"plugin\":\"EC-PCE\",\"growCfg\":["
            + "{\"value\":\"test\",\"name\":\"pce_config\"},"
            + "{\"value\":\"testgroup\",\"name\":\"pce_security_group\"},"
            + "{\"value\":\"ami-123456\",\"name\":\"pce_image\"}],"
            + "\"shrinkCfg\":[{\"value\":\"test\",\"name\":\"pce_config\"}]},"
            + "\"current\":\"10\",\"appRequest\":\"0\"}";
    @NonNls static final String TEST_PLUGIN_NAME = "EC-PCE";
    @NonNls static final String TEST_PLUGIN      = "{\"growParams\":["
            + "{\"required\":\"0\",\"parameterName\":\"param1\","
            + "\"default\":\"\",\"description\":\"descr1\"},"
            + "{\"required\":\"0\",\"parameterName\":\"param2\","
            + "\"default\":\"\",\"description\":\"descr2\"},"
            + "{\"required\":\"1\",\"parameterName\":\"param3\","
            + "\"default\":\"default3\",\"description\":\"descr3\"}],"
            + "\"name\":\"" + TEST_PLUGIN_NAME + "\","
            + "\"shrinkParams\":["
            + "{\"required\":\"1\",\"parameterName\":\"param4\","
            + "\"default\":\"default4\",\"description\":\"descr4\"}]}";
    @NonNls static final String TEST_CONFIG      = "{\"cfg\":["
            + TEST_PLAN
            + "], \"plugins\":[" + TEST_PLUGIN + "]}";

    //~ Methods ----------------------------------------------------------------

    @Override public String getModuleName()
    {
        return "ecplugins.EC_CloudManager.CloudManager"; // NON-NLS
    }
}
