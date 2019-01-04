
// PlanEditorUiHandlers.java --
//
// PlanEditorUiHandlers.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.view;

import com.gwtplatform.mvp.client.UiHandlers;

import ecplugins.EC_CloudManager.client.model.TimeOfDayLimit;

public interface PlanEditorUiHandlers
    extends UiHandlers
{

    //~ Methods ----------------------------------------------------------------

    void changeLimit(TimeOfDayLimit value);

    void deleteLimit(TimeOfDayLimit value);

    void handleAddLimit();
}
