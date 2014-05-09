package com.wordpress.marianbuenosayres.proccep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.drools.core.ClockType;
import org.drools.core.time.SessionPseudoClock;
import org.jbpm.process.instance.event.listeners.RuleAwareProcessEventLister;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.kie.api.event.rule.RuleFlowGroupActivatedEvent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;

import com.wordpress.marianbuenosayres.model.Requirement;

public class ProcessCEPTest {

	@Test
	public void testOver10ProcessesIn5Minutes() {
		
		KieServices ks = KieServices.Factory.get();
		KieBaseConfiguration kbconf = ks.newKieBaseConfiguration();
		//configuration to use CEP
		kbconf.setOption(EventProcessingOption.STREAM);
		KieBase kbase = ks.getKieClasspathContainer().newKieBase(kbconf);
		
		KieSessionConfiguration ksconf = ks.newKieSessionConfiguration();
		//configuration to control time speed
		ksconf.setOption(ClockTypeOption.get(ClockType.PSEUDO_CLOCK.getId()));
		KieSession ksession = kbase.newKieSession(ksconf, null);
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
		
		MockNotifierService mockNotifierService = new MockNotifierService();
		ksession.setGlobal("notifier", mockNotifierService);

		SessionPseudoClock clock = ksession.getSessionClock();
		for (int index = 0; index < 10; index++) {
			Map<String, Object> params = new HashMap<String, Object>();
			Requirement req = new Requirement("req" + index, "Requirement " + index);
			params.put("req", req);
			ksession.startProcess("reqPriorityProcess", params);
			clock.advanceTime(20, TimeUnit.SECONDS);
		}
		
		Assert.assertEquals(0, mockNotifierService.getWarnings().size());

		//Adding one more process instance triggers the rule
		Map<String, Object> params = new HashMap<String, Object>();
		Requirement req = new Requirement("req11", "Requirement 11");
		params.put("req", req);
		ksession.startProcess("reqPriorityProcess", params);
		
		Assert.assertEquals(1, mockNotifierService.getWarnings().size());
		String warning = mockNotifierService.getWarnings().iterator().next();
		Assert.assertEquals("reqPriorityProcess: more than 10 processes in 5 minutes", warning);

		ksession.dispose();
	}
	
	@Test
	public void testAfterRule() throws Exception {
		KieServices ks = KieServices.Factory.get();
		KieBaseConfiguration kbconf = ks.newKieBaseConfiguration();
		//configuration to use CEP
		kbconf.setOption(EventProcessingOption.STREAM);
		KieBase kbase = ks.getKieClasspathContainer().newKieBase(kbconf);
		
		KieSessionConfiguration ksconf = ks.newKieSessionConfiguration();
		//configuration to control time speed
		ksconf.setOption(ClockTypeOption.get(ClockType.PSEUDO_CLOCK.getId()));
		final KieSession ksession = kbase.newKieSession(ksconf, null);
		TestAsyncWorkItemHandler handler = new TestAsyncWorkItemHandler();
		//register the work item handlers needed for the process executions
		ksession.getWorkItemManager().registerWorkItemHandler("asyncTask", handler);
		//add the RuleAwareProcessEventLister to put process instances in the rules memory
		ksession.addEventListener(new RuleAwareProcessEventLister());
		//Add list to keep track of fired rules
		final List<String> executions = new ArrayList<String>();
		ksession.addEventListener(new DefaultAgendaEventListener() {
			@Override
			public void afterMatchFired(AfterMatchFiredEvent event) {
				String ruleName = event.getMatch().getRule().getName();
				executions.add(ruleName);
			}
		});

		//adding something to an entry point
		ksession.getEntryPoint("tempop-test").insert(new Object());
		
		SessionPseudoClock clock = ksession.getSessionClock();
		Map<String, Object> params1 = new HashMap<String, Object>();
		Requirement req1 = new Requirement("req1", "Requirement 1");
		params1.put("req", req1);
		ksession.startProcess("reqPriorityProcess", params1);
		clock.advanceTime(50, TimeUnit.SECONDS);
		Map<String, Object> params2 = new HashMap<String, Object>();
		Requirement req2 = new Requirement("req2", "Requirement 2");
		params2.put("req", req2);
		ksession.startProcess("reqPriorityProcess", params2);
		ksession.fireAllRules();
		
		Assert.assertFalse(executions.contains("two processes at least 3 minutes appart"));
		
		clock.advanceTime(4, TimeUnit.MINUTES);
		Map<String, Object> params3 = new HashMap<String, Object>();
		Requirement req3 = new Requirement("req3", "Requirement 3");
		params3.put("req", req3);
		ksession.startProcess("reqPriorityProcess", params3);
		ksession.fireAllRules();

		Assert.assertTrue(executions.contains("two processes at least 3 minutes appart"));
		
		ksession.dispose();
	}
}
