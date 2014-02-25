package com.wordpress.marianbuenosayres.handlers;

import java.util.HashMap;
import java.util.Map;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

/**
 * Mock Work Item Handler used for testing purposes. When executed it saves
 * all the input parameters received.
 */
public class MockWorkItemHandler implements WorkItemHandler{

    private long workItemId;
    private WorkItemManager manager;
    
    private Map<String,Object> inputParameters;
    
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        //save the info needed to complete the work item handler later
        this.workItemId = workItem.getId();
        this.manager = manager;
        
        //clear any previous input
        this.inputParameters = new HashMap<String, Object>();
        
        //save the map of received parameters
        for (Map.Entry<String, Object> entry : workItem.getParameters().entrySet()) {
            inputParameters.put(entry.getKey(), entry.getValue());
        }
        
        //do not complete the work item handler -> Asynchronous behavior.
    }

    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
    }
    
    /**
     * Completes the Work Item this handler makes reference to.
     * @param results 
     */
    public void completeWorkItem(Map<String, Object> results){
        this.manager.completeWorkItem(workItemId, results);
    }

    public Map<String, Object> getInputParameters() {
        return inputParameters;
    }
    
    public Object getInputParameter(String parameterName) {
        return inputParameters.get(parameterName);
    }

}
