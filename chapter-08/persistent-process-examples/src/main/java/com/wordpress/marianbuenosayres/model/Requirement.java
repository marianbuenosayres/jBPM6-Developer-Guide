package com.wordpress.marianbuenosayres.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;

import org.kie.api.definition.type.PropertyReactive;

@Entity
@PropertyReactive
public class Requirement implements Serializable {

	private static final long serialVersionUID = 3271877931717694639L;
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	private int priority = 10;
	private String name;
	private String description;
	private String developerId = null;
	private String testerId = null;
	private boolean compiled = false;
	private boolean deployed = false;
	private boolean tested = false;
    @ElementCollection
    @CollectionTable(name="RequirementBugs", joinColumns=@JoinColumn(name="reqId"))
    @Column(name="element")
	private List<String> bugs = new ArrayList<String>();
    @ElementCollection
    @CollectionTable(name="RequirementSolvedBugs", joinColumns=@JoinColumn(name="reqId"))
    @Column(name="element")
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
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bugs == null) ? 0 : bugs.hashCode());
		result = prime * result + (compiled ? 1231 : 1237);
		result = prime * result + (deployed ? 1231 : 1237);
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result
				+ ((developerId == null) ? 0 : developerId.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + priority;
		result = prime * result
				+ (int) (processInstanceId ^ (processInstanceId >>> 32));
		result = prime * result
				+ ((solvedBugs == null) ? 0 : solvedBugs.hashCode());
		result = prime * result + (tested ? 1231 : 1237);
		result = prime * result
				+ ((testerId == null) ? 0 : testerId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Requirement other = (Requirement) obj;
		if (bugs == null) {
			if (other.bugs != null)
				return false;
		} else if (!bugs.equals(other.bugs))
			return false;
		if (compiled != other.compiled)
			return false;
		if (deployed != other.deployed)
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (developerId == null) {
			if (other.developerId != null)
				return false;
		} else if (!developerId.equals(other.developerId))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (priority != other.priority)
			return false;
		if (processInstanceId != other.processInstanceId)
			return false;
		if (solvedBugs == null) {
			if (other.solvedBugs != null)
				return false;
		} else if (!solvedBugs.equals(other.solvedBugs))
			return false;
		if (tested != other.tested)
			return false;
		if (testerId == null) {
			if (other.testerId != null)
				return false;
		} else if (!testerId.equals(other.testerId))
			return false;
		return true;
	}
}
