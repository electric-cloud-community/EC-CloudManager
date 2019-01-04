
// PlanDetails.java --
//
// PlanDetails.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.model;

import java.util.Map;

public interface PlanDetails
{

    //~ Methods ----------------------------------------------------------------

    String getActive();

    String getAdjustPlugin();

    String getCostMax();

    String getCostPeriod();

    String getDebug();

    String getDescription();

    Map<String, String> getGrowConfig();

    String getKillLimitMax();

    String getKillLimitMin();

    String getKillLimitPolicy();

    String getName();

    String getPoolName();

    Map<String, String> getQueryConfig();

    String getQueryProcedure();

    String getQueryProject();

    Map<String, String> getShrinkConfig();

    String getTimeOfDayMax(int hour);

    String getTimeOfDayMin(int hour);

    void setActive(String value);

    void setAdjustPlugin(String value);

    void setCostMax(String value);

    void setCostPeriod(String value);

    void setDebug(String value);

    void setDescription(String value);

    void setGrowConfig(Map<String, String> values);

    void setKillLimitMax(String value);

    void setKillLimitMin(String value);

    void setKillLimitPolicy(String value);

    void setName(String value);

    void setPoolName(String value);

    void setQueryConfig(Map<String, String> values);

    void setQueryProcedure(String value);

    void setQueryProject(String value);

    void setShrinkConfig(Map<String, String> values);

    void setTimeOfDayLimit(
            int    hour,
            String min,
            String max);
}
