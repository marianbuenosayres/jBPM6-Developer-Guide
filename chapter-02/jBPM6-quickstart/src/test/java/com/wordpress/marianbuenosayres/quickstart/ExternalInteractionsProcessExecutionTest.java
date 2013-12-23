package com.wordpress.marianbuenosayres.quickstart;

import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;

import com.wordpress.marianbuenosayres.handlers.TestAsyncWorkItemHandler;
import com.wordpress.marianbuenosayres.handlers.TestSyncWorkItemHandler;

/**
 * Simple test to start getting familiarized with jBPM6 APIs and concepts
 * for working with connectors to external systems and components
 * 
 * @author marianbuenosayres
 *
 */
public class ExternalInteractionsProcessExecutionTest {

	@Test 
	public void testExternalInterations() {
		//Get a Kie Session with the kwnoledge in the default classpath
		KieSession ksession = KieServices.Factory.get().getKieClasspathContainer().newKieSession();
		//Create 2 Work Item Handlers for the two generic tasks of my-external-interactions-process.bpmn2
		TestSyncWorkItemHandler handler1 = new TestSyncWorkItemHandler();
		TestAsyncWorkItemHandler handler2 = new TestAsyncWorkItemHandler();
		//And we register them
		ksession.getWorkItemManager().registerWorkItemHandler("task1", handler1);
		ksession.getWorkItemManager().registerWorkItemHandler("task2", handler2);
		//And register another for the "Human Task" key, to handle user tasks
		ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler2);
		
		//After everything is registered, we start the process that uses those 3 interactions
		ProcessInstance instance = ksession.startProcess("myExternalInteractionsProcess");
		Assert.assertNotNull(instance);
		//Because it has asynchronous work item handlers, the instance will remain active
		//until all work items involved are completed
		Assert.assertEquals(ProcessInstance.STATE_ACTIVE, instance.getState());
		//TestSyncWorkItemHandler counts how many times it was invoked
		Assert.assertEquals(1, handler1.getInvocationCount());
		//handler2 will hold the work item that invoked it
		WorkItem item2 = handler2.getItem();
		Assert.assertNotNull(item2);
		//But only returns it once, then it returns null
		Assert.assertNull(handler2.getItem());
		
		//When we complete the work item, we move to the next step in the process (the user task)
		ksession.getWorkItemManager().completeWorkItem(item2.getId(), null);
		//Since the next step is also asynchronous, the instance is still active
		Assert.assertEquals(ProcessInstance.STATE_ACTIVE, instance.getState());
		//Now we should have a different item waiting for completion
		WorkItem item3 = handler2.getItem();
		Assert.assertNotNull(item3);
		Assert.assertNull(handler2.getItem());
		Assert.assertTrue(item2.getId() != item3.getId());
		
		//When we complete it, since it is the last asynchronous item of the instance, the instance ends
		ksession.getWorkItemManager().completeWorkItem(item3.getId(), null);
		Assert.assertEquals(ProcessInstance.STATE_COMPLETED, instance.getState());
	}
}
