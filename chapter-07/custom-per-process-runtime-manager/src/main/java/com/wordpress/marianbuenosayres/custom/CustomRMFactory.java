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
        TaskServiceFactory taskServiceFactory = new LocalTaskServiceFactory(environment);
        RuntimeManager manager = new PerProcessDefinitionRuntimeManager(environment, factory, taskServiceFactory, identifier);
        initTimerService(environment, manager);
        ((AbstractRuntimeManager) manager).init();
        return manager;
    }
    
    protected SessionFactory getSessionFactory(RuntimeEnvironment environment) {
        SessionFactory factory = null;
        if (environment.usePersistence()) {
            factory = new JPASessionFactory(environment);
        } else {
            factory = new InMemorySessionFactory(environment);
        }
        
        return factory;
    }

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
