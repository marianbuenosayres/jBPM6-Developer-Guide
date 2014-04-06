package com.wordpress.marianbuenosayres.listeners;

import java.util.LinkedList;
import java.util.List;

import org.jbpm.services.task.lifecycle.listeners.TaskLifeCycleEventListener;
import org.kie.api.task.TaskEvent;

public class LogTaskChangeListener implements TaskLifeCycleEventListener {

	private final List<TaskLog> logs = new LinkedList<TaskLog>();
	
	@Override
	public void beforeTaskActivatedEvent(TaskEvent event) { }

	@Override
	public void beforeTaskClaimedEvent(TaskEvent event) { }

	@Override
	public void beforeTaskSkippedEvent(TaskEvent event) { }

	@Override
	public void beforeTaskStartedEvent(TaskEvent event) { }

	@Override
	public void beforeTaskStoppedEvent(TaskEvent event) { }

	@Override
	public void beforeTaskCompletedEvent(TaskEvent event) { }

	@Override
	public void beforeTaskFailedEvent(TaskEvent event) { }

	@Override
	public void beforeTaskAddedEvent(TaskEvent event) { }

	@Override
	public void beforeTaskExitedEvent(TaskEvent event) { }

	@Override
	public void beforeTaskReleasedEvent(TaskEvent event) { }

	@Override
	public void beforeTaskResumedEvent(TaskEvent event) { }

	@Override
	public void beforeTaskSuspendedEvent(TaskEvent event) { }

	@Override
	public void beforeTaskForwardedEvent(TaskEvent event) { }

	@Override
	public void beforeTaskDelegatedEvent(TaskEvent event) { }

	@Override
	public void afterTaskActivatedEvent(TaskEvent event) {
		logs.add(new TaskLog(event.getTask().getId(), "TaskActivated"));
	}

	@Override
	public void afterTaskClaimedEvent(TaskEvent event) {
		logs.add(new TaskLog(event.getTask().getId(), "TaskClaimed"));
	}

	@Override
	public void afterTaskSkippedEvent(TaskEvent event) {
		logs.add(new TaskLog(event.getTask().getId(), "TaskSkipped"));
	}

	@Override
	public void afterTaskStartedEvent(TaskEvent event) {
		logs.add(new TaskLog(event.getTask().getId(), "TaskStarted"));
	}

	@Override
	public void afterTaskStoppedEvent(TaskEvent event) {
		logs.add(new TaskLog(event.getTask().getId(), "TaskStopped"));
	}

	@Override
	public void afterTaskCompletedEvent(TaskEvent event) {
		logs.add(new TaskLog(event.getTask().getId(), "TaskCompleted"));
	}

	@Override
	public void afterTaskFailedEvent(TaskEvent event) {
		logs.add(new TaskLog(event.getTask().getId(), "TaskFailed"));
	}

	@Override
	public void afterTaskAddedEvent(TaskEvent event) {
		logs.add(new TaskLog(event.getTask().getId(), "TaskAdded"));
	}

	@Override
	public void afterTaskExitedEvent(TaskEvent event) {
		logs.add(new TaskLog(event.getTask().getId(), "TaskExited"));
	}

	@Override
	public void afterTaskReleasedEvent(TaskEvent event) {
		logs.add(new TaskLog(event.getTask().getId(), "TaskReleased"));
	}

	@Override
	public void afterTaskResumedEvent(TaskEvent event) {
		logs.add(new TaskLog(event.getTask().getId(), "TaskResumed"));
	}

	@Override
	public void afterTaskSuspendedEvent(TaskEvent event) {
		logs.add(new TaskLog(event.getTask().getId(), "TaskSuspended"));
	}

	@Override
	public void afterTaskForwardedEvent(TaskEvent event) {
		logs.add(new TaskLog(event.getTask().getId(), "TaskForwarded"));
	}

	@Override
	public void afterTaskDelegatedEvent(TaskEvent event) {
		logs.add(new TaskLog(event.getTask().getId(), "TaskDelegated"));
	}

	public List<TaskLog> getLogs() {
		return logs;
	}
}
