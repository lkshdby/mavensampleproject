package com.ibm.scas.analytics.test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.ibm.scas.analytics.backend.NetworkService;
import com.ibm.scas.analytics.backend.SoftLayerAPIGateway;
import com.ibm.scas.analytics.backend.SoftLayerAccountProvider;
import com.ibm.scas.analytics.backend.impl.SoftLayerNetworkService;
import com.ibm.scas.analytics.beans.SoftLayerAccount;
import com.ibm.scas.analytics.beans.Subnet;
import com.ibm.scas.analytics.beans.Vlan;
import com.ibm.scas.analytics.persistence.beans.Cluster;
import com.ibm.scas.analytics.persistence.beans.Subscriber;
import com.ibm.scas.analytics.utils.CPEException;
import com.ibm.scas.analytics.utils.JsonUtil;
import com.ibm.scas.analytics.utils.SoftLayerObjectFilter;

public class TestVlanService extends BaseTestCase {
	private String slAcctId;
	private NetworkService networkProvider;
	
	private static final Logger logger = Logger.getLogger(TestVlanService.class);
	
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
		networkProvider = injector.getInstance(SoftLayerNetworkService.class);
	}
	
	@Override
	public void tearDown() throws Exception {
		final SoftLayerAccountProvider acctProvider = injector.getInstance(SoftLayerAccountProvider.class);
		
		acctProvider.deleteSoftLayerAccount(slAcctId);
		
		super.tearDown();
	}
	
	public void testAddBadVlanByVlanNumber() throws Exception {
		logger.info("****** testAddBadVlanByVlanNumber()");
		/** test adding a Vlan that doesn't exist in softlayer **/
		final SoftLayerAccountProvider acctProvider = injector.getInstance(SoftLayerAccountProvider.class);
		final SoftLayerAccount slAcct = acctProvider.getSoftLayerAccountById(slAcctId);
		
		final com.ibm.scas.analytics.beans.Vlan vlanReq = new com.ibm.scas.analytics.beans.Vlan();
		vlanReq.setSoftLayerAccount(slAcct);
		vlanReq.setPrimaryRouter("someBogusString");
		vlanReq.setVlanNumber("9999");
	
		try {
			networkProvider.addVlan(vlanReq);
			assertTrue("VLAN that doesn't exist in SoftLayer added to CPE DB", true);
		} catch (CPEException e) {
			logger.info("Caught exception when adding bad VLAN to CPE: " + e.getLocalizedMessage());
		}
	}
	
	public void testAddBadVlanBySLId() throws Exception {
		logger.info("****** testAddBadVlanBySLId()");
		/** test adding a Vlan that doesn't exist in softlayer **/
		final SoftLayerAccountProvider acctProvider = injector.getInstance(SoftLayerAccountProvider.class);
		final SoftLayerAccount slAcct = acctProvider.getSoftLayerAccountById(slAcctId);
		
		final com.ibm.scas.analytics.beans.Vlan vlanReq = new com.ibm.scas.analytics.beans.Vlan();
		vlanReq.setSoftLayerAccount(slAcct);
		vlanReq.setSoftLayerId("9999999999");
	
		try {
			networkProvider.addVlan(vlanReq);
			assertTrue("VLAN that doesn't exist in SoftLayer added to CPE DB", true);
		} catch (CPEException e) {
			logger.info("Caught exception when adding bad VLAN to CPE: " + e.getLocalizedMessage());
		}
	}
	
	public void testAddVlanByVlanNumber() throws Exception {
		logger.info("****** testAddVlanByVlanNumber()");
		/** test adding a floating Vlan **/
		final SoftLayerAccountProvider acctProvider = injector.getInstance(SoftLayerAccountProvider.class);
		final SoftLayerAccount slAcct = acctProvider.getSoftLayerAccountById(slAcctId);
		final SoftLayerAPIGateway sgw = new SoftLayerAPIGateway(slAcct);
		final SoftLayerObjectFilter objFilter = new SoftLayerObjectFilter();
		objFilter.setPropertyFilter("networkVlans.attachedNetworkGatewayFlag", false);
		
		final JsonElement elem = sgw.callGetMethod("SoftLayer_Account", "getNetworkVlans", objFilter, 
				"id", 
				"primaryRouter.hostname", 
				"vlanNumber", 
				"subnets",
				"attachedNetworkGateway.id");
		
		final JsonArray arr = elem.getAsJsonArray();
		if (arr.size() == 0) {
			logger.info("Skipping test, there are no vlans in this account");
			return;
		}
		
		JsonElement vlanElem = null;
		final Set<String> slSubnetIds = new HashSet<String>();
		for (final JsonElement vlanObj : arr) {
			final String gatewayId = JsonUtil.getStringFromPath(vlanObj, "attachedNetworkGateway.id");
			if (gatewayId != null) {
				continue;
			}
			
			final JsonArray subnetArr = JsonUtil.getArrayFromPath(vlanObj, "subnets");
			if (subnetArr.size() == 0) {
				// no subnets
				continue;
			}

			for (final JsonElement subnetElem : subnetArr) {
				final String subnetId = JsonUtil.getStringFromPath(subnetElem, "id");
				slSubnetIds.add(subnetId);
			}

			vlanElem = vlanObj;
		}
		
		if (vlanElem == null) {
			logger.info("No free VLANs exist; exit");
			return;
		}
		
		logger.info(String.format("Adding vlan %s.%d", JsonUtil.getStringFromPath(vlanElem, "primaryRouter.hostname"), JsonUtil.getIntFromPath(vlanElem, "vlanNumber")));
		final com.ibm.scas.analytics.beans.Vlan vlanReq = new com.ibm.scas.analytics.beans.Vlan();
		vlanReq.setSoftLayerAccount(slAcct);
		vlanReq.setPrimaryRouter(JsonUtil.getStringFromPath(vlanElem, "primaryRouter.hostname"));
		vlanReq.setVlanNumber(String.valueOf(JsonUtil.getIntFromPath(vlanElem, "vlanNumber")));
	
		service.beginTransaction();
		final String newVlanId = networkProvider.addVlan(vlanReq);
		service.commitTransaction();
		
		logger.info("New vlan record: " + newVlanId);
		
		final com.ibm.scas.analytics.beans.Vlan vlan = networkProvider.getVlanById(newVlanId);
		logger.info(ReflectionToStringBuilder.toString(vlan));
		
		//assertTrue("Gateway name doesn't match", gateway.getName().equals(gatewayName));
		assertTrue("Vlan id doesn't match", vlan.getSoftLayerId().equals(String.valueOf(JsonUtil.getIntFromPath(vlanElem, "id"))));
		
		// make sure subnets are added
		final List<Subnet> subnets = networkProvider.getAllSubnets();
		for (final Subnet subnet : subnets) {
			assertTrue(slSubnetIds.contains(subnet.getSoftLayerId()));
		}
		
		service.beginTransaction();
		networkProvider.deleteVlan(newVlanId);
		service.commitTransaction();
		
		final Vlan oldVlan = networkProvider.getVlanById(newVlanId);
		assertTrue(oldVlan == null);
	
		final List<Subnet> newSubnets = networkProvider.getAllSubnets();
		for (final Subnet subnet : newSubnets) {
			assertTrue(!slSubnetIds.contains(subnet.getSoftLayerId()));
		}
		
	}
	
	public void testAddVlanWithGateway() throws Exception {
		logger.info("****** testAddVlanWithGateway()");
		/** test adding a Vlan already associated with a gateway **/
		final SoftLayerAccountProvider acctProvider = injector.getInstance(SoftLayerAccountProvider.class);
		final SoftLayerAccount slAcct = acctProvider.getSoftLayerAccountById(slAcctId);
		final SoftLayerAPIGateway sgw = new SoftLayerAPIGateway(slAcct);
		
		final SoftLayerObjectFilter objFilter = new SoftLayerObjectFilter();
		objFilter.setPropertyFilter("networkVlans.attachedNetworkGatewayFlag", false);
		
		final JsonElement elem = sgw.callGetMethod("SoftLayer_Account", "getNetworkVlans", objFilter, 
				"id", 
				"primaryRouter.hostname", 
				"vlanNumber", 
				"subnets",
				"attachedNetworkGateway.id");
		
		final JsonArray arr = elem.getAsJsonArray();
		if (arr.size() == 0) {
			logger.info("Skipping test, there are no vlans in this account");
			return;
		}
		
		JsonElement vlanElem = null;
		final Set<String> slSubnetIds = new HashSet<String>();
		for (final JsonElement vlanObj : arr) {
			final String gatewayId = JsonUtil.getStringFromPath(vlanObj, "attachedNetworkGateway.id");
			if (gatewayId == null) {
				continue;
			}
			
			vlanElem = vlanObj;
		}
		
		if (vlanElem == null) {
			logger.info("No attached VLANs exist; exit");
			return;
		}
		
		final com.ibm.scas.analytics.beans.Vlan vlanReq = new com.ibm.scas.analytics.beans.Vlan();
		vlanReq.setSoftLayerAccount(slAcct);
		vlanReq.setPrimaryRouter(JsonUtil.getStringFromPath(vlanElem, "primaryRouter.hostname"));
		vlanReq.setVlanNumber(String.valueOf(JsonUtil.getIntFromPath(vlanElem, "vlanNumber")));
	
		try {
			final String newVlanId = networkProvider.addVlan(vlanReq);
		
		    assertTrue("Added an already attached VLAN", false);
		} catch (CPEException e) {
			logger.info("can't add vlan: " + e.getLocalizedMessage());
			
		}
	}
	
	public void testAddVlanBySLId() throws Exception {
		logger.info("****** testAddVlanBySLId()");
		/** test adding a floating Vlan **/
		final SoftLayerAccountProvider acctProvider = injector.getInstance(SoftLayerAccountProvider.class);
		final SoftLayerAccount slAcct = acctProvider.getSoftLayerAccountById(slAcctId);
		final SoftLayerAPIGateway sgw = new SoftLayerAPIGateway(slAcct);
		
		final SoftLayerObjectFilter objFilter = new SoftLayerObjectFilter();
		objFilter.setPropertyFilter("networkVlans.attachedNetworkGatewayFlag", false);
		
		final JsonElement elem = sgw.callGetMethod("SoftLayer_Account", "getNetworkVlans", objFilter, 
				"id", 
				"primaryRouter.hostname", 
				"vlanNumber", 
				"attachedNetworkGateway.id",
				"subnets");
		
		final JsonArray arr = elem.getAsJsonArray();
		if (arr.size() == 0) {
			logger.info("Skipping test, there are no vlans in this account");
			return;
		}
		
		JsonElement vlanElem = null;
		final Set<String> slSubnetIds = new HashSet<String>();
		for (final JsonElement vlanObj : arr) {
			final String gatewayId = JsonUtil.getStringFromPath(vlanObj, "attachedNetworkGateway.id");
			if (gatewayId != null) {
				continue;
			}
			
			final JsonArray subnetArr = JsonUtil.getArrayFromPath(vlanObj, "subnets");
			if (subnetArr == null || subnetArr.size() == 0) {
				// no subnets
				continue;
			}

			for (final JsonElement subnetElem : subnetArr) {
				final String subnetId = JsonUtil.getStringFromPath(subnetElem, "id");
				slSubnetIds.add(subnetId);
			}

			vlanElem = vlanObj;
		}
		
		if (vlanElem == null) {
			logger.info("No free VLANs exist; exit");
			return;
		}
		
		service.beginTransaction();
		final com.ibm.scas.analytics.beans.Vlan vlanReq = new com.ibm.scas.analytics.beans.Vlan();
		vlanReq.setSoftLayerAccount(slAcct);
		vlanReq.setSoftLayerId(String.valueOf(JsonUtil.getIntFromPath(vlanElem, "id")));
	
		logger.info(String.format("Adding vlan %d", JsonUtil.getIntFromPath(vlanElem, "id")));
		final String newVlanId = networkProvider.addVlan(vlanReq);
		service.commitTransaction();
		
		logger.info("New vlan record: " + newVlanId);
		
		final com.ibm.scas.analytics.beans.Vlan vlan = networkProvider.getVlanById(newVlanId);
		logger.info(ReflectionToStringBuilder.toString(vlan));
		
		//assertTrue("Gateway name doesn't match", gateway.getName().equals(gatewayName));
		assertTrue("Vlan id doesn't match", vlan.getSoftLayerId().equals(String.valueOf(JsonUtil.getIntFromPath(vlanElem, "id"))));
		
		// make sure subnets are added
		final List<Subnet> subnets = networkProvider.getAllSubnets();
		for (final Subnet subnet : subnets) {
			assertTrue(slSubnetIds.contains(subnet.getSoftLayerId()));
		}
		
		service.beginTransaction();
		networkProvider.deleteVlan(newVlanId);
		service.commitTransaction();
		
		final Vlan oldVlan = networkProvider.getVlanById(newVlanId);
		assertTrue(oldVlan == null);
	
		final List<Subnet> newSubnets = networkProvider.getAllSubnets();
		for (final Subnet subnet : newSubnets) {
			assertTrue(!slSubnetIds.contains(subnet.getSoftLayerId()));
		}
	}
	
	
	public void testModifyVlanSubscriber() throws Exception {
		logger.info("****** testModifyVlanSubscriber()");
		
		service.beginTransaction();
		final Subscriber subscriber = new Subscriber();
		subscriber.setName("me");
		subscriber.setApiKey(UUID.randomUUID().toString().replace("-", ""));
		subscriber.setExternalId("externalId");
		
		service.saveObject(Subscriber.class, subscriber);
		service.commitTransaction();
		
		final SoftLayerAccountProvider acctProvider = injector.getInstance(SoftLayerAccountProvider.class);
		final SoftLayerAccount slAcct = acctProvider.getSoftLayerAccountById(slAcctId);
		final SoftLayerAPIGateway sgw = new SoftLayerAPIGateway(slAcct);
		
		final SoftLayerObjectFilter objFilter = new SoftLayerObjectFilter();
		objFilter.setPropertyFilter("networkVlans.attachedNetworkGatewayFlag", false);
		
		final JsonElement elem = sgw.callGetMethod("SoftLayer_Account", "getNetworkVlans", objFilter, 
				"id", 
				"primaryRouter.hostname", 
				"vlanNumber", 
				"attachedNetworkGateway.id",
				"subnets");
		
		final JsonArray arr = elem.getAsJsonArray();
		if (arr.size() == 0) {
			logger.info("Skipping test, there are no vlans in this account");
			return;
		}
		
		JsonElement vlanElem = null;
		final Set<String> slSubnetIds = new HashSet<String>();
		for (final JsonElement vlanObj : arr) {
			final String gatewayId = JsonUtil.getStringFromPath(vlanObj, "attachedNetworkGateway.id");
			if (gatewayId != null) {
				continue;
			}
			
			final JsonArray subnetArr = JsonUtil.getArrayFromPath(vlanObj, "subnets");
			if (subnetArr == null || subnetArr.size() == 0) {
				// no subnets
				continue;
			}

			for (final JsonElement subnetElem : subnetArr) {
				final String subnetId = JsonUtil.getStringFromPath(subnetElem, "id");
				slSubnetIds.add(subnetId);
			}

			vlanElem = vlanObj;
		}
		
		if (vlanElem == null) {
			logger.info("No free VLANs exist; exit");
			return;
		}
		
		service.beginTransaction();
		logger.info(String.format("Adding vlan %s.%d", JsonUtil.getStringFromPath(vlanElem, "primaryRouter.hostname"), JsonUtil.getIntFromPath(vlanElem, "vlanNumber")));
		final com.ibm.scas.analytics.beans.Vlan vlanReq = new com.ibm.scas.analytics.beans.Vlan();
		vlanReq.setSoftLayerAccount(slAcct);
		vlanReq.setPrimaryRouter(JsonUtil.getStringFromPath(vlanElem, "primaryRouter.hostname"));
		vlanReq.setVlanNumber(String.valueOf(JsonUtil.getIntFromPath(vlanElem, "vlanNumber")));
		final String newVlanId = networkProvider.addVlan(vlanReq);
		service.commitTransaction();
		
		service.beginTransaction();
		logger.info("Creating a cluster ");
		final Cluster clusterRec = new Cluster();
		clusterRec.setName("abcd");
		clusterRec.setOwner(subscriber);
		clusterRec.setSize(2);
		clusterRec.setCurrentStep(com.ibm.scas.analytics.beans.Cluster.ClusterStep.RESERVE_NETWORK.name());
		service.saveObject(Cluster.class, clusterRec);
		service.commitTransaction();
		
		service.beginTransaction();
		com.ibm.scas.analytics.persistence.beans.Vlan vlanRec = service.getObjectById(com.ibm.scas.analytics.persistence.beans.Vlan.class, newVlanId);
		vlanRec.setCluster(clusterRec);
		service.commitTransaction();
		
		// try to delete vlan
		logger.info(String.format("Deleting vlan %s while assigned to cluster %s", newVlanId, clusterRec.getId()));
		try {
			service.beginTransaction();
			networkProvider.deleteVlan(newVlanId);
			service.commitTransaction();
			assertTrue("Deleted VLAN when it's already assigned", true);
		} catch (CPEException e) {
			logger.info(String.format("Caught exception when deleting VLAN: %s", e.getLocalizedMessage()));
		}
		
		// try to unassign vlan
		service.beginTransaction();
		logger.info(String.format("Unassigning vlan %s", newVlanId));
		networkProvider.unassignVlan(newVlanId, true);
		service.commitTransaction();
		
		final Vlan vlan = networkProvider.getVlanById(newVlanId);
		assertTrue(String.format("Vlan %s is still assigned to cluster: %s", newVlanId, ReflectionToStringBuilder.toString(vlan.getCluster())), vlan.getCluster() == null);
			
		service.beginTransaction();
		networkProvider.deleteVlan(newVlanId);
		service.deleteObject(Subscriber.class, subscriber);
		service.deleteObject(Cluster.class, clusterRec);
		service.commitTransaction();
				
	}

}
