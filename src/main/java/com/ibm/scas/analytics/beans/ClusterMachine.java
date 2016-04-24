package com.ibm.scas.analytics.beans;


public class ClusterMachine {
	private String id;
	private String name;
	private String hostname;
	private String tierName;
	private String status;
	private int percentUsedCpu;
	private int percentUsedMemory;
	private String ipAddress;
	private String physicalHostIP;
	
	public ClusterMachine() {
		
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getTierName() {
		return tierName;
	}

	public void setTierName(String tierName) {
		this.tierName = tierName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPercentUsedCpu() {
		return percentUsedCpu;
	}

	public void setPercentUsedCpu(int percentUsedCpu) {
		this.percentUsedCpu = percentUsedCpu;
	}

	public int getPercentUsedMemory() {
		return percentUsedMemory;
	}

	public void setPercentUsedMemory(int percentUsedMemory) {
		this.percentUsedMemory = percentUsedMemory;
	}

	public String getPhysicalHostIP() {
		return physicalHostIP;
	}

	public void setPhysicalHostIP(String physicalHostIP) {
		this.physicalHostIP = physicalHostIP;
	}
	
}
