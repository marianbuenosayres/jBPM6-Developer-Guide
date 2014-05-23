package com.wordpress.marianbuenosayres.service;

import java.util.HashMap;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceResponse;

@WebService
public interface RuntimeManagerWebService {

	@WebMethod
	JaxbProcessInstanceResponse startProcess(
			@WebParam(name = "releaseId") String releaseId, 
			@WebParam(name = "processId") String processId, 
			@WebParam(name = "parameters") HashMap<String, Object> parameters);

	@WebMethod
	void signalEventAll(
			@WebParam(name = "releaseId") String releaseId,
			@WebParam(name = "signalRef") String signalRef);

	@WebMethod
	void signalEventPerInstance(
			@WebParam(name = "releaseId") String releaseId,
			@WebParam(name = "processInstanceId") Long processInstanceId,
			@WebParam(name = "signalRef") String signalRef);

	@WebMethod
	void signalEventWithData(
			@WebParam(name = "releaseId") String releaseId,
			@WebParam(name = "signalRef") String signalRef,
			@WebParam(name = "parameter") Object parameter);

	@WebMethod
	void signalEventPerInstanceWithData(
			@WebParam(name = "releaseId") String releaseId,
			@WebParam(name = "processInstanceId") Long processInstanceId,
			@WebParam(name = "signalRef") String signalRef,
			@WebParam(name = "parameter") Object parameter);
}
