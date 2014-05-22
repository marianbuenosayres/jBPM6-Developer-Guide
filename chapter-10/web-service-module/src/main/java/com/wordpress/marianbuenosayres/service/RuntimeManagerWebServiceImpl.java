package com.wordpress.marianbuenosayres.service;

import java.util.Map;

import javax.jws.WebService;

import org.jbpm.runtime.manager.impl.PerProcessInstanceRuntimeManager;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceResponse;

@WebService(
		targetNamespace = "com.wordpress.marianbuenosayres.service",
		name = "RuntimeManagerWebService",
		portName = "WebServicePort",
		endpointInterface="com.wordpress.marianbuenosayres.service.RuntimeManagerWebService")
public class RuntimeManagerWebServiceImpl implements RuntimeManagerWebService {

	@Override
	public JaxbProcessInstanceResponse startProcess(String releaseId, String processId, Map<String, Object> parameters) {
		RuntimeEngine engine = getRuntimeEngine(releaseId);
		ProcessInstance processInstance = engine.getKieSession().startProcess(processId, parameters);
		return new JaxbProcessInstanceResponse(processInstance);
	}

	@Override
	public void signalEventPerInstance(String releaseId, Long processInstanceId, String signalRef) {
		this.signalEventPerInstanceWithData(releaseId, processInstanceId, signalRef, null);
	}
	
	@Override
	public void signalEventAll(String releaseId, String signalRef) {
		this.signalEventPerInstanceWithData(releaseId, null, signalRef, null);
	}
	
	@Override
	public void signalEventWithData(String releaseId, String signalRef, Object parameter) {
		this.signalEventPerInstanceWithData(releaseId, null, signalRef, parameter);
	}
	
	@Override
	public void signalEventPerInstanceWithData(String releaseId, Long processInstanceId, String signalRef, Object parameter) {
		RuntimeEngine engine = getRuntimeEngine(releaseId, processInstanceId);
		if (processInstanceId == null) {
			engine.getKieSession().signalEvent(signalRef, parameter);
		} else {
			engine.getKieSession().signalEvent(signalRef, parameter, processInstanceId);
		}
	}

	protected RuntimeEngine getRuntimeEngine(String releaseId) {
		return getRuntimeEngine(releaseId, null);
	}
	
	protected RuntimeEngine getRuntimeEngine(String releaseId, Long processInstanceId) {
		RuntimeManager manager = RuntimeManagerRegistry.get().getManager(releaseId);
		Context<?> context = EmptyContext.get();
		if (processInstanceId != null && manager instanceof PerProcessInstanceRuntimeManager) {
			context = ProcessInstanceIdContext.get(processInstanceId);
		}
		RuntimeEngine engine = manager.getRuntimeEngine(context);
		return engine;
	}
	
	
}
