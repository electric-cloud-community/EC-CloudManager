
// CloudManagerPlaceManager.java --
//
// CloudManagerPlaceManager.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client;

import javax.inject.Inject;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;

import com.gwtplatform.mvp.client.proxy.PlaceManagerImpl;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TokenFormatter;

import ecplugins.EC_CloudManager.client.model.PlanDetails;

import com.electriccloud.commander.gwt.client.util.CommanderUrlBuilder;

import static com.electriccloud.commander.gwt.client.util.CommanderUrlBuilder.createLinkUrl;

public class CloudManagerPlaceManager
    extends PlaceManagerImpl
{

    //~ Static fields/initializers ---------------------------------------------

    @NonNls public static final String PLAN_NAME = "planName";

    //~ Constructors -----------------------------------------------------------

    @Inject public CloudManagerPlaceManager(
            EventBus       eventBus,
            TokenFormatter tokenFormatter)
    {
        super(eventBus, tokenFormatter);
    }

    //~ Methods ----------------------------------------------------------------

    public SafeUri generatePlanUrl(
            PlanDetails      plan,
            @Nullable String token)
    {
        return UriUtils.fromString("#"
                + buildHistoryToken(new PlaceRequest(token).with(PLAN_NAME,
                        plan.getName())));
    }

    public SafeUri generateResourceUrl(String resourceName)
    {
        CommanderUrlBuilder urlBuilder = createLinkUrl("editResource",
                resourceName);

        return urlBuilder.setParameter("redirectTo",
            CommanderUrlBuilder.createRedirectUrl()
                               .buildString());
    }

    @Override public void revealDefaultPlace()
    {
        revealPlace(new PlaceRequest(NameTokens.listPage), false);
    }
}
