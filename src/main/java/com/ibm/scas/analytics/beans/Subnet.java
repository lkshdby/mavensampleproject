package com.ibm.scas.analytics.beans;

public class Subnet extends SoftLayerIdObject {
	private SoftLayerIdObject vlan;
	private SoftLayerAccount softLayerAccount;	
	
	private String type;
	
	private String networkAddr;
	private int cidr;
	private String broadcastAddr;
	private String gatewayAddr;
	
	public SoftLayerIdObject getVlan() {
		return vlan;
	}

	public void setVlan(SoftLayerIdObject vlan) {
		this.vlan = vlan;
	}
	
	public String getNetworkAddr() {
		return networkAddr;
	}
	
	public void setNetworkAddr(String networkAddr) {
		this.networkAddr = networkAddr;
	}
	
	public String getBroadcastAddr() {
		return broadcastAddr;
	}
	
	public void setBroadcastAddr(String broadcastAddr) {
		this.broadcastAddr = broadcastAddr;
	}
	
	public String getGatewayAddr() {
		return gatewayAddr;
	}
	
	public void setGatewayAddr(String gatewayAddr) {
		this.gatewayAddr = gatewayAddr;
	}
	
	public int getCidr() {
		return cidr;
	}
	
	public void setCidr(int cidr) {
		this.cidr = cidr;
	}

	public SoftLayerAccount getSoftLayerAccount() {
		return softLayerAccount;
	}

	public void setSoftLayerAccount(SoftLayerAccount softLayerAccount) {
		this.softLayerAccount = softLayerAccount;
	}		
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
