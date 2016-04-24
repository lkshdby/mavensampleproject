package com.ibm.pcmae.cluster.beans;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class Quotas {
	private Quota machine;
	private Quota cpu;
	private Quota memory;
	private Quota storage;
	
	public Quotas() {
		
	}
	public Quota getMachine() {
		return machine;
	}
	public void setMachine(Quota machine) {
		this.machine = machine;
	}
	public Quota getCpu() {
		return cpu;
	}
	public void setCpu(Quota cpu) {
		this.cpu = cpu;
	}
	public Quota getMemory() {
		return memory;
	}
	public void setMemory(Quota memory) {
		this.memory = memory;
	}
	public Quota getStorage() {
		return storage;
	}
	public void setStorage(Quota storage) {
		this.storage = storage;
	}
}
