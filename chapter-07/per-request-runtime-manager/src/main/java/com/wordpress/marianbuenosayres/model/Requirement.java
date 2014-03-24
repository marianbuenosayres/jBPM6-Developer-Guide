package com.wordpress.marianbuenosayres.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.kie.api.definition.type.PropertyReactive;

@PropertyReactive
public class Requirement implements Serializable {

	private static final long serialVersionUID = 3271877931717694639L;
	private static final AtomicLong atomic = new AtomicLong(0);
	
	private final long id = atomic.incrementAndGet();
	private int priority = 10;
	private String name;
	private String description;
	private String developerId = null;
	private String testerId = null;
	private boolean compiled = false;
	private boolean deployed = false;
	private boolean tested = false;
	private List<String> bugs = new ArrayList<String>();
	private List<String> solvedBugs = new ArrayList<String>();
	
	private long processInstanceId = -1;

	public Requirement() {
	}
	
	public Requirement(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	public String getName() {
		return name;
	}
	
	public long getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean compiled() {
		return compiled;
	}

	public boolean deployed() {
		return deployed;
	}
	
	public void setDeployed(boolean deployed) {
		this.deployed = deployed;
	}
	
	public void setCompiled(boolean compiled) {
		this.compiled = compiled;
	}

	public List<String> getBugs() {
		return bugs;
	}

	public void setBugs(List<String> bugs) {
		this.bugs = bugs;
	}
	
	public List<String> getSolvedBugs() {
		return solvedBugs;
	}
	
	public void setSolvedBugs(List<String> solvedBugs) {
		this.solvedBugs = solvedBugs;
	}

	public long getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(long processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public void setTested(boolean tested) {
		this.tested = tested;
	}
	
	public boolean finished() {
		return this.tested && this.bugs.isEmpty();
	}
	
	public String getDeveloperId() {
		return developerId;
	}
	
	public void setDeveloperId(String developerId) {
		this.developerId = developerId;
	}
	
	public String getTesterId() {
		return testerId;
	}
	
	public void setTesterId(String testerId) {
		this.testerId = testerId;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getPriority() {
		return priority;
	}
	
	@Override
	public String toString() {
		String br = System.getProperty("line.separator", "\n");
		StringBuilder sb = new StringBuilder("*************").append(br)
				.append("Requirement number ")
				.append(id).append(": ").append(this.name).append(br)
				.append("Description: ").append(this.description).append(br)
				.append("Priority: ").append(this.priority).append(br)
				.append("Developer: ").append(this.developerId).append(br)
				.append("Tester: ").append(this.testerId).append(br)
				.append("Compiled: ").append(this.compiled).append(br)
				.append("Deployed: ").append(this.deployed).append(br)
				.append("Tested: ").append(this.tested).append(br);
		if (this.bugs != null && !this.bugs.isEmpty()) {
			sb.append("Known bugs:").append(br);
			for (String bug : this.bugs) {
				sb.append(bug).append(br);
			}
		}
		if (this.solvedBugs != null && !this.solvedBugs.isEmpty()) {
			sb.append("Solved bugs:").append(br);
			for (String bug : this.solvedBugs) {
				sb.append(bug).append(br);
			}
		}
		sb.append("*************").append(br);
		return sb.toString();
	}

	public void addBug(String bug) {
		this.bugs.add(bug);
	}
	
	public void resolveBug(String bug, String solution) {
		if (this.bugs.remove(bug)) {
			this.solvedBugs.add(bug + ": " + solution);
		}
	}
}
