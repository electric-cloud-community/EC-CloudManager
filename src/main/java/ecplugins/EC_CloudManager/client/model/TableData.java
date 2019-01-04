
// TableData.java --
//
// TableData.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.model;

import java.util.List;

public interface TableData
{

    //~ Methods ----------------------------------------------------------------

    List<Column> getColumns();

    int getRowCount();

    String getTitle();

    double getValue(
            int row,
            int col);

    AxisOptions getXAxisOptions();

    int getXChartSize();

    AxisOptions getYAxisOptions();

    int getYChartSize();
}
