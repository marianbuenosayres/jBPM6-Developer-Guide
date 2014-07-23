package com.wordpress.marianbuenosayres.demo.client.editors.messagelist;

import java.util.Comparator;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.workbench.events.NotificationEvent;

import com.github.gwtbootstrap.client.ui.Label;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.wordpress.marianbuenosayres.demo.client.i18n.Constants;

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

    private ListHandler<String> sortHandler;
    
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
        
        sortHandler = new ListHandler<String>(presenter.getDataProvider().getList());
        messageListGrid.addColumnSortHandler(sortHandler);

        initTableColumns();

        presenter.addDataDisplay(messageListGrid);
    }

    private void initTableColumns() {
        Column<String, String> messageTextColumn = new Column<String, String>(new TextCell()) {
            @Override
            public String getValue(String string) {
                return string;
            }
        };
        messageTextColumn.setSortable(true);
        sortHandler.setComparator(messageTextColumn, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        messageListGrid.addColumn(messageTextColumn);
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

