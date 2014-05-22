package com.wordpress.marianbuenosayres.api.service;

import java.util.List;

import org.jboss.errai.bus.server.annotations.Remote;

@Remote
public interface DemoServiceEntryPoint {

    public List<String> getMessages();

    public void sendMessage(String message);

}
