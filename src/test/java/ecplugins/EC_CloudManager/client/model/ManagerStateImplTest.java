
// ManagerStateImplTest.java --
//
// ManagerStateImplTest.java is part of ElectricCommander.
//
// Copyright (c) 2005-2013 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.gwt.test.FinallyCommandTrigger;

import ecplugins.EC_CloudManager.client.MockPlanDetails;
import ecplugins.EC_CloudManager.client.MockProviderDetails;
import ecplugins.EC_CloudManager.client.jtest.CMTestBase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import static com.googlecode.gwt.test.FinallyCommandTrigger.triggerCommands;

@SuppressWarnings({"HardCodedStringLiteral"})
public class ManagerStateImplTest
    extends CMTestBase
{

    //~ Instance fields --------------------------------------------------------

    private ManagerStateImpl m_managerState;

    //~ Methods ----------------------------------------------------------------

    @Test public void testChangePlan_add()
        throws Exception
    {
        PlanDetails plan    = new MockPlanDetails("foo", "descr");
        PlanDetails newPlan = new MockPlanDetails("bar", "descr2");

        m_managerState.changePlan("", plan);

        List<PlanDetails> list = m_managerState.getPlans()
                                               .getList();

        assertEquals("one entry", 1, list.size());
        assertTrue("plan added", list.contains(plan));
        m_managerState.changePlan("", newPlan);
        FinallyCommandTrigger.triggerCommands();
        assertEquals("two entries", 2, list.size());
        assertTrue("original still exists", list.contains(plan));
        assertTrue("new plan added", list.contains(newPlan));
    }

    @Test public void testChangePlan_rename()
        throws Exception
    {
        PlanDetails plan    = new MockPlanDetails("foo", "descr");
        PlanDetails newPlan = new MockPlanDetails("bar", "descr2");

        m_managerState.changePlan("", plan);
        m_managerState.changePlan("foo", newPlan);
        triggerCommands();

        List<PlanDetails> list = m_managerState.getPlans()
                                               .getList();

        assertEquals("one entry", 1, list.size());
        assertTrue("plan renamed", list.contains(newPlan));
    }

    @Test public void testChangePlan_update()
        throws Exception
    {
        PlanDetails plan    = new MockPlanDetails("foo", "descr");
        PlanDetails newPlan = new MockPlanDetails("foo", "descr2");

        m_managerState.changePlan("", plan);
        m_managerState.changePlan("foo", newPlan);
        triggerCommands();

        List<PlanDetails> list = m_managerState.getPlans()
                                               .getList();

        assertEquals("one entry", 1, list.size());
        assertTrue("plan updated", list.contains(newPlan));
    }

    @Test public void testFindPlanByName_found()
        throws Exception
    {
        PlanDetails plan1 = new MockPlanDetails("foo", "descr");
        PlanDetails plan2 = new MockPlanDetails("bar", "descr2");

        m_managerState.setData(new ArrayList<PlanDetails>(
                Arrays.asList(plan1, plan2)), new ArrayList<ProviderDetails>());
        triggerCommands();
        assertEquals("plan1", plan1, m_managerState.findPlanByName("foo"));
        assertEquals("plan2", plan2, m_managerState.findPlanByName("bAR"));
    }

    @Test public void testFindPlanByName_notFound()
        throws Exception
    {
        m_managerState.setData(new ArrayList<PlanDetails>(),
            new ArrayList<ProviderDetails>());
        triggerCommands();
        assertNull("no plan found", m_managerState.findPlanByName("foo"));
    }

    @Test public void testFindPlanByName_notLoaded()
        throws Exception
    {
        assertNull("no plan found", m_managerState.findPlanByName("foo"));
    }

    @Test public void testFindProviderByName_found()
        throws Exception
    {
        ProviderDetails provider1 = new MockProviderDetails("foo");
        ProviderDetails provider2 = new MockProviderDetails("bar");

        m_managerState.setData(new ArrayList<PlanDetails>(),
            new ArrayList<ProviderDetails>(
                Arrays.asList(provider1, provider2)));
        triggerCommands();
        assertEquals("Provider1", provider1,
            m_managerState.findProviderByName("foo"));
        assertEquals("provider2", provider2,
            m_managerState.findProviderByName("bAR"));
    }

    @Test public void testFindProviderByName_notFound()
        throws Exception
    {
        m_managerState.setData(new ArrayList<PlanDetails>(),
            new ArrayList<ProviderDetails>());
        triggerCommands();
        assertNull("no Provider found",
            m_managerState.findProviderByName("foo"));
    }

    @Test public void testFindProviderByName_notLoaded()
        throws Exception
    {
        assertNull("no Provider found",
            m_managerState.findProviderByName("foo"));
    }

    @Test public void testgetPlanIndex_notFound()
        throws Exception
    {
        m_managerState.setData(new ArrayList<PlanDetails>(),
            new ArrayList<ProviderDetails>());
        triggerCommands();
        assertEquals("no plan found", -1, m_managerState.getPlanIndex("foo"));
    }

    @Test public void testgetPlanIndex_notLoaded()
        throws Exception
    {
        assertEquals("no plan found", -1, m_managerState.getPlanIndex("foo"));
    }

    @Test public void testGetPlanIndexByName_found()
        throws Exception
    {
        PlanDetails plan1 = new MockPlanDetails("foo", "descr");
        PlanDetails plan2 = new MockPlanDetails("bar", "descr2");

        m_managerState.setData(new ArrayList<PlanDetails>(
                Arrays.asList(plan1, plan2)), new ArrayList<ProviderDetails>());
        triggerCommands();
        assertEquals("plan1", 0, m_managerState.getPlanIndex("foo"));
        assertEquals("plan2", 1, m_managerState.getPlanIndex("bAR"));
    }

    @Test public void testSetData()
        throws Exception
    {
        PlanDetails     plan1     = new MockPlanDetails("foo", "descr");
        PlanDetails     plan2     = new MockPlanDetails("bar", "descr2");
        ProviderDetails provider1 = new MockProviderDetails("foo");
        ProviderDetails provider2 = new MockProviderDetails("bar");

        m_managerState.setData(new ArrayList<PlanDetails>(Arrays.asList(plan1)),
            new ArrayList<ProviderDetails>(Arrays.asList(provider1)));
        assertTrue("loaded", m_managerState.isLoaded());

        {
            List<PlanDetails>     plans     = m_managerState.getPlans()
                                                            .getList();
            List<ProviderDetails> providers = m_managerState.getProviders();

            assertEquals("one entry", 1, plans.size());
            assertTrue("plan updated", plans.contains(plan1));
            assertEquals("one entry", 1, providers.size());
            assertTrue("provider updated", providers.contains(provider1));
        }

        m_managerState.setData(new ArrayList<PlanDetails>(Arrays.asList(plan2)),
            new ArrayList<ProviderDetails>(Arrays.asList(provider2)));

        List<PlanDetails>     plans     = m_managerState.getPlans()
                                                        .getList();
        List<ProviderDetails> providers = m_managerState.getProviders();

        assertEquals("one entry", 1, plans.size());
        assertTrue("plan updated", plans.contains(plan2));
        assertEquals("one entry", 1, providers.size());
        assertTrue("provider updated", providers.contains(provider2));
        triggerCommands();
    }

    @Before @Override public void setUp()
    {
        super.setUp();
        m_managerState = new ManagerStateImpl();
    }
}
