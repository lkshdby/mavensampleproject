package com.ibm.pcmae.test;

import java.util.List;

import org.apache.log4j.Logger;

import com.ibm.pcmae.cluster.beans.Cluster;
import com.ibm.pcmae.cluster.beans.ClusterDetails;
import com.ibm.pcmae.cluster.beans.ClusterMachineGroup;
import com.ibm.pcmae.cluster.beans.ClusterTier;

public class ListClusters extends APITestAdapter {
	private static final Logger logger = Logger.getLogger(ListClusters.class);

	public ListClusters() {
	}

	public void run1() {
		ClusterDetails details = api.getClusterDetails("abc");
		if (details == null) {
			logger.info("not found");
		}
	}
	public void run() {
		List<Cluster> clusters = api.listClusters();
		logger.info(clusters.size() + " clusters found");
		for (Cluster cluster : clusters) {
			String clusterId = cluster.getId();
			logger.info("---------------------------------------------------------");
			ClusterDetails details = api.getClusterDetails(clusterId);
			String defId = details.getClusterDefinition().getId();
			String defName = details.getClusterDefinition().getName();
			logger.info("Cluster details for " + clusterId);
			logger.info(" name: " + details.getName());
			logger.info(" description: " + details.getDescription());
			logger.info(" def id: " + defId);
			logger.info(" def name: " + defName);
			logger.info(" state: " + details.getState());
			logger.info(" application action: " + details.getApplicationAction());
			logger.info(" number of tiers: " + details.getTiers().size());
			for (ClusterTier tier : details.getTiers()) {
				logger.info("  Tier: " + tier.getName());
				ClusterMachineGroup machine = tier.getMachines().get(0);
				logger.info("    machine: " + machine.getNumberOfMachines());
				logger.info("    CPU: " + machine.getCpu());
				logger.info("    memory: " + machine.getMemory());
			}

		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ListClusters().run();

	}

}
