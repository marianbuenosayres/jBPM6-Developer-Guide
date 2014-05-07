package com.wordpress.marianbuenosayres.procrules;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

public class TestAsyncWorkItemHandler implements WorkItemHandler {

	private WorkItem workItem;

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		this.workItem = workItem;		
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		this.workItem = null;
	}
	
	public WorkItem getWorkItem() {
		WorkItem retval = workItem;
		workItem = null;
		return retval;
	}

}
