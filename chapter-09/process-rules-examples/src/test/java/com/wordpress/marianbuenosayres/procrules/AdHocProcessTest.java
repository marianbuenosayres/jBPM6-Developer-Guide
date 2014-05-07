package com.wordpress.marianbuenosayres.procrules;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


import org.jbpm.process.instance.event.listeners.RuleAwareProcessEventLister;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.kie.api.event.rule.MatchCreatedEvent;
import org.kie.api.event.rule.RuleFlowGroupActivatedEvent;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;

public class AdHocProcessTest {

	@Test
	public void testAdHocProcess() {
		KieServices ks = KieServices.Factory.get();
		KieContainer kcontainer = ks.getKieClasspathContainer();
		final KieSession ksession = kcontainer.newKieSession();
		TestAsyncWorkItemHandler htHandler = new TestAsyncWorkItemHandler();
		ksession.getWorkItemManager().registerWorkItemHandler("Human Task", htHandler);
		ksession.addEventListener(new RuleAwareProcessEventLister());
		ksession.addEventListener(new DefaultAgendaEventListener() {
			@Override
			public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
				ksession.fireAllRules();
			}
			@Override
			public void matchCreated(MatchCreatedEvent event) {
				ksession.fireAllRules();
			}
		});
		ProcessInstance processInstance = ksession.startProcess("adhocProcess");
		
		Assert.assertEquals(ProcessInstance.STATE_ACTIVE, processInstance.getState());
		
		WorkItem item = htHandler.getWorkItem();

		Assert.assertEquals("decide what's next", item.getParameter("TaskName"));
		
		Map<String, Object> results = new HashMap<String, Object>();
		results.put("outVar1", "SOME ACTUAL VALUE");

                ksession.insert(results.get("outVar1"));
		ksession.getWorkItemManager().completeWorkItem(item.getId(), results);
		ksession.fireAllRules();

		WorkItem item2 = htHandler.getWorkItem();
		Assert.assertEquals("execute action", item2.getParameter("TaskName"));

		results.clear();
		results.put("outVar2", Integer.valueOf(22));
                ksession.insert(results.get("outVar2"));
		ksession.getWorkItemManager().completeWorkItem(item2.getId(), results);
		ksession.fireAllRules();
		
		Assert.assertEquals(ProcessInstance.STATE_COMPLETED, processInstance.getState());
	}
}
