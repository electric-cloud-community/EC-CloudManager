
// TimeOfDayLimit.java --
//
// TimeOfDayLimit.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.model;

public class TimeOfDayLimit
{

    //~ Instance fields --------------------------------------------------------

    private String m_min;
    private String m_max;
    private int    m_start;
    private int    m_end;

    //~ Constructors -----------------------------------------------------------

    public TimeOfDayLimit(
            int    start,
            int    end,
            String min,
            String max)
    {
        m_start = start;
        m_end   = end;
        m_max   = max;
        m_min   = min;
    }

    //~ Methods ----------------------------------------------------------------

    public int getEnd()
    {
        return m_end;
    }

    public String getMax()
    {
        return m_max;
    }

    public String getMin()
    {
        return m_min;
    }

    public int getStart()
    {
        return m_start;
    }

    public void setEnd(int end)
    {
        m_end = end;
    }

    public void setMax(String max)
    {
        m_max = max;
    }

    public void setMin(String min)
    {
        m_min = min;
    }

    public void setStart(int start)
    {
        m_start = start;
    }
}
