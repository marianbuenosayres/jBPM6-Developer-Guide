package com.wordpress.marianbuenosayres.handlers;

import org.kie.api.runtime.process.WorkItem;
import org.kie.internal.executor.api.Command;
import org.kie.internal.executor.api.CommandContext;
import org.kie.internal.executor.api.ExecutionResults;

public class DomainXExecCommand implements Command {

	@Override
	public ExecutionResults execute(CommandContext context) throws Exception {
		WorkItem workItem = (WorkItem) context.getData("workItem");
		Object domainXParameter = workItem.getParameter("domainXParameter");

		//Your specific domain operations should go here
		
		ExecutionResults results = new ExecutionResults();
		results.setData("domainXResult", domainXParameter);
		return results;
	}

}
