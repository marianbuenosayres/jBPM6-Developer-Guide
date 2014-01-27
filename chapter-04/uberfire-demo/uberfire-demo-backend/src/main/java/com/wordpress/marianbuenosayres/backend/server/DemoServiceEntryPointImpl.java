package com.wordpress.marianbuenosayres.backend.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.wordpress.marianbuenosayres.api.service.DemoServiceEntryPoint;

import org.jboss.errai.bus.server.annotations.Service;

@Service
@ApplicationScoped
public class DemoServiceEntryPointImpl implements DemoServiceEntryPoint {

    private List<String> messages = new ArrayList<String>();

    @Override
    public List<String> getMessages() {
        return messages;
    }

    @Override
    public void sendMessage(String message) {
        messages.add(message);
    }
}
