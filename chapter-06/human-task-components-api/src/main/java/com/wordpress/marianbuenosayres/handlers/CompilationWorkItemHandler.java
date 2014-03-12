package com.wordpress.marianbuenosayres.handlers;

import java.util.HashMap;
import java.util.Map;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import com.wordpress.marianbuenosayres.model.Requirement;

public class CompilationWorkItemHandler implements WorkItemHandler {

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		Requirement req = (Requirement) workItem.getParameter("compileReq");
		System.out.println("Compiling the process...");
		req.setCompiled(true);
		System.out.println("Compilation done");;
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
