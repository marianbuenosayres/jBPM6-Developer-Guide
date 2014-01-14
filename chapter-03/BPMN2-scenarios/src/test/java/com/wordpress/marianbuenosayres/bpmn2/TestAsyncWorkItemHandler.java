package com.wordpress.marianbuenosayres.bpmn2;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

public class TestAsyncWorkItemHandler implements WorkItemHandler {

	private WorkItem item = null;
	
	@Override
	public void abortWorkItem(WorkItem item, WorkItemManager manager) {
	}

	@Override
	public void executeWorkItem(WorkItem item, WorkItemManager manager) {
		this.item = item;
	}
	
	public WorkItem getItem() {
		WorkItem retval = item;
		item = null;
		return retval;
	}

}
