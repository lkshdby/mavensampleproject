package com.ibm.scas.analytics.backend;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.ibm.scas.analytics.beans.ClusterRequest;
import com.ibm.scas.analytics.beans.SoftLayerOrder;
import com.ibm.scas.analytics.beans.Usage;
import com.ibm.scas.analytics.beans.BillingItem;
import com.ibm.scas.analytics.beans.Cluster;
import com.ibm.scas.analytics.utils.CPEException;

/**
 * The abstract definition of a provisioning service
 * 
 * @author Han Chen
 *
 */
public interface ProvisioningService {
	
	/*
	 * admin view
	 */
	List<Cluster> listAllClusters() throws CPEException;
	List<Cluster> getClusters(Collection<String> clusterIds) throws CPEException;
	
	int getTotalCapacity() throws CPEException;
	int getAvailableCapacity() throws CPEException;
	int getCommittedCapacity() throws CPEException;
	
	/*
	 * user view
	 */
	boolean subscriberHasAccessToCluster(String subscriberId, String clusterId) throws CPEException, NotAuthorizedException;
	List<Cluster> listClusters(String subscriberId) throws CPEException;
	List<Cluster> listClustersInAccount(String subscriberId) throws CPEException;
	Cluster getClusterDetails(String clusterId) throws CPEException;
	String createCluster(String subscriberId, ClusterRequest cluster, Locale locale) throws CPEException;
	String provisionCluster(String clusterId) throws CPEException;
	boolean modifyCluster(String subscriberId, String clusterId, ClusterRequest cluster, Locale locale) throws CPEException, ProvisioningException;
	void cancelCluster(String clusterId) throws CPEException;
	void removeCluster(String clusterId) throws CPEException;
	void deleteCluster(String subscriberId, String clusterId, Locale locale) throws CPEException;
	void deleteAllClusters(String accountId) throws CPEException;
	List<Usage<? extends BillingItem>> getUsagesFromCluster(String clusterId) throws CPEException;
	String getPcmaeBackendId();

	// Cluster Order methods
	boolean addSoftlayerOrder(String subcriberId, String clusterId, SoftLayerOrder order) throws CPEException;
	void reloadHardwareOS(String clusterId) throws CPEException;

}
