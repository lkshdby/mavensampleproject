package com.ibm.scas.analytics.test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.scas.analytics.backend.NetworkService;
import com.ibm.scas.analytics.backend.SoftLayerAPIGateway;
import com.ibm.scas.analytics.backend.SoftLayerAccountProvider;
import com.ibm.scas.analytics.beans.Gateway;
import com.ibm.scas.analytics.beans.Gateway.GatewayType;
import com.ibm.scas.analytics.beans.GatewayMember;
import com.ibm.scas.analytics.beans.SoftLayerAccount;
import com.ibm.scas.analytics.utils.JsonUtil;

public class TestGatewayService extends BaseTestCase {
	private String slAcctId;
	private NetworkService networkProvider;
	
	private static final Logger logger = Logger.getLogger(TestGatewayService.class);
	
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
		
	}
	
	@Override
	public void tearDown() throws Exception {
		final SoftLayerAccountProvider acctProvider = injector.getInstance(SoftLayerAccountProvider.class);
		
		acctProvider.deleteSoftLayerAccount(slAcctId);
		
		super.tearDown();
	}
	
	public void testAddGatewayByName() throws Exception {
		logger.info("****** testAddGatewayByName()");
		service.beginTransaction();
		final SoftLayerAccountProvider acctProvider = injector.getInstance(SoftLayerAccountProvider.class);
		final SoftLayerAccount slAcct = acctProvider.getSoftLayerAccountById(slAcctId);
		final SoftLayerAPIGateway sgw = new SoftLayerAPIGateway(slAcct);
		
		final JsonElement elem = sgw.callGetMethod("SoftLayer_Account", "getNetworkGateways", null, "id", "name");
		
		final JsonArray arr = elem.getAsJsonArray();
		if (arr.size() == 0) {
			logger.info("Skipping test, there are no gateways in this account");
			return;
		}
		final JsonObject idObj = arr.get(0).getAsJsonObject();
		final int gatewayId = idObj.get("id").getAsInt();
		final String gatewayName = idObj.get("name").getAsString();
		
		logger.info("Adding gateway with sl ID: " + gatewayId + ", name: " + gatewayName);
		
		final Gateway gatewayReq = new Gateway();
		final SoftLayerAccount newSlAcct = new SoftLayerAccount();
		newSlAcct.setId(slAcctId);
		
		gatewayReq.setSoftLayerAccount(newSlAcct);
		gatewayReq.setName(gatewayName);
		gatewayReq.setType(GatewayType.SHARED);
		
		final String newGatewayId = networkProvider.addGateway(gatewayReq);
		service.commitTransaction();
		
		logger.info("New gateway record: " + newGatewayId);
		
		final Gateway gateway = networkProvider.getGatewayById(newGatewayId);
		logger.info(ReflectionToStringBuilder.toString(gateway));
		
		//assertTrue("Gateway name doesn't match", gateway.getName().equals(gatewayName));
		assertTrue("Gateway id doesn't match", gateway.getSoftLayerId().equals(String.valueOf(gatewayId)));
		
		service.beginTransaction();
		networkProvider.deleteGateway(newGatewayId);
		service.commitTransaction();
		
		final List<Gateway> allGateways = networkProvider.getAllGateways();
		for (final Gateway gw : allGateways) {
			assertTrue("Gateway still exists", gw.getId().equals(newGatewayId));
		}
		
	}
	
	public void testAddGatewayBySlId() throws Exception {
		logger.info("****** testAddGatewayBySlId()");
		final SoftLayerAccountProvider acctProvider = injector.getInstance(SoftLayerAccountProvider.class);
		final SoftLayerAccount slAcct = acctProvider.getSoftLayerAccountById(slAcctId);
		final SoftLayerAPIGateway sgw = new SoftLayerAPIGateway(slAcct);
		
		final JsonElement elem = sgw.callGetMethod("SoftLayer_Account", "getNetworkGateways", null, "id", "name");
		
		final JsonArray arr = elem.getAsJsonArray();
		if (arr.size() == 0) {
			logger.info("Skipping test, there are no gateways in this account");
			return;
		}
		final JsonObject idObj = arr.get(0).getAsJsonObject();
		final int gatewayId = idObj.get("id").getAsInt();
		final String gatewayName = idObj.get("name").getAsString();
		
		logger.info("Adding gateway with sl ID: " + gatewayId + ", name: " + gatewayName);
		
		service.beginTransaction();
		final Gateway gatewayReq = new Gateway();
		final SoftLayerAccount newSlAcct = new SoftLayerAccount();
		newSlAcct.setId(slAcctId);
		
		gatewayReq.setSoftLayerAccount(newSlAcct);
		gatewayReq.setSoftLayerId(String.valueOf(gatewayId));;
		gatewayReq.setType(GatewayType.SHARED);
		
		final String newGatewayId = networkProvider.addGateway(gatewayReq);
		service.commitTransaction();
		
		logger.info("New gateway record: " + newGatewayId);
		
		final Gateway gateway = networkProvider.getGatewayById(newGatewayId);
		logger.info(ReflectionToStringBuilder.toString(gateway));
		
		//assertTrue("Gateway name doesn't match", gateway.getName().equals(gatewayName));
		assertTrue("Gateway id doesn't match", gateway.getSoftLayerId().equals(String.valueOf(gatewayId)));
		
		service.beginTransaction();
		networkProvider.deleteGateway(newGatewayId);
		service.commitTransaction();
		
		final List<Gateway> allGateways = networkProvider.getAllGateways();
		for (final Gateway gw : allGateways) {
			assertTrue("Gateway still exists", gw.getId().equals(newGatewayId));
		}
	}
	
	public void testAddGatewayWithNoPassword() throws Exception {
		// ensure no VLANs are put in the DB
		logger.info("****** testAddGatewayWithMemberUserPassword()");
		final SoftLayerAccountProvider acctProvider = injector.getInstance(SoftLayerAccountProvider.class);
		final SoftLayerAccount slAcct = acctProvider.getSoftLayerAccountById(slAcctId);
		final SoftLayerAPIGateway sgw = new SoftLayerAPIGateway(slAcct);

		final JsonElement elem = sgw.callGetMethod("SoftLayer_Account", "getNetworkGateways", null, "id", 
				"name", 
				"members.hardware.primaryBackendIpAddress",
				"members.hardware.operatingSystem.id",
				"members.hardware.operatingSystem.passwords.username",
				"members.hardware.operatingSystem.passwords.password");

		final JsonArray arr = elem.getAsJsonArray();
		if (arr.size() == 0) {
			logger.info("Skipping test, there are no gateways in this account");
			return;
		}

		JsonObject myGatewayObj = null;

		final Set<GatewayMember> members = new HashSet<GatewayMember>();
		for (final JsonElement gatewayElem : arr) {
			JsonObject gatewayObj = gatewayElem.getAsJsonObject();
			JsonArray memberArr = JsonUtil.getArrayFromPath(gatewayObj, "members");
			if (memberArr == null || memberArr.size() < 2) {
				// we want an HA vyatta
				continue;
			}
			
			for (final JsonElement memberElem  : memberArr) {
				final GatewayMember mem = new GatewayMember();
				final String privateIp = JsonUtil.getStringFromPath(memberElem, "hardware.primaryBackendIpAddress");
				mem.setMemberIp(privateIp);
				final JsonArray passwordsArr = JsonUtil.getArrayFromPath(memberElem, "hardware.operatingSystem.passwords");
				for (final JsonElement passwordElem : passwordsArr) {
					final String username = JsonUtil.getStringFromPath(passwordElem, "username");
					final String password = JsonUtil.getStringFromPath(passwordElem, "password");
					
					if (username.equals("root")) {
						// cannot connect to REST API as root
						continue;
					}
					
					mem.setUsername(username);
					mem.setPassword(password); 
				}
				
				members.add(mem);
			}
			
			
			myGatewayObj = gatewayObj;
			break;
		}

		if (myGatewayObj == null) {
			logger.info("Skipping test, there are no gateways with associated vlans in this account");
			return;
		}

		final String gatewayName = myGatewayObj.get("name").getAsString();
		final String gatewayId = myGatewayObj.get("id").getAsString();

		logger.info("Adding gateway with sl ID: " + gatewayId + ", name: " + gatewayName);

		service.beginTransaction();
		final Gateway gatewayReq = new Gateway();
		final SoftLayerAccount newSlAcct = new SoftLayerAccount();
		newSlAcct.setId(slAcctId);

		gatewayReq.setSoftLayerAccount(newSlAcct);
		gatewayReq.setSoftLayerId(String.valueOf(gatewayId));;
		gatewayReq.setType(GatewayType.SHARED);
		
		final String newGatewayId = networkProvider.addGateway(gatewayReq);
		service.commitTransaction();

		logger.info("New gateway record: " + newGatewayId);

		final Gateway gateway = networkProvider.getGatewayById(newGatewayId);
		logger.info(ReflectionToStringBuilder.toString(gateway));

		assertTrue("Gateway id doesn't match", gateway.getSoftLayerId().equals(String.valueOf(gatewayId)));
		
		// make sure both members have password from SL API
		for (final GatewayMember member : gateway.getGatewayMembers()) {
			for (final GatewayMember apiMember : members) {
				if (!apiMember.getMemberIp().equals(member.getMemberIp())) {
					continue;
				}
				assertTrue("Gateway username does not match api username", member.getUsername().equals(apiMember.getUsername()));
				assertTrue("Gateway password does not match api password", member.getPassword().equals(apiMember.getPassword()));
				break;
			}
		}

		service.beginTransaction();
		networkProvider.deleteGateway(newGatewayId);
		service.commitTransaction();

		final List<Gateway> allGateways = networkProvider.getAllGateways();
		for (final Gateway gw : allGateways) {
			assertTrue("Gateway still exists", gw.getId().equals(newGatewayId));
		}
	}
	
	public void testUpdateGatewayWithMemberUserPassword() throws Exception {
		logger.info("****** testUpdateGatewayWithMemberUserPassword()");
		final SoftLayerAccountProvider acctProvider = injector.getInstance(SoftLayerAccountProvider.class);
		final SoftLayerAccount slAcct = acctProvider.getSoftLayerAccountById(slAcctId);
		final SoftLayerAPIGateway sgw = new SoftLayerAPIGateway(slAcct);

		final JsonElement elem = sgw.callGetMethod("SoftLayer_Account", "getNetworkGateways", null, "id", 
				"name", 
				"members.hardware.primaryBackendIpAddress",
				"members.hardware.operatingSystem.id",
				"members.hardware.operatingSystem.passwords.username",
				"members.hardware.operatingSystem.passwords.password");

		final JsonArray arr = elem.getAsJsonArray();
		if (arr.size() == 0) {
			logger.info("Skipping test, there are no gateways in this account");
			return;
		}

		final Set<GatewayMember> members = new HashSet<GatewayMember>();
		JsonObject myGatewayObj = null;
		for (final JsonElement gatewayElem : arr) {
			final JsonObject gatewayObj = gatewayElem.getAsJsonObject();

			JsonArray memberArr = JsonUtil.getArrayFromPath(gatewayObj, "members");
			if (memberArr == null || memberArr.size() < 2) {
				// we want an HA vyatta
				continue;
			}
			
			for (final JsonElement memberElem  : memberArr) {
				final GatewayMember mem = new GatewayMember();
				final String privateIp = JsonUtil.getStringFromPath(memberElem, "hardware.primaryBackendIpAddress");
				mem.setMemberIp(privateIp);
				mem.setUsername("jkwong");
				mem.setPassword("password" + privateIp); 
				
				members.add(mem);
			}
			
			myGatewayObj = gatewayObj;
			break;
		}

		if (myGatewayObj == null) {
			logger.info("Skipping test, there are no gateways with associated vlans in this account");
			return;
		}

		final String gatewayName = myGatewayObj.get("name").getAsString();
		final String gatewayId = myGatewayObj.get("id").getAsString();

		logger.info("Adding gateway with sl ID: " + gatewayId + ", name: " + gatewayName);

		service.beginTransaction();
		final Gateway gatewayReq = new Gateway();
		final SoftLayerAccount newSlAcct = new SoftLayerAccount();
		newSlAcct.setId(slAcctId);

		gatewayReq.setSoftLayerAccount(newSlAcct);
		gatewayReq.setSoftLayerId(String.valueOf(gatewayId));;
		gatewayReq.setType(GatewayType.SHARED);
		
		//gatewayReq.setGatewayMembers(members);

		final String newGatewayId = networkProvider.addGateway(gatewayReq);
		service.commitTransaction();

		logger.info("New gateway record: " + newGatewayId);

		Gateway gateway = networkProvider.getGatewayById(newGatewayId);
		logger.info(ReflectionToStringBuilder.toString(gateway));

		assertTrue("Gateway id doesn't match", gateway.getSoftLayerId().equals(String.valueOf(gatewayId)));
		
		service.beginTransaction();
		final Gateway gatewayReq2 = new Gateway();
		gatewayReq2.setGatewayMembers(members);
		networkProvider.updateGatewayUserPassword(newGatewayId, gatewayReq2);
		service.commitTransaction();

		gateway = networkProvider.getGatewayById(newGatewayId);	
		// make sure both members have my passed in password
		for (final GatewayMember member : gateway.getGatewayMembers()) {
			assertTrue("Gateway username does not match username: expected jkwong, got " + member.getUsername(), member.getUsername().equals("jkwong"));
			assertTrue("Gateway password does not match password: expected password" + member.getMemberIp() + " got " + member.getPassword(), member.getPassword().equals("password" + member.getMemberIp()));
		}
		
		service.beginTransaction();
		networkProvider.deleteGateway(newGatewayId);
		service.commitTransaction();

		final List<Gateway> allGateways = networkProvider.getAllGateways();
		for (final Gateway gw : allGateways) {
			assertTrue("Gateway still exists", gw.getId().equals(newGatewayId));
		}

	}
	
	public void testAddGatewayWithMemberUserPassword() throws Exception {
		// ensure no VLANs are put in the DB
		logger.info("****** testAddGatewayWithMemberUserPassword()");
		final SoftLayerAccountProvider acctProvider = injector.getInstance(SoftLayerAccountProvider.class);
		final SoftLayerAccount slAcct = acctProvider.getSoftLayerAccountById(slAcctId);
		final SoftLayerAPIGateway sgw = new SoftLayerAPIGateway(slAcct);

		final JsonElement elem = sgw.callGetMethod("SoftLayer_Account", "getNetworkGateways", null, "id", 
				"name", 
				"members.hardware.primaryBackendIpAddress",
				"members.hardware.operatingSystem.id",
				"members.hardware.operatingSystem.passwords.username",
				"members.hardware.operatingSystem.passwords.password");

		final JsonArray arr = elem.getAsJsonArray();
		if (arr.size() == 0) {
			logger.info("Skipping test, there are no gateways in this account");
			return;
		}

		final Set<GatewayMember> members = new HashSet<GatewayMember>();
		JsonObject myGatewayObj = null;
		for (final JsonElement gatewayElem : arr) {
			final JsonObject gatewayObj = gatewayElem.getAsJsonObject();

			
			JsonArray memberArr = JsonUtil.getArrayFromPath(gatewayObj, "members");
			if (memberArr == null || memberArr.size() < 2) {
				// we want an HA vyatta
				continue;
			}
			
			for (final JsonElement memberElem  : memberArr) {
				final GatewayMember mem = new GatewayMember();
				final String privateIp = JsonUtil.getStringFromPath(memberElem, "hardware.primaryBackendIpAddress");
				mem.setMemberIp(privateIp);
				mem.setUsername("jkwong");
				mem.setPassword("password" + privateIp); 
				
				members.add(mem);
			}
			
			myGatewayObj = gatewayObj;
			break;
		}

		if (myGatewayObj == null) {
			logger.info("Skipping test, there are no gateways with associated vlans in this account");
			return;
		}

		final String gatewayName = myGatewayObj.get("name").getAsString();
		final String gatewayId = myGatewayObj.get("id").getAsString();

		logger.info("Adding gateway with sl ID: " + gatewayId + ", name: " + gatewayName);

		service.beginTransaction();
		final Gateway gatewayReq = new Gateway();
		final SoftLayerAccount newSlAcct = new SoftLayerAccount();
		newSlAcct.setId(slAcctId);

		gatewayReq.setSoftLayerAccount(newSlAcct);
		gatewayReq.setSoftLayerId(String.valueOf(gatewayId));;
		gatewayReq.setType(GatewayType.SHARED);
		
		gatewayReq.setGatewayMembers(members);

		final String newGatewayId = networkProvider.addGateway(gatewayReq);
		service.commitTransaction();

		logger.info("New gateway record: " + newGatewayId);

		final Gateway gateway = networkProvider.getGatewayById(newGatewayId);
		logger.info(ReflectionToStringBuilder.toString(gateway));

		assertTrue("Gateway id doesn't match", gateway.getSoftLayerId().equals(String.valueOf(gatewayId)));
		
		// make sure both members have my passed in password
		for (final GatewayMember member : gateway.getGatewayMembers()) {
			assertTrue("Gateway username does not match username: expected jkwong, got " + member.getUsername(), member.getUsername().equals("jkwong"));
			assertTrue("Gateway password does not match password: expected password" + member.getMemberIp() + " got " + member.getPassword(), member.getPassword().equals("password" + member.getMemberIp()));
		}
		service.beginTransaction();
		networkProvider.deleteGateway(newGatewayId);
		service.commitTransaction();

		final List<Gateway> allGateways = networkProvider.getAllGateways();
		for (final Gateway gw : allGateways) {
			assertTrue("Gateway still exists", gw.getId().equals(newGatewayId));
		}

	}
	
	public void testAddGatewayWithUserPassword() throws Exception {
		// ensure no VLANs are put in the DB
		logger.info("****** testAddGatewayWithUserPassword()");
		final SoftLayerAccountProvider acctProvider = injector.getInstance(SoftLayerAccountProvider.class);
		final SoftLayerAccount slAcct = acctProvider.getSoftLayerAccountById(slAcctId);
		final SoftLayerAPIGateway sgw = new SoftLayerAPIGateway(slAcct);

		final JsonElement elem = sgw.callGetMethod("SoftLayer_Account", "getNetworkGateways", null, "id", 
				"name", 
				"members.hardware.primaryBackendIpAddress",
				"members.hardware.operatingSystem.id",
				"members.hardware.operatingSystem.passwords.username",
				"members.hardware.operatingSystem.passwords.password");

		final JsonArray arr = elem.getAsJsonArray();
		if (arr.size() == 0) {
			logger.info("Skipping test, there are no gateways in this account");
			return;
		}

		JsonObject myGatewayObj = null;

		for (final JsonElement gatewayElem : arr) {
			final JsonObject gatewayObj = gatewayElem.getAsJsonObject();
		
			myGatewayObj = gatewayObj;
			break;
		}

		final String gatewayName = myGatewayObj.get("name").getAsString();
		final String gatewayId = myGatewayObj.get("id").getAsString();

		logger.info("Adding gateway with sl ID: " + gatewayId + ", name: " + gatewayName);

		service.beginTransaction();
		final Gateway gatewayReq = new Gateway();
		final SoftLayerAccount newSlAcct = new SoftLayerAccount();
		newSlAcct.setId(slAcctId);

		gatewayReq.setSoftLayerAccount(newSlAcct);
		gatewayReq.setSoftLayerId(String.valueOf(gatewayId));;
		gatewayReq.setType(GatewayType.SHARED);
		gatewayReq.setUsername("jkwong");
		gatewayReq.setPassword("Letmein");

		final String newGatewayId = networkProvider.addGateway(gatewayReq);
		service.commitTransaction();

		logger.info("New gateway record: " + newGatewayId);

		final Gateway gateway = networkProvider.getGatewayById(newGatewayId);
		logger.info(ReflectionToStringBuilder.toString(gateway));

		assertTrue("Gateway id doesn't match", gateway.getSoftLayerId().equals(String.valueOf(gatewayId)));
		
		// make sure both members have my passed in password
		for (final GatewayMember member : gateway.getGatewayMembers()) {
			assertTrue("Gateway username does not match passed in username", member.getUsername().equals("jkwong"));
			assertTrue("Gateway password does not match passed in password", member.getPassword().equals("Letmein"));
		}

		service.beginTransaction();
		networkProvider.deleteGateway(newGatewayId);
		service.commitTransaction();

		final List<Gateway> allGateways = networkProvider.getAllGateways();
		for (final Gateway gw : allGateways) {
			assertTrue("Gateway still exists", gw.getId().equals(newGatewayId));
		}
	}
	
	public void testResetGatewayUserPassword() throws Exception {
		logger.info("****** testResetGatewayUserPassword()");
		final SoftLayerAccountProvider acctProvider = injector.getInstance(SoftLayerAccountProvider.class);
		final SoftLayerAccount slAcct = acctProvider.getSoftLayerAccountById(slAcctId);
		final SoftLayerAPIGateway sgw = new SoftLayerAPIGateway(slAcct);

		final JsonElement elem = sgw.callGetMethod("SoftLayer_Account", "getNetworkGateways", null, "id", 
				"name", 
				"members.hardware.primaryBackendIpAddress",
				"members.hardware.operatingSystem.id",
				"members.hardware.operatingSystem.passwords.username",
				"members.hardware.operatingSystem.passwords.password");

		final JsonArray arr = elem.getAsJsonArray();
		if (arr.size() == 0) {
			logger.info("Skipping test, there are no gateways in this account");
			return;
		}

		JsonObject myGatewayObj = null;
		final Set<GatewayMember> members = new HashSet<GatewayMember>();
		for (final JsonElement gatewayElem : arr) {
			final JsonObject gatewayObj = gatewayElem.getAsJsonObject();

			JsonArray memberArr = JsonUtil.getArrayFromPath(gatewayObj, "members");
			if (memberArr == null || memberArr.size() < 2) {
				// we want an HA vyatta
				continue;
			}
			
			for (final JsonElement memberElem  : memberArr) {
				final GatewayMember mem = new GatewayMember();
				final String privateIp = JsonUtil.getStringFromPath(memberElem, "hardware.primaryBackendIpAddress");
				mem.setMemberIp(privateIp);
				final JsonArray passwordsArr = JsonUtil.getArrayFromPath(memberElem, "hardware.operatingSystem.passwords");
				for (final JsonElement passwordElem : passwordsArr) {
					final String username = JsonUtil.getStringFromPath(passwordElem, "username");
					final String password = JsonUtil.getStringFromPath(passwordElem, "password");
					
					if (username.equals("root")) {
						// cannot connect to REST API as root
						continue;
					}
					
					mem.setUsername(username);
					mem.setPassword(password); 
				}
				
				members.add(mem);
			}
		
			myGatewayObj = gatewayObj;
			break;
		}

		final String gatewayName = myGatewayObj.get("name").getAsString();
		final String gatewayId = myGatewayObj.get("id").getAsString();

		logger.info("Adding gateway with sl ID: " + gatewayId + ", name: " + gatewayName);

		service.beginTransaction();
		final Gateway gatewayReq = new Gateway();
		final SoftLayerAccount newSlAcct = new SoftLayerAccount();
		newSlAcct.setId(slAcctId);

		gatewayReq.setSoftLayerAccount(newSlAcct);
		gatewayReq.setSoftLayerId(String.valueOf(gatewayId));;
		gatewayReq.setType(GatewayType.SHARED);
		gatewayReq.setUsername("jkwong");
		gatewayReq.setPassword("Letmein");

		final String newGatewayId = networkProvider.addGateway(gatewayReq);
		service.commitTransaction();

		logger.info("New gateway record: " + newGatewayId);

		Gateway gateway = networkProvider.getGatewayById(newGatewayId);
		logger.info(ReflectionToStringBuilder.toString(gateway));

		assertTrue("Gateway id doesn't match", gateway.getSoftLayerId().equals(String.valueOf(gatewayId)));
		
		// make sure both members have my passed in password
		for (final GatewayMember member : gateway.getGatewayMembers()) {
			assertTrue("Gateway username does not match passed in username", member.getUsername().equals("jkwong"));
			assertTrue("Gateway password does not match passed in password", member.getPassword().equals("Letmein"));
		}
		
		// now try to update the user password with a blank password
		service.beginTransaction();
		// resetting the gateway's user/password
		final Gateway gatewayReq2 = new Gateway();
		networkProvider.updateGatewayUserPassword(newGatewayId, gatewayReq2);
		service.commitTransaction();
		
		gateway = networkProvider.getGatewayById(newGatewayId);
		logger.info(ReflectionToStringBuilder.toString(gateway));

		// make sure both members have password from SL API
		for (final GatewayMember member : gateway.getGatewayMembers()) {
			for (final GatewayMember apiMember : members) {
				if (!apiMember.getMemberIp().equals(member.getMemberIp())) {
					continue;
				}
				assertTrue("Gateway username does not match api username", member.getUsername().equals(apiMember.getUsername()));
				assertTrue("Gateway password does not match api password", member.getPassword().equals(apiMember.getPassword()));
				break;
			}
		}
		
		service.beginTransaction();
		networkProvider.deleteGateway(newGatewayId);
		service.commitTransaction();

		final List<Gateway> allGateways = networkProvider.getAllGateways();
		for (final Gateway gw : allGateways) {
			assertTrue("Gateway still exists", gw.getId().equals(newGatewayId));
		}
	}
	
	public void testUpdateGatewayUserPassword() throws Exception {
		logger.info("****** testUpdateGatewayUserPassword()");
		final SoftLayerAccountProvider acctProvider = injector.getInstance(SoftLayerAccountProvider.class);
		final SoftLayerAccount slAcct = acctProvider.getSoftLayerAccountById(slAcctId);
		final SoftLayerAPIGateway sgw = new SoftLayerAPIGateway(slAcct);

		final JsonElement elem = sgw.callGetMethod("SoftLayer_Account", "getNetworkGateways", null, "id", 
				"name", 
				"members.hardware.primaryBackendIpAddress",
				"members.hardware.operatingSystem.id",
				"members.hardware.operatingSystem.passwords.username",
				"members.hardware.operatingSystem.passwords.password");

		final JsonArray arr = elem.getAsJsonArray();
		if (arr.size() == 0) {
			logger.info("Skipping test, there are no gateways in this account");
			return;
		}

		JsonObject myGatewayObj = null;
		final Set<GatewayMember> members = new HashSet<GatewayMember>();
		for (final JsonElement gatewayElem : arr) {
			final JsonObject gatewayObj = gatewayElem.getAsJsonObject();

			JsonArray memberArr = JsonUtil.getArrayFromPath(gatewayObj, "members");
			if (memberArr == null || memberArr.size() < 2) {
				// we want an HA vyatta
				continue;
			}
			
			for (final JsonElement memberElem  : memberArr) {
				final GatewayMember mem = new GatewayMember();
				final String privateIp = JsonUtil.getStringFromPath(memberElem, "hardware.primaryBackendIpAddress");
				mem.setMemberIp(privateIp);
				mem.setUsername(null);
				mem.setPassword(null); 
				
				members.add(mem);
			}
		
			myGatewayObj = gatewayObj;
			break;
		}

		final String gatewayName = myGatewayObj.get("name").getAsString();
		final String gatewayId = myGatewayObj.get("id").getAsString();

		logger.info("Adding gateway with sl ID: " + gatewayId + ", name: " + gatewayName);

		service.beginTransaction();
		final Gateway gatewayReq = new Gateway();
		final SoftLayerAccount newSlAcct = new SoftLayerAccount();
		newSlAcct.setId(slAcctId);

		gatewayReq.setSoftLayerAccount(newSlAcct);
		gatewayReq.setSoftLayerId(String.valueOf(gatewayId));;
		gatewayReq.setType(GatewayType.SHARED);
		//gatewayReq.setUsername("jkwong");
		//gatewayReq.setPassword("Letmein");

		final String newGatewayId = networkProvider.addGateway(gatewayReq);
		service.commitTransaction();

		logger.info("New gateway record: " + newGatewayId);

		Gateway gateway = networkProvider.getGatewayById(newGatewayId);
		logger.info(ReflectionToStringBuilder.toString(gateway));

		assertTrue("Gateway id doesn't match", gateway.getSoftLayerId().equals(String.valueOf(gatewayId)));
		
		// now try to update the user password
		logger.info("Updating gateway user/password to jkwong/Letmein ...");
		service.beginTransaction();
		final Gateway gatewayReq2 = new Gateway();
		gatewayReq2.setUsername("jkwong");
		gatewayReq2.setPassword("Letmein");
		networkProvider.updateGatewayUserPassword(newGatewayId, gatewayReq2);
		service.commitTransaction();
		
		gateway = networkProvider.getGatewayById(newGatewayId);
		logger.info("Gateway:" + ReflectionToStringBuilder.toString(gateway, ToStringStyle.MULTI_LINE_STYLE));
		// make sure both members have my passed in password
		for (final GatewayMember member : gateway.getGatewayMembers()) {
			logger.info("GatewayMember:" + ReflectionToStringBuilder.toString(member, ToStringStyle.MULTI_LINE_STYLE));
			assertTrue("Gateway username does not match passed in username, expected jkwong, got " + member.getUsername(), member.getUsername().equals("jkwong"));
			assertTrue("Gateway password does not match passed in password, expected Letmein, got " + member.getPassword(), member.getPassword().equals("Letmein"));
		}
		
		service.beginTransaction();
		networkProvider.deleteGateway(newGatewayId);
		service.commitTransaction();

		final List<Gateway> allGateways = networkProvider.getAllGateways();
		for (final Gateway gw : allGateways) {
			assertTrue("Gateway still exists", gw.getId().equals(newGatewayId));
		}
	}
}
