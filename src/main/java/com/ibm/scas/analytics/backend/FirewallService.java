package com.ibm.scas.analytics.backend;

import java.util.List;
import java.util.Set;

import com.ibm.scas.analytics.beans.Gateway;
import com.ibm.scas.analytics.beans.VPNTunnel;
import com.ibm.scas.analytics.beans.Vlan;
import com.ibm.scas.analytics.utils.CPEException;

public interface FirewallService {
	/**
	 * Test the gateway's username/password using the stored SSL Certificates.
	 * @param privateIp
	 * @param username
	 * @param password
	 * @throws CPEException
	 */
	public abstract void testGatewayCredentials(Gateway gateway) throws CPEException;
	
	/**
	 * get the SSL certificate from gateway member
	 * @param memberPrivateIp
	 * @return
	 * @throws CPEException
	 */
	public abstract String getSSLCertificate(String memberPrivateIp) throws CPEException;

	/**
	 * Add the subnets to the management firewall's allow list so CPE can manage Vyatta on those subnets
	 * @param subnets
	 * @throws CPEException
	 */
	public abstract void allowManagementSubnets(Gateway mgmtGateway, Set<String> subnets) throws CPEException;
	
	/**
	 * Add the subnets to the management firewall's allow list so CPE can manage customer clusters on those subnets
	 * @param subnets
	 * @throws CPEException
	 */
	public abstract void allowCustomerSubnets(Gateway mgmtGateway, Set<String> subnets) throws CPEException;
	
	/**
	 * Remove the subnets from the management firewall's allow list (no more Vyatta on these subnets)
	 * @param subnets
	 * @throws CPEException
	 */
	public abstract void removeMgmtSubnets(Gateway mgmtGateway, Set<String> subnetList) throws CPEException;
	
	/**
	 * Remove the subnets from the management firewall's allow list (no more clusters on these subnets)
	 * @param subnets
	 * @throws CPEException
	 */
	public abstract void removeCustomerSubnets(Gateway mgmtGateway, Set<String> subnets) throws CPEException;
	

	/**
	 * Add the VIF to the gateway with the all vlan subnet addresses
	 * @param gateway
	 * @param vlan
	 * @throws CPEException
	 */
	public abstract void addVifGateway(Gateway gateway, Vlan vlan) throws CPEException;

	/**
	 * Remove the VIF from the gateway
	 * @param gatewayId
	 * @param vlanNumber
	 * @param networkSpace
	 * @throws CPEException
	 */
	public abstract void removeVif(Gateway gateay, Vlan vlan) throws CPEException;


	/**
	 * Create all VPN tunnels on a gateway and delete the ones that aren't configured
	 * @param gatewayId
	 * @param clusterId
	 * @throws CPEException
	 */
	public abstract void configureTunnels(String gatewayId) throws CPEException;
	
	/**
	 * Remove VPN tunnel configuration on its associated gateway and reconfigure
	 * @param gatewayId
	 * @param clusterId
	 * @throws CPEException
	 */
	public abstract void removeTunnel(String tunnelId) throws CPEException;

	/**
	 * Validate the VPN Tunnel passed  from a cluster request
	 * @param vpnTunnel
	 * @throws CPEException 
	 */
	public abstract void validateVPNTunnel(VPNTunnel vpnTunnel) throws CPEException;
	
	/**
	 * @param tunnelId
	 * @return
	 * @throws CPEException
	 */
	public abstract VPNTunnel getVPNTunnel(String tunnelId, boolean getStatus) throws CPEException;
	

	/**
	 * Get all VPN tunnels on a particular gateway
	 * @param gatewayId
	 * @return
	 * @throws CPEException
	 */
	public abstract List<VPNTunnel> getVPNTunnels(String gatewayId) throws CPEException;
	
	
	/**
	 * Add a new VPN Tunnel
	 * @param tunnel
	 * @throws CPEException
	 */
	public abstract void addVPNTunnelToGateway(String gatewayId, VPNTunnel tunnel) throws CPEException;
	
	/**
	 * Modify existing VPN Tunnel
	 * @param vpnTunnels
	 * @throws CPEException
	 */
	public abstract void updateVPNTunnel(String tunnelId, VPNTunnel vpnTunnel) throws CPEException;

	/**
	 * Set default firewall rules to a gateway
	 * @param gatewayId
	 * @throws CPEException
	 */
	public abstract void addDefaultFirewallRules(Gateway gateway) throws CPEException;

	/**
	 * Update the credentials on the gateway
	 * @param gateway
	 * @throws CPEException
	 */
	public abstract void updateGatewayConfigSync(Gateway gateway) throws CPEException;
}