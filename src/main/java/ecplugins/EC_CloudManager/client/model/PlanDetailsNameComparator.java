package ecplugins.EC_CloudManager.client.model;

import java.util.Comparator;

public class PlanDetailsNameComparator
    implements Comparator<PlanDetails>
{

    //~ Methods ------------------------------------------------------------

    @Override public int compare(
            PlanDetails o1,
            PlanDetails o2)
    {
        return o1.getName()
                 .compareToIgnoreCase(o2.getName());
    }
}
