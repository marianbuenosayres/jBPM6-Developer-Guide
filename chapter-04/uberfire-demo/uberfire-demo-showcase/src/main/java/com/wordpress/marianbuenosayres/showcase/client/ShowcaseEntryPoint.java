package com.wordpress.marianbuenosayres.showcase.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import com.wordpress.marianbuenosayres.showcase.client.i18n.Constants;
import org.uberfire.client.UberFirePreferences;
import org.uberfire.client.mvp.AbstractWorkbenchPerspectiveActivity;
import org.uberfire.client.mvp.ActivityManager;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.widgets.menu.WorkbenchMenuBarPresenter;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.security.Identity;
import org.uberfire.security.Role;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.MenuItem;
import org.uberfire.workbench.model.menu.Menus;

@EntryPoint
public class ShowcaseEntryPoint {

    private Constants constants = GWT.create( Constants.class );

    @Inject
    private PlaceManager placeManager;

    @Inject
    private WorkbenchMenuBarPresenter menubar;

    @Inject
    private ActivityManager activityManager;

    @Inject
    private SyncBeanManager iocManager;

    @Inject
    public Identity identity;

    @AfterInitialization
    public void startApp() {
        UberFirePreferences.setProperty( "org.uberfire.client.workbench.widgets.listbar.context.disable", true );
        setupMenu();
        hideLoadingPopup();
    }


    private void setupMenu() {
        final AbstractWorkbenchPerspectiveActivity defaultPerspective = getDefaultPerspectiveActivity();
        final Menus menus = MenuFactory
                .newTopLevelMenu( constants.Home() ).respondsWith( new Command() {
                        @Override
                        public void execute() {
                            if ( defaultPerspective != null ) {
                                placeManager.goTo( new DefaultPlaceRequest( "Message List" ) );
                            } else {
                                Window.alert( "Default perspective not found." );
                            }
                        }
                    } ).endMenu()
                .build();
        menubar.addMenus( menus );
    }

    private List<? extends MenuItem> getRoles() {
        final List<MenuItem> result = new ArrayList<MenuItem>( identity.getRoles().size() );
        for ( final Role role : identity.getRoles() ) {
            if(!role.getName().equals("IS_REMEMBER_ME")){
                result.add( MenuFactory.newSimpleItem( constants.Role() +": " + role.getName() ).endMenu().build().getItems().get( 0 ) );
            }
        }

        result.add( MenuFactory.newSimpleItem( constants.Users() ).respondsWith( new Command() {
            @Override
            public void execute() {
                placeManager.goTo( new DefaultPlaceRequest( "Identity" ) );
            }
        } ).endMenu().build().getItems().get( 0 ) );

        result.add(MenuFactory.newSimpleItem(constants.LogOut()).respondsWith(new Command() {
                    @Override
                    public void execute() {
                        redirect( GWT.getModuleBaseURL() + "uf_logout" );
                    }
                }).endMenu().build().getItems().get(0));
        return result;
    }

    private AbstractWorkbenchPerspectiveActivity getDefaultPerspectiveActivity() {
        AbstractWorkbenchPerspectiveActivity defaultPerspective = null;
        final Collection<IOCBeanDef<AbstractWorkbenchPerspectiveActivity>> perspectives = iocManager
                .lookupBeans( AbstractWorkbenchPerspectiveActivity.class );
        final Iterator<IOCBeanDef<AbstractWorkbenchPerspectiveActivity>> perspectivesIterator = perspectives.iterator();
        outer_loop:
        while ( perspectivesIterator.hasNext() ) {
            final IOCBeanDef<AbstractWorkbenchPerspectiveActivity> perspective = perspectivesIterator.next();
            final AbstractWorkbenchPerspectiveActivity instance = perspective.getInstance();
            if ( instance.isDefault() ) {
                defaultPerspective = instance;
                break outer_loop;
            } else {
                iocManager.destroyBean( instance );
            }
        }
        return defaultPerspective;
    }

    private List<AbstractWorkbenchPerspectiveActivity> getPerspectiveActivities() {

        // Get Perspective Providers
        final Set<AbstractWorkbenchPerspectiveActivity> activities = activityManager
                .getActivities( AbstractWorkbenchPerspectiveActivity.class );

        // Sort Perspective Providers so they're always in the same sequence!
        List<AbstractWorkbenchPerspectiveActivity> sortedActivities = new ArrayList<AbstractWorkbenchPerspectiveActivity>(
                activities );
        Collections.sort( sortedActivities, new Comparator<AbstractWorkbenchPerspectiveActivity>() {
            @Override
            public int compare( AbstractWorkbenchPerspectiveActivity o1,
                                AbstractWorkbenchPerspectiveActivity o2 ) {
                return o1.getPerspective().getName().compareTo( o2.getPerspective().getName() );
            }
        } );

        return sortedActivities;
    }

    // Fade out the "Loading application" pop-up

    private void hideLoadingPopup() {
        final Element e = RootPanel.get( "loading" ).getElement();

        new Animation() {
            @Override
            protected void onUpdate( double progress ) {
                e.getStyle().setOpacity( 1.0 - progress );
            }

            @Override
            protected void onComplete() {
                e.getStyle().setVisibility( Style.Visibility.HIDDEN );
            }
        }.run( 500 );
    }

    public static native void redirect( String url )/*-{
        $wnd.location = url;
    }-*/;

}
