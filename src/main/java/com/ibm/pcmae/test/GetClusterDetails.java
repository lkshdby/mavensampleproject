package com.ibm.pcmae.test;

import org.apache.log4j.Logger;

import com.ibm.pcmae.cluster.beans.ClusterDetails;
import com.ibm.pcmae.cluster.beans.ClusterMachineGroup;
import com.ibm.pcmae.cluster.beans.ClusterTier;
import com.ibm.pcmae.cluster.beans.Parameter;

public class GetClusterDetails extends APITestAdapter {
	private static final Logger logger = Logger.getLogger(GetClusterDetails.class);

	private String id = "2c9e8790-424e0880-0142-f7f513a4-4ffc";

	public GetClusterDetails() {
	}

	public void run1() {
		ClusterDetails details = api.getClusterDetails("abc");
		if (details == null) {
			logger.info("not found");
		}
	}

	public void run() {
		ClusterDetails details = api.getClusterDetails(id);
		logger.info("---------------------------------------------------------");
		String defId = details.getClusterDefinition().getId();
		String defName = details.getClusterDefinition().getName();
		logger.info("Cluster details for " + id);
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
		logger.info(" datatable:");
		for (Parameter p : details.getDatatable()) {
			String name = p.getName();
			String value = p.getValue();
			logger.info("  " + name + ": " + value);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new GetClusterDetails().run();

	}

}
