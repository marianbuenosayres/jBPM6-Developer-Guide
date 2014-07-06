package com.wordpress.marianbuenosayres.custom;

import org.drools.core.time.TimerService;
import org.jbpm.process.core.timer.GlobalSchedulerService;
import org.jbpm.process.core.timer.TimerServiceRegistry;
import org.jbpm.process.core.timer.impl.GlobalTimerService;
import org.jbpm.runtime.manager.api.SchedulerProvider;
import org.jbpm.runtime.manager.impl.AbstractRuntimeManager;
import org.jbpm.runtime.manager.impl.SimpleRuntimeEnvironment;
import org.jbpm.runtime.manager.impl.factory.InMemorySessionFactory;
import org.jbpm.runtime.manager.impl.factory.JPASessionFactory;
import org.jbpm.runtime.manager.impl.factory.LocalTaskServiceFactory;
import org.jbpm.runtime.manager.impl.tx.TransactionAwareSchedulerServiceInterceptor;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.internal.runtime.manager.SessionFactory;
import org.kie.internal.runtime.manager.TaskServiceFactory;

/**
 * This is a factory class to create our own PerProcessDefinitionRuntimeManager
 * objects the same way we would create any built-in runtime manager implementations.
 * The newPerProcessDefinitionRuntimeManager methods provide you with helper methods
 * to create new instances of this custom runtime manager.
 */
public class CustomRMFactory {

	private static final CustomRMFactory INSTANCE = new CustomRMFactory();
	
	public static CustomRMFactory getInstance() {
		return INSTANCE;
	}
	
    public RuntimeManager newPerProcessDefinitionRuntimeManager(RuntimeEnvironment environment) {
        return newPerProcessDefinitionRuntimeManager(environment, "default-per-process-def");
    }
	
    public RuntimeManager newPerProcessDefinitionRuntimeManager(RuntimeEnvironment environment, String identifier) {
        SessionFactory factory = getSessionFactory(environment);
        //LocalTaskServiceFactory creates a local task service, which will communicate locally with the KIE Session
        TaskServiceFactory taskServiceFactory = new LocalTaskServiceFactory(environment);
        //We instance our runtime manager with all required elements
        RuntimeManager manager = new PerProcessDefinitionRuntimeManager(environment, factory, taskServiceFactory, identifier);
        //Helper method to create persistent timers if necessary
        initTimerService(environment, manager);
        ((AbstractRuntimeManager) manager).init();
        return manager;
    }
    
    /**
     * Creates a session factory. Depending on the environment, it will just create a simple session
     * or try to create/load a persistence session from a JPA database
     */
    protected SessionFactory getSessionFactory(RuntimeEnvironment environment) {
        SessionFactory factory = null;
        if (environment.usePersistence()) {
            factory = new JPASessionFactory(environment);
        } else {
            factory = new InMemorySessionFactory(environment);
        }
        
        return factory;
    }

    /**
     * This code takes care of configuring the runtime environment to configure the timer service
     * (an internal component of the KIE Session used to measure how time progresses, used
     * for signals and time based rules). It will create a specific timer service, separate from any
     * other, exclusively for the new runitme manager, and if the configuration makes it a transactional
     * object, it adds an interceptor to the timer service to store scheduled tasks in the database as well.
     */
    protected void initTimerService(RuntimeEnvironment environment, RuntimeManager manager) {
        if (environment instanceof SchedulerProvider) {
            GlobalSchedulerService schedulerService = ((SchedulerProvider) environment).getSchedulerService();  
            if (schedulerService != null) {
                TimerService globalTs = new GlobalTimerService(manager, schedulerService);
                String timerServiceId = manager.getIdentifier()  + TimerServiceRegistry.TIMER_SERVICE_SUFFIX;
                // and register it in the registry under 'default' key
                TimerServiceRegistry.getInstance().registerTimerService(timerServiceId, globalTs);
                ((SimpleRuntimeEnvironment)environment).addToConfiguration("drools.timerService", 
                        "new org.jbpm.process.core.timer.impl.RegisteredTimerServiceDelegate(\""+timerServiceId+"\")");
                
                if (!schedulerService.isTransactional()) {
                    schedulerService.setInterceptor(new TransactionAwareSchedulerServiceInterceptor(environment, schedulerService));
                }
            }
        }
    }
}
