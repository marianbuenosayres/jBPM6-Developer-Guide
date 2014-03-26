package com.wordpress.marianbuenosayres.rm;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jbpm.runtime.manager.impl.SimpleRegisterableItemsFactory;
import org.jbpm.services.task.identity.JBossUserGroupCallbackImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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

import bitronix.tm.resource.jdbc.PoolingDataSource;

import com.wordpress.marianbuenosayres.custom.CustomRMFactory;
import com.wordpress.marianbuenosayres.custom.ProcessDefContext;

public class PerProcessDefinitionRuntimeManagerTest {
	
	private KieBase kbase;
	private UserGroupCallback userGroupCallback;
    private PoolingDataSource ds;
	
	@Before
	public void setUp() {
		KieContainer kcontainer = KieServices.Factory.get().getKieClasspathContainer();
		this.kbase = kcontainer.getKieBase();
        Properties userGroups = new Properties();
        userGroups.setProperty("john", "developers");
        userGroups.setProperty("mary", "testers");
        userGroups.setProperty("Administrator", "Administrators,developers,testers");
        this.userGroupCallback = new JBossUserGroupCallbackImpl(userGroups);

        this.ds = new PoolingDataSource();
        this.ds.setUniqueName("jdbc/testDS");
        this.ds.setClassName("org.h2.jdbcx.JdbcDataSource");
        this.ds.setMaxPoolSize(3);
        this.ds.setAllowLocalTransactions(true);
        this.ds.getDriverProperties().setProperty("URL", "jdbc:h2:tasks;MVCC=true;DB_CLOSE_ON_EXIT=FALSE");
        this.ds.getDriverProperties().setProperty("user", "sa");
        this.ds.getDriverProperties().setProperty("password", "sasa");
        this.ds.init();
    }

    @After
    public void tearDown() throws Exception {
        if (this.ds != null) {
            this.ds.close();
        }
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

		RuntimeManager manager = CustomRMFactory.getInstance().
			newPerProcessDefinitionRuntimeManager(environment, "test-B");        

		assertTwoProcessesSameDefinition(manager, false);
		
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

		RuntimeManager manager = CustomRMFactory.getInstance().
			newPerProcessDefinitionRuntimeManager(environment, "test-C");        

		assertTwoProcessesDifferentDefinitions(manager, false);
		
		// close manager which will close session maintained by the manager
		manager.close();
	}
	
	@Test @Ignore("To be corrected")
	public void testOneProcessWithPersistence() throws Exception {
		SimpleRegisterableItemsFactory factory = new SimpleRegisterableItemsFactory();
		factory.addWorkItemHandler("Human Task", TestAsyncWorkItemHandler.class);
		RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get()
			.newDefaultBuilder()
			.userGroupCallback(userGroupCallback)
			.knowledgeBase(kbase)
			.registerableItemsFactory(factory)
			.get();

		RuntimeManager manager = CustomRMFactory.getInstance().
			newPerProcessDefinitionRuntimeManager(environment, "test-D");

		assertOneProcess(manager);
		
		// close manager which will close session maintained by the manager
		manager.close();
	}
	
	@Test @Ignore("To be corrected")
	public void testTwoProcessesSameDefinitionWithPersistence() {
		SimpleRegisterableItemsFactory factory = new SimpleRegisterableItemsFactory();
		factory.addWorkItemHandler("Human Task", TestAsyncWorkItemHandler.class);
		RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get()
			.newDefaultBuilder()
			.userGroupCallback(userGroupCallback)
			.knowledgeBase(kbase)
			.registerableItemsFactory(factory)
			.get();

		RuntimeManager manager = CustomRMFactory.getInstance().
			newPerProcessDefinitionRuntimeManager(environment, "test-E");        

		assertTwoProcessesSameDefinition(manager, true);
		
		// close manager which will close session maintained by the manager
		manager.close();
	}

	@Test @Ignore("To be corrected")
	public void testTwoProcessesDifferentDefinitionsWithPersistence() {
		SimpleRegisterableItemsFactory factory = new SimpleRegisterableItemsFactory();
		factory.addWorkItemHandler("Human Task", TestAsyncWorkItemHandler.class);
		RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get()
			.newDefaultBuilder()
			.userGroupCallback(userGroupCallback)
			.knowledgeBase(kbase)
			.registerableItemsFactory(factory)
			.get();

		RuntimeManager manager = CustomRMFactory.getInstance().
			newPerProcessDefinitionRuntimeManager(environment, "test-F");        

		assertTwoProcessesDifferentDefinitions(manager, true);
		
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
	
	private void assertTwoProcessesSameDefinition(RuntimeManager manager, boolean persistent) {
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
		if (persistent == true) {
			Assert.assertTrue(processInstanceId1 != processInstanceId2);
		}
	}

	private void assertTwoProcessesDifferentDefinitions(RuntimeManager manager, boolean persistent) {
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

		Assert.assertFalse(sessionId1 == sessionId2);
		if (persistent == true) {
			Assert.assertFalse(processInstanceId1 == processInstanceId2);
		}
	}
}
