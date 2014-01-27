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

import com.wordpress.marianbuenosayres.api.model.events.NewMessageEvent;
import com.wordpress.marianbuenosayres.api.service.DemoServiceEntryPoint;

@Dependent
@WorkbenchPopup(identifier = "New Message")
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

    public NewMessagePresenter() {
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return "New Message";
    }

    @WorkbenchPartView
    public UberView<NewMessagePresenter> getView() {
        return view;
    }

    @PostConstruct
    public void init() {
    }

    public void sendMessage(String message) {
        this.demoService.call( new RemoteCallback<Void>() {
            @Override
            public void callback( Void response ) {
                //send event
                newMsgEvent.fire(new NewMessageEvent( view.getMessage() ) );
            }
        } ).sendMessage( message );
    }

}
