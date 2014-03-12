package com.wordpress.marianbuenosayres.ht;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.wordpress.marianbuenosayres.model.Requirement;
import com.wordpress.marianbuenosayres.handlers.CompilationWorkItemHandler;
import com.wordpress.marianbuenosayres.handlers.DeploymentWorkItemHandler;
import com.wordpress.marianbuenosayres.handlers.NotificationWorkItemHandler;

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
        //create mock users and groups
        Properties userGroups = new Properties();
        userGroups.setProperty("john", "developers");
        userGroups.setProperty("mary", "testers");
        userGroups.setProperty("Administrator", "Administrators,developers,testers");
        JBossUserGroupCallbackImpl userGroupCallback = new JBossUserGroupCallbackImpl(userGroups);

        //start taskService
        TaskService taskService = HumanTaskServiceFactory.newTaskServiceConfigurator()
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
        ksession.getWorkItemManager().registerWorkItemHandler("compilation", new CompilationWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("deployment", new DeploymentWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("notification", new NotificationWorkItemHandler());

        Map<String, Object> params = new HashMap<String, Object>();
        Requirement req = new Requirement("test requirement", "This is a test requirement");
        params.put("req", req);
        WorkflowProcessInstance instance = (WorkflowProcessInstance) ksession.startProcess("sprintManagement", params);
        assertEquals(ProcessInstance.STATE_ACTIVE, instance.getState());

        //developers (john) can see the requirement to be done
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
        results1.put("reqResult", req);
        taskService.complete(firstTask.getId(), "john", results1);
        //up to here, all direct interaction is handled through task service
		
        // the handler is in charge of getting to the next task
        // the process is still active and we have the document variable loaded
        assertEquals(ProcessInstance.STATE_ACTIVE, instance.getState());
        assertNotNull(instance.getVariable("req"));

        //now we have the testing task which can only be assigned to mary
        List<TaskSummary> marysTasks = taskService.getTasksAssignedAsPotentialOwner("mary", "en-UK");
        assertNotNull(marysTasks);
        assertEquals(1, marysTasks.size());

        TaskSummary marysTask = marysTasks.iterator().next();
        assertEquals(Status.Ready, marysTask.getStatus());
        taskService.claim(marysTask.getId(), "mary");
        taskService.start(marysTask.getId(), "mary");

        Map<String, Object> results2 = new HashMap<String, Object>();
        req.addBug("bug 1");
        results2.put("reqResult", req);
        taskService.complete(marysTask.getId(), "mary", results2);

        //the process is still active, now waiting for a developer to solve a bug
        assertEquals(ProcessInstance.STATE_ACTIVE, instance.getState());

        //now that the requirement has bugs, there's a task assigned so john bugfixes the issue
        List<TaskSummary> bugfixTasks = taskService.getTasksAssignedAsPotentialOwner("john", "en-UK");
        assertNotNull(bugfixTasks);
        assertEquals(1, bugfixTasks.size());
        TaskSummary bugfixTask = bugfixTasks.iterator().next();

        System.out.println("=====> Status = " + bugfixTask.getStatus());

        taskService.claim(bugfixTask.getId(), "john");
        taskService.start(bugfixTask.getId(), "john");
        Map<String, Object> results3 = new HashMap<String, Object>();
        req.resolveBug("bug 1", "bug fixed");
        results3.put("reqResult", req);
        taskService.complete(bugfixTask.getId(), "john", results3);

        //now that the bugfix is completed, the tester can retest the issue
        List<TaskSummary> marysTasks2 = taskService.getTasksAssignedAsPotentialOwner("mary", "en-UK");
        assertNotNull(marysTasks2);
        assertEquals(1, marysTasks2.size());

        TaskSummary marysTask2 = marysTasks2.iterator().next();
        assertEquals(Status.Ready, marysTask2.getStatus());
        taskService.claim(marysTask2.getId(), "mary");
        taskService.start(marysTask2.getId(), "mary");

        Map<String, Object> results4 = new HashMap<String, Object>();
        results4.put("reqResult", req);
        taskService.complete(marysTask2.getId(), "mary", results4);

        assertEquals(ProcessInstance.STATE_COMPLETED, instance.getState());
    }
}

