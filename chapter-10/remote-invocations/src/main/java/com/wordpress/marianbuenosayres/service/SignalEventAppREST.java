package com.wordpress.marianbuenosayres.service;

import java.net.URL;

import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.services.client.api.RemoteRestRuntimeEngineFactoryBuilderImpl;

public class SignalEventAppREST {

    public static void main(String[] args) {
        if (args.length == 1 && ("-h".equals(args[0]) || "--help".equals(args[0]))) {
            System.out.println("In order to invoke this class, you should:");
            System.out.println(" - Install the kie-wb-edition WAR file inside a JBoss7 (at standalone/deployments/kie-wb.war)");
            System.out.println(" - Start the JBoss7 server with bin/standalone.sh --server-config=standalone-full.xml");
            System.out.println(" - Deploy the HR project in the jbpm-playground default repository");
            System.out.println(" - Then run this test");
            System.exit(-1);
        }
        try {
            System.out.println("Creating REST Runtime Factory...");
            RuntimeEngine engine = RemoteRestRuntimeEngineFactoryBuilderImpl.newBuilder().
            	addUrl(new URL("http://localhost:8080/kie-wb")).
                addDeploymentId("org.jbpm:HR:1.0").
            	addUserName("mariano").addPassword("mypass").
            	build().newRuntimeEngine();
            engine.getKieSession().signalEvent("my-signal", null);
        } catch (Exception e) {
            System.out.println("An error has occurred: " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }
}
