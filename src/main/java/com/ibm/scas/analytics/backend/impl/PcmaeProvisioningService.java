package com.ibm.scas.analytics.backend.impl;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ibm.pcmae.ClusterAPI;
import com.ibm.pcmae.client.ClusterNotFoundException;
import com.ibm.pcmae.cluster.beans.ClusterDetails;
import com.ibm.pcmae.cluster.beans.ClusterMachine;
import com.ibm.pcmae.cluster.beans.Message;
import com.ibm.pcmae.cluster.beans.Parameter;
import com.ibm.scas.analytics.EngineProperties;
import com.ibm.scas.analytics.backend.NetworkService;
import com.ibm.scas.analytics.backend.PcmaeGateway;
import com.ibm.scas.analytics.backend.ProvisioningException;
import com.ibm.scas.analytics.backend.ProvisioningService;
import com.ibm.scas.analytics.backend.TenantService;
import com.ibm.scas.analytics.backend.monitoring.NagiosEventLogger;
import com.ibm.scas.analytics.beans.BillingItem;
import com.ibm.scas.analytics.beans.Cluster;
import com.ibm.scas.analytics.beans.Cluster.ClusterStep;
import com.ibm.scas.analytics.beans.ClusterRequest;
import com.ibm.scas.analytics.beans.SoftLayerOrder;
import com.ibm.scas.analytics.beans.Usage;
import com.ibm.scas.analytics.beans.utils.BeanConversionUtil;
import com.ibm.scas.analytics.persistence.PersistenceException;
import com.ibm.scas.analytics.persistence.PersistenceService;
import com.ibm.scas.analytics.persistence.beans.Account;
import com.ibm.scas.analytics.persistence.beans.CPELocation;
import com.ibm.scas.analytics.persistence.beans.Offering;
import com.ibm.scas.analytics.persistence.beans.SoftLayerAccount;
import com.ibm.scas.analytics.persistence.beans.SoftLayerLocation;
import com.ibm.scas.analytics.persistence.beans.Subscriber;
import com.ibm.scas.analytics.persistence.beans.VPNTunnel;
import com.ibm.scas.analytics.persistence.util.WhereClause;
import com.ibm.scas.analytics.persistence.util.WhereInClause;
import com.ibm.scas.analytics.provider.NameValuePair;
import com.ibm.scas.analytics.provider.ServiceProvider;
import com.ibm.scas.analytics.provider.ServiceProviderException;
import com.ibm.scas.analytics.provider.impl.ServiceProviderPluginFactory;
import com.ibm.scas.analytics.utils.CPEException;
import com.ibm.scas.analytics.utils.CPEParamException;
import com.ibm.scas.analytics.utils.OauthAdapter;
import com.ibm.scas.analytics.utils.PromoCode;

/**
 * A mock implementation of the provisioning service. No actual PCM-AE calls are
 * made
 * 
 * @author Han Chen
 * 
 */
@Singleton
public class PcmaeProvisioningService implements ProvisioningService {
	private final static Logger logger = Logger.getLogger(PcmaeProvisioningService.class);
	private final static SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
	private final static String USAGE_API_PATH = "/api/integration/v1/billing/usage";
	private final static String cpeLocationName = EngineProperties.getInstance().getProperty(EngineProperties.CPE_LOCATION_NAME, EngineProperties.DEFAULT_CPE_LOCATION_NAME);
	private static String pcmaeBackendId = null;
	
	@Inject	private PersistenceService persistence;
	@Inject private NetworkService networkService;
	@Inject private ServiceProviderPluginFactory pluginFactory;
	@Inject private PcmaeGateway pcmaeGateway;
	@Inject private TenantService tenantService;
	@Inject private ClusterBuilder clusterBuilder;
	@Inject private ClusterGarbageCollector garbageCollector;
	
	private final Set<String> availableClusters;
	private final Set<String> inUseClusters;
	private final Map<String, String> testClusterMap;

	private final static String myLocationName = EngineProperties.getInstance().getProperty(EngineProperties.CPE_LOCATION_NAME, EngineProperties.DEFAULT_CPE_LOCATION_NAME);
	
	public PcmaeProvisioningService() { 
		availableClusters = new HashSet<String>();
		inUseClusters = new HashSet<String>();
		testClusterMap = new HashMap<String, String>();
	}
	
	public void afterInject() {
		// called after Guice instantiates me and injects my dependencies
		String clusterPool = EngineProperties.getInstance().getProperty(EngineProperties.CLUSTER_POOL);
		String[] clusterIds = clusterPool.split(",");
		availableClusters.addAll(Arrays.asList(clusterIds));
		logger.info("Initializing cluster pool");
		logger.info(" : " + availableClusters);
		
		logger.info("Discovering clusters that are already in use...");
		try {
			final List<com.ibm.scas.analytics.persistence.beans.Cluster> discoveredCluster = 
					persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Cluster.class, 
							new WhereClause("owner.type", Subscriber.CLOUDOE_PULSE_DEMO));

			logger.info(discoveredCluster.size() + " clusters found.");
			synchronized (availableClusters) {
				for (com.ibm.scas.analytics.persistence.beans.Cluster cluster : discoveredCluster) {
					if (availableClusters.contains(cluster.getId())) {
						logger.info(" - " + cluster.getId() + " moved to in-use pool"); 
						availableClusters.remove(cluster.getId());
						inUseClusters.add(cluster.getId());
					} else {
						logger.info(" - " + cluster.getId() + " is not from the pool. Ignored!");
					}
				}
				logger.info("Available: " + availableClusters.size() + ", In use: " + inUseClusters.size());
			}
			
		} catch (CPEException e) {
			logger.warn("Unable to get clusters: " + e.getLocalizedMessage());
		}
	
		List<Offering> offerings = Collections.emptyList();
		try {
			offerings = persistence.getAllObjects(Offering.class);
		} catch (PersistenceException e) {
			logger.error(e.getLocalizedMessage(), e);
			System.exit(1);
		}
		
		for (final Offering offering : offerings) {
			String offeringId = offering.getId();
			String testClusterId = EngineProperties.getInstance().getProperty("test.cluster." + offeringId);
			if (testClusterId != null) {
				logger.info("Found test cluster for '" + offeringId + "': " + testClusterId);
				testClusterMap.put(offeringId, testClusterId);
			}
		}
	}
	
	private String acquireClusterFromPool() {
		logger.info("Acquiring cluster from the pool");
		synchronized (availableClusters) {
			if (availableClusters.isEmpty()) {
				logger.error("Pool is empty!");
				return null;
			}
			String clusterId = availableClusters.iterator().next();
			availableClusters.remove(clusterId);
			inUseClusters.add(clusterId);
			logger.info("Cluster id: " + clusterId);
			logger.info("Available: " + availableClusters.size() + ", In use: " + inUseClusters.size());
			return clusterId;
		}
	}
	
	private void releaseClusterToPool(String clusterId) {
		logger.info("Releasing cluster to the pool. cluster id: " + clusterId);
		synchronized (availableClusters) {
			if (!inUseClusters.contains(clusterId)) {
				logger.error("Cluster is not from the pool!");
				return;
			}
			inUseClusters.remove(clusterId);
			availableClusters.add(clusterId);
			logger.info("Available: " + availableClusters.size() + ", In use: " + inUseClusters.size());
		}
	}
	
	@Override
	public List<Cluster> getClusters(Collection<String> clusterIds) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("getClusters(): %s", clusterIds));
		}
		
		final List<com.ibm.scas.analytics.persistence.beans.Cluster> clusterRecs = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Cluster.class,
				new WhereInClause("id", clusterIds));
		
		if (clusterRecs.isEmpty()) {
			return Collections.emptyList();
		}
		
		final List<Cluster> clusters = new ArrayList<Cluster>(clusterRecs.size());
		for (final com.ibm.scas.analytics.persistence.beans.Cluster clusterRec : clusterRecs) {
			final Cluster cluster = BeanConversionUtil.convertToBean(clusterRec);
			
			clusters.add(cluster);
			this.addClusterDetails(cluster);
		}
		
		return clusters;
	}

	@Override
	public List<Cluster> listAllClusters() throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace("listAllClusters()");
		}
		
		final List<com.ibm.scas.analytics.persistence.beans.Cluster> clusterRecs = persistence.getAllObjects(com.ibm.scas.analytics.persistence.beans.Cluster.class );
		
		if (clusterRecs.isEmpty()) {
			return Collections.emptyList();
		}
		
		final List<Cluster> clusters = new ArrayList<Cluster>(clusterRecs.size());
		for (final com.ibm.scas.analytics.persistence.beans.Cluster clusterRec : clusterRecs) {
			final Cluster cluster = BeanConversionUtil.convertToBean(clusterRec);
			
			clusters.add(cluster);
			this.addClusterDetails(cluster);
		}
		
		return clusters;
	}

	
	@Override
	public int getTotalCapacity() throws CPEException {
		return new PcmaeDatabaseConnector().getTotalCapacity();
	}

	@Override
	public int getAvailableCapacity() throws CPEException {
		return getTotalCapacity() - getCommittedCapacity();
	}

	@Override
	public int getCommittedCapacity() throws CPEException {
		int committedCapacity = 0;
		final List<Account> accounts;
		try {
			accounts = persistence.getAllObjects(Account.class);
		} catch (PersistenceException e) {
			logger.error(e);
			throw e;
		}
		
		for (final Account account : accounts) {
			if (Account.SYSTEM_PARTNER.equals(account.getPartner())) {
				continue;
			}
			int state = account.getState();
			if (state != Account.ACTIVE && state != Account.FREE_TRIAL) {
				/*
				 * only count active accounts towards committed capacity.
				 * expired accounts should not have clusters
				 */
				continue;
			}
			/*
			 * translate nominal node quantity to canonical node quantity 
			 */
			final int nodes = account.getQuantity();
			final String edition = account.getEdition();
			final ServiceProvider plugin = pluginFactory.getPlugin(account.getOffering().getId());
			final int nodeSize = plugin.getEditionNodeSize(edition);
			committedCapacity += nodes * nodeSize;
		}
		
		return committedCapacity;
	}

	@Override
	public List<Cluster> listClusters(String subscriberId) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace("listClusters(): subscriberId: " + subscriberId);
		}
		
		try {
			final List<com.ibm.scas.analytics.persistence.beans.Cluster> clusterRecs = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Cluster.class, new WhereClause("owner.id", subscriberId));
			
			if (clusterRecs.isEmpty()) {
				return Collections.emptyList();
			}
			
			final List<Cluster> clusters = new ArrayList<Cluster>(clusterRecs.size());
			for (final com.ibm.scas.analytics.persistence.beans.Cluster clusterRec : clusterRecs) {
				final Cluster cluster = BeanConversionUtil.convertToBean(clusterRec);
				
				clusters.add(cluster);
				this.addClusterDetails(cluster);
			}
			
			return clusters;
		} catch (PersistenceException e) {
			logger.error(e);
			throw e;
		}
	
	}

	@Override
	public List<Cluster> listClustersInAccount(String subscriberId) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace("listClustersInAccount(), subscriberId: " + subscriberId);
		}
		
		if (subscriberId == null) {
			return Collections.emptyList();
		}
		
		final Subscriber subscriber = persistence.getObjectById(Subscriber.class, subscriberId);
		String accountId = subscriber.getAccount().getId();
		if (logger.isTraceEnabled()) {
			logger.trace("listClustersInAccount(): subscriberId: " + subscriberId + ", account id: " + accountId);
		}
		
		final List<com.ibm.scas.analytics.persistence.beans.Cluster> clusterRecs = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Cluster.class, new WhereClause("owner.account.id", accountId));
		if (clusterRecs.isEmpty()) {
			return Collections.emptyList();
		}
		
		final List<Cluster> clusters = new ArrayList<Cluster>(clusterRecs.size());
		for (final com.ibm.scas.analytics.persistence.beans.Cluster clusterRec : clusterRecs) {
			final Cluster cluster = BeanConversionUtil.convertToBean(clusterRec);
			
			clusters.add(cluster);
			this.addClusterDetails(cluster);
		}
		
		return clusters;
	}

	@Override
	public Cluster getClusterDetails(String clusterId) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace("getClusterDetails(): cluster: " + clusterId);
		}
	
		final com.ibm.scas.analytics.persistence.beans.Cluster clusterRec = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Cluster.class, clusterId);
		if (clusterRec == null) {
			return null;
		}
		
		final Cluster cluster = BeanConversionUtil.convertToBean(clusterRec);
		this.addClusterDetails(cluster);
		
		return cluster;
	}

	@Override
	public String createCluster(String subscriberId, ClusterRequest req, Locale locale) throws CPEException {
		/* creates the cluster record and start the cluster provisioning process */
		// note: caller starts the transaction
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("createCluster(): subscriberId: %s, req: %s, locale: %s", subscriberId, ReflectionToStringBuilder.toString(req), locale));
		}
		
		String returnClusterId = null;
		
		if (logger.isDebugEnabled()) {
			logger.debug("createCluster(): Request parameters:");
			for (String k : req.getParameters().keySet()) {
				String v = req.getParameters().get(k);
				logger.debug(" " + k + ": " + v);
			}
		}
		
		final com.ibm.scas.analytics.persistence.beans.Cluster cluster = new com.ibm.scas.analytics.persistence.beans.Cluster();
		cluster.setName(req.getName());
		cluster.setDescription(req.getDescription());
		cluster.setSize(req.getSize());

		long timeNow = System.currentTimeMillis();
		cluster.setLaunchTime(timeNow);

		final Subscriber subscriber = persistence.getObjectById(Subscriber.class, subscriberId);
		if (subscriber == null) {
			throw new CPEParamException(String.format("Subscriber not found in database: %s", subscriberId));
		}
		
		// make sure there's no other clusters with same name
		final List<com.ibm.scas.analytics.persistence.beans.Cluster> existingClusterRec = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Cluster.class, new WhereClause("owner.id", subscriberId), new WhereClause("name", req.getName()));
		if (!existingClusterRec.isEmpty()) {
			throw new CPEParamException(String.format("A cluster with name %s already exists for subscriber %s.", req.getName(), subscriber.getName()));
		}
		
		final Account account = subscriber.getAccount();
		final String offeringId = account.getOffering().getId();
		long expiration = account.getExpiration();
		if (expiration == PromoCode.NO_EXPIRATION) {
			/*
			 * set expiration to be 20 years from now. this indicates to PCM-AE that there is no expiry
			 */
			expiration = timeNow + 20 * 365 * 86400L * 1000;
			cluster.setTerminateTime(expiration);
		} else {
			cluster.setTerminateTime(expiration);
		}
		
		ResourceBundle rsrc = ResourceBundle.getBundle("cpe_messages", locale);
		if (timeNow >= expiration) {
			throw new ProvisioningException(rsrc.getString("pcmae.trial"));
		}

		if (account.getQuantity() > 0) {
			int currentUsage = this.tenantService.getCurrentUsageForAccount(account.getId());
			int newUsage = currentUsage + cluster.getSize();
			if (newUsage > account.getQuantity()) {
				throw new ProvisioningException(rsrc.getString("pcmae.avail"));
			}
		}
		
		final ServiceProvider plugin = pluginFactory.getPlugin(offeringId);
		if (plugin == null) {
			logger.error("createCluster(): Failed to obtain service provider plugin");
			throw new ProvisioningException(String.format("Service provider plugin for offering \"%s\" not found.", offeringId));
		}
		
		cluster.setOwner(subscriber);
		cluster.setClusterParams(req.getParameters());
		
		persistence.saveObject(com.ibm.scas.analytics.persistence.beans.Cluster.class, cluster);
		
		returnClusterId = cluster.getId();
		
		//first step: reserve  network
		cluster.setCurrentStep(ClusterStep.INIT.name());	
		persistence.updateObject(com.ibm.scas.analytics.persistence.beans.Cluster.class, cluster);
		
		logger.info(String.format("Created new cluster record with id %s (name: %s).", returnClusterId, cluster.getName()));
		
		// tell the cluster builder to start building
		this.clusterBuilder.addClusterId(returnClusterId);

		return returnClusterId;
	}

	@Override
	public boolean modifyCluster(String subscriberId, String clusterId, ClusterRequest req, Locale locale) throws CPEException {
		// note: caller starts the transaction
		if (logger.isTraceEnabled()) {
			logger.trace("modifyCluster(): subscriber: " + subscriberId + ", cluster: " + clusterId);
		}

		final com.ibm.scas.analytics.persistence.beans.Cluster cluster = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Cluster.class, clusterId);
		if (cluster == null) {
			throw new ProvisioningException(String.format("Cannot find cluster with ID %s.", clusterId));
		}
		
		// Update the name if its not blank and is different from what is currently stored in DB
		if (!StringUtils.isBlank(req.getName()) && !req.getName().equals(cluster.getName()))
			cluster.setName(req.getName());
		
		// Update the descr if its not blank and is different from what is currently stored in DB
		if (!StringUtils.isBlank(req.getDescription()) && !req.getDescription().equals(cluster.getDescription()))
			cluster.setDescription(req.getDescription());
		
		// save these details first
		persistence.saveObject(com.ibm.scas.analytics.persistence.beans.Cluster.class, cluster);

		int currentSize = cluster.getSize();
		int newSize = req.getSize();
		
		if (newSize <= 0 || newSize == currentSize) {
			// no flexing to do
			return true;
		}
		
		if (newSize < currentSize) {
			logger.info("modifyCluster(): Flexing down from " + currentSize + " to " + newSize + "...");
			logger.warn("modifyCluster(): - Flex down not supported now. Ignored");
			return true;
		}

		// newSize > currentSize
		logger.info("Flexing up from " + currentSize + " to " + newSize + "...");

		final Subscriber subscriber = persistence.getObjectById(Subscriber.class, subscriberId);
		final Account account = subscriber.getAccount();
		
		if (account.getQuantity() > 0) {
			int currentUsage = this.tenantService.getCurrentUsageForAccount(account.getId());
			int newUsage = currentUsage + newSize - currentSize;
			if (newUsage > account.getQuantity()) {
				logger.info(" - Quota reached: " + newUsage + "/" + account.getQuantity());
				throw new ProvisioningException("You do not have enough available nodes in your account.");
			}
		}
		
		// set cluster size if there is available capacity
		cluster.setSize(newSize);
		
		String offeringId = account.getOffering().getId();
		ServiceProvider plugin = pluginFactory.getPlugin(offeringId);
		final String pcmaeClusterId = cluster.getClusterId();
		
		if (plugin == null) {
			throw new ProvisioningException("Service provider plugin not found!");
		}
		
		if (plugin.getFlexTierName() == null) {
			logger.info("Flex not supported by product. Request ignored.");		
			return true;
		}
		
		if (pcmaeClusterId == null) {
			logger.info(String.format("modifyCluster(): No PCM-AE cluster found for cluster ID %s. Dummy cluster.", cluster.getId()));
			
			persistence.saveObject(com.ibm.scas.analytics.persistence.beans.Cluster.class, cluster);
			return true;
		} 
		
		if (subscriber.getType() == Subscriber.CLOUDOE_PULSE_DEMO) {
			logger.info("Subscriber is special PULSE demo user. Flexing request ignored.");
			persistence.saveObject(com.ibm.scas.analytics.persistence.beans.Cluster.class, cluster);
			return true;		
		} 
		
		final ClusterAPI api = pcmaeGateway.getClusterApi(getPcmaeBackendId());
		final com.ibm.pcmae.cluster.beans.ClusterDetails pcmaeDetails = api.getClusterDetails(pcmaeClusterId);
		if (pcmaeDetails == null) {
			logger.warn("No PCM-AE cluster ID found. Cluster maybe removed by backend. Flexing request ignored.");
			return true;
		}
		
		if (!"ACTIVE".equals(pcmaeDetails.getState()) || !"".equals(pcmaeDetails.getApplicationAction())) {
			logger.info("Cluster is not in Active state. Flexing request ignored.");
			return true;
		} 
		//for now commented the billing part
		/*if (subscriber.getType() == Subscriber.APPDIRECT) {
			List<Usage<? extends BillingItem>> usages = new ArrayList<Usage<? extends BillingItem>>();
			plugin.generateFlexUsages(subscriberId, currentSize, newSize, usages);
			if (logger.isDebugEnabled()) {	
				logger.debug("modifyCluster(): Calling AppDirect now...");
			}
			submitCharge(offeringId, subscriberId, usages);
		} else {
			logger.info(String.format("modifyCluster(): Subscriber %s is not from AppDirect. Skipping billing...", subscriber.getId()));
		}*/

		cluster.setSize(newSize);

		if (logger.isDebugEnabled()) {
			logger.debug("Updating cluster details in database.");
		}
		//first step: init
		cluster.setCurrentStep(ClusterStep.INIT.name());
		persistence.updateObject(com.ibm.scas.analytics.persistence.beans.Cluster.class, cluster);
		logger.info(String.format("modifyCluster() : Modified the cluster record with id %s (name: %s).", cluster.getId(), cluster.getName()));
		
		// tell the cluster builder to start flexing the cluster
		this.clusterBuilder.addClusterId(cluster.getId());
		
		return true;
	}
	
	@Override
	public boolean subscriberHasAccessToCluster(String subscriberId, String clusterId) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace("subscriberHasAccessToCluster(): subscriberId: " + subscriberId + ", clusterId: " + clusterId);
		}
		
		final Subscriber subscriber = persistence.getObjectById(Subscriber.class, subscriberId);
		
		final List<com.ibm.scas.analytics.persistence.beans.Cluster> subscriberClusters = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Cluster.class, new WhereClause("owner.account.id", subscriber.getAccount().getId()), new WhereClause("id", clusterId));
		
		return !subscriberClusters.isEmpty();
	}
	
	@Override
	public  void deleteCluster(String subscriberId, final String clusterId, Locale locale) throws CPEException {
		// note: caller starts the transaction
		if (logger.isTraceEnabled()) {
			logger.trace("deleteCluster(): subscriber: " + subscriberId + ", cluster: " + clusterId);
		}

		// note: caller starts the transaction
		final com.ibm.scas.analytics.persistence.beans.Cluster cluster = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Cluster.class, clusterId);
		if (cluster == null) {
			throw new ProvisioningException(String.format("Cluster %s not found!", clusterId));
		}
		
		// note that VLAN and IPs are not released here.  cluster garbage collector will reclaim when cluster has been cancelled.
		
		final String pcmaeClusterId = cluster.getClusterId();

		final Subscriber subscriber = cluster.getOwner();
		final Account account = subscriber.getAccount();
		final String offeringId = account.getOffering().getId();

		if (subscriber.getType() == Subscriber.CLOUDOE_PULSE_DEMO) {
			logger.info("deleteCluster(): Subscriber is special PULSE demo user.");

			releaseClusterToPool(pcmaeClusterId);

			if (logger.isDebugEnabled()) {
				logger.debug("deleteCluster(): Removing cluster from database.");
			}
			persistence.deleteObject(com.ibm.scas.analytics.persistence.beans.Cluster.class, cluster);
		} else if (testClusterMap.containsKey(offeringId)) {
			logger.info("deleteCluster(): Test cluster for offering '" + offeringId + "'");
			if (logger.isDebugEnabled()) {
				logger.debug("deleteCluter(): Removing cluster from database.");
			}
			persistence.deleteObject(com.ibm.scas.analytics.persistence.beans.Cluster.class, cluster);
		} else {
			// make sure the cluster is not already assigned to garbage collector
			if (cluster.getOwner().getType() == Subscriber.GARBAGE_COLLECTOR) {
				logger.warn(String.format("Cluster %s is already assigned to garbage collector %s.  Will start the cluster over at %s", cluster.getId(), cluster.getOwner().getId(), ClusterStep.CANCEL_CLUSTER.name())); 
			}
			
			final List<Subscriber> gcList = persistence.getObjectsBy(Subscriber.class, new WhereClause("account.offering.id", offeringId), 
					new WhereClause("type", Subscriber.GARBAGE_COLLECTOR));
			if (gcList == null || gcList.isEmpty()) {
				logger.error("deleteCluster(): CPE database is mis-configured. Garbage collector not defined for offering: " + offeringId); 
				new NagiosEventLogger().emitCritical("Failed to schedule cluster deletion, misconfiguration (name: " + cluster.getName() + ", id: " + pcmaeClusterId + ")");
			} else {
				final String gcSubscriberId = gcList.get(0).getId();
				final Subscriber gcSubscriber = persistence.getObjectById(Subscriber.class, gcSubscriberId);

			
				// set the owner to garbage collector
				cluster.setOwner(gcSubscriber);
				cluster.setCurrentStep(ClusterStep.CANCEL_CLUSTER.name());
				persistence.saveObject(com.ibm.scas.analytics.persistence.beans.Cluster.class, cluster);
				
				this.garbageCollector.addClusterId(clusterId);
				
				logger.info(String.format("deleteCluster(): Cluster %s is scheduled for deletion by garbage collector.", cluster.getId())); 
				new NagiosEventLogger().emitOK("Cluster deletion scheduled (name: " + cluster.getName() + ", id: " + pcmaeClusterId + ")");
			}
		}

	}

	/**
	 * submits metered usage events back to AppDirect
	 * 
	 * @param offeringId
	 * @param subscriberId
	 * @param usages a list of usage events
	 */
	protected void submitCharge(String offeringId, String subscriberId, List<Usage<? extends BillingItem>> usages) throws CPEException {
		final Subscriber subscriber = persistence.getObjectById(Subscriber.class, subscriberId);
		final Account acct = subscriber.getAccount();
		final String eventUrl = acct.getMarketUrl() + USAGE_API_PATH;
		if (logger.isDebugEnabled()) {	
			logger.debug("Metered usage url: " + eventUrl);
		}

		final Offering offering = persistence.getObjectById(Offering.class, offeringId);
		OauthAdapter adapter = new OauthAdapter(offering.getOauthKey(), offering.getOauthSecret());

		for (Usage<? extends BillingItem> usage : usages) {
			usage.getAccount().setAccountIdentifier(String.valueOf(acct.getId()));
			String usageJson = new Gson().toJson(usage);
			if (logger.isDebugEnabled()) {
				logger.debug("Submitting usage event: " + usageJson);
			}
			adapter.doPost(eventUrl, usageJson);
		}
	}


	@Override
	public void deleteAllClusters(String accountId) throws CPEException {
		// note: caller starts the transaction
		logger.info("deleteAllClusters called, account: " + accountId);
		final List<com.ibm.scas.analytics.persistence.beans.Cluster> clusters = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Cluster.class, new WhereClause("owner.account.id", accountId));
		for (final com.ibm.scas.analytics.persistence.beans.Cluster cluster: clusters) {
			final String subscriberId = cluster.getOwner().getId();
			final String clusterId = cluster.getId();
			deleteCluster(subscriberId, clusterId, Locale.ENGLISH);
		}
	}
	
	 public String getPcmaeBackendId()
	{
		try {
			if (pcmaeBackendId == null) {
				CPELocation cpeLocation = persistence.getObjectById(CPELocation.class, cpeLocationName);
				pcmaeBackendId = cpeLocation.getPcmaeBackend().getId();
			}
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return pcmaeBackendId;
	}
	
	private void addClusterDetails(final Cluster cluster) throws CPEException {
		if (cluster.getDetails() == null)
			cluster.setDetails(new ArrayList<NameValuePair>());
		
		cluster.getDetails().add(new NameValuePair(EngineProperties.CPE_LOCATION_NAME, myLocationName));
		
		final ServiceProvider plugin = pluginFactory.getPlugin(cluster.getOwner().getAccount().getOffering().getId());
		if (plugin == null) {
			throw new CPEException(String.format("Failed to obtain service provider plugin for offering: %s", cluster.getOwner().getAccount().getOffering().getId()));
		}
		
		try {
			plugin.populateClusterDetails(cluster);
		} catch (ServiceProviderException e1) {
			throw new CPEException(e1.getLocalizedMessage());
		}
				
		final String clusterType = cluster.getClusterParams().get(com.ibm.scas.analytics.persistence.beans.Cluster.CLUSTER_PROP_CLUSTERTYPE);
		final String gatewayId = cluster.getClusterParams().get(com.ibm.scas.analytics.persistence.beans.Cluster.CLUSTER_PROP_GATEWAYID);
		

		if(com.ibm.scas.analytics.persistence.beans.Cluster.CLUSTERTYPE_DEDICATED_GATEWAY.equals(clusterType) &&  gatewayId != null)
		{
			final List<VPNTunnel> vpnTunnels = persistence.getObjectsBy(VPNTunnel.class, new WhereClause("gateway.id", gatewayId));
			if (!vpnTunnels.isEmpty()) {
				cluster.setVpnTunnel(BeanConversionUtil.convertToBean(vpnTunnels.get(0)));
			}
		}
		
		if (cluster.getClusterId() == null) {
			// no PCMAE cluster (yet)
			return;
		}
		
		if (logger.isTraceEnabled()) {
			logger.trace("addClusterDetails(): Calling PCM-AE now...");
		}
		
		com.ibm.pcmae.cluster.beans.ClusterDetails pcmaeDetails = null;
		try {
			pcmaeDetails = pcmaeGateway.getClusterApi(getPcmaeBackendId()).getClusterDetails(cluster.getClusterId());
		} catch (ClusterNotFoundException e) {
			// if cluster was not found, then create an attribute that says it's not found and return
			final List<NameValuePair> attributes = new ArrayList<NameValuePair>();
			attributes.add(new NameValuePair("State", "NOT_FOUND"));
			cluster.getDetails().addAll(attributes);
			return;
		} catch (Exception e) {
			logger.debug(e.getLocalizedMessage(), e);
		}
		
		if (pcmaeDetails == null) {
			logger.warn(String.format("Cluster %s not found!", cluster.getClusterId()));
			return;
		} 
		
		try {
			// get all cluster machines
			final List<ClusterMachine> clusterMachines = pcmaeGateway.getClusterApi(pcmaeBackendId).getClusterMachines(cluster.getClusterId(), null);
			for (final ClusterMachine machine : clusterMachines) {
				final com.ibm.scas.analytics.beans.ClusterMachine m = new com.ibm.scas.analytics.beans.ClusterMachine();
				try {
					BeanUtils.copyProperties(m, machine);
					cluster.getClusterMachines().add(m);
				} catch (IllegalAccessException e) {
					throw new CPEException(e.getLocalizedMessage(), e);
				} catch (InvocationTargetException e) {
					throw new CPEException(e.getLocalizedMessage(), e);
				}
			}
		} catch (Exception e) {
			// if no machines, suppress the message
			logger.debug(e.getLocalizedMessage(), e);
		}
		
		final List<NameValuePair> attributes = new ArrayList<NameValuePair>();
		plugin.populateClusterDetails(pcmaeDetails, attributes);
		
		cluster.getDetails().addAll(attributes);
		
		if (pcmaeDetails.getProperties() != null) {
			for (final Parameter param : pcmaeDetails.getProperties()) {
				cluster.getProperties().put(param.getName(), param.getValue());
			}
		}
	}
	
	private ClusterRequest getClusterReqFromRecord(com.ibm.scas.analytics.persistence.beans.Cluster cluster) throws CPEException {
		// reconstruct the cluster request from the record
		final ClusterRequest req = new ClusterRequest();
		req.setName(cluster.getName());
		req.setDescription(cluster.getDescription());
		req.setSize(cluster.getSize());
		req.setParameters(cluster.getClusterParams());
		
		return req;
	}
	
	private ClusterDetails getClusterDetailsFromRecord(com.ibm.scas.analytics.persistence.beans.Cluster cluster) throws CPEException {
		final Account accountRec = cluster.getOwner().getAccount();
		final Offering offeringRec = accountRec.getOffering();
		final String edition = accountRec.getEdition();
		final ServiceProvider plugin = pluginFactory.getPlugin(accountRec.getOffering().getId());
		if (plugin == null) {
			throw new CPEException(String.format("Failed to obtain service provider plugin for offering: %s", offeringRec.getId()));
		}
		
		final List<com.ibm.scas.analytics.persistence.beans.Cluster> existingClusters = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Cluster.class, new WhereClause("owner.id", cluster.getOwner().getId()));
		
		// reconstruct the cluster request from the record
		final ClusterRequest req = this.getClusterReqFromRecord(cluster);
	
		final com.ibm.pcmae.cluster.beans.ClusterDetails details = new com.ibm.pcmae.cluster.beans.ClusterDetails();
		final List<Usage<? extends BillingItem>> usages = new ArrayList<Usage<? extends BillingItem>>();
		String location = req.getParameters().get("DATA_IN_LOCATION");

		//if data location null then check for sdfs location
		if(location == null) {
			String SecureHadoop = req.getParameters().get("USE_SDFS");
			if (SecureHadoop!=null && !SecureHadoop.trim().isEmpty()) {
				location = req.getParameters().get("SDFS_LOCATION");
			}
		}

		SoftLayerLocation slLocationRec = null;
		if (location != null) {
			slLocationRec = persistence.getObjectById(SoftLayerLocation.class, location);
		}

		SoftLayerLocation slLocation = null;
		if (slLocationRec != null) {
			slLocation = new SoftLayerLocation();
			slLocation.setName(slLocationRec.getName());
			slLocation.setPrivateUrl(slLocationRec.getPrivateUrl());
			slLocation.setPublicUrl(slLocationRec.getPublicUrl());
		}
		
		long timeNow = System.currentTimeMillis();
		details.setStartDate(isoDateFormat.format(new Date(timeNow)));
		details.setEndDate(isoDateFormat.format(new Date(cluster.getTerminateTime())));
		
		try {
			plugin.generateCreateClusterDetails(cluster.getOwner().getId(), edition, existingClusters, req, details, usages, slLocation, Locale.ENGLISH);
		} catch (ServiceProviderException e) {
			throw new CPEException(String.format("Failed to generate cluster details for cluster %s: %s", cluster.getId(), e.getMessage()));
		}
		
		return details;
	}

	@Override
	public String provisionCluster(String clusterId) throws CPEException {
		/**
		 * calls PCMAE to actually provision the cluster
		 */
		
		final com.ibm.scas.analytics.persistence.beans.Cluster clusterRec = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Cluster.class, clusterId);
		final Subscriber subscriberRec = clusterRec.getOwner();
		final Account accountRec = subscriberRec.getAccount();
		
		final ClusterDetails details = this.getClusterDetailsFromRecord(clusterRec);
		
		final String magicPassword = EngineProperties.getInstance().getProperty(EngineProperties.PROVISION_MAGIC_PASSWORD);
		if (magicPassword != null && (clusterRec.getDescription() != null && clusterRec.getDescription().contains(magicPassword))) {
			logger.info("Provision magic password not found in your description. no cluster is going to be actually provisioned.");
			new NagiosEventLogger().emitWarning("Dummy cluster created, no password (name: " + clusterRec.getName() + ")");		
			
			return null;
		}
	
		// add to the datatable the subscriber ID, api key, cluster ID, and local REST API
		final CPELocation cpeLocation = persistence.getObjectById(CPELocation.class, cpeLocationName);
		details.getDatatable().add(new Parameter("CPE_SUBSCRIBER_ID", subscriberRec.getId()));
		details.getDatatable().add(new Parameter("CPE_SUBSCRIBER_API_KEY", subscriberRec.getApiKey()));
		details.getDatatable().add(new Parameter("CPE_URL", cpeLocation.getUrl()));
		details.getDatatable().add(new Parameter("CPE_LOCATION_NAME", cpeLocation.getName()));
		details.getDatatable().add(new Parameter("CPE_CLUSTER_ID", clusterRec.getId()));
		
		String doShred = "Yes";
		final String accPropShredVal = (accountRec.getProperties() == null ? null : accountRec.getProperties().get(Account.ACCOUNT_PROPS_SHRED));
		
		if (String.valueOf("no").equalsIgnoreCase(accPropShredVal))
			doShred = "No";

		details.getDatatable().add(new Parameter(Account.ACCOUNT_PROPS_SHRED, doShred));

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Calling PCM-AE for provisioning now, details:\n%s", ReflectionToStringBuilder.toString(details)));
		}
			
		// call PCMAE
		final Message message = pcmaeGateway.getClusterApi(getPcmaeBackendId()).createCluster(details);
		
		if (logger.isDebugEnabled()) {
			logger.debug("result: " + message.getType());
			logger.debug("message: " + message.getMessage());
			logger.debug("Cluster id: " + message.getId());
		}
		
		if (message.getId() == null) {
			throw new CPEException("Failed to create cluster (name: " + details.getName() + ", def id: " + details.getClusterDefinition().getId() + ")");
		}
	
		return message.getId();
	}
	
	public List<Usage<? extends BillingItem>> getUsagesFromCluster(String clusterId) throws CPEException {
		final com.ibm.scas.analytics.persistence.beans.Cluster cluster = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Cluster.class, clusterId);
		if (cluster == null) {
			throw new CPEException(String.format("Cannot find cluster with id %s", clusterId));
		}
		
		final Account account = cluster.getOwner().getAccount();
		final Offering offeringRec = account.getOffering();
		final String edition = account.getEdition();
		final ServiceProvider plugin = pluginFactory.getPlugin(account.getOffering().getId());
		if (plugin == null) {
			throw new CPEException(String.format("Failed to obtain service provider plugin for offering: %s", offeringRec.getId()));
		}
		
		final List<com.ibm.scas.analytics.persistence.beans.Cluster> existingClusters = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Cluster.class, new WhereClause("owner.id", cluster.getOwner().getId()));
		
		// reconstruct the cluster request from the record
		final ClusterRequest req = this.getClusterReqFromRecord(cluster);
	
		final com.ibm.pcmae.cluster.beans.ClusterDetails details = new com.ibm.pcmae.cluster.beans.ClusterDetails();
		final List<Usage<? extends BillingItem>> usages = new ArrayList<Usage<? extends BillingItem>>();
		String location = req.getParameters().get("DATA_IN_LOCATION");

		//if data location null then check for sdfs location
		if(location == null) {
			String SecureHadoop = req.getParameters().get("USE_SDFS");
			if (SecureHadoop!=null && !SecureHadoop.trim().isEmpty()) {
				location = req.getParameters().get("SDFS_LOCATION");
			}
		}

		SoftLayerLocation slLocationRec = null;
		if (location != null) {
			slLocationRec = persistence.getObjectById(SoftLayerLocation.class, location);
		}

		SoftLayerLocation slLocation = null;
		if (slLocationRec != null) {
			slLocation = new SoftLayerLocation();
			slLocation.setName(slLocationRec.getName());
			slLocation.setPrivateUrl(slLocationRec.getPrivateUrl());
			slLocation.setPublicUrl(slLocationRec.getPublicUrl());
		}
		try {
			plugin.generateCreateClusterDetails(cluster.getOwner().getId(), edition, existingClusters, req, details, usages, slLocation, Locale.ENGLISH);
		} catch (ServiceProviderException e) {
			throw new CPEException(String.format("Failed to generate cluster details for cluster %s: %s", cluster.getId(), e.getMessage()));
		}
		
		return usages;
	}

	@Override
	public void cancelCluster(String clusterId) throws CPEException {
		final com.ibm.scas.analytics.persistence.beans.Cluster clusterRec = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Cluster.class, clusterId);
		if (clusterRec == null) {
			throw new CPEException(String.format("Cannot find cluster: %s", clusterId));
		}
		
		if (clusterRec.getClusterId() == null) {
			throw new CPEException(String.format("Cannot find PCM-AE cluster ID for cluster: %s", clusterId));
			
		}
		
		final ClusterAPI api = pcmaeGateway.getClusterApi(getPcmaeBackendId());
		final com.ibm.pcmae.cluster.beans.Cluster pcmaeDetails = api.getClusterDetails(clusterRec.getClusterId());
		
		if (pcmaeDetails == null) {
			throw new CPEException(String.format("Cannot find cluster ID %s in PCM-AE for cluster: %s", clusterId, clusterRec.getClusterId()));
		}
		
		logger.debug(String.format("Canceling cluster %s ...", clusterRec.getClusterId()));
		final Message message = api.cancelCluster(clusterRec.getClusterId());
		if (logger.isDebugEnabled()) {
			logger.debug("result: " + message.getType());
			logger.debug("message: " + message.getMessage());
			logger.debug("Cluster id: " + message.getId());
		}
		
		if (message.getType().equals(Message.ERROR)) {
			throw new CPEException("Failed to cancel cluster (name: " + pcmaeDetails.getName() + "): " + message.getMessage());
		}
		
	}
	
	@Override
	public void removeCluster(String clusterId) throws CPEException {
		final com.ibm.scas.analytics.persistence.beans.Cluster clusterRec = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Cluster.class, clusterId);
		if (clusterRec == null) {
			throw new CPEException(String.format("Cannot find cluster: %s", clusterId));
		}
		
		if (clusterRec.getClusterId() == null) {
			throw new CPEException(String.format("Cannot find PCM-AE cluster ID for cluster: %s", clusterId));
		}
		
		final ClusterAPI api = pcmaeGateway.getClusterApi(getPcmaeBackendId());
		final com.ibm.pcmae.cluster.beans.Cluster pcmaeDetails = api.getClusterDetails(clusterRec.getClusterId());
		
		if (pcmaeDetails == null) {
			throw new CPEException(String.format("Cannot find cluster ID %s in PCM-AE for cluster: %s", clusterId, clusterRec.getClusterId()));
		}
		
		logger.debug(String.format("Removing cluster %s ...", clusterRec.getClusterId()));
		final Message message = api.removeCluster(clusterRec.getClusterId());
		if (logger.isDebugEnabled()) {
			logger.debug("result: " + message.getType());
			logger.debug("message: " + message.getMessage());
			logger.debug("Cluster id: " + message.getId());
		}
		
		if (message.getType().equals(Message.ERROR)) {
			throw new CPEException("Failed to remove cluster (name: " + pcmaeDetails.getName() + "): " + message.getMessage());
		}
	}
	
	@Override
	public boolean addSoftlayerOrder(String subcriberId, String clusterId, SoftLayerOrder order) throws CPEException {
		    	if (logger.isTraceEnabled()) {
					logger.trace(String.format("%s.addSoftlayerOrder(): subscriber: %s cluster: %s" , this.getClass().getSimpleName(),subcriberId, clusterId));
				}

				final com.ibm.scas.analytics.persistence.beans.Cluster cluster = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Cluster.class, clusterId);
				if (cluster == null) {
					throw new ProvisioningException(String.format("%s.addSoftlayerOrder(). Cannot find cluster with ID %s.", this.getClass().getSimpleName(),clusterId));
				}
				
				final com.ibm.scas.analytics.persistence.beans.SoftLayerOrder slorder = new com.ibm.scas.analytics.persistence.beans.SoftLayerOrder();
								
				final SoftLayerAccount slaccount = persistence.getObjectById(SoftLayerAccount.class, order.getSoftLayerAccount().getId());
				if (slaccount == null) {
					throw new ProvisioningException(String.format("%s.addSoftlayerOrder(). Cannot find account with ID %s.", this.getClass().getSimpleName(),order.getSoftLayerAccount().getId()));
				}
				
				slorder.setSoftLayerId(order.getSoftLayerId());
				slorder.setCluster(cluster);
				slorder.setSoftLayerAccount(slaccount);
				// save these details 
				persistence.saveObject(com.ibm.scas.analytics.persistence.beans.SoftLayerOrder.class, slorder);
				return true;
	}

	@Override
	public void reloadHardwareOS(String clusterId) throws CPEException {
		final com.ibm.scas.analytics.persistence.beans.Cluster cluster = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Cluster.class, clusterId);
		if (cluster == null) {
			throw new ProvisioningException(String.format("%s.addSoftlayerOrder(). Cannot find cluster with ID %s.", this.getClass().getSimpleName(),clusterId));
		}

		String clusterType = cluster.getClusterParams().get(com.ibm.scas.analytics.persistence.beans.Cluster.CLUSTER_PROP_CLUSTERTYPE);

		if (com.ibm.scas.analytics.persistence.beans.Cluster.CLUSTERTYPE_DEDICATED_GATEWAY.equals(clusterType)) {
			String gatewayId = cluster.getClusterParams().get(com.ibm.scas.analytics.persistence.beans.Cluster.CLUSTER_PROP_GATEWAYID);
			if (gatewayId == null)
				throw new CPEException(String.format("Cannot reload OS for gateway because GatewayID property is missing in cluster params for cluster with id %s", clusterId));
			
			networkService.osReload(gatewayId);
		}
			
		cluster.setCurrentStep(ClusterStep.PROVISION_NODES.name());
		persistence.saveObject(com.ibm.scas.analytics.persistence.beans.Cluster.class, cluster);
		
		this.clusterBuilder.addClusterId(clusterId);
	}
		
}
