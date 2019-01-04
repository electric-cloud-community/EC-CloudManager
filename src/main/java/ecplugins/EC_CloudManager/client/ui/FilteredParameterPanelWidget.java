
// FilteredParameterPanelWidget.java --
//
// FilteredParameterPanelWidget.java is part of ElectricCommander.
//
// Copyright (c) 2005-2012 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;

import com.electriccloud.commander.client.CommanderRequestManager;
import com.electriccloud.commander.client.domain.FormalParameter;

import ecinternal.client.ui.InternalUIFactory;
import ecinternal.client.ui.impl.ParameterPanelWidgetImpl;

public class FilteredParameterPanelWidget
    extends ParameterPanelWidgetImpl
{

    //~ Instance fields --------------------------------------------------------

    private Set<String> m_excludedParameters;

    //~ Constructors -----------------------------------------------------------

    @Inject public FilteredParameterPanelWidget(
            CommanderRequestManager requestManager,
            InternalUIFactory       uiFactory)
    {
        super(requestManager, uiFactory);
    }

    //~ Methods ----------------------------------------------------------------

    @Override public void handleResponse(List<FormalParameter> response)
    {
        List<FormalParameter> parameters = new ArrayList<FormalParameter>();

        for (FormalParameter parameter : response) {

            if (!m_excludedParameters.contains(parameter.getName())) {
                parameters.add(parameter);
            }
        }

        super.handleResponse(parameters);
    }

    public void setExcludedParameters(Set<String> excludedParameters)
    {
        m_excludedParameters = excludedParameters;
    }
}
