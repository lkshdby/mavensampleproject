package com.ibm.scas.analytics.backend.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import com.ibm.scas.analytics.backend.FirewallService;
import com.ibm.scas.analytics.backend.NetworkService;
import com.ibm.scas.analytics.backend.ProvisioningService;
import com.ibm.scas.analytics.backend.SoftLayerOrderProvider;
import com.ibm.scas.analytics.backend.monitoring.NagiosEventLogger;
import com.ibm.scas.analytics.beans.Cluster.ClusterStep;
import com.ibm.scas.analytics.beans.Gateway;
import com.ibm.scas.analytics.beans.Gateway.GatewayType;
import com.ibm.scas.analytics.beans.Subnet;
import com.ibm.scas.analytics.beans.Vlan;
import com.ibm.scas.analytics.beans.Vlan.VlanNetworkSpace;
import com.ibm.scas.analytics.persistence.PersistenceException;
import com.ibm.scas.analytics.persistence.PersistenceService;
import com.ibm.scas.analytics.persistence.beans.Account;
import com.ibm.scas.analytics.persistence.beans.Cluster;
import com.ibm.scas.analytics.persistence.beans.Offering;
import com.ibm.scas.analytics.persistence.beans.SoftLayerOrder;
import com.ibm.scas.analytics.persistence.beans.Subscriber;
import com.ibm.scas.analytics.persistence.util.WhereClause;
import com.ibm.scas.analytics.provider.NameValuePair;
import com.ibm.scas.analytics.utils.CPEException;
import com.ibm.scas.analytics.utils.ReadWriteLockTable;

@Singleton
public class ClusterGarbageCollector implements Runnable {
	private static final Logger logger = Logger.getLogger(ClusterGarbageCollector.class);
	private static final ConcurrentLinkedQueue<String> clusterIdsToSelect = new ConcurrentLinkedQueue<String>();
	
	@Inject private PersistenceService persistence;
	@Inject private ProvisioningService engine;
	@Inject private NetworkService networkService;
	@Inject private FirewallService firewallService;
	@Inject private ReadWriteLockTable<String> locktable;
	@Inject private SoftLayerOrderProvider orderProvider;
	
	private boolean isRunning;
	
	public ClusterGarbageCollector() {
		isRunning = false;
	}
	
	public void addClusterId(String clusterId) {
		synchronized(clusterIdsToSelect) {
			if (clusterIdsToSelect.contains(clusterId))
				return; //already handling this cluster so return;
			
			clusterIdsToSelect.add(clusterId);
		}
	}
	
	public boolean isRunning() {
		return isRunning;
	}

	@Transactional
	public void afterInject() throws CPEException {
		logger.info("afterInject(): Setting up garbage collection...");
		List<Offering> offerings;
		try {
			offerings = persistence.getAllObjects(Offering.class);
		} catch (PersistenceException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw e;
		}

		for (final Offering offering : offerings) {
			final String offeringId = offering.getId();
			final List<Subscriber> garbageCollectors = persistence.getObjectsBy(Subscriber.class, new WhereClause("account.offering.id", offeringId), new WhereClause("type", Subscriber.GARBAGE_COLLECTOR));
			if (garbageCollectors != null && !garbageCollectors.isEmpty()) {
				continue;
			}
			logger.info("No garbage collector defined for offering \"" + offering.getName() + "\". Creating one...");
			try {
				/*
				 * create account
				 */
				Account account = new Account();
				account.setOffering(offering);
				account.setMarketUrl(Account.SYSTEM_URL);
				account.setPartner(Account.SYSTEM_PARTNER);

				persistence.saveObject(Account.class, account);

				/*
				 * create first subscriber
				 */
				Subscriber subscriber = new Subscriber();
				subscriber.generateRandomApiKey();
				subscriber.setAccount(account);
				subscriber.setType(Subscriber.GARBAGE_COLLECTOR);
				subscriber.setExternalId("");
				subscriber.setName("Garbage Collector");
				persistence.saveObject(Subscriber.class, subscriber);
			} catch (PersistenceException e) {
				logger.error(e);
				throw e;
			}
		}
		
		// Build-up list of clusters to delete on start-up
		final List<WhereClause> wheres = new ArrayList<WhereClause>();
		wheres.add(new WhereClause("owner.type", Subscriber.GARBAGE_COLLECTOR, true));
		
		try {
			final List<Cluster> clusters = persistence.getObjectsBy(Cluster.class, wheres.toArray(new WhereClause[] {}));
			
			for (Cluster c : clusters) {
				addClusterId(c.getId());
			}
		} catch (PersistenceException e) {
			logger.error("Unable to retrieve clusters from database", e);
		}
	}

	@Override
	public void run() {
		isRunning = true;
		try {
			if (clusterIdsToSelect.isEmpty()) {
				return;
			}
			
			logger.debug(String.format("Performing garbage collection on clusters: %s ", clusterIdsToSelect));

			for (Iterator<String> it = clusterIdsToSelect.iterator(); it.hasNext(); ) {
				final String clusterId = it.next();
				final Lock lock = this.locktable.getLockWithAdd(clusterId).writeLock();
				final Cluster clusterRec;
				
				try {
					logger.debug("Locking on cluster " + clusterId + " ...");
					lock.lock();
					logger.debug("Lock acquired on cluster " + clusterId);

					clusterRec = persistence.getObjectById(Cluster.class, clusterId);
							
					if (clusterRec == null) {
						logger.info(String.format("Cluster record for id %s has been removed. Remove it from my list.", clusterId));
						it.remove();
						continue;
					}
					
					try {
						logger.debug(String.format("Performing step %s for cluster %s (%s)...", clusterRec.getCurrentStep(), clusterRec.getName(), clusterRec.getId()));
						performClusterStep(clusterRec);
						logger.debug("Done performClusterStep().");
					} catch (CPEException e) {
						logger.error(String.format("Unable to garbage collect cluster %s: %s", clusterId, e.getLocalizedMessage()), e);
					}
				} catch (PersistenceException e) {
					logger.error(String.format("Unable to garbage collect cluster %s: %s", clusterId, e.getLocalizedMessage()), e);
				} finally {
					lock.unlock();
					logger.debug("Lock released for cluster " + clusterId + " ...");
				}
			}
		} catch (RuntimeException e) {
			// prevent thread death
			logger.error(String.format("Abnormal thread exit: %s", e.getLocalizedMessage()), e);
		} finally {
			// drop the session cache to force refresh next time
			persistence.clear();
		}
	}


	void cancelCluster(Offering offering, Cluster cluster) throws CPEException {
		final String clusterId = cluster.getClusterId();
		
		final String pcmaeClusterId = clusterId != null ? clusterId : null;
		final com.ibm.scas.analytics.beans.Cluster clusterDetails = engine.getClusterDetails(cluster.getId());
		
		String clusterState = null;
		String clusterAction = null;		
		if (clusterDetails != null) {
			// cluster state is stored in the cluster details

			final List<NameValuePair> attrs = clusterDetails.getDetails();
			if (attrs != null) {
				for (final NameValuePair attr : attrs) {
					if (attr.getName().equals("State")) {
						clusterState = attr.getValue();
					}
					if (attr.getName().equals("ApplicationAction")) {
						clusterAction = attr.getValue();
					}		
				}
			}
		}

		if (clusterDetails == null) {
			// if the cluster record is not even in our database, then how did we get here?  it's possible the next loop will not even attempt to handle
			// this record
			logger.warn(String.format("Cluster %s not found!", cluster.getId()));
			goToNextStep(cluster);
			//new NagiosEventLogger().emitOK("Cluster deleted (name: " + cluster.getName() + ")");
		} else if (clusterDetails.getClusterId() == null) {
			//pcmaeClusterId is null, so there is no cluster in pcmae, can proceed to the next step
			logger.info(String.format("Cluster %s has not been provisioned by provisioning engine. Continue to next step.", cluster.getId()));
			goToNextStep(cluster);
		} else if (clusterState == null) {
			logger.warn(String.format("Cluster %s (ID %s) has unknown state in PCM-AE (PCM-AE cluster ID %s)...", cluster.getName(), cluster.getId(), pcmaeClusterId));
		} else if (clusterState.equals("NOT_FOUND")) {
			logger.info(String.format("Cluster %s (ID %s) has been removed from PCM-AE (PCM-AE cluster ID %s).", cluster.getName(), cluster.getId(), pcmaeClusterId));		
			cluster.setClusterId(null);
			goToNextStep(cluster);
		} else if ("EXPIRED".equals(clusterState)) {
			logger.info(String.format("Cluster %s (ID %s) has expired. Deleting it...", cluster.getName(), cluster.getId()));
			
			engine.removeCluster(cluster.getId());
			
			// stay on this step until cluster is gone from PCM-AE
			
			new NagiosEventLogger().emitOK("NAGIOSMONITOR CLUSTERPROVISIONING :Removing cluster (name: " + cluster.getName() + ", id: " + pcmaeClusterId + ")");
		} else if ("CANCELED".equals(clusterState)) {
			logger.info(String.format("Cluster %s (ID %s) state is canceled in PCM-AE. Deleting it...", cluster.getName(), cluster.getId()));
			
			engine.removeCluster(cluster.getId());
			// stay here until cluster is gone from PCM-AE
			
			new NagiosEventLogger().emitOK("NAGIOSMONITOR CLUSTERPROVISIONING :Removing cluster (name: " + cluster.getName() + ", id: " + pcmaeClusterId + ")");
		}else if ("ACTIVE".equals(clusterState) && "Cancel".equals(clusterAction)) {
			// Don't try to cancel a cluster twice
			logger.info(String.format("Cluster %s (ID %s) is being canceled. Will check again next time.", cluster.getName(), cluster.getId()));
		}
		else {
			logger.info(String.format("Cluster %s (ID %s) state is \"%s\" (%s).  Canceling it ...", cluster.getName(), cluster.getId(), clusterState, clusterAction));
			engine.cancelCluster(cluster.getId());
			// stay on this step until cluster is CANCELED in PCM-AE
			
			new NagiosEventLogger().emitOK("NAGIOSMONITOR CLUSTERPROVISIONING :Canceling cluster (name: " + cluster.getName() + ", id: " + pcmaeClusterId + ")");
		}
	}
	
	void cancelClusterOrder(Cluster cluster) throws CPEException{
		//dedicated gateway cluster condition
		
		final String clusterId = cluster.getId();
		
		//cancel all hardware associated with orders from the SOFTLAYER_ORDERS table.
		orderProvider.cancelOrdersForCluster(clusterId);
		 
		//Proceed to next step which should be REMOVE_RECORDS
		goToNextStep(cluster);
	}
	
	/* NOTE: @Transactional only works on public, protected, and package-private methods.  */
	@Transactional(rollbackOn = { CPEException.class, RuntimeException.class } )
	void performClusterStep(Cluster cluster) throws CPEException {
		final ClusterStep currentStep = ClusterStep.valueOf(cluster.getCurrentStep());
		final String type = cluster.getClusterParams().get(Cluster.CLUSTER_PROP_CLUSTERTYPE);
		
		logger.info(String.format("Garbage collecting \"%s\" (%s): %s cluster type: %s", cluster.getName(), cluster.getId(), cluster.getCurrentStep(), type));
		switch (currentStep) {
		case CANCEL_CLUSTER:
			if(type == null){
				cancelCluster(cluster.getOwner().getAccount().getOffering(), cluster);
			} else{
				//Account has purchased dedicated gateway
				cancelClusterOrder(cluster);
			}
			break;
		case UNTRUNK_HYPERVISORS:
			untrunkHypervisors(cluster);
			break;
		case UNCONFIGURE_CUST_GATEWAY:
			unconfigureCustGateway(cluster);
			break;
		case UNTRUNK_CUST_GATEWAY:
			untrunkCustomerGateway(cluster);
			break;
		case UNCONFIGURE_MGMT_GATEWAY:
			unconfigureMgmtGateway(cluster);
			break;
		case UNRESERVE_NETWORK:
			unreserveNetwork(cluster);
			break;
		case REMOVE_RECORDS:
			removeCluster(cluster);
			break;
		default:
			logger.warn(String.format("Cluster %s is in state: %s, nothing to do", cluster.getId(), currentStep.name()));
			break;
		}
	}

	/**
	 * advance the cluster to the next step
	 * @param cluster
	 * @throws CPEException
	 */
	private void goToNextStep(Cluster cluster) throws CPEException {
		final ClusterStep currentStep = ClusterStep.valueOf(cluster.getCurrentStep());
		final String type = cluster.getClusterParams().get(Cluster.CLUSTER_PROP_CLUSTERTYPE);
		
		switch(currentStep)
		{
			case CANCEL_CLUSTER:
				if(type == null)
					cluster.setCurrentStep(ClusterStep.UNTRUNK_HYPERVISORS.name());
				else
					cluster.setCurrentStep(ClusterStep.REMOVE_RECORDS.name());
				break;
			case UNTRUNK_HYPERVISORS:
				cluster.setCurrentStep(ClusterStep.UNCONFIGURE_CUST_GATEWAY.name());
				break;
			case UNCONFIGURE_CUST_GATEWAY:
				cluster.setCurrentStep(ClusterStep.UNTRUNK_CUST_GATEWAY.name());
				break;
			case UNTRUNK_CUST_GATEWAY:
				cluster.setCurrentStep(ClusterStep.UNCONFIGURE_MGMT_GATEWAY.name());
				break;
			case UNCONFIGURE_MGMT_GATEWAY:
				cluster.setCurrentStep(ClusterStep.UNRESERVE_NETWORK.name());
				break;
			case UNRESERVE_NETWORK:
				cluster.setCurrentStep(ClusterStep.REMOVE_RECORDS.name());
				break;
			case REMOVE_RECORDS:
				cluster.setCurrentStep(ClusterStep.NONE.name());
				break;
			default:
				break;
		}
		
		logger.info(String.format("Cluster %s state change: %s -> %s", cluster.getId(), currentStep.name(), cluster.getCurrentStep()));
		
		if (!cluster.getCurrentStep().equals(ClusterStep.NONE.name())) {
			// update if we didn't delete the record in our step
			persistence.updateObject(Cluster.class, cluster);
		}
	}
	private void removeCluster(Cluster cluster) throws CPEException{
		//remove all rows in SOFTLAYER_ORDERS table that correspond with the cluster.
		if(Cluster.CLUSTERTYPE_DEDICATED_GATEWAY.equals(cluster.getClusterParams().get(Cluster.CLUSTER_PROP_CLUSTERTYPE))) {
			logger.info(String.format("Deleting softlayerorder records for the cluster: %s", cluster.getId()));
			List<SoftLayerOrder> slorders = persistence.getObjectsBy(SoftLayerOrder.class, new WhereClause("cluster.id",cluster.getId()));
			for (SoftLayerOrder slorder : slorders)
				persistence.deleteObject(SoftLayerOrder.class, slorder);
			
			//unassign gateway 
			final String gatewayId = cluster.getClusterParams().get(Cluster.CLUSTER_PROP_GATEWAYID);
			// gateway id can be null if the gateway was not created it.
			if (gatewayId != null) {
				logger.info(String.format("Deleting gateway record %s for the cluster %s", gatewayId, cluster.getId()));
				networkService.deleteGateway(gatewayId);
			}
		}
		
		// remove the cluster record
		logger.info(String.format("Deleting cluster record: %s", cluster.getId()));
		persistence.deleteObject(Cluster.class, cluster);
		goToNextStep(cluster);
	}

	private void unreserveNetwork(Cluster cluster) throws CPEException  {
		// get the list of cluster vlans before we unreserve the IPs
		final List<Vlan> clusterVlans = networkService.getClusterVlans(cluster.getId());
		
		networkService.unreserveIPAddressByCluster(cluster.getId());
		
		// unreserve VLAN
		for (final Vlan vlan : clusterVlans) {
			if (vlan.getNetworkSpace() == VlanNetworkSpace.PUBLIC) {
				logger.debug(String.format("Skipping unassign of public VLAN %s.%s (%s) ...", 
					vlan.getPrimaryRouter(), vlan.getVlanNumber(), vlan.getId()));							
				continue;
			}
			
			networkService.unassignVlan(vlan.getId(), true);
		}
		
		// TODO: unreserve gateway???
		
		goToNextStep(cluster);
		
	}

	private void unconfigureMgmtGateway(Cluster cluster) throws CPEException  {
		final List<Subnet> clusterSubnetList = networkService.getClusterSubnets(cluster.getId());
		final Set<String> subnetCidrs = new HashSet<String>(clusterSubnetList.size());
		for (final Subnet subnet : clusterSubnetList) {
			subnetCidrs.add(String.format("%s/%d", subnet.getNetworkAddr(), subnet.getCidr()));
		}
		
    	final List<Gateway> mgmtGWs = networkService.getGateways(GatewayType.MANAGEMENT);
    	final Gateway mgmtGW = mgmtGWs.get(0);
		try {
			firewallService.removeCustomerSubnets(mgmtGW, subnetCidrs);
		} catch (CPEException e) {
			new NagiosEventLogger().emitCritical("NAGIOSMONITOR VYATTACONFIG :"+e.getLocalizedMessage());
			throw e;
		}
		
		goToNextStep(cluster);
		
	}

	private void untrunkCustomerGateway(Cluster cluster) throws CPEException  {
		final List<Vlan> clusterVlans = networkService.getClusterVlans(cluster.getId());
		for (final Vlan vlan : clusterVlans) {
			if (vlan.getGateway() == null) {
				logger.warn(String.format("Cluster \"%s\" (%s) VLAN %s.%s (%s) is not associated to a gateway.", 
						cluster.getName(), cluster.getId(), vlan.getPrimaryRouter(), vlan.getVlanNumber(), vlan.getId()));
				continue;
			}
			
			// if the VLAN is public, leave it attached on the shared gateway -- there may be other clusters using it
			final Gateway gw = networkService.getGatewayById(vlan.getGateway().getId());
			if (vlan.getNetworkSpace() == VlanNetworkSpace.PUBLIC && gw.getType() == GatewayType.SHARED) {
				logger.debug(String.format("Skipping detach of public VLAN %s.%s (%s) from SHARED gateway %s (%s)...", 
						vlan.getPrimaryRouter(), vlan.getVlanNumber(), vlan.getId(), gw.getName(), gw.getId()));
				continue;
			}
			
			try {
				networkService.detachVlanFromGateway(vlan.getId(), true);
			} catch (CPEException e) {
				new NagiosEventLogger().emitCritical("NAGIOSMONITOR VLANASSOCIATIONGW :"+e.getLocalizedMessage());
				throw e;
			}
		}
		
		goToNextStep(cluster);
	}

	private void unconfigureCustGateway(Cluster cluster) throws CPEException  {
		final List<Subnet> clusterSubnetList = networkService.getClusterSubnets(cluster.getId());
	
		// get the subnet's parent VLAN and gateway
		final Set<String> subnetCidrs = new HashSet<String>(clusterSubnetList.size());
		final Iterator<Subnet> subnetsIter = clusterSubnetList.iterator();
		final Set<String> vlanIds = new HashSet<String>();
		while (subnetsIter.hasNext()) {
			final Subnet subnet = subnetsIter.next();
			final Vlan vlan = networkService.getVlanById(subnet.getVlan().getId());
			if (vlan.getNetworkSpace() == VlanNetworkSpace.PUBLIC) {
				// for now, only remove private subnet VIF from gateway
				subnetsIter.remove();
			}
			
			vlanIds.add(subnet.getVlan().getId());
			subnetCidrs.add(String.format("%s/%d", subnet.getNetworkAddr(), subnet.getCidr()));
		}
		
		if (clusterSubnetList.isEmpty()) {
			// no subnets?
			goToNextStep(cluster);
			return;
		}
	
		final Collection<Vlan> vlans = networkService.getVlans(vlanIds);
		final Map<String, Vlan> vlanIDMap = new HashMap<String, Vlan>();
		for (final Vlan vlan : vlans) {
			vlanIDMap.put(vlan.getId(), vlan);
		}
		
		for (final Subnet clusterSubnet : clusterSubnetList) {
			final String vlanId = clusterSubnet.getVlan().getId();
			final Vlan vlan = vlanIDMap.get(vlanId);
			
			if (vlan.getGateway() == null) {
				logger.warn(String.format("The VLAN %s.%s (%s) is no longer trunked to a Gateway.", vlan.getPrimaryRouter(), vlan.getVlanNumber(), vlan.getId()));
				continue;
			}
			
			final String gatewayId = vlan.getGateway().getId();
			
			// if the VLAN is public, leave it attached on the shared gateway -- there may be other clusters using it
			final Gateway gateway = networkService.getGatewayById(gatewayId);
			if (gateway.getType() == GatewayType.SHARED &&
				vlan.getNetworkSpace() == VlanNetworkSpace.PUBLIC) {
				logger.debug(String.format("Skipping removal of public VIF %s.%s (%s) from SHARED gateway %s (%s)...", 
						vlan.getPrimaryRouter(), vlan.getVlanNumber(), gateway.getName(), gateway.getId()));			
				continue;
			}

			try {
				// unconfigure the gateway address in the gateway
				firewallService.removeVif(gateway, vlan);
			} catch (CPEException e) {
				// Vyatta configuration error
				new NagiosEventLogger().emitCritical("NAGIOSMONITOR VYATTACONFIG :"+e.getLocalizedMessage());
				throw new CPEException(String.format("Failed to remove cluster VLAN %s from gateway %s (id %s) for cluster %s: %s", vlan.getVlanNumber(), gateway.getName(), gatewayId, cluster.getId(), e.getLocalizedMessage()), e);
			}
		}
	
		goToNextStep(cluster);
	}

	private void untrunkHypervisors(Cluster cluster) throws CPEException {
		final List<Vlan> clusterVlans = networkService.getClusterVlans(cluster.getId());
		
		for (final Vlan vlan : clusterVlans) {
			try {
				// TODO: if vlan is PUBLIC, check if there's other clusters using it on each host.  for now, don't
				// untrunk from hypervisor
				if (vlan.getNetworkSpace() == VlanNetworkSpace.PUBLIC) {
					logger.debug(String.format("Skipping untrunk of public VLAN %s.%s (%s) ...", 
						vlan.getPrimaryRouter(), vlan.getVlanNumber(), vlan.getId()));							
					continue;
				}
				networkService.removeNetworkVlanTrunks(vlan.getId(), true);
			} catch (CPEException e) {
				new NagiosEventLogger().emitCritical("NAGIOSMONITOR VLANTRUNKHYP :"+e.getLocalizedMessage());
				throw e;
			}
		}
		
		goToNextStep(cluster);
	}
	
}
