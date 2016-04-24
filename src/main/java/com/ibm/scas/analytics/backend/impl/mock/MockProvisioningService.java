package com.ibm.scas.analytics.backend.impl.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.ibm.scas.analytics.backend.ProvisioningException;
import com.ibm.scas.analytics.backend.ProvisioningService;
import com.ibm.scas.analytics.beans.BillingItem;
import com.ibm.scas.analytics.beans.ClusterRequest;
import com.ibm.scas.analytics.beans.SoftLayerOrder;
import com.ibm.scas.analytics.beans.Usage;
import com.ibm.scas.analytics.beans.utils.BeanConversionUtil;
import com.ibm.scas.analytics.persistence.PersistenceService;
import com.ibm.scas.analytics.persistence.beans.Cluster;
import com.ibm.scas.analytics.persistence.beans.SoftLayerAccount;
import com.ibm.scas.analytics.persistence.beans.Subscriber;
import com.ibm.scas.analytics.persistence.util.WhereClause;
import com.ibm.scas.analytics.persistence.util.WhereInClause;
import com.ibm.scas.analytics.utils.CPEException;

/**
 * A mock implementation of the provisioning service. No actual PCM-AE calls are
 * made
 * 
 * @author Han Chen
 * 
 */
public class MockProvisioningService implements ProvisioningService {
	private final static Logger logger = Logger.getLogger(MockProvisioningService.class);

	@Inject private PersistenceService persistence;
	
	@Override
	public boolean subscriberHasAccessToCluster(String subscriberId, String clusterId) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace("subscriberHasAccessToCluster(): subscriberId: " + subscriberId + ", clusterId: " + clusterId);
		}
		
		final List<com.ibm.scas.analytics.persistence.beans.Cluster> subscriberClusters = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Cluster.class, 
				new WhereClause("owner.id", subscriberId), new WhereClause("id", clusterId));
		
		return !subscriberClusters.isEmpty();
	}
	

	@Override
	public List<com.ibm.scas.analytics.beans.Cluster> listAllClusters() throws CPEException {
		logger.debug("listAllClusters called");
		final List<Cluster> allClusterRecs = persistence.getAllObjects(Cluster.class);
		
		final List<com.ibm.scas.analytics.beans.Cluster> toReturn = new ArrayList<com.ibm.scas.analytics.beans.Cluster>(allClusterRecs.size());
		for (final Cluster clusterRec : allClusterRecs) {
			toReturn.add(BeanConversionUtil.convertToBean(clusterRec));
		}
		
		return toReturn;
	}

	@Override
	public List<com.ibm.scas.analytics.beans.Cluster> getClusters(Collection<String> clusterIds) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("getClusters(): %s", clusterIds));
		}
		
		final List<com.ibm.scas.analytics.persistence.beans.Cluster> clusterRecs = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Cluster.class,
				new WhereInClause("id", clusterIds));
		
		if (clusterRecs.isEmpty()) {
			return Collections.emptyList();
		}
		
		final List<com.ibm.scas.analytics.beans.Cluster> clusters = new ArrayList<com.ibm.scas.analytics.beans.Cluster>(clusterRecs.size());
		for (final com.ibm.scas.analytics.persistence.beans.Cluster clusterRec : clusterRecs) {
			final com.ibm.scas.analytics.beans.Cluster cluster = BeanConversionUtil.convertToBean(clusterRec);
			
			clusters.add(cluster);
		}
		
		return clusters;
	}

	@Override
	public int getTotalCapacity() {
		// TODO Auto-generated method stub
		return 5;
	}

	@Override
	public int getAvailableCapacity() {
		return getTotalCapacity() - getCommittedCapacity();
	}

	@Override
	public int getCommittedCapacity() {
		return 0;
	}

	@Override
	public List<com.ibm.scas.analytics.beans.Cluster> listClusters(String subscriberId) throws CPEException {
		logger.debug("listClusters called");
		final List<Cluster> allClusterRecs = persistence.getObjectsBy(Cluster.class, new WhereClause("owner.id", subscriberId));
		final List<com.ibm.scas.analytics.beans.Cluster> toReturn = new ArrayList<com.ibm.scas.analytics.beans.Cluster>(allClusterRecs.size());
		for (final Cluster clusterRec : allClusterRecs) {
			toReturn.add(BeanConversionUtil.convertToBean(clusterRec));
		}
		
		return toReturn;
	
	}
	
	@Override
	public List<com.ibm.scas.analytics.beans.Cluster> listClustersInAccount(String subscriberId) throws CPEException {
		logger.debug("listClustersInAccount called");
		Subscriber subscriber = persistence.getObjectById(Subscriber.class, subscriberId);
		String accountId = subscriber.getAccount().getId();
		
		final List<Cluster> allClusterRecs = persistence.getObjectsBy(Cluster.class, new WhereClause("owner.account.id", accountId));
		final List<com.ibm.scas.analytics.beans.Cluster> toReturn = new ArrayList<com.ibm.scas.analytics.beans.Cluster>(allClusterRecs.size());
		for (final Cluster clusterRec : allClusterRecs) {
			toReturn.add(BeanConversionUtil.convertToBean(clusterRec));
		}
		return toReturn;
	
	}

	@Override
	public com.ibm.scas.analytics.beans.Cluster getClusterDetails(String clusterId) throws CPEException {
		logger.debug("getClusterDetails called");
		final com.ibm.scas.analytics.persistence.beans.Cluster clusterRec = persistence.getObjectById(Cluster.class, clusterId);
		if (clusterRec == null) {
			return null;
		}
		
		return BeanConversionUtil.convertToBean(clusterRec);
	}

	@Override
	public String createCluster(String subscriberId, ClusterRequest req, Locale locale) throws CPEException {
		logger.debug("createCluster called");

		final Cluster cluster = new Cluster();
		cluster.setName(req.getName());
		cluster.setDescription(req.getDescription());
		cluster.setSize(req.getSize());
		cluster.setLaunchTime(System.currentTimeMillis());
		
		final Subscriber subscriber = persistence.getObjectById(Subscriber.class, subscriberId);
		cluster.setOwner(subscriber);

		persistence.saveObject(Cluster.class, cluster);
		
		return cluster.getId();
	}

	@Override
	public void deleteCluster(String subscriberId, String clusterId, Locale locale) throws CPEException {
		logger.debug("deleteCluster called");
		persistence.deleteObjectById(Cluster.class, clusterId);
	}
	
	@Override
	public boolean modifyCluster(String subscriberId, String clusterId, ClusterRequest req, Locale locale) throws CPEException {
		logger.debug("modifyCluster called");
		
		final Cluster cluster = persistence.getObjectById(Cluster.class, clusterId);
		cluster.setName(req.getName());
		cluster.setDescription(req.getDescription());
		cluster.setSize(req.getSize());

		persistence.saveObject(Cluster.class, cluster);
		
		return true;
	}

	@Override
	public void deleteAllClusters(String accountId) throws CPEException {
		logger.debug("deleteAllClusters called");
		final List<Cluster> clusters = persistence.getObjectsBy(Cluster.class,  new WhereClause("owner.account.id", accountId));
		for (final Cluster cluster: clusters) {
			final String subscriberId = cluster.getOwner().getId();
			final String clusterId = cluster.getId();
			deleteCluster(subscriberId, clusterId, Locale.ENGLISH);
		}		
	}


	@Override
	public String provisionCluster(String clusterId) throws CPEException {
		// mockery 
		return null;
	}
	
	public List<Usage<? extends BillingItem>> getUsagesFromCluster(String clusterId) throws CPEException {
		final com.ibm.scas.analytics.persistence.beans.Cluster cluster = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Cluster.class, clusterId);
		if (cluster == null) {
			throw new CPEException(String.format("Cannot find cluster with id %s", clusterId));
		}
		

		return new ArrayList<Usage<? extends BillingItem>>();
	}


	@Override
	public String getPcmaeBackendId() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void cancelCluster(String clusterId) throws CPEException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void removeCluster(String clusterId) throws CPEException {
		persistence.deleteObjectById(Cluster.class, clusterId);
		
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
		// TODO Auto-generated method stub
		
	}
}
