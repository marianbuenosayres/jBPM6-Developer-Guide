package com.wordpress.marianbuenosayres.quickstart;


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
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessContext;
import org.kie.api.runtime.process.ProcessInstance;

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

