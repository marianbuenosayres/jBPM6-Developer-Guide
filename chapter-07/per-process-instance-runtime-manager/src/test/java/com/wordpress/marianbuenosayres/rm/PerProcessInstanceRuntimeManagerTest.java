package com.wordpress.marianbuenosayres.rm;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jbpm.runtime.manager.impl.SimpleRegisterableItemsFactory;
import org.jbpm.services.task.identity.JBossUserGroupCallbackImpl;
import org.junit.After;
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
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.UserGroupCallback;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;

import bitronix.tm.resource.jdbc.PoolingDataSource;

public class PerProcessInstanceRuntimeManagerTest {
	
	private KieBase kbase;
	private UserGroupCallback userGroupCallback;
    private PoolingDataSource ds;
	
	@Before
	public void setUp() {
		KieContainer kcontainer = KieServices.Factory.get().newKieClasspathContainer();
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
	public void testOneProcess() {
		SimpleRegisterableItemsFactory factory = new SimpleRegisterableItemsFactory();
		factory.addWorkItemHandler("Human Task", TestAsyncWorkItemHandler.class);
		RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get()
			.newEmptyBuilder()
			.userGroupCallback(userGroupCallback)
			.knowledgeBase(kbase)
			.registerableItemsFactory(factory)
			.get();

		RuntimeManager manager = RuntimeManagerFactory.Factory.get().
			newPerProcessInstanceRuntimeManager(environment, "test-A");        

		assertOneProcess(manager);
		
		// close manager which will close session maintained by the manager
		manager.close();
	}
	
	@Test
	public void testMultipleProcesses() {
		SimpleRegisterableItemsFactory factory = new SimpleRegisterableItemsFactory();
		factory.addWorkItemHandler("Human Task", TestAsyncWorkItemHandler.class);
		RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get()
			.newEmptyBuilder()
			.userGroupCallback(userGroupCallback)
			.knowledgeBase(kbase)
			.registerableItemsFactory(factory)
			.get();

		RuntimeManager manager = RuntimeManagerFactory.Factory.get().
			newPerProcessInstanceRuntimeManager(environment, "test-B");        

		assertManyProcesses(manager, false);
		
		// close manager which will close session maintained by the manager
		manager.close();
	}
	
	@Test
	public void testOneProcessWithPersistence() {
		SimpleRegisterableItemsFactory factory = new SimpleRegisterableItemsFactory();
		factory.addWorkItemHandler("Human Task", TestAsyncWorkItemHandler.class);
		RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get()
			.newDefaultBuilder()
			.userGroupCallback(userGroupCallback)
			.knowledgeBase(kbase)
			.registerableItemsFactory(factory)
			.get();

		RuntimeManager manager = RuntimeManagerFactory.Factory.get().
			newPerProcessInstanceRuntimeManager(environment, "test-C");        

		assertOneProcess(manager);
		
		// close manager which will close session maintained by the manager
		manager.close();
	}
	
	@Test
	public void testMultipleProcessesWithPersistence() {
		SimpleRegisterableItemsFactory factory = new SimpleRegisterableItemsFactory();
		factory.addWorkItemHandler("Human Task", TestAsyncWorkItemHandler.class);
		RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get()
			.newDefaultBuilder()
			.userGroupCallback(userGroupCallback)
			.knowledgeBase(kbase)
			.registerableItemsFactory(factory)
			.get();

		RuntimeManager manager = RuntimeManagerFactory.Factory.get().
			newPerProcessInstanceRuntimeManager(environment, "test-D");        

		assertManyProcesses(manager, true);
		
		// close manager which will close session maintained by the manager
		manager.close();
	}

	private void assertOneProcess(RuntimeManager manager) {
		Assert.assertNotNull(manager);

		RuntimeEngine runtime = manager.getRuntimeEngine(EmptyContext.get());
		KieSession ksession = runtime.getKieSession();
		Assert.assertNotNull(ksession);       

		int sessionId = ksession.getId();
		Assert.assertTrue(sessionId >= 0);
		
		Map<String, Object> params = new HashMap<String, Object>();
		
		ProcessInstance processInstance = ksession.startProcess("sprintManagement", params);
		long processInstanceId = processInstance.getId();
		
		// dispose session that should not have affect on the session and its process instances at all
		manager.disposeRuntimeEngine(runtime);

		ksession = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId)).getKieSession();
		Assert.assertEquals(sessionId, ksession.getId());
		ProcessInstance processInstance2 = ksession.getProcessInstance(processInstanceId);
		
		Assert.assertEquals(processInstance.getState(), processInstance2.getState());
	}

	private void assertManyProcesses(RuntimeManager manager, boolean persistent) {
		Assert.assertNotNull(manager);
		Map<String, Object> params = new HashMap<String, Object>();

		//Each runtime should have its own session
		RuntimeEngine runtime1 = manager.getRuntimeEngine(EmptyContext.get());
		Assert.assertNotNull(runtime1);       
		ProcessInstance processInstance1 = runtime1.getKieSession().startProcess("sprintManagement", params);
		long processInstanceId1 = processInstance1.getId();
		int sessionId1 = runtime1.getKieSession().getId();
		
		RuntimeEngine runtime2 = manager.getRuntimeEngine(EmptyContext.get());
		Assert.assertNotNull(runtime2);
		ProcessInstance processInstance2 = runtime2.getKieSession().startProcess("sprintManagement", params);
		long processInstanceId2 = processInstance2.getId();
		int sessionId2 = runtime2.getKieSession().getId();

		RuntimeEngine runtime3 = manager.getRuntimeEngine(EmptyContext.get());
		Assert.assertNotNull(runtime3);
		ProcessInstance processInstance3 = runtime3.getKieSession().startProcess("sprintManagement", params);
		long processInstanceId3 = processInstance3.getId();
		int sessionId3 = runtime3.getKieSession().getId();
		
		Assert.assertFalse(runtime1.equals(runtime2));
		Assert.assertFalse(runtime1.equals(runtime3));
		Assert.assertFalse(runtime2.equals(runtime3));
		
		Assert.assertFalse(sessionId1 == sessionId2);
		Assert.assertFalse(sessionId1 == sessionId3);
		Assert.assertFalse(sessionId2 == sessionId3);
		
		if (persistent == true) {
			//Unique IDs for process instances on different sessions is only guaranteed on persistent schemes 
			KieSession ksession1 = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId1)).getKieSession();
			KieSession ksession2 = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId2)).getKieSession();
			KieSession ksession3 = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId3)).getKieSession();
			Assert.assertEquals(sessionId1, ksession1.getId());
			Assert.assertEquals(sessionId2, ksession2.getId());
			Assert.assertEquals(sessionId3, ksession3.getId());
		}
	}
}
