
// CloudDetailsImpl.java --
//
// CloudDetailsImpl.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.service;

import java.util.Map;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import ecplugins.EC_CloudManager.client.model.PlanDetails;

public class PlanDetailsImpl
    extends JavaScriptObject
    implements PlanDetails
{

    //~ Constructors -----------------------------------------------------------

    protected PlanDetailsImpl()
    {
    }

    //~ Methods ----------------------------------------------------------------

    @Override public final native String getActive() /*-{
        return this.active;
    }-*/;

    private native Adjust getAdjust() /*-{
        if (typeof(this.adjust) == "undefined") {
        this.adjust = {};
        }
        return this.adjust;
    }-*/;

    @Override public final String getAdjustPlugin()
    {
        return getAdjust().getPlugin();
    }

    private native Cost getCost() /*-{
        if (typeof(this.cost) == "undefined") {
        this.cost = {};
        }
        return this.cost;
    }-*/;

    @Override public final String getCostMax()
    {
        return getCost().getMax();
    }

    @Override public final String getCostPeriod()
    {
        return getCost().getPeriod();
    }

    @Override public final native String getDebug() /*-{
        return this.debug;
    }-*/;

    @Override public final native String getDescription() /*-{
        return this.desc;
    }-*/;

    @Override public final Map<String, String> getGrowConfig()
    {
        return JsUtils.jsArrayToMap(getAdjust().getGrowCfg());
    }

    @Override public final String getKillLimitMax()
    {
        return getKillLimits().getKillLimitMax();
    }

    @Override public final String getKillLimitMin()
    {
        return getKillLimits().getKillLimitMin();
    }

    @Override public final String getKillLimitPolicy()
    {
        return getKillLimits().getKillLimitPolicy();
    }

    private native KillLimits getKillLimits() /*-{
        if (typeof(this.killLimits) == "undefined") {
        this.killLimits = {};
        }
        return this.killLimits;
    }-*/;

    @Override public final native String getName() /*-{
        return this.name;
    }-*/;

    @Override public final native String getPoolName() /*-{
        return this.poolName;
    }-*/;

    private native Query getQuery() /*-{
        if (typeof(this.query) == "undefined") {
        this.query = {};
        }
        return this.query;
    }-*/;

    @Override public final Map<String, String> getQueryConfig()
    {
        return JsUtils.jsArrayToMap(getQuery().getQueryCfg());
    }

    @Override public final String getQueryProcedure()
    {
        return getQuery().getProc();
    }

    @Override public final String getQueryProject()
    {
        return getQuery().getProj();
    }

    @Override public final Map<String, String> getShrinkConfig()
    {
        return JsUtils.jsArrayToMap(getAdjust().getShrinkCfg());
    }

    private native TimeOfDayLimits getTimeOfDayLimits() /*-{
        if (typeof(this.tod) == "undefined") {
        this.tod = {};
        }
        return this.tod;
    }-*/;

    @Override public final String getTimeOfDayMax(int hour)
    {
        return getTimeOfDayLimits().getMax(hour);
    }

    @Override public final String getTimeOfDayMin(int hour)
    {
        return getTimeOfDayLimits().getMin(hour);
    }

    @Override public final native void setActive(String value) /*-{
        this.active = value;
    }-*/;

    @Override public final void setAdjustPlugin(String value)
    {
        getAdjust().setPlugin(value);
    }

    @Override public final void setCostMax(String value)
    {
        getCost().setMax(value);
    }

    @Override public final void setCostPeriod(String value)
    {
        getCost().setPeriod(value);
    }

    @Override public final native void setDebug(String value) /*-{
        this.debug = value;
    }-*/;

    @Override public final native void setDescription(String value) /*-{
        this.desc = value;
    }-*/;

    @Override public final void setGrowConfig(Map<String, String> values)
    {
        JsUtils.mapToJsArray(values, getAdjust().getGrowCfg());
    }

    @Override public final void setKillLimitMax(String value)
    {
        getKillLimits().setKillLimitMax(value);
    }

    @Override public final void setKillLimitMin(String value)
    {
        getKillLimits().setKillLimitMin(value);
    }

    @Override public final void setKillLimitPolicy(String value)
    {
        getKillLimits().setKillLimitPolicy(value);
    }

    @Override public final native void setName(String value) /*-{
        this.name = value;
    }-*/;

    @Override public final native void setPoolName(String value) /*-{
        this.poolName = value;
    }-*/;

    @Override public final void setQueryConfig(Map<String, String> values)
    {
        JsUtils.mapToJsArray(values, getQuery().getQueryCfg());
    }

    @Override public final void setQueryProcedure(String value)
    {
        getQuery().setProc(value);
    }

    @Override public final void setQueryProject(String value)
    {
        getQuery().setProj(value);
    }

    @Override public final void setShrinkConfig(Map<String, String> values)
    {
        JsUtils.mapToJsArray(values, getAdjust().getShrinkCfg());
    }

    @Override public final void setTimeOfDayLimit(
            int    hour,
            String min,
            String max)
    {
        TimeOfDayLimits limits = getTimeOfDayLimits();

        limits.setMin(hour, min);
        limits.setMax(hour, max);
    }

    //~ Inner Classes ----------------------------------------------------------

    private static class Adjust
        extends JavaScriptObject
    {

        //~ Constructors -------------------------------------------------------

        protected Adjust()
        {
        }

        //~ Methods ------------------------------------------------------------

        public final native JsArray<ActualParameterImpl> getGrowCfg() /*-{
            if (typeof(this.growCfg) == "undefined") {
            this.growCfg = new Array();
            }
            return this.growCfg;
        }-*/;

        public final native String getPlugin() /*-{
            return this.plugin;
        }-*/;

        public final native JsArray<ActualParameterImpl> getShrinkCfg() /*-{
            if (typeof(this.shrinkCfg) == "undefined") {
            this.shrinkCfg = new Array();
            }
            return this.shrinkCfg;
        }-*/;

        public final native void setPlugin(String value) /*-{
            this.plugin = value;
        }-*/;
    }

    private static class Cost
        extends JavaScriptObject
    {

        //~ Constructors -------------------------------------------------------

        protected Cost()
        {
        }

        //~ Methods ------------------------------------------------------------

        public final native String getMax() /*-{
            return this.COST_MAX;
        }-*/;

        public final native String getPeriod() /*-{
            return this.COST_PERIOD;
        }-*/;

        public final native void setMax(String value) /*-{
            this.COST_MAX = value;
        }-*/;

        public final native void setPeriod(String value) /*-{
            this.COST_PERIOD = value;
        }-*/;
    }

    private static class KillLimits
        extends JavaScriptObject
    {

        //~ Constructors -------------------------------------------------------

        protected KillLimits()
        {
        }

        //~ Methods ------------------------------------------------------------

        public final native String getKillLimitMax() /*-{
            return this.KillLimitMax;
        }-*/;

        public final native String getKillLimitMin() /*-{
            return this.KillLimitMin;
        }-*/;

        public final native String getKillLimitPolicy() /*-{
            return this.KillLimitPolicy;
        }-*/;

        public final native void setKillLimitMax(String value) /*-{
            this.KillLimitMax = value;
        }-*/;

        public final native void setKillLimitMin(String value) /*-{
            this.KillLimitMin = value;
        }-*/;

        public final native void setKillLimitPolicy(String value) /*-{
            this.KillLimitPolicy = value;
        }-*/;
    }

    private static class Query
        extends JavaScriptObject
    {

        //~ Constructors -------------------------------------------------------

        protected Query()
        {
        }

        //~ Methods ------------------------------------------------------------

        public final native String getProc() /*-{
            return this.Proc;
        }-*/;

        public final native String getProj() /*-{
            return this.Proj;
        }-*/;

        public final native JsArray<ActualParameterImpl> getQueryCfg() /*-{
            if (typeof(this.queryCfg) == "undefined") {
            this.queryCfg = new Array();
            }
            return this.queryCfg;
        }-*/;

        public final native void setProc(String value) /*-{
            this.Proc = value;
        }-*/;

        public final native void setProj(String value) /*-{
            this.Proj = value;
        }-*/;
    }

    private static class TimeOfDayLimits
        extends JavaScriptObject
    {

        //~ Constructors -------------------------------------------------------

        protected TimeOfDayLimits()
        {
        }

        //~ Methods ------------------------------------------------------------

        public final native String getMax(int hour) /*-{
            return this[hour+"-max" ];
        }-*/;

        public final native String getMin(int hour) /*-{
            return this[hour+"-min"];
        }-*/;

        public final native void setMax(
                int    hour,
                String value) /*-{
            this[hour+"-max"] = value;
        }-*/;

        public final native void setMin(
                int    hour,
                String value) /*-{
            this[hour+"-min"] = value;
        }-*/;
    }
}
