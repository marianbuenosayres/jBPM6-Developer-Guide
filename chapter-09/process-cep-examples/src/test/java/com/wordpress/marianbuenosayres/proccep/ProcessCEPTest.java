package com.wordpress.marianbuenosayres.proccep;

import java.util.HashMap;
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
		Assert.assertEquals("more than reqPriorityProcesses 10 processes in 5 minutes", warning);

		ksession.dispose();
	}
}
