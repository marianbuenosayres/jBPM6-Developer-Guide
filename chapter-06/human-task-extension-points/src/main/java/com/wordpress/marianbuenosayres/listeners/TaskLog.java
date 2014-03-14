package com.wordpress.marianbuenosayres.listeners;

public class TaskLog {

	private final long taskId;
	private final String type;
	
	public TaskLog(long taskId, String type) {
		this.taskId = taskId;
		this.type = type;
	}

	public long getTaskId() {
		return taskId;
	}
	
	public String getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return "{"+type+":"+taskId+"}";
	}
}
