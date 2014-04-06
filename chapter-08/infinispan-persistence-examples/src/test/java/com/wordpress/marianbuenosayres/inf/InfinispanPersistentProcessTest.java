package com.wordpress.marianbuenosayres.inf;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import junit.framework.Assert;

import org.drools.persistence.jta.JtaTransactionManager;
import org.infinispan.manager.DefaultCacheManager;
import org.jbpm.persistence.InfinispanProcessPersistenceContextManager;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.persistence.jpa.KieStoreServices;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.internal.utils.ServiceRegistryImpl;

import bitronix.tm.TransactionManagerServices;

public class InfinispanPersistentProcessTest {

	@Before
	public void setUp() throws Exception {
		ServiceRegistryImpl.getInstance().registerLocator(KieStoreServices.class, new Callable<KieStoreServices>() {
			@Override
			public KieStoreServices call() throws Exception {
				return (KieStoreServices) Class.forName("org.drools.persistence.infinispan.KnowledgeStoreServiceImpl").newInstance();
			}
			
		});
	}
	
	@Test
	public void testPersistentKieSessionInstantiation() throws Exception {
		KieServices kservices = KieServices.Factory.get();
		KieStoreServices kstore = kservices.getStoreServices();
		
		//We need to configure all these component in the environment to make
		//the Infinispan persistence work with Drools and jBPM6
		DefaultCacheManager cm = new DefaultCacheManager("infinispan.xml");
		Environment environment = kservices.newEnvironment();
		environment.set(EnvironmentName.ENTITY_MANAGER_FACTORY, cm);
		environment.set(EnvironmentName.TRANSACTION_MANAGER, new JtaTransactionManager(
				TransactionManagerServices.getTransactionManager().getCurrentTransaction(), 
				TransactionManagerServices.getTransactionSynchronizationRegistry(), 
				TransactionManagerServices.getTransactionManager()));
		environment.set(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER, new InfinispanProcessPersistenceContextManager(environment));
		
		KieSessionConfiguration ksconf = kservices.newKieSessionConfiguration();
		
		//KieBase we will use to create and restore the runtime from the infinispan cache
		KieBase kbase = kservices.getKieClasspathContainer().getKieBase();
		
		//Invocation to create a new persistent session
		KieSession ksession = kstore.newKieSession(kbase, ksconf, environment);
		
		//Configure the session runtime. We have to do this for every manually created or reloaded session
		TestAsyncWorkItemHandler htHandler = new TestAsyncWorkItemHandler();
		ksession.getWorkItemManager().registerWorkItemHandler("Human Task", htHandler);
		
		//We start a process
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("project", "My project");
		params.put("reqDescription", "A description of the requirement");
		ProcessInstance processInstance = ksession.startProcess("sprintManagementV1", params);
		//Make sure the process behaviour is as expected
		Assert.assertNotNull(processInstance);
		Assert.assertEquals(ProcessInstance.STATE_ACTIVE, processInstance.getState());
		
		//We get the process instance id
		long processInstanceId = processInstance.getId();
		
		//We create a new Kie session from the database
		//Create an environment variable with all the Infinispan configuration content
		Environment newEnvironment = kservices.newEnvironment();
		newEnvironment.set(EnvironmentName.ENTITY_MANAGER_FACTORY, cm);
		newEnvironment.set(EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager());
		newEnvironment.set(EnvironmentName.TRANSACTION_MANAGER, new JtaTransactionManager(
				TransactionManagerServices.getTransactionManager().getCurrentTransaction(), 
				TransactionManagerServices.getTransactionSynchronizationRegistry(), 
				TransactionManagerServices.getTransactionManager()));
		newEnvironment.set(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER, new InfinispanProcessPersistenceContextManager(newEnvironment));
		KieSession newKsession = kstore.newKieSession(kbase, ksconf, newEnvironment);

		//We make sure the new session is a different instance 
		Assert.assertNotSame(newKsession.getId(), ksession.getId());
		
		//Because the processes are persisted, we can reload the process instance from another session
		ProcessInstance reloadedInstance = newKsession.getProcessInstance(processInstanceId);
		Assert.assertNotNull(reloadedInstance);
		Assert.assertEquals(processInstance.getId(), reloadedInstance.getId());
		Assert.assertEquals(processInstance.getState(), reloadedInstance.getState());
		
		//We compare variables to see they are equal
		WorkflowProcessInstance previous = (WorkflowProcessInstance) processInstance;
		WorkflowProcessInstance reloaded = (WorkflowProcessInstance) reloadedInstance;
		Assert.assertEquals(previous.getVariable("project"), reloaded.getVariable("project"));
		Assert.assertEquals(previous.getVariable("reqDescription"), reloaded.getVariable("reqDescription"));
	}
	
	@Test
	public void testSessionReloadingWithProcesses() throws Exception {
		KieServices kservices = KieServices.Factory.get();
		KieStoreServices kstore = kservices.getStoreServices();
		
		//Create an environment variable with all the database configuration content
		Environment environment = kservices.newEnvironment();
		DefaultCacheManager cm = new DefaultCacheManager("infinispan.xml");
		environment.set(EnvironmentName.ENTITY_MANAGER_FACTORY, cm);
		environment.set(EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager());
		environment.set(EnvironmentName.TRANSACTION_MANAGER, new JtaTransactionManager(
				TransactionManagerServices.getTransactionManager().getCurrentTransaction(), 
				TransactionManagerServices.getTransactionSynchronizationRegistry(), 
				TransactionManagerServices.getTransactionManager()));
		environment.set(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER, new InfinispanProcessPersistenceContextManager(environment));
		
		//KieSession configuration object to configure special components
		KieSessionConfiguration ksconf = kservices.newKieSessionConfiguration();
		
		//KieBase we will use to create and restore the runtime from the database 
		KieBase kbase = kservices.getKieClasspathContainer().getKieBase();
		
		//Invocation to create a new persistent session
		KieSession ksession = kstore.newKieSession(kbase, ksconf, environment);
		//Store the session ID
		int sessionId = ksession.getId();

		//Configure the session runtime. We have to do this for every manually created or reloaded session
		TestAsyncWorkItemHandler htHandler = new TestAsyncWorkItemHandler();
		ksession.getWorkItemManager().registerWorkItemHandler("Human Task", htHandler);
		
		//We start a process
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("project", "My project");
		params.put("reqDescription", "A description of the requirement");
		ProcessInstance processInstance = ksession.startProcess("sprintManagementV1", params);
		//Make sure the process behaviour is as expected
		Assert.assertNotNull(processInstance);
		Assert.assertEquals(ProcessInstance.STATE_ACTIVE, processInstance.getState());
		
		//We get the process instance id
		long processInstanceId = processInstance.getId();
		
		//We reload the Kie session from the database
		//Create an environment variable with all the Infinispan configuration content
		Environment newEnvironment = kservices.newEnvironment();
		newEnvironment.set(EnvironmentName.ENTITY_MANAGER_FACTORY, cm);
		newEnvironment.set(EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager());
		newEnvironment.set(EnvironmentName.TRANSACTION_MANAGER, new JtaTransactionManager(
				TransactionManagerServices.getTransactionManager().getCurrentTransaction(), 
				TransactionManagerServices.getTransactionSynchronizationRegistry(), 
				TransactionManagerServices.getTransactionManager()));
		newEnvironment.set(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER, new InfinispanProcessPersistenceContextManager(newEnvironment));
		KieSession reloadedKsession = kstore.loadKieSession(sessionId, kbase, ksconf, newEnvironment);
		
		//This runtime configuration has to be done also every time you reload the session manually
		ksession.getWorkItemManager().registerWorkItemHandler("Human Task", htHandler);

		//We make sure the new session is a different instance 
		Assert.assertEquals(reloadedKsession.getId(), ksession.getId());
		
		//Because the processes are persisted, we can reload the process instance
		ProcessInstance reloadedInstance = reloadedKsession.getProcessInstance(processInstanceId);
		Assert.assertNotNull(reloadedInstance);
		Assert.assertEquals(processInstance.getId(), reloadedInstance.getId());
		Assert.assertEquals(processInstance.getState(), reloadedInstance.getState());
		
		//We compare variables to see they are equal, but different instances
		WorkflowProcessInstance previous = (WorkflowProcessInstance) processInstance;
		WorkflowProcessInstance reloaded = (WorkflowProcessInstance) reloadedInstance;
		Assert.assertEquals(previous.getVariable("project"), reloaded.getVariable("project"));
		Assert.assertEquals(previous.getVariable("reqDescription"), reloaded.getVariable("reqDescription"));
	}
}
