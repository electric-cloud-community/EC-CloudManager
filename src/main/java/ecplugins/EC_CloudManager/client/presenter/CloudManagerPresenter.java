
// CloudManagerPresenter.java --
//
// CloudManagerPresenter.java is part of ElectricCommander.
//
// Copyright (c) 2005-2012 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.EC_CloudManager.client.presenter;

import org.jetbrains.annotations.NonNls;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.HasText;

import com.google.inject.Inject;

import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.RevealRootContentEvent;

import com.electriccloud.commander.client.util.StringUtil;

import ecinternal.client.ComponentController;
import ecinternal.client.events.ChangeTitleEvent;
import ecinternal.client.events.ChangeTitleEvent.ChangeTitleHandler;
import ecinternal.client.events.ReturnToListEvent;
import ecinternal.client.presenter.ObjectIteratorPresenter;

import static ecplugins.EC_CloudManager.client.NameTokens.listPage;

public class CloudManagerPresenter
    extends Presenter<CloudManagerPresenter.MyView,
    CloudManagerPresenter.MyProxy>
    implements ChangeTitleHandler,
        ReturnToListEvent.ReturnToListHandler
{

    //~ Static fields/initializers ---------------------------------------------

    /**
     * Use this in leaf presenters, inside their {@link #revealInParent} method.
     */
    @ContentSlot public static final GwtEvent.Type<RevealContentHandler<?>> TYPE_SetMainContent =
        new GwtEvent.Type<RevealContentHandler<?>>();
    @ContentSlot public static final GwtEvent.Type<RevealContentHandler<?>> TYPE_SetIterator    =
        new GwtEvent.Type<RevealContentHandler<?>>();

    //~ Instance fields --------------------------------------------------------

    private final ObjectIteratorPresenter m_iteratorPresenter;
    private final PlaceManager            m_placeManager;
    private final ComponentController     m_controller;

    //~ Constructors -----------------------------------------------------------

    @Inject public CloudManagerPresenter(
            com.google.web.bindery.event.shared.EventBus eventBus,
            MyView                                       view,
            MyProxy                                      proxy,
            ObjectIteratorPresenter                      iteratorPresenter,
            PlaceManager                                 placeManager,
            ComponentController                          controller)
    {
        super(eventBus, view, proxy);
        m_iteratorPresenter = iteratorPresenter;
        m_placeManager      = placeManager;
        m_controller        = controller;
    }

    //~ Methods ----------------------------------------------------------------

    @Override public void onChangeTitle(ChangeTitleEvent event)
    {
        MyView        view        = getView();
        String        title       = StringUtil.nullToEmpty(event.getTitle());
        String        subtitle    = StringUtil.nullToEmpty(event.getSubtitle());
        boolean       hasSubtitle = !subtitle.isEmpty();
        StringBuilder windowTitle = new StringBuilder();

        windowTitle.append(title);

        if (hasSubtitle) {
            windowTitle.append(" - ")
                       .append(subtitle);
        }

        view.getTitle()
            .setText(title);
        view.getSubtitle()
            .setText(subtitle);
        view.setSubtitleVisible(hasSubtitle);
        view.setWindowTitle(windowTitle.toString());
        view.updateShortcut(windowTitle.toString());
    }

    @Override public void onReturnToList(ReturnToListEvent event)
    {
        m_placeManager.revealPlace(new PlaceRequest(listPage));
    }

    @Override protected void onBind()
    {
        super.onBind();
        addRegisteredHandler(ChangeTitleEvent.getType(), this);
        addRegisteredHandler(ReturnToListEvent.getType(), this);
    }

    @Override protected void onReveal()
    {
        super.onReveal();
        setInSlot(TYPE_SetIterator, m_iteratorPresenter, true);
    }

    @Override protected void revealInParent()
    {
        RevealRootContentEvent.fire(this, this);
    }

    @Override public void setInSlot(
            Object             slot,
            PresenterWidget<?> content)
    {
        super.setInSlot(slot, content);

        if (slot == TYPE_SetMainContent) {

            try {
                Presenter<?, ?>       presenter = (Presenter<?, ?>) content;
                Place                 proxy     = (Place) presenter.getProxy();
                @NonNls String        nameToken = proxy.getNameToken();
                String                helpLink  = m_controller.getHelpLink();
                @NonNls
                @SuppressWarnings("DynamicRegexReplaceableByCompiledPattern")
                StringBuilder         href = new StringBuilder(
                        helpLink.replaceFirst("#.*", ""));

                m_controller.setHelpLink(href.append('#')
                                             .append(nameToken)
                                             .toString());
            }
            catch (Exception ignore) {
                // Something went wrong, don't bother updating the help link
            }
        }
    }

    //~ Inner Interfaces -------------------------------------------------------

    /**
     * {@link
     * ecplugins.EC_CloudManager.client.presenter.CloudManagerPresenter}'s
     * proxy.
     */
    @ProxyStandard public interface MyProxy
        extends Proxy<CloudManagerPresenter> { }

    /**
     * {@link
     * ecplugins.EC_CloudManager.client.presenter.CloudManagerPresenter}'s view.
     */
    public interface MyView
        extends View
    {

        //~ Methods ------------------------------------------------------------

        void updateShortcut(String name);

        HasText getSubtitle();

        HasText getTitle();

        void setSubtitleVisible(boolean visible);

        void setWindowTitle(String title);
    }
}
