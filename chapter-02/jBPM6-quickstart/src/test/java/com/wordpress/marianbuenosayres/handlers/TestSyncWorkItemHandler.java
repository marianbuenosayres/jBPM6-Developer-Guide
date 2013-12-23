package com.wordpress.marianbuenosayres.handlers;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

public class TestSyncWorkItemHandler implements WorkItemHandler {

	private int invocationCount = 0;
	
	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		invocationCount++;
		System.out.println("Execution is called to start a generic or user task." +
				"Since this implementation calls manager.completeWorkItem(...), " +
				"it will consider the step completed after invoking it and will " +
				"continue with the process execution");
		manager.completeWorkItem(workItem.getId(), workItem.getResults());
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		System.out.println("Aborting is only called when a process instance is " +
			"aborted while an asynchronous task is still waiting for completion. " +
			"In a synchronous Work Item Hanlder it might only be called by a separate " +
			"thread while the 'executeWorkItem' is still running.");
	}

	public int getInvocationCount() {
		return invocationCount;
	}

}
