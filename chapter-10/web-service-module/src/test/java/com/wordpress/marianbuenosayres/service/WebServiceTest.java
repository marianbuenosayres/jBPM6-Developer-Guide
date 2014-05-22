package com.wordpress.marianbuenosayres.service;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;

import org.easymock.EasyMock;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceResponse;

public class WebServiceTest {

	private RuntimeEngine engine;
	private Endpoint server;
	private RuntimeManagerWebService client;
	
	@Before
	public void startUp() throws Exception {
		this.engine = EasyMock.createMock(RuntimeEngine.class);
		this.server = Endpoint.publish(
			"http://localhost:9191/jbpmWebService",
			new RuntimeManagerWebServiceImpl() {
				@Override
				protected RuntimeEngine getRuntimeEngine(String releaseId, Long processInstanceId) {
					return engine;
				}
		});
		
		
		URL wsdlLocation = new URL("http://localhost:9191/jbpmWebService?wsdl");
		QName serviceName = new QName("com.wordpress.marianbuenosayres.service", "Service");
		Service service = Service.create(wsdlLocation, serviceName);
	    this.client = service.getPort(RuntimeManagerWebService.class);
	}
	
	@After
	public void shutDown() {
		this.server.stop();
		this.server = null;
		this.client = null;
		this.engine = null;
	}
	
	@Test
	public void testSignalEvent() {

		KieSession ksession = EasyMock.createMock(KieSession.class);
		EasyMock.expect(engine.getKieSession()).andReturn(ksession);
		ksession.signalEvent(EasyMock.eq("signalRef"), EasyMock.isNull());
		EasyMock.expectLastCall().once();
		
		EasyMock.replay(ksession, this.engine);
		
		client.signalEventAll("org.jbpm:jbpm-playground:1.0", "signalRef");
		
		EasyMock.verify(ksession, this.engine);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testStartProcess() {

		KieSession ksession = EasyMock.createMock(KieSession.class);
		EasyMock.expect(engine.getKieSession()).andReturn(ksession);
		RuleFlowProcessInstance processInstance = new RuleFlowProcessInstance();
		processInstance.setProcessId("Evaluation");
		EasyMock.expect(ksession.startProcess(
				EasyMock.eq("Evaluation"), 
				EasyMock.anyObject(Map.class))).andReturn(processInstance).once();
		
		EasyMock.replay(ksession, this.engine);
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("userId", "krisv");
		JaxbProcessInstanceResponse response = client.startProcess(
				"org.jbpm:jbpm-playground:1.0", "Evaluation", parameters);
		
		EasyMock.verify(ksession, this.engine);
		
		Assert.assertEquals("Evaluation", response.getProcessId());
	}
}
