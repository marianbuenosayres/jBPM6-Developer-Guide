package com.wordpress.marianbuenosayres.service;

import javax.jms.Connection;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.naming.InitialContext;

import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.services.client.api.RemoteJmsRuntimeEngineFactoryBuilderImpl;

public class SignalEventAppJMS {

    public static void main(String[] args) {
        if (args.length == 1 && ("-h".equals(args[0]) || "--help".equals(args[0]))) {
            System.out.println("In order to invoke this class, you should:");
            System.out.println(" - Install the kie-wb-edition WAR file inside a JBoss7 (at standalone/deployments/kie-wb.war)");
            System.out.println(" - Start the JBoss7 server with bin/standalone.sh --server-config=standalone-full.xml");
            System.out.println(" - Deploy the HR project in the jbpm-playground default repository");
            System.out.println(" - Make sure you have a user called mariano, with password mypass, and roles admin and guest");
            System.out.println(" - Then run this test");
            System.exit(-1);
        }
        try {
            System.out.println("Creating JMS context ...");
            InitialContext ctx = new InitialContext();
            QueueConnectionFactory connFactory = (QueueConnectionFactory) ctx.lookup("jms/RemoteConnectionFactory");
            Connection conn = connFactory.createConnection("mariano", "mypass");
            Session session = conn.createSession(true, Session.AUTO_ACKNOWLEDGE);
            Queue ksessionQueue = (Queue) ctx.lookup("jms/queue/KIE.SESSION");
            Queue taskQueue = (Queue) ctx.lookup("jms/queue/KIE.TASK");
            Queue responseQueue = (Queue) ctx.lookup("jms/queue/KIE.RESPONSE");
            System.out.println("Creating JMS Runtime Factory...");
            
            RuntimeEngine engine = RemoteJmsRuntimeEngineFactoryBuilderImpl.newBuilder().
            	addDeploymentId("org.jbpm:HR:1.0").addConnectionFactory(connFactory).
        	addKieSessionQueue(ksessionQueue).addTaskServiceQueue(taskQueue).
        	addResponseQueue(responseQueue).addUserName("mariano").addPassword("mypass").
        	build().newRuntimeEngine();

            engine.getKieSession().signalEvent("my-signal", "");
        } catch (Exception e) {
            System.out.println("An error has occurred: " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }
}
