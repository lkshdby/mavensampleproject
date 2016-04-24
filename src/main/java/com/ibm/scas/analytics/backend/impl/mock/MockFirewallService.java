/**
 * 
 */
package com.ibm.scas.analytics.backend.impl.mock;

import java.util.List;
import java.util.Set;

import com.ibm.scas.analytics.backend.FirewallService;
import com.ibm.scas.analytics.beans.Gateway;
import com.ibm.scas.analytics.beans.VPNTunnel;
import com.ibm.scas.analytics.beans.Vlan;
import com.ibm.scas.analytics.utils.CPEException;

/**
 * @author jkwong
 *
 */
public class MockFirewallService implements FirewallService {

	@Override
	public void configureTunnels(String gatewayId) throws CPEException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void validateVPNTunnel(VPNTunnel vpnTunnel) throws CPEException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeTunnel(String tunnelId) throws CPEException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public VPNTunnel getVPNTunnel(String tunnelId, boolean getStatus) throws CPEException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<VPNTunnel> getVPNTunnels(String gatewayId) throws CPEException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateVPNTunnel(String tunnelId, VPNTunnel vpnTunnels) throws CPEException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addVifGateway(Gateway gateway, Vlan vlan) throws CPEException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addDefaultFirewallRules(Gateway gateway) throws CPEException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void testGatewayCredentials(Gateway gateway) throws CPEException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void allowManagementSubnets(Gateway mgmtGateway, Set<String> subnets) throws CPEException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void allowCustomerSubnets(Gateway mgmtGateway, Set<String> subnets) throws CPEException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeMgmtSubnets(Gateway mgmtGateway, Set<String> subnetList) throws CPEException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeCustomerSubnets(Gateway mgmtGateway, Set<String> subnets) throws CPEException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeVif(Gateway gateay, Vlan vlan) throws CPEException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getSSLCertificate(String memberPrivateIp) throws CPEException {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public void updateGatewayConfigSync(Gateway gateway) throws CPEException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addVPNTunnelToGateway(String gatewayId, VPNTunnel tunnel)
			throws CPEException {
		// TODO Auto-generated method stub
		
	}
}
