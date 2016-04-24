package com.ibm.scas.analytics.beans;

import java.util.ArrayList;
import java.util.List;

public class Vlan extends SoftLayerIdObject {
	public enum VlanNetworkSpace {
		PRIVATE { @Override public String toString() { return "PRIVATE"; } },
		PUBLIC { @Override public String toString() { return "PUBLIC"; } }
	}
	
	private String name;			// from SoftLayer API
	private String primaryRouter;	// from SoftLayer API
	private String vlanNumber;		// from SoftLayer API
	private VlanNetworkSpace networkSpace; // from SoftLayer API
	private List<VlanTrunk> vlanTrunks = new ArrayList<Vlan.VlanTrunk>();	// from SoftLayer API
	
	private Cluster cluster;
	
	private SoftLayerIdObject gateway;
	private List<SoftLayerIdObject> subnets;
	
	private SoftLayerAccount softLayerAccount;	
	private String location;
	
    public Cluster getCluster() {
		return cluster;
	}
	
	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	public SoftLayerIdObject getGateway() {
		return gateway;
	}

	public void setGateway(SoftLayerIdObject gateway) {
		this.gateway = gateway;
	}
	
	public SoftLayerAccount getSoftLayerAccount() {
		return softLayerAccount;
	}

	public void setSoftLayerAccount(SoftLayerAccount softLayerAccount) {
		this.softLayerAccount = softLayerAccount;
	}

	public String getPrimaryRouter() {
		return primaryRouter;
	}

	public void setPrimaryRouter(String router) {
		this.primaryRouter = router;
	}

	public String getVlanNumber() {
		return vlanNumber;
	}

	public void setVlanNumber(String vlanNumber) {
		this.vlanNumber = vlanNumber;
	}

	public List<SoftLayerIdObject> getSubnets() {
		return subnets;
	}

	public void setSubnets(List<SoftLayerIdObject> subnets) {
		this.subnets = subnets;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public VlanNetworkSpace getNetworkSpace() {
		return networkSpace;
	}

	public void setNetworkSpace(VlanNetworkSpace networkSpace) {
		this.networkSpace = networkSpace;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
	public List<VlanTrunk> getVlanTrunks() {
		return vlanTrunks;
	}

	public void setVlanTrunks(List<VlanTrunk> vlanTrunks) {
		this.vlanTrunks = vlanTrunks;
	}

	public static class VlanTrunk {
		private final String hostname;
		private final String privateIPAddr;
		private final String portName;		
		
		public VlanTrunk(String hostname, String privateIPAddr, String portName) {
			super();
			this.hostname = hostname;
			this.privateIPAddr = privateIPAddr;
			this.portName = portName;
		}

		public String getHostname() {
			return hostname;
		}
		
		public String getPrivateIPAddr() {
			return privateIPAddr;
		}
		
		public String getPortName() {
			return portName;
		}
		
		
		@Override
		public String toString() {
			return String.format("%s: %s", hostname, portName);
		}
	}
}
