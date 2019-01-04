
// AxisOptions.java --
//
// AxisOptions.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.model;

public interface AxisOptions
{

    //~ Methods ----------------------------------------------------------------

    double getAxisMax();

    double getAxisMin();

    boolean getHasGridLines();

    int getTickCount();

    String getTickLabelFormat();
}
