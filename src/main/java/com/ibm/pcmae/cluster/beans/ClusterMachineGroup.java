package com.ibm.pcmae.cluster.beans;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class ClusterMachineGroup extends ClusterTierLayer {
	private String template;
	private int numberOfMachines;
	private int cpu;
	private int memory;
	private int storage;
	
	public ClusterMachineGroup() {
		
	}
	
	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public int getNumberOfMachines() {
		return numberOfMachines;
	}

	public void setNumberOfMachines(int numberOfMachines) {
		this.numberOfMachines = numberOfMachines;
	}

	public int getCpu() {
		return cpu;
	}

	public void setCpu(int cpu) {
		this.cpu = cpu;
	}

	public int getMemory() {
		return memory;
	}

	public void setMemory(int memory) {
		this.memory = memory;
	}

	public int getStorage() {
		return storage;
	}

	public void setStorage(int storage) {
		this.storage = storage;
	}

}
