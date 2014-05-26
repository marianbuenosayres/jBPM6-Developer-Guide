package com.wordpress.marianbuenosayres.service;

import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
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
            System.out.println(" - Then run this test");
            System.exit(-1);
        }
        try {
            System.out.println("Creating JMS context ...");
            QueueConnectionFactory connFactory = (QueueConnectionFactory) new InitialContext().lookup("jms/ConnectionFactory");
            QueueConnection qconnection = connFactory.createQueueConnection();
            QueueSession qsession = qconnection.createQueueSession(true, 1);
            System.out.println("Creating JMS Runtime Factory...");
            
            RuntimeEngine engine = RemoteJmsRuntimeEngineFactoryBuilderImpl.newBuilder().
            	addDeploymentId("org.jbpm:HR:1.0").
            	addConnectionFactory(connFactory).
        		addKieSessionQueue(qsession.createQueue("jms/queue/KIE.SESSION")).
        		addTaskServiceQueue(qsession.createQueue("jms/queue/KIE.TASK")).
        		addUserName("mariano").addPassword("mypass")
        	.build().newRuntimeEngine();

            engine.getKieSession().signalEvent("my-signal", null);
        } catch (Exception e) {
            System.out.println("An error has occurred: " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }
}
