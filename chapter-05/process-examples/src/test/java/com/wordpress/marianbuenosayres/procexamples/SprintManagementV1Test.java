package com.wordpress.marianbuenosayres.procexamples;

import com.wordpress.marianbuenosayres.handlers.ContinueWorkItemHandler;
import com.wordpress.marianbuenosayres.handlers.ErrorWorkItemHandler;
import com.wordpress.marianbuenosayres.handlers.TestAsyncWorkItemHandler;

import java.util.HashMap;
import java.util.Map;

import org.drools.core.WorkingMemory;
import org.drools.core.event.ActivationCancelledEvent;
import org.drools.core.event.ActivationCreatedEvent;
import org.drools.core.event.AfterActivationFiredEvent;
import org.drools.core.event.AgendaEventListener;
import org.drools.core.event.AgendaGroupPoppedEvent;
import org.drools.core.event.AgendaGroupPushedEvent;
import org.drools.core.event.BeforeActivationFiredEvent;
import org.drools.core.event.RuleFlowGroupActivatedEvent;
import org.drools.core.event.RuleFlowGroupDeactivatedEvent;
import org.drools.core.impl.StatefulKnowledgeSessionImpl;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.internal.io.ResourceFactory;

public class SprintManagementV1Test {

	@Test
	public void testProcessV1HappyPath() throws Exception {
		KieSession ksession = createKieSession();
		
		TestAsyncWorkItemHandler devHandler = new TestAsyncWorkItemHandler();
		TestAsyncWorkItemHandler compileHandler = new TestAsyncWorkItemHandler();
		TestAsyncWorkItemHandler deployHandler = new TestAsyncWorkItemHandler();
		ksession.getWorkItemManager().registerWorkItemHandler("Human Task", devHandler);
		ksession.getWorkItemManager().registerWorkItemHandler("compiler", compileHandler);
		ksession.getWorkItemManager().registerWorkItemHandler("deployer", deployHandler);
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("project", "MyProject");
		params.put("reqDescription", "My new requirement");
		ProcessInstance instance = ksession.startProcess("sprintManagement-V1", params);
		Assert.assertNotNull(instance);
		Assert.assertEquals(ProcessInstance.STATE_ACTIVE, instance.getState());
		
		WorkItem item1 = devHandler.getItem();
		Assert.assertNotNull(item1);
		Map<String, Object> results1 = new HashMap<String, Object>();
		results1.put("ActorId", "my-actor");
		ksession.getWorkItemManager().completeWorkItem(item1.getId(), results1);
		Assert.assertEquals(ProcessInstance.STATE_ACTIVE, instance.getState());
		
		WorkItem item2 = compileHandler.getItem();
		Assert.assertNotNull(item2);
		Map<String, Object> results2 = new HashMap<String, Object>();
		results2.put("isCompilationOk", Boolean.TRUE);
		ksession.getWorkItemManager().completeWorkItem(item2.getId(), results2);
		Assert.assertEquals(ProcessInstance.STATE_ACTIVE, instance.getState());
		
		WorkItem item3 = deployHandler.getItem();
		Assert.assertNotNull(item3);
		Map<String, Object> results3 = new HashMap<String, Object>();
		results3.put("isDeploymentOk", Boolean.TRUE);
		ksession.getWorkItemManager().completeWorkItem(item3.getId(), results3);
		Assert.assertEquals(ProcessInstance.STATE_COMPLETED, instance.getState());
	}
	
	@Test
	public void testProcessV1CompileError() throws Exception {
		KieSession ksession = createKieSession();
		
		TestAsyncWorkItemHandler devHandler = new TestAsyncWorkItemHandler();
		ErrorWorkItemHandler errHandler = new ErrorWorkItemHandler();
		TestAsyncWorkItemHandler notifHandler = new TestAsyncWorkItemHandler();
		ksession.getWorkItemManager().registerWorkItemHandler("Human Task", devHandler);
		ksession.getWorkItemManager().registerWorkItemHandler("compiler", errHandler);
		ksession.getWorkItemManager().registerWorkItemHandler("notifier", notifHandler);
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("project", "MyProject");
		params.put("reqDescription", "My new requirement");
		ProcessInstance instance = ksession.startProcess("sprintManagement-V1", params);
		Assert.assertNotNull(instance);
		Assert.assertEquals(ProcessInstance.STATE_ACTIVE, instance.getState());
		
		WorkItem item = devHandler.getItem();
		Assert.assertNotNull(item);
		Map<String, Object> results1 = new HashMap<String, Object>();
		results1.put("ActorId", "my-actor");
		ksession.getWorkItemManager().completeWorkItem(item.getId(), results1);
		
		WorkItem item2 = notifHandler.getItem();
		Assert.assertNotNull(item2);
		ksession.getWorkItemManager().completeWorkItem(item2.getId(), null);
		
		Assert.assertEquals(ProcessInstance.STATE_ACTIVE, instance.getState());
		
		WorkItem item3 = devHandler.getItem();
		Assert.assertNotNull(item3);
	}
	
	@Test
	public void testProcessV1DeployError() throws Exception {
		KieSession ksession = createKieSession();
		
		TestAsyncWorkItemHandler devHandler = new TestAsyncWorkItemHandler();
		ErrorWorkItemHandler errHandler = new ErrorWorkItemHandler();
		ContinueWorkItemHandler continueHandler = new ContinueWorkItemHandler();
		TestAsyncWorkItemHandler notifHandler = new TestAsyncWorkItemHandler();
		ksession.getWorkItemManager().registerWorkItemHandler("Human Task", devHandler);
		ksession.getWorkItemManager().registerWorkItemHandler("compiler", continueHandler);
		ksession.getWorkItemManager().registerWorkItemHandler("deployer", errHandler);
		ksession.getWorkItemManager().registerWorkItemHandler("notifier", notifHandler);
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("project", "MyProject");
		params.put("reqDescription", "My new requirement");
		ProcessInstance instance = ksession.startProcess("sprintManagement-V1", params);
		Assert.assertNotNull(instance);
		Assert.assertEquals(ProcessInstance.STATE_ACTIVE, instance.getState());
		
		WorkItem item = devHandler.getItem();
		Assert.assertNotNull(item);
		Map<String, Object> results1 = new HashMap<String, Object>();
		results1.put("ActorId", "my-actor");
		ksession.getWorkItemManager().completeWorkItem(item.getId(), results1);
		
		WorkItem item2 = notifHandler.getItem();
		Assert.assertNotNull(item2);
		ksession.getWorkItemManager().completeWorkItem(item2.getId(), null);
		
		Assert.assertEquals(ProcessInstance.STATE_ACTIVE, instance.getState());
		
		WorkItem item3 = devHandler.getItem();
		Assert.assertNotNull(item3);
	}
	
	@Test
	public void testProcessV1CancelReq() throws Exception {
		KieSession ksession = createKieSession();
		
		TestAsyncWorkItemHandler devHandler = new TestAsyncWorkItemHandler();
		TestAsyncWorkItemHandler notifHandler = new TestAsyncWorkItemHandler();
		ksession.getWorkItemManager().registerWorkItemHandler("Human Task", devHandler);
		ksession.getWorkItemManager().registerWorkItemHandler("notifier", notifHandler);
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("project", "MyProject");
		params.put("reqDescription", "My new requirement");
		ProcessInstance instance = ksession.startProcess("sprintManagement-V1", params);
		Assert.assertNotNull(instance);
		Assert.assertEquals(ProcessInstance.STATE_ACTIVE, instance.getState());
		
		WorkItem item = devHandler.getItem();
		Assert.assertNotNull(item);
		
		ksession.signalEvent("reqCancelled", null, instance.getId());
		
		WorkItem item2 = notifHandler.getItem();
		Assert.assertNotNull(item2);
		ksession.getWorkItemManager().completeWorkItem(item2.getId(), null);
		Assert.assertEquals(ProcessInstance.STATE_COMPLETED, instance.getState());
	}

	private KieSession createKieSession() {
		KieServices ks = KieServices.Factory.get();
		KieFileSystem kfs = ks.newKieFileSystem();
		kfs.write("src/main/resources/p2.bpmn2", ResourceFactory.newClassPathResource("sprintManagement-V1.bpmn2"));
		kfs.write("src/main/resources/sp.drl", ResourceFactory.newClassPathResource("assign-story-points.drl"));
		KieBuilder kbuilder = ks.newKieBuilder(kfs);
		kbuilder.buildAll();
		if (kbuilder.getResults().hasMessages(Message.Level.ERROR)) {
			System.out.println(kbuilder.getResults());
			throw new IllegalArgumentException("Invalid knowledge base!! " + kbuilder.getResults());
		}
		KieContainer kcontainer = ks.newKieContainer(kbuilder.getKieModule().getReleaseId());
		final KieSession ksession = kcontainer.newKieSession();
		((StatefulKnowledgeSessionImpl) ksession).session.addEventListener(new AgendaEventListener() {
			@Override
			public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event, WorkingMemory workingMemory) { }
			@Override
			public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event, WorkingMemory workingMemory) { }
			@Override
			public void beforeActivationFired(BeforeActivationFiredEvent event, WorkingMemory workingMemory) { }
			@Override
			public void agendaGroupPushed(AgendaGroupPushedEvent event, WorkingMemory workingMemory) { }
			@Override
			public void agendaGroupPopped(AgendaGroupPoppedEvent event, WorkingMemory workingMemory) { }
			@Override
			public void afterRuleFlowGroupDeactivated( RuleFlowGroupDeactivatedEvent event, WorkingMemory workingMemory) { }
			@Override
			public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event, WorkingMemory workingMemory) { 
				workingMemory.fireAllRules();
			}
			@Override
			public void afterActivationFired(AfterActivationFiredEvent event, WorkingMemory workingMemory) { }
			@Override
			public void activationCreated(ActivationCreatedEvent event, WorkingMemory workingMemory) {
				ksession.fireAllRules();
			}
			@Override
			public void activationCancelled(ActivationCancelledEvent event, WorkingMemory workingMemory) { }
		});
		return ksession;
	}
}
