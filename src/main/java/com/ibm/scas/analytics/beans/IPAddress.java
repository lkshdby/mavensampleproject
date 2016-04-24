package com.ibm.scas.analytics.beans;

import com.ibm.scas.analytics.beans.Cluster;

public class IPAddress {
	private String id;
	private SoftLayerIdObject subnet;
	
	private String ipAddress; // String representation of IP address
	private String hostname;
	
	private Cluster cluster;
	private String tierName;
	
	private boolean isReservable;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public SoftLayerIdObject getSubnet() {
		return subnet;
	}
	

	public void setSubnet(SoftLayerIdObject subnet) {
		this.subnet = subnet;
	}
	

	public Cluster getCluster() {
		return cluster;
	}
	

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	public boolean isReservable() {
		return isReservable;
	}

	public void setReservable(boolean isReservable) {
		this.isReservable = isReservable;
	}
	
}
