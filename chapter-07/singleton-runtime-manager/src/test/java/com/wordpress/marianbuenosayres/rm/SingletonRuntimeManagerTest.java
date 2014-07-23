package com.wordpress.marianbuenosayres.rm;

import java.io.File;
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

import bitronix.tm.resource.jdbc.PoolingDataSource;

public class SingletonRuntimeManagerTest {
	
	private KieBase kbase;
	private UserGroupCallback userGroupCallback;
    private PoolingDataSource ds;
	
	@Before
	public void setUp() {
		KieContainer kcontainer = KieServices.Factory.get().newKieClasspathContainer();
		this.kbase = kcontainer.getKieBase();
		String location = System.getProperty("jbpm.data.dir", System.getProperty("jboss.server.data.dir"));
        if (location == null) {
            location = System.getProperty("java.io.tmpdir");
        }
		File sessionIdStore = new File(location + File.separator + "singleton-A-jbpmSessionId.ser");
		if (sessionIdStore.exists()) {
			sessionIdStore.delete();
		}
		sessionIdStore = new File(location + File.separator + "singleton-B-jbpmSessionId.ser");
		if (sessionIdStore.exists()) {
			sessionIdStore.delete();
		}
		sessionIdStore = new File(location + File.separator + "singleton-C-jbpmSessionId.ser");
		if (sessionIdStore.exists()) {
			sessionIdStore.delete();
		}
		sessionIdStore = new File(location + File.separator + "singleton-D-jbpmSessionId.ser");
		if (sessionIdStore.exists()) {
			sessionIdStore.delete();
		}
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
			newSingletonRuntimeManager(environment, "singleton-A");        

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
			newSingletonRuntimeManager(environment, "singleton-B");        

		assertManyProcesses(manager);
		
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
			newSingletonRuntimeManager(environment, "singleton-C");        

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
			newSingletonRuntimeManager(environment, "singleton-D");        

		assertManyProcesses(manager);
		
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
		
		runtime = manager.getRuntimeEngine(EmptyContext.get());
		ksession = runtime.getKieSession();       
		Assert.assertEquals(sessionId, ksession.getId());
		
		Map<String, Object> params = new HashMap<String, Object>();
		
		ProcessInstance processInstance = ksession.startProcess("sprintManagement", params);
		long processInstanceId = processInstance.getId();
		
		// dispose session that should not have affect on the session and its process instances at all
		manager.disposeRuntimeEngine(runtime);

		ksession = manager.getRuntimeEngine(EmptyContext.get()).getKieSession();        
		Assert.assertEquals(sessionId, ksession.getId());
		ProcessInstance processInstance2 = ksession.getProcessInstance(processInstanceId);
		
		Assert.assertEquals(processInstance.getState(), processInstance2.getState());
	}

	private void assertManyProcesses(RuntimeManager manager) {
		Assert.assertNotNull(manager);

		RuntimeEngine runtime = manager.getRuntimeEngine(EmptyContext.get());
		KieSession ksession = runtime.getKieSession();
		Assert.assertNotNull(ksession);       

		int sessionId = ksession.getId();
		Assert.assertTrue(sessionId >= 0);
		
		runtime = manager.getRuntimeEngine(EmptyContext.get());
		ksession = runtime.getKieSession();       
		Assert.assertEquals(sessionId, ksession.getId());
		
		Map<String, Object> params = new HashMap<String, Object>();
		ProcessInstance processInstance = ksession.startProcess("sprintManagement", params);
		long processInstanceId = processInstance.getId();
		ProcessInstance processInstance2 = ksession.startProcess("sprintManagement", params);
		long processInstanceId2 = processInstance2.getId();
		ProcessInstance processInstance3 = ksession.startProcess("sprintManagement", params);
		long processInstanceId3 = processInstance2.getId();
		// dispose session that should not have affect on the session and its process instances at all
		manager.disposeRuntimeEngine(runtime);

		ksession = manager.getRuntimeEngine(EmptyContext.get()).getKieSession();        
		Assert.assertEquals(sessionId, ksession.getId());
		ProcessInstance afterDisposeProcessInstance = ksession.getProcessInstance(processInstanceId);
		ProcessInstance afterDisposeProcessInstance2 = ksession.getProcessInstance(processInstanceId2);
		ProcessInstance afterDisposeProcessInstance3 = ksession.getProcessInstance(processInstanceId3);
		
		Assert.assertEquals(processInstance.getState(), afterDisposeProcessInstance.getState());
		Assert.assertEquals(processInstance2.getState(), afterDisposeProcessInstance2.getState());
		Assert.assertEquals(processInstance3.getState(), afterDisposeProcessInstance3.getState());
	}
}
