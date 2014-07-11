package com.wordpress.marianbuenosayres.api.model.events;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class NewMessageEvent {

    private String message;

    public NewMessageEvent() {
    }

    public NewMessageEvent(String message) {
        this();
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
