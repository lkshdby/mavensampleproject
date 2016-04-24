package com.ibm.pcmae.cluster.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ClusterTier {

	private String id;
	private String name;
	private String description;
	private List<ClusterMachineGroup> machines;
	private List<ClusterScriptLayer> postscripts;

	public ClusterTier() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ClusterMachineGroup> getMachines() {
		return machines;
	}

	public void setMachines(List<ClusterMachineGroup> machines) {
		this.machines = machines;
	}

	public List<ClusterScriptLayer> getPostscripts() {
		return postscripts;
	}

	public void setPostscripts(List<ClusterScriptLayer> postscripts) {
		this.postscripts = postscripts;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
