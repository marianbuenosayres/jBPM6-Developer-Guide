package com.wordpress.marianbuenosayres.rm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

import bitronix.tm.resource.jdbc.PoolingDataSource;

public class PerRequestRuntimeManagerTest {
	
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
			newPerRequestRuntimeManager(environment, "test-A");        

		assertOneProcess(manager, false);
		
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
			newPerRequestRuntimeManager(environment, "test-B");        

		assertManyProcesses(manager, false);
		
		// close manager which will close session maintained by the manager
		manager.close();
	}
	
	@Test
	public void testMultipleProcessesInMultipleThreads() {
		SimpleRegisterableItemsFactory factory = new SimpleRegisterableItemsFactory();
		factory.addWorkItemHandler("Human Task", TestAsyncWorkItemHandler.class);
		RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get()
			.newEmptyBuilder()
			.userGroupCallback(userGroupCallback)
			.knowledgeBase(kbase)
			.registerableItemsFactory(factory)
			.get();

		RuntimeManager manager = RuntimeManagerFactory.Factory.get().
			newPerRequestRuntimeManager(environment, "test-C");        

		assertManyThreads(manager, false);
		
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
			newPerRequestRuntimeManager(environment, "test-D");        

		assertOneProcess(manager, true);
		
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
			newPerRequestRuntimeManager(environment, "test-E");

		assertManyProcesses(manager, true);
		
		// close manager which will close session maintained by the manager
		manager.close();
	}

	@Test
	public void testMultipleProcessesWithPersistenceInMultipleThreads() {
		SimpleRegisterableItemsFactory factory = new SimpleRegisterableItemsFactory();
		factory.addWorkItemHandler("Human Task", TestAsyncWorkItemHandler.class);
		RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get()
			.newDefaultBuilder()
			.userGroupCallback(userGroupCallback)
			.knowledgeBase(kbase)
			.registerableItemsFactory(factory)
			.get();

		RuntimeManager manager = RuntimeManagerFactory.Factory.get().
			newPerRequestRuntimeManager(environment, "test-F");        

		assertManyThreads(manager, true);
		
		// close manager which will close session maintained by the manager
		manager.close();
	}

	private void assertOneProcess(RuntimeManager manager, boolean persistent) {
		Assert.assertNotNull(manager);

		RuntimeEngine runtime = manager.getRuntimeEngine(EmptyContext.get());
		KieSession ksession = runtime.getKieSession();
		Assert.assertNotNull(ksession);       

		int sessionId = ksession.getId();
		Assert.assertTrue(sessionId >= 0);
		
		Map<String, Object> params = new HashMap<String, Object>();
		
		ksession.startProcess("sprintManagement", params);
		
		// dispose session that should not have affect on the session and its process instances at all
		manager.disposeRuntimeEngine(runtime);

		runtime = manager.getRuntimeEngine(EmptyContext.get());
		ksession = runtime.getKieSession();
		Assert.assertFalse(sessionId == ksession.getId());
		
		manager.disposeRuntimeEngine(runtime);
	}

	private void assertManyProcesses(RuntimeManager manager, boolean persistent) {
		Assert.assertNotNull(manager);
		Map<String, Object> params = new HashMap<String, Object>();

		//Each runtime should have its own session
		RuntimeEngine runtime1 = manager.getRuntimeEngine(EmptyContext.get());
		Assert.assertNotNull(runtime1);       
		ProcessInstance processInstance1 = runtime1.getKieSession().startProcess("sprintManagement", params);
		long processInstanceId1 = processInstance1.getId();
		
		RuntimeEngine runtime2 = manager.getRuntimeEngine(EmptyContext.get());
		Assert.assertNotNull(runtime2);
		ProcessInstance processInstance2 = runtime2.getKieSession().startProcess("sprintManagement", params);
		long processInstanceId2 = processInstance2.getId();

		Assert.assertTrue(runtime1.equals(runtime2));
		
		//Disposing a runtime is the only way to get two runtimes in the same thread
		manager.disposeRuntimeEngine(runtime1);
		
		RuntimeEngine runtime3 = manager.getRuntimeEngine(EmptyContext.get());
		
		//Unique IDs for process instances on different sessions is only guaranteed on persistent schemes 
		Assert.assertNotSame(runtime3, runtime1);
		if (persistent) {
			Assert.assertFalse(processInstanceId1 == processInstanceId2);
		}
		
		manager.disposeRuntimeEngine(runtime2);
		manager.disposeRuntimeEngine(runtime3);
	}

	private void assertManyThreads(RuntimeManager manager, boolean persistent) {
		List<RuntimeEngineThread> threads = new ArrayList<RuntimeEngineThread>();
		for (int index = 0; index < 3; index++) {
			RuntimeEngineThread thread = new RuntimeEngineThread(manager);
			threads.add(thread);
			thread.start();
		}
		for (RuntimeEngineThread thread : threads) {
			thread.dispose();
		}
		Assert.assertFalse(threads.get(0).getSessionId() == threads.get(1).getSessionId());
		Assert.assertFalse(threads.get(0).getSessionId() == threads.get(2).getSessionId());
		if (persistent) {
			Assert.assertFalse(threads.get(0).getProcessInstanceId() == threads.get(1).getProcessInstanceId());
			Assert.assertFalse(threads.get(0).getProcessInstanceId() == threads.get(2).getProcessInstanceId());
		}
	}
}
