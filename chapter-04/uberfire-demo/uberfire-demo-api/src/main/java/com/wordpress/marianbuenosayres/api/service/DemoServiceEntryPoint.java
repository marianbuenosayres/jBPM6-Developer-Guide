package com.wordpress.marianbuenosayres.api.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jboss.errai.bus.server.annotations.Remote;

@Remote
public interface DemoServiceEntryPoint {

    public List<String> getMessages();

    public void sendMessage(String message);

}
