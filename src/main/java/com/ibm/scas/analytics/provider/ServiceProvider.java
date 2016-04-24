package com.ibm.scas.analytics.provider;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.ibm.pcmae.ClusterAPI;
import com.ibm.pcmae.cluster.beans.ClusterDetails;
import com.ibm.scas.analytics.beans.BillingItem;
import com.ibm.scas.analytics.beans.ClusterRequest;
import com.ibm.scas.analytics.persistence.beans.SoftLayerLocation;
import com.ibm.scas.analytics.beans.Usage;
import com.ibm.scas.analytics.persistence.beans.Cluster;

public interface ServiceProvider {
	/**
	 * init the plugin, the plugin should limit the use of the API to caching
	 * the cluster definitions that are needed for future processing, but no
	 * more.
	 * 
	 * Future revision may restrict the api parameter to a view on ClusterAPI
	 * 
	 * @param api
	 */
	void init(ClusterAPI api, Map<String, String> params) throws ServiceProviderException;

	/**
	 * Given the cluster creation request object, fill out the cluster detail,
	 * which will be used for PCM-AE, and a list of usage events, which will be
	 * submitted to AppDirect
	 * 
	 * @param subscriberId
	 * @param edition
	 * @param existingClusters a list of all existing clusters belonging to the subscriber
	 * @param request
	 * @param cluster
	 *            an empty cluster object, guaranteed to be not null
	 * @param usages
	 *            an empty list, guaranteed to be not null
	 * @throws ServiceProviderException
	 */
	void generateCreateClusterDetails(String subscriberId, String edition, List<Cluster> existingClusters, ClusterRequest request, ClusterDetails cluster, List<Usage<? extends BillingItem>> usages, SoftLayerLocation slLocation, Locale locale) throws ServiceProviderException;

	/**
	 * Given the cluster details retrieved from PCM-AE, fill out a list of
	 * name/value pairs that should be displayed on the details section of the
	 * dashboard
	 * 
	 * @param cluster
	 * @param attributes
	 *            an empty list, guaranteed to be not null
	 * @return
	 */
	boolean populateClusterDetails(ClusterDetails cluster, List<NameValuePair> attributes);
	
	/**
	 * Given the cluster object, add the cluster definition details 
	 * 
	 * @param cluster
	 * 
	 * @return
	 * @throws ServiceProviderException 
	 */
	void populateClusterDetails(com.ibm.scas.analytics.beans.Cluster cluster) throws ServiceProviderException;

	/**
	 * Returns the tier that flexes
	 * @return
	 */
	String getFlexTierName();
	
	/**
	 * Given the current total cluster size and requested new total cluster size, compute the new tier size for the flex tier
	 * @param edition
	 * @param currentSize
	 * @param newSize
	 * @return
	 */
	int computeFlexTierSize(String edition, int currentSize, int newSize);
	
	/**
	 * Generate usage events for the flex request
	 * 
	 * @param subscriberId
	 * @param currentSize
	 * @param newSize
	 * @param usages
	 */
	void generateFlexUsages(String subscriberId, int currentSize, int newSize, List<Usage<? extends BillingItem>> usages);
	
	/**
	 * return the node quota for a given edition, if any
	 * @param editionCode
	 * @return
	 */
	int getEditionQuota(String editionCode);
	
	/**
	 * return the canonical node size for a given edition
	 * @param editionCode
	 * @return
	 */
	int getEditionNodeSize(String editionCode);
	
	/**
	 * @return plugin version
	 */
	String getVersion();
	
	/**
	 * @return plugin build
	 */
	String getBuild();
	
	/**
	 * determines if a given edition is under access control, i.e., promo code
	 * @param editionCode
	 * @return
	 */
	boolean editionRequiresAccessCode(String editionCode);
}
