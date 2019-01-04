// ECGwtSuite.java --
//
// ECGwtSuite.java is part of ElectricCommander.
//
// Copyright (c) 2005-2012 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager;

import junit.framework.Test;
import com.google.gwt.junit.tools.GWTTestSuite;

import com.electriccloud.test.IsGwtTestClass;
import com.electriccloud.test.TestSuiteUtil;

public class ECCloudManagerGwtSuite
    extends GWTTestSuite
{
    //~ Methods ----------------------------------------------------------------

    public static Test suite()
    {
        return TestSuiteUtil.suite(ECCloudManagerGwtSuite.class,
            new IsGwtTestClass());
    }
}
