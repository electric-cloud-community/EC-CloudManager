
// ManagerStateImpl.java --
//
// ManagerStateImpl.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.model;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.google.gwt.view.client.ListDataProvider;

public class ManagerStateImpl
    implements ManagerState
{

    //~ Instance fields --------------------------------------------------------

    private boolean                             m_loaded;
    private final ListDataProvider<PlanDetails> m_plans     =
        new ListDataProvider<PlanDetails>();
    private List<ProviderDetails>               m_providers;

    //~ Methods ----------------------------------------------------------------

    @Override public void changePlan(
            @NotNull String oldPlanName,
            PlanDetails     plan)
    {
        // Look for the cloud in the list

        // Check for an existing entry.  It may be found under the old
        // or the new name depending on how we get here.
        List<PlanDetails> list  = m_plans.getList();
        boolean           found = false;

        for (int i = 0; i < list.size(); i++) {
            PlanDetails entry = list.get(i);
            String      name  = entry.getName();

            if (name.compareToIgnoreCase(oldPlanName) == 0
                    || name.compareToIgnoreCase(plan.getName()) == 0) {
                list.set(i, plan);
                found = true;

                break;
            }
        }

        if (!found) {

            // Entry not found, add it into the list
            list.add(plan);
        }

        m_plans.flush();
    }

    @Override public PlanDetails findPlanByName(String planName)
    {
        PlanDetails result = null;

        if (m_loaded) {

            for (PlanDetails details : m_plans.getList()) {

                if (details.getName()
                           .compareToIgnoreCase(planName) == 0) {
                    result = details;

                    break;
                }
            }
        }

        return result;
    }

    @Override public ProviderDetails findProviderByName(String providerName)
    {
        ProviderDetails result = null;

        if (m_loaded) {

            for (ProviderDetails details : m_providers) {

                if (details.getName()
                           .compareToIgnoreCase(providerName) == 0) {
                    result = details;

                    break;
                }
            }
        }

        return result;
    }

    @Override public int getPlanIndex(String planName)
    {

        if (m_loaded) {
            List<PlanDetails> plans = m_plans.getList();

            for (int index = 0; index < plans.size(); index++) {
                PlanDetails details = plans.get(index);

                if (details.getName()
                           .compareToIgnoreCase(planName) == 0) {
                    return index;
                }
            }
        }

        return -1;
    }

    @Override public ListDataProvider<PlanDetails> getPlans()
    {
        return m_plans;
    }

    @Override public List<ProviderDetails> getProviders()
    {
        return m_providers;
    }

    @Override public boolean isLoaded()
    {
        return m_loaded;
    }

    @Override public void setData(
            List<PlanDetails>     plans,
            List<ProviderDetails> providers)
    {
        m_loaded = true;

        // Note that we clear and add rather than calling setList to avoid
        // generating a new list instance since ColumnSortEvent.ListHandler
        // is directly refeferring to the list.
        m_plans.getList()
               .clear();
        m_plans.getList()
               .addAll(plans);
        m_providers = new ArrayList<ProviderDetails>(providers);
    }
}
