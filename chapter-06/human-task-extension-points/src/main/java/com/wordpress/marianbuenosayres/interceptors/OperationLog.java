package com.wordpress.marianbuenosayres.interceptors;

public class OperationLog {

	private final String userId;
	private final String operation;
	
	public OperationLog(String userId, String operation) {
		this.userId = userId;
		this.operation = operation;
	}
	
	public String getOperation() {
		return operation;
	}
	
	public String getUserId() {
		return userId;
	}

	@Override
	public String toString() {
		return "{OperationLog:"+userId+";"+operation+"}";
	}
}
