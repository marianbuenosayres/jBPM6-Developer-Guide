package com.wordpress.marianbuenosayres.handlers;

import java.util.HashMap;
import java.util.Map;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import com.wordpress.marianbuenosayres.model.Requirement;

public class NotificationWorkItemHandler implements WorkItemHandler {

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		System.out.println("Notification of failure on requirement");
		Requirement req = (Requirement) workItem.getParameter("notifyReq");
		req.setCompiled(false);
		req.setTested(false);
		System.out.println(req);
		Map<String, Object> results = workItem.getResults();
		if (results == null) {
			results = new HashMap<String, Object>();
		}
		results.put("reqResult", req);
		manager.completeWorkItem(workItem.getId(), results);
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
	}

}
