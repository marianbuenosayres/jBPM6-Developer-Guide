package com.wordpress.marianbuenosayres;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.TransactionManager;

import junit.framework.Assert;

import org.drools.core.marshalling.impl.ClassObjectMarshallingStrategyAcceptor;
import org.drools.core.marshalling.impl.SerializablePlaceholderResolverStrategy;
import org.jbpm.process.audit.AuditLoggerFactory;
import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.persistence.jpa.KieStoreServices;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkflowProcessInstance;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;

import com.wordpress.marianbuenosayres.model.Requirement;
import com.wordpress.marianbuenosayres.omstrategy.JPAReadAndWriteStrategy;

public class JPAPersistentProcessTest {

	private PoolingDataSource ds = null;
	
	@Before
	public void startUp() throws Exception {
		ds = new PoolingDataSource();
		ds.setUniqueName("jdbc/testDS");
        ds.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
        ds.setAllowLocalTransactions(true);
        ds.setMaxPoolSize(3);
        ds.getDriverProperties().put("driverClassName", "org.h2.Driver");
        ds.getDriverProperties().put("Url", "jdbc:h2:mem:mydb");
        ds.getDriverProperties().put("password", "sasa");
        ds.getDriverProperties().put("user", "sa");
        ds.init();
	}
	
	@After
	public void tearDown() throws Exception {
		if (ds != null) {
			ds.close();
		}
		ds = null;
	}
	
	@Test
	public void testPersistentKieSessionInstantiation() throws Exception {
		KieServices kservices = KieServices.Factory.get();
		KieStoreServices kstore = kservices.getStoreServices();
		
		//Create an environment variable with all the database configuration content
		Environment environment = kservices.newEnvironment();
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.jbpm.persistence.jpa");
		TransactionManager tm = TransactionManagerServices.getTransactionManager();
		environment.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
		environment.set(EnvironmentName.TRANSACTION_MANAGER, tm);
		
		//KieSession configuration object to configure special components
		KieSessionConfiguration ksconf = kservices.newKieSessionConfiguration();
		
		//KieBase we will use to create and restore the runtime from the database 
		KieBase kbase = kservices.getKieClasspathContainer().getKieBase();
		
		//Invocation to create a new persistent session
		KieSession ksession = kstore.newKieSession(kbase, ksconf, environment);

		//Configure the session runtime. We have to do this for every manually created or reloaded session
		TestAsyncWorkItemHandler htHandler = new TestAsyncWorkItemHandler();
		ksession.getWorkItemManager().registerWorkItemHandler("Human Task", htHandler);
		
		//We start a process
		Map<String, Object> params = new HashMap<String, Object>();
		Requirement req = new Requirement("title", "description");
		params.put("req", req);
		ProcessInstance processInstance = ksession.startProcess("sprintManagementV2", params);
		//Make sure the process behaviour is as expected
		Assert.assertNotNull(processInstance);
		Assert.assertEquals(ProcessInstance.STATE_ACTIVE, processInstance.getState());
		
		//We get the process instance id
		long processInstanceId = processInstance.getId();
		
		//We create a new Kie session from the database
		//Create an environment variable with all the database configuration content
		Environment newEnvironment = kservices.newEnvironment();
		newEnvironment.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
		newEnvironment.set(EnvironmentName.TRANSACTION_MANAGER, tm);
		KieSession newKsession = kstore.newKieSession(kbase, ksconf, newEnvironment);

		//We make sure the new session is a different instance 
		Assert.assertNotSame(newKsession.getId(), ksession.getId());
		
		//Because the processes are persisted, we can reload the process instance from another session
		ProcessInstance reloadedInstance = newKsession.getProcessInstance(processInstanceId);
		Assert.assertNotNull(reloadedInstance);
		Assert.assertEquals(processInstance.getId(), reloadedInstance.getId());
		Assert.assertEquals(processInstance.getState(), reloadedInstance.getState());
		
		//We compare variables to see they are equal, but different instances
		WorkflowProcessInstance previous = (WorkflowProcessInstance) processInstance;
		WorkflowProcessInstance reloaded = (WorkflowProcessInstance) reloadedInstance;
		Assert.assertNotSame(previous.getVariable("req"), reloaded.getVariable("req"));
		Assert.assertEquals(previous.getVariable("req"), reloaded.getVariable("req"));
	}
	
	@Test
	public void testProcessModelStorage() throws Exception {
		KieServices kservices = KieServices.Factory.get();
		KieStoreServices kstore = kservices.getStoreServices();
		
		//Create an environment variable with all the database configuration content
		Environment environment = kservices.newEnvironment();
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.jbpm.persistence.jpa");
		TransactionManager tm = TransactionManagerServices.getTransactionManager();
		environment.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
		environment.set(EnvironmentName.TRANSACTION_MANAGER, tm);
		
		//Object Marshalling Strategies: Tell the ksession how to store objects added to the engine
		environment.set(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES, new ObjectMarshallingStrategy[] {
			new JPAReadAndWriteStrategy(emf), //if an external model class is an entity, persist it using JPA, just store the id in the blob 
			new SerializablePlaceholderResolverStrategy(ClassObjectMarshallingStrategyAcceptor.DEFAULT) //everything else is serialized in a blob	
		});

		//KieSession configuration object to configure special components
		KieSessionConfiguration ksconf = kservices.newKieSessionConfiguration();
		
		//KieBase we will use to create and restore the runtime from the database 
		KieBase kbase = kservices.getKieClasspathContainer().getKieBase();
		
		//Invocation to create a new persistent session
		KieSession ksession = kstore.newKieSession(kbase, ksconf, environment);

		//Configure the session runtime. We have to do this for every manually created or reloaded session
		TestAsyncWorkItemHandler htHandler = new TestAsyncWorkItemHandler();
		ksession.getWorkItemManager().registerWorkItemHandler("Human Task", htHandler);
		
		//We start a process with a new (entity) parameter
		Map<String, Object> params = new HashMap<String, Object>();
		Requirement req = new Requirement("title", "description");
		params.put("req", req);
		Assert.assertNull(req.getId());
		ProcessInstance processInstance = ksession.startProcess("sprintManagementV2", params);
		//After starting the process with the persistent session, the object is persisted, so an ID is generated
		Assert.assertNotNull(req.getId());
		
		//Make sure the process behaviour is as expected
		Assert.assertNotNull(processInstance);
		Assert.assertEquals(ProcessInstance.STATE_ACTIVE, processInstance.getState());
		
		//Find the object in the database and check it is the same as we wrote 
		Requirement dbReq = emf.createEntityManager().find(Requirement.class, req.getId());
		Assert.assertNotNull(dbReq);
		Assert.assertEquals(req.getName(), dbReq.getName());
		Assert.assertEquals(req.getDescription(), dbReq.getDescription());
	}
	
	@Test
	public void testSessionReloadingWithProcesses() throws Exception {
		KieServices kservices = KieServices.Factory.get();
		KieStoreServices kstore = kservices.getStoreServices();
		
		//Create an environment variable with all the database configuration content
		Environment environment = kservices.newEnvironment();
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.jbpm.persistence.jpa");
		TransactionManager tm = TransactionManagerServices.getTransactionManager();
		environment.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
		environment.set(EnvironmentName.TRANSACTION_MANAGER, tm);
		
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
		Requirement req = new Requirement("title", "description");
		params.put("req", req);
		ProcessInstance processInstance = ksession.startProcess("sprintManagementV2", params);
		//Make sure the process behaviour is as expected
		Assert.assertNotNull(processInstance);
		Assert.assertEquals(ProcessInstance.STATE_ACTIVE, processInstance.getState());
		
		//We get the process instance id
		long processInstanceId = processInstance.getId();
		
		//We reload the Kie session from the database
		//Create an environment variable with all the database configuration content
		Environment newEnvironment = kservices.newEnvironment();
		newEnvironment.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
		newEnvironment.set(EnvironmentName.TRANSACTION_MANAGER, tm);
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
		Assert.assertNotSame(previous.getVariable("req"), reloaded.getVariable("req"));
		Assert.assertEquals(previous.getVariable("req"), reloaded.getVariable("req"));
	}
	
	@Test
	public void testHistoryLogs() throws Exception {
		KieServices kservices = KieServices.Factory.get();
		KieStoreServices kstore = kservices.getStoreServices();
		
		//Create an environment variable with all the database configuration content
		Environment environment = kservices.newEnvironment();
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.jbpm.persistence.jpa");
		TransactionManager tm = TransactionManagerServices.getTransactionManager();
		environment.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
		environment.set(EnvironmentName.TRANSACTION_MANAGER, tm);
		
		//KieSession configuration object to configure special components
		KieSessionConfiguration ksconf = kservices.newKieSessionConfiguration();
		
		//KieBase we will use to create and restore the runtime from the database 
		KieBase kbase = kservices.getKieClasspathContainer().getKieBase();
		
		//Invocation to create a new persistent session
		KieSession ksession = kstore.newKieSession(kbase, ksconf, environment);

		//Configure the session runtime. We have to do this for every manually created or reloaded session
		TestAsyncWorkItemHandler handler = new TestAsyncWorkItemHandler();
		ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);
		ksession.getWorkItemManager().registerWorkItemHandler("compilation", handler);
		ksession.getWorkItemManager().registerWorkItemHandler("deployment", handler);
		ksession.getWorkItemManager().registerWorkItemHandler("notification", handler);
		//We configure the history logs populating module by just adding a listener
		ksession.addEventListener(AuditLoggerFactory.newJPAInstance(environment));
		
		//We start a process
		Map<String, Object> params = new HashMap<String, Object>();
		Requirement req = new Requirement("title", "description");
		params.put("req", req);
		ProcessInstance processInstance = ksession.startProcess("sprintManagementV2", params);
		//Make sure the process behaviour is as expected
		Assert.assertNotNull(processInstance);
		Assert.assertEquals(ProcessInstance.STATE_ACTIVE, processInstance.getState());

		//We get the process instance id
		long processInstanceId = processInstance.getId();

		//We complete the process by completing all its tasks in the correct order:
		//We first develop the requirement
		WorkItem developItem = handler.getWorkItem();
		Map<String, Object> developResults = new HashMap<String, Object>();
		Requirement develReq = (Requirement) developItem.getParameter("develReq");
		develReq.setDeveloperId("mariano");
		developResults.put("reqResult", develReq);
		ksession.getWorkItemManager().completeWorkItem(developItem.getId(), developResults);
		
		//Then we compile it
		WorkItem compileItem = handler.getWorkItem();
		Map<String, Object> compileResults = new HashMap<String, Object>();
		Requirement compileReq = (Requirement) compileItem.getParameter("compileReq");
		compileReq.setCompiled(true);
		compileResults.put("reqResult", compileReq);
		ksession.getWorkItemManager().completeWorkItem(compileItem.getId(), compileResults);
		Assert.assertEquals(ProcessInstance.STATE_ACTIVE, processInstance.getState());
		
		//Then we deploy it
		WorkItem deployItem = handler.getWorkItem();
		Map<String, Object> deployResults = new HashMap<String, Object>();
		Requirement deployReq = (Requirement) deployItem.getParameter("deployReq");
		deployReq.setDeployed(true);
		deployResults.put("reqResult", deployReq);
		ksession.getWorkItemManager().completeWorkItem(deployItem.getId(), deployResults);
		Assert.assertEquals(ProcessInstance.STATE_ACTIVE, processInstance.getState());
		
		//Finally we test it
		WorkItem testItem = handler.getWorkItem();
		Map<String, Object> testResults = new HashMap<String, Object>();
		Requirement testReq = (Requirement) testItem.getParameter("testReq");
		testReq.setBugs(new ArrayList<String>());
		testResults.put("reqResult", testReq);
		ksession.getWorkItemManager().completeWorkItem(testItem.getId(), testResults);
		
		//At this point, the process is completed.
		//After a process is finished, it is removed from the database:
		ProcessInstance reloadedRuntimeInstance = ksession.getProcessInstance(processInstanceId);
		Assert.assertNull(reloadedRuntimeInstance);
		
		//The only way to retrieve its state is through the history logs
		JPAAuditLogService auditService = new JPAAuditLogService(emf);
		ProcessInstanceLog reloadedHistoryInstance = auditService.findProcessInstance(processInstanceId);
		Assert.assertNotNull(reloadedHistoryInstance);
		Assert.assertEquals(processInstanceId, reloadedHistoryInstance.getProcessInstanceId());
		Assert.assertEquals(ProcessInstance.STATE_COMPLETED, reloadedHistoryInstance.getStatus().intValue());
	}
}
