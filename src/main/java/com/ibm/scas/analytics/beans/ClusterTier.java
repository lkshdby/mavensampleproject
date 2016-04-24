package com.ibm.scas.analytics.beans;

public class ClusterTier {
	private String name;
	private String description;
	private ClusterMachineGroup machineGroup;

	public ClusterTier() {
	}

	public String getName() {
		return name;
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

	public ClusterMachineGroup getMachineGroup() {
		return machineGroup;
	}

	public void setMachineGroup(ClusterMachineGroup machineGroup) {
		this.machineGroup = machineGroup;
	}
	
}
