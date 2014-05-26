package com.wordpress.marianbuenosayres.service;

import java.net.URL;
import java.util.HashMap;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

public class SignalEventAppWS {

	/**
	 * In order to invoke this class, you should:
	 * - Install the kie-wb-edition WAR file inside a JBoss7 (at standalone/deployments/kie-wb.war)
	 * - Start the JBoss7 server with bin/standalone.sh --server-config=standalone-full.xml
	 * - Deploy the HR project in the jbpm-playground default repository
	 * - Then run this test
	 */
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
			System.out.println("Creating web service client...");
			URL wsdlLocation = new URL("http://localhost:8080/kie-wb/RuntimeManagerWebServiceImpl?wsdl");
			QName serviceName = new QName("com.wordpress.marianbuenosayres.service", "RuntimeManagerWebServiceImplService");
			Service service = Service.create(wsdlLocation, serviceName);
			RuntimeManagerWebService client = service.getPort(RuntimeManagerWebService.class);
			System.out.println("Web service created!");
	
			System.out.println("Invoking web service...");
			HashMap<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("userId", "krisv");
			client.signalEventAll("org.jbpm:HR:1.0", "my-signal");
			System.out.println("Web service invoked!");
		} catch (Exception e) {
			System.out.println("An error has occurred: " + e.getMessage());
			e.printStackTrace(System.out);
		}
	}
}
