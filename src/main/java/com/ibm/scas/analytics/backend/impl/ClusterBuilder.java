package com.ibm.scas.analytics.backend.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import com.ibm.pcmae.cluster.beans.Message;
import com.ibm.scas.analytics.EngineProperties;
import com.ibm.scas.analytics.backend.FirewallService;
import com.ibm.scas.analytics.backend.NetworkService;
import com.ibm.scas.analytics.backend.PcmaeGateway;
import com.ibm.scas.analytics.backend.ProvisioningService;
import com.ibm.scas.analytics.backend.SoftLayerOrderProvider;
import com.ibm.scas.analytics.backend.TenantService;
import com.ibm.scas.analytics.backend.monitoring.NagiosEventLogger;
import com.ibm.scas.analytics.beans.Account;
import com.ibm.scas.analytics.beans.BillingItem;
import com.ibm.scas.analytics.beans.Cluster.ClusterStep;
import com.ibm.scas.analytics.beans.ClusterMachine;
import com.ibm.scas.analytics.beans.ClusterMachineGroup;
import com.ibm.scas.analytics.beans.ClusterTier;
import com.ibm.scas.analytics.beans.Gateway;
import com.ibm.scas.analytics.beans.Gateway.GatewayStatus;
import com.ibm.scas.analytics.beans.Gateway.GatewayType;
import com.ibm.scas.analytics.beans.IPAddress;
import com.ibm.scas.analytics.beans.SoftLayerIdObject;
import com.ibm.scas.analytics.beans.SoftLayerOrder;
import com.ibm.scas.analytics.beans.SoftLayerOrder.SoftLayerHardware;
import com.ibm.scas.analytics.beans.Subnet;
import com.ibm.scas.analytics.beans.Usage;
import com.ibm.scas.analytics.beans.Vlan;
import com.ibm.scas.analytics.beans.Vlan.VlanNetworkSpace;
import com.ibm.scas.analytics.persistence.PersistenceException;
import com.ibm.scas.analytics.persistence.PersistenceService;
import com.ibm.scas.analytics.persistence.beans.Cluster;
import com.ibm.scas.analytics.persistence.beans.Subscriber;
import com.ibm.scas.analytics.persistence.beans.VPNTunnel;
import com.ibm.scas.analytics.persistence.util.WhereClause;
import com.ibm.scas.analytics.persistence.util.WhereInClause;
import com.ibm.scas.analytics.provider.ServiceProvider;
import com.ibm.scas.analytics.provider.impl.ServiceProviderPluginFactory;
import com.ibm.scas.analytics.utils.CPEException;
import com.ibm.scas.analytics.utils.CollectionsUtil;
import com.ibm.scas.analytics.utils.OauthAdapter;
import com.ibm.scas.analytics.utils.ReadWriteLockTable;

@Singleton
public class ClusterBuilder implements Runnable {
	private static final Logger logger = Logger.getLogger(ClusterBuilder.class);
	private static final ConcurrentLinkedQueue<String> clusterIdsToSelect = new ConcurrentLinkedQueue<String>();
	
	private final static String cpeLocationName = EngineProperties.getInstance().getProperty(EngineProperties.CPE_LOCATION_NAME, EngineProperties.DEFAULT_CPE_LOCATION_NAME);
	private final static String USAGE_API_PATH = "/api/integration/v1/billing/usage";
	
	private final static String myLocationName = EngineProperties.getInstance().getProperty(EngineProperties.CPE_LOCATION_NAME, EngineProperties.DEFAULT_CPE_LOCATION_NAME);
	
	@Inject private PersistenceService persistence;
	@Inject private ProvisioningService provisionService;
	@Inject private NetworkService networkService;
	@Inject private FirewallService firewallService;
	@Inject private SoftLayerOrderProvider orderProvider;
	@Inject private ServiceProviderPluginFactory pluginFactory;
	@Inject private PcmaeGateway pcmaeGateway;
	@Inject private TenantService tenantService;
	@Inject private ReadWriteLockTable<String> locktable;

	
	private boolean isRunning;
	
	public ClusterBuilder() {
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
		logger.info("afterInject(): Setting up cluster builder...");
		
		// get all construct functions on clusters not marked for deletion
		final List<WhereClause> wheres = new ArrayList<WhereClause>();
		wheres.add(new WhereInClause("currentStep", 
				Arrays.asList(
						ClusterStep.INIT.name(),
						ClusterStep.ORDER_PENDING.name(),
						ClusterStep.WAITING_FOR_GATEWAY.name(),
						ClusterStep.RESERVE_NETWORK.name(),
						ClusterStep.CONFIGURE_MGMT_GATEWAY.name(),
						ClusterStep.TRUNK_CUST_GATEWAY.name(),
						ClusterStep.CONFIGURE_CUST_GATEWAY.name(),
						ClusterStep.PROVISION_NODES.name(),
						ClusterStep.TRUNK_HYPERVISORS.name(),
						ClusterStep.CONFIG_NODES.name(), 
						ClusterStep.SUBMIT_BILLING.name()
						)));
		wheres.add(new WhereClause("owner.type", Subscriber.GARBAGE_COLLECTOR, false));

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
			if (logger.isTraceEnabled()) {
				logger.trace("Waking up ...");
			}

			if (clusterIdsToSelect.isEmpty()) {
				return;
			}
			
			logger.debug(String.format("Performing provisioning steps for clusters: %s", clusterIdsToSelect));
			
			for (Iterator<String> it = clusterIdsToSelect.iterator(); it.hasNext(); ) {
				final String clusterId = it.next();
				final Lock lock = this.locktable.getLockWithAdd(clusterId).writeLock();
				final Cluster clusterRec;
				
				try {
					logger.debug("Locking on cluster " + clusterId + " ...");
					lock.lock();
					logger.debug("Lock acquired on cluster " + clusterId);

					clusterRec = persistence.getObjectById(Cluster.class, clusterId);
					
					try {
						if (clusterRec == null || clusterRec.getOwner().getType() == Subscriber.GARBAGE_COLLECTOR) {
							logger.debug(String.format("The cluster %S has been recently deleted. Remove it from my list.", clusterId));
							it.remove();
							continue;
						}

						logger.debug(String.format("Performing step %s for cluster %s (%s)...", clusterRec.getCurrentStep(), clusterRec.getName(), clusterRec.getId()));
						this.performClusterStep(clusterRec);
						logger.debug("Done performClusterStep().");

						if (clusterRec.getCurrentStep().equals(ClusterStep.NONE.name())) {
							logger.debug(String.format("The cluster %s is done building. Remove it from my list.", clusterId));
							it.remove();
						}
					} catch (CPEException e) {
						logger.error(String.format("Error performing cluster step %s for cluster %s, try again later: %s", clusterRec.getCurrentStep(), clusterRec.getId(), e.getLocalizedMessage()), e);
						new NagiosEventLogger().emitCritical(String.format("NAGIOSMONITOR CLUSTERPROVISIONING :Error performing cluster step %s for cluster %s, try again later: %s", clusterRec.getCurrentStep(), clusterRec.getId(), e.getLocalizedMessage()));
						continue;
					}
				} catch (PersistenceException e) {
					logger.error(String.format("Unable to retrieve cluster for id %s from database due to: %s", clusterId, e.getLocalizedMessage()), e);
				} finally {
					lock.unlock();
					logger.debug("Lock released for cluster " + clusterId + " ...");
				}
			}		
			// cluster builder goes to sleep for one second
		} catch (RuntimeException e) {
			// prevent thread death
			logger.error(String.format("Abnormal thread exit: %s", e.getLocalizedMessage()), e);
		} finally {
			if (logger.isTraceEnabled()) {
				logger.trace("Clear DB session cache.");
			}
			
			// drop the session cache to force refresh next loop
			persistence.clear();
			
			if (logger.isTraceEnabled()) {
				logger.trace("Back to sleep ...");
			}			
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
		
		switch (currentStep) {
		case INIT:
			if (type == null) {
				final Account account = this.tenantService.getAccountById(cluster.getOwner().getAccount().getId());
				final String dgwPropVal = account.getProperties().get(com.ibm.scas.analytics.persistence.beans.Account.ACCOUNT_PROPS_DGW);
				if (dgwPropVal != null && Boolean.valueOf(dgwPropVal)) {
					// Account has purchased a dedicated gateway so wait for gateway to be ready.
					cluster.setCurrentStep(ClusterStep.WAITING_FOR_GATEWAY.name());
				} else {
					// Account has not purchased a dedicated gateway so reserve network on shared gateway.
					cluster.setCurrentStep(ClusterStep.RESERVE_NETWORK.name());
				}
			// all other types of clusters will goto ORDER_PENDING;
			} else {
				// Cluster has a pending order so goto ORDER_PENDING
				cluster.setCurrentStep(ClusterStep.ORDER_PENDING.name());
			}
			break;
		case WAITING_FOR_GATEWAY:
			cluster.setCurrentStep(ClusterStep.RESERVE_NETWORK.name());
			break;
		case ORDER_PENDING:
			cluster.setCurrentStep(ClusterStep.PROVISION_NODES.name());
			break;
		case RESERVE_NETWORK:
			// allow traffic from management Vyatta
			cluster.setCurrentStep(ClusterStep.CONFIGURE_MGMT_GATEWAY.name());
			break;
		case CONFIGURE_MGMT_GATEWAY:
			// add customer VLAN to gateway
			cluster.setCurrentStep(ClusterStep.TRUNK_CUST_GATEWAY.name());
			break;
		case TRUNK_CUST_GATEWAY:
			// configure customer gateway
			cluster.setCurrentStep(ClusterStep.CONFIGURE_CUST_GATEWAY.name());
			break;
		case CONFIGURE_CUST_GATEWAY:
			// provision nodes
			cluster.setCurrentStep(ClusterStep.PROVISION_NODES.name());
			break;
		case PROVISION_NODES:
			if (type == null) {
				// this is a regular cluster so trunk the VLANs on the hypervisors
				cluster.setCurrentStep(ClusterStep.TRUNK_HYPERVISORS.name());
			} else {
				// all other cluster types will go configure the hardware
				cluster.setCurrentStep(ClusterStep.CONFIG_NODES.name());
			}
			break;
		case CONFIG_NODES:
			// for clusters with orders, this is the last step, no further things to do
			cluster.setCurrentStep(ClusterStep.NONE.name());
			break;
		case TRUNK_HYPERVISORS:
			cluster.setCurrentStep(ClusterStep.SUBMIT_BILLING.name());
			break;
		case SUBMIT_BILLING:
			// last step, no further things to do
			cluster.setCurrentStep(ClusterStep.NONE.name());
			break;
		default:
			break;
		}
		
		logger.info(String.format("Cluster %s state change: %s -> %s", cluster.getId(), currentStep.name(), cluster.getCurrentStep()));
		
		// update
		persistence.updateObject(Cluster.class, cluster);
	}
	
	/**
	 * reserve a gateway, vlan, and IP address in the CPE database.
	 * @param cluster
	 * @throws CPEException
	 */
	private void reserveNetwork(Cluster cluster) throws CPEException {
		final Set<String> vlansInUse = new HashSet<String>();
		
		// get the dedicated gateway assigned to my subscriber
		Collection<Gateway> gatewaysToUse;	
		
		final Account account = this.tenantService.getAccountById(cluster.getOwner().getAccount().getId());
		final String dgwPropVal = account.getProperties().get(com.ibm.scas.analytics.persistence.beans.Account.ACCOUNT_PROPS_DGW);
		
		final boolean useDedicatedGateway = (dgwPropVal != null && Boolean.valueOf(dgwPropVal));

		if (!useDedicatedGateway) {
			// use a shared gateway, get a list of shared gateways to reserve the vlan on
			final Collection<Gateway> sharedGWs = networkService.getGateways(GatewayType.SHARED);
			if (sharedGWs.isEmpty()) {
				throw new CPEException(String.format("Could not find a shared gateway to use for cluster %s!", cluster.getId()));
			}
			gatewaysToUse = sharedGWs;
		} else {
			// Dedicated Gateway
			// if the account already has a gateway, just re-use that.  put all of their clusters behind
			// the same gateway.
			final Collection<Gateway> dedicatedGWs = networkService.getAccountGateways(cluster.getOwner().getAccount().getId());
			if (!dedicatedGWs.isEmpty()) {
				gatewaysToUse = dedicatedGWs;
			} else {
				// TODO: Put into WAITING_FOR_GATEWAY
				gatewaysToUse = Collections.emptyList();
			}
		}
		
		// cluster may already have some vlans; find all VLANs this cluster is on already
		Vlan vlan = null;
		Gateway gateway = null;	
		final List<Vlan> vlans = networkService.getClusterVlans(cluster.getId());
		if (vlans.isEmpty()) {
			// loop through available gateways looking for a VLAN

			for (final Gateway gatewayToTry : gatewaysToUse) {
				// reserve a new vlan
				if (logger.isDebugEnabled()) {
					logger.debug(String.format("Reserving a VLAN for Cluster ID %s, Subscriber ID %s ...", cluster.getId(),  cluster.getOwner().getId()));
				}

				String vlanId = networkService.reserveVlan(cluster.getId(), gatewayToTry.getId());
				if (vlanId == null) {
					logger.warn(String.format("Unable to reserve VLAN for cluster %s behind gateway %s, trying another gateway ...", cluster.getId(), gatewayToTry.getId()));
					continue;
				}

				gateway = gatewayToTry;
				vlansInUse.add(vlanId);
				vlan = networkService.getVlanById(vlanId);

				break;
			}
			
			if (vlan == null) {
				final Set<String> gatewayNames = new HashSet<String>(gatewaysToUse.size());
				for (final Gateway gatewayToTry : gatewaysToUse) {
					gatewayNames.add(gatewayToTry.getName());
				}
				throw new CPEException(String.format("Unable to reserve a VLAN for cluster %s.  Add a free VLAN next to one of the following gateways: %s.", cluster.getId(), gatewayNames));
			}
		} else {
			// figure out which VLAN is the private VLAN, and which of my gateways is the gateway that the cluster is on
			vlanLoop: for (final Vlan existingVlan : vlans) {
				if (existingVlan.getNetworkSpace() == VlanNetworkSpace.PUBLIC) {
					continue;
				}
				
				Vlan vlanToUse = existingVlan;
		
				for (final Gateway gatewayToTry : gatewaysToUse) {
					if (!vlanToUse.getPrimaryRouter().equals(gatewayToTry.getPrimaryBackendRouter())) {
						// find the gateway behind the same vlan
						continue;
					}
					
					// matched a vlan with a gateway, use these
					
					gateway = gatewayToTry;
					vlan = vlanToUse;
					break vlanLoop;
				}
			}
		
			if (vlan == null) {
				final Set<String> gatewayNames = new HashSet<String>(gatewaysToUse.size());
				for (final Gateway gatewayToTry : gatewaysToUse) {
					gatewayNames.add(gatewayToTry.getName());
				}
				throw new CPEException(String.format("Unable to use existing VLAN(s) %s for cluster %s.  Add a free VLAN next to one of the following gateways: %s.", vlans, cluster.getId(), gatewayNames));	
			}
		
			vlansInUse.add(vlan.getId());
			logger.info(String.format("Cluster \"%s\" (%s) already has VLAN %s.%s (%s) reserved; using gateway %s. skipping VLAN reservation ...", cluster.getName(), cluster.getId(), vlan.getPrimaryRouter(), vlan.getVlanNumber(), vlan.getId(), gateway.getName()));
		}
		
		// grab the cluster from the provision service.  in PCM-AE case, the public tiers will be added to the cluster properties
		final com.ibm.scas.analytics.beans.Cluster details = provisionService.getClusterDetails(cluster.getId());
		
		// plugin adds the tiers to provision a public IP for as a properties
		final Set<String> publicIPTiers = new HashSet<String>();
		
		// TODO: for now, no public tiers if DEDICATED_GATEWAY is true.  Note that some clusters may want edge nodes with public IP addresses,
		// but we're going to ignore them for now
		Vlan pubVlan = null;
		if (!useDedicatedGateway && details.getProperties() != null) {
			if (details.getProperties().containsKey("PUBLIC_IP_TIERS")) {
				final String publicIpTiers = details.getProperties().get("PUBLIC_IP_TIERS");
				publicIPTiers.addAll(CollectionsUtil.fromStringList(publicIpTiers, ";"));
				
				// get the public vlan from the gateway.
				String pubVlanId = networkService.fetchPublicVlan(gateway.getId());
				pubVlan = networkService.getVlanById(pubVlanId);
			}
		}
		
		// reserve IP for each tier
		for (final ClusterTier tier : details.getClusterTiers()) {
			final ClusterMachineGroup machineGroup = tier.getMachineGroup();
			
			// check how many existing IPs -- in case we already have IPs reserved
			final List<IPAddress> addrs = networkService.getIPAddressByCluster(cluster.getId(), tier.getName());
			final Set<String> existingPrivAddrs = new HashSet<String>();
			final Set<String> existingPubAddrs = new HashSet<String>();
			
			// check what subnet/vlan these are on: only consider private subnet first
			for (final IPAddress addr : addrs) {
				final Subnet subnet = (Subnet)addr.getSubnet();
				final Vlan onVlan = (Vlan)subnet.getVlan();
				
				if (onVlan.getNetworkSpace() == VlanNetworkSpace.PRIVATE) {
					existingPrivAddrs.add(String.format(addr.getIpAddress()));
				}
				if (onVlan.getNetworkSpace() == VlanNetworkSpace.PUBLIC) {
					existingPubAddrs.add(String.format(addr.getIpAddress()));
				}			
			}
			
			final int toReserve = machineGroup.getNumberOfMachines() - existingPrivAddrs.size();
			if (toReserve <= 0) {
				logger.warn(String.format("Cluster \"%s\" (%s) already has %d IP(s) reserved for it for tier %s on VLAN %s.%s (%s): %s, skipping IP reservation ...",
						cluster.getName(), cluster.getId(), (existingPrivAddrs.size()), tier.getName(), vlan.getPrimaryRouter(), vlan.getVlanNumber(),
						vlan.getId(), existingPrivAddrs));
			} else {
				logger.debug(String.format("Reserving %d IPs from VLAN %s.%s (%s) for Cluster \"%s\" (%s), Tier %s ...", 
						toReserve, vlan.getPrimaryRouter(), vlan.getVlanNumber(), vlan.getId(), cluster.getName(), cluster.getId(), tier.getName()));
				networkService.reserveIPAddressesForClusterTier(vlan.getId(), toReserve, cluster.getId(), tier.getName());
			}
			
			/* TODO: we expect the plugin to mark whether a machine group is on the public internet.  It shoudl be able to figure this out from
			 * the ClusterReq.  If the ClusterReq says we need a dedicated gateway, then plugin should mark "isPublic" to false on all
			 * tiers (unless it really needs public internet but that's a roadmap item) */		

			if (publicIPTiers.contains(tier.getName())) {
				// these machines are on the public internet; reserve the public IP
				//fetch a public vlan to be used 
				if (pubVlan == null){
					throw new CPEException(String.format("No available public VLANs for reservation behind Gateway %s (id %s)!", gateway.getName(), gateway.getId()));
				}
				
				final int pubToReserve = machineGroup.getNumberOfMachines() - existingPubAddrs.size();
				if (pubToReserve <= 0) {
					logger.warn(String.format("Cluster \"%s\" (%s) already has PUBLIC %d IP(s) reserved for it for tier %s on VLAN %s.%s (%s): %s, skipping IP reservation ...",
						cluster.getName(), cluster.getId(), (existingPubAddrs.size()), tier.getName(), vlan.getPrimaryRouter(), vlan.getVlanNumber(),
						vlan.getId(), existingPubAddrs));
				} else {
					logger.debug(String.format("Reserving %d IPs from VLAN %s.%s (%s) for Cluster \"%s\" (%s), Tier %s ...", 
							toReserve, pubVlan.getPrimaryRouter(), pubVlan.getVlanNumber(), pubVlan.getId(), cluster.getName(), cluster.getId(), tier.getName()));
					networkService.reserveIPAddressesForClusterTier(pubVlan.getId(), pubToReserve, cluster.getId(), tier.getName());
				}			
				
				vlansInUse.add(pubVlan.getId());
			}
		}
		
		// the cluster's VPNTunnel configuration will be synced to the reserved gateway
		if (gateway.getType() == GatewayType.DEDICATED) {
			// associate the reserved subnets to the VPNTunnel now
			final List<Subnet> clusterSubnets = networkService.getClusterSubnets(cluster.getId());
			
			// get the vlans used by the cluster
			final Collection<Vlan> clusterVlans = networkService.getVlans(vlansInUse);
			final Map<String, Vlan> clusterVlanMap = new HashMap<String, Vlan>();
			for (final Vlan clusterVlan : clusterVlans) {
				clusterVlanMap.put(clusterVlan.getId(), clusterVlan);
			}
			
			// get list of cluster subnets to create the tunnel for
			final Set<String> subnetIds = new HashSet<String>(clusterSubnets.size());
			for (final Subnet subnet : clusterSubnets) {
				final Vlan clusterVlan = clusterVlanMap.get(subnet.getVlan().getId());
				if (clusterVlan == null || clusterVlan.getNetworkSpace() == VlanNetworkSpace.PUBLIC) {
					// only private subnets are added to the tunnel
					continue;
				}
				
				subnetIds.add(subnet.getId());
			}
		}
		
		this.goToNextStep(cluster);
	}

	private void provisionCluster(Cluster cluster) throws CPEException {
		final String clusterType = cluster.getClusterParams().get(Cluster.CLUSTER_PROP_CLUSTERTYPE);
		
		if (clusterType == null) {
			provisionNodes(cluster);
		} else if (Cluster.CLUSTERTYPE_DEDICATED_GATEWAY.equals(clusterType)) {
			provisionGateway(cluster);
		} else {
			throw new CPEException(String.format("%s.provisionCluster() - Internal Error. Cluster type %s is not handled", ClusterBuilder.class.getSimpleName(), clusterType));
		}		
	}
	
	/**
	 * create the actual machines for the cluster, in PCM-AE.  The network must already be reserved.
	 * @param cluster
	 * @throws CPEException
	 */
	private void provisionNodes(Cluster cluster) throws CPEException {
		if (cluster.getClusterId() != null) {
			// a cluster already exists in PCM-AE, check flex up
			final com.ibm.scas.analytics.beans.Cluster details = provisionService.getClusterDetails(cluster.getId());
			
			// provision
			final List<ClusterMachine> existingMachines = details.getClusterMachines();
			
			final Account account = this.tenantService.getAccountById(cluster.getOwner().getAccount().getId());
			final ServiceProvider plugin = pluginFactory.getPlugin(cluster.getOwner().getAccount().getOffering().getId());
			
			int currentSize = existingMachines.size();
			
			//fetch new size of the cluster
			int newSize = cluster.getSize();
			
			if (currentSize != newSize) {
				//what if we have a request for streamsdatanode and edge node , would the tierName be same?
				final String tierName = plugin.getFlexTierName();
				final String edition = account.getEdition();
				final int newTierSize = plugin.computeFlexTierSize(edition, currentSize, newSize);

				//TODO : try an option to preserve the new size and current size in the cluster request
				//not good to fetch each time from pcmae

				if (logger.isDebugEnabled()) {
					logger.debug(String.format("Calling PCM-AE for flexing up cluster %s with %d nodes ",cluster.getId(),newTierSize));
				}

				// TODO: get rid of direct call to PCM-AE here, use provisionService.flexUpCluster()
				final Message message = pcmaeGateway.getClusterApi(provisionService.getPcmaeBackendId()).flexUpCluster(cluster.getClusterId(), tierName, newTierSize);
				if (logger.isDebugEnabled()) {	
					logger.debug("result: " + message.getType());
					logger.debug("message: " + message.getMessage());
				}
			} else {
				logger.warn(String.format("Cluster \"%s\" (%s) already has %d nodes (%d requested), skipping provision ...",
						cluster.getName(), cluster.getId(), currentSize, newSize));
			}
			
			this.goToNextStep(cluster);
			return;
		}
		
		// call PCMAE
		final String clusterId = provisionService.provisionCluster(cluster.getId());

		// add the PCM-AE cluster Id to the database
		cluster.setClusterId(clusterId);
		
		new NagiosEventLogger().emitOK("NAGIOSMONITOR CLUSTERPROVISIONING :Cluster " + cluster.getId() + " created (name: " + cluster.getName() + ", id: " + clusterId + ")");

		// next step
		this.goToNextStep(cluster);
	}
	
	/**
	 * open the firewall on the management gateway for cluster traffic 
	 */
	private void configureMgmtGateway(Cluster cluster) throws CPEException {
		// get all the subnets that the cluster is on
    	// before we connect to customer subnet, we need to whitelist the subnet it's on in our management vyatta
    	final List<Gateway> mgmtGWs = networkService.getGateways(GatewayType.MANAGEMENT);
    	
    	if (mgmtGWs.isEmpty()) {
    		throw new CPEException(String.format("Cannot get the local MANAGEMENT gateway for location %s", cpeLocationName));
    	}
    	
    	final Gateway mgmtGW = mgmtGWs.get(0);
    	
		final List<Subnet> clusterSubnets = networkService.getClusterSubnets(cluster.getId());
		final Iterator<Subnet> subnetsIter = clusterSubnets.iterator();
		while (subnetsIter.hasNext()) {
			final Subnet subnet = subnetsIter.next();
			final Vlan vlan = networkService.getVlanById(subnet.getVlan().getId());
			if (vlan.getNetworkSpace() == VlanNetworkSpace.PUBLIC) {
				// only add private subnets to the mgmtGW as it only talks to management infra over private network
				subnetsIter.remove();
			}
		}
		
		if (clusterSubnets.isEmpty()) {
			// next step
			this.goToNextStep(cluster);
			return;
		}
		
		final Set<String> clusterSubnetCidrs = new HashSet<String>(clusterSubnets.size());
		for (final Subnet clusterSubnet : clusterSubnets) {
			clusterSubnetCidrs.add(String.format("%s/%d", clusterSubnet.getNetworkAddr(), clusterSubnet.getCidr()));
		}
		
		try {
			// add the private subnets to vyatta
			firewallService.allowCustomerSubnets(mgmtGW, clusterSubnetCidrs);
		} catch (CPEException e) {
			// Vyatta configuration error
			throw new CPEException(String.format("Failed to add cluster subnets %s to MANAGEMENT gateway %s (id %s) for cluster %s: %s", clusterSubnetCidrs, mgmtGW.getName(), mgmtGW.getId(), cluster.getId(), e.getLocalizedMessage()));
		}

		// next step
		this.goToNextStep(cluster);
	}
	
	/**
	 * associate the customer gateway with the cluster vlan(s)
	 * @param cluster
	 * @throws CPEException
	 */
	private void trunkCustomerGateway(Cluster cluster) throws CPEException {
		// Find out what vlans the cluster is using
		final List<Vlan> clusterVlans = networkService.getClusterVlans(cluster.getId());
		final Set<String> vlanIds = new HashSet<String>();
		Vlan vlanToCheck = null;
		for (final Vlan vlan : clusterVlans) {
			vlanIds.add(vlan.getId());
			if (vlan.getNetworkSpace() != VlanNetworkSpace.PRIVATE){
				continue;
			}
			
			// pick a private VLAN to compare primary routers with a gateway
			vlanToCheck = vlan;
		}
	
		// Find out if the account has dedicated gateway add-on
		
		final Account account = this.tenantService.getAccountById(cluster.getOwner().getAccount().getId());
		final String dgwPropVal = account.getProperties().get(com.ibm.scas.analytics.persistence.beans.Account.ACCOUNT_PROPS_DGW);
		final boolean useDedicatedGateway = (dgwPropVal != null && Boolean.valueOf(dgwPropVal));
		
		Gateway gateway = null;
		Collection<Gateway> gatewaysToCheck = null;
		if (useDedicatedGateway) {
			// TODO: are we assuming the account can only own one gateway? -- this code doesn't but not tested
			gatewaysToCheck = networkService.getAccountGateways(cluster.getOwner().getAccount().getId());
		
			if (gatewaysToCheck.isEmpty()) {
				// cluster requested a dedicated gateway, but we don't have one.  should have reserved one when
				// during cluster creation
				throw new CPEException(String.format("Failed to find dedicated gateway for subscriber %s for cluster %s.", cluster.getOwner().getId(), cluster.getId()));
			}
		} else {
			gatewaysToCheck = networkService.getGateways(GatewayType.SHARED);
		}
			
		// get the gateway that matches the cluster VLAN's backend router
		final Iterator<Gateway> gatewayIter = gatewaysToCheck.iterator();
		while (gatewayIter.hasNext()) {
			final Gateway gatewayToCheck = gatewayIter.next();
			if (!gatewayToCheck.getPrimaryBackendRouter().equals(vlanToCheck.getPrimaryRouter())) {
				// can't trunk this VLAN to this gateway
				gatewayIter.remove();
				continue;
			}
			
			if (gatewayToCheck.getAssociatedVlans() == null) {
				// this gateway has no vlans on it; avoid the next check
				continue;
			}
			
			// prefer the gateway that already has some of my vlans on it
			for (final SoftLayerIdObject vlan : gatewayToCheck.getAssociatedVlans()) {
				if (vlanIds.contains(vlan.getId())) {
					// one of my VLANs is already associated here, use this gateway
					gateway = gatewayToCheck;
					break;
				}
			}
		}
		
		if (gatewaysToCheck.isEmpty()) {
			// this is a weird situation.  there aren't any eligible gateways for my cluster VLANs.  we're going to 
			// throw an exception and someone is going to have to add a gateway to CPE that is eligible.
			throw new CPEException(String.format("No gateways found that are eligible to trunk VLANs %s on.  You may need to add an eligible Gateway to the CPE database to proceed with cluster %s.", vlanIds, cluster.getId()));
		}
		
		if (gateway == null) {
			// if I still haven't picked a gateway yet, choose the first one of the remaining eligible 
			// gateways
			gateway = new ArrayList<Gateway>(gatewaysToCheck).get(0);
		}
		
		// call the trunk API
		// TODO: if the gateway is busy, try again in a minute
		try {
			networkService.attachVlanToGateway(vlanIds, gateway.getId(), false);
		} catch (CPEException e) {
			// not necessarily an error
			final Set<String> vlanNames = new HashSet<String>(vlanIds.size());
			for (final Vlan vlan : clusterVlans) {
				vlanNames.add(String.format("%s.%s", vlan.getPrimaryRouter(), vlan.getVlanNumber()));
			}
			String emessage = String.format("Not all VLANs %s (ids: %s)were trunked on gateway %s (id: %s). CPE will try cluster %s (%s) again in the next interval: %s", vlanNames, vlanIds, gateway.getName(), gateway.getId(), cluster.getName(), cluster.getId(), e.getLocalizedMessage());
			new NagiosEventLogger().emitCritical("NAGIOSMONITOR VLANASSOCIATIONGW :"+emessage);
			logger.warn(emessage);
			return;
		}
		
		this.goToNextStep(cluster);
	}
	
	/**
	 * add the VIFs and gateway address for cluster subnets on the customer gateway
	 * 
	 * also, configure the firewall if necessary and open traffic to management gateway
	 * 
	 * @param cluster
	 * @throws CPEException
	 */
	private void configureCustomerGateway(Cluster cluster) throws CPEException {
		// TODO: should we wait until the gateway isn't busy?  it doesn't really matter
		
		final List<Vlan> clusterVlans = networkService.getClusterVlans(cluster.getId());
		
		// get the gateways that the vlans are on.
		// we can assume it's going to end up on just one gateway, but you never know!
		final Map<String, Vlan> vlanMap = new HashMap<String, Vlan>();
		// find the gateway that's got all my VLANs trunked on it
		String gatewayId = null;
		
		final Set<String> vlanNames = new HashSet<String>(clusterVlans.size());
		final Set<String> vlanIds = new HashSet<String>(clusterVlans.size());
		for (final Vlan vlan : clusterVlans) {
			vlanNames.add(vlan.getPrimaryRouter() + "." + vlan.getVlanNumber());
			vlanIds.add(vlan.getId());
		}
		
		for (final Vlan vlan : clusterVlans) {
			// Note: vlans can only be trunked to one gateway.  here we make sure all Vlans are associated before we proceed
			if (vlan.getGateway() == null) {
				// one vlan doesn't have a parent gateway.  this could mean that trunk hasn't finished, or it got untrunked.  wake up later and try again
				logger.warn(String.format("Vlan %s (%s.%s) is not trunked to a gateway yet.  CPE will attempt to add the VIF to the gateway when all VLANs " +
						"in %s (%s) are associated to one Vyatta gateway SoftLayer.", vlan.getId(), vlan.getPrimaryRouter(), vlan.getVlanNumber(), vlanNames, vlanIds));
				return;
			}
			
			if (gatewayId != null && !vlan.getGateway().getId().equals(gatewayId)) {
				// one vlan is associated to a different gateway than the others.  we cannot continue until this is fixed.
				logger.warn(String.format("Vlan %s (%s.%s) is not trunked to gateway %s.  CPE will attempt to add the VIF to the gateway when all VLANs " +
						"in %s (%s) are associated to one Vyatta gateway SoftLayer.", vlan.getId(), vlan.getPrimaryRouter(), vlan.getVlanNumber(), 
						vlan.getGateway().getId(), vlanNames, vlanIds));
				return;			
			}
			
			gatewayId = vlan.getGateway().getId();
			vlanMap.put(vlan.getId(), vlan);
		}
		
    	// loop through subnets
		final Gateway gwObj = networkService.getGatewayById(gatewayId);
		
		// Test the gateway credentials we have in the DB, if it fails then just move to the next step.
		// This is to cover the case where the customer changes the username and password of their dedicated gateway.
		if (gwObj.getType() == GatewayType.DEDICATED) {
			try {
				firewallService.testGatewayCredentials(gwObj);
    		} catch (CPEException e) {
				String emessage = String.format("Failed to configure customer's dedicated gateway %s (%s) due to: %s. CPE will move to the next step.", gwObj.getName(), gatewayId, e.getLocalizedMessage());
    			new NagiosEventLogger().emitWarning("NAGIOSMONITOR VYATTACONFIG :"+emessage);
    			this.goToNextStep(cluster);
    		}
		}
		
    	for (final Vlan vlan : clusterVlans) {
    		try {
    			firewallService.addVifGateway(gwObj, vlan);
    		} catch (CPEException e) {
				String emessage = String.format("Failed to add VIF %s to gateway %s. CPE will try cluster %s again in the next interval: %s", vlan.getVlanNumber(), gatewayId, cluster.getId(), e.getLocalizedMessage());
    			new NagiosEventLogger().emitCritical("NAGIOSMONITOR VYATTACONFIG :"+emessage);
				throw new CPEException(emessage);
    		}
    	}
    	
    	// configure the tunnels, if they exist
    	final List<VPNTunnel> tunnel = persistence.getObjectsBy(VPNTunnel.class, new WhereClause("gateway.id", gatewayId));
    	if (!tunnel.isEmpty()) {
    		firewallService.configureTunnels(gatewayId);
    	}
		
		// next step
		this.goToNextStep(cluster);
	}

	/**
	 * trunk the hypervisors that the cluster VMs are on with the VLANs they should be on
	 * 
	 * TODO: this method is error prone as we don't know what hypervisors the VMs are going to be going on until they appear "On" in PCM-AE.  
	 * There is a race condition where "Starting" will show the wrong hypervisor for a split second, so we may end up trunking the VLAN on the
	 * wrong hypervisor momentarily.  if the VM moves to the right hypervisor, the wrong hypervisor may be trunked until
	 * the cluster is destroyed.
	 * 
	 * Ideally we should tell PCM-AE where to place the VM, trunk the hypervisors there, and then provision the VMs.  Probably
	 * not possible in PCM-AE.
	 * 
	 * @param cluster
	 * @throws CPEException
	 */
	private void trunkHypervisors(Cluster cluster) throws CPEException {
		// wait until the cluster in PCM-AE has number of machines matching our cluster size
		
		//should we be calling pcmae getClusterDetails method here?
		final com.ibm.scas.analytics.beans.Cluster clusterDetails = provisionService.getClusterDetails(cluster.getId());
	
		// map the hypervisors by tiers
		final Map<String, List<String>> hypervisorTierMap = new HashMap<String, List<String>>();
		for (final ClusterMachine m : clusterDetails.getClusterMachines()) {
			if (!m.getStatus().equals("Starting") && !m.getStatus().equals("On")) {
				// the machine has to be "Starting" or "On" for it to be placed, try again later
				continue;
			}
			CollectionsUtil.addToMap(hypervisorTierMap, m.getTierName(), m.getPhysicalHostIP());
		}
		
		for (final ClusterTier tier : clusterDetails.getClusterTiers()) {
			final List<Vlan> tierVlans = networkService.getClusterVlans(cluster.getId(), tier.getName());
			if (tierVlans.isEmpty()) {
				// no vlans on this tier (?)
				continue;
			}
			
			// get the list of vlan IDs
			final Set<String> vlanIDs = new HashSet<String>();
			for (final Vlan vlan : tierVlans) {
				vlanIDs.add(vlan.getId());
			}
			
			// get the list of hypervisors on this tier
			final List<String> hypervisors = hypervisorTierMap.get(tier.getName());
			if (hypervisors == null || hypervisors.isEmpty()) {
				// no hypervisors on this tier (?)
				continue;
			}
			
			// make it a set to remove duplicates
			final Set<String> hypervisorSet = new HashSet<String>(hypervisors);
			
			for (final String hypervisor : hypervisorSet) {
				try {
					networkService.addNetworkVlanTrunks(hypervisor, vlanIDs);
				} catch (CPEException e) {
					new NagiosEventLogger().emitCritical("NAGIOSMONITOR VLANTRUNKHYP :"+e.getLocalizedMessage());
					throw e;
				}
			}
		}

		if (clusterDetails.getClusterMachines().size() != cluster.getSize()) {
			logger.info(String.format("Cluster %s has %d machines; CPE expects %d.  Try again next interval.", cluster.getId(), clusterDetails.getClusterMachines().size(), cluster.getSize()));
			return;
		}
		
		for (final ClusterMachine machine : clusterDetails.getClusterMachines()) {
			if (!machine.getStatus().equals("Starting") && !machine.getStatus().equals("On")) {
				// machine must be "On", this means PCM-AE has placed the machine on the hyp where it will be trunked
				logger.info(String.format("Cluster %s machine %s status is %s. CPE expects \"Starting\" or  \"On\".  Try again.", cluster.getId(), machine.getName(), machine.getStatus()));
				return;
			}
		}
		
		
		// next step!
		this.goToNextStep(cluster);
	}
	
	/**
	 * submits metered usage events back to AppDirect
	 * 
	 * @param offeringId
	 * @param subscriberId
	 * @param usages a list of usage events
	 */
	private void submitBilling(Cluster cluster) throws CPEException {
		//find a way to include billing logic for flexup request
		//would the existing logic work?
		
		final com.ibm.scas.analytics.persistence.beans.Subscriber subscriberRec = cluster.getOwner();

		if (subscriberRec.getType() != com.ibm.scas.analytics.persistence.beans.Subscriber.APPDIRECT) {
			logger.info(String.format("Subscriber %s is not from AppDirect. Skipping billing...", subscriberRec.getId()));
			this.goToNextStep(cluster);
			return;
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Calling AppDirect now...");
		}

		final List<Usage<? extends BillingItem>> usages = provisionService.getUsagesFromCluster(cluster.getId());
		final com.ibm.scas.analytics.persistence.beans.Account acct = subscriberRec.getAccount();
		final String eventUrl = acct.getMarketUrl() + USAGE_API_PATH;
		if (logger.isDebugEnabled()) {	
			logger.debug("Metered usage url: " + eventUrl);
		}

		OauthAdapter adapter = new OauthAdapter(acct.getOffering().getOauthKey(), acct.getOffering().getOauthSecret());

		for (Usage<? extends BillingItem> usage : usages) {
			usage.getAccount().setAccountIdentifier(String.valueOf(acct.getId()));
			String usageJson = new Gson().toJson(usage);
			if (logger.isDebugEnabled()) {
				logger.debug("Submitting usage event: " + usageJson);
			}
			adapter.doPost(eventUrl, usageJson);
		}
		
		this.goToNextStep(cluster);
	}
	
	private void waitForGateway(Cluster cluster) throws CPEException {
		final Account account = this.tenantService.getAccountById(cluster.getOwner().getAccount().getId());
		final String dgwPropVal = account.getProperties().get(com.ibm.scas.analytics.persistence.beans.Account.ACCOUNT_PROPS_DGW);
		
		boolean isDgw = (dgwPropVal == null) ? false : Boolean.valueOf(dgwPropVal);
		
		// If account has purchased a dedicated gateway and gateway is not ready then just return.
		// If account has purchased a dedicated gateway and gateway is ready then goto next step.
		// If account did not purchase a dedicated gateway then goto next step.
		if (isDgw && !isAccountGatewayReady(account.getId())) {
			return;
		}
		
		this.goToNextStep(cluster);
		
	}

	private void waitForOrder(Cluster cluster) throws CPEException {
		final List<SoftLayerOrder> orders = orderProvider.getOrdersForCluster(cluster.getId());
		
		if (orders == null || orders.size() == 0) {
			logger.debug(String.format("Found 0 orders for cluster %s", cluster.getId()));
			return;
		}

		logger.trace(String.format("Found %d orders for cluster %s", orders.size(), cluster.getId()));
		
		for (SoftLayerOrder order : orders) {
			logger.trace(String.format("Found order id %s with softLayerId %s with %d hardwares", order.getId(), order.getSoftLayerId(), order.getHardwares().size()));
			if (order.getSoftLayerId() == null)
				return;
		}
		
		this.goToNextStep(cluster);
	}
	
	// The customer's dedicated gateway is ready if:
	// 1. The account has a dedicated gateway assigned to it.
	// 2. The dedicated gateway is in the ACTIVE state
	// 3. There are no gateway clusters that are still provisioning
	private boolean isAccountGatewayReady(String accountId) throws CPEException {
		final List<Gateway> gateways = networkService.getAccountGateways(accountId);
		
		if (gateways.isEmpty()) {
			logger.debug(String.format("Found 0 dedicated gateways for account %s", accountId));
			return false;
		}
		
		for (Gateway gateway : gateways) {
			if (gateway.getStatus() != GatewayStatus.ACTIVE) {
				logger.debug(String.format("Found dedicated gateway %s (%s) for account %s with status %s. Wait for gateway to go Active.", gateway.getName(), gateway.getId(), accountId, gateway.getStatus().name()));
				return false;
			}
		}

		final List<WhereClause> wheres = new ArrayList<WhereClause>();
		wheres.add(new WhereClause("currentStep", ClusterStep.NONE.name(), false));
		wheres.add(new WhereClause("owner.type", Subscriber.SYSTEM, true));
		
		final List<Cluster> clusters = persistence.getObjectsBy(Cluster.class, wheres.toArray(new WhereClause[] {}));
		
		for (Cluster cluster : clusters) {
			if (Cluster.CLUSTERTYPE_DEDICATED_GATEWAY.equals(cluster.getClusterParams().get(Cluster.CLUSTER_PROP_CLUSTERTYPE))) {
				return false;
			}
		}
		
		return true;
	}
	
	private void provisionGateway(Cluster cluster) throws CPEException {
		final Account account = tenantService.getAccountById(cluster.getOwner().getAccount().getId());

		if (account == null) {
			throw new CPEException(String.format("Could not find account record for account id %s", cluster.getOwner().getAccount().getId()));
		}
		
		final List<Gateway> gateways = this.networkService.getAllGateways();
		
		final Map<String, Gateway> gatewayMap = new HashMap<String, Gateway>();
		for (Gateway gateway : gateways) {
			gatewayMap.put(gateway.getName(), gateway);
		}
		
		final List<SoftLayerOrder> orders = this.orderProvider.getOrdersForCluster(cluster.getId());
		
		for (SoftLayerOrder order : orders) {
			final List<SoftLayerHardware> hardwares = order.getHardwares();
			
			hardwareLoop : for (SoftLayerHardware hardware : hardwares) {
				final Gateway g = gatewayMap.get(hardware.getName());
				logger.debug(String.format("Checking hardware %s in order %s for cluster %s ...", hardware.getName(), order.getId(), cluster.getId()));

				// If the order contains a gateway that already exists in cpe then ...
				if (g != null) {
					logger.debug(String.format("Found an existing gateway in CPE with the same name %s", g.getName()));
					// Check if it is a dedicated gateway, if it is not then continue
					if (g.getType() != GatewayType.DEDICATED) {
						logger.info(String.format("Skip monitoring this gateway %s because it is of type %s.", g.getName(), g.getType()));
						continue hardwareLoop;
					}
					
					// If it is a dedicated gateway, then check if it is assigned to a different account, if it is then continue
					if (g.getAccount() != null && !account.getId().equals(g.getAccount().getId())) {
						logger.info(String.format("Skip monitoring the gateway %s because it is already assigned to another account. Verify the SoftLayer order id %s is correct.", g.getName(), order.getSoftLayerId()));
						continue hardwareLoop;
					}
				}
				
				if (!hardware.getStatus().equals("ACTIVE")) {
					logger.info(String.format("Waiting for hardware %s in order %s to become ACTIVE.", hardware.getName(), order.getId()));
					return;
				}
				
				logger.info(String.format("Hardware %s in order %s is now ACTIVE.", hardware.getName(), order.getId()));
				
				final String gatewayId;
				
				// Revist for HA dedicated gateway...do we add each member gateway???
				if (g == null) {
					//Add gateway into DB
					final Gateway gatewayReq = new Gateway();
					gatewayReq.setType(GatewayType.DEDICATED);
					gatewayReq.setName(hardware.getName());
					gatewayReq.setSoftLayerAccount(order.getSoftLayerAccount());
					gatewayReq.setAccount(account);
					gatewayReq.setLocation(myLocationName);
					
					logger.info(String.format("Adding gateway %s ...", hardware.getName()));
					gatewayId = this.networkService.addGateway(gatewayReq);
				} else {
					gatewayId = g.getId();
					this.networkService.updateGatewayAccount(gatewayId, account.getAccountIdentifier());
				}

				logger.info(String.format("Updating SSL Certificate for gateway %s (%s) ...", hardware.getName(), gatewayId));
				this.networkService.updateGatewaySSLCert(gatewayId);

				if (cluster.getClusterParams() == null) {
					cluster.setClusterParams(new HashMap<String, String>());
				} 
						
				logger.debug(String.format("Adding gateway id %s to Cluster %s params.", gatewayId, cluster.getId()));
				cluster.getClusterParams().put(Cluster.CLUSTER_PROP_GATEWAYID, gatewayId);
			}
		}

		this.goToNextStep(cluster);
	}
	
	private void configNodes(Cluster cluster) throws CPEException {
		final String type = cluster.getClusterParams().get(Cluster.CLUSTER_PROP_CLUSTERTYPE);

		if (Cluster.CLUSTERTYPE_DEDICATED_GATEWAY.equals(type)) {
			final String gatewayId = cluster.getClusterParams().get(Cluster.CLUSTER_PROP_GATEWAYID);
			
			if (gatewayId == null) {
				throw new CPEException(String.format("%s property for cluster %s is not set in CLUSTER_PARAMS table.", Cluster.CLUSTER_PROP_GATEWAYID, cluster.getId()));
			}
			final Gateway gateway = this.networkService.getGatewayById(gatewayId);
			
			if (gateway == null) {
				throw new CPEException(String.format("Cannot find gateway record for id %s", gatewayId));
			}
			// Apply default firewall rules
			logger.debug(String.format("Applying default firewall rules to gateway %s (%s) ... ", gateway.getName(), gateway.getId()));
			this.firewallService.addDefaultFirewallRules(gateway);
		}

		this.goToNextStep(cluster);
	}
	
	/**
	 * Perform a step in cluster creation.  If successful, the cluster moves to the next step.  If failed, try again.
	 * Note that if the method throws an exception, the transaction doesn't commit, so on the next interval, the cluster
	 * build will try again.
	 * 
	 * NOTE: @Transactional only works on public, protected, and package-private methods. 
	 * @param cluster
	 * @throws CPEException
	 */
	@Transactional(rollbackOn = { CPEException.class, RuntimeException.class } )
	void performClusterStep(Cluster cluster) throws CPEException {
		final ClusterStep currentStep = ClusterStep.valueOf(cluster.getCurrentStep());
		
		switch (currentStep) {
		case INIT:
			goToNextStep(cluster);
			break;
		case WAITING_FOR_GATEWAY:
			waitForGateway(cluster);
			break;
		case ORDER_PENDING:
			waitForOrder(cluster);
			break;
		case RESERVE_NETWORK:
			reserveNetwork(cluster);
			break;
		case PROVISION_NODES:
			provisionCluster(cluster);
			break;
		case CONFIG_NODES:
			configNodes(cluster);
			break;
		case CONFIGURE_MGMT_GATEWAY:
			configureMgmtGateway(cluster);
			break;
		case TRUNK_CUST_GATEWAY:
			trunkCustomerGateway(cluster);
			break;
		case CONFIGURE_CUST_GATEWAY:
			configureCustomerGateway(cluster);
			break;
		case TRUNK_HYPERVISORS:
			trunkHypervisors(cluster);
			break;
		case SUBMIT_BILLING:
			submitBilling(cluster);
			break;
		default:
			logger.warn(String.format("Cluster %s is in state: %s, nothing to do", cluster.getId(), currentStep.name()));
			break;
		}
	}
}
