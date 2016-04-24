package com.ibm.scas.analytics.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonSetter;

public class Gateway extends SoftLayerIdObject {
	
	public enum GatewayType {
		DEDICATED { @Override public String toString() { return "DEDICATED"; } },
		MANAGEMENT { @Override public String toString() { return "MANAGEMENT"; } },
		SHARED { @Override public String toString() { return "SHARED"; } },
	};
	
	public enum GatewayStatus {
		PENDING {@Override public String toString() { return "PENDING"; } },
		ACTIVE {@Override public String toString() { return "ACTIVE"; } },
		UPDATING {@Override public String toString() { return "UPDATING"; } },
	}

	private String name;
	private GatewayType type;
	
	private String username;						// write only
	private String password;						// write only
	
	private Account account;
	private SoftLayerAccount softLayerAccount;	
	private String location;
	
	private String privateIpAddress;  				// detail from SoftLayer
	private String publicIpAddress;  				// detail from SoftLayer
	private String primaryFrontendRouter;			// detail from SoftLayer
	private String primaryBackendRouter;			// detail from SoftLayer
	private Collection<GatewayMember> gatewayMembers;		// detail from SoftLayer
	private List<SoftLayerIdObject> associatedVlans;	// detail from SoftLayer
	private GatewayStatus status;						// detail from SoftLayer
	private int groupNumber;							// detail from SoftLayer
	

	private List<VPNTunnel> vpnTunnels;
	
	public String getPrimaryFrontendRouter() {
		return primaryFrontendRouter;
	}

	public void setPrimaryFrontendRouter(String primaryFrontendRouter) {
		this.primaryFrontendRouter = primaryFrontendRouter;
	}

	public String getPrimaryBackendRouter() {
		return primaryBackendRouter;
	}

	public void setPrimaryBackendRouter(String primaryBackendRouter) {
		this.primaryBackendRouter = primaryBackendRouter;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public GatewayType getType() {
		return type;
	}
	
	public void setType(GatewayType type) {
		this.type = type;
	}
	
	@JsonIgnore
	public String getUsername() {
		return username;
	}
	
	@JsonSetter
	public void setUsername(String username) {
		this.username = username;
	}
	
	@JsonIgnore
	public String getPassword() {
		return password;
	}
	
	@JsonSetter
	public void setPassword(String password) {
		this.password = password;
	}
	
	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public SoftLayerAccount getSoftLayerAccount() {
		return softLayerAccount;
	}

	public void setSoftLayerAccount(SoftLayerAccount softLayerAccount) {
		this.softLayerAccount = softLayerAccount;
	}

	public String getPrivateIpAddress() {
		return privateIpAddress;
	}

	public void setPrivateIpAddress(String privateIpAddress) {
		this.privateIpAddress = privateIpAddress;
	}

	public String getPublicIpAddress() {
		return publicIpAddress;
	}
	

	public void setPublicIpAddress(String publicIpAddress) {
		this.publicIpAddress = publicIpAddress;
	}
	

	public List<SoftLayerIdObject> getAssociatedVlans() {
		return associatedVlans;
	}
	

	public void setAssociatedVlans(List<SoftLayerIdObject> associatedVlans) {
		this.associatedVlans = associatedVlans;
	}


	public String getLocation() {
		return location;
	}


	public void setLocation(String location) {
		this.location = location;
	}


	public GatewayStatus getStatus() {
		return status;
	}

	public void setStatus(GatewayStatus status) {
		this.status = status;
	}

	public List<VPNTunnel> getVpnTunnels() {
		return vpnTunnels;
	}

	public void setVpnTunnels(List<VPNTunnel> vpnTunnels) {
		this.vpnTunnels = vpnTunnels;
	}

	public int getGroupNumber() {
		return groupNumber;
	}

	public void setGroupNumber(int groupNumber) {
		this.groupNumber = groupNumber;
	}

	public Collection<GatewayMember> getGatewayMembers() {
		return gatewayMembers;
	}

	public void setGatewayMembers(Collection<GatewayMember> gatewayMembers) {
		this.gatewayMembers = gatewayMembers;
	}
	
	@JsonIgnore
	// find the gateway with the max priority
	public GatewayMember getMasterGatewayMember() {
		if (gatewayMembers == null) {
			return null;
		}
		
		final List<GatewayMember> sortedMembers = new ArrayList<GatewayMember>(gatewayMembers);
		Collections.sort(sortedMembers, new Comparator<GatewayMember>() {
			@Override
			public int compare(GatewayMember object1, GatewayMember object2) {
				// descending order of priority
				return 0 - (object1.getPriority() - object2.getPriority());
			}
		});
		
		return sortedMembers.get(0);
	}
}
