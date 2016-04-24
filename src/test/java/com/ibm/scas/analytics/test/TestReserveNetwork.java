package com.ibm.scas.analytics.test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.ibm.scas.analytics.backend.NetworkService;
import com.ibm.scas.analytics.backend.ProvisioningService;
import com.ibm.scas.analytics.backend.SoftLayerAPIGateway;
import com.ibm.scas.analytics.backend.SoftLayerAccountProvider;
import com.ibm.scas.analytics.beans.Gateway;
import com.ibm.scas.analytics.beans.Gateway.GatewayType;
import com.ibm.scas.analytics.beans.IPAddress;
import com.ibm.scas.analytics.beans.SoftLayerAccount;
import com.ibm.scas.analytics.beans.Vlan;
import com.ibm.scas.analytics.persistence.beans.Cluster;
import com.ibm.scas.analytics.persistence.beans.Subscriber;
import com.ibm.scas.analytics.utils.CPEException;
import com.ibm.scas.analytics.utils.JsonUtil;
import com.ibm.scas.analytics.utils.SoftLayerObjectFilter;

public class TestReserveNetwork extends BaseTestCase {
	private NetworkService networkProvider;
	private ProvisioningService provisionService;
	
	protected String slAcctId;
	protected String gatewayId;
	protected String vlanId;
	
	private static final Logger logger = Logger.getLogger(TestReserveNetwork.class);
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		
		final SoftLayerAccountProvider acctProvider = injector.getInstance(SoftLayerAccountProvider.class);
		
		// add my SL credentials
		final SoftLayerAccount slAccount = new SoftLayerAccount();
		slAccount.setUrl(SoftLayerAPIGateway.PUBLIC_REST_API_URL);
		slAccount.setUsername("ldubey.3@us.ibm.com");
		slAccount.setApiKey("b5e5d43e39099f08347bf12b3e68da7671345741ca6aee2b779d760d126ff9c0");
		
		slAcctId = acctProvider.createSoftLayerAccount(slAccount);
		networkProvider = injector.getInstance(NetworkService.class);
		provisionService = injector.getInstance(ProvisioningService.class);
		
		super.addDummyAccount();
	}
	
	@Override
	public void tearDown() throws Exception {
		final SoftLayerAccountProvider acctProvider = injector.getInstance(SoftLayerAccountProvider.class);
		
		acctProvider.deleteSoftLayerAccount(slAcctId);
		
		super.tearDown();
	}
	
	protected boolean addGateway(GatewayType type) throws CPEException {
		service.beginTransaction();
		
		final SoftLayerAccountProvider acctProvider = injector.getInstance(SoftLayerAccountProvider.class);
		final SoftLayerAccount slAcct = acctProvider.getSoftLayerAccountById(slAcctId);
		final SoftLayerAPIGateway sgw = new SoftLayerAPIGateway(slAcct);
		// find a gateway first -- with available VLANs
		final JsonElement gwArrElem = sgw.callGetMethod("SoftLayer_Account", "getNetworkGateways", null, 
				"id", 
				"name", 
				"privateVlan.primaryRouter.id");
		
		final JsonArray gwArr = gwArrElem.getAsJsonArray();
		if (gwArr.size() == 0) {
			logger.info("Skipping test, there are no gateways in this account");
			return false;
		}
		
		int slGatewayId = -1;
		String gatewayName = null;
		JsonElement vlanElem = null;
		final Set<String> slSubnetIds = new HashSet<String>();
		for (final JsonElement gwElem : gwArr) {
			final int backendRouter = JsonUtil.getIntFromPath(gwElem, "privateVlan.primaryRouter.id");
			final SoftLayerObjectFilter objFilter = new SoftLayerObjectFilter();
			
			objFilter.setPropertyFilter("networkVlans.attachedNetworkGatewayFlag", false);
			final JsonElement vlanArrElem = sgw.callGetMethod("SoftLayer_Account", "getNetworkVlans", objFilter, 
				"id", 
				"primaryRouter.id", 
				"vlanNumber", 
				"attachedNetworkGateway",
				"subnets.id",
				"subnets.subnetType");
		
			final JsonArray vlanArr = vlanArrElem.getAsJsonArray();
			if (vlanArr.size() == 0) {
				// no vlans eligible for this gateway
				continue;
			}
			

			for (final JsonElement vlanObj : vlanArr) {
				slSubnetIds.clear();
				final String attachedGatewayId = JsonUtil.getStringFromPath(vlanObj, "attachedNetworkGateway.id");
				if (attachedGatewayId != null) {
					continue;
				}
				
				final int primaryRouter = JsonUtil.getIntFromPath(vlanObj, "primaryRouter.id");
				if (primaryRouter != backendRouter) {
					continue;
				}

				final JsonArray subnetArr = JsonUtil.getArrayFromPath(vlanObj, "subnets");
				if (subnetArr == null || subnetArr.size() == 0) {
					// no subnets?
					continue;
				}

				for (final JsonElement subnetElem : subnetArr) {
					final String subnetId = JsonUtil.getStringFromPath(subnetElem, "id");
					final String subnetType = JsonUtil.getStringFromPath(subnetElem, "subnetType");
					// must have at least one SECONDARY_ON_VLAN subnet
					if (!subnetType.equals("SECONDARY_ON_VLAN")) {
						continue;
					}
					slSubnetIds.add(subnetId);
				}

				if (slSubnetIds.isEmpty()) {
					continue;
				}

				vlanElem = vlanObj;
				break;
			}
			
			if (vlanElem == null) {
				// this gateway cannot be used; no vlans
				continue;
			}
			
			
			// valid vlan is in vlanElem, grab the gateway ID and use it
			slGatewayId = JsonUtil.getIntFromPath(gwElem, "id");
			gatewayName = JsonUtil.getStringFromPath(gwElem, "name");
			break;
		}
		
		if (slGatewayId == -1 || vlanElem == null) {
			logger.info("No free VLANs exist; exit");
			return false;
		}
		
		logger.info("Adding gateway with sl ID: " + gatewayId + ", name: " + gatewayName);
		
		final Gateway gatewayReq = new Gateway();
		final SoftLayerAccount newSlAcct = new SoftLayerAccount();
		newSlAcct.setId(slAcctId);
		
		gatewayReq.setSoftLayerAccount(newSlAcct);
		gatewayReq.setName(gatewayName);
		gatewayReq.setType(type);
		gatewayId = networkProvider.addGateway(gatewayReq);
		
		logger.info("New shared gateway record: " + gatewayId);
		final com.ibm.scas.analytics.beans.Gateway gw = networkProvider.getGatewayById(gatewayId);
		logger.info(ReflectionToStringBuilder.toString(gw));
		

		service.commitTransaction();
		
		return true;
	}
	
	protected boolean addPrivateVlan() throws CPEException {
		final SoftLayerAccountProvider acctProvider = injector.getInstance(SoftLayerAccountProvider.class);
		final SoftLayerAccount slAcct = acctProvider.getSoftLayerAccountById(slAcctId);
		final SoftLayerAPIGateway sgw = new SoftLayerAPIGateway(slAcct);
		
		// if a gateway is already provisioned, add the vlan that is behind this router
		Gateway gw = null;
		if (gatewayId != null) {
			gw = networkProvider.getGatewayById(gatewayId);
		}
		
		String backendRouter = null;
		if (gw != null) {
			backendRouter = gw.getPrimaryBackendRouter();
		}
			
		final SoftLayerObjectFilter objFilter = new SoftLayerObjectFilter();
		objFilter.setPropertyFilter("networkVlans.attachedNetworkGatewayFlag", false);
		final JsonElement vlanArrElem = sgw.callGetMethod("SoftLayer_Account", "getNetworkVlans", objFilter, 
				"id", 
				"primaryRouter.hostname", 
				"vlanNumber", 
				"attachedNetworkGateway",
				"subnets.id",
				"subnets.subnetType");
		
		final JsonArray vlanArr = vlanArrElem.getAsJsonArray();
		if (vlanArr.size() == 0) {
			// no vlans eligible for this gateway
			return false;
		}
		
		final Set<String> slSubnetIds = new HashSet<String>();
		JsonElement vlanElem = null;
		for (final JsonElement vlanObj : vlanArr) {
			final String attachedGatewayId = JsonUtil.getStringFromPath(vlanObj, "attachedNetworkGateway.id");
			if (attachedGatewayId != null) {
				continue;
			}

			final String primaryRouter = JsonUtil.getStringFromPath(vlanObj, "primaryRouter.hostname");
			if (backendRouter != null && !primaryRouter.equals(backendRouter)) {
				continue;
			}

			final JsonArray subnetArr = JsonUtil.getArrayFromPath(vlanObj, "subnets");
			if (subnetArr == null || subnetArr.size() == 0) {
				// no subnets?
				continue;
			}

			for (final JsonElement subnetElem : subnetArr) {
				final String subnetId = JsonUtil.getStringFromPath(subnetElem, "id");
				final String subnetType = JsonUtil.getStringFromPath(subnetElem, "subnetType");
				// must have at least one SECONDARY_ON_VLAN subnet
				if (!subnetType.equals("SECONDARY_ON_VLAN")) {
					continue;
				}
				slSubnetIds.add(subnetId);
			}

			if (slSubnetIds.isEmpty()) {
				continue;
			}

			vlanElem = vlanObj;
			break;
		}
			
		if (vlanElem == null) {
			// this gateway cannot be used; no vlans
			return false;
		}

		service.beginTransaction();
		final com.ibm.scas.analytics.beans.Vlan vlanReq = new com.ibm.scas.analytics.beans.Vlan();
		vlanReq.setSoftLayerAccount(slAcct);
		vlanReq.setSoftLayerId(String.valueOf(JsonUtil.getIntFromPath(vlanElem, "id")));
	
		vlanId = networkProvider.addVlan(vlanReq);	
		service.commitTransaction();
		
		return true;
	}
	
	public void testDeleteAccountUnassignGateway() throws Exception {
		logger.info("****** testDeleteAccountUnassignGateway()");

		if (!this.addGateway(GatewayType.DEDICATED)) {
			logger.info("No eligible gateways in this account, return");
			return;
		}

		if (!this.addPrivateVlan()) {
			logger.info("No eligible Vlans in this account, return");
			return;
		}

		final com.ibm.scas.analytics.beans.Vlan vlan = networkProvider.getVlanById(vlanId);
		logger.info(ReflectionToStringBuilder.toString(vlan));

		// now delete subscriber
		final com.ibm.scas.analytics.beans.Account acc = tenantService.getAccountById(dummyAccountId);

		logger.info(String.format("Delete account: %s", ReflectionToStringBuilder.toString(acc)));

		service.beginTransaction();
		tenantService.deleteAccount(dummyAccountId);
		service.commitTransaction();

		// unassigning the gateway should unassign and unassociate all vlans
		final Collection<Gateway> accGateways = networkProvider.getAccountGateways(dummyAccountId);
		assertTrue(String.format("Account still has gateway! %s", accGateways.toString()), accGateways.isEmpty());
		// TODO  the below step is done asynchronously
		//assertTrue(String.format("Gateway %s still has VLANs associated! %s", reservedGatewayId, unassignedGW.getAssociatedVlans()), unassignedGW.getAssociatedVlans().size() == 0);
	}
	
	public void testReserveVlan() throws Exception {
		logger.info("****** testReserveVlan()");
		
		if (!this.addGateway(GatewayType.SHARED)) {
			logger.info("No eligible gateways in this account, return");
			return;
		}
		
		if (!this.addPrivateVlan()) {
			logger.info("No eligible Vlans in this account, return");
			return;
		}
			
		service.beginTransaction();
		logger.info("New vlan record: " + vlanId);
		
		final com.ibm.scas.analytics.beans.Vlan vlan = networkProvider.getVlanById(vlanId);
		logger.info(ReflectionToStringBuilder.toString(vlan));
		
		//assertTrue("Gateway name doesn't match", gateway.getName().equals(gatewayName));
		

		service.beginTransaction();
		logger.info("Creating a cluster ");
		final Subscriber subscriber = service.getObjectById(Subscriber.class, dummySubscriberId);
		final Cluster clusterRec = new Cluster();
		clusterRec.setName("abcd");
		clusterRec.setOwner(subscriber);
		clusterRec.setSize(2);
		clusterRec.setCurrentStep(com.ibm.scas.analytics.beans.Cluster.ClusterStep.RESERVE_NETWORK.name());
		service.saveObject(Cluster.class, clusterRec);
		service.commitTransaction();
		
		service.beginTransaction();
		logger.info("Reserving a vlan for cluster " + clusterRec.getId() + " on gateway " + gatewayId);
		networkProvider.reserveVlan(clusterRec.getId(), gatewayId);
		service.commitTransaction();
		
		final Vlan newVlan = networkProvider.getVlanById(vlanId);
		assertTrue(String.format("Vlan %s was not reserved", newVlan.getId()), newVlan.getCluster().getId().equals(clusterRec.getId()));
		// TODO: The below step is done asynchronously
		//assertTrue(String.format("Vlan %s was not assigned to gateway %s", newVlan.getId(), gatewayId), newVlan.getGateway().getId().equals(gatewayId));
		
		logger.info(String.format("Vlan after reserve: %s", ReflectionToStringBuilder.toString(newVlan )));
	
		service.beginTransaction();
		logger.info(String.format("Unassigning Vlan %s", vlanId));
		try {
			networkProvider.unassignVlan(vlanId, false);
			assertTrue(String.format("Expected exception, unassign a vlan when it is assigned to a cluster"), true);
		} catch (CPEException e) {
			logger.info(String.format("Expected exception: " + e.getLocalizedMessage(), false));
			
		}
		service.commitTransaction();
		
		service.beginTransaction();
		logger.info(String.format("Unassigning Vlan %s", vlanId));
		networkProvider.unassignVlan(vlanId, true);
		service.commitTransaction();
	
		final Vlan unassignedVlan = networkProvider.getVlanById(vlanId);
		assertTrue(String.format("Vlan %s is still assigned! %s", vlanId, ReflectionToStringBuilder.toString(unassignedVlan)), unassignedVlan.getCluster() == null);
		// TODO The below step is done asynchronously
		//assertTrue(String.format("Vlan %s is still associated to gateway! %s", vlanId, ReflectionToStringBuilder.toString(unassignedVlan)), unassignedVlan.getGateway() == null);

	}
	
	public void testReserveIpsNonExistentCluster() throws Exception {
		logger.info("****** testReserveIpsNonExistentCluster()");
		
		if (!this.addGateway(GatewayType.SHARED)) {
			logger.info("No eligible gateways in this account, return");
			return;
		}
		
		if (!this.addPrivateVlan()) {
			logger.info("No eligible Vlans in this account, return");
			return;
		}
			
		logger.info("New vlan record: " + vlanId);
		
		final Vlan vlan = networkProvider.getVlanById(vlanId);
		logger.info("New vlan object: " + ReflectionToStringBuilder.toString(vlan));
		
		service.beginTransaction();
		try {
			networkProvider.reserveIPAddressesForClusterTier(vlanId, 1, UUID.randomUUID().toString(), "Master");
			assertTrue("Able to reserve IPs for non-existent cluster", true);
			service.commitTransaction();
		} catch (CPEException e) {
			logger.info(String.format("Caught exception when reserving IPs for non-existent cluster: %s", e.getLocalizedMessage()));
		}

	}
	
	public void testReserveIpsUnassignedVlan() throws Exception {
		logger.info("****** testReserveIpsUnassignedVlan()");

		if (!this.addGateway(GatewayType.SHARED)) {
			logger.info("No eligible gateways in this account, return");
			return;
		}
		
		if (!this.addPrivateVlan()) {
			logger.info("No eligible Vlans in this account, return");
			return;
		}
		
		logger.info("New vlan record: " + vlanId);
		
		final Vlan vlan = networkProvider.getVlanById(vlanId);
		logger.info("New vlan object: " + ReflectionToStringBuilder.toString(vlan));
		
		try {
			service.beginTransaction();
	
			logger.info(String.format("reserve 1 IP for Master and 2 for ComputeNodes before vlan is assigned"));		
			// Create a cluster record
			final com.ibm.scas.analytics.persistence.beans.Subscriber subscriberRec = service.getObjectById(com.ibm.scas.analytics.persistence.beans.Subscriber.class, dummySubscriberId);
			final Cluster cluster = new Cluster();
			cluster.setOwner(subscriberRec);
			cluster.setName("myName");
			cluster.setSize(3);
			cluster.setState(0);
			cluster.setLaunchTime(Calendar.getInstance().getTimeInMillis());
			cluster.setLaunchTime(Calendar.getInstance().getTimeInMillis() + 1000000);
		
			service.saveObject(Cluster.class, cluster);
			final String clusterId = cluster.getId();
			
			networkProvider.reserveIPAddressesForClusterTier(vlanId, 1, clusterId, "Master");
			assertTrue("Non-owner was able to reserve IPs on unassigned VLAN", true);
			service.commitTransaction();
		} catch (CPEException e) {
			logger.info(String.format("Caught exception: %s", e.getLocalizedMessage()));
		}
	}
	
	public void testReserveIps() throws Exception {
		logger.info("****** testReserveIps()");
		
		if (!this.addGateway(GatewayType.SHARED)) {
			logger.info("No eligible gateways in this account, return");
			return;
		}
		
		if (!this.addPrivateVlan()) {
			logger.info("No eligible Vlans in this account, return");
			return;
		}
			
		final com.ibm.scas.analytics.beans.Vlan vlan = networkProvider.getVlanById(vlanId);
		logger.info(ReflectionToStringBuilder.toString(vlan));
		
		service.beginTransaction();
		logger.info("Creating a cluster ");
		final Subscriber subscriber = service.getObjectById(Subscriber.class, dummySubscriberId);
		final Cluster clusterRec = new Cluster();
		clusterRec.setName("abcd");
		clusterRec.setOwner(subscriber);
		clusterRec.setSize(2);
		clusterRec.setCurrentStep(com.ibm.scas.analytics.beans.Cluster.ClusterStep.RESERVE_NETWORK.name());
		clusterRec.setState(0);
		clusterRec.setLaunchTime(Calendar.getInstance().getTimeInMillis());
		clusterRec.setLaunchTime(Calendar.getInstance().getTimeInMillis() + 1000000);
		service.saveObject(Cluster.class, clusterRec);
		service.commitTransaction();
		
		
		service.beginTransaction();
		networkProvider.reserveVlan(clusterRec.getId(), gatewayId);
		service.commitTransaction();
		
		final Vlan newVlan = networkProvider.getVlanById(vlanId);
		assertTrue(String.format("Vlan %s was not reserved", newVlan.getId()), newVlan.getCluster().getId().equals(clusterRec.getId()));
		// TODO: The below step is done asynchronously
		//assertTrue(String.format("Vlan %s was not assigned to gateway %s", newVlan.getId(), gatewayId), newVlan.getGateway().getId().equals(gatewayId));
		
		logger.info(String.format("Vlan after reserve: %s", ReflectionToStringBuilder.toString(newVlan )));
		
		service.beginTransaction();

		String clusterId = clusterRec.getId();
		logger.info(String.format("reserve 1 IP for Master and 2 for ComputeNodes"));		
		networkProvider.reserveIPAddressesForClusterTier(vlanId, 1, clusterId, "Master");
		networkProvider.reserveIPAddressesForClusterTier(vlanId, 2, clusterId, "ComputeNodes");
		
		service.commitTransaction();
		
		// get the IP addresses by cluster id/tier
		List<IPAddress> ipAddresses = networkProvider.getIPAddressByCluster(clusterId, null);
		logger.info(String.format("Reserved: %s", new GsonBuilder().setPrettyPrinting().create().toJson(ipAddresses)));
		assertTrue(String.format("Found %d, not 3 total ip addresses reserved", ipAddresses.size()), ipAddresses.size() == 3);
		
		ipAddresses = networkProvider.getIPAddressByCluster(clusterId, "Master");
		logger.info(String.format("Reserved for Master: %s", new GsonBuilder().setPrettyPrinting().create().toJson(ipAddresses)));
		assertTrue(String.format("Found %d, not 1 ip addresses reserved for Master tier", ipAddresses.size()), ipAddresses.size() == 1);
		ipAddresses = networkProvider.getIPAddressByCluster(clusterId, "ComputeNodes");
		logger.info(String.format("Reserved for ComputeNodes: %s", new GsonBuilder().setPrettyPrinting().create().toJson(ipAddresses)));
		assertTrue(String.format("Found %d, not 2 ip addresses reserved for ComputeNodes tier", ipAddresses.size()), ipAddresses.size() == 2);	

		// simulate flex up by reserving one more IP for the computenodes tier
		service.beginTransaction();
		logger.info(String.format("reserve 1 more IP for ComputeNodes"));
		networkProvider.reserveIPAddressesForClusterTier(vlanId, 1, clusterId, "ComputeNodes");
		service.commitTransaction();
		
		ipAddresses = networkProvider.getIPAddressByCluster(clusterId, "ComputeNodes");
		logger.info(String.format("Reserved for ComputeNodes: %s", new GsonBuilder().setPrettyPrinting().create().toJson(ipAddresses)));
		assertTrue(String.format("Found %d, Not 3 ip addresses reserved for ComputeNodes tier", ipAddresses.size()), ipAddresses.size() == 3);
		
		// simulate flex down by removing one of the IP addresses
		service.beginTransaction();
		String ipAddrToUnreserve = ipAddresses.get(0).getId();
		String oldIPAddr = ipAddresses.get(0).getIpAddress();
		logger.info(String.format("Unreserve IP: %s (%s)for ComputeNodes", ipAddrToUnreserve, oldIPAddr));
		networkProvider.unreserveIPAddress(Arrays.asList(ipAddrToUnreserve));
		service.commitTransaction();
		
		ipAddresses = networkProvider.getIPAddressByCluster(clusterId, "ComputeNodes");
		logger.info(String.format("Reserved for ComputeNodes: %s", new GsonBuilder().setPrettyPrinting().create().toJson(ipAddresses)));
		assertTrue(String.format("Found %d reserved for compute nodes, (%s), not 2 ip addresses reserved for ComputeNodes tier", ipAddresses.size(), ipAddresses), ipAddresses.size() == 2);
		
		logger.info(String.format("get IP address: %s, vlan %s", oldIPAddr, vlanId));
		final IPAddress ipAddress = networkProvider.getIPAddress(oldIPAddr, vlanId);
		assertTrue(String.format("IP address %s is not found!", oldIPAddr), ipAddress != null);
		logger.info(String.format("after unreserve, IP address: %s", ReflectionToStringBuilder.toString(ipAddress)));
		assertTrue(String.format("IP address %s is still reserved to cluster", ipAddress.getIpAddress()), ipAddress.getCluster() == null);
		
		service.beginTransaction();
		oldIPAddr = ipAddresses.get(0).getIpAddress();
		logger.info(String.format("Unreserve IP by IP address string and Vlan: %s", oldIPAddr));
		networkProvider.unreserveIPAddressByAddress(Arrays.asList(oldIPAddr), vlanId);
		service.commitTransaction();
		
		ipAddresses = networkProvider.getIPAddressByCluster(clusterId, "ComputeNodes");
		assertTrue(String.format("Found %d reserved for ComputeNodes (%s), not 1 ip addresses reserved for ComputeNodes tier", ipAddresses.size(), ipAddresses), ipAddresses.size() == 1);
		logger.info(String.format("IP Addresses reserved on ComputeNodes tier: %s ", ipAddresses));

		final IPAddress ipAddress2 = networkProvider.getIPAddress(oldIPAddr, vlanId);
		assertTrue(String.format("IP address %s is not found!", oldIPAddr), ipAddress2 != null);
		logger.info(String.format("after unreserve, IP address: %s", ReflectionToStringBuilder.toString(ipAddress)));
		assertTrue(String.format("IP address %s is still reserved to cluster", ipAddress.getIpAddress()), ipAddress.getCluster() == null);
		
		// unreserve all
		service.beginTransaction();
		networkProvider.unreserveIPAddressByCluster(clusterId);
		service.commitTransaction();
		
		ipAddresses = networkProvider.getIPAddressByCluster(clusterId, null);
		assertTrue(String.format("IP Addresses still reserved for cluster %s (%s)", clusterId, ipAddresses), ipAddresses.size() == 0);
		
	}
}
