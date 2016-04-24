package com.ibm.scas.analytics.backend.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.inject.Inject;
import com.ibm.scas.analytics.EngineProperties;
import com.ibm.scas.analytics.backend.FirewallService;
import com.ibm.scas.analytics.backend.NetworkService;
import com.ibm.scas.analytics.backend.ProvisioningService;
import com.ibm.scas.analytics.backend.SoftLayerAPIGateway;
import com.ibm.scas.analytics.backend.monitoring.NagiosEventLogger;
import com.ibm.scas.analytics.beans.Cluster;
import com.ibm.scas.analytics.beans.Gateway;
import com.ibm.scas.analytics.beans.Gateway.GatewayStatus;
import com.ibm.scas.analytics.beans.Gateway.GatewayType;
import com.ibm.scas.analytics.beans.IPAddress;
import com.ibm.scas.analytics.beans.SoftLayerIdObject;
import com.ibm.scas.analytics.beans.Subnet;
import com.ibm.scas.analytics.beans.Vlan;
import com.ibm.scas.analytics.beans.Vlan.VlanNetworkSpace;
import com.ibm.scas.analytics.beans.Vlan.VlanTrunk;
import com.ibm.scas.analytics.beans.utils.BeanConversionUtil;
import com.ibm.scas.analytics.persistence.PersistenceException;
import com.ibm.scas.analytics.persistence.PersistenceService;
import com.ibm.scas.analytics.persistence.beans.Account;
import com.ibm.scas.analytics.persistence.beans.CPELocation;
import com.ibm.scas.analytics.persistence.beans.GatewayMember;
import com.ibm.scas.analytics.persistence.beans.SoftLayerAccount;
import com.ibm.scas.analytics.persistence.beans.VPNTunnel;
import com.ibm.scas.analytics.persistence.util.WhereClause;
import com.ibm.scas.analytics.persistence.util.WhereInClause;
import com.ibm.scas.analytics.utils.CPEException;
import com.ibm.scas.analytics.utils.CPEParamException;
import com.ibm.scas.analytics.utils.CollectionsUtil;
import com.ibm.scas.analytics.utils.IPAddressUtil;
import com.ibm.scas.analytics.utils.JsonUtil;
import com.ibm.scas.analytics.utils.SoftLayerObjectFilter;

/**
 * This class fetches the network (vlan, gateway, subnet) information from
 * database
 * 
 * @author rekha_udabale
 * 
 */

public class SoftLayerNetworkService implements NetworkService {
	private final static Logger logger = Logger.getLogger(SoftLayerNetworkService.class);
	
	@Inject protected PersistenceService persistence;
	@Inject private ProvisioningService provisioningService;
	@Inject private FirewallService firewallService;
	
	private final static String cpeLocationName = EngineProperties.getInstance().getProperty(EngineProperties.CPE_LOCATION_NAME, EngineProperties.DEFAULT_CPE_LOCATION_NAME);
	
	private final static String[] GATEWAY_OBJ_MASK_FRAGMENT = {
		"id",
		"name",
		"groupNumber",
		"privateIpAddress.ipAddress",
		"publicIpAddress.ipAddress",
		"members.priority",
		"members.hardware.id",
		"members.hardware.primaryBackendIpAddress",
		"members.hardware.operatingSystem.id",
		"members.hardware.operatingSystem.passwords.username",
		"members.hardware.operatingSystem.passwords.password",
		"members.hardware.activeTransactionCount",
		"privateVlan.id",
		"privateVlan.subnets.networkIdentifier",
		"privateVlan.subnets.cidr",
		"privateVlan.primaryRouter.hostname",
		"publicVlan.id",
		"publicVlan.primaryRouter.hostname",
		"insideVlans.id",
		"status.keyName"
		/*
		// below properties are duplicated in the VLAN_OBJECT_MASK
		"insideVlans.networkVlan.id",
		"insideVlans.networkVlan.name",
		"insideVlans.networkVlan.primaryRouter.hostname",
		"insideVlans.networkVlan.vlanNumber",	
		// below properties are duplicated in the SUBNET_OBJECT_MASK
		"insideVlans.networkVlan.subnets.networkIdentifier",
		"insideVlans.networkVlan.subnets.cidr",
		"insideVlans.networkVlan.subnets.broadcastAddress",
		"insideVlans.networkVlan.subnets.gateway",
		"insideVlans.networkVlan.subnets.subnetType"		
		*/
	};
	
	private final static String[] VLAN_OBJ_MASK_FRAGMENT = {
		"id",
		"name",
		"vlanNumber", 
		"primaryRouter.hostname",
		"networkSpace",
		"networkComponentTrunks.networkComponent.id",
		"networkComponentTrunks.networkComponent.downlinkComponent.id",
		"networkComponentTrunks.networkComponent.downlinkComponent.name",
		"networkComponentTrunks.networkComponent.downlinkComponent.port",
		"networkComponentTrunks.networkComponent.downlinkComponent.hardware.networkGatewayMemberFlag",
		"networkComponentTrunks.networkComponent.downlinkComponent.hardware.hostname",
		"networkComponentTrunks.networkComponent.downlinkComponent.hardware.primaryBackendIpAddress",
		/*
		// below properties are duplicated in the SUBNET_OBJECT_MASK
		"subnets.id",
		"subnets.networkIdentifier",
		"subnets.cidr",
		"subnets.broadcastAddress",
		"subnets.gateway",
		"subnets.subnetType",
		*/
		// below property is for the parent object
		"attachedNetworkGateway.id"
		
	};
	
	private final static String[] SUBNET_OBJ_MASK_FRAGMENT = {
		"id",
		"networkIdentifier",
		"cidr",
		"broadcastAddress",
		"gateway",
		"subnetType",
		// below property is for the parent object
		//"networkVlan.id"
	};

	private static final String[] HW_OBJ_MASK_FRAGMENT = {
		"id",
		"hostname",
		"networkComponents.id",
		"networkComponents.name",
		"networkComponents.port",
		"networkComponents.uplinkComponent.id",
		"networkComponents.uplinkComponent.networkVlan.id",
		"networkComponents.uplinkComponent.networkVlan.networkSpace",
		"networkComponents.uplinkComponent.networkVlanTrunks.networkVlanId",
    };
	
	private final static String[] GATEWAY_OBJECT_MASK;
	private final static String[] VLAN_OBJECT_MASK;
	private final static String[] SUBNET_OBJECT_MASK = SUBNET_OBJ_MASK_FRAGMENT;
	private final static String[] HW_OBJ_MASK = HW_OBJ_MASK_FRAGMENT;
	
	static {
		// build up the object masks
		final List<String> vlanObjMaskTemp = new ArrayList<String>(Arrays.asList(VLAN_OBJ_MASK_FRAGMENT));
		final List<String> gatewayObjMaskTemp = new ArrayList<String>(Arrays.asList(GATEWAY_OBJ_MASK_FRAGMENT));
		
		// querying VLANs and Gateways will get you subnet details
		for (final String subnetObjMask : SUBNET_OBJ_MASK_FRAGMENT) {
			vlanObjMaskTemp.add(String.format("subnets.%s", subnetObjMask));
			gatewayObjMaskTemp.add(String.format("insideVlans.networkVlan.subnets.%s", subnetObjMask));
		}
		
		// querying Gateways will get you VLAN details
		for (final String vlanObjMask : VLAN_OBJ_MASK_FRAGMENT) {
			gatewayObjMaskTemp.add(String.format("insideVlans.networkVlan.%s", vlanObjMask));
		}
		
		GATEWAY_OBJECT_MASK = gatewayObjMaskTemp.toArray(new String[] {});
		VLAN_OBJECT_MASK = vlanObjMaskTemp.toArray(new String[] {});
	}
	
	private void addVyattaToWhitelist(JsonObject slGatewayObj) throws CPEException {
    	// before we connect to the Vyatta, we need to whitelist the subnet it's on in our management vyatta
    	final String privateIp = JsonUtil.getStringFromPath(slGatewayObj, "privateIpAddress.ipAddress");
    	final JsonArray subnetArr = JsonUtil.getArrayFromPath(slGatewayObj, "privateVlan.subnets");

    	final Set<String> subnets = new HashSet<String>();
    	for (final JsonElement subnetElem : subnetArr) {
    		final String network = JsonUtil.getStringFromPath(subnetElem, "networkIdentifier");
    		final int cidr = JsonUtil.getIntFromPath(subnetElem, "cidr");

    		if (!IPAddressUtil.isIpInNetwork(privateIp, network, cidr)) {
    			// if the gateway is not in this subnet, skip it
    			continue;
    		}
    		
    		subnets.add(String.format("%s/%d", network, cidr));
    	}
    	
    	if (subnets.isEmpty()) {
    		// do nothing (?) -- how is this possible, vyatta itself is not on any of its vlan's subnets
    		logger.warn(String.format("Strange occurrence: Vyatta Gateway %s is not on any of its private VLAN's subnets!", privateIp));
    		return;
    	}

    	try {
    		final List<Gateway> mgmtGWs = this.getGateways(GatewayType.MANAGEMENT);
    		for (final Gateway mgmtGW : mgmtGWs) {
    			// add subnets to all management gateways
				firewallService.allowManagementSubnets(mgmtGW, subnets);
    		}
		} catch (CPEException e) {
			new NagiosEventLogger().emitCritical("NAGIOSMONITOR VYATTACONFIG :"+e.getLocalizedMessage());
			throw e;
		}
	}

	/* (non-Javadoc)
	 * @see com.ibm.scas.analytics.backend.NetworkProvider#addGateway(com.ibm.scas.analytics.beans.Gateway)
	 */
	@Override
	public String addGateway(Gateway gatewayReq) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("addGateway(): %s", ReflectionToStringBuilder.toString(gatewayReq)));
		}
		
		// Validate the type of gatewayReq passed in
		final GatewayType gwType = gatewayReq.getType();
		if (gwType == null) { //TODO : condition to check gateway is in active state
			throw new CPEParamException("gateway.type");
		}
		
		/* Validate if username is set, but not password.  Otherwise, use the
		   values in SoftLayer API */
		if (gatewayReq.getUsername() != null &&
			gatewayReq.getPassword() == null) {
			throw new CPEParamException("gateway.password");
		}
		
		// this should be a nested structure like getSoftLayerAccount().getId(), i.e. "softlayerAccount": { "id": "xxxxx" }
		if (gatewayReq.getSoftLayerAccount() == null) {
			throw new CPEParamException("gateway.softlayerAccount");
		}

		if (gatewayReq.getSoftLayerAccount().getId() == null) {
			throw new CPEParamException("gateway.softlayerAccount.id");
		}
		
    	final CPELocation myLocation = persistence.getObjectById(CPELocation.class, cpeLocationName);
    	if (myLocation == null) {
    		throw new CPEException(String.format("Could not find location record: %s", cpeLocationName));
    	}	
	
		// validate the softlayer account id passed in exists in our database
		final com.ibm.scas.analytics.persistence.beans.SoftLayerAccount slAcct = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.SoftLayerAccount.class, gatewayReq.getSoftLayerAccount().getId());
		if (slAcct == null) {
			throw new CPEException(String.format("The SoftLayer Account with id %s was not found in the database", gatewayReq.getSoftLayerAccount().getId()));
		}

		// validate the account, if one was passed in
		Account account = null;
		if (gatewayReq.getAccount() != null) {
			if (gatewayReq.getAccount().getId() == null) {
				throw new CPEParamException("gateway.account.id");
			}
			
			if (gatewayReq.getType() != GatewayType.DEDICATED) {
				throw new CPEParamException(String.format("Cannot assign %s gateway to a account", gatewayReq.getType().toString()));
			}
			
			account = persistence.getObjectById(Account.class, gatewayReq.getAccount().getId()); 
			if (account == null) {
				throw new CPEException(String.format("The account with id %s was not found in the database", gatewayReq.getAccount().getId()));
			}
		}
		
		// validate the gateway actually exists in SoftLayer.  There should be a single gateway
		final SoftLayerAPIGateway slg = new SoftLayerAPIGateway(slAcct);
		final SoftLayerObjectFilter objFilter = new SoftLayerObjectFilter();
		
		// two ways of getting gateways from softlayer, either name, or id.  at least one must be filled in.
		if (gatewayReq.getName() != null) {
			objFilter.setPropertyFilter("networkGateways.name", gatewayReq.getName());
		} else if (gatewayReq.getSoftLayerId() != null) {
			objFilter.setPropertyFilter("networkGateways.id", gatewayReq.getSoftLayerId());
		} else {
			throw new CPEParamException("At least one of gateway.name or gateway.softLayerId must be passed");
		}
		
		// ask SoftLayer for the gateway Id
		final JsonElement slGatewayArrElem = slg.callGetMethod("SoftLayer_Account", "getNetworkGateways", objFilter, GATEWAY_OBJECT_MASK);
		if (slGatewayArrElem == null) {
			throw new CPEException(String.format("Could not find gateway!"));
		}
		
    	final JsonArray arr = slGatewayArrElem.getAsJsonArray();
    	if (arr.size() == 0) {
			throw new CPEException(String.format("Could not find gateway!"));
    	}
	
 		// Set field values from the req using the conversion util
		final com.ibm.scas.analytics.persistence.beans.Gateway gateway = BeanConversionUtil.convertToRecord(gatewayReq);
		
		// clear out the gateway members; we will build it ourselves from the  SL API and fill in
		// any passed in user/passwords
		gateway.setGatewayMembers(new ArrayList<GatewayMember>());
		
		JsonObject slGatewayObj = null;
    	for (final JsonElement elem : arr) {
    		slGatewayObj = elem.getAsJsonObject();
			final JsonElement idElem = slGatewayObj.get("id");
			
			final int gatewayId = idElem.getAsInt();
			// set the gateway ID in our database record
			gateway.setSoftLayerId(String.valueOf(gatewayId));
			
			// we're just grabbing the first id from the list.  hopefully the name/id was unique in SL
			break;
    	}
		
    	if (slGatewayObj == null) {
			throw new CPEException(String.format("Could not find gateway!"));
    	}
    	
    	// make sure that the gateway's not already in the database
    	final List<com.ibm.scas.analytics.persistence.beans.Gateway> existingGWs = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Gateway.class, new WhereClause("softLayerId", gateway.getSoftLayerId()));
    	if (!existingGWs.isEmpty()) {
			throw new CPEException(String.format("Gateway with SoftLayer ID %s already exists as: %s!", gateway.getSoftLayerId(), existingGWs.get(0).getId()));
    	}
    	
    	// before we connect SHARED or DEDICATED Vyatta, we need to whitelist the subnet it's on in our management vyatta
    	if (gatewayReq.getType() != GatewayType.MANAGEMENT) {
    		this.addVyattaToWhitelist(slGatewayObj);
    	}
   	
    	if ((gatewayReq.getUsername() != null && gatewayReq.getPassword() != null) ||
			(gatewayReq.getGatewayMembers() != null && !gatewayReq.getGatewayMembers().isEmpty())) {
    		final JsonArray membersArr = JsonUtil.getArrayFromPath(slGatewayObj, "members");
    		for (final JsonElement memberElem : membersArr) {
    			// validate the username/password that is passed in using the Vyatta REST API.  First, collect all the
    			// SSL certificates
    			final String privateIp = JsonUtil.getStringFromPath(memberElem, "hardware.primaryBackendIpAddress");

    			final GatewayMember gwMember = new GatewayMember();
    			gwMember.setMemberIp(privateIp);
    			// invalid SSL Cert -- these need to be filled in later
    			gwMember.setSslCert("");

    			if (gatewayReq.getUsername() != null && gatewayReq.getPassword() != null) {
    				// both members have the same username and password as passed in the gatewayReq
    				gwMember.setUsername(gatewayReq.getUsername());
    				gwMember.setPassword(gatewayReq.getPassword()); 
    			} else if (gatewayReq.getGatewayMembers() != null && !gatewayReq.getGatewayMembers().isEmpty()) {
    				// check if caller passed in members array with username/password
    				for (final com.ibm.scas.analytics.beans.GatewayMember reqMember : gatewayReq.getGatewayMembers()) {
    					if (!reqMember.getMemberIp().equals(privateIp)) {
    						continue;
    					}

    					if (reqMember.getUsername() == null || reqMember.getPassword() == null) {
    						continue;
    					}
    					
						// save the password if it's passed in
						gwMember.setUsername(reqMember.getUsername());
						gwMember.setPassword(reqMember.getPassword());
    					break;
    				}
    			} 

    			gateway.getGatewayMembers().add(gwMember);
    		}
    	}
		
		gateway.setSoftLayerAccount(slAcct);
		gateway.setAccount(account);
		gateway.setLocation(myLocation);
		gateway.setType(gatewayReq.getType().name());
		
		persistence.saveObject(com.ibm.scas.analytics.persistence.beans.Gateway.class, gateway);
	
		logger.info(String.format("addGateway(): New Gateway %s added to database with id %s.", JsonUtil.getStringFromPath(slGatewayObj, "name"), gateway.getId()));
		return gateway.getId();
	}
	
	private <T> Map<String, T> getObjectBySoftLayerIdMap(Class<T> persistenceType, List<String> softLayerIds) throws CPEException {
		final Map<String, T> softLayerIdMap = new HashMap<String, T>();
		
		if (softLayerIds.isEmpty()) {
			return softLayerIdMap;
		}
		
		try {
			final List<T> objects = persistence.getObjectsBy(persistenceType, new WhereInClause("softLayerId", softLayerIds));
			
			for (final T obj : objects) {
				final Method slMethod = obj.getClass().getMethod("getSoftLayerId");
				final String slId = (String) slMethod.invoke(obj);
				
				softLayerIdMap.put(slId, obj);
			}
		} catch (PersistenceException e) {
			throw new CPEException(e);
		} catch (IllegalAccessException e) {
			throw new CPEException(e);
		} catch (IllegalArgumentException e) {
			throw new CPEException(e);
		} catch (InvocationTargetException e) {
			throw new CPEException(e);
		} catch (NoSuchMethodException e) {
			throw new CPEException(e);
		} catch (SecurityException e) {
			throw new CPEException(e);
		}
		
		return softLayerIdMap;
	}
	
	private void setDBRecsForGatewayChildren(List<Gateway> gatewayDetails) throws CPEException {
		// grab vlan and subnet ids from the database by softlayer id
		final List<String> vlanSLIds = new ArrayList<String>();
		final List<String> subnetSLIds = new ArrayList<String>();
		for (final Gateway gw : gatewayDetails) {
			for (final SoftLayerIdObject vlanIdObj : gw.getAssociatedVlans()) {
				vlanSLIds.add(vlanIdObj.getSoftLayerId());
				
				if (vlanIdObj instanceof Vlan) {
					final Vlan vlan = (Vlan)vlanIdObj;
					for (final SoftLayerIdObject subnetIdObj : vlan.getSubnets()) {
						subnetSLIds.add(subnetIdObj.getSoftLayerId());
					}
				}
			}
		}
		
		final Map<String, com.ibm.scas.analytics.persistence.beans.Vlan> vlanSLIdToDBIdMap = this.getObjectBySoftLayerIdMap(com.ibm.scas.analytics.persistence.beans.Vlan.class, vlanSLIds);
		final Map<String, com.ibm.scas.analytics.persistence.beans.Subnet> subnetSLIdToDBIdMap = this.getObjectBySoftLayerIdMap(com.ibm.scas.analytics.persistence.beans.Subnet.class, subnetSLIds);
		
		for (final Gateway gw : gatewayDetails) {
			for (final SoftLayerIdObject vlanIdObj : gw.getAssociatedVlans()) {
				final com.ibm.scas.analytics.persistence.beans.Vlan vlanObj = vlanSLIdToDBIdMap.get(vlanIdObj.getSoftLayerId());
				if (vlanObj == null) {
					// vlan is not in the DB (e.g. management vlans)
					continue;
				}
				
				vlanIdObj.setId(vlanObj.getId());
				
				if (vlanIdObj instanceof Vlan) {
					final Vlan vlan = (Vlan)vlanIdObj;
					
					for (final SoftLayerIdObject subnetIdObj : vlan.getSubnets()) {
						subnetIdObj.setId(subnetSLIdToDBIdMap.get(subnetIdObj.getSoftLayerId()).getId());
					}
				}
			}
		}
	}
	
	private void setDBIdsForVlanChildren(List<Vlan> vlanDetails) throws CPEException {
		// grab subnet ids from the database by softlayer id
		final List<String> subnetSLIds = new ArrayList<String>();
		for (final Vlan vlan : vlanDetails) {
			logger.debug("Vlan Object : " + ReflectionToStringBuilder.toString(vlan));
			for (final SoftLayerIdObject subnetIdObj : vlan.getSubnets()) {
				subnetSLIds.add(subnetIdObj.getSoftLayerId());
			}
		}
		
		final Map<String, com.ibm.scas.analytics.persistence.beans.Subnet> subnetSLIdToDBIdMap = this.getObjectBySoftLayerIdMap(com.ibm.scas.analytics.persistence.beans.Subnet.class, subnetSLIds);
		
		for (final Vlan vlan : vlanDetails) {
			for (final SoftLayerIdObject subnetIdObj : vlan.getSubnets()) {
				subnetIdObj.setId(subnetSLIdToDBIdMap.get(subnetIdObj.getSoftLayerId()).getId());
			}
		}
	}
	
	/**
	 * Helper to merge softlayer details with gateway details
	 * @param gatewayRecs
	 * @return
	 * @throws CPEException
	 */
	protected List<Gateway> getGatewaysFromGatewayRecords(List<com.ibm.scas.analytics.persistence.beans.Gateway> gatewayRecs) throws CPEException {
		if (gatewayRecs.isEmpty()) {
			return Collections.emptyList();
		}
		
		final List<Gateway> gatewayDetails = new ArrayList<Gateway>(gatewayRecs.size());
		
		final Map<String, List<String>> softLayerIdMap = new HashMap<String, List<String>>(); // map objects by softlayer account
		final Map<String, Gateway> gatewayIdMap = new HashMap<String, Gateway>();			  // map gateways by softlayer id
		
		for (final com.ibm.scas.analytics.persistence.beans.Gateway g : gatewayRecs) {
			final Gateway gw = BeanConversionUtil.convertToBean(g);
			gatewayDetails.add(gw);
			
			// Map these so we can add SoftLayer details
			gatewayIdMap.put(g.getSoftLayerId(), gw);
			CollectionsUtil.addToMap(softLayerIdMap, g.getSoftLayerAccount().getId(), g.getSoftLayerId());
		}
		
		for (final Entry<String, List<String>> softLayerId : softLayerIdMap.entrySet()) {
			final com.ibm.scas.analytics.persistence.beans.SoftLayerAccount slAcct = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.SoftLayerAccount.class, softLayerId.getKey());
			final List<String> gatewayIdList = softLayerId.getValue();
			final SoftLayerAPIGateway slg = new SoftLayerAPIGateway(slAcct);
			
			// build object filter
			final SoftLayerObjectFilter objFilter = new SoftLayerObjectFilter();
			objFilter.setPropertyFilter("networkGateways.id", new HashSet<String>(gatewayIdList));

			// add the details from the SoftLayer API
			final JsonElement slGatewayArrElem = slg.callGetMethod("SoftLayer_Account", "getNetworkGateways", objFilter, GATEWAY_OBJECT_MASK);
			if (slGatewayArrElem == null || !slGatewayArrElem.isJsonArray()) {
				throw new CPEException(String.format("Cannot retrieve list of gateways from SoftLayer Account with ID: %s, ID list: %s", softLayerId.getKey(), gatewayIdList));
			}
			
			final JsonArray slGatewayArr = slGatewayArrElem.getAsJsonArray();
			for (final JsonElement slGatewayObjElem : slGatewayArr) {
				final JsonObject slGatewayObj = slGatewayObjElem.getAsJsonObject();
				final String slGatewayId = JsonUtil.getStringFromPath(slGatewayObj, "id");
				
				// decorate the gateway object with SoftLayer object
				addSLGatewayDetails(gatewayIdMap.get(slGatewayId), slGatewayObj);
			}
		}
		
		// fill in DB IDs for VLAN and subnet objects
		setDBRecsForGatewayChildren(gatewayDetails);
		
		for (final Gateway gw : gatewayDetails) {
			// avoid circular references
			replaceChildObjectParentRefs(gw);
		}
		
		return gatewayDetails;	
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.scas.analytics.backend.NetworkProvider#getAllGateways()
	 */
	@Override
	public List<Gateway> getAllGateways() throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace("getAllGateways()");
		}
		
		final List<com.ibm.scas.analytics.persistence.beans.Gateway> allGateways = persistence.getAllObjects(com.ibm.scas.analytics.persistence.beans.Gateway.class);
		if (allGateways.size() == 0) {
			return Collections.emptyList();
		}
		
		return getGatewaysFromGatewayRecords(allGateways);

	}
	
	private static void addSLGatewayDetails(Gateway gatewayObj, JsonObject slGatewayObj) {
		if (slGatewayObj.has("name")) {
			gatewayObj.setName(JsonUtil.getStringFromPath(slGatewayObj, "name"));
		}
		if (slGatewayObj.has("privateIpAddress")) {
			gatewayObj.setPrivateIpAddress(JsonUtil.getStringFromPath(slGatewayObj, "privateIpAddress.ipAddress"));
		}
		if (slGatewayObj.has("publicIpAddress")) {
			gatewayObj.setPublicIpAddress(JsonUtil.getStringFromPath(slGatewayObj, "publicIpAddress.ipAddress"));
		}
		
		if (slGatewayObj.has("status")) {
			final GatewayStatus status = GatewayStatus.valueOf(JsonUtil.getStringFromPath(slGatewayObj, "status.keyName"));
			// if nothing's happening, this is "ACTIVE"
			gatewayObj.setStatus(status);
		}	
		
		if (slGatewayObj.has("members")) {
			final JsonArray memberArray = JsonUtil.getArrayFromPath(slGatewayObj, "members");
			for (final JsonElement memberElem : memberArray) {
				final Integer activeTxnCount = JsonUtil.getIntFromPath(memberElem, "hardware.activeTransactionCount");
				if (activeTxnCount != null && activeTxnCount > 0 && gatewayObj.getStatus() == GatewayStatus.ACTIVE) {
					// if one of my members has an active transaction on it, then set it to "UPDATING" and
					// I can't use the gateway
					gatewayObj.setStatus(GatewayStatus.UPDATING);
				}
				
				if (gatewayObj.getGatewayMembers() == null) {
					gatewayObj.setGatewayMembers(new HashSet<com.ibm.scas.analytics.beans.GatewayMember>());
				}
				
				final String memberIP = JsonUtil.getStringFromPath(memberElem, "hardware.primaryBackendIpAddress");
				final int memberPriority = JsonUtil.getIntFromPath(memberElem, "priority");
				
				// find the member with the matching memberIP, if it exists
				com.ibm.scas.analytics.beans.GatewayMember existingMember = null;
				if (gatewayObj.getGatewayMembers() != null && !gatewayObj.getGatewayMembers().isEmpty()) {
					for (final com.ibm.scas.analytics.beans.GatewayMember member : gatewayObj.getGatewayMembers()) {
						if (!member.getMemberIp().equals(memberIP)) {
							continue;
						}

						existingMember = member;
						break;
					}
				}
				
				if (existingMember == null) {
					// add the member using details from SL (not store in our DB
					existingMember = new com.ibm.scas.analytics.beans.GatewayMember();
					existingMember.setMemberIp(memberIP);
					gatewayObj.getGatewayMembers().add(existingMember);
				}
				
				existingMember.setPriority(memberPriority);
				if (existingMember.getUsername() == null || existingMember.getPassword() == null) {
					// fill in username and password from SL API
					final JsonArray passwordsArr = JsonUtil.getArrayFromPath(memberElem, "hardware.operatingSystem.passwords");
					for (final JsonElement passwordElem : passwordsArr) {
						final String username = JsonUtil.getStringFromPath(passwordElem, "username");
						final String password = JsonUtil.getStringFromPath(passwordElem, "password");

						if (username.equals("root")) {
							// cannot connect to REST API as root
							continue;
						}

						existingMember.setUsername(username);
						existingMember.setPassword(password); 
					}

				}
			}
		}
	
		if (gatewayObj.getPrimaryFrontendRouter() == null) {
			// get this property from the public vlan
			if (slGatewayObj.has("publicVlan")) {
				final String frontendRouter = JsonUtil.getStringFromPath(slGatewayObj, "publicVlan.primaryRouter.hostname");
				gatewayObj.setPrimaryFrontendRouter(frontendRouter);
			}
		}
		
		if (gatewayObj.getPrimaryBackendRouter() == null) {
			// get this property from the private vlan
			if (slGatewayObj.has("privateVlan")) {
				final String backendRouter = JsonUtil.getStringFromPath(slGatewayObj, "privateVlan.primaryRouter.hostname");
				gatewayObj.setPrimaryBackendRouter(backendRouter);
			}
		}	

		// collect vlan IDs from SoftLayer and merge with details from our DB
		if (slGatewayObj.has("insideVlans")) {
			final JsonArray vlanArr = JsonUtil.getArrayFromPath(slGatewayObj, "insideVlans");
			final List<SoftLayerIdObject> vlans = new ArrayList<SoftLayerIdObject>(vlanArr.size());
			for (final JsonElement vlanElem : vlanArr) {
				final JsonObject slNetworkGwVlanObj = vlanElem.getAsJsonObject();
				final JsonObject slVlanObj = JsonUtil.getObjFromPath(slNetworkGwVlanObj, "networkVlan");
				final Vlan vlan = new Vlan();

				addSLVlanDetails(vlan, slVlanObj);

				// just set an ID object to avoid infinite recursion
				if (vlan.getGateway() == null) {
					vlan.setGateway(new SoftLayerIdObject());
				}
				vlan.getGateway().setId(gatewayObj.getId());
				vlan.getGateway().setSoftLayerId(gatewayObj.getSoftLayerId());

				vlans.add(vlan);
			}

			gatewayObj.setAssociatedVlans(vlans);
		} else {
			gatewayObj.setAssociatedVlans(new ArrayList<SoftLayerIdObject>());
		}
		
		if (slGatewayObj.has("groupNumber")) {
			gatewayObj.setGroupNumber(JsonUtil.getIntFromPath(slGatewayObj, "groupNumber"));
		}

	}
	
	private static void addSLVlanDetails(Vlan vlanObj, JsonObject slVlanObj) {
		vlanObj.setSoftLayerId(String.valueOf(slVlanObj.get("id").getAsInt()));
		if (slVlanObj.has("name")) {
			vlanObj.setName(JsonUtil.getStringFromPath(slVlanObj, "name"));
		}
		if (slVlanObj.has("primaryRouter")) {
			vlanObj.setPrimaryRouter(JsonUtil.getStringFromPath(slVlanObj, "primaryRouter.hostname"));
		}
		if (slVlanObj.has("vlanNumber")) {
			vlanObj.setVlanNumber(String.valueOf(JsonUtil.getIntFromPath(slVlanObj, "vlanNumber")));
		}
		if (slVlanObj.has("networkSpace")) {
			vlanObj.setNetworkSpace(VlanNetworkSpace.valueOf(JsonUtil.getStringFromPath(slVlanObj, "networkSpace")));
		}
		
		if (slVlanObj.has("networkComponentTrunks")) {
			final JsonArray vlanTrunkArr = JsonUtil.getArrayFromPath(slVlanObj, "networkComponentTrunks");
			if (vlanTrunkArr != null) {
				for (final JsonElement vlanTrunk : vlanTrunkArr) {
					final JsonElement downlinkCompElem = JsonUtil.getObjFromPath(vlanTrunk, "networkComponent.downlinkComponent");
					
					if (downlinkCompElem == null) {
						final int networkCompId = JsonUtil.getIntFromPath(vlanTrunk, "networkComponent.id");
						logger.warn("Network Component with ID " + networkCompId + " does not have an associated downlinkComponent in SoftLayer.  This indicates" +
								" that the VLAN with ID " + vlanObj.getSoftLayerId() + " (" + vlanObj.getPrimaryRouter() + "." + vlanObj.getVlanNumber() + ")" +
								" may be trunked on some hardware that the SoftLayer account does not have access to.");
						continue;
					}
					
					final String portName = JsonUtil.getStringFromPath(vlanTrunk, "networkComponent.downlinkComponent.name");
					final int portNum = JsonUtil.getIntFromPath(vlanTrunk, "networkComponent.downlinkComponent.port");
					final String hostname = JsonUtil.getStringFromPath(vlanTrunk, "networkComponent.downlinkComponent.hardware.hostname");
					final String hostIPAddr = JsonUtil.getStringFromPath(vlanTrunk, "networkComponent.downlinkComponent.hardware.primaryBackendIpAddress");
					final Boolean isNetworkGateway = JsonUtil.getBooleanFromPath(vlanTrunk, "networkComponent.downlinkComponent.hardware.networkGatewayMemberFlag");
					
					if (isNetworkGateway != null && isNetworkGateway) {
						// don't list network gateways
						continue;
					}
					vlanObj.getVlanTrunks().add(new VlanTrunk(hostname, hostIPAddr, String.format("%s%d", portName, portNum)));
				}
			}
		}
	
		if (slVlanObj.has("subnets")) {
			final JsonArray subnetArr = JsonUtil.getArrayFromPath(slVlanObj, "subnets");
			if (subnetArr != null) {

				final List<SoftLayerIdObject> subnets = new ArrayList<SoftLayerIdObject>(subnetArr.size());
				for (final JsonElement subnetElem : subnetArr) {
					final JsonObject slSubnetObj = subnetElem.getAsJsonObject();
					final Subnet subnet = new Subnet();
					addSLSubnetDetails(subnet, slSubnetObj);

					// parent objects of children objects do not have full details
					subnet.setVlan(new SoftLayerIdObject());
					subnet.getVlan().setSoftLayerId(vlanObj.getSoftLayerId());
					subnet.getVlan().setId(vlanObj.getId());

					subnets.add(subnet);
				}
				vlanObj.setSubnets(subnets);
			}
		}
		
		/*
		if (slVlanObj.has("attachedNetworkGateway")) {
			// set the parent objects' softlayer ID if available
			final JsonObject gatewayObj = JsonUtil.getObjFromPath(slVlanObj, "attachedNetworkGateway");
			if (gatewayObj != null) {
				if (vlanObj.getGateway() == null) {
					vlanObj.setGateway(new Gateway());
				}
				vlanObj.getGateway().setSoftLayerId(String.valueOf(gatewayObj.getAsJsonObject().get("id").getAsInt()));
				addSLGatewayDetails((Gateway)vlanObj.getGateway(), gatewayObj.getAsJsonObject());
			}
		}
		*/
	}
	
	private static void addSLSubnetDetails(Subnet subnetObj, JsonObject slSubnetObj) {
		subnetObj.setSoftLayerId(String.valueOf(slSubnetObj.get("id").getAsInt()));
		if (slSubnetObj.has("networkIdentifier")) {
			subnetObj.setNetworkAddr(JsonUtil.getStringFromPath(slSubnetObj, "networkIdentifier"));
		}
		if (slSubnetObj.has("cidr")) {
			subnetObj.setCidr(JsonUtil.getIntFromPath(slSubnetObj, "cidr"));
		}
		if (slSubnetObj.has("broadcastAddress")) {
			subnetObj.setBroadcastAddr(JsonUtil.getStringFromPath(slSubnetObj, "broadcastAddress"));
		}
		if (slSubnetObj.has("gateway")) {
			subnetObj.setGatewayAddr(JsonUtil.getStringFromPath(slSubnetObj, "gateway"));
		}
		if (slSubnetObj.has("subnetType")) {
			subnetObj.setType(JsonUtil.getStringFromPath(slSubnetObj, "subnetType"));
		}
		
		// set the parent objects' softlayer ID if available
		if (slSubnetObj.has("networkVlan")) {
			final JsonObject vlanObj = JsonUtil.getObjFromPath(slSubnetObj, "networkVlan");
			if (vlanObj != null) {
				if (subnetObj.getVlan() == null) {
					subnetObj.setVlan(new Vlan());
				}
				subnetObj.getVlan().setSoftLayerId(String.valueOf(JsonUtil.getIntFromPath(vlanObj, "id")));
				addSLVlanDetails((Vlan)subnetObj.getVlan(), vlanObj);
			}
		}
	}

    /* (non-Javadoc)
	 * @see com.ibm.scas.analytics.backend.NetworkProvider#getGatewayById(java.lang.String)
	 */
	@Override
	public Gateway getGatewayById(String gatewayId) throws CPEException {

		if (logger.isTraceEnabled()) {
			logger.trace("getGatewayDetailsById(): " + gatewayId);
		}

		final com.ibm.scas.analytics.persistence.beans.Gateway gateway = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Gateway.class, gatewayId.toString());
		if (gateway == null) {
			return null;
		}
	
		return getGatewaysFromGatewayRecords(Arrays.asList(gateway)).get(0);
	}

	/* (non-Javadoc)
	 * @see com.ibm.scas.analytics.backend.NetworkProvider#modifyGateway(java.lang.String, com.ibm.scas.analytics.beans.Gateway)
	 */
	@Override
	public void updateGatewayAccount(String gatewayId, String accountId) throws CPEException {
		// note: caller starts the transaction
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("updateGatewayAccount(): gatewayId: %s, accountId: %s", gatewayId, accountId));
		}

		final com.ibm.scas.analytics.persistence.beans.Gateway gateway = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Gateway.class, gatewayId);
		if (gateway == null) {
			throw new CPEException("Gateway : " + gatewayId + " does not exist in the database");
		}
		
	
		if (accountId == null) {
			throw new CPEParamException("accountId cannot be null, use unassignGateway() to unassign account");
		}
		
		// validate the subscriber
		final Account account = persistence.getObjectById(Account.class, accountId);
		if (account == null) {
			throw new CPEParamException(String.format("Account %s not found in database", accountId));
		}
		
		// make sure the gateway isn't busy
		final Gateway gwObj = this.getGatewaysFromGatewayRecords(Arrays.asList(gateway)).get(0);
		final GatewayType gwType = GatewayType.valueOf(gateway.getType());
		if (gwType != GatewayType.DEDICATED) { 
			throw new CPEException(String.format("Gateway %s (%s) is not of type DEDICATED", gatewayId, gwObj.getName()));
		}
		
		if (gwObj.getStatus() != GatewayStatus.ACTIVE) {
			throw new CPEParamException(String.format("Gateway %s (%s) is not active.", gwObj.getId(), gwObj.getName()));
		}
		
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("updateGatewayAccount(): gateway to modify: %s", ReflectionToStringBuilder.toString(gateway)));
		}
		
		
		gateway.setAccount(account);
		
		//modify subscriber details condition
		final List<com.ibm.scas.analytics.persistence.beans.Vlan> associatedVlans = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Vlan.class, new WhereClause("gateway.id", gatewayId));
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("updateGatewayAccount(): Associated VLANs to gateway %s: %s", gatewayId, ReflectionToStringBuilder.toString(associatedVlans)));
		}
	
		//assign subscriber condition -- all VLANs become owned by subscriber
		if (associatedVlans != null && !associatedVlans.isEmpty()) {
			for (final com.ibm.scas.analytics.persistence.beans.Vlan vlan : associatedVlans){
				try {
					verifyVlanUsedByCluster(vlan.getId());
				} catch (CPEException e) {
        			throw new CPEException(String.format("Cannot modify gateway %s because associated vlan %s in use: %s", gatewayId, vlan.getId(), e.getLocalizedMessage()), e);
				}

				persistence.updateObject(com.ibm.scas.analytics.persistence.beans.Vlan.class, vlan);
			}
		}
		
		persistence.updateObject(com.ibm.scas.analytics.persistence.beans.Gateway.class, gateway);
	}

    /* (non-Javadoc)
	 * @see com.ibm.scas.analytics.backend.NetworkProvider#deleteGateway(java.lang.String)
	 */
	@Override
	public void deleteGateway(String gatewayId) throws CPEException {
		// note: caller starts the transaction
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("deleteGateway(): %s", gatewayId));
		}

		final com.ibm.scas.analytics.persistence.beans.Gateway gateway = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Gateway.class, gatewayId);

		if (gateway == null) {
			throw new CPEParamException(String.format("Gateway %s doesn't exist in the database for deletion", gatewayId));
		}
		
		final Gateway gwObj = this.getGatewaysFromGatewayRecords(Arrays.asList(gateway)).get(0);
		final List<com.ibm.scas.analytics.persistence.beans.Vlan> associatedVlans = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Vlan.class, new WhereClause("gateway.id", gatewayId));
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("deleteGateway(): Associated VLANs to gateway %s (%s): %s", gatewayId, gwObj.getName(), ReflectionToStringBuilder.toString(associatedVlans)));
		}
		
		/* if the gateway type is not MANAGEMENT, we do not allow deleting a gateway with VLANs attach.  detach the VLANs before 
		 * deleting the gateway.  this is because VLAN detach is asynchronous so we cannot do every step here synchronously
		 */
		if (gwObj.getType() != GatewayType.MANAGEMENT &&
			!gwObj.getAssociatedVlans().isEmpty()) {
			
			final Set<String> vlanIds = new HashSet<String>(gwObj.getAssociatedVlans().size());
			final Set<String> vlanNames = new HashSet<String>(gwObj.getAssociatedVlans().size());
			for (final SoftLayerIdObject vlanIdObj : gwObj.getAssociatedVlans()) {
				vlanIds.add(vlanIdObj.getId());
				
				final Vlan vlanObj = (Vlan)vlanIdObj;
				vlanNames.add(String.format("%s.%s", vlanObj.getPrimaryRouter(), vlanObj.getVlanNumber()));
			}
			
			throw new CPEException(String.format("Cannot delete gateway %s (%s) of type %s while there are attached VLANs %s (%s).  Detach them first.", gwObj.getId(), gwObj.getName(), gwObj.getType(), vlanIds, vlanNames));
		}
	
        if (associatedVlans != null && !associatedVlans.isEmpty()) {       
        	for (final com.ibm.scas.analytics.persistence.beans.Vlan vlan : associatedVlans){
        		try {
        			verifyVlanUsedByCluster(vlan.getId());
        		} catch (CPEException e) {
        			// this is strange.  management gateway has clusters on the VLAN.  
        			throw new CPEException(String.format("Cannot delete gateway %s because associated vlan %s in use: %s", gatewayId, vlan.getId(), e.getLocalizedMessage()), e);
        		}
       		 
        		// When removing the MANAGEMENT gateway, also remove the associated VLANs and Subnets.
        		persistence.deleteObjectById(com.ibm.scas.analytics.persistence.beans.Vlan.class, vlan.getId());
        		deleteSubnet(vlan.getId());
        	}
        }
        
        if(gwObj.getType() == GatewayType.DEDICATED)
        {
        	List<VPNTunnel> vpnTunnels = persistence.getObjectsBy(VPNTunnel.class, new WhereClause("gateway.id", gwObj.getId()));
        	if(vpnTunnels != null && vpnTunnels.size() > 0)
        	{
        		//There will only be single VPN Tunnel per dedicated gateway
        		persistence.deleteObject(VPNTunnel.class, vpnTunnels.get(0));
        	}
        }
        
		persistence.deleteObject(com.ibm.scas.analytics.persistence.beans.Gateway.class, gateway);
		logger.info(String.format("Gateway %s (%s) deleted. Return gateway to softlayer from portal if required", gatewayId, gwObj.getName()));
	}

	// VLAN CHANGES
	/**
	 * Helper method to merge VLAN DB details with SoftLayer details
	 * @param vlanRecs list of VLAN records from the database
	 * @return
	 * @throws CPEException
	 */
	private List<Vlan> getVlansFromVlanRecords(List<com.ibm.scas.analytics.persistence.beans.Vlan> vlanRecs) throws CPEException {
		if (vlanRecs.isEmpty()) {
			return Collections.emptyList();
		}
		
		final List<Vlan> allVlans = new ArrayList<Vlan>(vlanRecs.size());
		
		final Map<String, List<String>> softLayerIdMap = new HashMap<String, List<String>>();
		final Map<String, Vlan> vlanIdMap = new HashMap<String, Vlan>();	
		for (final com.ibm.scas.analytics.persistence.beans.Vlan vlanRec : vlanRecs) {
			final Vlan vlan = BeanConversionUtil.convertToBean(vlanRec);
			allVlans.add(vlan);
			
			CollectionsUtil.addToMap(softLayerIdMap, vlanRec.getSoftLayerAccount().getId(), vlanRec.getSoftLayerId());
			vlanIdMap.put(vlanRec.getSoftLayerId(), vlan);
		}
		
		for (final Entry<String, List<String>> softLayerId : softLayerIdMap.entrySet()) {
			final com.ibm.scas.analytics.persistence.beans.SoftLayerAccount slAcct = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.SoftLayerAccount.class, softLayerId.getKey());
			final List<String> vlanIdList = softLayerId.getValue();
			final SoftLayerAPIGateway slg = new SoftLayerAPIGateway(slAcct);
			
			// build object filter
			final SoftLayerObjectFilter objFilter = new SoftLayerObjectFilter();
			objFilter.setPropertyFilter("networkVlans.id", new HashSet<String>(vlanIdList));

			// add the details from the SoftLayer API
			final JsonElement slVlanArrElem = slg.callGetMethod("SoftLayer_Account", "getNetworkVlans", objFilter, VLAN_OBJECT_MASK);
			if (slVlanArrElem == null || !slVlanArrElem.isJsonArray()) {
				throw new CPEException(String.format("Cannot retrieve list of VLANs from SoftLayer Account with ID: %s, ID list: %s", softLayerId.getKey(), vlanIdList));
			}
			
			final JsonArray slVlanArr = slVlanArrElem.getAsJsonArray();
			for (final JsonElement slVlanObjElem : slVlanArr) {
				final JsonObject slVlanObj = slVlanObjElem.getAsJsonObject();
				final String slVlanId = JsonUtil.getStringFromPath(slVlanObj, "id");
				addSLVlanDetails(vlanIdMap.get(slVlanId), slVlanObj);
			}
		}
		
		setDBIdsForVlanChildren(allVlans);	
		
		for (final Vlan vlan : allVlans) {
			// ensure no circular reference
			replaceChildObjectParentRefs(vlan);
		}
		
		return allVlans;
	}

	/* (non-Javadoc)
	 * @see com.ibm.scas.analytics.backend.NetworkProvider#getAllVlans()
	 */
	@Override
	public List<Vlan> getAllVlans() throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace("getAllVlans()");
		}
		
		final List<com.ibm.scas.analytics.persistence.beans.Vlan> vlans = persistence.getAllObjects(com.ibm.scas.analytics.persistence.beans.Vlan.class);
		if (vlans.isEmpty()) {
			return Collections.emptyList();
		}
	
		return getVlansFromVlanRecords(vlans);
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.scas.analytics.backend.NetworkProvider#getAllVlans()
	 */
	@Override
	public Collection<Vlan> getVlans(Collection<String> vlanIds) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("getVlans(): %s", vlanIds));
		}
		if (vlanIds.isEmpty()) {
			return Collections.emptyList();
		}
		
		final List<com.ibm.scas.analytics.persistence.beans.Vlan> vlans = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Vlan.class, new WhereInClause("id", vlanIds));
		if (vlans.isEmpty()) {
			return Collections.emptyList();
		}
	
		return getVlansFromVlanRecords(vlans);	
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.scas.analytics.backend.NetworkProvider#getVlanById(java.lang.String)
	 */
	@Override
	public Vlan getVlanById(String vlanId) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("getVlanById(): id: %s", vlanId));
		}

		final com.ibm.scas.analytics.persistence.beans.Vlan vlan = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Vlan.class, vlanId);
		if (vlan == null) {
			return null;
		}
		
		return getVlansFromVlanRecords(Arrays.asList(vlan)).get(0);
	}
	
    /* (non-Javadoc)
	 * @see com.ibm.scas.analytics.backend.NetworkProvider#addVlan(com.ibm.scas.analytics.beans.Vlan)
	 */
	@Override
	public String addVlan(Vlan vlanReq) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("addVlan(): %s", ReflectionToStringBuilder.toString(vlanReq)));
		}
		
    	final CPELocation myLocation = persistence.getObjectById(CPELocation.class, cpeLocationName);
    	if (myLocation == null) {
    		throw new CPEException(String.format("Could not find location record: %s", cpeLocationName));
    	}	
		
		// this should be a nested structure like getSoftLayerAccount().getId(), i.e. "softlayerAccount": { "id": "xxxxx" }
		if (vlanReq.getSoftLayerAccount() == null) {
			throw new CPEParamException("vlan.softlayerAccount");
		}

		if (vlanReq.getSoftLayerAccount().getId() == null) {
			throw new CPEParamException("vlan.softlayerAccount.id");
		}
	
		// validate the softlayer account id passed in exists in our database
		final com.ibm.scas.analytics.persistence.beans.SoftLayerAccount slAcct = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.SoftLayerAccount.class, vlanReq.getSoftLayerAccount().getId());
		if (slAcct == null) {
			throw new CPEException(String.format("The SoftLayer Account with id %s was not found in the database", vlanReq.getSoftLayerAccount().getId()));
		}

		// validate the vlan actually exists in SoftLayer.  There should be a single vlan
		final SoftLayerAPIGateway slg = new SoftLayerAPIGateway(slAcct);
		
		// the vlan is defined by primaryRouter/vlanNumber, or ID
		final SoftLayerObjectFilter objFilter = new SoftLayerObjectFilter();
		if (vlanReq.getSoftLayerId() == null) {
			if (vlanReq.getPrimaryRouter() == null) {
				throw new CPEParamException("vlan.primaryRouter");
			}
			if (vlanReq.getVlanNumber() == null) {
				throw new CPEParamException("vlan.vlanNumber");
			}
			objFilter.setPropertyFilter("networkVlans.vlanNumber", vlanReq.getVlanNumber());
			objFilter.setPropertyFilter("networkVlans.primaryRouter.hostname", vlanReq.getPrimaryRouter());
		} else {
			objFilter.setPropertyFilter("networkVlans.id", vlanReq.getSoftLayerId());
		}

		// ask SoftLayer for the vlan Id
		final JsonElement slVlanArrElem = slg.callGetMethod("SoftLayer_Account", "getNetworkVlans", objFilter, VLAN_OBJECT_MASK);
		if (slVlanArrElem == null) {
			throw new CPEException(String.format("Could not find VLAN!"));
		}
		   	
    	final JsonArray arr = slVlanArrElem.getAsJsonArray();
    	if (arr.size() == 0) {
			throw new CPEException(String.format("Could not find VLAN!"));
    	}
    	
		int softLayerId = -1;
		JsonObject slVlanObj = null;
    	for (final JsonElement elem : arr) {
    		slVlanObj = elem.getAsJsonObject();
			final JsonElement idElem = slVlanObj.get("id");
			
			softLayerId = idElem.getAsInt();
			// set the gateway ID in our database record
			
			// we're just grabbing the first id from the list.  hopefully the name/id was unique in SL
			break;
    	}
    	
    	if (slVlanObj == null) {
			throw new CPEException(String.format("Could not find VLAN!"));
    	}
    	
		final String gatewayId = JsonUtil.getStringFromPath(slVlanObj, "attachedNetworkGateway.id");
		if (gatewayId != null) {
			throw new CPEException(String.format("VLAN is already associated with gateway %s", gatewayId));
		}
    	
		// validate that the VLAN isn't already there in the DB
		final List<com.ibm.scas.analytics.persistence.beans.Vlan> existingVlans = 
				persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Vlan.class, 
					new WhereClause("softLayerId", String.valueOf(softLayerId)));
		
		if (!existingVlans.isEmpty()) {
			throw new CPEParamException(String.format("VLAN already exists in database with ID %s", existingVlans.get(0).getId()));
		}
		

		final com.ibm.scas.analytics.persistence.beans.Vlan vlan = new com.ibm.scas.analytics.persistence.beans.Vlan();
		vlan.setSoftLayerId(String.valueOf(softLayerId));
		vlan.setSoftLayerAccount(slAcct);
		vlan.setLocation(myLocation);
		
		persistence.saveObject(com.ibm.scas.analytics.persistence.beans.Vlan.class, vlan);
		
		// grab all of the associated subnets and save them to DB
		final JsonArray subnetsArr = JsonUtil.getArrayFromPath(slVlanObj, "subnets");
		for (final JsonElement subnetElem : subnetsArr) {
			final JsonObject slSubnetObj = subnetElem.getAsJsonObject();
			
			final JsonElement subnetIdElem = slSubnetObj.get("id");
			final int subnetId = subnetIdElem.getAsInt();		
					
			final com.ibm.scas.analytics.persistence.beans.Subnet subnetRec = new com.ibm.scas.analytics.persistence.beans.Subnet();
			subnetRec.setSoftLayerAccount(slAcct);
			subnetRec.setVlan(vlan);
			subnetRec.setSoftLayerId(String.valueOf(subnetId));
			
			persistence.saveObject(com.ibm.scas.analytics.persistence.beans.Subnet.class, subnetRec);
			logger.info(String.format("addVlan(): Associated subnet added to database with ID %s.  %s/%d.", 
					subnetRec.getId(), 
					JsonUtil.getStringFromPath(slSubnetObj, "networkIdentifier"), 
					JsonUtil.getIntFromPath(slSubnetObj, "cidr")));		
		}	
	
		logger.info(String.format("addVlan(): Vlan added to database with id %s: %s.%d.", 
				vlan.getId(), 
				JsonUtil.getStringFromPath(slVlanObj, "primaryRouter.hostname"), 
				JsonUtil.getIntFromPath(slVlanObj, "vlanNumber")));
		
		return vlan.getId();
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.scas.analytics.backend.NetworkProvider#deleteVlan(java.lang.String)
	 */
	@Override
	public void deleteVlan(String vlanId) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("deleteVlan(): %s",vlanId));
		}

		final Vlan vlan = this.getVlanById(vlanId);
		if (vlan == null) {
			throw new CPEException(String.format("Vlan id %s does not exist in the database", vlanId));
		}
		
		if (vlan.getCluster() != null) {
			throw new CPEException(String.format("Vlan id %s (%s.%s) is assigned to Cluster %s (%s), unassign it first.", vlanId, vlan.getPrimaryRouter(), vlan.getVlanNumber(), vlan.getCluster().getName(), vlan.getCluster().getId()));
		}

		// if vlan associated with gateway, do not allow removal
		if (vlan.getGateway() != null) {
			final Gateway gw = this.getGatewayById(vlan.getGateway().getId());
			throw new CPEException(String.format("Vlan id %s (%s.%s) is associated with gateway %s (%s), detach it first.", vlanId, vlan.getPrimaryRouter(), vlan.getVlanNumber(), gw.getId(), gw.getName()));
		}
		
		// verify if Vlan is associated with Subnet->IPAddress->Cluster,
		// TODO : check if the vlan is not trunked or routed on any gateways -
		// sl api
		try {
			verifyVlanUsedByCluster(vlanId);
		} catch (CPEException e) {
			throw new CPEException(String.format("Cannot remove VLAN %s: %s", vlanId, e.getLocalizedMessage()), e);
		}
		
	
		persistence.deleteObjectById(com.ibm.scas.analytics.persistence.beans.Vlan.class, vlanId);
		deleteSubnet(vlanId);
		
		logger.info(String.format("VLAN record for %s.%s deleted. Return vlan to softlayer from portal if required", vlan.getPrimaryRouter(), vlan.getVlanNumber()));
	}

	/* (non-Javadoc)
	 * @see com.ibm.scas.analytics.backend.NetworkService#attachVlanToGateway(java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public void attachVlanToGateway(Collection<String> vlanIds, String gatewayId, boolean isBypass) throws CPEException {
		if (vlanIds.isEmpty()) {
			// nothing to do
			return;
		}
		
		// validate the vlan exists in our database
		final Collection<Vlan> vlans = this.getVlans(vlanIds);
		if (vlans.isEmpty()) {
			throw new CPEException(String.format("Vlan ids %s does not exist in the database", vlanIds.toString()));
		}
		
		// TODO: validate that the vlans are not trunked on some other gateway.  I suppose we could untrunk them here too but
		// perhaps that should be another method 
		
		// validate the gateway exists in our database
		final com.ibm.scas.analytics.persistence.beans.Gateway gateway = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Gateway.class, gatewayId);
		if (gateway == null) {
			throw new CPEException(String.format("Cannot find gateway with id %s", gatewayId));
		}
		
		// get the current gateway status on softlayer
		final SoftLayerAPIGateway slg = new SoftLayerAPIGateway(gateway.getSoftLayerAccount());
		final JsonElement elem = slg.getObjectById("SoftLayer_Network_Gateway", gateway.getSoftLayerId(), null, null, GATEWAY_OBJECT_MASK);
		
		final GatewayStatus status = GatewayStatus.valueOf(JsonUtil.getStringFromPath(elem, "status.keyName"));
		final String gwName = JsonUtil.getStringFromPath(elem, "name");
		if (status == GatewayStatus.UPDATING) {
			throw new CPEException(String.format("Gateway %s (name: %s) is updating on SoftLayer.  Try again later.", gateway.getId(), gwName));
		}
		
		// we need to prune the vlans that are already on the gateway, SL doesn't like that
		final JsonArray insideVlanArr = JsonUtil.getArrayFromPath(elem, "insideVlans");
		if (insideVlanArr != null) {
			final Set<String> slInsideVlanIds = new HashSet<String>(insideVlanArr.size());
			for (final JsonElement insideVlanElem : insideVlanArr) {
				slInsideVlanIds.add(String.valueOf(JsonUtil.getIntFromPath(insideVlanElem, "networkVlan.id")));
			}

			final Iterator<Vlan> vlanIter = vlans.iterator();
			while (vlanIter.hasNext()) {
				final Vlan vlanToTrunk = vlanIter.next();
				if (slInsideVlanIds.contains(vlanToTrunk.getSoftLayerId())) {
					// prune the vlans that are already on this gateway
					vlanIter.remove();
				}
			}
		}
		
		// get display names of all VLANs
		final Set<String> vlanNames = new HashSet<String>(vlans.size());
		for (final Vlan vlanToTrunk : vlans) {
			vlanNames.add(String.format("%s.%s", vlanToTrunk.getPrimaryRouter(), vlanToTrunk.getVlanNumber()));
		}
		
		logger.debug(String.format("Attempting to trunk VLANs %s to gateway %s", vlanNames, gwName));
		
		// Construct the call to SoftLayer
		final String urlPath = "SoftLayer_Network_Gateway_Vlan/createObjects";
					
		final JsonArray gvArray = new JsonArray();
		for (final Vlan vlan : vlans) {
			final JsonObject gv = new JsonObject();
			gv.addProperty("bypassFlag", Boolean.valueOf(isBypass));
			gv.addProperty("networkGatewayId", Integer.valueOf(gateway.getSoftLayerId()));
			gv.addProperty("networkVlanId", Integer.valueOf(vlan.getSoftLayerId()));
			
			gvArray.add(gv);
		}
		
		// the expected parameter is an array with an array inside
		final JsonArray parametersArray = new JsonArray();
		parametersArray.add(gvArray);
		
		final JsonObject parameters = new JsonObject();
		parameters.add("parameters", parametersArray);

		try {
			slg.callPostMethod(urlPath, parameters);
		} catch (CPEException e) {
			new NagiosEventLogger().emitCritical("NAGIOSMONITOR VLANASSOCIATIONGW :"+e.getLocalizedMessage());
			throw e;
		}
		
		String emessage = String.format("Trunked VLANs %s to gateway %s.", vlanNames, gwName); 
		logger.info(emessage);
		new NagiosEventLogger().emitOK("NAGIOSMONITOR VLANASSOCIATIONGW :"+emessage);
	}
	
	@Override
	public void detachVlanFromGateway(String vlanId, boolean force) throws CPEException {
		// validate the vlan exists in our database
		final Vlan vlan = this.getVlanById(vlanId);
		if (vlan == null) {
			throw new CPEException(String.format("Vlan id %s does not exist in the database", vlanId));
		}
		
		if (!force) {
			this.verifyVlanUsedByCluster(vlanId);
		}
		
		if (vlan.getGateway() == null) {
			// no error, just do nothing
			return;
		}
		
		final Gateway gateway = this.getGatewayById(vlan.getGateway().getId());
		if (gateway.getType() == GatewayType.MANAGEMENT) {
			throw new CPEException(String.format("Unable to detach VLAN %s (%s.%s) from MANAGEMENT gateway %s (name: %s).", vlan.getId(), vlan.getPrimaryRouter(),vlan.getVlanNumber(), gateway.getId(), gateway.getName()));
		}
		
		final GatewayStatus status = gateway.getStatus();
		if (status == GatewayStatus.UPDATING) {
			throw new CPEException(String.format("Gateway %s (name: %s) is updating on SoftLayer.  Try again later.", vlan.getGateway().getId(), gateway.getName()));
		}

		
		// find the ID of the Network_Gateway_Vlan item associated with this vlan/gateway pair
		if (gateway.getAssociatedVlans().isEmpty()) {
			// no error, the poller thread will probably update us soon
			logger.warn(String.format("The CPE database and SoftLayer are out of sync.  VLAN ID %s (%s.%s) is not actually associated with gateway %s (%s).", 
					vlanId, vlan.getPrimaryRouter(), vlan.getVlanNumber(), gateway.getId(), gateway.getName()));
			return;
		}
		
		String insideVlanId = null;
		for (final SoftLayerIdObject assocVlan : gateway.getAssociatedVlans()) {
			if (!vlan.getSoftLayerId().equals(assocVlan.getSoftLayerId())) {
				// not this vlan
				continue;
			}
			
			insideVlanId = vlan.getSoftLayerId();
			// it should only appear once
			break;
		}
		
		if (insideVlanId == null) {
			// no error, the poller thread will probably update us soon
			logger.warn(String.format("The CPE database and SoftLayer are out of sync.  VLAN ID %s (%s.%s) is not actually associated with gateway %s (%s).", 
					vlanId, vlan.getPrimaryRouter(), vlan.getVlanNumber(), gateway.getId(), gateway.getName()));
		}
		
		logger.debug(String.format("Attempting to untrunk VLAN %s (%s.%s) from gateway %s (%s)", vlan.getId(), vlan.getPrimaryRouter(), vlan.getVlanNumber(), gateway.getId(), gateway.getName()));
		
		// Construct the call to SoftLayer
		try {
			final SoftLayerAPIGateway slg = new SoftLayerAPIGateway(gateway.getSoftLayerAccount());
			
			// first get the SoftLayer_Network_Gateway_Vlan ID
			final JsonElement slVlanObj = slg.getObjectById("SoftLayer_Network_Vlan", insideVlanId, null, null, "id", "attachedNetworkGatewayVlan.id");
			final Integer networkGatewayVlanId = JsonUtil.getIntFromPath(slVlanObj, "attachedNetworkGatewayVlan.id");
			
			if (networkGatewayVlanId == null) {
				// no error, the poller thread will probably update us soon
				logger.warn(String.format("The CPE database and SoftLayer are out of sync.  VLAN ID %s (%s.%s) is not actually associated with gateway %s (%s).", 
					vlanId, vlan.getPrimaryRouter(), vlan.getVlanNumber(), gateway.getId(), gateway.getName()));			
			}
			
			slg.deleteObjectById("SoftLayer_Network_Gateway_Vlan", String.valueOf(networkGatewayVlanId));
		} catch (CPEException e) {
			new NagiosEventLogger().emitCritical("NAGIOSMONITOR VLANASSOCIATIONGW :"+e.getLocalizedMessage());
			throw e;
		}
		
		final String emessage = String.format("Untrunked VLANs %s (%s.%s) from gateway %s (%s).", vlan.getId(), vlan.getPrimaryRouter(), vlan.getVlanNumber(), gateway.getId(), gateway.getName()); 
		logger.info(emessage);
		new NagiosEventLogger().emitOK("NAGIOSMONITOR VLANASSOCIATIONGW :"+emessage);
	}
	
	/**
	 * MEthod to verify vlan used by cluster
	 * @param vlanId
	 * @return
	 * @throws CPEException
	 */
	protected void verifyVlanUsedByCluster(String vlanId) throws CPEException {
		// check if vlan record's "Cluster" field is set
		final com.ibm.scas.analytics.persistence.beans.Vlan vlanRec = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Vlan.class, vlanId);
		if (vlanRec == null) {
			throw new CPEException(String.format("Cannot find VLAN with id: %s", vlanId));
		}
		
		if (vlanRec.getCluster() != null) {
			throw new CPEException(String.format("VLAN %s is used by clusters: %s", vlanId, vlanRec.getCluster().getId()));
		}
		
		List<com.ibm.scas.analytics.persistence.beans.Subnet> subnetList = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Subnet.class,
				new WhereClause("vlan.id", vlanId));
		if (subnetList == null || subnetList.isEmpty()) {
			// no subnets so there's no clusters on this vlan
			return;
		}
		
		final Set<String> subnetIdList = new HashSet<String>();
		for (final com.ibm.scas.analytics.persistence.beans.Subnet subnet : subnetList) {
			subnetIdList.add(subnet.getId());
		}
		
		final List<com.ibm.scas.analytics.persistence.beans.IPAddress> ipAdressBySubnet = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.IPAddress.class, new WhereInClause("subnet.id", subnetIdList));
		if (ipAdressBySubnet == null || ipAdressBySubnet.isEmpty()) {
			// not IP address records on these subnets
			return;
		}
		
		final Set<String> clusterIdList = new HashSet<String>();
		for (final com.ibm.scas.analytics.persistence.beans.IPAddress ipAddr : ipAdressBySubnet) {
			clusterIdList.add(ipAddr.getCluster().getId());
		}
		throw new CPEException(String.format("VLAN %s is used by clusters: %s", vlanId, clusterIdList));
	}
	
	/**
	 * helper method to merge subnet details with SoftLayer details
	 * @param subnetRecords
	 * @return
	 */
	private List<Subnet> getSubnetsFromSubnetRecords(List<com.ibm.scas.analytics.persistence.beans.Subnet> subnetRecords) throws CPEException {
		if (subnetRecords.isEmpty()) {
			return Collections.emptyList();
		}
		final List<Subnet> subnets = new ArrayList<Subnet>();
		
		final Map<String, List<String>> softLayerIdMap = new HashMap<String, List<String>>();
		final Map<String, Subnet> subnetIdMap = new HashMap<String, Subnet>();
		for (final com.ibm.scas.analytics.persistence.beans.Subnet subnetRecord : subnetRecords) {
			final Subnet subnet = BeanConversionUtil.convertToBean(subnetRecord);
			if (subnet.getSoftLayerId() != null) {
				// get the details from SoftLayer
				CollectionsUtil.addToMap(softLayerIdMap, subnet.getSoftLayerAccount().getId(), subnet.getSoftLayerId());
				subnetIdMap.put(subnet.getSoftLayerId(), subnet);
			}
			subnets.add(subnet);
		}
		
		for (final Entry<String, List<String>> softLayerId : softLayerIdMap.entrySet()) {
			final com.ibm.scas.analytics.persistence.beans.SoftLayerAccount slAcct = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.SoftLayerAccount.class, softLayerId.getKey());
			final List<String> subnetIdList = softLayerId.getValue();
			final SoftLayerAPIGateway slg = new SoftLayerAPIGateway(slAcct);
			
			// build object filter
			final SoftLayerObjectFilter objFilter = new SoftLayerObjectFilter();
			objFilter.setPropertyFilter("subnets.id", new HashSet<String>(subnetIdList));

			// add the details from the SoftLayer API
			final JsonElement slSubnetArrElem = slg.callGetMethod("SoftLayer_Account", "getSubnets", objFilter, SUBNET_OBJECT_MASK);
			if (slSubnetArrElem == null || !slSubnetArrElem.isJsonArray()) {
				throw new CPEException(String.format("Cannot retrieve list of subnets from SoftLayer Account with ID: %s, ID list: %s", softLayerId.getKey(), subnetIdList));
			}
			
			final JsonArray slSubnetArr = slSubnetArrElem.getAsJsonArray();
			for (final JsonElement slSubnetObjElem : slSubnetArr) {
				final JsonObject slsubnetObj = slSubnetObjElem.getAsJsonObject();
				final String slSubnetId = JsonUtil.getStringFromPath(slsubnetObj, "id");
				
				addSLSubnetDetails(subnetIdMap.get(slSubnetId), slsubnetObj);
			}
		}	
		
		return subnets;	
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.scas.analytics.backend.NetworkProvider#getAllSubnets()
	 */
	@Override
	public List<Subnet> getAllSubnets() throws CPEException{
		if (logger.isTraceEnabled()) {
			logger.trace("getAllSubnets()");
		}
		
		final List<com.ibm.scas.analytics.persistence.beans.Subnet> subnetRecords = persistence.getAllObjects(com.ibm.scas.analytics.persistence.beans.Subnet.class);
		if (subnetRecords.isEmpty()) {
			return Collections.emptyList();
		}
		
		return getSubnetsFromSubnetRecords(subnetRecords);

	}
	
	/* (non-Javadoc)
	 * @see com.ibm.scas.analytics.backend.NetworkProvider#getSubnetById(java.lang.String)
	 */
	@Override
	public Subnet getSubnetById(String subnetId) throws CPEException{
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("getSubnetById(): %s", subnetId));
		}
		final com.ibm.scas.analytics.persistence.beans.Subnet subnetRec = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Subnet.class, subnetId);
		
		if (subnetRec == null) {
			return null;
		}
		
		return getSubnetsFromSubnetRecords(Arrays.asList(subnetRec)).get(0);
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.scas.analytics.backend.NetworkProvider#getSubnetById(java.lang.String)
	 */
	@Override
	public List<Subnet> getSubnets(Collection<String> subnetIds) throws CPEException{
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("getSubnets(): %s", subnetIds));
		}
		
		if (subnetIds.isEmpty()) {
			return Collections.emptyList();
		}
		
		final List<com.ibm.scas.analytics.persistence.beans.Subnet> subnetRecs = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Subnet.class, new WhereInClause("id", subnetIds));
		
		if (subnetRecs.isEmpty()) {
			return Collections.emptyList();
		}
		
		return getSubnetsFromSubnetRecords(subnetRecs);
	}
	
	@Override
	public List<Subnet> getSubnets(String gatewayId) throws CPEException{
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("getSubnets(): gatewayid=%s", gatewayId));
		}
		
		final List<com.ibm.scas.analytics.persistence.beans.Subnet> subnetRecs = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Subnet.class, new WhereClause("vlan.gateway.id", gatewayId));
		
		if (subnetRecs.isEmpty()) {
			return Collections.emptyList();
		}
		
		return getSubnetsFromSubnetRecords(subnetRecs);
	}


	/* (non-Javadoc)
	 * @see com.ibm.scas.analytics.backend.NetworkProvider#deleteSubnet(java.lang.String)
	 */
	@Override
	public void deleteSubnet (String vlanID) throws CPEException{
		final List<com.ibm.scas.analytics.persistence.beans.Subnet> subnets = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Subnet.class, new WhereClause("vlan.id",vlanID));
		if (subnets != null) {
			for (final com.ibm.scas.analytics.persistence.beans.Subnet subnet : subnets) {
				persistence.deleteObjectById(com.ibm.scas.analytics.persistence.beans.Subnet.class, subnet.getId());
				deleteIpaddress(subnet.getId());
			}
		}
	}
	
	//try passing vlanID
	/* (non-Javadoc)
	 * @see com.ibm.scas.analytics.backend.NetworkProvider#deleteIpaddress(java.lang.String)
	 */
	@Override
	public void deleteIpaddress(String subnetId) throws CPEException
	{
		List<IPAddress> ipaddresses = persistence.getObjectsBy(IPAddress.class, new WhereClause("subnet.id",subnetId));
		if(ipaddresses != null){
			for(IPAddress ipaddress : ipaddresses){
				persistence.deleteObjectById(IPAddress.class, ipaddress.getId());
			}
		}
	}
	

	/* (non-Javadoc)
	 * @see com.ibm.scas.analytics.backend.NetworkProvider#reserveVlan(java.lang.String)
	 */
	@Override
	public synchronized String reserveVlan(String clusterId, String gatewayId) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("reserveVlan(): clusterId: %s, gatewayId: %s", clusterId, gatewayId));
		}
		
		final com.ibm.scas.analytics.persistence.beans.Cluster clusterRec = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Cluster.class, clusterId);
		if (clusterRec == null) {
			throw new CPEParamException(String.format("Cluster %s does not exist!", clusterId));
		}
		
		// validate that the gateway is in our database
		final com.ibm.scas.analytics.persistence.beans.Gateway gatewayRec = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Gateway.class, gatewayId); 
		if (gatewayRec == null) {
			throw new CPEParamException(String.format("Gateway %s could not be found!", gatewayId));
		}
		
		final Gateway gateway = getGatewaysFromGatewayRecords(Arrays.asList(gatewayRec)).get(0);
		
		// get VLANs not attached to a gateway and not owned by any other subscribers
		final List<com.ibm.scas.analytics.persistence.beans.Vlan> availableVlans = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Vlan.class, 
				new WhereClause("cluster", null), new WhereClause("gateway", null));
	
		// collect the vlan records in a map
		final Map<String, com.ibm.scas.analytics.persistence.beans.Vlan> vlanRecMap = new HashMap<String, com.ibm.scas.analytics.persistence.beans.Vlan>();
		for (final com.ibm.scas.analytics.persistence.beans.Vlan vlanRec : availableVlans) {
			vlanRecMap.put(vlanRec.getSoftLayerId(), vlanRec);
		}
		
		final List<Vlan> allVlans = getVlansFromVlanRecords(availableVlans);
		final Iterator<Vlan> vlanIter = allVlans.iterator();
		
		// prune the ineligible vlans from the list
		while (vlanIter.hasNext()) {
			final Vlan vlan = vlanIter.next();
			if (vlan.getNetworkSpace() != VlanNetworkSpace.PRIVATE) {
				// non-private VLANs are not eligible for reserving
				vlanIter.remove();
				continue;
			}
			
			if (!vlan.getPrimaryRouter().equals(gateway.getPrimaryBackendRouter())) {
				// the Vlan has to be behind the same backend router(s) as the gateway
				vlanIter.remove();
				continue;
			}
			
			if (vlan.getGateway() != null) {
				// vlan might be added to some gateway in softlayer already that we don't have in our db
				vlanIter.remove();
				continue;
			}
		}
		
		if (allVlans.size() == 0) {
			throw new CPEException(String.format("No available VLANs for reservation behind Gateway %s (id %s)!", gateway.getName(), gateway.getId()));
		}
		
		// select the first private VLAN -- note that the network space is not stored in db
		final Vlan vlanElem = allVlans.get(0);
		final com.ibm.scas.analytics.persistence.beans.Vlan vlanRec = vlanRecMap.get(vlanElem.getSoftLayerId());
		vlanRec.setCluster(clusterRec);
		
		persistence.updateObject(com.ibm.scas.analytics.persistence.beans.Vlan.class, vlanRec);
		
		// in case we are reserving multiple VLANs in the same session, send the update to the database
		persistence.flush();
		
		logger.info(String.format("reserveVlan(): Reserved VLAN %s (%s.%s) for cluster %s; will trunk to gateway %s (%s)", 
				vlanRec.getId(), vlanElem.getPrimaryRouter(), vlanElem.getVlanNumber(), clusterRec.getId(), gateway.getName(), gateway.getId()));
		
		return vlanRec.getId();
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.scas.analytics.backend.NetworkProvider#reserveIPAddressesForClusterTier(java.lang.String, int, java.lang.String, java.lang.String)
	 */
	@Override
	public synchronized List<String> reserveIPAddressesForClusterTier(String vlanId, int numIPsToReserve, String clusterId, String clusterTierName) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("reserveIPAddressesForClusterTier(): vlan: %s, num: %d, clusterId: %s, tierName: %s", vlanId, numIPsToReserve, clusterId, clusterTierName));
		}
		
		// validate: totalIPs > 0
		if (numIPsToReserve <= 0) {
			throw new CPEParamException("Cannot reserve less than 1 IP Address");
		}
		
		if (clusterTierName == null) {
			throw new CPEParamException("TierName required");
		}
		
		// Lookup the cluster to validate it exists
		final com.ibm.scas.analytics.persistence.beans.Cluster cluster = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Cluster.class, clusterId);
		if (cluster == null) {
			throw new CPEParamException(String.format("Cannot find cluster record: %s", clusterId));
		}
		
		// Lookup the vlan to validate it exists
		final Vlan vlan = this.getVlanById(vlanId);
		if (vlan == null) {
			throw new CPEParamException(String.format("Cannot find vlan record: %s", vlanId));
		}
		
		if (vlan.getCluster() == null && vlan.getNetworkSpace() != VlanNetworkSpace.PUBLIC) {
			// an unassigned, non-public VLAN is not eligible to have IPs reserved on it
			throw new CPEException(String.format("Cluster owner %s does not own VLAN %s", cluster.getOwner().getId(), vlan.getId()));
		}
		
		// validate the vlan and cluster are owned by the same subscriber
		if (vlan.getCluster() != null && 
			!vlan.getCluster().getId().equals(cluster.getId())) {
			throw new CPEException(String.format("Cluster owner %s does not own VLAN %s", cluster.getOwner().getId(), vlan.getId()));
		}
		
		final List<Subnet> portableSubnets = new ArrayList<Subnet>(vlan.getSubnets().size());
		for (final SoftLayerIdObject subnetIdObj : vlan.getSubnets()) {
			final Subnet subnet = (Subnet) subnetIdObj;
			// only secondary on vlan (portable) subnets, or BYOIP subnets can be used
			if (!subnet.getType().equals("SECONDARY_ON_VLAN") &&
				!subnet.getType().equals("BYOIP")) {
				continue;
			}
			portableSubnets.add(subnet);
		}

		if (portableSubnets.size() == 0) {
			/* throwing an exception immediately because of lack of subnets.  the design states that we should try to reserve again
			 * in another interval since subnets may be added later.
			 */
			throw new CPEException(String.format("No available subnets on VLAN %s.%s.  Order a portable subnet.", vlan.getPrimaryRouter(), vlan.getVlanNumber()));
		}

		// Check how many IPs are already reserved on this VLAN
		final List<com.ibm.scas.analytics.persistence.beans.IPAddress> reservedIpAddrList = 
				persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.IPAddress.class, 
						new WhereClause("subnet.vlan.id", vlan.getId()));
		
		logger.trace(String.format("Already reserved on VLAN %s.%s: %s", vlan.getPrimaryRouter(), vlan.getVlanNumber(), reservedIpAddrList));

		// Map subnets by softlayer account id for lookup later
		final List<String> subnetIds = new ArrayList<String>();
		for (final Subnet subnet : portableSubnets) {
			if (subnet.getSoftLayerId() == null) {
				/* TODO: BYOIP subnets will not have softlayer IDs.  for now just look at SoftLayer ID subnets */
				continue;
			}
			subnetIds.add(subnet.getSoftLayerId());
		}
		
		// start a softLayer query for IP addresses for all subnet IDs 
		final SoftLayerAPIGateway slg = new SoftLayerAPIGateway(vlan.getSoftLayerAccount());
		final SoftLayerObjectFilter objectFilter = new SoftLayerObjectFilter();
		
		// just the subnets in our database
		objectFilter.setPropertyFilter("subnets.id", new HashSet<String>(subnetIds));
			
		// SECONDARY_ON_VLAN are portable subnets only -- we cannot use primary subnets as SoftLayer reserves all of the addresses on these
		objectFilter.setPropertyFilter("subnets.subnetType", "SECONDARY_ON_VLAN");
			
		// IPv4 only
		objectFilter.setPropertyFilter("subnets.version", 4);
			
		final JsonElement subnetArrElem = slg.callGetMethod("SoftLayer_Account", "getSubnets",
				objectFilter,
				"ipAddresses.ipAddress",
				"ipAddresses.isReserved",
				"ipAddresses.isGateway",
				"ipAddresses.isBroadcast",
				"ipAddresses.isNetwork");

		final JsonArray subnetArr = subnetArrElem.getAsJsonArray();
		
		// map the SoftLayer objects by ID for quick lookup later
		final Map<String, JsonElement> slSubnetIdMap = new HashMap<String, JsonElement>();
		for (final JsonElement subnetElem : subnetArr) {
			slSubnetIdMap.put(String.valueOf(JsonUtil.getIntFromPath(subnetElem, "id")), subnetElem);
		}
		
		// NOTE: reserved IPs can span multiple subnets.  the traffic just goes through the router.
		final Map<Long, Subnet> availableIPAddrMap = new HashMap<Long, Subnet>();
		for (final Subnet subnet : portableSubnets) {
			// look up the IP addresses in the database that are already reserved, and combine it with the IP Addresses marked
			// reserved on SoftLayer, to determine if we have enough.  If we do, then we can create new records for IP addresses
			// requested by the caller.
			
			if (subnet.getSoftLayerId() == null && slSubnetIdMap.get(subnet.getSoftLayerId()) == null) {
				// not an eligible subnet.  it must either be a SoftLayer portable subnet, or a BYOIP subnet (softLayerId will be null)
				continue;
			}
			
			// expand this subnet definition into a list of available IPAddress records
			if (subnet.getSoftLayerId() == null) {
				for (final Long ipAddrLong : IPAddressUtil.expandSubnetToLongArray(subnet.getNetworkAddr(), subnet.getCidr())) {
					if (ipAddrLong == IPAddressUtil.ipToLong(subnet.getNetworkAddr()) ||
						ipAddrLong == IPAddressUtil.ipToLong(subnet.getGatewayAddr()) ||
						ipAddrLong == IPAddressUtil.ipToLong(subnet.getBroadcastAddr())) {
						// network, gateway, and broadcast are not available
						continue;
					}
					
					availableIPAddrMap.put(ipAddrLong, subnet);
				}			
			} else {
				// add all IPs from the softlayer definition to available IP list
				final JsonElement slSubnetElem = slSubnetIdMap.get(subnet.getSoftLayerId());
				if (slSubnetElem == null) {
					continue;
				}
				
				final JsonArray ipAddressArray = JsonUtil.getArrayFromPath(slSubnetElem, "ipAddresses");
				for (final JsonElement ipAddrElem : ipAddressArray) {
					// remove reserved IPAddresses from available IPAddress record list
					if (JsonUtil.getBooleanFromPath(ipAddrElem, "isReserved") || 
						JsonUtil.getBooleanFromPath(ipAddrElem, "isNetwork") ||	
						JsonUtil.getBooleanFromPath(ipAddrElem, "isGateway") ||	
						JsonUtil.getBooleanFromPath(ipAddrElem, "isBroadcast")) {
						continue;
					}
				
					final String ipAddr = JsonUtil.getStringFromPath(ipAddrElem, "ipAddress");
					final Long ipAddrLong = IPAddressUtil.ipToLong(ipAddr);
					availableIPAddrMap.put(ipAddrLong, subnet);
				}			
			}
		}
		
		// remove already reserved IPAddresses from available IPAddress record list
		for (final com.ibm.scas.analytics.persistence.beans.IPAddress ip : reservedIpAddrList) {
			availableIPAddrMap.remove(new Long(ip.getIpAddress()));
		}
		
		if (availableIPAddrMap.keySet().size() < numIPsToReserve) {
			throw new CPEException(String.format("Not enough IPs on VLAN %s to reserve!  Please add an additional portable subnet!", vlanId));
		}
		
		final List<String>  reservedIPs = new ArrayList<String>(numIPsToReserve);
		int count = 0;
		for (final Entry<Long, Subnet> ipAddrEnt : availableIPAddrMap.entrySet()) {
			// add available IPs to the List
			final com.ibm.scas.analytics.persistence.beans.IPAddress newIPAddress = new com.ibm.scas.analytics.persistence.beans.IPAddress();
			final com.ibm.scas.analytics.persistence.beans.Subnet subnetRec = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Subnet.class, ipAddrEnt.getValue().getId());
			logger.debug(String.format("adding IP %s on subnet %s", IPAddressUtil.longToIp(ipAddrEnt.getKey()), subnetRec));
			newIPAddress.setSubnet(subnetRec);
			newIPAddress.setCluster(cluster);
			newIPAddress.setTierName(clusterTierName);
			newIPAddress.setIpAddress(ipAddrEnt.getKey());
			
			// generate the hostname
			newIPAddress.setHostname(IPAddressUtil.generateHostnameForIP(ipAddrEnt.getKey()));
			
			// save new records
			persistence.saveObject(com.ibm.scas.analytics.persistence.beans.IPAddress.class, newIPAddress);
			
			reservedIPs.add(IPAddressUtil.longToIp(newIPAddress.getIpAddress()));
			
			if (++count == numIPsToReserve) {
				// break out when we reach the required number
				break;
			}
		}
		
		// in case we are reserving multiple IPs in the same session, send the update to the database
		// immediately instead of waiting for the persistence framework to do it
		persistence.flush();
	
		logger.info(String.format("Reserved %d IP addresses on VLAN %s for Cluster %s Tier %s: %s", numIPsToReserve, vlanId, clusterId, clusterTierName, reservedIPs));
		
		return reservedIPs;
	}
	
	/**
	 * to avoid infinite loop on output, set the asociatedVlan objects to SoftLayerIdObjects
	 * this is because vlans have refs to gateway, which have refs back to vlan, etc.
	 * 
	 * gateway.getVlan().getGateway() should not be the original gateway again, or the json output
	 * will result in stack overflow when it attempts to print out the object
	 * similarly, gateway.getVlan().getSubnets().getVlan() should be a reference only and not the
	 * actual Vlan.
	 * 
	 * @param gw
	 * @return new gateway object with children's parent references updated
	 * @throws CPEException
	 */
	private static void replaceChildObjectParentRefs(Gateway gw) throws CPEException {
		// get a reference to the gateway itself
		final SoftLayerIdObject parentGatewayRef = BeanConversionUtil.convertToRef(gw);
		
		for (final SoftLayerIdObject myIdObj : gw.getAssociatedVlans()) {
			final Vlan myVlan = (Vlan)myIdObj;
			
			// change my subnets' vlan parents to references
			replaceChildObjectParentRefs(myVlan);

			// change my gateway references to parentGatewayRef
			myVlan.setGateway(parentGatewayRef);
		}
	}
	
	/**
	 * to avoid infinite loop on output, set the subnets objects to SoftLayerIdObjects
	 * this is because subnets have refs to vlans, which have refs to subnet, etc.
	 * 
	 * vlan.getSubnets().getVlan() should be a reference only and not the
	 * actual Vlan.
	 * 
	 * @param vlan 
	 * @return new vlan object with children's parent references updated
	 * @throws CPEException
	 */
	private static void replaceChildObjectParentRefs(Vlan vlan) throws CPEException {
		// get a reference to myself
		final SoftLayerIdObject parentVlanRef = BeanConversionUtil.convertToRef(vlan);
		for (final SoftLayerIdObject myIdObj : vlan.getSubnets()) {
			final Subnet mySubnet = (Subnet)myIdObj;
			// update my childrens' parent objects to the reference
			mySubnet.setVlan(parentVlanRef);
		}
	}
	
	/**
	 * Helper to get SL details on ip address records
	 * @param ipAddressRecs
	 * @return
	 * @throws CPEException
	 */
	private List<IPAddress> getIPAddressFromRecords(List<com.ibm.scas.analytics.persistence.beans.IPAddress> ipAddressRecs) throws CPEException {
		if (ipAddressRecs.isEmpty()) {
			return Collections.emptyList();
		}
		
		final List<IPAddress> toReturn = new ArrayList<IPAddress>(ipAddressRecs.size());
		final Set<String> vlanIds = new HashSet<String>();
		for (final com.ibm.scas.analytics.persistence.beans.IPAddress ipAddress : ipAddressRecs) {
			vlanIds.add(ipAddress.getSubnet().getVlan().getId());
		}
		
		// get the VLAN details from network service, fill them in on the vlan and subnet objects
		final Collection<Vlan> myVlans = getVlans(vlanIds);
		final Map<String, Subnet> subnetIdMap = new HashMap<String, Subnet>(); 
		for (final Vlan myVlan : myVlans) {
			
			// create a stub vlan with just the primary router, vlan number, gateway, etc.
			// this reduces the size of output and avoids infinite recursion during serialization to JSON
			final Vlan stubVlan = new Vlan();
			try {
				final Map<String, Object> vlanProps = PropertyUtils.describe(myVlan);
				vlanProps.put("subnets", new ArrayList<Subnet>());
				vlanProps.put("softLayerAccount", null);
				vlanProps.put("cluster", null);
				vlanProps.put("gateway", BeanConversionUtil.convertToRef(myVlan.getGateway()));
				BeanUtils.populate(stubVlan, vlanProps);
			} catch (IllegalAccessException e) {
				throw new CPEException(e.getLocalizedMessage(), e);
			} catch (InvocationTargetException e) {
				throw new CPEException(e.getLocalizedMessage(), e);
			} catch (NoSuchMethodException e) {
				throw new CPEException(e.getLocalizedMessage(), e);
			}
			
			for (final SoftLayerIdObject myIdObj : myVlan.getSubnets()) {
				final Subnet mySubnet = (Subnet)myIdObj;
				subnetIdMap.put(mySubnet.getId(), mySubnet);
				
				mySubnet.setVlan(stubVlan);
			}
			
			myVlan.setSubnets(new ArrayList<SoftLayerIdObject>());
		}
		
		// create an IP address object for each record
		for (final com.ibm.scas.analytics.persistence.beans.IPAddress ipAddress : ipAddressRecs) {
			final IPAddress ipAddr = BeanConversionUtil.convertToBean(ipAddress);
		
			ipAddr.setSubnet(subnetIdMap.get(ipAddress.getSubnet().getId()));
			// if there's a record, we can reserve it
			ipAddr.setReservable(true);
			toReturn.add(ipAddr);
		}
	
		return toReturn;
	}

	@Override
	public List<IPAddress> getIPAddressByCluster(String clusterId, String clusterTier) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("getIPAddressByCluster(): clusterId: %s, clusterTier: %s", clusterId, clusterTier));
		}
		
		// first try to validate the cluster
		final com.ibm.scas.analytics.persistence.beans.Cluster cluster = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Cluster.class, clusterId);
		if (cluster == null) {
			throw new CPEParamException(String.format("Could not find cluster with ID: %s", clusterId));
		}
		
		// get the IP address for the cluster
		final List<WhereClause> wheres = new ArrayList<WhereClause>();
		wheres.add(new WhereClause("cluster.id", clusterId));
		if (clusterTier != null) {
			wheres.add(new WhereClause("tierName", clusterTier));
		}
		
		final List<com.ibm.scas.analytics.persistence.beans.IPAddress> reservedIpAddress = persistence.getObjectsBy(
				com.ibm.scas.analytics.persistence.beans.IPAddress.class, 
				wheres.toArray(new WhereClause[] {}));
		if (reservedIpAddress.size() == 0) {
			return Collections.emptyList();
		}
		
		// note the caller is only interested in reserved IPs
		return getIPAddressFromRecords(reservedIpAddress);
	}

	@Override
	public IPAddress getIPAddress(String ipAddressId) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("getIPAddress(): id: %s", ipAddressId));
		}
		
		final com.ibm.scas.analytics.persistence.beans.IPAddress ipAddress = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.IPAddress.class, ipAddressId);
		if (ipAddress == null) {
			// since we're getting it by DB ID, if it's not in the database, don't return it
			return null;
		}
		
		return getIPAddressFromRecords(Arrays.asList(ipAddress)).get(0);
	}
	
	/**
	 * Method to query ips by vlan.  note that addresses are unique in each vlan.
	 * If the ip is not in any of the subnets on the vlan, return null.  otherwise return
	 * an IP address object with references to subnet and vlan
	 * @param ipAddress
	 * @param vlanId
	 * @return
	 * @throws CPEException
	 */
	public IPAddress getIPAddress(String ipAddress, String vlanId) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("getIPAddress(): ipAddress: %s, vlan: %s", ipAddress, vlanId));
		}
		
		final Long ipAddressLong = IPAddressUtil.ipToLong(ipAddress);
		// get the reserved IP if it's reserved
		final List<com.ibm.scas.analytics.persistence.beans.IPAddress> ipAddressList = 
				persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.IPAddress.class, 
						new WhereClause("subnet.vlan.id", vlanId), 
						new WhereClause("ipAddress", ipAddressLong));
		
		if (!ipAddressList.isEmpty()) {
			return getIPAddressFromRecords(ipAddressList).get(0);
		}
		
		// but wait!  the IP address might be in some subnet in this VLAN, but we don't have a record.  the
		// IP address table is sparse, so let's return the constructed IP address without any
		// database fields filled in
	
		// first, grab the VLAN record
		final com.ibm.scas.analytics.persistence.beans.Vlan vlanRec = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Vlan.class, vlanId); 
		if (vlanRec == null) {
			// if the VLAN doesn't exist, then the IP address doesn't exist for sure
			return null;
		}
		
		// grab the subnet records
		final List<com.ibm.scas.analytics.persistence.beans.Subnet> subnetRecs = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Subnet.class, new WhereClause("vlan.id", vlanId));
		final Map<String, com.ibm.scas.analytics.persistence.beans.Subnet> subnetRecMap = new HashMap<String, com.ibm.scas.analytics.persistence.beans.Subnet>(subnetRecs.size());
		for (final com.ibm.scas.analytics.persistence.beans.Subnet subnetRec : subnetRecs) {
			if (subnetRec.getSoftLayerId() == null) {
				// shortcut here.  if the subnet does not exist in softlayer, then check if the passed in address exists within the
				// subnet range.  if it does, we can return an IP Address object right away
				
				if (!IPAddressUtil.isIpInNetwork(ipAddress, subnetRec.getNetworkAddr(), subnetRec.getCidr())) {
					continue;
				}
				
				// construct the IP for returning
				final IPAddress toReturn = new IPAddress();
				toReturn.setIpAddress(ipAddress);
				toReturn.setReservable(true);
				
				if (IPAddressUtil.getBroadcastAddr(subnetRec.getNetworkAddr(), subnetRec.getCidr()).equals(ipAddress) ||
					subnetRec.getNetworkAddr().equals(ipAddress)) {
					toReturn.setReservable(false);
				}
				
				// gateway address of the subnet is network address + 1
				if (ipAddress.equals(IPAddressUtil.longToIp(IPAddressUtil.ipToLong(subnetRec.getNetworkAddr()) + 1))) {
					toReturn.setReservable(false);
				}
				
				final SoftLayerIdObject parentSubnetObj = new SoftLayerIdObject();
				parentSubnetObj.setId(subnetRec.getId());
				toReturn.setSubnet(parentSubnetObj);
				
				return toReturn;
			}
			
			subnetRecMap.put(subnetRec.getSoftLayerId(), subnetRec);
		}
		
		// grab all softlayer subnet objects
		final SoftLayerAPIGateway slg = new SoftLayerAPIGateway(vlanRec.getSoftLayerAccount());
		final SoftLayerObjectFilter objFilter = new SoftLayerObjectFilter();
		objFilter.setPropertyFilter("subnets.id", subnetRecMap.keySet());
		final JsonElement slSubnetArrElem = slg.callGetMethod("SoftLayer_Account", "getSubnets", objFilter, 
			"id",
			"networkIdentifier",
			"cidr",
			"ipAddresses.ipAddress",
			"ipAddresses.isReserved",
			"ipAddresses.isGateway",
			"ipAddresses.isBroadcast",
			"ipAddresses.isNetwork");
		
		JsonElement slSubnetElem = null;
		for (final JsonElement subnetObj : slSubnetArrElem.getAsJsonArray()) {
			final String networkAddr = JsonUtil.getStringFromPath(subnetObj, "networkIdentifier");
			final int cidr = JsonUtil.getIntFromPath(subnetObj, "cidr");
			
			if (!IPAddressUtil.isIpInNetwork(ipAddress, networkAddr, cidr)) {
				continue;
			}
			
			slSubnetElem = subnetObj;
			break;
		}
		
		if (slSubnetElem == null) {
			// couldn't find the subnet in softlayer
			return null;
		}
	
		final String networkAddr = JsonUtil.getStringFromPath(slSubnetElem, "networkIdentifier");
		final int cidr = JsonUtil.getIntFromPath(slSubnetElem, "cidr");
		final String slSubnetId = String.valueOf(JsonUtil.getIntFromPath(slSubnetElem, "id"));
	
		final SoftLayerIdObject parentSubnetObj = new SoftLayerIdObject();
		parentSubnetObj.setId(subnetRecMap.get(slSubnetId).getId());
		parentSubnetObj.setSoftLayerId(slSubnetId);
		
		final List<IPAddress> allIPAddress = this.getIPAddressesInSubnet(networkAddr, cidr, new ArrayList<com.ibm.scas.analytics.persistence.beans.IPAddress>(), JsonUtil.getArrayFromPath(slSubnetElem, "ipAddresses"), parentSubnetObj);
		
		for (final IPAddress ipAddrObj : allIPAddress) {
			if (!ipAddrObj.getIpAddress().equals(ipAddress)) {
				continue;
			}
			return ipAddrObj;
		}
		
		// not found in the subnet
		return null;

	}
	
	/**
	 * Method to unreserve a list of IP address IDs
	 * @param ipAddressId
	 * @throws CPEException
	 */
	@Override
	public void unreserveIPAddress(List<String> ipAddressIds) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("unreserveIPAddress(): ipAddressIds: %s", ipAddressIds));
		}
		
		final List<com.ibm.scas.analytics.persistence.beans.IPAddress> ipAddrs = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.IPAddress.class, 
				new WhereInClause("id", ipAddressIds));
	
		final List<String> unreservedIPs = new ArrayList<String>();
		for (final com.ibm.scas.analytics.persistence.beans.IPAddress ipAddr: ipAddrs) {
			unreservedIPs.add(IPAddressUtil.longToIp(ipAddr.getIpAddress()));
			persistence.deleteObject(com.ibm.scas.analytics.persistence.beans.IPAddress.class, ipAddr);
		}
		
		persistence.flush();
		
		logger.debug(String.format("unreserveIPAddress(): Unreserved IPs: %s", unreservedIPs));
	}
	
	/**
	 * Method to unreserve a list of IP addresses by VLAN
	 * @param ipAddressId
	 * @throws CPEException
	 */
	@Override
	public void unreserveIPAddressByAddress(List<String> ipAddress, String vlanId) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("unreserveIPAddressByAddress(): ipAddress: %s, vlan: %s", ipAddress, vlanId));
		}
		
		final List<Long> ipAddressLongList = new ArrayList<Long>(ipAddress.size());
		for (final String ipAddrStr : ipAddress) {
			ipAddressLongList.add(IPAddressUtil.ipToLong(ipAddrStr));
		}
		
		final List<com.ibm.scas.analytics.persistence.beans.IPAddress> ipAddressList = 
			persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.IPAddress.class, 
					new WhereInClause("ipAddress", ipAddressLongList),
					new WhereClause("subnet.vlan.id", vlanId));
		
		final List<String> unreservedIPs = new ArrayList<String>();
		for (final com.ibm.scas.analytics.persistence.beans.IPAddress ipAddr : ipAddressList) {
			unreservedIPs.add(IPAddressUtil.longToIp(ipAddr.getIpAddress()));
			persistence.deleteObject(com.ibm.scas.analytics.persistence.beans.IPAddress.class, ipAddr);
		}	
		
		persistence.flush();
		
		logger.debug(String.format("unreserveIPAddressByAddress(): Unreserved IPs: %s", unreservedIPs));
	}
	
	/**
	 * Method to unreserve a list of IP address IDs
	 * @param ipAddressId
	 * @throws CPEException
	 */
	@Override
	public void unreserveIPAddressByCluster(String clusterId) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("unreserveIPAddressByCluster(): clusterId: %s", clusterId));
		}
		
		// unreserving an IP address is basically just deleting the ip address from the database.  since
		// the ip table is sparse, dropping the record will remove the reservation
		final List<com.ibm.scas.analytics.persistence.beans.IPAddress> ipAddressList = 
			persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.IPAddress.class, 
					new WhereClause("cluster.id", clusterId));
		
		final List<String> unreservedIPs = new ArrayList<String>();
		for (final com.ibm.scas.analytics.persistence.beans.IPAddress ipAddr : ipAddressList) {
			unreservedIPs.add(IPAddressUtil.longToIp(ipAddr.getIpAddress()));
			persistence.deleteObject(com.ibm.scas.analytics.persistence.beans.IPAddress.class, ipAddr);
		}
		
		persistence.flush();
		
		logger.debug(String.format("unreserveIPAddressByCluster(): Unreserved IPs: %s", unreservedIPs));
	}
	
	private List<IPAddress> getIPAddressesInSubnet(String networkIdentifier, int cidr, List<com.ibm.scas.analytics.persistence.beans.IPAddress> reservedIPs, JsonArray slIPAddrArray, SoftLayerIdObject parentSubnetObj) throws CPEException {
		final List<Long> longIPAddrList= IPAddressUtil.expandSubnetToLongArray(networkIdentifier, cidr);
		
		// map the reservedIP records by long
		final Set<String> clusterIDs = new HashSet<String>();
		final Map<Long, com.ibm.scas.analytics.persistence.beans.IPAddress> reservedIPMap = new HashMap<Long, com.ibm.scas.analytics.persistence.beans.IPAddress>();
		for (final com.ibm.scas.analytics.persistence.beans.IPAddress ipAddrRec : reservedIPs) {
			reservedIPMap.put(ipAddrRec.getIpAddress(), ipAddrRec);
			if (ipAddrRec.getCluster() != null) {
				clusterIDs.add(ipAddrRec.getCluster().getId());
			}
		}
		
		// grab all clusters
		final Map<String, Cluster> clusterMap = new HashMap<String, Cluster>();
		if (!clusterIDs.isEmpty()) {
			final List<Cluster> clusters = provisioningService.getClusters(clusterIDs);
			for (final Cluster cluster : clusters) {
				clusterMap.put(cluster.getId(), cluster);
			}
		}
		
		// map the SoftLayer IP address objects by long
		final Map<Long, JsonElement> slIPAddrMap = new HashMap<Long, JsonElement>();
		for (final JsonElement slIPElem : slIPAddrArray) {
			slIPAddrMap.put(IPAddressUtil.ipToLong(JsonUtil.getStringFromPath(slIPElem, "ipAddress")), slIPElem);
		}
		
		final List<IPAddress> toReturn = new ArrayList<IPAddress>(longIPAddrList.size());
		for (final Long longIPAddr : longIPAddrList) {
			final IPAddress ipAddr = new IPAddress();
			ipAddr.setIpAddress(IPAddressUtil.longToIp(longIPAddr));
			ipAddr.setSubnet(parentSubnetObj);
			
			final com.ibm.scas.analytics.persistence.beans.IPAddress ipAddrRec = reservedIPMap.get(longIPAddr);
			if (ipAddrRec != null) {
				// IP is in the database, add the database information
				ipAddr.setCluster(ipAddrRec.getCluster() != null ? clusterMap.get(ipAddrRec.getCluster().getId()) : null);
				ipAddr.setHostname(ipAddrRec.getHostname());
				ipAddr.setTierName(ipAddrRec.getTierName());
				ipAddr.setId(ipAddrRec.getId());
			}
			
			final JsonElement slIPAddrElem = slIPAddrMap.get(longIPAddr);
			if (slIPAddrElem != null) {
				// if the IP is one of the reserved addresses, then mark "isReservable" to be false
				if (JsonUtil.getBooleanFromPath(slIPAddrElem, "isReserved") || 
					JsonUtil.getBooleanFromPath(slIPAddrElem, "isNetwork") ||	
					JsonUtil.getBooleanFromPath(slIPAddrElem, "isGateway") ||	
					JsonUtil.getBooleanFromPath(slIPAddrElem, "isBroadcast")) {
					ipAddr.setReservable(false);
				} else {
					ipAddr.setReservable(true);
				}
			}
			
			
			toReturn.add(ipAddr);
		}
		
		return toReturn;
	}

	@Override
	public List<IPAddress> getIPAddressBySubnet(String subnetId) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("getIPAddressBySubnet(): subnet: %s", subnetId));
		}
		final com.ibm.scas.analytics.persistence.beans.Subnet subnet = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Subnet.class, subnetId);
		if (subnet == null) {
			throw new CPEException(String.format("Subnet %s not found in database!", subnetId));
		}
		
		// these might be null if it's a softlayer subnet, fill it in below
		String networkAddr = subnet.getNetworkAddr();
		int cidr = subnet.getCidr();
		
		final SoftLayerIdObject parentSubnetObj = new SoftLayerIdObject();
		parentSubnetObj.setId(subnet.getId());
		parentSubnetObj.setSoftLayerId(subnet.getSoftLayerId());
		
		// get the softlayer details
		JsonArray slIPAddrArr = null;

		if (subnet.getSoftLayerId() != null) {
			final SoftLayerAPIGateway slg = new SoftLayerAPIGateway(subnet.getSoftLayerAccount());
			final JsonElement slSubnetElem = slg.getObjectById("SoftLayer_Network_Subnet", subnet.getSoftLayerId(), null, null,
				"id",
				"networkIdentifier",
				"cidr",
				"ipAddresses.ipAddress",
				"ipAddresses.isReserved",
				"ipAddresses.isGateway",
				"ipAddresses.isBroadcast",
				"ipAddresses.isNetwork");
			slIPAddrArr = JsonUtil.getArrayFromPath(slSubnetElem, "ipAddresses");
			networkAddr = JsonUtil.getStringFromPath(slSubnetElem, "networkIdentifier");
			cidr = JsonUtil.getIntFromPath(slSubnetElem, "cidr");
		}
		
		final List<com.ibm.scas.analytics.persistence.beans.IPAddress> ipAddrs = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.IPAddress.class, 
				new WhereClause("subnet.id", subnetId));
		
		return this.getIPAddressesInSubnet(networkAddr, cidr, ipAddrs, slIPAddrArr, parentSubnetObj);
	}
	
	/**
	 * Get existing vlan used by subscriber
	 * @param subscriber
	 * @return
	 * @throws CPEException
	 */
	public List<Vlan> getSubscriberVlans(String subscriberId) throws CPEException {
		// cluster is associated to VLAN through IP Addresses
		final List<com.ibm.scas.analytics.persistence.beans.Vlan> vlanRecs = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Vlan.class, 
				new WhereClause("cluster.owner.id", subscriberId));
		
		final Set<String> vlanIds = new HashSet<String>();
		for (final com.ibm.scas.analytics.persistence.beans.Vlan vlanRec : vlanRecs) {
			vlanIds.add(vlanRec.getId());
		}
		
		return new ArrayList<Vlan>(getVlans(vlanIds));
		
	}
	
	/**
	 * Get existing subnet used by cluster
	 * @param subscriber
	 * @return
	 * @throws CPEException
	 */
	@Override
	public List<Subnet> getClusterSubnets(String clusterId) throws CPEException {
		// cluster is associated to VLAN through IP Addresses
		final List<com.ibm.scas.analytics.persistence.beans.IPAddress> ipAddressRecs = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.IPAddress.class, 
				new WhereClause("cluster.id", clusterId));
		
		final Map<String, com.ibm.scas.analytics.persistence.beans.Subnet> subnetMap = new HashMap<String, com.ibm.scas.analytics.persistence.beans.Subnet>();
		for (final com.ibm.scas.analytics.persistence.beans.IPAddress ipAddr : ipAddressRecs) {
			subnetMap.put(ipAddr.getSubnet().getId(), ipAddr.getSubnet());
		}
		
		return getSubnetsFromSubnetRecords(new ArrayList<com.ibm.scas.analytics.persistence.beans.Subnet> (subnetMap.values()));
	}
	
	@Override
	public List<Vlan> getClusterVlans(String clusterId) throws CPEException {
		return this.getClusterVlans(clusterId, null);
	}
	
	/**
	 * Get existing vlan used by cluster
	 * @param subscriber
	 * @return
	 * @throws CPEException
	 */
	@Override
	public List<Vlan> getClusterVlans(String clusterId, String tierName) throws CPEException {
		// cluster is associated to VLAN through IP Addresses
		final Set<String> vlanIds = new HashSet<String>();
		
		final List<WhereClause> wheres = new ArrayList<WhereClause>();
		wheres.add(new WhereClause("cluster.id", clusterId));
		if (tierName != null) {
			wheres.add(new WhereClause("tierName", tierName));
		} else {
			// if no tiername specified, get all vlans associated with this cluster
			final List<com.ibm.scas.analytics.persistence.beans.Vlan> vlanRecs =  persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Vlan.class,
					new WhereClause("cluster.id", clusterId));
			for (final com.ibm.scas.analytics.persistence.beans.Vlan vlanRec : vlanRecs) {
				vlanIds.add(vlanRec.getId());
			}
		}	

		final List<com.ibm.scas.analytics.persistence.beans.IPAddress> ipAddressRecs = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.IPAddress.class, 
				wheres.toArray(new WhereClause[] {}));
		
		for (final com.ibm.scas.analytics.persistence.beans.IPAddress ipAddr : ipAddressRecs) {
			vlanIds.add(ipAddr.getSubnet().getVlan().getId());
		}
	
		return new ArrayList<Vlan>(getVlans(vlanIds));
	}

	@Override
	public List<Gateway> getGateways(GatewayType type) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("getGateways(): type=%s", type));
		}
		
		final List<com.ibm.scas.analytics.persistence.beans.Gateway> allGateways = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Gateway.class,
				new WhereClause("type", type.toString()));
		if (allGateways.size() == 0) {
			return Collections.emptyList();
		}
		
		return getGatewaysFromGatewayRecords(allGateways);	
	}
	
	
	@Override
	public Collection<Gateway> getGateways(Collection<String> gatewayIds) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("getGateways(): type=%s", gatewayIds));
		}
		
		if (gatewayIds.isEmpty()) {
			return Collections.emptyList();
		}
		
		final List<com.ibm.scas.analytics.persistence.beans.Gateway> allGateways = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Gateway.class,
				new WhereInClause("id", gatewayIds));
		if (allGateways.size() == 0) {
			return Collections.emptyList();
		}
		
		return getGatewaysFromGatewayRecords(allGateways);	
	}

	@Override
	public void unassignVlan(String vlanId, boolean force) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("unassibnVlan(): vlanId: %s", vlanId));
		}	
		// caller starts transaction
		final Vlan vlan = this.getVlanById(vlanId);
		if (vlan == null) {
			throw new CPEParamException(String.format("Could not find VLAN with id: %s", vlanId));
		}
		
		if (!force) {
			this.verifyVlanUsedByCluster(vlanId);
		}

		// vlan must be untrunked before returning it to free pool
		if (vlan.getGateway() != null) {
			throw new CPEParamException(String.format("Unable to unassign the VLAN %s (%s.%s). The VLAN is still attached to the gateway %s. Detach the VLAN from the gateway first.", vlanId, vlan.getPrimaryRouter(), vlan.getVlanNumber(), vlan.getGateway().getId()));
		}
		
		final com.ibm.scas.analytics.persistence.beans.Vlan vlanRec = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Vlan.class, vlanId);
		vlanRec.setCluster(null);
		
		persistence.updateObject(com.ibm.scas.analytics.persistence.beans.Vlan.class, vlanRec);
		
		logger.info(String.format("unassignVlan(): Return VLAN %s (%s.%s) to free pool", vlanId, vlan.getPrimaryRouter(), vlan.getVlanNumber()));
	}

	@Override
	public void unassignGateway(String gatewayId) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("unassignGateway(): gatewayId: %s", gatewayId));
		}		
		// caller starts transaction
		final Gateway gw = this.getGatewayById(gatewayId);
		if (gw == null) {
			throw new CPEParamException(String.format("Could not find gateway with id: %s", gatewayId));
		}
		
		// must be a dedicated gateway
		if (gw.getType() != GatewayType.DEDICATED) {
			throw new CPEParamException(String.format("Gateway %s is not a dedicated gateway", gatewayId));
		}
		
		// check there's no vlans on this gateway
		final Set<String> vlanIds = new HashSet<String>();
		final Set<String> vlanNames = new HashSet<String>();
		for (final SoftLayerIdObject vlanIdObj : gw.getAssociatedVlans()) {
			vlanIds.add(vlanIdObj.getId());
			final Vlan vlan = (Vlan) vlanIdObj;
			vlanNames.add(String.format("%s.%s", vlan.getPrimaryRouter(), vlan.getVlanNumber()));
		}
		
		if (!vlanIds.isEmpty()) {
			/* if there's still VLANs, force them to be unassociated first.  this is because dissassociating VLANs is an
			 * asynchronous operation and we can't do them all in this thread. 
			 */
			throw new CPEParamException(String.format("Gateway %s (%s) still has VLANs %s (%s) associated.  Unassociate them first.", gw.getId(), gw.getName(), vlanIds, vlanNames));
		}
	
		final com.ibm.scas.analytics.persistence.beans.Gateway gwRec = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Gateway.class, gatewayId);
		gwRec.setAccount(null);
		
		persistence.updateObject(com.ibm.scas.analytics.persistence.beans.Gateway.class, gwRec);
		
		logger.info(String.format("unassignGateway(): unassigned gateway %s (%s) from account.", gatewayId, gw.getName()));
	}

	@Override
	public List<Gateway> getAccountGateways(String accountId) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("getAccountGateways(): accountId: %s", accountId));
		}			
		// cluster is associated to VLAN through IP Addresses
		final List<com.ibm.scas.analytics.persistence.beans.Gateway> gwRecs = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Gateway.class, 
				new WhereClause("account.id", accountId), new WhereClause("type", GatewayType.DEDICATED.name()));
		
		final Set<String> gwIds = new HashSet<String>();
		for (final com.ibm.scas.analytics.persistence.beans.Gateway gwRec : gwRecs) {
			gwIds.add(gwRec.getId());
		}
	
		return new ArrayList<Gateway>(getGateways(gwIds));
	}

	@Override
	public Map<String, String> getGatewaySSLCertFromDB(String gatewayId) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("getGatewaySSLCertFromDB(): gatewayId: %s", gatewayId));
		}				
		final com.ibm.scas.analytics.persistence.beans.Gateway gw = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Gateway.class, gatewayId);
		if (gw == null) {
			throw new CPEException(String.format("Gateway %s not found!", gatewayId));
		}
		
		final Map<String,String> sslCerts = new HashMap<String, String>();
		for (final GatewayMember member : gw.getGatewayMembers()) {
			sslCerts.put(member.getMemberIp(), member.getSslCert());
		}
		
		return sslCerts;
	}

	@Override
	public void updateGatewaySSLCert(String gatewayId) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("updateGatewaySSLCert(): gatewayId: %s", gatewayId));
		}				
		
		final com.ibm.scas.analytics.persistence.beans.Gateway gw = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Gateway.class, gatewayId);
		if (gw == null) {
			throw new CPEException(String.format("Gateway %s not found!", gatewayId));
		}
		
		// use the gateway object from SoftLayer (in case new members were added)
		final Gateway gatewayObj = this.getGatewayById(gatewayId);
		
		objMember: for (final com.ibm.scas.analytics.beans.GatewayMember member : gatewayObj.getGatewayMembers()) {
			// validate the username/password that is passed in using the Vyatta REST API.  First, collect all the
			// SSL certificates
			final String privateIp = member.getMemberIp();
			final String sslCert = firewallService.getSSLCertificate(privateIp);

			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Retrieved SSL Certificate from Vyatta member %s: %s", privateIp, sslCert));
			}
			
			// find the matching gateway member in the record.  if none exist, add it
			for (final GatewayMember memberRec : gw.getGatewayMembers()) {
				if (!memberRec.getMemberIp().equals(member.getMemberIp())) {
					continue;
				}
				memberRec.setSslCert(sslCert);
				continue objMember;
			}
			
			// if we get here, none of the member records matched, so it's a new member.  add the new member record to the database
			final GatewayMember newMemberRec = new GatewayMember();
			newMemberRec.setMemberIp(privateIp);
			newMemberRec.setSslCert(sslCert);
			gw.getGatewayMembers().add(newMemberRec);
		}
		
		persistence.updateObject(com.ibm.scas.analytics.persistence.beans.Gateway.class, gw);
	}

	@Override
	public void updateGatewayUserPassword(String gatewayId, Gateway gatewayReq) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("updateGatewayUserPassword(): gatewayId: %s, gatewayReq: %s", gatewayId, ReflectionToStringBuilder.toString(gatewayReq)));
		}				
		
		final com.ibm.scas.analytics.persistence.beans.Gateway gw = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Gateway.class, gatewayId);
		if (gw == null) {
			throw new CPEException(String.format("Gateway %s not found!", gatewayId));
		}
		
		// get the gateway details from SL -- build object filter
		final SoftLayerAPIGateway slg = new SoftLayerAPIGateway(gw.getSoftLayerAccount());
		final SoftLayerObjectFilter objFilter = new SoftLayerObjectFilter();
		objFilter.setPropertyFilter("networkGateways.id", new HashSet<String>(Arrays.asList(gw.getSoftLayerId())));

		// add the details from the SoftLayer API
		final JsonElement slGatewayArrElem = slg.callGetMethod(
				"SoftLayer_Account", 
				"getNetworkGateways", 
				objFilter, 
				"id",
				"members.hardware.id",
				"members.hardware.primaryBackendIpAddress"
				);
		if (slGatewayArrElem == null || !slGatewayArrElem.isJsonArray()) {
			throw new CPEException(String.format("Cannot retrieve list of gateways from SoftLayer Account with ID: %s, ID list: %s", gw.getSoftLayerAccount().getId(), gw.getSoftLayerId()));
		}

		JsonObject slGatewayObj = null;
		for (final JsonElement slGatewayObjElem : slGatewayArrElem.getAsJsonArray()) {
			slGatewayObj = slGatewayObjElem.getAsJsonObject();
			break;
		}

		if (slGatewayObj == null) {
			throw new CPEException(String.format("Cannot find gateway %s from SoftLayer Account with ID: %s, SoftLayer ID : %s", gatewayId, 
					gw.getSoftLayerAccount().getId(), gw.getSoftLayerId()));
		}
		
		if (gw.getGatewayMembers() == null) {
			// if no members exist in the record yet
			gw.setGatewayMembers(new ArrayList<GatewayMember>());
		}
		
		// ensure that the gateway record has members as returned from SoftLayer API
		final JsonArray memberArr = JsonUtil.getArrayFromPath(slGatewayObj, "members");
		nextMember: for (final JsonElement memberElem : memberArr) {
			final String privateIp = JsonUtil.getStringFromPath(memberElem, "hardware.primaryBackendIpAddress");
			for (final GatewayMember member : gw.getGatewayMembers()) {
				if (member.getMemberIp().equals(privateIp)) {
					// already exists
					continue nextMember;
				}
			}
			
			// the member is not in my member list
			final GatewayMember newMember = new GatewayMember();
			newMember.setMemberIp(privateIp);
			newMember.setSslCert("");	// invalid SSL Cert for now, ssl cert cannot be null
			gw.getGatewayMembers().add(newMember);
		}
		
		for (final GatewayMember member : gw.getGatewayMembers()) {
			if (gatewayReq.getUsername() != null && gatewayReq.getPassword() != null) {
				// set the password for both members
				member.setUsername(gatewayReq.getUsername());
				member.setPassword(gatewayReq.getPassword());
			} else if (gatewayReq.getGatewayMembers() != null && !gatewayReq.getGatewayMembers().isEmpty()) {
				// user passed in members
				for (final com.ibm.scas.analytics.beans.GatewayMember reqMember : gatewayReq.getGatewayMembers()) {
					if (!reqMember.getMemberIp().equals(member.getMemberIp())) {
						continue;
					}
					if (reqMember.getUsername() != null && reqMember.getPassword() != null) {
						member.setUsername(reqMember.getUsername());
						member.setPassword(reqMember.getPassword());			
					}
				}
			} else {
				// no members were passed in; make sure both members have no user/password configured
				// so the password is retrieved from  SL on demand
				member.setUsername(null);
				member.setPassword(null);
			}
		}	
	
		persistence.saveObject(com.ibm.scas.analytics.persistence.beans.Gateway.class, gw);	
		
		// flush the record so the firewallService can see the changes
		persistence.flush();
		
		// make sure the passwords work
		final Gateway gateway = getGatewaysFromGatewayRecords(Arrays.asList(gw)).get(0);
		firewallService.testGatewayCredentials(gateway);

		// if the passwords work, check if we need to update the gateway to use these credentials for config-sync
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
	
		// if there is no user/password defined for config-sync, we need to update that tree with the user/password for each member
		if (StringUtils.isBlank(vyattaConfigSyncUser) || StringUtils.isBlank(vyattaConfigSyncPasswd)) {
			firewallService.updateGatewayConfigSync(gateway);
		}
	}
	
	/**
	 * Get Vlans trunked to hardware
	 * 
	 * @param account
	 * @param ipAddress
	 * @param vlansToTrunk
	 * @throws CPEException
	 */
	public List<Vlan> getVlanTrunks(String ipAddress) throws CPEException{
		if (logger.isTraceEnabled()) {
			logger.trace("getHardwareTrunkDetails");
		}
		
		final Set<String> slVlanIds = new HashSet<String>();
		
		// look through all my softlayer accounts for the hardware
		final List<SoftLayerAccount> slAccounts = persistence.getAllObjects(SoftLayerAccount.class);
		for (final SoftLayerAccount slAcct : slAccounts) {
			final SoftLayerAPIGateway slg = new SoftLayerAPIGateway(slAcct);
			final JsonElement hardwareElem = slg.callGetMethod("SoftLayer_Hardware", String.format("findByIpAddress/%s", ipAddress), null, HW_OBJ_MASK);
			
			if (hardwareElem == null) {
				continue;
			}		
			
			final JsonObject hardwareObj = hardwareElem.getAsJsonObject();
			final JsonArray insideNetworkComponentsArr = JsonUtil.getArrayFromPath(hardwareObj, "networkComponents");
			
			for (final JsonElement networkComponent : insideNetworkComponentsArr) {
				final JsonObject networkComponentObj = networkComponent.getAsJsonObject();
				if (!networkComponentObj.has("uplinkComponent")) {
					// unplugged network interface
					continue;
				}
				
				if (JsonUtil.getStringFromPath(networkComponentObj, "name").equals("mgmt")) {
					// we don't consider the mgmt interface
					continue;
				}
				
				// add all the trunks on each port
				final JsonArray vlanTrunkArray = JsonUtil.getArrayFromPath(networkComponentObj, "uplinkComponent.networkVlanTrunks");
				for (final JsonElement vlanTrunk : vlanTrunkArray) {
					slVlanIds.add(String.valueOf(JsonUtil.getIntFromPath(vlanTrunk, "networkVlanId")));
				}
			}
		}
		
		if (slVlanIds.isEmpty()) {
			// no VLANs to return
			return Collections.emptyList();
		}
		
		final List<com.ibm.scas.analytics.persistence.beans.Vlan> vlans = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Vlan.class, 
				new WhereInClause("softLayerId", slVlanIds));
		return this.getVlansFromVlanRecords(vlans);
		
		/*
			List<String> trunkedVlans = getNetworkVlanTrunks(account,uplinkComponentId);
			List<String> vlansToTrunk = new ArrayList<String>();
			List<String> vlansToUntrunk = new ArrayList<String>();
			for(String associatedVlan : associatedVlans) {
				if(trunkedVlans.contains(associatedVlan))
					continue;
				else 
					vlansToTrunk.add(associatedVlan);
			}	

			if (vlansToTrunk == null || vlansToTrunk.isEmpty())
				return;

			addNetworkVlanTrunk(account,networkComponentID,vlansToTrunk);
			logger.info(String.format("Trunked VLANs %s to Physical host %s " , vlansToTrunk.toString(), ipAddress));

		//commented the untrunk part for now. Reason being hypervisor can have vlans trunked outside of cpe
		//which would be untrunked via this thread, which is not what we want.
		//so, to untrunk we should get the information of vlan to be untrunked after cluster is deleted.
		//can be included in garbagecollector
		//Trunk part can also be done when cluster is created, but handling in this thread for now does not harm.
		
		/*for(String trunkedVlan : trunkedVlans) {
			if(!associatedVlans.contains(trunkedVlan))
				vlansToUntrunk.add(trunkedVlan);
			else 
				continue;
		}	
		removeNetworkVlanTrunks(account,networkComponentID,vlansToUntrunk);
		logger.info(String.format("Untrunked VLANs %s from Physical host %s " , vlansToUntrunk.toString(), ipAddress));
		*/
	}
	
	public List<String> getNetworkVlanTrunks(SoftLayerAccount account ,String uplinkComponentId) throws CPEException{
		final SoftLayerAPIGateway slg = new SoftLayerAPIGateway(account);
		final String urlPath = "SoftLayer_Network_Component/" + uplinkComponentId + "/getNetworkVlanTrunks";
		final JsonElement vlanTrunkElem = slg.callGetMethod(urlPath);
		 
		if (vlanTrunkElem == null)
				return null;
		
		final List<String> vlanIds = new ArrayList<String>();
		for (final JsonElement vlanTrunk : vlanTrunkElem.getAsJsonArray()){
			final JsonObject vlanTrunkObj = vlanTrunk.getAsJsonObject();
			final String networkVlanId = JsonUtil.getStringFromPath(vlanTrunkObj, "networkVlanId");
			vlanIds.add(networkVlanId);
		}
		return vlanIds;
	}
	
	/**
	 * Method to trunk vlans to the host.  Note that this method will trunk what it can, but since VLAN trunking is
	 * asynchronous, we may only trunk part of the VLANs.  Any VLANs already trunked on the hardware will not be attempted again.
	 * So the caller could try again on a later interval with the same parameters to trunk the rest of the VLANs.  If this
	 * method returns without any errors, then all VLANs were trunked successfully.
	 * 
	 * @param account
	 * @param networkComponentId
	 * @param vlanIDs
	 * @throws CPEException 
	 */
	@Override
	public void addNetworkVlanTrunks(String ipAddress, Set<String> vlanIDs) throws CPEException{
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("addNetworkVlanTrunk(): ip=%s, vlanIDs=%s", ipAddress, vlanIDs));
		}
		
		if (vlanIDs == null || vlanIDs.isEmpty()) {
			return;
		}
		
		// get the VLANs
		final List<Vlan> vlans = new ArrayList<Vlan>(this.getVlans(vlanIDs));
		final EnumMap<VlanNetworkSpace, List<Vlan>> vlanMap = new EnumMap<VlanNetworkSpace, List<Vlan>>(VlanNetworkSpace.class);
		for (final Vlan vlan : vlans) {
			CollectionsUtil.addToMap(vlanMap, vlan.getNetworkSpace(), vlan);
		}

		JsonElement hardwareElem = null;
		
		// look through all my softlayer accounts for the hardware
		final List<SoftLayerAccount> slAccounts = persistence.getAllObjects(SoftLayerAccount.class);
		SoftLayerAPIGateway slg = null;
		for (final SoftLayerAccount slAcct : slAccounts) {
			final SoftLayerAPIGateway s = new SoftLayerAPIGateway(slAcct);
			hardwareElem = s.callGetMethod("SoftLayer_Hardware", String.format("findByIpAddress/%s", ipAddress), null, HW_OBJ_MASK);
			
			if (hardwareElem == null) {
				continue;
			}		
			
			slg = s;
			break;
		}
		
		if (hardwareElem == null) {
			throw new CPEException(String.format("Unable to find hardware with IP Address %s", ipAddress));
		}
		
		final String hostname = JsonUtil.getStringFromPath(hardwareElem, "hostname");
		final Map<Integer, String> componentNameMap = new HashMap<Integer, String>();
		
		// find the uplinkComponents 
		final EnumMap<VlanNetworkSpace, List<JsonElement>> networkComponents = new EnumMap<VlanNetworkSpace, List<JsonElement>>(VlanNetworkSpace.class);
		final JsonArray networkComponentsArray = JsonUtil.getArrayFromPath(hardwareElem, "networkComponents");
		for (final JsonElement networkComponentElem : networkComponentsArray) {
			final JsonObject networkComponentObj = networkComponentElem.getAsJsonObject();
			if (!networkComponentObj.has("uplinkComponent")) {
				// unplugged nic
				continue;
			}
			
			if (JsonUtil.getStringFromPath(networkComponentObj, "name").equals("mgmt")) {
				// BMC
				continue;
			}
			
			final VlanNetworkSpace networkSpace = VlanNetworkSpace.valueOf(JsonUtil.getStringFromPath(networkComponentObj, "uplinkComponent.networkVlan.networkSpace"));
			
			// NOTE: it's possible to have dual NIC bare metal that are both active.  We will trunk all VLANs matching the network
			// space onto all uplink components on this host.
			CollectionsUtil.addToMap(networkComponents, networkSpace, networkComponentObj);
			
			// save the name of the port connected to each uplinkComponent for logging later
			final String name = JsonUtil.getStringFromPath(networkComponentElem, "name");
			final int port = JsonUtil.getIntFromPath(networkComponentElem, "port");
			final int componentId = JsonUtil.getIntFromPath(networkComponentElem, "id");
			componentNameMap.put(componentId, String.format("%s%d", name, port));
		}
		
		for (final VlanNetworkSpace networkSpace : VlanNetworkSpace.values()) {
			final List<Vlan> vlansToTrunk = vlanMap.get(networkSpace);
			final List<JsonElement> networkComponentsToTrunk = networkComponents.get(networkSpace);
			
			if (vlansToTrunk == null || vlansToTrunk.isEmpty()) {
				// nothing to trunk here, move to the next network space
				continue;
			}
			
			if (networkComponentsToTrunk == null || networkComponentsToTrunk.isEmpty()) {
				// this is weird, we want to trunk vlans in some network space, but this host has no interfaces
				// on that network space.  log a warning and skip it			
				
				final Set<String> vlanNames = new HashSet<String>(vlansToTrunk.size());
				for (final Vlan vlan : vlansToTrunk) {
					vlanNames.add(String.format("%s.%s", vlan.getPrimaryRouter(), vlan.getVlanNumber()));
				}

				logger.warn(String.format("Skipping trunk of VLANs %s on hardware %s (%s); no network component available in network space %s", 
						vlanNames, hostname, ipAddress, networkSpace.toString()));
				continue;
			}
			
			for (final JsonElement networkComponent : networkComponentsToTrunk) {
				// prune the list of VLANs to trunk on this component; some of the vlans may already be trunked
				final Map<String, Vlan> vlansToTrunkOnComp = new HashMap<String, Vlan>();
				for (final Vlan vlan : vlansToTrunk) {
					vlansToTrunkOnComp.put(vlan.getSoftLayerId(), vlan);
				}
				
				final JsonArray trunkArr = JsonUtil.getArrayFromPath(networkComponent, "uplinkComponent.networkVlanTrunks");
				for (final JsonElement trunkElem : trunkArr) {
					// already trunked, don't try to trunk again
					final String trunkedVlanId = JsonUtil.getStringFromPath(trunkElem, "networkVlanId");
					vlansToTrunkOnComp.remove(trunkedVlanId);
				}
			
				if (vlansToTrunkOnComp.isEmpty()) {
					// everything was already trunked on this component, move to the next component
					continue;
				}
				
				final Set<String> vlanNames = new HashSet<String>(vlansToTrunk.size());
				for (final Vlan vlan : vlansToTrunkOnComp.values()) {
					vlanNames.add(String.format("%s.%s", vlan.getPrimaryRouter(), vlan.getVlanNumber()));
				}
				
				// ok, now we have a list of vlans to trunk on this component.  now do the work!
				final int networkComponentId = JsonUtil.getIntFromPath(networkComponent, "id");
				logger.debug(String.format("Attempting to trunk VLANs %s to hardware %s on IP %s on %s port %s" , vlanNames, hostname, ipAddress, networkSpace, componentNameMap.get(networkComponentId)));
				final String urlPath = String.format("SoftLayer_Network_Component/%s/addNetworkVlanTrunks", networkComponentId);
				
				final JsonObject parameterObj = new JsonObject();
				final JsonObject dataObject = new JsonObject();
				final JsonArray dataArray = new JsonArray();
				for (final String vlanID : vlansToTrunkOnComp.keySet()){
					dataObject.add("id", new JsonPrimitive(Integer.valueOf(vlanID)));
					dataArray.add(dataObject);
				}
				final JsonArray networkVlansArray = new JsonArray();
				networkVlansArray.add(dataArray);
				parameterObj.add("parameters", networkVlansArray);
				
				slg.callPutMethod(urlPath, parameterObj);
				
				logger.info(String.format("Trunked VLANs %s to hardware %s on IP %s on %s port %s" , vlanNames, hostname, ipAddress, networkSpace, componentNameMap.get(networkComponentId)));
			}
		}
    }
	
	/**
	 * Method to remove trunk
	 * @param account
	 * @param networkComponentId
	 * @param vlanIDs
	 * @throws CPEException 
	 */
	@Override
	public void removeNetworkVlanTrunks(String vlanID, boolean force) throws CPEException{
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("removeNetworkVlanTrunks(): vlanId: %s", vlanID));
		}
		
		// get the Vlan record
		final com.ibm.scas.analytics.persistence.beans.Vlan vlanRec = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Vlan.class, vlanID);
		if (vlanRec == null) {
			throw new CPEException(String.format("Cannot find VLAN with id %s", vlanID));
		}
		
		// get the vlan from softlayer
		final SoftLayerAPIGateway slg = new SoftLayerAPIGateway(vlanRec.getSoftLayerAccount());
		final JsonElement slVlanObj = slg.getObjectById("SoftLayer_Network_Vlan", vlanRec.getSoftLayerId(), null, null, VLAN_OBJECT_MASK);
		
		final JsonArray vlanTrunkArr = JsonUtil.getArrayFromPath(slVlanObj, "networkComponentTrunks");
		if (vlanTrunkArr == null) {
			//nothing to do
			return;
		}
		
		final String primaryRouter = JsonUtil.getStringFromPath(slVlanObj, "primaryRouter.hostname");
		final int vlanNumber = JsonUtil.getIntFromPath(slVlanObj, "vlanNumber");
		
		// loop through all of the network components that have this vlan trunked, and remove them one by one
		for (final JsonElement vlanTrunk : vlanTrunkArr) {
			final int componentId = JsonUtil.getIntFromPath(vlanTrunk, "networkComponent.downlinkComponent.id");
			final String portName = JsonUtil.getStringFromPath(vlanTrunk, "networkComponent.downlinkComponent.name");
			final int portNum = JsonUtil.getIntFromPath(vlanTrunk, "networkComponent.downlinkComponent.port");
			final String hostname = JsonUtil.getStringFromPath(vlanTrunk, "networkComponent.downlinkComponent.hardware.hostname");
			final String hostIPAddr = JsonUtil.getStringFromPath(vlanTrunk, "networkComponent.downlinkComponent.hardware.primaryBackendIpAddress");
			final boolean isNetworkGateway = JsonUtil.getBooleanFromPath(vlanTrunk, "networkComponent.downlinkComponent.hardware.networkGatewayMemberFlag");
			
			if (isNetworkGateway) {
				// don't untrunk from any gateways, there's another call for that
				continue;
			}
			
			final String urlPath = String.format("SoftLayer_Network_Component/%s/removeNetworkVlanTrunks", componentId);
			
			final JsonObject parameterObj = new JsonObject();
			final JsonObject dataObject = new JsonObject();
			final JsonArray dataArray = new JsonArray();
			dataObject.add("id", new JsonPrimitive(Integer.valueOf(vlanRec.getSoftLayerId())));
			dataArray.add(dataObject);
			
			final JsonArray networkVlansArray = new JsonArray();
			networkVlansArray.add(dataArray);
			parameterObj.add("parameters", networkVlansArray);

			logger.debug(String.format("Attempting to untrunk VLAN %s (%s.%d) from host %s (%s) port %s%d" , vlanID, primaryRouter, vlanNumber, hostname, hostIPAddr, portName, portNum));
			
			slg.callPutMethod(urlPath, parameterObj);

			logger.info(String.format("removeNetworkVlanTrunks(): Untrunked VLAN %s (%s.%d) from host %s (%s) port %s%d" , vlanID, primaryRouter, vlanNumber, hostname, hostIPAddr, portName, portNum));
		}
	}

	@Override
	public void testGatewayUserPassword(String gatewayId) throws CPEException {
		final Gateway gateway = this.getGatewayById(gatewayId);
		
		if (gateway == null) {
			throw new CPEException(String.format("Cannot find gateway with ID %s", gatewayId));
		}
		
		try {
			firewallService.testGatewayCredentials(gateway);
		} catch (CPEException e) {
			throw new CPEException(String.format("Unable to authenticate credentials with Vyatta: %s", e.getLocalizedMessage()), e);
		}		
	}

	@Override
	public String fetchPublicVlan(String gatewayId) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("fetchPublicVlan(): shared gatewayId: %s", gatewayId));
		}
		// validate that the gateway is in our database
		final com.ibm.scas.analytics.persistence.beans.Gateway gatewayRec = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Gateway.class, gatewayId); 
		if (gatewayRec == null) {
			throw new CPEParamException(String.format("Gateway %s could not be found!", gatewayId));
		}
		
		final Gateway gateway = getGatewaysFromGatewayRecords(Arrays.asList(gatewayRec)).get(0);
		
		String vlanId = null;
		
		//return the public vlan if gateway already has a public vlan with portable subnet associated with it
		for (final SoftLayerIdObject idObj : gateway.getAssociatedVlans()) {
			final Vlan vlan = (Vlan)idObj;
			//vlan has to be public and on the same pod as the gateway
			if (vlan.getNetworkSpace() == VlanNetworkSpace.PUBLIC && vlan.getPrimaryRouter().equals(gateway.getPrimaryFrontendRouter())) {
				for (final SoftLayerIdObject subnetIdObj : vlan.getSubnets()) {
					Subnet subnet = (Subnet)subnetIdObj;
						//vlan must have portable subnet (i.e either secondary_on_vlan or byoip type)
						if (subnet.getType().equals("SECONDARY_ON_VLAN") || subnet.getType().equals("BYOIP")) {
							vlanId = vlan.getId();
							logger.info(String.format("fetchPublicVlan(): Using the public VLAN %s (%s.%s) already associated to gateway %s (%s)", 
									vlan.getId(), vlan.getPrimaryRouter(), vlan.getVlanNumber(),gateway.getName(), gateway.getId()));
							return vlanId;
						}	
				}	
			}
		}
		//find a vlan from the db that is not assigned to any gateway and subscriber
		final List<com.ibm.scas.analytics.persistence.beans.Vlan> availableVlans = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Vlan.class, 
				new WhereClause("cluster", null), new WhereClause("gateway", null));
		final List<Vlan> allVlans = getVlansFromVlanRecords(availableVlans);
		for(Vlan vlan : allVlans){
			//vlan has to be public and on the same pod as the gateway
			if (vlan.getNetworkSpace() == VlanNetworkSpace.PUBLIC && vlan.getPrimaryRouter().equals(gateway.getPrimaryFrontendRouter())){
				for (final SoftLayerIdObject subnetIdObj : vlan.getSubnets()) {
					Subnet subnet = (Subnet)subnetIdObj;
						//vlan must have portable subnet (i.e either secondary_on_vlan or byoip type)
						if (subnet.getType().equals("SECONDARY_ON_VLAN") || subnet.getType().equals("BYOIP")) {
							vlanId = vlan.getId();
							logger.info(String.format("fetchPublicVlan(): Using the available public VLAN %s (%s.%s) from the db. Will trunk to gateway %s (%s)", 
									vlan.getId(), vlan.getPrimaryRouter(), vlan.getVlanNumber(),gateway.getName(), gateway.getId()));
							return vlanId;
						}	
				}	
			}
		}
		return null;
	}

	@Override
	public void osReload(String gatewayId) throws CPEException {
		final Gateway gw = this.getGatewayById(gatewayId);
		if (gw == null) {
			throw new CPEParamException(String.format("Could not find gateway with id: %s", gatewayId));
		}
		
		final SoftLayerAPIGateway slg = new SoftLayerAPIGateway(gw.getSoftLayerAccount());
		final JsonElement hardwareIdArrayElem = slg.getObjectById("SoftLayer_Network_Gateway", gw.getSoftLayerId(), null, null, "id", "members.hardware.id", "members.hardware.hostname");
		final JsonArray membersArray = JsonUtil.getArrayFromPath(hardwareIdArrayElem, "members");
		for (final JsonElement membersElem : membersArray) {
			final String hostname = JsonUtil.getStringFromPath(membersElem, "hardware.hostname");
			final int id = JsonUtil.getIntFromPath(membersElem, "hardware.id");
			
			final String reloadOSURL = String.format("SoftLayer_Hardware_Server/%d/reloadCurrentOperatingSystemConfiguration", id);
			final JsonArray parametersArray = new JsonArray();
			parametersArray.add(new JsonPrimitive("FORCE"));
			
			final JsonObject parameters = new JsonObject();
			parameters.add("parameters", parametersArray);
			
			logger.debug(String.format("Attempting to OS reload gateway %s member %s (hardware ID %d)", gw.getName(), hostname, id));
			slg.callPutMethod(reloadOSURL, parameters);
			logger.info(String.format("Sent OS reload request to SoftLayer for gateway %s (%s) member %s (hardware ID %d)", gw.getId(), gw.getName(), hostname, id));
		}
	}
}
