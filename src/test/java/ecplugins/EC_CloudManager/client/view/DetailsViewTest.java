
// DetailsViewTest.java --
//
// DetailsViewTest.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.view;

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.gwt.cell.client.HasCell;
import com.google.gwt.core.client.GWT;

import com.googlecode.gwt.test.GwtTestWithMockito;

import ecplugins.EC_CloudManager.client.Constants;
import ecplugins.EC_CloudManager.client.model.Deployment;

import static ecplugins.EC_CloudManager.client.view.DetailsView.DAYS;
import static ecplugins.EC_CloudManager.client.view.DetailsView.DeploymentDurationColumn;
import static ecplugins.EC_CloudManager.client.view.DetailsView.HOURS;
import static ecplugins.EC_CloudManager.client.view.DetailsView.MINUTES;
import static ecplugins.EC_CloudManager.client.view.DetailsView.SECONDS;

public class DetailsViewTest
    extends GwtTestWithMockito
{

    //~ Instance fields --------------------------------------------------------

    private HasCell<Deployment, String> m_duration;

    // Mocked objects
    @Mock private Deployment m_deployment;

    //~ Methods ----------------------------------------------------------------

    @Before public void setup()
    {
        Constants constants = GWT.create(Constants.class);

        m_duration = new DeploymentDurationColumn(constants);
    }

    @Test public void testDeploymentDurationColumn_days()
    {
        long now   = new Date().getTime();
        long delta = 5 * DAYS + 6 * HOURS + 43 * MINUTES + 24 * SECONDS + 123;


        Mockito.when(m_deployment.getStart())
               .thenReturn(now - delta);
        Assert.assertEquals("duration", "5d 6h 43m 24s",
            m_duration.getValue(m_deployment));
    }

    @Test public void testDeploymentDurationColumn_hms()
    {
        long now   = new Date().getTime();
        long delta = 3 * HOURS + 43 * MINUTES + 24 * SECONDS + 123;

        Mockito.when(m_deployment.getStart())
               .thenReturn(now - delta);
        Assert.assertEquals("duration", "3h 43m 24s",
            m_duration.getValue(m_deployment));
    }

    @Override public String getModuleName()
    {
        return "ecplugins.EC_CloudManager.CloudManager";
    }
}
