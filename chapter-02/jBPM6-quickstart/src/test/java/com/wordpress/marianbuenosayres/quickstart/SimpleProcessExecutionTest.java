package com.wordpress.marianbuenosayres.quickstart;


import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.drools.core.RuleBase;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.reteoo.ReteooRuleBase;
import org.jbpm.process.instance.impl.Action;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.DroolsAction;
import org.jbpm.workflow.core.impl.ConnectionImpl;
import org.jbpm.workflow.core.node.ActionNode;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.StartNode;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.Message.Level;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessContext;
import org.kie.api.runtime.process.ProcessInstance;

/**
 * Simple test to start getting familiarized with jBPM6 APIs and concepts
 * 
 * @author marianbuenosayres
 *
 */
public class SimpleProcessExecutionTest {

	@Test
	public void testProgramaticSimpleProcess() {
		//We create the process using the internal objects of the engine
		RuleFlowProcess processDef = createProcessDefinition();
		Assert.assertNotNull(processDef);

		//We then create a session with the defined process
		KieSession ksession = createSessionForProcess(processDef);
		Assert.assertNotNull(ksession);
		Assert.assertNotNull(ksession.getKieBase().getProcess("myProgramaticProcess"));

		//Now, we use the runtime to actually start an instance of that process 
		ProcessInstance instance = ksession.startProcess("myProgramaticProcess");
		Assert.assertNotNull(instance);
		//Since the process was just a script, it executes without stopping till it's finished
		Assert.assertEquals(ProcessInstance.STATE_COMPLETED, instance.getState());
	}
	
	@Test
	public void testKieAPIConfigurations() throws Exception {
		//Get the KieServices instance for singletons fetching
		KieServices ks = KieServices.Factory.get();

		//Create a virtual file system to write Kie resources
		KieFileSystem kfs = ks.newKieFileSystem();
		
		//Write content in a maven project structure
		kfs.write("src/main/resources/my-process.bpmn2", getFile("myDesignedProcess.bpmn2"));
		
		//Set a maven release ID for the filesystem
		kfs.generateAndWritePomXML(ks.newReleaseId("com.wordpress.marianbuenosayres", "test", "1.0-SNAPSHOT"));
		
		//Use a KieBuilder instance to generate a KieModule
		KieBuilder kbuilder = ks.newKieBuilder(kfs);
		kbuilder.buildAll();
		//check that the Kie Module compilation had no errors
		if (kbuilder.getResults().hasMessages(Level.ERROR)) {
			System.out.println("Errors compiling Kie Module");
			System.out.println(kbuilder.getResults().getMessages());
			throw new IllegalStateException("Errors compiling Kie Module");
		}
		KieModule kmodule = kbuilder.getKieModule();
		
		//Adding the KieModule to the repository for it to be available from other sources
		ks.getRepository().addKieModule(kmodule);
		
		//Get a container for all the kie runtimes from kmodule
		KieContainer kcontainer = ks.newKieContainer(kmodule.getReleaseId());
		
		//Create a Kie Base from the Kie Container
		KieBase kbase = kcontainer.getKieBase();
		
		//Create a Kie Session from the KieBase
		KieSession ksession = kbase.newKieSession();
		Assert.assertNotNull(ksession);
		Assert.assertNotNull(ksession.getKieBase().getProcess("myDesignedProcess"));

		//Now, we use the runtime to actually start an instance of that process 
		ProcessInstance instance = ksession.startProcess("myDesignedProcess");
		Assert.assertNotNull(instance);
		//Since the process was just a script, it executes without stopping till it's finished
		Assert.assertEquals(ProcessInstance.STATE_COMPLETED, instance.getState());
	}
	
	@Test 
	public void testExternalInterations() {
		//TODO IMPLEMENT
	}
	
	/**
	 * Creates a session based on the process. It looks very cryptic at first but 
	 * it is only because this is not the usual method to add a dynamic process.
	 * 
	 * @param process
	 * @return
	 */
	private KieSession createSessionForProcess(RuleFlowProcess process) {
		
		KieBase kbase = KieServices.Factory.get().getKieClasspathContainer().getKieBase(); 
		InternalKnowledgeBase intKbase = (InternalKnowledgeBase) kbase;
		RuleBase rbase = intKbase.getRuleBase();
		ReteooRuleBase reteooRbase = (ReteooRuleBase) rbase;
		reteooRbase.addProcess(process);
		return kbase.newKieSession();
	}

	/**
	 * This is a helper method to simply read a file from the claspath.
	 * @param path The file to read
	 * @return the content of the file as a byte array
	 */
	private byte[] getFile(String path) throws Exception {
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/"+path)));
		while (reader.ready()) {
			sb.append(reader.readLine()).append('\n');
		}
		return sb.toString().getBytes();
	}

	/**
	 * Creates a process defininiton programmatically using the
	 * jBPM6 internal APIs
	 * @return a RuleFlowProcess instance (not to be confused with a RuleFlowProcessInstance instance)
	 */
	private RuleFlowProcess createProcessDefinition() {
		
		//Process Definition
    	RuleFlowProcess process = new RuleFlowProcess();
    	process.setId("myProgramaticProcess");
    	
    	//Start Task
    	StartNode startTask = new StartNode();
    	startTask.setId(1);
    	
    	//Script Task
    	ActionNode scriptTask = new ActionNode();
    	scriptTask.setId(2);
    	DroolsAction action = new DroolsAction();
    	action.setMetaData("Action", new Action() {
			@Override
			public void execute(ProcessContext context) throws Exception {
				System.out.println("Executing the Action!!");
			}
		});
		scriptTask.setAction(action);
		
		//End Task
    	EndNode endTask = new EndNode();
    	endTask.setId(3);

    	//Adding the connections to the nodes and the nodes to the process
    	new ConnectionImpl(startTask, "DROOLS_DEFAULT", scriptTask, "DROOLS_DEFAULT");
    	new ConnectionImpl(scriptTask, "DROOLS_DEFAULT", endTask, "DROOLS_DEFAULT");
    	process.addNode(startTask);
    	process.addNode(scriptTask);
    	process.addNode(endTask);
    	
    	return process;
	}
}
