package ecplugins.EC_CloudManager.client;

import java.util.Map;

import ecplugins.EC_CloudManager.client.model.PlanDetails;

public class MockPlanDetails
    implements PlanDetails
{

    //~ Instance fields ----------------------------------------------------

    private final String m_name;
    private final String m_description;

    //~ Constructors -------------------------------------------------------

    public MockPlanDetails(
            String name,
            String description)
    {
        m_description = description;
        m_name        = name;
    }

    //~ Methods ------------------------------------------------------------

    @Override public String getActive()
    {
        return null;
    }

    @Override public String getAdjustPlugin()
    {
        return null;
    }

    @Override public String getCostMax()
    {
        return null;
    }

    @Override public String getCostPeriod()
    {
        return null;
    }

    @Override public String getDebug()
    {
        return null;
    }

    public String getDescription()
    {
        return m_description;
    }

    @Override public Map<String, String> getGrowConfig()
    {
        return null;
    }

    @Override public String getKillLimitMax()
    {
        return null;
    }

    @Override public String getKillLimitMin()
    {
        return null;
    }

    @Override public String getKillLimitPolicy()
    {
        return null;
    }

    public String getName()
    {
        return m_name;
    }

    @Override public String getPoolName()
    {
        return null;
    }

    @Override public Map<String, String> getQueryConfig()
    {
        return null;
    }

    @Override public String getQueryProcedure()
    {
        return null;
    }

    @Override public String getQueryProject()
    {
        return null;
    }

    @Override public Map<String, String> getShrinkConfig()
    {
        return null;
    }

    @Override public String getTimeOfDayMax(int hour)
    {
        return null;
    }

    @Override public String getTimeOfDayMin(int hour)
    {
        return null;
    }

    @Override public void setActive(String value)
    {
    }

    @Override public void setAdjustPlugin(String value)
    {
    }

    @Override public void setCostMax(String costMax)
    {
    }

    @Override public void setCostPeriod(String costPeriod)
    {
    }

    @Override public void setDebug(String value)
    {
    }

    @Override public void setDescription(String description)
    {
    }

    @Override public void setGrowConfig(Map<String, String> values)
    {
    }

    @Override public void setKillLimitMax(String value)
    {
    }

    @Override public void setKillLimitMin(String value)
    {
    }

    @Override public void setKillLimitPolicy(String value)
    {
    }

    @Override public void setName(String name)
    {
    }

    @Override public void setPoolName(String poolName)
    {
    }

    @Override public void setQueryConfig(Map<String, String> values)
    {
    }

    @Override public void setQueryProcedure(String value)
    {
    }

    @Override public void setQueryProject(String value)
    {
    }

    @Override public void setShrinkConfig(Map<String, String> values)
    {
    }

    @Override public void setTimeOfDayLimit(
            int    hour,
            String min,
            String max)
    {
    }
}
