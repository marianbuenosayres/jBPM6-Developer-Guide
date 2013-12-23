package com.wordpress.marianbuenosayres.handlers;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

public class TestAsyncWorkItemHandler implements WorkItemHandler {

	private WorkItem item;
	
	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		System.out.println("Execution is called to start a generic or user task." +
			"It won't be deemed completed until manager.completeWorkItem(...) is " +
			"called");
		System.out.println("This execution has the following parameters:");
		if (workItem.getParameters().isEmpty()) {
			System.out.println("No parameters in this execution");
		}
		for (String key : workItem.getParameters().keySet()) {
			System.out.println(" - " + key + " => " + workItem.getParameter(key));
		}
		this.item = workItem;
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		System.out.println("Aborting is only called when a process instance is " +
				"aborted while an asynchronous task is still waiting for completion");
	}

	public WorkItem getItem() {
		WorkItem retval = item;
		item = null;
		return retval;
	}
}
