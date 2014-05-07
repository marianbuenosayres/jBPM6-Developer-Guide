package com.wordpress.marianbuenosayres.procrules;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jbpm.process.instance.event.listeners.RuleAwareProcessEventLister;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.kie.api.event.rule.RuleFlowGroupActivatedEvent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;

import com.wordpress.marianbuenosayres.model.ProcessCreationTrigger;
import com.wordpress.marianbuenosayres.model.Requirement;

public class RulesAndProcessesTest {

	private KieSession ksession;
	
	@Before
	public void setUp() {
		KieServices ks = KieServices.Factory.get();
		this.ksession = ks.getKieClasspathContainer().newKieSession();
	}
	
	@After
	public void tearDown() {
		if (this.ksession != null) {
			this.ksession.dispose();
		}
		this.ksession = null;
	}
	
	@Test
	public void testInvokeRulesFromProcess() {
		TestAsyncWorkItemHandler handler = new TestAsyncWorkItemHandler();
		//register the work item handlers needed for the process executions
		ksession.getWorkItemManager().registerWorkItemHandler("asyncTask", handler);
		//add the RuleAwareProcessEventLister to put process instances in the rules memory
		ksession.addEventListener(new RuleAwareProcessEventLister());
		//add the AgendaEventListener to fire rules activated within a rule flow group
		ksession.addEventListener(new DefaultAgendaEventListener() {
			@Override
			public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
				KieSession kses = (KieSession) event.getKieRuntime();
				kses.fireAllRules();
			}
		});
		Map<String, Object> params = new HashMap<String, Object>();
		Requirement req1 = new Requirement("req1", "Requirement 1");
		
		//We make sure no priority is set for the rule execution
		Assert.assertEquals(-1, req1.getPriority());
		params.put("req", req1);
		ProcessInstance instance = ksession.startProcess("reqPriorityProcess", params);

		//Assignment of the priority is done through the rules execution
		Assert.assertEquals(5, req1.getPriority());

		Assert.assertEquals(ProcessInstance.STATE_ACTIVE, instance.getState());
		WorkItem item = handler.getWorkItem();
		//Because the priority of the requirement is under 10, it is handled by a 
		//low priority requirement task
		Object taskName = item.getParameter("TaskName");
		Assert.assertEquals("lowpriorityrequirement", taskName);

		//We complete the process
		ksession.getWorkItemManager().completeWorkItem(item.getId(), null);
		Assert.assertEquals(ProcessInstance.STATE_COMPLETED, instance.getState());
	}
	
	@Test
	public void testInvokeProcessFromRules() {
		TestAsyncWorkItemHandler handler = new TestAsyncWorkItemHandler();
		//register the work item handlers needed for the process executions
		ksession.getWorkItemManager().registerWorkItemHandler("asyncTask", handler);
		//add the RuleAwareProcessEventLister to put process instances in the rules memory
		ksession.addEventListener(new RuleAwareProcessEventLister());
		//add the AgendaEventListener to fire rules activated within a rule flow group
		ksession.addEventListener(new DefaultAgendaEventListener() {
			@Override
			public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
				KieSession kses = (KieSession) event.getKieRuntime();
				kses.fireAllRules();
			}
		});
		
		Requirement req1 = new Requirement("req1", "Requirement 1");
		ProcessCreationTrigger trigger = new ProcessCreationTrigger();
		//When we insert the requirement and the ProcessCreationtrigger objects in the 
		//KIE Session, we will match a rule to start the process
		ksession.insert(trigger);
		ksession.insert(req1);
		//After rules are executed, we should have one process instance active
		ksession.fireAllRules();
		Collection<ProcessInstance> instances = ksession.getProcessInstances();
		Assert.assertEquals(1, instances.size());
		ProcessInstance instance = instances.iterator().next();

		Assert.assertEquals(ProcessInstance.STATE_ACTIVE, instance.getState());
		WorkItem item = handler.getWorkItem();
		//Because the priority of the requirement is under 10, it is handled by a 
		//low priority requirement task
		Object taskName = item.getParameter("TaskName");
		Assert.assertEquals("lowpriorityrequirement", taskName);

		//We complete the process
		ksession.getWorkItemManager().completeWorkItem(item.getId(), null);
		Assert.assertEquals(ProcessInstance.STATE_COMPLETED, instance.getState());
	}
}
