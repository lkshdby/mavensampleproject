package com.ibm.scas.analytics.test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.ibm.scas.analytics.backend.NetworkService;
import com.ibm.scas.analytics.backend.SoftLayerAPIGateway;
import com.ibm.scas.analytics.backend.SoftLayerAccountProvider;
import com.ibm.scas.analytics.backend.impl.SoftLayerNetworkService;
import com.ibm.scas.analytics.beans.IPAddress;
import com.ibm.scas.analytics.beans.SoftLayerAccount;
import com.ibm.scas.analytics.beans.Subnet;
import com.ibm.scas.analytics.utils.IPAddressUtil;
import com.ibm.scas.analytics.utils.JsonUtil;
import com.ibm.scas.analytics.utils.SoftLayerObjectFilter;

public class TestSubnetService extends BaseTestCase {
	private String slAcctId;
	private NetworkService networkProvider;
	
	private static final Logger logger = Logger.getLogger(TestSubnetService.class);
	
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
	
	public void testGetIPsInSubnet() throws Exception {
		logger.info("****** testGetIPsInSubnet()");
		/** first add a floating Vlan **/
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
		service.beginTransaction();
		final com.ibm.scas.analytics.beans.Vlan vlanReq = new com.ibm.scas.analytics.beans.Vlan();
		vlanReq.setSoftLayerAccount(slAcct);
		vlanReq.setPrimaryRouter(JsonUtil.getStringFromPath(vlanElem, "primaryRouter.hostname"));
		vlanReq.setVlanNumber(String.valueOf(JsonUtil.getIntFromPath(vlanElem, "vlanNumber")));
	
		final String newVlanId = networkProvider.addVlan(vlanReq);
		service.commitTransaction();
		
		logger.info("New vlan record: " + newVlanId);
		
		final List<Subnet> subnets = networkProvider.getAllSubnets();
		for (final Subnet subnet : subnets) {
			final List<IPAddress> ipAddrs = networkProvider.getIPAddressBySubnet(subnet.getId());
			
			for (final IPAddress ipAddr : ipAddrs) {
				assertTrue(String.format("Returned an IP address outside of subnet: %s (%s/%s)", ipAddr.getIpAddress(), subnet.getNetworkAddr(), subnet.getCidr()), IPAddressUtil.isIpInNetwork(ipAddr.getIpAddress(), subnet.getNetworkAddr(), subnet.getCidr()));
				
				if (IPAddressUtil.ipToLong(subnet.getNetworkAddr()) == IPAddressUtil.ipToLong(ipAddr.getIpAddress())) {
					assertTrue("Network address is reservable", !ipAddr.isReservable());
				}
				if (IPAddressUtil.ipToLong(subnet.getNetworkAddr()) + 1 == IPAddressUtil.ipToLong(ipAddr.getIpAddress())) {
					assertTrue("Gateway address is reservable", !ipAddr.isReservable());
				}
				if (IPAddressUtil.ipToLong(subnet.getBroadcastAddr()) + 1 == IPAddressUtil.ipToLong(ipAddr.getIpAddress())) {
					assertTrue("Gateway address is reservable", !ipAddr.isReservable());
				}
			}
		}
		
		service.beginTransaction();
		networkProvider.deleteVlan(newVlanId);
		service.commitTransaction();
	}
	

}
