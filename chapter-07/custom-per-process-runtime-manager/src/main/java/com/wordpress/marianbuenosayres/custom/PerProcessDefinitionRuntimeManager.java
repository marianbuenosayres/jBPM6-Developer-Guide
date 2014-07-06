package com.wordpress.marianbuenosayres.custom;

import java.util.HashMap;
import java.util.Map;

import org.drools.core.command.CommandService;
import org.drools.core.command.SingleSessionCommandService;
import org.drools.core.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.impl.KnowledgeCommandContext;
import org.drools.persistence.OrderedTransactionSynchronization;
import org.drools.persistence.TransactionManagerHelper;
import org.drools.persistence.jta.JtaTransactionManager;
import org.jbpm.runtime.manager.impl.AbstractRuntimeManager;
import org.jbpm.runtime.manager.impl.RuntimeEngineImpl;
import org.jbpm.runtime.manager.impl.factory.CDITaskServiceFactory;
import org.jbpm.runtime.manager.impl.tx.DisposeSessionTransactionSynchronization;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.internal.runtime.manager.Disposable;
import org.kie.internal.runtime.manager.Mapper;
import org.kie.internal.runtime.manager.SessionFactory;
import org.kie.internal.runtime.manager.TaskServiceFactory;
import org.kie.internal.task.api.InternalTaskService;

/**
 * Implements a RuntimeManager which will manage sessions on a per process definition basis.
 * It is created following the structure of the PerProcessInstanceRuntimeManager, but changing
 * IDs to be managed through process definition ID instead of process instance ID.
 */
public class PerProcessDefinitionRuntimeManager extends AbstractRuntimeManager {
    private SessionFactory factory;
    private TaskServiceFactory taskServiceFactory;
    
    private static ThreadLocal<Map<Object, RuntimeEngine>> local = new ThreadLocal<Map<Object, RuntimeEngine>>();
    
    private Mapper mapper;
    
    public PerProcessDefinitionRuntimeManager(RuntimeEnvironment environment, SessionFactory factory, TaskServiceFactory taskServiceFactory, String identifier) {
        super(environment, identifier);
        this.factory = factory;
        this.taskServiceFactory = taskServiceFactory;
        this.mapper = ((org.kie.internal.runtime.manager.RuntimeEnvironment)environment).getMapper();
        this.registry.register(this);
    }
    
    @Override
    public RuntimeEngine getRuntimeEngine(Context<?> context) {
    	if (isClosed()) {
    		throw new IllegalStateException("Runtime manager " + identifier + " is already closed");
    	}
        Object contextId = context.getContextId();
        Integer ksessionId = null;
        KieSession ksession = null;
        //If no context is used (EmptyContext) we asume we need to create a new KIE Session.
        if (contextId == null) {
        	ksession = factory.newKieSession();
        	ksessionId = ksession.getId();
        } else { //Otherwhise, we try to find it through the database or thread local variables
        	RuntimeEngine localRuntime = findLocalRuntime(contextId);
        	if (localRuntime != null) {
        		return localRuntime;
        	}
        	ksessionId = mapper.findMapping(context, getIdentifier());
        	if (ksessionId == null) { //if we cannot find a KIE session, we will create one
        		ksession = factory.newKieSession();
        		ksessionId = ksession.getId();
        	} else {
        		ksession = factory.findKieSessionById(ksessionId);
        	}
        }
        //After we created the KIE Session, we will create a new TaskService. Since it is basically
        //a stateless service connected to a database, we don't need to check any configurations, but
        //use them directly instead to create a new TaskService very time
        InternalTaskService internalTaskService = (InternalTaskService) taskServiceFactory.newTaskService();
        //Configures connections between the task service and the runtime manager
        configureRuntimeOnTaskService(internalTaskService);
        RuntimeEngine runtime = new RuntimeEngineImpl(ksession, internalTaskService);
        ((RuntimeEngineImpl) runtime).setManager(this);
        //used to clear internal data once a transaction in the KIE Session or Task Service is commited
        registerDisposeCallback(runtime, new DisposeSessionTransactionSynchronization(this, runtime));
        registerItems(runtime);
        attachManager(runtime);
        //We store it in a thread local after creating all the configuration (for caching purposes) 
        saveLocalRuntime(contextId, runtime);
        //This event listener will keep a register of the mapping between process definition ID and KIE Session ID:
        ksession.addEventListener(new MaintainMappingListener(ksessionId));
        return runtime;
    }
    
    @Override
    public void validate(KieSession ksession, Context<?> context) throws IllegalStateException {
    	if (isClosed()) {
    		throw new IllegalStateException("Runtime manager " + identifier + " is already closed");
    	}
        if (context == null || context.getContextId() == null) {
            return;
        }
        Integer ksessionId = mapper.findMapping(context, getIdentifier());
                
        if (ksessionId == null) {
            return;
        }
        if (ksession.getId() != ksessionId) {
            throw new IllegalStateException("Invalid session was used for this context " + context);
        }
    }

    @Override
    public void disposeRuntimeEngine(RuntimeEngine runtime) {
    	if (isClosed()) {
    		throw new IllegalStateException("Runtime manager " + identifier + " is already closed");
    	}
        removeLocalRuntime(runtime);
        if (runtime instanceof Disposable && environment.usePersistence()) {
            ((Disposable) runtime).dispose();
        }
    }

    @Override
    public void close() {
        try {
            if (taskServiceFactory instanceof CDITaskServiceFactory) {
                // if it's CDI based (meaning single application scoped bean) we need to unregister context
                removeRuntimeFromTaskService((InternalTaskService) taskServiceFactory.newTaskService());
            }
        } catch(Exception e) {
           // do nothing 
        }
        super.close();
        factory.close();
    }
    
    private class MaintainMappingListener extends DefaultProcessEventListener {

        private Integer ksessionId;
        
        MaintainMappingListener(Integer ksessionId) {
            this.ksessionId = ksessionId;
        }

        @Override
        public void beforeProcessStarted(ProcessStartedEvent event) {
        	String processId = event.getProcessInstance().getProcessId();
        	if (mapper.findContextId(ksessionId, getIdentifier()) == null) { 
                        //if no relation between process def ID and KIE Session is found, we store it
        		mapper.saveMapping(ProcessDefContext.get(processId), ksessionId, getIdentifier());
        	}
        }
    }

    public SessionFactory getFactory() {
        return factory;
    }

    public void setFactory(SessionFactory factory) {
        this.factory = factory;
    }

    public TaskServiceFactory getTaskServiceFactory() {
        return taskServiceFactory;
    }

    public void setTaskServiceFactory(TaskServiceFactory taskServiceFactory) {
        this.taskServiceFactory = taskServiceFactory;
    }

    public Mapper getMapper() {
        return mapper;
    }

    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }
    
    protected RuntimeEngine findLocalRuntime(Object processId) {
        if (processId == null) {
            return null;
        }
        Map<Object, RuntimeEngine> map = local.get();
        if (map == null) {
            return null;
        } else {
            return map.get(processId);
        }
    }
    
    protected void saveLocalRuntime(Object processId, RuntimeEngine runtime) {
        // since this manager is strictly for process definition ids it should only store 
        // process definition ids as local cache keys
        if (processId == null || !(processId instanceof String)) {
            return;
        }
        Map<Object, RuntimeEngine> map = local.get();
        if (map == null) {
            map = new HashMap<Object, RuntimeEngine>();
            local.set(map);
        } 
        map.put(processId, runtime);
    }
    
    protected void removeLocalRuntime(RuntimeEngine runtime) {
        Map<Object, RuntimeEngine> map = local.get();
        Object keyToRemove = "";
        if (map != null) {
            for (Map.Entry<Object, RuntimeEngine> entry : map.entrySet()) {
                if (runtime.equals(entry.getValue())) {
                    keyToRemove = entry.getKey();
                    break;
                }
            }
            map.remove(keyToRemove);
        }
    }
    
    @Override
    public void init() {
        // need to init one session to bootstrap all case - such as start timers
        KieSession ksession = factory.newKieSession();
        ksession.execute(new GenericCommand<Void>() {            
            private static final long serialVersionUID = 1L;

            @Override
            public Void execute(org.kie.internal.command.Context context) {
                //This command is used to register a transaction interceptor, which will destroy the 
                //internal KIE Session of a persistent session when the transaction is completed. It is
                //used by the Drools and jBPM code to clean up elements in memory fast
                final KieSession ksession = ((KnowledgeCommandContext) context).getKieSession();
                if (hasEnvironmentEntry("IS_JTA_TRANSACTION", false)) {
                	ksession.destroy();
                	return null;
            	}
                JtaTransactionManager tm = new JtaTransactionManager(null, null, null);
                if (tm.getStatus() != JtaTransactionManager.STATUS_NO_TRANSACTION
                        && tm.getStatus() != JtaTransactionManager.STATUS_ROLLEDBACK
                        && tm.getStatus() != JtaTransactionManager.STATUS_COMMITTED) {
                	TransactionManagerHelper.registerTransactionSyncInContainer(tm, new OrderedTransactionSynchronization(5) {
                        @Override
                        public void beforeCompletion() {
                            if (ksession instanceof CommandBasedStatefulKnowledgeSession) {
                                CommandService commandService = ((CommandBasedStatefulKnowledgeSession) ksession).getCommandService();
                                ((SingleSessionCommandService) commandService).destroy();
                             }                            
                        }
                        
                        @Override
                        public void afterCompletion(int arg0) {
                            ksession.dispose();
                            
                        }
					});
                } else {
                    ksession.destroy();
                }
                return null;
            }
        });
    }
}
