package com.wordpress.marianbuenosayres.rm;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jbpm.runtime.manager.impl.SimpleRegisterableItemsFactory;
import org.jbpm.services.task.identity.JBossUserGroupCallbackImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.UserGroupCallback;

import com.wordpress.marianbuenosayres.custom.CustomRMFactory;
import com.wordpress.marianbuenosayres.custom.ProcessDefContext;

public class PerProcessDefinitionRuntimeManagerTest {
	
	private KieBase kbase;
	private UserGroupCallback userGroupCallback;
	
	@Before
	public void setUp() {
		KieContainer kcontainer = KieServices.Factory.get().newKieClasspathContainer();
		this.kbase = kcontainer.getKieBase();
        Properties userGroups = new Properties();
        userGroups.setProperty("john", "developers");
        userGroups.setProperty("mary", "testers");
        userGroups.setProperty("Administrator", "Administrators,developers,testers");
        this.userGroupCallback = new JBossUserGroupCallbackImpl(userGroups);
    }

	@Test
	public void testOneProcess() throws Exception {
		SimpleRegisterableItemsFactory factory = new SimpleRegisterableItemsFactory();
		factory.addWorkItemHandler("Human Task", TestAsyncWorkItemHandler.class);
		RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get()
			.newEmptyBuilder()
			.userGroupCallback(userGroupCallback)
			.knowledgeBase(kbase)
			.registerableItemsFactory(factory)
			.get();

                //Please see the comments in CustomRMFactory and PerProcessDefinitionRuntimeManager
                //to further analyze how the code is written
		RuntimeManager manager = CustomRMFactory.getInstance().
			newPerProcessDefinitionRuntimeManager(environment, "test-A");        

		assertOneProcess(manager);
		
		// close manager which will close session maintained by the manager
		manager.close();
	}
	
	@Test
	public void testTwoProcessesSameDefinition() {
		SimpleRegisterableItemsFactory factory = new SimpleRegisterableItemsFactory();
		factory.addWorkItemHandler("Human Task", TestAsyncWorkItemHandler.class);
		RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get()
			.newEmptyBuilder()
			.userGroupCallback(userGroupCallback)
			.knowledgeBase(kbase)
			.registerableItemsFactory(factory)
			.get();

                //Please see the comments in CustomRMFactory and PerProcessDefinitionRuntimeManager
                //to further analyze how the code is written
		RuntimeManager manager = CustomRMFactory.getInstance().
			newPerProcessDefinitionRuntimeManager(environment, "test-B");        

		assertTwoProcessesSameDefinition(manager);
		
		// close manager which will close session maintained by the manager
		manager.close();
	}
	
	@Test
	public void testTwoProcessesDifferentDefinitions() {
		SimpleRegisterableItemsFactory factory = new SimpleRegisterableItemsFactory();
		factory.addWorkItemHandler("Human Task", TestAsyncWorkItemHandler.class);
		RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get()
			.newEmptyBuilder()
			.userGroupCallback(userGroupCallback)
			.knowledgeBase(kbase)
			.registerableItemsFactory(factory)
			.get();

                //Please see the comments in CustomRMFactory and PerProcessDefinitionRuntimeManager
                //to further analyze how the code is written
		RuntimeManager manager = CustomRMFactory.getInstance().
			newPerProcessDefinitionRuntimeManager(environment, "test-C");        

		assertTwoProcessesDifferentDefinitions(manager);
		
		// close manager which will close session maintained by the manager
		manager.close();
	}
	
	private void assertOneProcess(RuntimeManager manager) throws Exception {
		Assert.assertNotNull(manager);
		
		RuntimeEngine runtime = manager.getRuntimeEngine(ProcessDefContext.get("sprintManagementV1"));
		KieSession ksession = runtime.getKieSession();
		Assert.assertNotNull(ksession);       

		int sessionId = ksession.getId();
		Assert.assertTrue(sessionId >= 0);
		
		Map<String, Object> params = new HashMap<String, Object>();
		
		ProcessInstance processInstance = ksession.startProcess("sprintManagementV1", params);
		long processInstanceId = processInstance.getId();
		
		// dispose session that should not have affect on the session and its process instances at all
		manager.disposeRuntimeEngine(runtime);

		ksession = manager.getRuntimeEngine(ProcessDefContext.get("sprintManagementV1")).getKieSession();
		Assert.assertEquals(sessionId, ksession.getId());
		ProcessInstance processInstance2 = ksession.getProcessInstance(processInstanceId);
		
		Assert.assertEquals(processInstance.getState(), processInstance2.getState());
	}
	
	private void assertTwoProcessesSameDefinition(RuntimeManager manager) {
		Assert.assertNotNull(manager);
		Map<String, Object> params = new HashMap<String, Object>();
		
		//Each runtime should have the same session
		RuntimeEngine runtime1 = manager.getRuntimeEngine(ProcessDefContext.get("sprintManagementV1"));
		Assert.assertNotNull(runtime1);       
		ProcessInstance processInstance1 = runtime1.getKieSession().startProcess("sprintManagementV1", params);
		long processInstanceId1 = processInstance1.getId();
		int sessionId1 = runtime1.getKieSession().getId();
		
		RuntimeEngine runtime2 = manager.getRuntimeEngine(ProcessDefContext.get("sprintManagementV1"));
		Assert.assertNotNull(runtime2);
		ProcessInstance processInstance2 = runtime2.getKieSession().startProcess("sprintManagementV1", params);
		long processInstanceId2 = processInstance2.getId();
		int sessionId2 = runtime2.getKieSession().getId();

		Assert.assertEquals(sessionId1, sessionId2);
	}

	private void assertTwoProcessesDifferentDefinitions(RuntimeManager manager) {
		Assert.assertNotNull(manager);
		Map<String, Object> params = new HashMap<String, Object>();
		
		//Each runtime should have the different sessions
		RuntimeEngine runtime1 = manager.getRuntimeEngine(ProcessDefContext.get("sprintManagementV1"));
		Assert.assertNotNull(runtime1);       
		ProcessInstance processInstance1 = runtime1.getKieSession().startProcess("sprintManagementV1", params);
		long processInstanceId1 = processInstance1.getId();
		int sessionId1 = runtime1.getKieSession().getId();
		
		RuntimeEngine runtime2 = manager.getRuntimeEngine(ProcessDefContext.get("sprintManagementV2"));
		Assert.assertNotNull(runtime2);
		ProcessInstance processInstance2 = runtime2.getKieSession().startProcess("sprintManagementV2", params);
		long processInstanceId2 = processInstance2.getId();
		int sessionId2 = runtime2.getKieSession().getId();

		Assert.assertFalse(runtime2.getKieSession().equals(runtime1.getKieSession()));
	}
}
