package com.ibm.scas.analytics.backend.impl;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ibm.scas.analytics.EngineProperties;
import com.ibm.scas.analytics.backend.FirewallService;
import com.ibm.scas.analytics.backend.NetworkService;
import com.ibm.scas.analytics.backend.VyattaAPIGateway;
import com.ibm.scas.analytics.backend.monitoring.NagiosEventLogger;
import com.ibm.scas.analytics.beans.Gateway;
import com.ibm.scas.analytics.beans.Gateway.GatewayType;
import com.ibm.scas.analytics.beans.GatewayMember;
import com.ibm.scas.analytics.beans.SoftLayerIdObject;
import com.ibm.scas.analytics.beans.VPNTunnel;
import com.ibm.scas.analytics.beans.VPNTunnel.IPSecVPNAuthModes;
import com.ibm.scas.analytics.beans.VPNTunnel.IPSecVPNDHGroup;
import com.ibm.scas.analytics.beans.VPNTunnel.IPSecVPNESP_PFS;
import com.ibm.scas.analytics.beans.VPNTunnel.IPSecVPNEncAlgorithms;
import com.ibm.scas.analytics.beans.VPNTunnel.IPSecVPNHashAlgorithms;
import com.ibm.scas.analytics.beans.VPNTunnel.VPNParamNames;
import com.ibm.scas.analytics.beans.VPNTunnel.VPNTunnelConnectionStatus;
import com.ibm.scas.analytics.beans.Vlan;
import com.ibm.scas.analytics.beans.Vlan.VlanNetworkSpace;
import com.ibm.scas.analytics.beans.utils.BeanConversionUtil;
import com.ibm.scas.analytics.persistence.PersistenceService;
import com.ibm.scas.analytics.persistence.util.WhereClause;
import com.ibm.scas.analytics.utils.AES;
import com.ibm.scas.analytics.utils.CPEException;
import com.ibm.scas.analytics.utils.CPEParamException;
import com.ibm.scas.analytics.utils.IPAddressUtil;
import com.ibm.scas.analytics.utils.JsonUtil;

@Singleton
public class VyattaFirewallService implements FirewallService {
	private final static Logger logger = Logger.getLogger(VyattaFirewallService.class);

	// firewall ruleset names -- bidirectional
	private static final String FIREWALL_NAME_CUST_PRIV_TO_SL_PRIV = "CustPriToSLPri";
	private static final String FIREWALL_NAME_SL_PRIV_TO_CUST_PRIV = "SLPriToCustPri";
	private static final String FIREWALL_NAME_CUST_PUB_TO_SL_PUB = "CustPubToSLPub";
	private static final String FIREWALL_NAME_SL_PUB_TO_CUST_PUB = "SLPubToCustPub";
	private static final String FIREWALL_NAME_LOCAL_TO_SL_PUB = "LocalToSLPub";
	private static final String FIREWALL_NAME_SL_PUB_TO_LOCAL = "SLPubToLocal";
	private static final String FIREWALL_NAME_LOCAL_TO_SL_PRIV = "LocalToSLPri";
	private static final String FIREWALL_NAME_SL_PRIV_TO_LOCAL = "SLPriToLocal";
	private static final String FIREWALL_NAME_LOCAL_TO_CUST_PRIV = "LocalToCustPri";
	private static final String FIREWALL_NAME_CUST_PRIV_TO_LOCAL = "CustPriToLocal";
	private static final String FIREWALL_NAME_CUST_PUB_TO_LOCAL = "CustPubToLocal";
	private static final String FIREWALL_NAME_LOCAL_TO_CUST_PUB = "LocalToCustPub";
	private static final String FIREWALL_NAME_SL_PUB_TO_CUST_PRIV = "SLPubToCustPri";
	private static final String FIREWALL_NAME_CUST_PRIV_TO_SL_PUB = "CustPriToSLPub";
	
	// dynamic network groups
	private static final String FIREWALL_NETWORK_GROUP_VYATTA_PRI_ALLOW_NETS = "VyattaPriAllowNets";
	private static final String FIREWALL_NETWORK_GROUP_CUST_PRI_ALLOW_NETS = "CustZonePriAllowNets";
	private static final String FIREWALL_NETWORK_GROUP_MGMT = "ICASMgmtSubnets";
	private static final String FIREWALL_ADDRESS_GROUP_GW_MEMBERS = "VyattaGatewayMembers";
	
	// static network groups
	private static final String FIREWALL_NETWORK_GROUP_SLPRIVATEALLOWED = "SoftLayerPriAllowNets";
	private static final String FIREWALL_NETWORK_GROUP_SLPUBLICALLOWED = "SoftLayerPubAllowNets";
	private static final String[] FIREWALL_NETWORK_GROUP_SLPUBLIC_ALLOWED_NETWORKS = {
		 "159.253.158.0/23",
		 "159.8.198.0/23",
		 "66.228.118.0/23",
		 "173.192.118.0/23",
		 "184.172.118.0/23",
		 "50.22.118.0/23",
		 "198.23.118.0/23",
		 "159.122.118.0/23",
		 "119.81.138.0/23",
		 "173.193.118.0/23",
		 "5.10.118.0/23",
		 "168.1.118.0/23",
		 "169.57.118.0/23",
		 "159.122.138.0/23",
		 "169.54.118.0/23",
		 "159.8.118.0/23",
		 "169.57.138.0/23",
		 "67.228.118.0/23",
		 "50.23.118.0/23",
		 "169.45.118.0/23",
		 "174.133.118.0/23",
		 "168.1.18.0/23",
		 "161.202.118.0/23",
		 "158.85.118.0/23",
		 "208.43.118.0/23",
		 "169.55.118.0/23",
	};
	private static final String[] FIREWALL_NETWORK_GROUP_SLPRIVATE_ALLOWED_NETWORKS = {
		"10.2.64.0/20", 
		"10.3.128.0/20", 
		"10.0.64.0/19",
		"10.1.128.0/19",
		"10.2.128.0/20",
		"10.1.176.0/20",
		"10.2.112.0/20",
		"10.3.80.0/20",
		"10.2.160.0/20",
		"10.1.160.0/20",
		"10.1.208.0/20",
		"10.2.80.0/20",
		"10.2.176.0/20",
		"10.3.112.0/20",
		"10.2.144.0/20",
		"10.1.64.0/19",
		"10.1.192.0/20",
		"10.2.32.0/20",
		"10.3.96.0/20",
		"10.3.64.0/20",
		"10.2.48.0/20",
		"10.1.96.0/19",
		"10.2.200.0/23",
		"10.3.220.0/24",
		"10.1.0.0/23",
		"10.1.24.0/23",
		"10.2.208.0/23",
		"10.1.236.0/24",
		"10.2.232.0/24",
		"10.2.236.0/24",
		"10.2.216.0/24",
		"10.1.56.0/23",
		"10.2.220.0/24",
		"10.2.228.0/24",
		"10.3.232.0/24",
		"10.3.224.0/24",
		"10.3.236.0/24",
		"10.1.8.0/23",
		"10.1.224.0/23",
		"10.2.192.0/23",
		"10.3.228.0/24",
		"10.2.224.0/24",
		"10.1.232.0/24",
		"10.1.16.0/23",
		"10.1.41.0/24",
		"10.1.49.0/24",
		"10.1.53.0/24",
		"10.1.33.0/24",
		"10.1.37.0/24",
		"10.1.45.0/24",
		"10.2.203.0/24",
		"10.3.221.0/24",
		"10.1.2.0/24",
		"10.1.27.0/24",
		"10.2.211.0/24",
		"10.1.238.0/24",
		"10.2.233.0/24",
		"10.2.237.0/24",
		"10.2.217.0/24",
		"10.1.59.0/24",
		"10.2.221.0/24"
	};
	
	private static final String FIREWALL_NETWORK_GROUP_LOCAL_MULTICAST = "LocalMulticast";
	private static final String[] FIREWALL_NETWORK_GROUP_LOCAL_MULTICAST_ALLOWED_NETWORKS = {
		"224.0.0.0/24"
	};

	// port groups
	private static final String FIREWALL_PORT_GROUP_MGMT_TO_VM = "MgmtToCustNodes";
	private static final String[] FIREWALL_PORT_GROUP_MGMT_TO_VM_ALLOWED_PORTS = { "7869", "7871", "8444" };
	private static final String FIREWALL_PORT_GROUP_VM_TO_MGMT = "CustNodesToMgmt";
	private static final String[] FIREWALL_PORT_GROUP_VM_TO_MGMT_ALLOWED_PORTS = { "7869", "7870", "7871", "60000", "443", "80" };
	private static final String FIREWALL_PORT_GROUP_PUB_APP = "ApplicationPortsAllowed";
	private static final String[] FIREWALL_PORT_GROUP_PUB_APP_ALLOWED_PORTS = { "18000", "22", "8000-8090", "8443-8453", "5696", "7180", "33001" };
	private static final String FIREWALL_PORT_GROUP_VPN = "VPNPortsAllowed";
	private static final String[] FIREWALL_PORT_GROUP_VPN_ALLOWED_PORTS = { "50", "51", "500", "4500" };
	private static final String FIREWALL_PORT_GROUP_REST_API = "RESTAPIPortsAllowed";
	private static final String[] FIREWALL_PORT_GROUP_REST_API_ALLOWED_PORTS = { "443" };
	private static final String FIREWALL_PORT_GROUP_SSH = "SSHPortsAllowed";
	private static final String[] FIREWALL_PORT_GROUP_SSH_ALLOWED_PORTS = { "22" };
	
	// zones
	private enum Zone {
		SL_PRIVATE { @Override public String toString() { return "SoftLayerPrivate"; } },
		SL_PUBLIC { @Override public String toString() { return "SoftLayerPublic"; } },
		CUST_PRIV { @Override public String toString() { return "CustPriv"; } },
		CUST_PUB { @Override public String toString() { return "CustPub"; } },
		LOCAL { @Override public String toString() { return "ZoneLocal"; } },
	};
	
	
	private static String[][] FIREWALL_ZONE_POLICY = { // From/To table
		/* FIREWALL_ZONE_SL_PRIVATE, FIREWALL_ZONE_SL_PUBLIC, FIREWALL_ZONE_CUST_PRIV, FIREWALL_ZONE_CUST_PUB, FIREWALL_ZONE_LOCAL */
		/* FIREWALL_ZONE_SL_PRIVATE */ { null, null, FIREWALL_NAME_SL_PRIV_TO_CUST_PRIV,  null, FIREWALL_NAME_SL_PRIV_TO_LOCAL },  
		/* FIREWALL_ZONE_SL_PUBLIC */ { null, null,  FIREWALL_NAME_SL_PUB_TO_CUST_PRIV, FIREWALL_NAME_SL_PUB_TO_CUST_PUB, FIREWALL_NAME_SL_PUB_TO_LOCAL },  
		/* FIREWALL_ZONE_CUST_PRIVATE */ { FIREWALL_NAME_CUST_PRIV_TO_SL_PRIV, FIREWALL_NAME_CUST_PRIV_TO_SL_PUB,  null, null, FIREWALL_NAME_CUST_PRIV_TO_LOCAL }, 
		/* FIREWALL_ZONE_CUST_PUBLIC */ { null, FIREWALL_NAME_CUST_PUB_TO_SL_PUB,  null, null, FIREWALL_NAME_CUST_PUB_TO_LOCAL}, 
		/* FIREWALL_ZONE_LOCAL */ { FIREWALL_NAME_LOCAL_TO_SL_PRIV, FIREWALL_NAME_LOCAL_TO_SL_PUB,  FIREWALL_NAME_LOCAL_TO_CUST_PRIV, FIREWALL_NAME_LOCAL_TO_CUST_PUB, null}, 
	};

	// port group map
	private static final Map<String, String[]> FIREWALL_PORT_GROUPS = new HashMap<String, String[]>();
	private static final Map<String, String[]> FIREWALL_NETWORK_GROUPS = new HashMap<String, String[]>();

	// VPN param maps
	public static final EnumMap<IPSecVPNEncAlgorithms, String> vyattaEncAlgTransMap = new EnumMap<IPSecVPNEncAlgorithms, String>(IPSecVPNEncAlgorithms.class);
	public static final EnumMap<IPSecVPNHashAlgorithms, String> vyattaHashAlgTransMap = new EnumMap<IPSecVPNHashAlgorithms, String>(IPSecVPNHashAlgorithms.class);
	public static final EnumMap<IPSecVPNAuthModes, String> vyattaAuthModeTransMap = new EnumMap<IPSecVPNAuthModes, String>(IPSecVPNAuthModes.class);
	public static final EnumMap<IPSecVPNDHGroup, String> vyattaDHGroupTransMap = new EnumMap<IPSecVPNDHGroup, String>(IPSecVPNDHGroup.class);
	public static final EnumMap<IPSecVPNESP_PFS, String> vyattaESP_PFSTransMap = new EnumMap<IPSecVPNESP_PFS, String>(IPSecVPNESP_PFS.class);
	
	static {
		// Vyatta specific strings for encryption algorithms
		vyattaEncAlgTransMap.put(IPSecVPNEncAlgorithms.AES256, "aes256");
		vyattaEncAlgTransMap.put(IPSecVPNEncAlgorithms.AES128, "aes128");
		vyattaEncAlgTransMap.put(IPSecVPNEncAlgorithms.TRIPLEDES, "3des");
		
		// Vyatta specific strings for hash algorithms
		vyattaHashAlgTransMap.put(IPSecVPNHashAlgorithms.SHA1, "sha1");
		vyattaHashAlgTransMap.put(IPSecVPNHashAlgorithms.MD5, "md5");
		
		// Vyatta specific strings for authentication modes
		vyattaAuthModeTransMap.put(IPSecVPNAuthModes.PRE_SHARED_KEY, "pre-shared-secret");
		
		// Vyatta specific strings for DH-Group
		vyattaDHGroupTransMap.put(IPSecVPNDHGroup.DH_GROUP_2, "2");	
		vyattaDHGroupTransMap.put(IPSecVPNDHGroup.DH_GROUP_5, "5");	

		// Vyatta specific strings for DH-Group
		vyattaESP_PFSTransMap.put(IPSecVPNESP_PFS.ENABLE, "enable");	
		vyattaESP_PFSTransMap.put(IPSecVPNESP_PFS.DH_GROUP_2, "2");	
		vyattaESP_PFSTransMap.put(IPSecVPNESP_PFS.DH_GROUP_5, "5");		
		vyattaESP_PFSTransMap.put(IPSecVPNESP_PFS.DISABLE, "disable");		
		
		// port gorup mapping
		FIREWALL_PORT_GROUPS.put(FIREWALL_PORT_GROUP_MGMT_TO_VM, FIREWALL_PORT_GROUP_MGMT_TO_VM_ALLOWED_PORTS);
		FIREWALL_PORT_GROUPS.put(FIREWALL_PORT_GROUP_VM_TO_MGMT, FIREWALL_PORT_GROUP_VM_TO_MGMT_ALLOWED_PORTS);
		FIREWALL_PORT_GROUPS.put(FIREWALL_PORT_GROUP_PUB_APP, FIREWALL_PORT_GROUP_PUB_APP_ALLOWED_PORTS);
		FIREWALL_PORT_GROUPS.put(FIREWALL_PORT_GROUP_VPN, FIREWALL_PORT_GROUP_VPN_ALLOWED_PORTS);
		FIREWALL_PORT_GROUPS.put(FIREWALL_PORT_GROUP_REST_API, FIREWALL_PORT_GROUP_REST_API_ALLOWED_PORTS);
		FIREWALL_PORT_GROUPS.put(FIREWALL_PORT_GROUP_SSH, FIREWALL_PORT_GROUP_SSH_ALLOWED_PORTS);
		
		// network group mapping
		FIREWALL_NETWORK_GROUPS.put(FIREWALL_NETWORK_GROUP_SLPRIVATEALLOWED, FIREWALL_NETWORK_GROUP_SLPRIVATE_ALLOWED_NETWORKS);
		FIREWALL_NETWORK_GROUPS.put(FIREWALL_NETWORK_GROUP_SLPUBLICALLOWED, FIREWALL_NETWORK_GROUP_SLPUBLIC_ALLOWED_NETWORKS);
		FIREWALL_NETWORK_GROUPS.put(FIREWALL_NETWORK_GROUP_LOCAL_MULTICAST, FIREWALL_NETWORK_GROUP_LOCAL_MULTICAST_ALLOWED_NETWORKS);
	}
	
	@Inject private PersistenceService persistence;
	@Inject private NetworkService networkService;		// circular dependency?
	
	/* (non-Javadoc)
	 * @see com.ibm.scas.analytics.backend.impl.FirewallService#testGatewayCredentials(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void testGatewayCredentials(Gateway gateway) throws CPEException {
		final VyattaAPIGateway vgw = new VyattaAPIGateway(gateway);
		vgw.testCredentials();
	}
	
	@Override
	public void updateGatewayConfigSync(Gateway gateway) throws CPEException {

		// assume the updated credentials work, update the config-sync tree to use these credentials
		final VyattaAPIGateway vgw = new VyattaAPIGateway(gateway);
		
		// create the config-sync tree
		try {
			// start the session on all members
			vgw.startSessionAllMembers();
			
			// now recreate it with updated user/password
			createConfigSyncTree(gateway, vgw);
			
			vgw.commitChanges();
			vgw.saveChanges();
		} catch (CPEException e) {
			throw new CPEException("Unable to update config-sync configuration with updated gateway credentials: " + e.getLocalizedMessage(), e);
		} finally {
			vgw.destroySession();
		}
		
	}
	
	/**
	 *  helper function to add networks to network group 
	 **/
	private static void allowSubnetsOnFirewallGroup(Gateway gateway, final Set<String> subnets, final String firewallGroupName) throws CPEException {
		final VyattaAPIGateway vyatta = new VyattaAPIGateway(gateway);
		boolean sessionStarted = vyatta.startSession(gateway.getMasterGatewayMember().getMemberIp());
		try {
			// first, get what's on the vyatta already
			final Set<String> subnetsInList = getFirewallGroupSubnets(vyatta, firewallGroupName);
			final Iterator<String> subnetIter = subnets.iterator();
			while (subnetIter.hasNext()) {
				final String subnet = subnetIter.next();
				if (subnetsInList.contains(subnet)) {
					if (logger.isDebugEnabled()) {
						logger.debug(String.format("Subnet %s is already in the network group %s", subnet, firewallGroupName));
					}		
	
					subnetIter.remove();
					continue;
				}
			}
			
			for (final String subnet : subnets) {
				vyatta.callConfigureCommand(String.format("set firewall group network-group %s network %s", firewallGroupName, subnet));
			}

			vyatta.commitChanges();
			vyatta.saveChanges();
			
		} finally {
			if (sessionStarted) {
				vyatta.destroySession();
			}
		}   		
	}
	/**
	 *  helper function to remove networks from network group 
	 **/
	private static void removeSubnetsFromFirewallGroup(Gateway gw, final Set<String> subnets, final String firewallGroupName) throws CPEException {
		final VyattaAPIGateway vyatta = new VyattaAPIGateway(gw);
		boolean sessionStarted = vyatta.startSession(gw.getMasterGatewayMember().getMemberIp());
		try {
			// first, get what's on the vyatta already
			final Set<String> subnetsInList = getFirewallGroupSubnets(vyatta, firewallGroupName);
			final Iterator<String> subnetIter = subnets.iterator();
			while (subnetIter.hasNext()) {
				final String subnet = subnetIter.next();
				if (!subnetsInList.contains(subnet)) {
					if (logger.isDebugEnabled()) {
						logger.debug(String.format("Subnet %s is not in the network group %s", subnet, firewallGroupName));
					}		
	
					subnetIter.remove();
					continue;
				}
			}
			
			for (final String subnet : subnets) {
				try {
					vyatta.callConfigureCommand(String.format("delete firewall group network-group %s network %s", firewallGroupName, subnet));
				} catch (CPEException e) {
					// non-fatal error if we can't delete this, it may not exist
					logger.debug("Non-fatal error: " + e.getLocalizedMessage(), e);
				}
			}

			vyatta.commitChanges();
			vyatta.saveChanges();
			
		} finally {
			if (sessionStarted) {
				vyatta.destroySession();
			}
		}   		
	}

	private static Set<String> getLocalPrivateMgmtSubnets() throws CPEException {
		// get the local mgmt subnets by looking at my own IP addresses in the
		// softlayer private network
	
		final Set<String> mgmtSubnets = new HashSet<String>();
		final Set<String> myIPAddrs = IPAddressUtil.getMyIPv4Addrs();
		
		final Iterator<String> myIPAddrIterator = myIPAddrs.iterator();
		
		while (myIPAddrIterator.hasNext()) {
			final String myIPAddr = myIPAddrIterator.next();
			if (!myIPAddr.startsWith("10.")) {
				continue;
			}
			
			final String[] ipAddrSplit = myIPAddr.split("/");
			final String networkAddr = IPAddressUtil.getNetworkAddr(ipAddrSplit[0], Integer.valueOf(ipAddrSplit[1]));
			
			mgmtSubnets.add(networkAddr + "/" + ipAddrSplit[1]);
		}
		
		return mgmtSubnets;
	}
	
	/**
	 * create a default firewall ruleset.  the firewall by default will allow related/established traffic, and drop all other packets
	 * with logging to syslog
	 * @param vgw
	 * @param firewallName
	 * @throws CPEException
	 */
	private static void createDefaultFirewallRuleset(String memberIp, VyattaAPIGateway vgw, String firewallName) throws CPEException {
		// clear the old firewall
		try {
			vgw.callConfigureCommand(memberIp, String.format("delete firewall name %s", firewallName));
		} catch (CPEException e) {
			// non-fatal error if we can't delete this, it may not exist
			logger.debug("Non-fatal error: " + e.getLocalizedMessage(), e);
		}

		// drop all packets by default
		vgw.callConfigureCommand(memberIp, String.format("set firewall name %s default-action drop", firewallName));
		vgw.callConfigureCommand(memberIp, String.format("set firewall name %s enable-default-log", firewallName));

		// allow related and established traffic (rule 5)
		vgw.callConfigureCommand(memberIp, String.format("set firewall name %s rule 5 action accept", firewallName));
		vgw.callConfigureCommand(memberIp, String.format("set firewall name %s rule 5 protocol all", firewallName));
		vgw.callConfigureCommand(memberIp, String.format("set firewall name %s rule 5 state established enable", firewallName));
		vgw.callConfigureCommand(memberIp, String.format("set firewall name %s rule 5 state related enable", firewallName));
		vgw.callConfigureCommand(memberIp, String.format("set firewall name %s rule 5 state new disable", firewallName));
		vgw.callConfigureCommand(memberIp, String.format("set firewall name %s rule 5 state invalid disable", firewallName));
		
		// drop everything else (rule 9999) and log to syslog
		vgw.callConfigureCommand(memberIp, String.format("set firewall name %s rule 9999 action drop", firewallName));
		vgw.callConfigureCommand(memberIp, String.format("set firewall name %s rule 9999 log enable", firewallName));
		vgw.callConfigureCommand(memberIp, String.format("set firewall name %s rule 9999 protocol all", firewallName));
	}
	
	/**
	 * configure the zone policy for the VIF based on the FIREWALL_ZONE_POLICY table
	 * @param vgw
	 * @param zone
	 * @param interfaceName
	 * @param vlanNumber
	 */
	private static void createZonePolicyForVif(VyattaAPIGateway vgw, Zone zone, String interfaceName, String vlanNumber) throws CPEException {
		final String myZoneName = String.format("%s%s", zone, vlanNumber);
		
		try {
			vgw.callConfigureCommand(String.format("delete zone-policy zone %s", myZoneName));
		} catch (CPEException e) {
			// non-fatal error if we can't delete this, it may not exist
			logger.debug("Non-fatal error: " + e.getLocalizedMessage(), e);
		}
		vgw.callConfigureCommand(String.format("set zone-policy zone %s default-action drop", myZoneName));
		vgw.callConfigureCommand(String.format("set zone-policy zone %s interface %s.%s", myZoneName, interfaceName, vlanNumber));
		
		for (final Zone fromZone : Zone.values()) {
			for (final Zone toZone : Zone.values()) {
				if (FIREWALL_ZONE_POLICY[fromZone.ordinal()][toZone.ordinal()] == null) {
					// no zone policy to apply
					continue;
				}
				
				if (fromZone == zone) {
					vgw.callConfigureCommand(String.format("set zone-policy zone %s from %s firewall name %s", toZone, myZoneName, FIREWALL_ZONE_POLICY[fromZone.ordinal()][toZone.ordinal()]));
				}
				
				if (toZone == zone) {
					vgw.callConfigureCommand(String.format("set zone-policy zone %s from %s firewall name %s", myZoneName, fromZone, FIREWALL_ZONE_POLICY[fromZone.ordinal()][toZone.ordinal()]));
				}
			}
		}
	}
	
	/**
	 * remove the zone policy for the VIF based on the FIREWALL_ZONE_POLICY table
	 * @param vgw
	 * @param zone
	 * @param interfaceName
	 * @param vlanNumber
	 */
	private static void deleteZonePolicyForVif(VyattaAPIGateway vgw, Zone zone, String vlanNumber) throws CPEException {
		final String myZoneName = String.format("%s%s", zone, vlanNumber);
		
		// delete the zone itself
		try {
			vgw.callConfigureCommand(String.format("delete zone-policy zone %s", myZoneName));
		} catch (CPEException e) {
			// non-fatal error if we can't delete this, it may not exist
			logger.debug("Non-fatal error: " + e.getLocalizedMessage(), e);
		}
		
		// loop through all other zones, delete the "from"
		for (final Zone fromZone : Zone.values()) {
			for (final Zone toZone : Zone.values()) {
				if (FIREWALL_ZONE_POLICY[fromZone.ordinal()][toZone.ordinal()] == null) {
					// no zone policy was applied
					continue;
				}
				
				if (fromZone != zone) {
					continue;
				}
				
				// delete all zone policies where my zone name appears
				try {
					vgw.callConfigureCommand(String.format("delete zone-policy zone %s from %s", toZone, myZoneName));
				} catch (CPEException e) {
					// non-fatal error if we can't delete this, it may not exist
					logger.debug("Non-fatal error: " + e.getLocalizedMessage(), e);
				}
			}
		}
	}
	
	private static void createConfigSyncTree(Gateway gateway, VyattaAPIGateway vgw) throws CPEException {
		String vyattaConfigSync = EngineProperties.getInstance().getProperty(EngineProperties.FIREWALL_VYATTA_CONFIG_SYNC);
		if (StringUtils.isBlank(vyattaConfigSync)) {
			vyattaConfigSync = EngineProperties.getInstance().getProperty(EngineProperties.FIREWALL_VYATTA_CONFIG_SYNC + "." + gateway.getId());
		}
		final Boolean manageConfigSync = Boolean.valueOf(vyattaConfigSync);
		if (manageConfigSync == null || !manageConfigSync) {
			// don't generate config-sync tree
			return;
		}
		
		// get the config-sync user/password from engine.properties
		String vyattaConfigSyncUser = EngineProperties.getInstance().getProperty(EngineProperties.FIREWALL_VYATTA_CONFIG_SYNC_USER_PREFIX);
		String vyattaConfigSyncPasswd = EngineProperties.getInstance().getProperty(EngineProperties.FIREWALL_VYATTA_CONFIG_SYNC_PASSWD_PREFIX);
		
		if (StringUtils.isBlank(vyattaConfigSyncUser)) {
			// check if there's one defined for my gateway
			vyattaConfigSyncUser = EngineProperties.getInstance().getProperty(EngineProperties.FIREWALL_VYATTA_CONFIG_SYNC_USER_PREFIX + "." + gateway.getId());
		}
		
		if (StringUtils.isBlank(vyattaConfigSyncPasswd)) {
			// check if there's one defined for my gateway
			vyattaConfigSyncPasswd = EngineProperties.getInstance().getProperty(EngineProperties.FIREWALL_VYATTA_CONFIG_SYNC_PASSWD_PREFIX + "."  + gateway.getId());
		}
		
		try {			
			// first, delete and recreate the config-sync sync-map tree on all members
			try {
				vgw.callConfigureCommand(String.format("delete system config-sync sync-map CPE-SYNC"));
			} catch (CPEException e) {
				// non-fatal error if we can't delete this, it may not exist
				logger.debug("Non-fatal error: " + e.getLocalizedMessage(), e);
			}
			vgw.callConfigureCommand(String.format("set system config-sync sync-map CPE-SYNC rule 1 action include"));
			vgw.callConfigureCommand(String.format("set system config-sync sync-map CPE-SYNC rule 1 location nat"));
			vgw.callConfigureCommand(String.format("set system config-sync sync-map CPE-SYNC rule 2 action include"));
			vgw.callConfigureCommand(String.format("set system config-sync sync-map CPE-SYNC rule 2 location firewall"));
			vgw.callConfigureCommand(String.format("set system config-sync sync-map CPE-SYNC rule 3 action include"));
			vgw.callConfigureCommand(String.format("set system config-sync sync-map CPE-SYNC rule 3 location vpn"));
			vgw.callConfigureCommand(String.format("set system config-sync sync-map CPE-SYNC rule 4 action include"));
			vgw.callConfigureCommand(String.format("set system config-sync sync-map CPE-SYNC rule 4 location zone-policy"));

			// create the credentials used to sync the CPE-SYNC map.  BE CAREFUL HERE!  we do not want to delete any other sync-maps
			// that are added on top.  for example, delivery could sync system login in a different sync-map; we want to keep that intact
			for (final GatewayMember member : gateway.getGatewayMembers()) {
				vgw.callConfigureCommand(String.format("set system config-sync remote-router %s sync-map CPE-SYNC", member.getMemberIp()));
				
				if (StringUtils.isBlank(vyattaConfigSyncUser) || StringUtils.isBlank(vyattaConfigSyncPasswd)) {
					logger.debug("No config-sync username/password is set in engine.properties for gateway " + gateway.getId() + " (" + gateway.getName() + ", using CPE member username/passwords for config-sync");
					vgw.callConfigureCommand(String.format("set system config-sync remote-router %s username %s", member.getMemberIp(), member.getUsername()));
					vgw.callConfigureCommand(String.format("set system config-sync remote-router %s password %s", member.getMemberIp(), member.getPassword()));
				} else {
					logger.debug("Setting " + vyattaConfigSyncUser +" as remote-router user for gateway " + gateway.getId() + " (" + gateway.getName() + ") member " + member.getMemberIp());
					vgw.callConfigureCommand(String.format("set system config-sync remote-router %s username %s", member.getMemberIp(), vyattaConfigSyncUser));
					vgw.callConfigureCommand(String.format("set system config-sync remote-router %s password %s", member.getMemberIp(), new AES().decrypt(vyattaConfigSyncPasswd)));
				}
			}
				
			// add users to config-sync map
			String configSyncUsers = EngineProperties.getInstance().getProperty(EngineProperties.FIREWALL_VYATTA_CONFIG_SYNC_USERS_PREFIX);
			if (StringUtils.isBlank(configSyncUsers)) {
				configSyncUsers = EngineProperties.getInstance().getProperty(EngineProperties.FIREWALL_VYATTA_CONFIG_SYNC_USERS_PREFIX) + "." + gateway.getId();
			}
			
			int ruleIdx = 5;
			if (!StringUtils.isBlank(configSyncUsers)) {
				for (final String configSyncUser : configSyncUsers.split(",")) {
					if (configSyncUser.equals("*")) {
						// sync all users
						logger.debug("Adding \"system login\" to config-sync sync-map CPE-SYNC for gateway " + gateway.getId() + " (" + gateway.getName() + ")");
						vgw.callConfigureCommand(String.format("set system config-sync sync-map CPE-SYNC rule " + ruleIdx + "  action include"));
						vgw.callConfigureCommand(String.format("set system config-sync sync-map CPE-SYNC rule " + ruleIdx + " location \"system login user\""));						
						break;
					}

					logger.debug("Adding \"system login " + configSyncUser + "\" to config-sync sync-map CPE-SYNC for gateway " + gateway.getId() + " (" + gateway.getName() + ")");
					vgw.callConfigureCommand(String.format("set system config-sync sync-map CPE-SYNC rule " + ruleIdx + "  action include"));
					vgw.callConfigureCommand(String.format("set system config-sync sync-map CPE-SYNC rule " + ruleIdx + " location \"system login user %s\"", configSyncUser));
					ruleIdx = ruleIdx + 1;
				}
			}
		} catch (CPEException e) {
			throw new CPEException(String.format("Error creating default firewall rules on gateway %s: %s", gateway.getName(), e.getLocalizedMessage()), e);
		} catch (GeneralSecurityException e) {
			throw new CPEException(String.format("Error decrypting password in engine.properties for config-sync on gateway %s (%s): %s", gateway.getId(), gateway.getName(), e.getLocalizedMessage()), e);
		}
	}
	
	private static void createFirewallGroups(Gateway gateway, VyattaAPIGateway vgw) throws CPEException {
		// find out the master gateway's IP
		final String masterIp = gateway.getMasterGatewayMember().getMemberIp();
		
		// create the firewall groups -- on the master only

		// FIREWALL NETWORK GROUP CREATION
		// create the management subnet group and add my private mgmt subnets (dynamically figures out mgmt subnets)
		try {
			vgw.callConfigureCommand(masterIp, String.format("delete firewall group network-group %s", FIREWALL_NETWORK_GROUP_MGMT));
		} catch (CPEException e) {
			// non-fatal error if we can't delete this, it may not exist
			logger.debug("Non-fatal error: " + e.getLocalizedMessage(), e);
		}
		for (final String mgmtSubnet : getLocalPrivateMgmtSubnets()) {
			vgw.callConfigureCommand(masterIp, String.format("set firewall group network-group %s network %s", FIREWALL_NETWORK_GROUP_MGMT, mgmtSubnet));
		}

		// create remaining network groups
		for (final Entry<String, String[]> networkGroupEnt : FIREWALL_NETWORK_GROUPS.entrySet()) {
			try {
				vgw.callConfigureCommand(masterIp, String.format("delete firewall group network-group %s", networkGroupEnt.getKey()));
			} catch (CPEException e) {
				// non-fatal error if we can't delete this, it may not exist
				logger.debug("Non-fatal error: " + e.getLocalizedMessage(), e);
			}
			for (final String slSubnet : networkGroupEnt.getValue()) {
				vgw.callConfigureCommand(masterIp, String.format("set firewall group network-group %s network %s", networkGroupEnt.getKey(), slSubnet));
			}
		}

		// FIREWALL PORTGROUP CREATION
		for (final Entry<String, String[]> portGroupEnt : FIREWALL_PORT_GROUPS.entrySet()) {
			try {
				vgw.callConfigureCommand(masterIp, String.format("delete firewall group port-group %s", portGroupEnt.getKey()));
			} catch (CPEException e) {
				// non-fatal error if we can't delete this, it may not exist
				logger.debug("Non-fatal error: " + e.getLocalizedMessage(), e);
			}
			for (final String allowedPort : portGroupEnt.getValue()) {
				vgw.callConfigureCommand(masterIp, String.format("set firewall group port-group %s port %s", portGroupEnt.getKey(), allowedPort));
			}
		}

		// FIREWALL ADDRESS GROUP CREATION
		// create the Vyatta Gateway Members address group
		try {
			vgw.callConfigureCommand(masterIp, String.format("delete firewall group address-group %s", FIREWALL_ADDRESS_GROUP_GW_MEMBERS));
		} catch (CPEException e) {
			// non-fatal error if we can't delete this, it may not exist
			logger.debug("Non-fatal error: " + e.getLocalizedMessage(), e);
		}	
		for (final GatewayMember member : gateway.getGatewayMembers()) {
			vgw.callConfigureCommand(masterIp, String.format("set firewall group address-group %s address %s", FIREWALL_ADDRESS_GROUP_GW_MEMBERS, member.getMemberIp()));
		}
	}
	
	private static void createFirewallRules(Gateway gateway, VyattaAPIGateway vgw) throws CPEException {
		final String masterIp = gateway.getMasterGatewayMember().getMemberIp();

		// create the firewall rules for incoming traffic from SL Private Network
		createDefaultFirewallRuleset(masterIp, vgw, FIREWALL_NAME_SL_PRIV_TO_CUST_PRIV);

		// allow new and established traffic from all softlayer allowed subnets (rule 10)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 action accept", FIREWALL_NAME_SL_PRIV_TO_CUST_PRIV));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 protocol all", FIREWALL_NAME_SL_PRIV_TO_CUST_PRIV));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 source group network-group %s", FIREWALL_NAME_SL_PRIV_TO_CUST_PRIV, FIREWALL_NETWORK_GROUP_SLPRIVATEALLOWED));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 state new enable", FIREWALL_NAME_SL_PRIV_TO_CUST_PRIV));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 state established enable", FIREWALL_NAME_SL_PRIV_TO_CUST_PRIV));

		// allow new and established traffic for VM management from mgmt subnets (rule 20)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 action accept", FIREWALL_NAME_SL_PRIV_TO_CUST_PRIV));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 destination group port-group %s", FIREWALL_NAME_SL_PRIV_TO_CUST_PRIV, FIREWALL_PORT_GROUP_MGMT_TO_VM));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 source group network-group %s", FIREWALL_NAME_SL_PRIV_TO_CUST_PRIV, FIREWALL_NETWORK_GROUP_MGMT));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 protocol all", FIREWALL_NAME_SL_PRIV_TO_CUST_PRIV));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 state new enable", FIREWALL_NAME_SL_PRIV_TO_CUST_PRIV));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 state established enable", FIREWALL_NAME_SL_PRIV_TO_CUST_PRIV));

		// create the firewall rules for customer zone to SL Private
		createDefaultFirewallRuleset(masterIp, vgw, FIREWALL_NAME_CUST_PRIV_TO_SL_PRIV);

		// allow new and established traffic from all softlayer allowed subnets (rule 10)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 action accept", FIREWALL_NAME_CUST_PRIV_TO_SL_PRIV));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 protocol all", FIREWALL_NAME_CUST_PRIV_TO_SL_PRIV));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 destination group network-group %s", FIREWALL_NAME_CUST_PRIV_TO_SL_PRIV, FIREWALL_NETWORK_GROUP_SLPRIVATEALLOWED));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 state new enable", FIREWALL_NAME_CUST_PRIV_TO_SL_PRIV));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 state established enable", FIREWALL_NAME_CUST_PRIV_TO_SL_PRIV));

		// allow new and established traffic for VM management to mgmt subnets (rule 20)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 action accept", FIREWALL_NAME_CUST_PRIV_TO_SL_PRIV));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 destination group port-group %s", FIREWALL_NAME_CUST_PRIV_TO_SL_PRIV, FIREWALL_PORT_GROUP_VM_TO_MGMT));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 destination group network-group %s", FIREWALL_NAME_CUST_PRIV_TO_SL_PRIV, FIREWALL_NETWORK_GROUP_MGMT));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 protocol all", FIREWALL_NAME_CUST_PRIV_TO_SL_PRIV));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 state new enable", FIREWALL_NAME_CUST_PRIV_TO_SL_PRIV));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 state established enable", FIREWALL_NAME_CUST_PRIV_TO_SL_PRIV));

		// create the firewall rules for outgoing internet traffic from customer pub zone
		createDefaultFirewallRuleset(masterIp, vgw, FIREWALL_NAME_CUST_PUB_TO_SL_PUB);

		// allow outgoing TCP/UDP traffic (rule 10)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 action accept", FIREWALL_NAME_CUST_PUB_TO_SL_PUB));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 protocol tcp_udp", FIREWALL_NAME_CUST_PUB_TO_SL_PUB));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 state new enable", FIREWALL_NAME_CUST_PUB_TO_SL_PUB));

		// allow outgoing VPN traffic (IKE) -- for local tunnel initialization (rule 20)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 action accept", FIREWALL_NAME_CUST_PUB_TO_SL_PUB));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 protocol udp", FIREWALL_NAME_CUST_PUB_TO_SL_PUB));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 destination group port-group %s", FIREWALL_NAME_CUST_PUB_TO_SL_PUB, FIREWALL_PORT_GROUP_VPN));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 state new enable", FIREWALL_NAME_CUST_PUB_TO_SL_PUB));		
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 state established enable", FIREWALL_NAME_CUST_PUB_TO_SL_PUB));		
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 state related enable", FIREWALL_NAME_CUST_PUB_TO_SL_PUB));		
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 state invalid disable", FIREWALL_NAME_CUST_PUB_TO_SL_PUB));		

		// allow outgoing ESP packets -- for encrypted VPN tunnel (rule 30)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 30 action accept", FIREWALL_NAME_CUST_PUB_TO_SL_PUB));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 30 protocol esp", FIREWALL_NAME_CUST_PUB_TO_SL_PUB));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 30 state new enable", FIREWALL_NAME_CUST_PUB_TO_SL_PUB));		
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 30 state established enable", FIREWALL_NAME_CUST_PUB_TO_SL_PUB));				
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 30 state related enable", FIREWALL_NAME_CUST_PUB_TO_SL_PUB));				
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 30 state invalid disable", FIREWALL_NAME_CUST_PUB_TO_SL_PUB));				

		// allow outgoing ICMP traffic (rule 7000)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 7000 action accept", FIREWALL_NAME_CUST_PUB_TO_SL_PUB));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 7000 protocol icmp", FIREWALL_NAME_CUST_PUB_TO_SL_PUB));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 7000 icmp type-name any", FIREWALL_NAME_CUST_PUB_TO_SL_PUB));

		// create the firewall rules for incoming internet traffic to customer pub zone
		createDefaultFirewallRuleset(masterIp, vgw, FIREWALL_NAME_SL_PUB_TO_CUST_PUB);

		// allow all incoming traffic from SoftLayerPubAllowNets (rule 10)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 action accept", FIREWALL_NAME_SL_PUB_TO_CUST_PUB));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 protocol all", FIREWALL_NAME_SL_PUB_TO_CUST_PUB));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 destination group network-group %s", FIREWALL_NAME_SL_PUB_TO_CUST_PUB, FIREWALL_NETWORK_GROUP_SLPUBLICALLOWED));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 state new enable", FIREWALL_NAME_SL_PUB_TO_CUST_PUB));		
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 state established enable", FIREWALL_NAME_SL_PUB_TO_CUST_PUB));		
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 state related enable", FIREWALL_NAME_SL_PUB_TO_CUST_PUB));		
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 state invalid disable", FIREWALL_NAME_SL_PUB_TO_CUST_PUB));

		// allow outgoing VPN traffic (IKE) -- for remote tunnel initialization
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 action accept", FIREWALL_NAME_SL_PUB_TO_CUST_PUB));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 protocol udp", FIREWALL_NAME_SL_PUB_TO_CUST_PUB));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 destination group port-group %s", FIREWALL_NAME_SL_PUB_TO_CUST_PUB, FIREWALL_PORT_GROUP_VPN));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 state new enable", FIREWALL_NAME_SL_PUB_TO_CUST_PUB));		
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 state established enable", FIREWALL_NAME_SL_PUB_TO_CUST_PUB));		
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 state related enable", FIREWALL_NAME_SL_PUB_TO_CUST_PUB));		
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 state invalid disable", FIREWALL_NAME_SL_PUB_TO_CUST_PUB));				

		// allow outgoing ESP packets -- for encrypted VPN tunnel (rule 30)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 30 action accept", FIREWALL_NAME_SL_PUB_TO_CUST_PUB));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 30 protocol esp", FIREWALL_NAME_SL_PUB_TO_CUST_PUB));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 30 state new enable", FIREWALL_NAME_SL_PUB_TO_CUST_PUB));		
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 30 state established enable", FIREWALL_NAME_SL_PUB_TO_CUST_PUB));				
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 30 state related enable", FIREWALL_NAME_SL_PUB_TO_CUST_PUB));				
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 30 state invalid disable", FIREWALL_NAME_SL_PUB_TO_CUST_PUB));						

		// allow application port traffic for public internet access (rule 40)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 40 action accept", FIREWALL_NAME_SL_PUB_TO_CUST_PUB));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 40 protocol tcp", FIREWALL_NAME_SL_PUB_TO_CUST_PUB));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 40 destination group port-group %s", FIREWALL_NAME_SL_PUB_TO_CUST_PUB, FIREWALL_PORT_GROUP_PUB_APP));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 40 state new enable", FIREWALL_NAME_SL_PUB_TO_CUST_PUB));		
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 40 state established enable", FIREWALL_NAME_SL_PUB_TO_CUST_PUB));		
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 40 state related enable", FIREWALL_NAME_SL_PUB_TO_CUST_PUB));		
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 40 state invalid disable", FIREWALL_NAME_SL_PUB_TO_CUST_PUB));						

		// create the firewall rules for customer pri zones to local firewall
		createDefaultFirewallRuleset(masterIp, vgw, FIREWALL_NAME_CUST_PRIV_TO_LOCAL);

		// allow customer zone to ping the gateway IPs (rule 10)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 action accept", FIREWALL_NAME_CUST_PRIV_TO_LOCAL));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 protocol icmp", FIREWALL_NAME_CUST_PRIV_TO_LOCAL));

		// create the firewall rules for customer pub zones to local firewall
		createDefaultFirewallRuleset(masterIp, vgw, FIREWALL_NAME_CUST_PUB_TO_LOCAL);

		// allow customer zone to ping the gateway IPs (rule 10)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 action accept", FIREWALL_NAME_CUST_PUB_TO_LOCAL));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 protocol icmp", FIREWALL_NAME_CUST_PUB_TO_LOCAL));		

		// create the firewall rules for local firewall to customer pri zones
		createDefaultFirewallRuleset(masterIp, vgw, FIREWALL_NAME_LOCAL_TO_CUST_PRIV);

		// allow ping from gateway to customer zone (rule 10)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 action accept", FIREWALL_NAME_LOCAL_TO_CUST_PRIV));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 protocol icmp", FIREWALL_NAME_LOCAL_TO_CUST_PRIV));		


		// create the firewall rules for local firewall to customer pub zones
		createDefaultFirewallRuleset(masterIp, vgw, FIREWALL_NAME_LOCAL_TO_CUST_PUB);		

		// allow ping from gateway to customer zone (rule 10)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 action accept", FIREWALL_NAME_LOCAL_TO_CUST_PUB));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 protocol icmp", FIREWALL_NAME_LOCAL_TO_CUST_PUB));				

		// create the firewall rules for softlayer private network to local firewall
		createDefaultFirewallRuleset(masterIp, vgw, FIREWALL_NAME_SL_PRIV_TO_LOCAL);

		// allow new and established traffic from all softlayer allowed subnets (rule 10)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 action accept", FIREWALL_NAME_SL_PRIV_TO_LOCAL));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 protocol all", FIREWALL_NAME_SL_PRIV_TO_LOCAL));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 source group network-group %s", FIREWALL_NAME_SL_PRIV_TO_LOCAL, FIREWALL_NETWORK_GROUP_SLPRIVATEALLOWED));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 state new enable", FIREWALL_NAME_SL_PRIV_TO_LOCAL));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 state established enable", FIREWALL_NAME_SL_PRIV_TO_LOCAL));

		// allow new and established traffic for Vyatta REST API from mgmt subnets (rule 20)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 action accept", FIREWALL_NAME_SL_PRIV_TO_LOCAL));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 destination group port-group %s", FIREWALL_NAME_SL_PRIV_TO_LOCAL, FIREWALL_PORT_GROUP_REST_API));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 source group network-group %s", FIREWALL_NAME_SL_PRIV_TO_LOCAL, FIREWALL_NETWORK_GROUP_MGMT));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 protocol all", FIREWALL_NAME_SL_PRIV_TO_LOCAL));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 state new enable", FIREWALL_NAME_SL_PRIV_TO_LOCAL));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 state established enable", FIREWALL_NAME_SL_PRIV_TO_LOCAL));		

		// allow new and established traffic for local multicast (rule 30)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 30 action accept", FIREWALL_NAME_SL_PRIV_TO_LOCAL));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 30 destination group network-group %s", FIREWALL_NAME_SL_PRIV_TO_LOCAL, FIREWALL_NETWORK_GROUP_LOCAL_MULTICAST));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 30 protocol all", FIREWALL_NAME_SL_PRIV_TO_LOCAL));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 30 state new enable", FIREWALL_NAME_SL_PRIV_TO_LOCAL));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 30 state established enable", FIREWALL_NAME_SL_PRIV_TO_LOCAL));						

		// open config sync to the other member (rule 40)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 40 action accept", FIREWALL_NAME_SL_PRIV_TO_LOCAL));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 40 source group address-group %s", FIREWALL_NAME_LOCAL_TO_SL_PRIV, FIREWALL_ADDRESS_GROUP_GW_MEMBERS));			
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 40 destination group address-group %s", FIREWALL_NAME_LOCAL_TO_SL_PRIV, FIREWALL_ADDRESS_GROUP_GW_MEMBERS));			

		// create the firewall rules for softlayer private network to local firewall 
		createDefaultFirewallRuleset(masterIp, vgw, FIREWALL_NAME_LOCAL_TO_SL_PRIV);

		// open config sync to the other member (rule 10)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 action accept", FIREWALL_NAME_LOCAL_TO_SL_PRIV));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 source group address-group %s", FIREWALL_NAME_LOCAL_TO_SL_PRIV, FIREWALL_ADDRESS_GROUP_GW_MEMBERS));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 destination group address-group %s", FIREWALL_NAME_LOCAL_TO_SL_PRIV, FIREWALL_ADDRESS_GROUP_GW_MEMBERS));

		// allow new and established traffic for local multicast (rule 20)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 action accept", FIREWALL_NAME_LOCAL_TO_SL_PRIV));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 destination group network-group %s", FIREWALL_NAME_LOCAL_TO_SL_PRIV, FIREWALL_NETWORK_GROUP_LOCAL_MULTICAST));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 protocol all", FIREWALL_NAME_LOCAL_TO_SL_PRIV));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 state new enable", FIREWALL_NAME_LOCAL_TO_SL_PRIV));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 state established enable", FIREWALL_NAME_LOCAL_TO_SL_PRIV));				

		// create the firewall rules for public internet to local firewall
		createDefaultFirewallRuleset(masterIp, vgw, FIREWALL_NAME_SL_PUB_TO_LOCAL);
		
		// open ssh only for dedicated gateways
		if (gateway.getType() == GatewayType.DEDICATED) {
			vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 50 action accept", FIREWALL_NAME_SL_PUB_TO_LOCAL));
			vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 50 protocol tcp", FIREWALL_NAME_SL_PUB_TO_LOCAL));
			vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 50 destination group port-group %s", FIREWALL_NAME_SL_PUB_TO_LOCAL, FIREWALL_PORT_GROUP_SSH));
			vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 50 state new enable", FIREWALL_NAME_SL_PUB_TO_LOCAL));		
			vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 50 state established enable", FIREWALL_NAME_SL_PUB_TO_LOCAL));		
			vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 50 state related enable", FIREWALL_NAME_SL_PUB_TO_LOCAL));		
			vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 50 state invalid disable", FIREWALL_NAME_SL_PUB_TO_LOCAL));	
		}
		
		// allow all incoming traffic from SoftLayerPubAllowNets (rule 10)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 action accept", FIREWALL_NAME_SL_PUB_TO_LOCAL));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 protocol all", FIREWALL_NAME_SL_PUB_TO_LOCAL));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 source group network-group %s", FIREWALL_NAME_SL_PUB_TO_LOCAL, FIREWALL_NETWORK_GROUP_SLPUBLICALLOWED));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 state new enable", FIREWALL_NAME_SL_PUB_TO_LOCAL));		
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 state established enable", FIREWALL_NAME_SL_PUB_TO_LOCAL));		
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 state related enable", FIREWALL_NAME_SL_PUB_TO_LOCAL));		
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 state invalid disable", FIREWALL_NAME_SL_PUB_TO_LOCAL));

		// allow outgoing VPN traffic (IKE) -- for remote tunnel initialization (rule 20)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 action accept", FIREWALL_NAME_SL_PUB_TO_LOCAL));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 protocol udp", FIREWALL_NAME_SL_PUB_TO_LOCAL));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 destination group port-group %s", FIREWALL_NAME_SL_PUB_TO_LOCAL, FIREWALL_PORT_GROUP_VPN));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 state new enable", FIREWALL_NAME_SL_PUB_TO_LOCAL));		
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 state established enable", FIREWALL_NAME_SL_PUB_TO_LOCAL));		
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 state related enable", FIREWALL_NAME_SL_PUB_TO_LOCAL));		
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 state invalid disable", FIREWALL_NAME_SL_PUB_TO_LOCAL));				

		// allow outgoing ESP packets -- for encrypted VPN tunnel (rule 30)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 30 action accept", FIREWALL_NAME_SL_PUB_TO_LOCAL));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 30 protocol esp", FIREWALL_NAME_SL_PUB_TO_LOCAL));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 30 state new enable", FIREWALL_NAME_SL_PUB_TO_LOCAL));		
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 30 state established enable", FIREWALL_NAME_SL_PUB_TO_LOCAL));				
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 30 state related enable", FIREWALL_NAME_SL_PUB_TO_LOCAL));				
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 30 state invalid disable", FIREWALL_NAME_SL_PUB_TO_LOCAL));						

		// allow new and established traffic for local multicast (rule 40)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 40 action accept", FIREWALL_NAME_SL_PUB_TO_LOCAL));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 40 destination group network-group %s", FIREWALL_NAME_SL_PUB_TO_LOCAL, FIREWALL_NETWORK_GROUP_LOCAL_MULTICAST));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 40 protocol all", FIREWALL_NAME_SL_PUB_TO_LOCAL));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 40 state new enable", FIREWALL_NAME_SL_PUB_TO_LOCAL));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 40 state established enable", FIREWALL_NAME_SL_PUB_TO_LOCAL));						


		// create the firewall rules for public internet to local firewall
		createDefaultFirewallRuleset(masterIp, vgw, FIREWALL_NAME_LOCAL_TO_SL_PUB);

		// allow outgoing VPN traffic (IKE) -- for remote tunnel initialization (rule 20)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 action accept", FIREWALL_NAME_LOCAL_TO_SL_PUB));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 protocol udp", FIREWALL_NAME_LOCAL_TO_SL_PUB));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 destination group port-group %s", FIREWALL_NAME_LOCAL_TO_SL_PUB, FIREWALL_PORT_GROUP_VPN));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 state new enable", FIREWALL_NAME_LOCAL_TO_SL_PUB));		
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 state established enable", FIREWALL_NAME_LOCAL_TO_SL_PUB));		
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 state related enable", FIREWALL_NAME_LOCAL_TO_SL_PUB));		
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 20 state invalid disable", FIREWALL_NAME_LOCAL_TO_SL_PUB));				

		// allow outgoing ESP packets -- for encrypted VPN tunnel (rule 30)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 30 action accept", FIREWALL_NAME_LOCAL_TO_SL_PUB));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 30 protocol esp", FIREWALL_NAME_LOCAL_TO_SL_PUB));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 30 state new enable", FIREWALL_NAME_LOCAL_TO_SL_PUB));		
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 30 state established enable", FIREWALL_NAME_LOCAL_TO_SL_PUB));				
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 30 state related enable", FIREWALL_NAME_LOCAL_TO_SL_PUB));				
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 30 state invalid disable", FIREWALL_NAME_LOCAL_TO_SL_PUB));						

		// allow new and established traffic for local multicast (rule 40)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 40 action accept", FIREWALL_NAME_LOCAL_TO_SL_PUB));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 40 destination group network-group %s", FIREWALL_NAME_LOCAL_TO_SL_PUB, FIREWALL_NETWORK_GROUP_LOCAL_MULTICAST));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 40 protocol all", FIREWALL_NAME_LOCAL_TO_SL_PUB));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 40 state new enable", FIREWALL_NAME_LOCAL_TO_SL_PUB));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 40 state established enable", FIREWALL_NAME_LOCAL_TO_SL_PUB));						

		// create firewall rules for public internet to customer private zone (tunneled traffic only)
		createDefaultFirewallRuleset(masterIp, vgw, FIREWALL_NAME_SL_PUB_TO_CUST_PRIV);

		// allow tunneled traffic (rule 10)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 action accept", FIREWALL_NAME_SL_PUB_TO_CUST_PRIV));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 ipsec match-ipsec", FIREWALL_NAME_SL_PUB_TO_CUST_PRIV));

		// create firewall rules for customer private zone to public internet (tunneled traffic only)
		createDefaultFirewallRuleset(masterIp, vgw, FIREWALL_NAME_CUST_PRIV_TO_SL_PUB);

		// allow tunneled traffic (rule 10)
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 action accept", FIREWALL_NAME_CUST_PRIV_TO_SL_PUB));
		vgw.callConfigureCommand(masterIp, String.format("set firewall name %s rule 10 ipsec match-ipsec", FIREWALL_NAME_CUST_PRIV_TO_SL_PUB));

	}
	
	private static void createZonePolicy(Gateway gateway, VyattaAPIGateway vgw) throws CPEException {
		final String masterIp = gateway.getMasterGatewayMember().getMemberIp();

		// gather all public and private interfaces (including the Virtual gateway interface) to add to the default zones
		final String physPrivIntfName;
		final String physPubIntfName;
		
		final Set<String> privInterfaces = new HashSet<String>();
		final Set<String> pubInterfaces = new HashSet<String>();
		try {
			physPrivIntfName = vgw.getPrivateInterfaceName();
			physPubIntfName = vgw.getPublicInterfaceName();
		
			final String intfOutput = vgw.callOp(masterIp, "show interfaces");
			final String[] intfLines = intfOutput.split(System.getProperty("line.separator"));
			for (final String intfLine : intfLines) {
				// split the intf line
				final String[] intfLineTokens = intfLine.split("\\s+");
			
				if (intfLineTokens[0].startsWith(physPrivIntfName)) {
					privInterfaces.add(intfLineTokens[0]);
				} else if (intfLineTokens[0].startsWith(physPubIntfName)) {
					pubInterfaces.add(intfLineTokens[0]);
				}
			}
		} catch (CPEException e) {
			throw new CPEException(String.format("Failed to get public and private interfaces on gateway: %s", gateway.getName()));
		}
		
		
		// create local zone
		try {
			vgw.callConfigureCommand(masterIp, String.format("delete zone-policy zone %s", Zone.LOCAL));
		} catch (CPEException e) {
			// non-fatal error if we can't delete this, it may not exist
			logger.debug("Non-fatal error: " + e.getLocalizedMessage(), e);
		}
		
		vgw.callConfigureCommand(masterIp, String.format("set zone-policy zone %s default-action drop", Zone.LOCAL));
		vgw.callConfigureCommand(masterIp, String.format("set zone-policy zone %s local-zone", Zone.LOCAL));

		// create zone for SoftLayer private network with default zone drop
		vgw.callConfigureCommand(masterIp, String.format("delete zone-policy zone %s", Zone.SL_PRIVATE));
		vgw.callConfigureCommand(masterIp, String.format("set zone-policy zone %s default-action drop", Zone.SL_PRIVATE));
		for (final String privIntf : privInterfaces) {
			if (privIntf.matches(String.format("%s\\.[0-9]+", physPrivIntfName))) {
				// build the SL Private Zone first
				continue;
			} 

			// non-vif interface, add it to the main SL Private zone
			vgw.callConfigureCommand(masterIp, String.format("set zone-policy zone %s default-action drop", Zone.SL_PRIVATE));
			vgw.callConfigureCommand(masterIp, String.format("set zone-policy zone %s interface %s", Zone.SL_PRIVATE, privIntf));
		}

		// create zone for public internet with default zone drop
		try {
			vgw.callConfigureCommand(masterIp, String.format("delete zone-policy zone %s", Zone.SL_PUBLIC));
		} catch (CPEException e) {
			// non-fatal error if we can't delete this, it may not exist
			logger.debug("Non-fatal error: " + e.getLocalizedMessage(), e);	
		}
		vgw.callConfigureCommand(masterIp, String.format("set zone-policy zone %s default-action drop", Zone.SL_PUBLIC));
		for (final String pubIntf : pubInterfaces) {
			if (pubIntf.matches(String.format("%s\\.[0-9]+", physPubIntfName))) {
				// build the public zone first
				continue;
			}

			// non-vif interface, add it to the main SL public zone
			vgw.callConfigureCommand(masterIp, String.format("set zone-policy zone %s interface %s", Zone.SL_PUBLIC, pubIntf));
		}		

		// configure the zone policies for non-customer VIFs
		for (final Zone fromZone : Zone.values()) {
			for (final Zone toZone : Zone.values()) {
				if (FIREWALL_ZONE_POLICY[fromZone.ordinal()][toZone.ordinal()] == null) {
					continue;
				}

				if (fromZone == Zone.CUST_PRIV || fromZone == Zone.CUST_PUB) {
					// these are done separately as the zone names are dynamic, continue
					continue;
				}

				if (toZone == Zone.CUST_PRIV || toZone == Zone.CUST_PUB) {
					// these are done separately as the zone names are dynamic, continue
					continue;
				}				
				vgw.callConfigureCommand(masterIp, String.format("set zone-policy zone %s from %s firewall name %s", toZone, fromZone, FIREWALL_ZONE_POLICY[fromZone.ordinal()][toZone.ordinal()]));
			}
		}
	}
	

	/**
	 *  function to add default firewall rule 
	 **/
	@Override
	public void addDefaultFirewallRules(Gateway gateway) throws CPEException {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Creating default fire wall rules: gateway %s", gateway.getName()));
		}	
		
		// we are assuming the Gateway has just been OS reloaded
		final VyattaAPIGateway vgw = new VyattaAPIGateway(gateway);	
		
		// find out the master gateway's IP
		final String masterIp = gateway.getMasterGatewayMember().getMemberIp();
	
	
		// create the config-sync tree
		try {
			// start the session on all members
			vgw.startSessionAllMembers();
			
			createConfigSyncTree(gateway, vgw);
			vgw.commitChanges();

			try {
				// create the firewall groups
				createFirewallGroups(gateway, vgw);
				
				// create the firewall rules
				createFirewallRules(gateway, vgw);
			
				// create the zone policy
				createZonePolicy(gateway, vgw);
				
				// commit on the master, will sync over to the backup
				vgw.commitChanges(masterIp);
			} catch (CPEException e) {
				throw new CPEException(String.format("Error creating default firewall rules on gateway %s: %s", gateway.getName(), e.getLocalizedMessage()), e);
			}		

			try {
				// vif configuration must be created on all gateways
				// use the VLANs from softlayer API/database to create the VIFs instead of depending on the existing configuration
				final String privIntfName = vgw.getPrivateInterfaceName();
				final String pubIntfName = vgw.getPublicInterfaceName();
				
				// initially delete all vifs
				String interfaceType = null;
				if (privIntfName.startsWith("eth")) {
					interfaceType = "ethernet";
				} else if (privIntfName.startsWith("bond")) {
					interfaceType = "bonding";
				}
				
				if (pubIntfName.startsWith("eth")) {
					interfaceType = "ethernet";
				} else if (pubIntfName.startsWith("bond")) {
					interfaceType = "bonding";
				}
				try {
					vgw.callConfigureCommand("delete interfaces " + interfaceType + " " + pubIntfName + " vif");			
				} catch (CPEException e) {
					// non-fatal error if we can't delete this, it may not exist
					logger.debug("Error deleting interfaces: " + e.getLocalizedMessage(), e);
				}

				for (final SoftLayerIdObject idObj : gateway.getAssociatedVlans()) {
					final Vlan vlan = (Vlan)idObj;
					
					final String intf;
					if (vlan.getNetworkSpace() == VlanNetworkSpace.PRIVATE) {
						intf = privIntfName;
					} else {
						intf = pubIntfName;
					}
					
					addVifGateway(vgw, gateway, vlan, intf);

					// create zone policy
					createZonePolicyForVif(vgw, vlan.getNetworkSpace() == VlanNetworkSpace.PRIVATE ? Zone.CUST_PRIV :Zone.CUST_PUB, intf, vlan.getVlanNumber());
				}

				// TODO: recreate VPN tunnel configuration from the database if available
				
				// commit
				vgw.commitChanges();

				if (logger.isDebugEnabled()) {
					logger.debug(String.format("Created default firewall rules to gateway: %s", gateway.getName()));
				}	
			} catch (CPEException e) {
				throw new CPEException(String.format("Error creating default firewall rules on gateway %s: %s", gateway.getName(), e.getLocalizedMessage()), e);
			}
			
			vgw.saveChanges();
		} finally {
			vgw.destroySession();
		}
	}	
	
	/* (non-Javadoc)
	 * @see com.ibm.scas.analytics.backend.impl.FirewallService#allowManagementSubnets(java.util.Set)
	 */
	@Override
	public void allowManagementSubnets(final Gateway mgmtGateway, final Set<String> subnets) throws CPEException {
     	if (logger.isDebugEnabled()) {
    		logger.info(String.format("Attempting to add subnets %s to MANAGEMENT gateway %s firewall group %s", subnets, mgmtGateway.getName(), FIREWALL_NETWORK_GROUP_VYATTA_PRI_ALLOW_NETS));	
    	}   	
		try {
			allowSubnetsOnFirewallGroup(mgmtGateway, subnets, FIREWALL_NETWORK_GROUP_VYATTA_PRI_ALLOW_NETS);
		} catch (CPEException e) {
			new NagiosEventLogger().emitCritical("NAGIOSMONITOR VYATTACONFIG : "+e.getLocalizedMessage());
			throw e;
		}
		
		String emessage = String.format("Added subnets %s to MANAGEMENT gateway %s firewall group %s", subnets, mgmtGateway.getName(), FIREWALL_NETWORK_GROUP_VYATTA_PRI_ALLOW_NETS);
    	logger.info(emessage);
    	new NagiosEventLogger().emitOK("NAGIOSMONITOR VYATTACONFIG : "+emessage);
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.scas.analytics.backend.impl.FirewallService#allowCustomerSubnets(java.util.Set)
	 */
	@Override
	public void allowCustomerSubnets(final Gateway mgmtGateway, final Set<String> subnets) throws CPEException {
    	if (subnets.isEmpty()) {
    		// nothing to do
    		return;
    	}
    	
    	if (logger.isDebugEnabled()) {
    		logger.info(String.format("Attempting to add subnets %s to MANAGEMENT gateway %s (%s) firewall group %s", subnets, mgmtGateway.getName(), mgmtGateway.getId(), FIREWALL_NETWORK_GROUP_CUST_PRI_ALLOW_NETS));	
    	}
		try {
			allowSubnetsOnFirewallGroup(mgmtGateway, subnets, FIREWALL_NETWORK_GROUP_CUST_PRI_ALLOW_NETS);
		} catch (CPEException e) {
			new NagiosEventLogger().emitCritical("NAGIOSMONITOR VYATTACONFIG : "+e.getLocalizedMessage());
			throw e;			
		}
		
		String emessage = String.format("Added subnets %s to MANAGEMENT gateway %s (%s) firewall group %s", subnets, mgmtGateway.getName(), mgmtGateway.getId(), FIREWALL_NETWORK_GROUP_CUST_PRI_ALLOW_NETS);
    	logger.info(emessage);	
    	new NagiosEventLogger().emitOK("NAGIOSMONITOR VYATTACONFIG :"+emessage);
	}
	
	private static Set<String> getFirewallGroupSubnets(VyattaAPIGateway gw, String firewallGroup) throws CPEException{
		// caller should have already started a session
		final Set<String> subnetList = new HashSet<String>();
		
		boolean sessionStarted = gw.startSession();
		try {
			final JsonObject jsonObj = gw.getConfigurationPath(String.format("firewall group network-group %s network", firewallGroup));
			
			if (jsonObj == null) {
				return Collections.emptySet();
			}
			
			if (!jsonObj.has("network")) {
				return Collections.emptySet();
			}
			
			final JsonArray subnetArr = JsonUtil.getArrayFromPath(jsonObj, "network");
			for (final JsonElement subnetElem : subnetArr) {
				subnetList.add(subnetElem.getAsString());
			}
		
			return subnetList; 
		} finally {
			if (sessionStarted) {
				// this is more of query than an actual configure command, so destroy the session if it's not part
				// of a larger session
				gw.destroySession();
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.scas.analytics.backend.impl.FirewallService#removeMgmtSubnets(java.util.Set)
	 */
	@Override
	public void removeMgmtSubnets(final Gateway mgmtGateway, Set<String> subnetList) throws CPEException {
		if (subnetList.isEmpty()) {
    		// nothing to remove
    		return;
    	}	
		
		try {
			removeSubnetsFromFirewallGroup(mgmtGateway, subnetList, FIREWALL_NETWORK_GROUP_VYATTA_PRI_ALLOW_NETS);
		} catch (CPEException e) {
			new NagiosEventLogger().emitCritical("NAGIOSMONITOR VYATTACONFIG : "+e.getLocalizedMessage());
			throw e;
		}
		
		String emessage = String.format("Removed subnets %s from MANAGEMENT gateway %s (%s) firewall group %s", subnetList, mgmtGateway.getName(), mgmtGateway.getId(), FIREWALL_NETWORK_GROUP_VYATTA_PRI_ALLOW_NETS); 
    	logger.info(emessage);	
    	new NagiosEventLogger().emitOK("NAGIOSMONITOR VYATTACONFIG : "+emessage);
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.scas.analytics.backend.impl.FirewallService#removeCustomerSubnets(java.util.Set)
	 */
	@Override
	public void removeCustomerSubnets(final Gateway mgmtGateway, Set<String> subnetList) throws CPEException {
		if (subnetList.isEmpty()) {
    		// nothing to remove
    		return;
    	}	
		
		try {
			removeSubnetsFromFirewallGroup(mgmtGateway, subnetList, FIREWALL_NETWORK_GROUP_CUST_PRI_ALLOW_NETS);
		} catch (CPEException e) {
			new NagiosEventLogger().emitCritical("NAGIOSMONITOR VYATTACONFIG : "+e.getLocalizedMessage());
			throw e;
		}
		String emessage = String.format("Removed subnets %s from MANAGEMENT gateway %s (%s) firewall group %s", subnetList, mgmtGateway.getName(), mgmtGateway.getId(), FIREWALL_NETWORK_GROUP_CUST_PRI_ALLOW_NETS); 
    	logger.info(emessage);	
    	new NagiosEventLogger().emitOK("NAGIOSMONITOR VYATTACONFIG :"+emessage);
	}
	
	private static void addVifGateway(VyattaAPIGateway vgw, Gateway gw, Vlan vlan, String intf) throws CPEException {
		// TODO: verify session started on all members
		
		String interfaceType = null;
		if (intf.startsWith("eth")) {
			interfaceType = "ethernet";
		} else if (intf.startsWith("bond")) {
			interfaceType = "bonding";
		}

		if (interfaceType == null) {
			throw new CPEException(String.format("Unable to determine %s interface type of %s in Vyatta gateway", vlan.getNetworkSpace().name(), intf));
		}

		try {
			// first delete the entire vif as we will be regenerating it
			try {
				vgw.callConfigureCommand(String.format("delete interfaces %s %s vif %s", interfaceType, intf, vlan.getVlanNumber()));
			} catch (CPEException e) {
				// non-fatal error if we can't delete this, it may not exist
				logger.debug("Non-fatal error: " + e.getLocalizedMessage(), e);	
			}		

			// configure a local IP and priority on each member
			for (final GatewayMember member : gw.getGatewayMembers()) {
				// my local address is set to be 169.254.x.x/29,
				// which is derived using a combination of VLAN number, private/public interface, and whether this is the primary or
				// backup Vyatta				

				int tmp = Integer.valueOf(vlan.getVlanNumber());
				tmp = tmp << 4;
				tmp = tmp + (vlan.getNetworkSpace() == VlanNetworkSpace.PUBLIC ? 8 : 0);
				tmp = tmp + (255 - member.getPriority());

				final String myVIFLocalIP = "169.254." + (tmp / 256) + "." + (tmp % 256) + "/29";
				vgw.callConfigureCommand(member.getMemberIp(), 
						String.format("set interfaces %s %s vif %s address %s", 
								interfaceType, intf, vlan.getVlanNumber(), 
								myVIFLocalIP));
				vgw.callConfigureCommand(member.getMemberIp(),
						String.format("set interfaces %s %s vif %s vrrp vrrp-group %d priority %d", 
								interfaceType, intf, vlan.getVlanNumber(), 
								gw.getGroupNumber(), member.getPriority()));
			}

			// the remaining configuration is called on both
			vgw.callConfigureCommand(String.format(
					"set interfaces %s %s vif %s vrrp vrrp-group %d preempt true", 
					interfaceType, intf, vlan.getVlanNumber(), gw.getGroupNumber()));
			vgw.callConfigureCommand(String.format(
					"set interfaces %s %s vif %s vrrp vrrp-group %d preempt-delay 10", 
					interfaceType, intf, vlan.getVlanNumber(), gw.getGroupNumber()));
			vgw.callConfigureCommand(String.format(
					"set interfaces %s %s vif %s vrrp vrrp-group %d sync-group vgroup%d", 
					interfaceType, intf, vlan.getVlanNumber(), gw.getGroupNumber(), gw.getGroupNumber()));
			
			String vrrpRandom = EngineProperties.getInstance().getProperty(EngineProperties.FIREWALL_VYATTA_VRRP_AH_PASSWD_RANDOM_PREFIX);
			if (vrrpRandom == null) {
				vrrpRandom = EngineProperties.getInstance().getProperty(EngineProperties.FIREWALL_VYATTA_VRRP_AH_PASSWD_RANDOM_PREFIX + "." + gw.getId());
			}
			
			// must explicitly be set to "false"
			final boolean vrrpRandomize = (vrrpRandom == null) || vrrpRandom.equalsIgnoreCase("false");
			if (!vrrpRandomize) {
				logger.debug("Generating random VRRP AH password for gateway " + gw.getId() + " (" + gw.getName() + "), " + intf + "." + vlan.getVlanNumber());
				final String vrrpPassword = RandomStringUtils.randomAlphanumeric(8);

				vgw.callConfigureCommand(String.format(
					"set interfaces %s %s vif %s vrrp vrrp-group %d authentication password %s", 
					interfaceType, intf, vlan.getVlanNumber(), gw.getGroupNumber(), vrrpPassword));
			} else {
				String vrrpPassword = EngineProperties.getInstance().getProperty(EngineProperties.FIREWALL_VYATTA_VRRP_AH_PASSWD_PREFIX);
				if (vrrpPassword == null) {
					// see if there's AH password defined for local gateway
					vrrpPassword = EngineProperties.getInstance().getProperty(EngineProperties.FIREWALL_VYATTA_VRRP_AH_PASSWD_PREFIX + "." + gw.getId());
				}
				if (StringUtils.isBlank(vrrpPassword)) {
					logger.warn("No VRRP AH password is set in engine.properties for gateway " + gw.getId() + " (" + gw.getName() + ", using CPE primary member password for VRRP AH");
					// use the master gateway member's password as the authentication password for VRRP 
					vgw.callConfigureCommand(String.format(
						"set interfaces %s %s vif %s vrrp vrrp-group %d authentication password %s", 
						interfaceType, intf, vlan.getVlanNumber(), gw.getGroupNumber(),
						gw.getMasterGatewayMember().getPassword()));			
				} else {
					vgw.callConfigureCommand(String.format(
						"set interfaces %s %s vif %s vrrp vrrp-group %d authentication password %s", 
						interfaceType, intf, vlan.getVlanNumber(), gw.getGroupNumber(),
						new AES().decrypt(vrrpPassword)));						
				}
			}
				
			vgw.callConfigureCommand(String.format(
				"set interfaces %s %s vif %s vrrp vrrp-group %d authentication type ah", 
				interfaceType, intf, vlan.getVlanNumber(), gw.getGroupNumber()));


			for (final SoftLayerIdObject subnetObj : vlan.getSubnets()) {
				// configure for each subnet in the VLAN, on both of the members (is not synced over)
				final com.ibm.scas.analytics.beans.Subnet subnet = (com.ibm.scas.analytics.beans.Subnet)subnetObj;
				vgw.callConfigureCommand(String.format(
						"set interfaces %s %s vif %s vrrp vrrp-group %d virtual-address %s/%s", 
						interfaceType, intf, vlan.getVlanNumber(), gw.getGroupNumber(),
						subnet.getGatewayAddr(), subnet.getCidr()));
			}			
		} catch (GeneralSecurityException e) {
			throw new CPEException(String.format("Error decrypting password in engine.properties for VRRP on gateway %s (%s): %s", gw.getId(), gw.getName(), e.getLocalizedMessage()), e);
		} catch (CPEException e) {
			new NagiosEventLogger().emitCritical("NAGIOSMONITOR VYATTACONFIG : "+e.getLocalizedMessage());
			throw e;
		}

	}	
	
	private static String getInterfaceNameForVlan(VyattaAPIGateway vgw, Vlan vlan) throws CPEException {
		final String intf;
		if (vlan.getNetworkSpace() == VlanNetworkSpace.PRIVATE) {
			intf = vgw.getPrivateInterfaceName();
		} else {
			intf = vgw.getPublicInterfaceName();
		}
		
		return intf;
		
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.scas.analytics.backend.impl.FirewallService#addVifGateway(java.lang.String, int, java.lang.String, com.ibm.scas.analytics.beans.Vlan.VlanNetworkSpace)
	 */
	@Override
	public void addVifGateway(Gateway gateway, Vlan vlan) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("addVifGateway(): gatewayId: %s, vif %s, subnets %s, networkSpace %s", gateway.getId(), vlan.getVlanNumber(), vlan.getSubnets(), vlan.getNetworkSpace().toString()));
		}
		
		final VyattaAPIGateway vgw = new VyattaAPIGateway(gateway);
		final String intf = getInterfaceNameForVlan(vgw, vlan);
	
		try {
			vgw.startSessionAllMembers();
			
			// note that this hits both gateways in a Vyatta pair.  if one of the gateways
			// is down, it should throw the error and destroy the session.
			addVifGateway(vgw, gateway, vlan, intf);
			
			// apply the default firewall rules -- these should be synced
			createZonePolicyForVif(vgw, vlan.getNetworkSpace() == VlanNetworkSpace.PRIVATE ? Zone.CUST_PRIV :Zone.CUST_PUB, intf, vlan.getVlanNumber());
			
			vgw.commitChanges();
			vgw.saveChanges();
		} catch (CPEException e) {
			throw e;
		} finally {
			vgw.destroySession();
		}
	
		final String emessage = String.format("Added VIF %s interface %s on gateway %s", vlan.getVlanNumber(), vlan.getNetworkSpace().toString(), intf, gateway.getId());
		logger.info(emessage);	
		new NagiosEventLogger().emitOK("NAGIOSMONITOR VYATTACONFIG : "+emessage);	

	}
	
	/* (non-Javadoc)
	 * @see com.ibm.scas.analytics.backend.impl.FirewallService#removeVif(java.lang.String, int, com.ibm.scas.analytics.beans.Vlan.VlanNetworkSpace)
	 */
	@Override
	public void removeVif(final Gateway gateway, Vlan vlan) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("removeVif(): gateway %s, vif %d, networkSpace %s", gateway.getId(), vlan.getVlanNumber(), vlan.getNetworkSpace().toString()));
		}
	
		final VyattaAPIGateway vgw = new VyattaAPIGateway(gateway);
		vgw.startSessionAllMembers();
		try {
			// TODO: HA Vyatta
			
			// find the interface that has the private ip; this always matches  "currIP"
			final String intf;
			if (vlan.getNetworkSpace() == VlanNetworkSpace.PRIVATE) {
				 intf = vgw.getPrivateInterfaceName();
			} else {
				 intf = vgw.getPublicInterfaceName();
			}
			
			String interfaceType = null;
			if (intf.startsWith("eth")) {
				interfaceType = "ethernet";
			} else if (intf.startsWith("bond")) {
				interfaceType = "bonding";
			}
			
			if (interfaceType == null) {
				throw new CPEException(String.format("Unable to determine %s interface type of %s in Vyatta gateway", vlan.getNetworkSpace().name(), intf));
			}
		
			try {
				// delete the VIF interface
				vgw.callConfigureCommand(String.format("delete interfaces %s %s vif %s", interfaceType, intf, vlan.getVlanNumber()));
				
				// remove the zone policies 
				try {
					deleteZonePolicyForVif(vgw, vlan.getNetworkSpace() == VlanNetworkSpace.PRIVATE ? Zone.CUST_PRIV : Zone.CUST_PUB, String.valueOf(vlan.getVlanNumber()));
				} catch (CPEException e) {
					logger.warn(String.format("Error deleting zone policy from zone %s%s: %s", vlan.getNetworkSpace() == VlanNetworkSpace.PRIVATE ? Zone.CUST_PRIV : Zone.CUST_PUB, vlan.getVlanNumber(), e.getLocalizedMessage()));
				}
				
				vgw.commitChanges();
				vgw.saveChanges();
			} catch (CPEException e) {
				new NagiosEventLogger().emitCritical("NAGIOSMONITOR VYATTACONFIG : "+e.getLocalizedMessage());
				throw e;
			}
			
			String emessage = String.format("Removed VIF %s on %s interface %s on gateway %s (%s)", vlan.getVlanNumber(), vlan.getNetworkSpace().toString(), intf, gateway.getName(), gateway.getId()); 
	    	logger.info(emessage);	
	    	new NagiosEventLogger().emitOK("NAGIOSMONITOR VYATTACONFIG : "+emessage);
	    	
		} finally {
			vgw.destroySession();
		}		
	}
	
	private VPNTunnelConnectionStatus isTunnelConnected(com.ibm.scas.analytics.persistence.beans.VPNTunnel tunnelRec) throws CPEException {
		if (tunnelRec.getGateway() == null) {
			return VPNTunnelConnectionStatus.DISCONNECTED;
		}
		
		final Gateway gateway = networkService.getGatewayById(tunnelRec.getGateway().getId());
		final VyattaAPIGateway vgw = new VyattaAPIGateway(gateway);	
		final String peerIPAddr = tunnelRec.getCustIpAddr();
		final String ikeStatusStr = vgw.callOp(String.format("show vpn ike sa peer %s", peerIPAddr));

		final String[] ikeStatusStrLines = ikeStatusStr.split(System.getProperty("line.separator"));
		String ikeSAStatusStr = null;
		for (final String line : ikeStatusStrLines) {
			if (!line.startsWith(" ")) {
				continue;
			}
			final String[] lineSplit = line.split("\\s+");

			//System.out.println(lineSplit.length);

			if (lineSplit.length != 8) {
				continue;
			}

			if (lineSplit[1].startsWith("-")) {
				// dashes for row lines
				continue;
			}

			// ike status is like:
			// status encAlg hashAlg dhGroup NAT-T A-Time L-Time
			ikeSAStatusStr = lineSplit[1];

			if (ikeSAStatusStr == null || !ikeSAStatusStr.equalsIgnoreCase("up")) {
				return VPNTunnelConnectionStatus.DISCONNECTED;
			}

			// ike SA must be established for the tunnel to be considered up

			break;
		}


		final String vpnStatusStr = vgw.callOp(String.format("show vpn ipsec sa peer %s", peerIPAddr));
		final String[] vpnStatusStrLines = vpnStatusStr.split(System.getProperty("line.separator"));
		String ipsecSAStatusStr = null;
		for (final String line : vpnStatusStrLines) {
			if (!line.startsWith(" ")) {
				continue;
			}

			final String[] lineSplit = line.split("\\s+");

			if (lineSplit.length != 10) {
				continue;
			}

			if (lineSplit[1].equals("Tunnel")) {
				// header row
				continue;
			}
			if (lineSplit[1].startsWith("-")) {
				// dashes for row lines
				continue;
			}

			// vpn status is like:
			// tunnel state bytesOut/in encAlg hashAlg NAT-T A-Time L-Time Proto
			ipsecSAStatusStr = lineSplit[2];

			if (ipsecSAStatusStr == null || !ipsecSAStatusStr.equalsIgnoreCase("up")) {
				return VPNTunnelConnectionStatus.DISCONNECTED;
			}

			// all tunnels (subnet pairs) must be connected for the tunnel to be considered up
		}	
		
		return VPNTunnelConnectionStatus.CONNECTED;
	}
	
	private List<VPNTunnel> getVPNTunnelsFromRecords(List<com.ibm.scas.analytics.persistence.beans.VPNTunnel> tunnelRecs, boolean getStatus) throws CPEException {
		final List<VPNTunnel> toReturn = new ArrayList<VPNTunnel>(tunnelRecs.size());
		final Map<String, Gateway> gatewayMap = new HashMap<String, Gateway>();
		final Map<String, List<com.ibm.scas.analytics.beans.Subnet>> gatewaySubnetMap = new HashMap<String, List<com.ibm.scas.analytics.beans.Subnet>>();
		for (final com.ibm.scas.analytics.persistence.beans.VPNTunnel tunnel : tunnelRecs) {
			// fill this in later
			if (tunnel.getGateway() != null) {
				gatewayMap.put(tunnel.getGateway().getId(), null);
			}
		}
	
		// map gateways to get their endpoint IP addresses
		final Collection<Gateway> gateways = networkService.getGateways(gatewayMap.keySet());
		for (final Gateway gateway : gateways) {
			gatewayMap.put(gateway.getId(), gateway);
			
			// map subnets to get cloud subnet objects
			final List<com.ibm.scas.analytics.beans.Subnet> subnets  = networkService.getSubnets(gateway.getId());
			gatewaySubnetMap.put(gateway.getId(), subnets);
		}
		
		for (final com.ibm.scas.analytics.persistence.beans.VPNTunnel tunnel : tunnelRecs) {
			final VPNTunnel myTunnel = BeanConversionUtil.convertToBean(tunnel);
			if (getStatus) {
				try {
					// set the tunnel connection
					myTunnel.setStatus(this.isTunnelConnected(tunnel));
				} catch (CPEException e) {
					// VPN tunnels error, just log a warning, assume it's not connected, and continue
					logger.warn(String.format("Error getting VPN tunnel status for tunnel id %s", tunnel.getId(), e.getLocalizedMessage(), e));
					myTunnel.setStatus(VPNTunnelConnectionStatus.UNKNOWN);
				}
			} else {
				myTunnel.setStatus(VPNTunnelConnectionStatus.UNKNOWN);
			}
			
			if (tunnel.getGateway() != null) {
				myTunnel.setCloudIpAddr(gatewayMap.get(tunnel.getGateway().getId()).getPublicIpAddress());
			}
			
			final List<com.ibm.scas.analytics.beans.Subnet> subnetList;
			
			if (myTunnel.getGatewayId() != null) {
				subnetList = gatewaySubnetMap.get(myTunnel.getGatewayId());
			} else {
				subnetList = Collections.emptyList();
			}
			
			myTunnel.setCloudSubnets(subnetList);
			
			toReturn.add(myTunnel);
		}
		
		return toReturn;	
	}
	
	@Override
	public List<VPNTunnel> getVPNTunnels(String gatewayId) throws CPEException {
		final Gateway gateway = networkService.getGatewayById(gatewayId);
	
		// get all VPNTunnels associated with my Gateway
		final List<com.ibm.scas.analytics.persistence.beans.VPNTunnel> gwVpnTunnels = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.VPNTunnel.class, new WhereClause("gateway.id", gateway.getId()));
		return getVPNTunnelsFromRecords(gwVpnTunnels, true);
	}
	
	@Override
	public void configureTunnels(String gatewayId) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("configureTunnels(): gateway %s", gatewayId));
		}
		
		final Gateway gateway = networkService.getGatewayById(gatewayId); 
	
		final List<com.ibm.scas.analytics.persistence.beans.VPNTunnel> gwVpnTunnels = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.VPNTunnel.class, new WhereClause("gateway.id", gatewayId));
		final Set<String> tunnelIds = new HashSet<String>(gwVpnTunnels.size());
		for (final com.ibm.scas.analytics.persistence.beans.VPNTunnel vpnTunnel : gwVpnTunnels) {
			tunnelIds.add(vpnTunnel.getId());
		}
		
		if (logger.isDebugEnabled()) {
			if (tunnelIds.isEmpty()) {
				logger.debug(String.format("Removing all tunnels %s on gateway %s ...", tunnelIds, gatewayId));
			} else {
				logger.debug(String.format("Configuring tunnels %s on gateway %s ...", tunnelIds, gatewayId));
			}
		}
		
		final VyattaAPIGateway vgw = new VyattaAPIGateway(gateway);	
		boolean startedSession = false;
		try {	
			
			final String publicInterface = vgw.getPublicInterfaceName();
			String intf_type = null;
			if (publicInterface.startsWith("bond")) {
				intf_type = "bonding";
			} else if (publicInterface.startsWith("eth")) {
				intf_type = "ethernet";
			}
			
			// the tunnel should be created on the public virtual IP; in SoftLayer this is <interface>v<vrrp group id>
			
			String publicVIPIntf = null;
			final String publicInterfaceBrief = vgw.callOp(String.format("show interfaces %s", intf_type));
			String[] publicInterfaceBriefArr = publicInterfaceBrief.split(System.getProperty("line.separator"));
			String publicIpCidr = null;
			for (final String line : publicInterfaceBriefArr) {
				if (!line.startsWith(String.format("%sv", publicInterface))) {
					continue;
				}
				String[] interfaceLineSplit = line.split("\\s+");
				publicIpCidr = interfaceLineSplit[1];
				publicVIPIntf = interfaceLineSplit[0];

				break;
			}

			if (publicIpCidr == null) {
				throw new CPEException("Can't get public IP!");
			}
			
			final String publicIp = publicIpCidr.split("/")[0];
			// start session
			startedSession = vgw.startSession();
			
			// delete all tunnel settings to start from clean slate
			vgw.callConfigureCommand(String.format("delete vpn ipsec"));
			
			// ipsec interface
			if (publicVIPIntf != null) {
				vgw.callConfigureCommand(String.format("set vpn ipsec ipsec-interfaces interface %s", publicVIPIntf));
			} else {
				vgw.callConfigureCommand(String.format("set vpn ipsec ipsec-interfaces interface %s", publicInterface));
			}

			// configure all tunnels
			for (final com.ibm.scas.analytics.persistence.beans.VPNTunnel tunnel : gwVpnTunnels) {
				final EnumMap<VPNParamNames, String> vpnParams = new EnumMap<VPNParamNames, String>(VPNParamNames.class);
				for (final Entry<String, String> paramEnt : tunnel.getParams().entrySet()) {
					vpnParams.put(VPNParamNames.valueOf(paramEnt.getKey()), paramEnt.getValue());
				}
				
				// ike params
				vgw.callConfigureCommand(String.format("delete vpn ipsec ike-group %s ", tunnel.getId()));
				
				vgw.callConfigureCommand(String.format("set vpn ipsec ike-group %s lifetime %s", tunnel.getId(), vpnParams.get(VPNParamNames.IKE_KEY_LIFETIME)));
				final IPSecVPNDHGroup ikeDHGroup = IPSecVPNDHGroup.getEnum(vpnParams.get(VPNParamNames.IKE_DH_GROUP));
				vgw.callConfigureCommand(String.format("set vpn ipsec ike-group %s proposal 1 dh-group %s", tunnel.getId(), vyattaDHGroupTransMap.get(ikeDHGroup)));
				final IPSecVPNEncAlgorithms ikeEncAlg = IPSecVPNEncAlgorithms.getEnum(vpnParams.get(VPNParamNames.IKE_ENCRYPTION_ALG));
				vgw.callConfigureCommand(String.format("set vpn ipsec ike-group %s proposal 1 encryption %s", tunnel.getId(), vyattaEncAlgTransMap.get(ikeEncAlg)));
				final IPSecVPNHashAlgorithms ikeHashAlg = IPSecVPNHashAlgorithms.getEnum(vpnParams.get(VPNParamNames.IKE_HASH_ALG));
				vgw.callConfigureCommand(String.format("set vpn ipsec ike-group %s proposal 1 hash %s", tunnel.getId(), vyattaHashAlgTransMap.get(ikeHashAlg)));

				// esp params
				vgw.callConfigureCommand(String.format("delete vpn ipsec esp-group %s ", tunnel.getId()));
				
				vgw.callConfigureCommand(String.format("set vpn ipsec esp-group %s lifetime %s", tunnel.getId(), vpnParams.get(VPNParamNames.ESP_KEY_LIFETIME)));
				//vgw.callConfigureCommand(String.format("set vpn ipsec esp-group %s proposal 1 dh-group %s", tunnel.getId(), vpnParams.get("espDhGroup")));
				final IPSecVPNEncAlgorithms espEncAlg = IPSecVPNEncAlgorithms.getEnum(vpnParams.get(VPNParamNames.ESP_ENCRYPTION_ALG));
				vgw.callConfigureCommand(String.format("set vpn ipsec esp-group %s proposal 1 encryption %s", tunnel.getId(), vyattaEncAlgTransMap.get(espEncAlg)));
				final IPSecVPNHashAlgorithms espHashAlg = IPSecVPNHashAlgorithms.getEnum(vpnParams.get(VPNParamNames.ESP_HASH_ALG));
				vgw.callConfigureCommand(String.format("set vpn ipsec esp-group %s proposal 1 hash %s", tunnel.getId(), vyattaHashAlgTransMap.get(espHashAlg)));		
				final IPSecVPNESP_PFS espPFS = IPSecVPNESP_PFS.getEnum(vpnParams.get(VPNParamNames.ESP_PFS));
				vgw.callConfigureCommand(String.format("set vpn ipsec esp-group %s pfs %s", tunnel.getId(), vyattaESP_PFSTransMap.get(espPFS)));	
				

				// delete peer tunnel
				vgw.callConfigureCommand(String.format("delete vpn ipsec site-to-site peer %s ", tunnel.getCustIpAddr()));

				// peer
				vgw.callConfigureCommand(String.format("set vpn ipsec site-to-site peer %s local-address %s", tunnel.getCustIpAddr(), publicIp));
				vgw.callConfigureCommand(String.format("set vpn ipsec site-to-site peer %s default-esp-group %s", tunnel.getCustIpAddr(), tunnel.getId()));
				vgw.callConfigureCommand(String.format("set vpn ipsec site-to-site peer %s ike-group %s", tunnel.getCustIpAddr(), tunnel.getId()));

				// authentication mode
				final IPSecVPNAuthModes authMode = IPSecVPNAuthModes.getEnum(vpnParams.get(VPNParamNames.AUTHENTICATION_MODE));
				vgw.callConfigureCommand(String.format("set vpn ipsec site-to-site peer %s authentication mode %s", tunnel.getCustIpAddr(), vyattaAuthModeTransMap.get(authMode)));
				if (authMode == IPSecVPNAuthModes.PRE_SHARED_KEY) {
					vgw.callConfigureCommand(String.format("set vpn ipsec site-to-site peer %s authentication pre-shared-secret %s", tunnel.getCustIpAddr(), vpnParams.get(VPNParamNames.PRE_SHARED_SECRET)));
				}

				// tunnels
				final String[] remoteSubnets = vpnParams.get(VPNParamNames.CUST_SUBNETS).split(";");

				int tunnelIdx = 1;
				
				// get the actual subnet definitions
				final List<com.ibm.scas.analytics.beans.Subnet> localSubnets = this.networkService.getSubnets(gatewayId);
				for (final com.ibm.scas.analytics.beans.Subnet localSubnet : localSubnets) {
					final String localSubnetCidr = String.format("%s/%d", localSubnet.getNetworkAddr(), localSubnet.getCidr());
					for (final String remoteSubnet : remoteSubnets) {
						vgw.callConfigureCommand(String.format("set vpn ipsec site-to-site peer %s tunnel %d local prefix %s", tunnel.getCustIpAddr(), tunnelIdx, localSubnetCidr));
						vgw.callConfigureCommand(String.format("set vpn ipsec site-to-site peer %s tunnel %d remote prefix %s", tunnel.getCustIpAddr(), tunnelIdx, remoteSubnet));
						vgw.callConfigureCommand(String.format("set vpn ipsec site-to-site peer %s tunnel %d protocol all", tunnel.getCustIpAddr(), tunnelIdx));
						tunnelIdx++;
					}
				}
			}

			if (startedSession) {
				/* on the commit, all old tunnels will be removed and all new tunnels will be added
				 * a tunnel parameter is the same, then no changes.
				 */
				vgw.commitChanges();
				vgw.saveChanges();
			}		
			
			if (tunnelIds.isEmpty()) {
				logger.info(String.format("Removed all tunnels %s on gateway %s ...", tunnelIds, gatewayId));
			} else {
				logger.info(String.format("Configured tunnels %s on gateway %s ...", tunnelIds, gatewayId));
			}
		} finally {
			if (startedSession) {
				vgw.destroySession();
			}
		}		
	}

	@Override
	public void validateVPNTunnel(VPNTunnel vpnTunnel) throws CPEException {
		if (vpnTunnel == null) {
			// nothing to validate
			return;
		}
		
		if (vpnTunnel.getCustIpAddr() == null) {
			throw new CPEParamException("Invalid or missing params: vpnTunnel.custIpAddr");
		}
		
		if (vpnTunnel.getParams() == null) {
			throw new CPEParamException("Invalid or missing params: vpnTunnel.params");
		}
		
		// make sure all params are declared
		for (final VPNParamNames paramName : VPNParamNames.values()) {
			if (vpnTunnel.getParams().get(paramName) != null) {
				continue;
			}
			
			String defaultParam;
			if ((defaultParam = VPNTunnel.DEFAULT_IPSEC_PARAMS.get(paramName)) != null) {
				// check if there's a default value
				logger.warn(String.format("Value for %s was not set, setting to default value: %s", paramName, defaultParam));
				vpnTunnel.getParams().put(paramName, defaultParam);
				continue;
			}
			
			// not set and no default value
			throw new CPEParamException(String.format("VPN Tunnel param %s was not defined!", paramName));
		}
		
		// validate IKE encryption algorithm is one of the valid ones
		final String ikeEncryptionString = vpnTunnel.getParams().get(VPNParamNames.IKE_ENCRYPTION_ALG);
		try {
			final IPSecVPNEncAlgorithms ikeEncAlg = IPSecVPNEncAlgorithms.getEnum(ikeEncryptionString);
			if (ikeEncAlg == null) {
				throw new CPEParamException(String.format("Invalid value of %s: %s.  Valid values are: %s", VPNParamNames.IKE_ENCRYPTION_ALG.name(), vpnTunnel.getParams().get(VPNParamNames.IKE_ENCRYPTION_ALG), IPSecVPNEncAlgorithms.values()));
			}
		} catch (IllegalArgumentException e) {
			throw new CPEParamException(String.format("Invalid value of %s: %s.  Valid values are: %s", VPNParamNames.IKE_ENCRYPTION_ALG.name(), vpnTunnel.getParams().get(VPNParamNames.IKE_ENCRYPTION_ALG), IPSecVPNEncAlgorithms.values()));
		}
		
		// validate IKE hash algorithm is one of the valid ones
		final String ikeHashString = vpnTunnel.getParams().get(VPNParamNames.IKE_HASH_ALG);
		try {
			final IPSecVPNHashAlgorithms ikeHashAlg = IPSecVPNHashAlgorithms.getEnum(ikeHashString);
			if (ikeHashAlg == null) {
				throw new CPEParamException(String.format("Invalid value of %s: %s.  Valid values are: %s", VPNParamNames.IKE_HASH_ALG.name(), vpnTunnel.getParams().get(VPNParamNames.IKE_HASH_ALG), IPSecVPNHashAlgorithms.values()));
			}	
		} catch (IllegalArgumentException e) {
			throw new CPEParamException(String.format("Invalid value of %s: %s.  Valid values are: %s", VPNParamNames.IKE_HASH_ALG.name(), vpnTunnel.getParams().get(VPNParamNames.IKE_HASH_ALG), IPSecVPNHashAlgorithms.values()));
		}
		
		// validate ESP encryption algorithm is one of the valid ones
		final String espEncryptionString = vpnTunnel.getParams().get(VPNParamNames.ESP_ENCRYPTION_ALG);
		try {
			final IPSecVPNEncAlgorithms espEncAlg = IPSecVPNEncAlgorithms.getEnum(espEncryptionString);
			if (espEncAlg == null) {
				throw new CPEParamException(String.format("Invalid value of %s: %s.  Valid values are: %s", VPNParamNames.ESP_ENCRYPTION_ALG.name(), vpnTunnel.getParams().get(VPNParamNames.ESP_ENCRYPTION_ALG), IPSecVPNEncAlgorithms.values()));
			}
		} catch (IllegalArgumentException e) {
			throw new CPEParamException(String.format("Invalid value of %s: %s.  Valid values are: %s", VPNParamNames.ESP_ENCRYPTION_ALG.name(), vpnTunnel.getParams().get(VPNParamNames.ESP_ENCRYPTION_ALG), IPSecVPNEncAlgorithms.values()));
		}
		
		// validate ESP hash algorithm is one of the valid ones
		final String espHashString = vpnTunnel.getParams().get(VPNParamNames.ESP_HASH_ALG);
		try {
		final IPSecVPNHashAlgorithms espHashAlg = IPSecVPNHashAlgorithms.getEnum(espHashString);
		if (espHashAlg == null) {
			throw new CPEParamException(String.format("Invalid value of %s: %s.  Valid values are: %s", VPNParamNames.ESP_HASH_ALG.name(), vpnTunnel.getParams().get(VPNParamNames.ESP_HASH_ALG), IPSecVPNHashAlgorithms.values()));
		}		
		} catch (IllegalArgumentException e) {
			throw new CPEParamException(String.format("Invalid value of %s: %s.  Valid values are: %s", VPNParamNames.ESP_HASH_ALG.name(), vpnTunnel.getParams().get(VPNParamNames.ESP_HASH_ALG), IPSecVPNHashAlgorithms.values()));
			
		}
		
		// validate authentication method is one of the valid ones
		final String authMethodString = vpnTunnel.getParams().get(VPNParamNames.AUTHENTICATION_MODE);
		try {
			final IPSecVPNAuthModes authMode = IPSecVPNAuthModes.getEnum(authMethodString);
			if (authMode == null) {
				throw new CPEParamException(String.format("Invalid value of %s: %s.  Valid values are: %s", VPNParamNames.AUTHENTICATION_MODE.name(), vpnTunnel.getParams().get(VPNParamNames.AUTHENTICATION_MODE), IPSecVPNAuthModes.values()));
			}		
		} catch (IllegalArgumentException e) {
			throw new CPEParamException(String.format("Invalid value of %s: %s.  Valid values are: %s", VPNParamNames.AUTHENTICATION_MODE.name(), vpnTunnel.getParams().get(VPNParamNames.AUTHENTICATION_MODE), IPSecVPNAuthModes.values()));
		}
	
		// validate subnet format is one of the valid ones
		final String subnetCidrs = vpnTunnel.getParams().get(VPNParamNames.CUST_SUBNETS);
		final String[] subnetArr = subnetCidrs.split(";");
		
		for (final String subnetCidr : subnetArr) {
			// TODO: validation that this is a real subnet definition: each octet should be > 0 and < 256
			if (!subnetCidr.matches("[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+\\/[0-9]+")) {
				throw new CPEParamException(String.format("Invalid value of %s: %s.  Valid format is: x.x.x.x/yy[;x.x.x.x/yy;...]", VPNParamNames.CUST_SUBNETS.name(), vpnTunnel.getParams().get(VPNParamNames.CUST_SUBNETS)));
			}
			
			// TODO: for now we do not support overlapping subnets with 10.0.0.0/8
		}
	}

	@Override
	public void removeTunnel(String tunnelId) throws CPEException {
		final com.ibm.scas.analytics.persistence.beans.VPNTunnel tunnelRec = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.VPNTunnel.class, tunnelId);
		
		if (tunnelRec == null) {
			throw new CPEException(String.format("Cannot find tunnel with id: %s", tunnelId));
		}
		
		persistence.deleteObject(com.ibm.scas.analytics.persistence.beans.VPNTunnel.class, tunnelRec);
		
		// push the change to the database
		persistence.flush();
		
		// find my gateway
		final Gateway gateway = networkService.getGatewayById(tunnelRec.getGateway().getId());
		if (gateway == null) {
			// no gateway is associated, we can just get rid of the tunnel record and return
			return;
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Removing tunnel %s from gateway %s (%s) ... Old tunnel record: %s", tunnelId, gateway.getName(), gateway.getId(), ReflectionToStringBuilder.toString(tunnelRec)));
		}	
	
		// reconfigure the remaining tunnels on the gateway
		this.configureTunnels(gateway.getId());
		
		logger.info(String.format("Removed tunnel %s (peer IP address: %s) from gateway %s (%s).  ", tunnelId, tunnelRec.getCustIpAddr(), gateway.getName(), gateway.getId()));
	}

	@Override
	public com.ibm.scas.analytics.beans.VPNTunnel getVPNTunnel(String tunnelId, boolean getStatus) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace("getVPNTunnel(): " + tunnelId);
		}

		final com.ibm.scas.analytics.persistence.beans.VPNTunnel tunnelRec = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.VPNTunnel.class,tunnelId);
		if (tunnelRec == null) {
			return null;
		}
		
		return getVPNTunnelsFromRecords(Arrays.asList(tunnelRec), getStatus).get(0);
	}


	@Override
	public void addVPNTunnelToGateway(String gatewayId, VPNTunnel tunnel) throws CPEException {
		final com.ibm.scas.analytics.persistence.beans.Gateway gatewayRec = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Gateway.class, gatewayId);
		
		if (gatewayRec == null) {
			throw new CPEParamException(String.format("Gateway %s doesn't exist in the database", gatewayId));
		}

		this.validateVPNTunnel(tunnel);
		
		final List<com.ibm.scas.analytics.persistence.beans.VPNTunnel> tunnelRecs = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.VPNTunnel.class, new WhereClause("gateway.id", gatewayId));
		
		if (tunnelRecs.size() > 0) {
			throw new CPEParamException(String.format("Gateway %s already has a VPN Tunnel configured.", gatewayId));
		}
		
		final com.ibm.scas.analytics.persistence.beans.VPNTunnel tunnelRec = new com.ibm.scas.analytics.persistence.beans.VPNTunnel();
		// set all the values that can be set from caller side
		tunnelRec.setCustIpAddr(tunnel.getCustIpAddr());
		final Map<VPNParamNames, String> params = tunnel.getParams();
		final Map<String, String> dbParams = new HashMap<String, String>();
		for (final Entry<VPNParamNames, String> ent : params.entrySet()) {
			dbParams.put(ent.getKey().name(), ent.getValue());
		}
		tunnelRec.setParams(dbParams);
		tunnelRec.setGateway(gatewayRec);
		
		persistence.saveObject(com.ibm.scas.analytics.persistence.beans.VPNTunnel.class, tunnelRec);
		
		this.configureTunnels(gatewayId);
	}
	
	@Override
	public void updateVPNTunnel(String tunnelId, VPNTunnel vpnTunnel) throws CPEException {
		final com.ibm.scas.analytics.persistence.beans.VPNTunnel tunnelRec = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.VPNTunnel.class, tunnelId);
		if (tunnelRec == null) {
			throw new CPEException(String.format("Cannot find tunnel with id: %s", tunnelId));
		}
		
		this.validateVPNTunnel(vpnTunnel);
		
		// set all the values that can be set from caller side
		tunnelRec.setCustIpAddr(vpnTunnel.getCustIpAddr());
		final Map<VPNParamNames, String> params = vpnTunnel.getParams();
		final Map<String, String> dbParams = new HashMap<String, String>();
		for (final Entry<VPNParamNames, String> ent : params.entrySet()) {
			dbParams.put(ent.getKey().name(), ent.getValue());
		}
		tunnelRec.setParams(dbParams);
	
		persistence.updateObject(com.ibm.scas.analytics.persistence.beans.VPNTunnel.class, tunnelRec);

		// push the change to the database
		persistence.flush();
		
		this.configureTunnels(tunnelRec.getGateway().getId());
	}

	@Override
	public String getSSLCertificate(String memberPrivateIp) throws CPEException {
		return VyattaAPIGateway.getSSLCertificate(memberPrivateIp);
	}
}
