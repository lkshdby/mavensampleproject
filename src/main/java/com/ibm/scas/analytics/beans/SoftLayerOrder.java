package com.ibm.scas.analytics.beans;

import java.util.List;


public class SoftLayerOrder {
	public static class SoftLayerHardware {
		private String hardwareId;
		private String billingItemId;
		private String name;
		private String status;
		
		public SoftLayerHardware() {}

		public String getHardwareId() {
			return hardwareId;
		}

		public void setHardwareId(String hardwareId) {
			this.hardwareId = hardwareId;
		}
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}

		public String getBillingItemId() {
			return billingItemId;
		}

		public void setBillingItemId(String billingItemId) {
			this.billingItemId = billingItemId;
		}
		
		@Override
		public String toString() {
			return String.format("hardwareId:%s, billingItemId:%s, name:%s, status:%s", hardwareId, billingItemId, name, status);
		}
	}
	
	private String id;
	private String softLayerId;
	private Cluster cluster; // read only
	private String tierName;
	private SoftLayerAccount softLayerAccount;
	private List<SoftLayerHardware> hardwares;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Cluster getCluster() {
		return cluster;
	}
	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}
	public String getTierName() {
		return tierName;
	}
	public void setTierName(String tierName) {
		this.tierName = tierName;
	}
	public SoftLayerAccount getSoftLayerAccount() {
		return softLayerAccount;
	}
	public void setSoftLayerAccount(SoftLayerAccount softLayerAccount) {
		this.softLayerAccount = softLayerAccount;
	}
	public String getSoftLayerId() {
		return softLayerId;
	}
	public void setSoftLayerId(String softLayerId) {
		this.softLayerId = softLayerId;
	}
	public List<SoftLayerHardware> getHardwares() {
		return hardwares;
	}
	public void setHardwares(List<SoftLayerHardware> hardwares) {
		this.hardwares = hardwares;
	}
}
