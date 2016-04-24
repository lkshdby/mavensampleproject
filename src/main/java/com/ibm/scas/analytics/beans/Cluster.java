package com.ibm.scas.analytics.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.scas.analytics.provider.NameValuePair;

public class Cluster extends BaseBean {
	
	public enum ClusterStep {
		NONE,
		INIT,
		// cluster order steps
		ORDER_PENDING,
		CONFIG_NODES,
		// provision steps
		WAITING_FOR_GATEWAY,
		RESERVE_NETWORK,
		CONFIGURE_MGMT_GATEWAY,
		TRUNK_CUST_GATEWAY,
		CONFIGURE_CUST_GATEWAY,
		TRUNK_HYPERVISORS,
		PROVISION_NODES,
		SUBMIT_BILLING,
		// unprovision steps
		CANCEL_CLUSTER,
		UNTRUNK_HYPERVISORS,
		UNCONFIGURE_CUST_GATEWAY,
		UNTRUNK_CUST_GATEWAY,
		UNCONFIGURE_MGMT_GATEWAY,
		UNRESERVE_NETWORK,
		REMOVE_RECORDS
	}
	
	public static final int INUSE = 1;
	public static final int DELETING = 2;
	public static final int DELETED = 3;
	
	private Subscriber owner;
	
	private String name;
	private String description;
	private String clusterId;
	private int size;
	private int state;
	private long launchTime;
	private long terminateTime;
	private ClusterStep currentStep;
	
	private List<NameValuePair> details;			// shown on the dashboard
	private Map<String, String> properties;			// for internal usage
	private Map<String, String> clusterParams;		// request parameters
	private VPNTunnel vpnTunnel;					// associated vpn tunnel
	
	private List<ClusterTier> clusterTiers;
	private List<ClusterMachine> clusterMachines;
	private List<SoftLayerOrder> orders;
	
	public Cluster() {
		details = new ArrayList<NameValuePair>();
		clusterMachines = new ArrayList<ClusterMachine>();
		properties = new HashMap<String, String>();
		clusterTiers = new ArrayList<ClusterTier>();
	}

	public Subscriber getOwner() {
		return owner;
	}
	public void setOwner(Subscriber owner) {
		this.owner = owner;
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
	public String getClusterId() {
		return clusterId;
	}
	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}
	
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public long getLaunchTime() {
		return launchTime;
	}
	public void setLaunchTime(long launchTime) {
		this.launchTime = launchTime;
	}
	public long getTerminateTime() {
		return terminateTime;
	}
	public void setTerminateTime(long terminateTime) {
		this.terminateTime = terminateTime;
	}

	public List<NameValuePair> getDetails() {
		return details;
	}
	
	public void setDetails(List<NameValuePair> details) {
		this.details = details;
	}
	public ClusterStep getCurrentStep() {
		return currentStep;
	}
	public void setCurrentStep(ClusterStep currentStep) {
		this.currentStep = currentStep;
	}

	public List<ClusterMachine> getClusterMachines() {
		return clusterMachines;
	}
	public void setClusterMachines(List<ClusterMachine> clusterMachines) {
		this.clusterMachines = clusterMachines;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public List<ClusterTier> getClusterTiers() {
		return clusterTiers;
	}

	public void setClusterTiers(List<ClusterTier> clusterTiers) {
		this.clusterTiers = clusterTiers;
	}

	public Map<String, String> getClusterParams() {
		return clusterParams;
	}

	public void setClusterParams(Map<String, String> clusterParams) {
		this.clusterParams = clusterParams;
	}

	public VPNTunnel getVpnTunnel() {
		return vpnTunnel;
	}

	public void setVpnTunnel(VPNTunnel vpnTunnel) {
		this.vpnTunnel = vpnTunnel;
	}

	public List<SoftLayerOrder> getOrders() {
		return orders;
	}

	public void setOrders(List<SoftLayerOrder> orders) {
		this.orders = orders;
	}
}
