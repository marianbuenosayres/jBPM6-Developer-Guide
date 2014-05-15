package com.wordpress.marianbuenosayres.demo.client.editors.messagelist;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.mvp.UberView;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.Menus;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.wordpress.marianbuenosayres.demo.client.i18n.Constants;
import com.wordpress.marianbuenosayres.api.service.DemoServiceEntryPoint;

@Dependent
@WorkbenchScreen(identifier = "uberFireDemo.MessageListScreen")
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
    
    @Inject
    private Caller<DemoServiceEntryPoint> demoService;

    private ListDataProvider<String> dataProvider = new ListDataProvider<String>();

    public MessageListPresenter() {
        makeMenuBar();
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return "My Customized Message List";
    }

    @WorkbenchPartView
    public UberView<MessageListPresenter> getView() {
        return view;
    }

    public void refreshMessages() {
        demoService.call(new RemoteCallback<List<String>>() {
            @Override
            public void callback(List<String> messages) {
                dataProvider.setList(messages);
                dataProvider.refresh();
            }
        }).getMessages();
    }

    public void init() {
        refreshMessages();
    }

    public ListDataProvider<String> getDataProvider() {
        return dataProvider;
    }

    public void refreshData() {
        dataProvider.refresh();
    }

    @WorkbenchMenu
    public Menus getMenus() {
        return menus;
    }

    private void makeMenuBar() {
        menus = MenuFactory
                .newTopLevelMenu(constants.NewMessage())
                .respondsWith(new Command() {
                    @Override
                    public void execute() {
                        placeManager.goTo( new DefaultPlaceRequest( "uberFireDemo.NewMessagePopup" ) );
                        
                    }
                })
                .endMenu().build();
    }
}
