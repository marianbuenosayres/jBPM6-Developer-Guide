package com.wordpress.marianbuenosayres.demo.client.editors.messagelist;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.mvp.UberView;
import org.uberfire.lifecycle.OnOpen;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.Menus;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.wordpress.marianbuenosayres.api.model.events.NewMessageEvent;
import com.wordpress.marianbuenosayres.api.service.DemoServiceEntryPoint;
import com.wordpress.marianbuenosayres.demo.client.i18n.Constants;

@Dependent
@WorkbenchScreen(identifier = "BookDemoMessageScreen")
public class MessageListPresenter {

    public interface MessageListView extends UberView<MessageListPresenter> {

        void displayNotification(String text);

        DataGrid<String> getDataGrid();
    }
    
    private Constants constants = GWT.create( Constants.class );
    
    @Inject
    private PlaceManager placeManager;

    @Inject
    private MessageListView view;
    
    private Menus menus;

    private PlaceRequest place;
    
    @Inject
    private Caller<DemoServiceEntryPoint> demoService;

    private ListDataProvider<String> dataProvider = new ListDataProvider<String>();

    public MessageListPresenter() {
        makeMenuBar();
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return "Book Demo Message Screen";
    }

    @WorkbenchPartView
    public UberView<MessageListPresenter> getView() {
        return view;
    }

    @PostConstruct
    public void init() {
        
    }

    @OnStartup
    public void onStartup(final PlaceRequest place) {
        this.place = place;
    }


    public void refreshMessages() {
        demoService.call(new RemoteCallback<List<String>>() {
            @Override
            public void callback(List<String> messages) {
                dataProvider.setList(messages);
                dataProvider.refresh();
            }
        }, new ErrorCallback<Message>() {
            @Override
            public boolean error(Message message, Throwable throwable) {
                view.displayNotification("Error: Reading the list...");
                return true;
            }
	}).getMessages();
    }

    @OnOpen
    public void onOpen() {
        refreshMessages();
    }

    public ListDataProvider<String> getDataProvider() {
        return dataProvider;
    }

    public void addDataDisplay(HasData<String> display) {
        dataProvider.addDataDisplay(display);
    }

    public void refreshData() {
        dataProvider.refresh();
    }

    @WorkbenchMenu
    public Menus getMenus() {
        return menus;
    }

    public void requestCreated( @Observes NewMessageEvent event ) {
        refreshMessages();
    }

    private void makeMenuBar() {
        menus = MenuFactory.newTopLevelMenu(constants.NewMessage()).respondsWith(new Command() {
            @Override
            public void execute() {
                placeManager.goTo( new DefaultPlaceRequest( "BookDemoMessagePopup" ) );
            }
        })
        .endMenu().build();
    }
}
