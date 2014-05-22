package com.wordpress.marianbuenosayres.handlers;

import java.util.Map;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

public class DomainXWorkItemHandler implements WorkItemHandler {

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		Object domainXParameter = workItem.getParameter("domainXParameter");

		//Your specific domain operations should go here
		
		Map<String, Object> results = workItem.getResults();
		results.put("domainXResult", domainXParameter);
		manager.completeWorkItem(workItem.getId(), results);
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		//Do nothing
	}

}
