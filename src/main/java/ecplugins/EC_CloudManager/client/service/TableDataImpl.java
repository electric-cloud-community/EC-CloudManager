
// TableDataImpl.java --
//
// TableDataImpl.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.service;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import ecplugins.EC_CloudManager.client.model.AxisOptions;
import ecplugins.EC_CloudManager.client.model.Column;
import ecplugins.EC_CloudManager.client.model.TableData;

import java.util.ArrayList;
import java.util.List;

public class TableDataImpl
    extends JavaScriptObject
    implements TableData
{

    //~ Constructors -----------------------------------------------------------

    protected TableDataImpl()
    {
    }

    //~ Methods ----------------------------------------------------------------

    private native JsArray<ColumnImpl> getCols() /*-{
        if (typeof(this.table.cols) == "undefined") {
        this.table.cols = new Array();
        }
        return this.table.cols;
    }-*/;

    @Override public final List<Column> getColumns()
    {
        List<Column> result = new ArrayList<Column>();

        for (int i = 0; i < getCols().length(); i++) {
            result.add(getCols().get(i));
        }

        return result;
    }

    @Override public final native int getRowCount() /*-{
        return this.table.rows.length;
    }-*/;

    @Override public final native String getTitle() /*-{
        return this.table.p.chartTitle;
    }-*/;

    @Override public final native double getValue(
            int row,
            int col) /*-{
        return this.table.rows[row].c[col].v;
    }-*/;

    @Override public final native AxisOptions getXAxisOptions() /*-{
        return typeof(this.table.p.xAxis) == "undefined" ? {} : this.table.p.xAxis;
    }-*/;

    @Override public final native int getXChartSize() /*-{
        return this.table.p.xChartSize;
    }-*/;

    @Override public final native AxisOptions getYAxisOptions() /*-{
        return typeof(this.table.p.yAxis) == "undefined" ? {} : this.table.p.yAxis;
    }-*/;

    @Override public final native int getYChartSize() /*-{
        return this.table.p.yChartSize;
    }-*/;

    //~ Inner Classes ----------------------------------------------------------

    private static class AxisOptionsImpl
        extends JavaScriptObject
        implements AxisOptions
    {

        //~ Constructors -------------------------------------------------------

        protected AxisOptionsImpl()
        {
        }

        //~ Methods ------------------------------------------------------------

        @Override public final native double getAxisMax() /*-{
            return this.axisMax;
        }-*/;

        @Override public final native double getAxisMin() /*-{
            return this.axisMin;
        }-*/;

        @Override public final native boolean getHasGridLines() /*-{
            return this.hasGridLines;
        }-*/;

        @Override public final native int getTickCount() /*-{
            return this.tickCount;
        }-*/;

        @Override public final native String getTickLabelFormat() /*-{
            return this.tickLabelFormat;
        }-*/;
    }

    private static class ColumnImpl
        extends JavaScriptObject
        implements Column
    {

        //~ Constructors -------------------------------------------------------

        protected ColumnImpl()
        {
        }

        //~ Methods ------------------------------------------------------------

        @Override public final native String getColor() /*-{
            var result;
            if (typeof(this.p) != "undefined") {
            result = this.p.color;
            }
            return result;
        }-*/;

        @Override public final native String getHovertextTemplate() /*-{
            var result;
            if (typeof(this.p) != "undefined") {
            result = this.p.hovertextTemplate;
            }
            return result;
        }-*/;

        @Override public final native String getLabel() /*-{
            return this.label;
        }-*/;
    }
}
