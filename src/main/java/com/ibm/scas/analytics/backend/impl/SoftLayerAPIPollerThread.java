package com.ibm.scas.analytics.backend.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.ibm.scas.analytics.EngineProperties;
import com.ibm.scas.analytics.backend.SoftLayerAPIGateway;
import com.ibm.scas.analytics.beans.Gateway.GatewayType;
import com.ibm.scas.analytics.persistence.PersistenceException;
import com.ibm.scas.analytics.persistence.PersistenceService;
import com.ibm.scas.analytics.persistence.beans.CPELocation;
import com.ibm.scas.analytics.persistence.beans.Gateway;
import com.ibm.scas.analytics.persistence.beans.SoftLayerAccount;
import com.ibm.scas.analytics.persistence.beans.Subnet;
import com.ibm.scas.analytics.persistence.beans.Vlan;
import com.ibm.scas.analytics.persistence.util.WhereClause;
import com.ibm.scas.analytics.persistence.util.WhereInClause;
import com.ibm.scas.analytics.utils.CPEException;
import com.ibm.scas.analytics.utils.JsonUtil;
import com.ibm.scas.analytics.utils.SoftLayerObjectFilter;

public class SoftLayerAPIPollerThread implements Runnable {
	private static final Logger logger = Logger.getLogger(SoftLayerAPIPollerThread.class);
	private boolean isRunning;

	@Inject private PersistenceService persistence;
	private final static String cpeLocationName = EngineProperties.getInstance().getProperty(EngineProperties.CPE_LOCATION_NAME, EngineProperties.DEFAULT_CPE_LOCATION_NAME);
		
	public SoftLayerAPIPollerThread() {
		isRunning = false;
	}
	
	public boolean isRunning() {
		return isRunning;
	}

	/* NOTE: @Transactional only works on public, protected, and package-private methods.  */
	@Transactional
	void syncGatewayVlansInDB(JsonElement gatewaysArrElem, Set<String> dbGatewaySlids, String accountID) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace("syncGatewayVlansInDB()");
		}
		
		if (gatewaysArrElem == null || !gatewaysArrElem.isJsonArray()) {
			throw new CPEException(String.format("Cannot retrieve list of Gateways from SoftLayer"));
		}
		
		final CPELocation locationRec = persistence.getObjectById(CPELocation.class, cpeLocationName);
		if (locationRec == null) {
			throw new CPEException(String.format("Cannot retrieve location record for %s", cpeLocationName));
		}
		
		final SoftLayerAccount softLayerAccount = persistence.getObjectById(SoftLayerAccount.class, accountID);
		if (softLayerAccount == null) {
			throw new CPEException(String.format("Cannot retrieve SoftLayer Account record for %s", accountID));
		}
		
		final SoftLayerAPIGateway slgw = new SoftLayerAPIGateway(softLayerAccount);	
		
		final Set<String> slGatewayids = new HashSet<String>();
		final Map<String,Set<String>> slvlanIdMap = new HashMap<String,Set<String>>();  //map vlans by gateway id
		final JsonArray slGatewayArr = gatewaysArrElem.getAsJsonArray();
				
		if (dbGatewaySlids == null || dbGatewaySlids.isEmpty()) {
			// no gateways in our database; nothing to do
			return;
		}
		
		for (final JsonElement slGatewayObjElem : slGatewayArr) {
			final JsonObject slGatewayObj = slGatewayObjElem.getAsJsonObject();
			final String slGatewayId = JsonUtil.getStringFromPath(slGatewayObj, "id");
			final Set<String> slVlanIds = new HashSet<String>();

			if (!dbGatewaySlids.contains(slGatewayId)){
				// gateway not in our database, don't need to handle it
				continue;
			}

			// discover VLANs in softlayer inside this gateway
			final JsonArray insideVlanArr = JsonUtil.getArrayFromPath(slGatewayObj, "insideVlans");
			for (final JsonElement insideVlanElem : insideVlanArr) {
				final JsonObject insideVlanObj = insideVlanElem.getAsJsonObject();
				final JsonObject slNetworkVlanObj = JsonUtil.getObjFromPath(insideVlanObj, "networkVlan");
				final JsonElement idElem = slNetworkVlanObj.get("id");
				final String vlanId = idElem.getAsString();
				slVlanIds.add(vlanId);
			}	
			
			slGatewayids.add(slGatewayId);
			slvlanIdMap.put(slGatewayId, slVlanIds);

			//dbGatewayId - gateway softlayerid from db
			//gatewayDbId - gateway id from db

			final List<Gateway> gateways = persistence.getObjectsBy(Gateway.class, new WhereInClause("softLayerId", dbGatewaySlids));
			for (final Gateway gatewayRec : gateways) {
				if (gatewayRec.getType().equals(GatewayType.MANAGEMENT.name())) {
					// don't sync up VLAN records for management gateways
					continue;
				}
				final String gatewayDbId = gatewayRec.getId();
				final List<Vlan> dbVlans = persistence.getObjectsBy(Vlan.class, new WhereClause("gateway.id", gatewayDbId));
				final Set<String> dbVlanSlIds = new HashSet<String>();

				if (dbVlans != null) {
					for (final Vlan vlan : dbVlans) {
						dbVlanSlIds.add(vlan.getSoftLayerId());
					}
				}

				// list of vlans on the softlayer side
				final Set<String> slvlanIds = slvlanIdMap.get(gatewayRec.getSoftLayerId());

				if (slvlanIds != null) {
					for (final String slvlanID : slvlanIds) {
						if(dbVlanSlIds.contains(slvlanID)){
							// vlan already there, nothing to do
							continue;
						}
	
						//add vlan to the database
						final List<Vlan> vlans = persistence.getObjectsBy(Vlan.class, new WhereClause("softLayerId", slvlanID));
						final Vlan vlan;
						if (vlans.isEmpty()) {
							// an unmanaged VLAN is added to the gateway, now it's managed by us
							vlan = new Vlan();
						} else {
							// vlan is already in our DB, it was just trunked to another gateway in the last interval
							vlan = vlans.get(0);
						}
						
						final Gateway gateway = new Gateway();
					
						// get the actual object from SoftLayer for logging
						final JsonElement vlanElem = slgw.getObjectById("SoftLayer_Network_Vlan", slvlanID, null, null, "id", "primaryRouter.hostname", "vlanNumber");
						if (vlanElem == null) {
							// strange error, perhaps will deal with it in next interval
							throw new CPEException(String.format("syncGatewayVlansInDB(): Could not retrieve VLAN with SoftLayer ID %s", slvlanID));
						}
						
						gateway.setId(gatewayDbId);
						
						vlan.setSoftLayerId(slvlanID);
						vlan.setGateway(gateway);
						vlan.setSoftLayerAccount(softLayerAccount);
						vlan.setLocation(locationRec);
						
						persistence.saveObject(Vlan.class, vlan);
						
						final String primaryRouter = JsonUtil.getStringFromPath(vlanElem, "primaryRouter.hostname");
						final int vlanNumber = JsonUtil.getIntFromPath(vlanElem, "vlanNumber");
						
						if (vlans.isEmpty()) {
							// a new VLAN we didn't know about got trunked to our gateway
							logger.info(String.format("syncGatewayVlansInDB(): Added VLAN %s.%d to database with ID %s on gateway %s (SoftLayer ID: %s)", primaryRouter, vlanNumber, vlan.getId(), gateway.getId(), slvlanID));
						} else {
							// a VLAN we already know about switched trunks to our gateway
							logger.info(String.format("syncGatewayVlansInDB(): Updated VLAN %s.%d (ID %s) association to gateway %s (SoftLayer ID: %s)", primaryRouter, vlanNumber, vlan.getId(), gateway.getId(), slvlanID));
						}
					}
				}

				for (final String dbVlanID : dbVlanSlIds){
					if (slvlanIds != null && slvlanIds.contains(dbVlanID)) {
						// db vlan is also trunked to this gateway in softlayer, nothing to do
						continue;
					}
					
					final List<Vlan> vlans = persistence.getObjectsBy(Vlan.class, new WhereClause("softLayerId", dbVlanID));
					
					// get the actual object from SoftLayer for logging
					final JsonElement vlanElem = slgw.getObjectById("SoftLayer_Network_Vlan", dbVlanID, null, null, "id", "primaryRouter.hostname", "vlanNumber", "attachedNetworkGateway.id");
					if (vlanElem == null) {
						// delete vlan from database, it's not on softlayer anymore
						logger.warn(String.format("syncGatewayVlansInDB(): VLAN with SoftLayer ID %s has been deleted.", dbVlanID));
						persistence.deleteObject(Vlan.class, vlans.get(0));
						continue;
					}
					
					final String primaryRouter = JsonUtil.getStringFromPath(vlanElem, "primaryRouter.hostname");
					final int vlanNumber = JsonUtil.getIntFromPath(vlanElem, "vlanNumber");
					final Integer newGatewayId = JsonUtil.getIntFromPath(vlanElem, "attachedNetworkGateway.id");
					if (newGatewayId == null) {
						// vlan got untrunked and is now floating
						logger.info(String.format("syncGatewayVlansInDB(): VLAN %s.%d (%s) became disassociated from gateway %s.", primaryRouter, vlanNumber, vlans.get(0).getId(), dbVlanID));
						vlans.get(0).setGateway(null);
						persistence.updateObject(Vlan.class, vlans.get(0));
						
						// TODO: remove the VIF from the gateway
						
						// TODO: check if VLAN was hosting any clusters; raise alert if it was.  this removes that cluster from any firewalls
						
						continue;
					} 
					
					// check if we have the gateway in our database
					final List<Gateway> newGatewayRecs = persistence.getObjectsBy(Gateway.class, new WhereClause("softLayerId", String.valueOf(newGatewayId)));
					if (newGatewayRecs.isEmpty()) {
						// vlan got trunked to a gateway we don't know about.  delete the record
						logger.info(String.format("syncGatewayVlansInDB(): VLAN %s.%d (%s) has been trunked to a gateway not managed by CPE (SoftLayer ID: %s).  The VLAN is removed from the database.", primaryRouter, vlanNumber, vlans.get(0).getId(), newGatewayId));
						persistence.deleteObject(Vlan.class, vlans.get(0));
						
						// TODO: check if VLAN was hosting any clusters, raise alert if it was
					}
					
					// if the gateway record is in the database, then the thread will be handling it in a different loop
				}
			}
		}
		
		// check if any gateways have disappeared
		for (final String dbGatewaySLId : dbGatewaySlids) {
			if (slGatewayids.contains(dbGatewaySLId)) {
				// gateway is still there; nothing to do
				continue;
			}
			
			// a gateway has disappeared
			final List<Gateway> gatewayRecs = persistence.getObjectsBy(Gateway.class, new WhereClause("softLayerId", dbGatewaySLId));
			final Gateway disappearedGW = gatewayRecs.get(0);
			
			logger.info(String.format("syncGatewayVlansInDB(): Gateway %s (SoftLayer ID %s) has been removed from SoftLayer.  The Gateway is removed from the database.", disappearedGW.getId(), disappearedGW.getSoftLayerId()));
			// get all of the gateway's associated VLANs from the database
			final List<Vlan> dbAssociatedVlans = persistence.getObjectsBy(Vlan.class, new WhereClause("gateway.id", disappearedGW.getId()));
			final Map<String, Vlan> dbAssociatedVlanIds = new HashMap<String, Vlan>();
			for (final Vlan vlanRec : dbAssociatedVlans) {
				dbAssociatedVlanIds.put(vlanRec.getSoftLayerId(), vlanRec);
			}
			
			// check which vlans are still on softlayer
			final SoftLayerObjectFilter vlanFilter = new SoftLayerObjectFilter();
			vlanFilter.setPropertyFilter("networkVlans.id", dbAssociatedVlanIds.keySet());
			
			final JsonElement vlanArrElem = slgw.callGetMethod("SoftLayer_Account", "getNetworkVlans", vlanFilter, "id", "primaryRouter.hostname", "vlanNumber", "attachedNetworkGateway.id");
			for (final JsonElement vlanElem : vlanArrElem.getAsJsonArray()) {
				final String vlanId = String.valueOf(JsonUtil.getIntFromPath(vlanElem, "id"));
				final String primaryRouter = JsonUtil.getStringFromPath(vlanElem, "primaryRouter.hostname");
				final int vlanNumber = JsonUtil.getIntFromPath(vlanElem, "vlanNumber");
				
				if (!dbAssociatedVlanIds.containsKey(vlanId)) {
					// SoftLayer API returned extra VLANs?  this shouldn't happen
					continue;
				}
				
				// the VLAN is still on SoftLayer -- keep the record if we can
				final Vlan oldVlanRec = dbAssociatedVlanIds.remove(vlanId);
			
				// check if the gateway is in our DB
				final Integer newGatewayId = JsonUtil.getIntFromPath(vlanElem, "associatedNetworkGateway.id");
				if (newGatewayId == null) {
					logger.info(String.format("syncGatewayVlansInDB(): VLAN %s.%d (%s) became disassociated from a deleted gateway %s (SoftLayer ID: %s).  The VLAN is now moving to the free pool.", primaryRouter, vlanNumber, oldVlanRec.getId(), vlanId));
					oldVlanRec.setGateway(null);
					persistence.updateObject(Vlan.class, oldVlanRec);
					
					// TODO: check if VLAN was hosting any clusters; raise alert if it was.  this removes that cluster from any firewalls
					continue;
				}
				
				// the vlan got associated with another gateway.  check if it's in the db.  if not, delete the vlan
				final List<Gateway> newGateway = persistence.getObjectsBy(Gateway.class, new WhereClause("softLayerId", String.valueOf(newGatewayId)));
				if (newGateway.isEmpty()) {
					// vlan got trunked to a gateway we don't know about.  delete the vlan record
					logger.info(String.format("syncGatewayVlansInDB(): VLAN %s.%d (%s) has been trunked to a gateway not managed by CPE (SoftLayer ID: %s).  The VLAN is removed from the database.", primaryRouter, vlanNumber, oldVlanRec.getId(), newGatewayId));
					persistence.deleteObject(Vlan.class, oldVlanRec);
						
					// TODO: check if VLAN was hosting any clusters, raise alert if it was				
				}
				
				// if the new gateway's record is in the database, then the thread will be handling it in a different loop
			}
				
			// handle inside VLANs on the deleted gateway that disappeared from SoftLayer.  delete the records
			// first, delete any subnets that were on the VLAN
			for (final Entry<String, Vlan> deletedVlanRecEnt : dbAssociatedVlanIds.entrySet()) {
				// TODO: check if VLAN was hosting any clusters, raise alert if it was				
				
				final Vlan vlanRec = deletedVlanRecEnt.getValue();
				final List<Subnet> subnets = persistence.getObjectsBy(Subnet.class, new WhereClause("vlan.softLayerId", deletedVlanRecEnt.getKey()));
				for (final Subnet subnetRec : subnets) {
					logger.info(String.format("syncGatewayVlansInDB(): Gateway %s associated VLAN %s subnet %s has been removed from SoftLayer.  The Gateway is removed from the database.", disappearedGW.getId(), vlanRec.getId(), subnetRec.getId()));
					persistence.deleteObject(Subnet.class, subnetRec);
				}
				
				logger.info(String.format("syncGatewayVlansInDB(): Gateway %s associated VLAN %s has been removed from SoftLayer.  The Gateway is removed from the database.", disappearedGW.getId(), vlanRec.getId()));
				persistence.deleteObject(Vlan.class, vlanRec);
			}
				
			logger.info(String.format("syncGatewayVlansInDB(): Gateway %s (SoftLayer ID %s) has been removed from SoftLayer.  The Gateway is removed from the database.", disappearedGW.getId(), disappearedGW.getSoftLayerId()));
			persistence.deleteObject(Gateway.class, disappearedGW);		
		}
	}
	
	/* NOTE: @Transactional only works on public, protected, and package-private methods.  */
	@Transactional
	void syncVlanSubnetsInDB(JsonElement vlansArrElem, Set<String> dbVlanSlIds, String accountID) throws CPEException{
		if (logger.isTraceEnabled()) {
			logger.trace("syncVlanSubnetsInDB()");
		}

		if (vlansArrElem == null || !vlansArrElem.isJsonArray()) {
			throw new CPEException(String.format("Cannot retrieve list of Vlans from SoftLayer"));
		}

		final Set<String> slVlanids = new HashSet<String>();
		final Map<String,Set<String>> slsubnetIdMap = new HashMap<String,Set<String>>();  //map subnets by vlan id
		final JsonArray slVlanArr = vlansArrElem.getAsJsonArray();

		if (dbVlanSlIds == null || dbVlanSlIds.isEmpty()) {
			// no VLANs in the database; nothing to do
			return;
		}
		
		for (final JsonElement slVlanObjElem : slVlanArr) {
			final JsonObject slVlanObj = slVlanObjElem.getAsJsonObject();
			final String slVlanId = JsonUtil.findPropertyPathElem(slVlanObj, "id").getAsString();
			final Set<String> slSubnetIds = new HashSet<String>();
			if (!dbVlanSlIds.contains(slVlanId)){
				// a vlan that is not in our database, don't need to worry about it
				continue;
			}
			
			final JsonArray insideSubnetArr = JsonUtil.getArrayFromPath(slVlanObj, "subnets");
			for (final JsonElement insideVlanElem : insideSubnetArr) {
				final JsonObject insideVlanObj = insideVlanElem.getAsJsonObject();
				final JsonElement idElem = insideVlanObj.get("id");
				final String subnetId = idElem.getAsString();
				slSubnetIds.add(subnetId);
			}	
			slVlanids.add(slVlanId);
			slsubnetIdMap.put(slVlanId, slSubnetIds);
		}

		//dbVlanId - vlan softlayerid field from db
		//vlanDbId - vlan id field from db
		
		final List<Vlan> vlanRecs = persistence.getObjectsBy(Vlan.class, new WhereInClause("softLayerId", dbVlanSlIds));
		for (final Vlan vlanRec : vlanRecs) {
			final String vlanDbId = vlanRec.getId();
			
			final List<Subnet> dbSubnets = persistence.getObjectsBy(Subnet.class, new WhereClause("vlan.id", vlanDbId));
			final Set<String> dbSubnetSlIds = new HashSet<String>();
			if (dbSubnets != null) {
				for (final Subnet subnet : dbSubnets) {
					dbSubnetSlIds.add(subnet.getSoftLayerId());
				}
			}
			
			// list of softlayer subnet IDs on this vlan
			final Set<String> slsubnetIds = slsubnetIdMap.get(vlanRec.getSoftLayerId());
			if (slsubnetIds != null) {
				for (final String slsubnetID : slsubnetIds) {
					if (dbSubnetSlIds.contains(slsubnetID)) {
						// already have this subnet in the database, no action
						continue;
					}
					
					//add subnet to the database
					final Subnet subnet = new Subnet();
					final Vlan vlan = new Vlan();
					final SoftLayerAccount softLayerAccount = new SoftLayerAccount();
					softLayerAccount.setId(accountID);
					vlan.setId(vlanDbId);
					subnet.setSoftLayerId(slsubnetID);
					subnet.setVlan(vlan);
					subnet.setSoftLayerAccount(softLayerAccount);
					persistence.saveObject(Subnet.class, subnet);
					
					logger.info(String.format("syncVlanSubnetsInDB(): Added Subnet to database: %s (SoftLayer ID: %s)", subnet.getId(), slsubnetID));
				}
			}
			
			for (final String dbSubnetID : dbSubnetSlIds){
				if (slsubnetIds.contains(dbSubnetID)) {
					// this subnet also in softlayer, continue;
					continue;
				}
				
				//delete subnet from database
				final List<Subnet> subnets = persistence.getObjectsBy(Subnet.class, new WhereClause("softLayerId",dbSubnetID));
				logger.info(String.format("syncVlanSubnetsInDB(): Deleting Subnet from database: %s (SoftLayer ID: %s)", subnets.get(0).getId(), dbSubnetID));
				persistence.deleteObject(Subnet.class, subnets.get(0));
			}
		}
		
		// check if any vlans have disappeared
		for (final String dbVlanSLId : dbVlanSlIds) {
			if (slVlanids.contains(dbVlanSLId)) {
				// vlan is still there; nothing to do
				continue;
			}
			
			//  the vlan has disappeared from SoftLayer.  clean up the object.  raise alert if a cluster was on the vlan
			final List<Vlan> deletedVlanRec = persistence.getObjectsBy(Vlan.class, new WhereClause("softLayerId", dbVlanSLId));
			if (deletedVlanRec.isEmpty()) {
				continue;
			}
			final List<Subnet> subnets = persistence.getObjectsBy(Subnet.class, new WhereInClause("vlan.softLayerId", deletedVlanRec));
			for (final Subnet subnetRec : subnets) {
				logger.info(String.format("syncVlanSubnetsInDB(): Subnet %s on removed VLAN %s has been removed from SoftLayer.  The Subnet is removed from the database.", subnetRec.getId(), deletedVlanRec.get(0).getId()));
				persistence.deleteObject(Subnet.class, subnetRec);
			}
			
			logger.info(String.format("syncVlanSubnetsInDB(): VLAN %s has been removed from SoftLayer.  The VLAN is removed from the database.", deletedVlanRec.get(0).getId()));
			persistence.deleteObject(Vlan.class, deletedVlanRec.get(0));
		}
	
	}
	
	public void run() {
		isRunning = true;
		
		try {
			List<SoftLayerAccount> accounts;
			try {
				accounts = persistence.getAllObjects(SoftLayerAccount.class);
			} catch (PersistenceException e1) {
				logger.error(e1);
				return;
			}

			for (final SoftLayerAccount account : accounts) {
				final SoftLayerAPIGateway sgw = new SoftLayerAPIGateway(account);
				try {
					final List<Gateway> gateways = persistence.getObjectsBy(Gateway.class, new WhereClause("softLayerAccount.id", account.getId()));
					final Set<String> gatewayIds = new HashSet<String>(gateways.size());
					for (final Gateway gateway : gateways) {
						if (gateway.getSoftLayerId() != null) {
							gatewayIds.add(gateway.getSoftLayerId());
						}
					}

					if (!gatewayIds.isEmpty()) {
						// get the gateway objects from SoftLayer
						final SoftLayerObjectFilter gwObjFilter = new SoftLayerObjectFilter();
						gwObjFilter.setPropertyFilter("networkGateways.id", gatewayIds);

						//final JsonElement gatewaysArrElem = sgw.callGetMethod("SoftLayer_Account", "getNetworkGateways", gwObjFilter, "id", "insideVlans.id");
						final JsonElement gatewaysArrElem = sgw.callGetMethod("SoftLayer_Account", "getNetworkGateways", gwObjFilter, "id", "insideVlans.networkVlan.id");
						logger.debug(String.format("Got gateway objects from SoftLayer: %s", gatewaysArrElem.toString()));

						// TODO: store it in cache?

						// update DB with new VLANs
						syncGatewayVlansInDB(gatewaysArrElem, gatewayIds, account.getId());

					}
					
					final List<com.ibm.scas.analytics.persistence.beans.Vlan> vlans = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Vlan.class, new WhereClause("softLayerAccount.id", account.getId()));
					final Set<String> vlanIds = new HashSet<String>(vlans.size());
					for (final com.ibm.scas.analytics.persistence.beans.Vlan vlan : vlans) {
						if (vlan.getSoftLayerId() != null) {
							vlanIds.add(vlan.getSoftLayerId());
						}
					}

					if (!vlanIds.isEmpty()) {
						// get the vlan objects from SoftLayer
						final SoftLayerObjectFilter vlanObjFilter = new SoftLayerObjectFilter();
						vlanObjFilter.setPropertyFilter("networkVlans.id", vlanIds);

						final JsonElement vlansArrElem = sgw.callGetMethod("SoftLayer_Account", "getNetworkVlans", vlanObjFilter, "id", "subnets.id");
						logger.debug(String.format("Got vlan objects from SoftLayer: %s", vlansArrElem.toString())); 

						// TODO: store it in cache?

						// update DB with new Subnets
						syncVlanSubnetsInDB(vlansArrElem, vlanIds,account.getId());
					}
				} catch (PersistenceException e) {
					logger.error(e.getLocalizedMessage(), e);
					return;
				} catch (CPEException e) {
					logger.error(e.getLocalizedMessage(), e);
					return;
				}
			}
		} catch (RuntimeException e) {
			logger.error(String.format("Abnormal thread exit: %s", e.getLocalizedMessage()), e);
		} finally {
			persistence.clear();
		}

	}
}
