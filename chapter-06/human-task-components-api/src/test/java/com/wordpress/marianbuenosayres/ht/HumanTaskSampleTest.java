package com.wordpress.marianbuenosayres.ht;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jbpm.services.task.HumanTaskServiceFactory;
import org.jbpm.services.task.identity.JBossUserGroupCallbackImpl;
import org.jbpm.services.task.wih.NonManagedLocalHTWorkItemHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;

import bitronix.tm.resource.jdbc.PoolingDataSource;

public class HumanTaskSampleTest {

	private PoolingDataSource ds;
	
	@Before
	public void setUp() throws Exception {
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
	public void testLocalTaskService() throws Exception {
		
		// Create the task service 
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.jbpm.services.task");
		//create mock groups
		
		Properties userGroups = new Properties();
		userGroups.setProperty("john", "writer");
		userGroups.setProperty("mary", "translator");
		userGroups.setProperty("BOSS", "users");
		userGroups.setProperty("Administrator", "writer,translator,users");
		JBossUserGroupCallbackImpl userGroupCallback = new JBossUserGroupCallbackImpl(userGroups);
		
		//start taskService
		TaskService taskService =HumanTaskServiceFactory.newTaskServiceConfigurator()
			.entityManagerFactory(emf)
			.userGroupCallback(userGroupCallback)
			.getTaskService();
		
		// Create the KIE session
		KieServices ks = KieServices.Factory.get();
    	KieContainer kc = ks.getKieClasspathContainer();
    	KieSession ksession = kc.newKieSession();
		
		//create the work item handler for human task
		WorkItemHandler humanTaskHandler = new NonManagedLocalHTWorkItemHandler(ksession, taskService);
		ksession.getWorkItemManager().registerWorkItemHandler("Human Task", humanTaskHandler);
		
		WorkflowProcessInstance instance = (WorkflowProcessInstance) ksession.startProcess("humanTaskSampleProcess", null);
		assertEquals(ProcessInstance.STATE_ACTIVE, instance.getState());
		
		List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner("john", "en-UK");
		assertNotNull(tasks);
		assertEquals(1, tasks.size());
		
		//see the task as part of the group tasks (Ready state)
		TaskSummary firstTask = tasks.iterator().next();
		assertEquals(Status.Ready, firstTask.getStatus());
		
		//claim the task as a personal task (Reserved state)
		taskService.claim(firstTask.getId(), "john");
		Task firstTaskAssigned = taskService.getTaskById(firstTask.getId());
		assertEquals(Status.Reserved, firstTaskAssigned.getTaskData().getStatus());
		assertEquals("john", firstTaskAssigned.getTaskData().getActualOwner().getId());

		//start the task (InProgress state)
		taskService.start(firstTask.getId(), "john");
		Task firstTaskStarted = taskService.getTaskById(firstTask.getId());
		assertEquals(Status.InProgress, firstTaskStarted.getTaskData().getStatus());
		
		Map<String, Object> results1 = new HashMap<String, Object>();
		results1.put("writtenDocument", "Hello World");
		taskService.complete(firstTask.getId(), "john", results1);
		//up to here, all direct interaction is handled through task service
		
		// the handler is in charge of getting to the next task
		// the process is still active and we have the document variable loaded
		assertEquals(ProcessInstance.STATE_ACTIVE, instance.getState());
		assertNotNull(instance.getVariable("document"));
		assertEquals("Hello World", instance.getVariable("document"));
		
		//now we have the translation task and the review task
		//translation task is the only one that can be assigned to mary
		List<TaskSummary> marysTasks = taskService.getTasksAssignedAsPotentialOwner("mary", "en-UK");
		assertNotNull(marysTasks);
		assertEquals(1, marysTasks.size());
		
		TaskSummary marysTask = marysTasks.iterator().next();
		assertEquals(Status.Ready, marysTask.getStatus());
		taskService.claim(marysTask.getId(), "mary");
		taskService.start(marysTask.getId(), "mary");

		Map<String, Object> results2 = new HashMap<String, Object>();
		results2.put("translatedDocument", "Hola Mundo");
		taskService.complete(marysTask.getId(), "mary", results2);
		
		//the process is still active
		assertEquals(ProcessInstance.STATE_ACTIVE, instance.getState());
		assertEquals("Hola Mundo", instance.getVariable("translation"));
		
		//now that the second task is completed, there's still one task for the BOSS user
		List<TaskSummary> bossTasks = taskService.getTasksOwned("BOSS", "en-UK");
		assertNotNull(bossTasks);
		assertEquals(1, bossTasks.size());
		TaskSummary bossTask = bossTasks.iterator().next();
		
		taskService.start(bossTask.getId(), "BOSS");
		Map<String, Object> results3 = new HashMap<String, Object>();
		results3.put("revisions", "OK");
		taskService.complete(bossTask.getId(), "BOSS", results3);
		
		//now that the three tasks are completed, the process is completed as well
		assertEquals(ProcessInstance.STATE_COMPLETED, instance.getState());
		assertNotNull(instance.getVariable("revision"));
		assertEquals("OK", instance.getVariable("revision"));
	}
}
