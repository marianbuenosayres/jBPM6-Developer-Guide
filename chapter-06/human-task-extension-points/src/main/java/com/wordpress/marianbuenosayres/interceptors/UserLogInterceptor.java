package com.wordpress.marianbuenosayres.interceptors;

import java.util.LinkedList;
import java.util.List;

import org.drools.core.command.impl.AbstractInterceptor;
import org.jbpm.services.task.commands.CompositeCommand;
import org.jbpm.services.task.commands.TaskCommand;
import org.kie.api.command.Command;

public class UserLogInterceptor extends AbstractInterceptor {

	private final List<OperationLog> logs = new LinkedList<OperationLog>();
	
	@Override
	public <T> T execute(Command<T> command) {
		String userId = getUserId(command);
		String operation = getOperationName(command);
		if (userId != null) {
			logs.add(new OperationLog(userId, operation));
		}
		return executeNext(command);
	}
	
	protected String getOperationName(Command<?> command) {
		Command<?> cmd = command;
		if (command instanceof CompositeCommand) {
			CompositeCommand<?> compCmd = (CompositeCommand<?>) command;
			cmd = compCmd.getMainCommand();
		}
		return cmd.getClass().getSimpleName().replace("Command", "");
	}
	
	protected String getUserId(Command<?> command) {
		if (command instanceof CompositeCommand) {
			CompositeCommand<?> compCmd = (CompositeCommand<?>) command;
			String userId = getUserId(compCmd.getMainCommand());
			if (userId != null) {
				return userId;
			}
			for (Command<?> cmd : compCmd.getCommands()) {
				userId = getUserId(cmd);
				if (userId != null) {
					return userId;
				}
			}
		}
		if (command instanceof TaskCommand) {
			return ((TaskCommand<?>) command).getUserId();
		}
		return null;
	}

	public List<OperationLog> getLogs() {
		return logs;
	}
	
}
