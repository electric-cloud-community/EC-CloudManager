
// CloudListUiHandlers.java --
//
// CloudListUiHandlers.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.view;

import com.gwtplatform.mvp.client.UiHandlers;

import ecplugins.EC_CloudManager.client.model.PlanDetails;

public interface PlanListUiHandlers
    extends UiHandlers
{

    //~ Methods ----------------------------------------------------------------

    void refreshList();

    void showDeleteDialog(PlanDetails cloud);
    
    void showCopyDialog(PlanDetails cloud);

    void configure();
}
