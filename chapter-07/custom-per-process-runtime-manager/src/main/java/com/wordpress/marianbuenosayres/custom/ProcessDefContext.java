package com.wordpress.marianbuenosayres.custom;

import org.kie.api.runtime.manager.Context;

public class ProcessDefContext implements Context<String> {

    private String processId;
    
    public ProcessDefContext(String processId) {
        this.processId = processId;
    }
    
    @Override
    public String getContextId() {
        return processId;
    }

    public void setContextId(String id) {
        this.processId = id;
    }
    
    /**
     * Returns new instance of <code>ProcessDefContext</code> with id of a process definition
     * @param processId actual identifier of process definition
     * @return
     */
    public static ProcessDefContext get(String processId) {
        return new ProcessDefContext(processId);
    }

}