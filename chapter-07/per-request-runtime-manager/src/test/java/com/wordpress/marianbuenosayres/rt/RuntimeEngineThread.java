package com.wordpress.marianbuenosayres.rt;

import java.util.HashMap;

import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.runtime.manager.context.EmptyContext;

public class RuntimeEngineThread extends Thread {
	
	private RuntimeEngine engine = null;
	private int sessionId;
	private long processInstanceId = -1;
	private RuntimeManager manager;

	public RuntimeEngineThread(RuntimeManager manager) {
		this.manager = manager;
	}
	
	@Override
	public void run() {
		this.engine = manager.getRuntimeEngine(EmptyContext.get());
		this.sessionId = this.engine.getKieSession().getId();
		ProcessInstance pi = this.engine.getKieSession().startProcess("sprintManagement", new HashMap<String, Object>());
		this.processInstanceId = pi.getId();
	}
	
	public synchronized int getSessionId() {
		return sessionId;
	}
	
	public synchronized long getProcessInstanceId() {
		return processInstanceId;
	}

	public void dispose() {
		manager.disposeRuntimeEngine(engine);
		try {
			join();
		} catch (InterruptedException e) { /* Ignore */ }
	}
}
