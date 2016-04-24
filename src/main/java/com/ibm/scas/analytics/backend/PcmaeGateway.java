package com.ibm.scas.analytics.backend;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ibm.pcmae.ClusterAPI;
import com.ibm.pcmae.client.PassthruClusterAPI;
import com.ibm.pcmae.mock.MockClusterAPI;
import com.ibm.scas.analytics.persistence.PersistenceException;
import com.ibm.scas.analytics.persistence.PersistenceService;
import com.ibm.scas.analytics.persistence.beans.PcmaeBackend;

/**
 * this gateway handles the credentials loading from properties
 * 
 * @author Han Chen
 *
 */
@Singleton 
public class PcmaeGateway {
	private static final Logger logger = Logger.getLogger(PcmaeGateway.class);
	
	public static final String MOCK_CLUSTER_API_URL = "mock";
	
	@Inject private PersistenceService persistence;
	
	public ClusterAPI getClusterApi(String backendId) {
		String clusterApiUrl;

		PcmaeBackend backend = null;
		try {
			backend = persistence.getObjectById(PcmaeBackend.class, backendId);
		} catch (PersistenceException e) {
			logger.error(e);
		}
		
		if (backend == null) {
			logger.error("Backend '" + backendId + "' is not configured! Using mock cluster API. ");
			clusterApiUrl = MOCK_CLUSTER_API_URL;
		} else {
			clusterApiUrl = backend.getUrl();
		}
		
		ClusterAPI api;
		if (MOCK_CLUSTER_API_URL.equals(clusterApiUrl)) {
			api = new MockClusterAPI();
		} else {
			api = new PassthruClusterAPI(clusterApiUrl);
		}
		api.setCredentials(backend.getAccount(), backend.getUsername(), backend.getPassword());
		
		return api;
	}
}
