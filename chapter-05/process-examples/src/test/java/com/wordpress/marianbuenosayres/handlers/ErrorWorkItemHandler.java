package com.wordpress.marianbuenosayres.handlers;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

public class ErrorWorkItemHandler implements WorkItemHandler {

	@Override
	public void abortWorkItem(WorkItem item, WorkItemManager manager) {
	}

	@Override
	public void executeWorkItem(WorkItem item, WorkItemManager manager) {
		throw new RuntimeException("Failure at item " + item.getId());
	}

}
