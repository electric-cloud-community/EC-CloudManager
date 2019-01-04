
// CommanderException.java --
//
// CommanderException.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.service;

import com.electriccloud.commander.client.responses.CommanderError;

public class CommanderException
    extends Exception
    implements CommanderError
{

    //~ Instance fields --------------------------------------------------------

    private final String m_code;
    private final String m_details;
    private final String m_message;
    private final String m_requestId;
    private final String m_where;

    //~ Constructors -----------------------------------------------------------

    public CommanderException(CommanderError error)
    {
        m_code      = error.getCode();
        m_details   = error.getDetails();
        m_message   = error.getMessage();
        m_requestId = error.getRequestId();
        m_where     = error.getWhere();
    }

    //~ Methods ----------------------------------------------------------------

    @Override public String getCode()
    {
        return m_code;
    }

    @Override public String getDetails()
    {
        return m_details;
    }

    @Override
    @SuppressWarnings({"RefusedBequest"})
    public String getMessage()
    {
        return m_message;
    }

    @Override public String getRequestId()
    {
        return m_requestId;
    }

    @Override public String getWhere()
    {
        return m_where;
    }
}
