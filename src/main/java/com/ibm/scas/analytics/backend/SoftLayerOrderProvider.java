package com.ibm.scas.analytics.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.inject.Inject;
import com.ibm.scas.analytics.beans.Gateway;
import com.ibm.scas.analytics.beans.Gateway.GatewayType;
import com.ibm.scas.analytics.beans.SoftLayerOrder;
import com.ibm.scas.analytics.beans.SoftLayerOrder.SoftLayerHardware;
import com.ibm.scas.analytics.beans.utils.BeanConversionUtil;
import com.ibm.scas.analytics.persistence.PersistenceService;
import com.ibm.scas.analytics.persistence.beans.SoftLayerAccount;
import com.ibm.scas.analytics.persistence.util.WhereClause;
import com.ibm.scas.analytics.utils.CPEException;
import com.ibm.scas.analytics.utils.JsonUtil;
import com.ibm.scas.analytics.utils.SoftLayerObjectFilter;

public class SoftLayerOrderProvider {

	private final static Logger logger = Logger.getLogger(SoftLayerOrderProvider.class);
	
	@Inject private PersistenceService persistence;
	@Inject private NetworkService networkService;
	
	private static final String[] BILLING_ITEMS_OBJ_MASK = {"items.order.id", "items.hostName", "items.domainName", "items.id", "items.billingItem.id"};
	private static final String[] BILLING_OBJ_MASK = {"billingItem.id"};
	
	public List<SoftLayerOrder> getOrdersForCluster(String clusterId) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("%s.getOrdersForCluster(): clusterId=%s", this.getClass().getSimpleName(), clusterId));
		}
		
		final List<com.ibm.scas.analytics.persistence.beans.SoftLayerOrder> recs = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.SoftLayerOrder.class, new WhereClause("cluster.id", clusterId));
		
		if (logger.isTraceEnabled())
			logger.trace(String.format("Found %d order recs for clusterId=%s", recs.size(), clusterId));
		
		final List<SoftLayerOrder> orders = getOrdersBySLOrderRecs(recs);
		
		return orders;
	}
	
	public SoftLayerOrder getOrderBySoftLayerId(String softLayerId) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("%s.getOrderBySoftLayerId(): softLayerId=%s", this.getClass().getSimpleName(), softLayerId));
		}
		
		final List<com.ibm.scas.analytics.persistence.beans.SoftLayerOrder> recs = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.SoftLayerOrder.class, new WhereClause("softLayerId", softLayerId));
		
		if (recs.isEmpty())
			return null;
		
		final List<SoftLayerOrder> orders = getOrdersBySLOrderRecs(recs);

		// only expect 1
		if (orders.isEmpty())
			return null;
		
		return orders.get(0);

	}
	
	private List<SoftLayerOrder> getOrdersBySLOrderRecs(List<com.ibm.scas.analytics.persistence.beans.SoftLayerOrder> recs) throws CPEException {
		final List<SoftLayerOrder> orderDetails = new ArrayList<SoftLayerOrder>(recs.size());
		
		if (recs == null || recs.isEmpty())
			return orderDetails;
		
		for (final com.ibm.scas.analytics.persistence.beans.SoftLayerOrder rec : recs) {
			final SoftLayerOrder order = BeanConversionUtil.convertToBean(rec);
			final SoftLayerAccount slAcct = rec.getSoftLayerAccount();
			final SoftLayerAPIGateway slg = new SoftLayerAPIGateway(slAcct);
			
			final SoftLayerObjectFilter objFilter = new SoftLayerObjectFilter();
			objFilter.setPropertyFilter("items.categoryCode", "server");
			final String slorderId = rec.getSoftLayerId();
			final JsonElement slBillingItemArrElem = slg.callGetMethod(String.format("SoftLayer_Billing_Order/%s", slorderId), "getItems", objFilter, BILLING_ITEMS_OBJ_MASK);

			if (slBillingItemArrElem == null || !slBillingItemArrElem.isJsonArray()) {
				throw new CPEException(String.format("Cannot retrieve list of billing items from SoftLayer Account with ID: %s, for Order: %s", slAcct.getId(), rec.getId()));
			}

			logger.debug(String.format("Got billing item objects from SoftLayer: %s", slBillingItemArrElem.toString()));
			
			order.setHardwares(new ArrayList<SoftLayerHardware>());
			final JsonArray slBillingItemrArr = slBillingItemArrElem.getAsJsonArray();
			for (final JsonElement slBillingItemObjElem : slBillingItemrArr) {
				final JsonObject slBillingItemObj = slBillingItemObjElem.getAsJsonObject();
				final String slBillingItemId = JsonUtil.getStringFromPath(slBillingItemObj, "billingItem.id");
				final SoftLayerHardware hardware = this.getHardware(slAcct, slBillingItemId);
				
				order.getHardwares().add(hardware);
			}			
			
			orderDetails.add(order);
		}
		
		return orderDetails;
	}
	
	private SoftLayerHardware getHardware(SoftLayerAccount slAcct, String billingItemId) throws CPEException {
		final SoftLayerAPIGateway slg = new SoftLayerAPIGateway(slAcct);
		
		final SoftLayerObjectFilter objFilter = new SoftLayerObjectFilter();
		objFilter.setPropertyFilter("hardware.billingItem.id", billingItemId);
		final JsonElement slHardwareArrElem = slg.callGetMethod("SoftLayer_Account", "getHardware", objFilter);

		if (slHardwareArrElem == null || !slHardwareArrElem.isJsonArray()) {
			throw new CPEException(String.format("Cannot retrieve hardware from SoftLayer Account with ID: %s, for billing item: %s", slAcct.getId(), billingItemId));
		}
		
		logger.debug(String.format("Got hardware objects for billing item id %s from SoftLayer: %s", billingItemId, slHardwareArrElem.toString()));
		
		final JsonArray slHardwareArr = slHardwareArrElem.getAsJsonArray();
		for (final JsonElement slHardwareObjElem : slHardwareArr) {
			final JsonObject slHardwareObj = slHardwareObjElem.getAsJsonObject();
			final String hardwareId  = JsonUtil.getStringFromPath(slHardwareObj, "id");
			final String status = JsonUtil.getStringFromPath(slHardwareObj, "hardwareStatus.status");
			final String name  = JsonUtil.getStringFromPath(slHardwareObj, "hostname");

			final SoftLayerHardware hardware = new SoftLayerHardware();
			hardware.setHardwareId(hardwareId);
			hardware.setBillingItemId(billingItemId);
			hardware.setName(name);
			hardware.setStatus(status);
			
			return hardware;
		}
		
		throw new CPEException(String.format("Hardware not found for SoftLayer Account with ID: %s, for billing item: %s", slAcct.getId(), billingItemId));
	}
	
	public void cancelOrdersForCluster(String clusterId) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("%s.cancelOrdersForCluster(): clusterId=%s", this.getClass().getSimpleName(), clusterId));
		}
		final List<Gateway> gateways = this.networkService.getAllGateways();
		
		final Map<String, Gateway> gatewayMap = new HashMap<String, Gateway>();
		for (Gateway gateway : gateways) {
			gatewayMap.put(gateway.getName(), gateway);
		}
		
		final List<SoftLayerOrder> orders = this.getOrdersForCluster(clusterId);
		
		for(SoftLayerOrder order : orders){
			final com.ibm.scas.analytics.persistence.beans.SoftLayerOrder orderRec = BeanConversionUtil.convertToRecord(order);
			final SoftLayerAccount slAcct = orderRec.getSoftLayerAccount();
			final SoftLayerAPIGateway slg = new SoftLayerAPIGateway(slAcct);
			
			hardwareLoop : for (SoftLayerHardware hardware : order.getHardwares()) {
				final String billingItemId = hardware.getBillingItemId();
				final String hardwareId = hardware.getHardwareId();
				
				final Gateway g = gatewayMap.get(hardware.getName());
				
				// In dev environment the order contains both the dgw and sgw.
				// So if the gateway is not of type Dedicated then just continue.
				if (g != null && g.getType() != GatewayType.DEDICATED)
					continue hardwareLoop;
				
				logger.info(String.format("%s.cancelOrdersForCluster(): Billing Item Id is=%s for the hardware=%s", this.getClass().getSimpleName(), billingItemId, hardwareId));
				
				//call the cancelItem api with the billingItemID
				
				final String urlPath = String.format("SoftLayer_Billing_Item/%s/cancelItem", billingItemId);
				
				final JsonObject parameterObj = new JsonObject();
				final JsonArray parameterArray = new JsonArray();
				final JsonPrimitive cancellationFlag = new JsonPrimitive(false);
				final JsonPrimitive reclaimFlag = new JsonPrimitive(true);
				final JsonPrimitive reason = new JsonPrimitive("No longer needed");
				final JsonPrimitive ticketInfo = new JsonPrimitive("Cancellation Request");
				parameterArray.add(cancellationFlag);
				parameterArray.add(reclaimFlag);
				parameterArray.add(reason);
				parameterArray.add(ticketInfo);
				parameterObj.add("parameters", parameterArray);
				
				logger.info(String.format("%s.cancelOrdersForCluster(): Sending the cancellation request for the hardware=id (%s), name (%s) ", this.getClass().getSimpleName(),hardwareId,hardware.getName()));
				slg.callPutMethod(urlPath, parameterObj);
				logger.info(String.format("%s.cancelOrdersForCluster(): The hardware id (%s), name (%s) is cancelled. It will be revoked at the end of the month  ", this.getClass().getSimpleName(),hardwareId,hardware.getName()));
			}
		}
			
	}
	
}
