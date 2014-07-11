package com.wordpress.marianbuenosayres.demo.client.editors.newmessage;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchPopup;
import org.uberfire.client.mvp.UberView;
import org.uberfire.lifecycle.OnOpen;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.client.workbench.events.BeforeClosePlaceEvent;

import com.wordpress.marianbuenosayres.api.service.DemoServiceEntryPoint;
import com.wordpress.marianbuenosayres.api.model.events.NewMessageEvent;

@Dependent
@WorkbenchPopup(identifier = "BookDemoMessagePopup")
public class NewMessagePresenter {

    public interface NewMessageView extends UberView<NewMessagePresenter> {
    	
        String getMessage();
    }

    @Inject
    NewMessageView view;

    @Inject
    private Caller<DemoServiceEntryPoint> demoService;
    @Inject
    private Event<NewMessageEvent> newMsgEvent;
    @Inject
    private Event<BeforeClosePlaceEvent> closePlaceEvent;

    private PlaceRequest place;

    public NewMessagePresenter() {
    }

    @PostConstruct
    public void init() {
        
    }

    @OnStartup
    public void onStartup( final PlaceRequest place ) {
        this.place = place;
    }

    @OnOpen
    public void onOpen() {
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return "Book Demo Message Popup";
    }

    @WorkbenchPartView
    public UberView<NewMessagePresenter> getView() {
        return view;
    }

    public void sendMessage(String message) {
        this.demoService.call( new RemoteCallback<Void>() {
            @Override
            public void callback( Void response ) {
                //send event
                newMsgEvent.fire(new NewMessageEvent( view.getMessage() ) );
                close();
            }
        } ).sendMessage( message );
    }

    public void close() {
        closePlaceEvent.fire( new BeforeClosePlaceEvent( this.place ) );
    }
}
