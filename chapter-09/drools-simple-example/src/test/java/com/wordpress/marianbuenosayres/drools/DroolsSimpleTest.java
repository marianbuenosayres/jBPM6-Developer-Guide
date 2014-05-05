package com.wordpress.marianbuenosayres.drools;

import junit.framework.Assert;

import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.BeforeMatchFiredEvent;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.kie.api.event.rule.MatchCancelledEvent;
import org.kie.api.event.rule.MatchCreatedEvent;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import com.wordpress.marianbuenosayres.model.Requirement;

public class DroolsSimpleTest {

	@Test
	public void testDroolsSimpleExample() {
		KieServices ks = KieServices.Factory.get();
		KieContainer kcontainer = ks.getKieClasspathContainer();
		KieSession ksession = kcontainer.newKieSession();
		ksession.addEventListener(new DefaultAgendaEventListener() {
			@Override
			public void matchCreated(MatchCreatedEvent event) {
				System.out.println("matchCreated: " + event);
			}

			@Override
			public void matchCancelled(MatchCancelledEvent event) {
				System.out.println("matchCancelled: " + event);
			}

			@Override
			public void beforeMatchFired(BeforeMatchFiredEvent event) {
				System.out.println("beforeMatchFired: " + event);
			}

			@Override
			public void afterMatchFired(AfterMatchFiredEvent event) {
				System.out.println("afterMatchFired: " + event);
			}
		});
		
		Requirement req1 = new Requirement("req1", "some requirement");
		ksession.insert(req1);
		Requirement req2 = new Requirement("req2", "some requirement");
		req2.addBug("bug1");
		req2.addBug("bug2");
		req2.addBug("bug3");
		req2.addBug("bug4");
		ksession.insert(req2);
		Requirement req3 = new Requirement("URGENT req3", "some requirement");
		ksession.insert(req3);
		Requirement req4 = new Requirement("req4", "some requirement");
		ksession.insert(req4);
		Requirement req5 = new Requirement("req5", "some requirement");
		ksession.insert(req5);

		Assert.assertEquals(-1, req1.getPriority());
		Assert.assertEquals(-1, req2.getPriority());
		Assert.assertEquals(-1, req3.getPriority());
		Assert.assertEquals(-1, req4.getPriority());
		Assert.assertEquals(-1, req5.getPriority());
		
		ksession.fireAllRules();
		
		Assert.assertEquals(10, req1.getPriority());
		Assert.assertEquals(1, req3.getPriority());
		Assert.assertEquals(10, req4.getPriority());
		Assert.assertEquals(10, req5.getPriority());
		Assert.assertEquals(5, req2.getPriority());
	}
}
