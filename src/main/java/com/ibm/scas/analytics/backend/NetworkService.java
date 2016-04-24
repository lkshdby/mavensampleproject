package com.ibm.scas.analytics.backend;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.scas.analytics.beans.Gateway;
import com.ibm.scas.analytics.beans.Gateway.GatewayType;
import com.ibm.scas.analytics.beans.IPAddress;
import com.ibm.scas.analytics.beans.Subnet;
import com.ibm.scas.analytics.beans.Vlan;
import com.ibm.scas.analytics.utils.CPEException;

public interface NetworkService {

	/**
	 * Method to add new gateway
	 * @param gatewayReq
	 * @return
	 * @throws CPEException
	 */
	public abstract String addGateway(Gateway gatewayReq) throws CPEException;

	/**
	 * Method to get list of all gateways
	 * @return
	 * @throws CPEException
	 */
	public abstract List<Gateway> getAllGateways() throws CPEException;
	
	/**
	 * Method to get list of gateways of certain type
	 * @return
	 * @throws CPEException
	 */
	public abstract List<Gateway> getGateways(GatewayType type) throws CPEException;

	/**
	 * Method to get gateway information by ID
	 * @param gatewayId
	 * @return
	 * @throws CPEException
	 */
	public abstract Collection<Gateway> getGateways(Collection<String> gatewayIds) throws CPEException;
	
	/**
	 * Method to get gateway information by ID
	 * @param gatewayId
	 * @return
	 * @throws CPEException
	 */
	public abstract List<Gateway> getAccountGateways(String accountId) throws CPEException;

	/**
	 * Method to get gateway information by ID
	 * @param gatewayId
	 * @return
	 * @throws CPEException
	 */
	public abstract Gateway getGatewayById(String gatewayId) throws CPEException;
	
	/**
	 * Method to get gateway's SSL certificate in the database
	 * @param gatewayId
	 * @return
	 * @throws CPEException
	 */
	public abstract Map<String, String> getGatewaySSLCertFromDB(String gatewayId) throws CPEException;
	
	/**
	 * Method to update gateway's SSL certificate in the database
	 * @param gatewayId
	 * @return
	 * @throws CPEException
	 */
	public abstract void updateGatewaySSLCert(String gatewayId) throws CPEException;

	/**
	 * Method to update gateway's user/password
	 * @param gatewayId
	 * @return
	 * @throws CPEException
	 */
	public abstract void updateGatewayUserPassword(String gatewayId, Gateway gatewayReq) throws CPEException;

	/**
	 * Method to test gateway's user/password
	 * @param gatewayId
	 * @return
	 * @throws CPEException
	 */
	public abstract void testGatewayUserPassword(String gatewayId) throws CPEException;

	/**
	 * Method to update account information for a gateway
	 * @param gatewayId
	 * @param gatewayReq
	 * @return
	 * @throws CPEException
	 */
	public abstract void updateGatewayAccount(String gatewayId, String accountId) throws CPEException;

	/**
	 * Method to delete gateway	
	 * @param gatewayId
	 * @throws CPEException
	 */
	public abstract void deleteGateway(String gatewayId) throws CPEException;
	
	/**
	 * Unassign a gateway for the subscriber.  
	 * @param subscriber to assign the VLAN to
	 * @param gatewayId gateway to place the VLAN behind
	 * @return
	 * @throws CPEException
	 */
	public abstract void unassignGateway(String gatewayId) throws CPEException;

	/**
	 * Get all the vlan details
	 * @return
	 * @throws CPEException
	 */
	public abstract List<Vlan> getAllVlans() throws CPEException;

	/**
	 * Add a VLAN to CPE management.
	 * @param vlanReq
	 * @return
	 * @throws CPEException
	 */
	public abstract String addVlan(Vlan vlanReq) throws CPEException;

	/**
	 * Get a VLAN by ID
	 * @param vlanId
	 * @return
	 * @throws CPEException
	 */
	public abstract Vlan getVlanById(String vlanId) throws CPEException;
	
	/**
	 * Get a VLAN by ID
	 * @param vlanId
	 * @return
	 * @throws CPEException
	 */
	public abstract Collection<Vlan> getVlans(Collection<String> vlanIds) throws CPEException;

	/**
	 * Get VLANs owned by subscriber
	 * @param vlanId
	 * @return
	 * @throws CPEException
	 */
	public abstract Collection<Vlan> getSubscriberVlans(String subscriberId) throws CPEException;

	/**
	 * Delete a VLAN
	 * 
	 * @param vlanId
	 * @throws CPEException
	 */
	public abstract void deleteVlan(String vlanId) throws CPEException;

	/**
	 * Method to get subnet details 
	 * @return
	 * @throws CPEException
	 */
	public abstract List<Subnet> getAllSubnets() throws CPEException;

	/**
	 * Method to get query subnet by id
	 * @param vlanId
	 * @return
	 * @throws CPEException
	 */
	public abstract Subnet getSubnetById(String subnetId) throws CPEException;
	
	/**
	 * Method to get query subnet by id
	 * @param vlanId
	 * @return
	 * @throws CPEException
	 */
	public abstract List<Subnet> getSubnets(Collection<String> subnetIds) throws CPEException;


	/**
	 * Delete subnets
	 * @param vlanID
	 * @throws CPEException
	 */
	public abstract void deleteSubnet(String vlanID) throws CPEException;

	//try passing vlanID
	/**
	 * Delete ipaddresses related by subnetID
	 * @param subnetId
	 * @throws CPEException
	 */
	public abstract void deleteIpaddress(String subnetId) throws CPEException;

	/**
	 * Reserve a vlan for the subscriber
	 * @param subscriber to assign the VLAN to
	 * @param gatewayId gateway to place the VLAN behind
	 * @return
	 * @throws CPEException
	 */
	public abstract String reserveVlan(String clusterId, String gatewayId) throws CPEException;
	
	/**
	 * Unassign a vlan for the cluster.
	 * and untrunks it from any hypervisors it's on
	 * 
	 * @param subscriber to assign the VLAN to
	 * @param gatewayId gateway to place the VLAN behind
	 * @param force if true, does not check if cluster associated on vlan
	 * @return
	 * @throws CPEException if cluster is associated with gateway
	 */
	public abstract void unassignVlan(String vlanId, boolean force) throws CPEException;
	
	/**
	 * Return all subnets in use by cluster
	 * @param clusterId
	 * @return
	 * @throws CPEException
	 */
	public abstract List<Subnet> getClusterSubnets(String clusterId) throws CPEException;
	
	/**
	 * Get existing vlans used by cluster
	 * @param subscriber
	 * @return
	 * @throws CPEException
	 */
	public abstract List<Vlan> getClusterVlans(String clusterId) throws CPEException;
	
	/**
	 * Get existing vlans used by cluster for tier
	 * @param subscriber
	 * @return
	 * @throws CPEException
	 */
	public abstract List<Vlan> getClusterVlans(String clusterId, String tier) throws CPEException;
	
	/**
	 * Associate a vlan to a gateway. If isBypass is false then the vlan is routed through the gateway.
	 * @param vlanId
	 * @param gatewayId
	 * @param isBypass
	 * @throws CPEException
	 */
	public abstract void attachVlanToGateway(Collection<String> vlanIds, String gatewayId, boolean isBypass) throws CPEException; 
	
	/**
	 * Detach a VLAN from any gateways it is associated with. 
	 * @param vlanId
	 * @throws CPEException
	 */
	public abstract void detachVlanFromGateway(String vlanId, boolean force) throws CPEException;
	
	/**
	 * Reserve some IP addresses for the cluster
	 * @param vlanId vlan ID to reserve the IP addresses on.  CPE selects the correct number of IPs based on subnets on this VLAN
	 * @param totalIPsReserved total number of IPs that should be reserved on this VLAN
	 * @param clusterId id of the cluster to reserve the IP addresses for
	 * @param clusterTierName cluster tier that the ip address is for
	 * @return
	 * @throws CPEException
	 */
	public abstract List<String> reserveIPAddressesForClusterTier(String vlanId, int totalIPsReserved, String clusterId,
			String clusterTierName) throws CPEException;
	

	/**
	 * Method to query ips by cluster
	 * @param clusterId
	 * @param clusterTier optional cluster tier, if null, returns all IPs assign to cluster
	 * @return
	 * @throws CPEException
	 */
	public abstract List<IPAddress> getIPAddressByCluster(String clusterId, String clusterTier) throws CPEException;
	
	/**
	 * Method to query ips by subnet
	 * @param subnetId
	 * @return
	 * @throws CPEException
	 */
	public abstract List<IPAddress> getIPAddressBySubnet(String subnetId) throws CPEException;

	/**
	 * Method to query ips by id
	 * @param clusterId
	 * @param clusterTier optional cluster tier, if null, returns all IPs assign to cluster
	 * @return
	 * @throws CPEException
	 */
	public abstract IPAddress getIPAddress(String ipAddressId) throws CPEException;
	
	/**
	 * Method to query ips by vlan.  note that addresses are unique in each vlan
	 * @param ipAddress
	 * @param vlanId
	 * @return
	 * @throws CPEException
	 */
	public abstract IPAddress getIPAddress(String ipAddress, String vlanId) throws CPEException;
	
	/**
	 * Method to unreserve a list of IP address IDs
	 * @param ipAddressId
	 * @throws CPEException
	 */
	public abstract void unreserveIPAddress(List<String> ipAddressIds) throws CPEException;
	
	/**
	 * Method to unreserve a list of IP addresses by VLAN
	 * @param ipAddressId
	 * @throws CPEException
	 */
	public abstract void unreserveIPAddressByAddress(List<String> ipAddress, String vlanId) throws CPEException;

	/**
	 * Method to unreserve all IPs by cluster
	 * @param ipAddressId
	 * @throws CPEException
	 */
	public abstract void unreserveIPAddressByCluster(String clusterId) throws CPEException;

	/**
	 * Method to trunk VLANs to hardware
	 * @param ipAddress
	 * @param vlanIDs
	 * @throws CPEException
	 */
	public abstract void addNetworkVlanTrunks(String ipAddress, Set<String> vlanIDs) throws CPEException;
	
	/**
	 * Method to untrunk VLANs from all hardware
	 * @param vlanID
	 * @throws CPEException
	 */
	public void removeNetworkVlanTrunks(String vlanID, boolean force) throws CPEException;
	
	/**
	 * Method to fetch public vlan
	 * @param gatewayId
	 * @return 
	 * @throws CPEException
	 */
	public abstract String fetchPublicVlan(String gatewayId) throws CPEException;

	/**
	 * @param gatewayId
	 * @return
	 * @throws CPEException
	 */
	public abstract List<Subnet> getSubnets(String gatewayId) throws CPEException;
	
	/**
	 * @param gatewayId
	 * @throws CPEException
	 */
	public abstract void osReload(String gatewayId) throws CPEException;
}