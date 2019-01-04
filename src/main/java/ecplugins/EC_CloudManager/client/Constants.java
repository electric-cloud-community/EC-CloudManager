
// Constants.java --
//
// Constants.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client;

import com.google.gwt.i18n.client.Messages;

public interface Constants
    extends Messages
{

    //~ Methods ----------------------------------------------------------------

    String actions();

    String alwaysPolicy();

    String cloudDetails();

    String cloudName();

    String cloudPlans();
    
    String copy();
    
    String copyCloud(String name);

    String copyDialogTitle();

    String copyingCloud(String name);

    String createCloud();

    String delete();

    String deleteCloud(String name);

    String deleteDialogTitle();

    String deletingCloud(String name);

    String description();

    String dhmsFormat(
            long days,
            long hours,
            long minutes,
            long seconds);

    String edit();

    String editCloud();

    String elapsed();

    String handle();

    String hmsFormat(
            long hours,
            long minutes,
            long seconds);

    String time(int hour, int minutes);

    String hourRange(
            int start,
            int end);

    String neverPolicy();

    String resource();

    String started();

    String state();

    String tryPolicy();
}
