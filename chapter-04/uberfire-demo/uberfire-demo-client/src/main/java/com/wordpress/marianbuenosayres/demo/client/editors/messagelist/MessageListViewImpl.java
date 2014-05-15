package com.wordpress.marianbuenosayres.demo.client.editors.messagelist;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.workbench.events.NotificationEvent;

import com.github.gwtbootstrap.client.ui.Label;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.wordpress.marianbuenosayres.demo.client.i18n.Constants;
import com.wordpress.marianbuenosayres.api.model.events.NewMessageEvent;

@Dependent
@Templated(value = "MessageListViewImpl.html")
public class MessageListViewImpl extends Composite implements MessageListPresenter.MessageListView, RequiresResize {

    private Constants constants = GWT.create( Constants.class );

    private MessageListPresenter presenter;

    @Inject
    @DataField
    public LayoutPanel listContainer;
   
    @Inject
    @DataField
    public DataGrid<String> messageListGrid;

    @Inject
    private Event<NotificationEvent> notification;

    public MessageListViewImpl() {
    }
    
    @Override
    public void onResize() {
        if( (getParent().getOffsetHeight()-120) > 0 ){
            listContainer.setHeight(getParent().getOffsetHeight()-120+"px");
        }
    }
    
    @Override
    public void init(final MessageListPresenter presenter ) {
        this.presenter = presenter;
        listContainer.add( messageListGrid );
        // Set the message to display when the table is empty.
        messageListGrid.setEmptyTableWidget( new Label( constants.NoMessages() ) );
    }

    public void requestCreated( @Observes NewMessageEvent event ) {
        presenter.refreshMessages();
    }

    @Override
    public void displayNotification( String text ) {
        notification.fire( new NotificationEvent( text ) );
    }

    @Override
    public DataGrid<String> getDataGrid() {
        return messageListGrid;
    }
}
