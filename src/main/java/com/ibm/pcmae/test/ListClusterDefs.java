package com.ibm.pcmae.test;

import java.util.List;

import org.apache.log4j.Logger;

import com.ibm.pcmae.cluster.beans.ClusterDefinition;
import com.ibm.pcmae.cluster.beans.ClusterDefinitionDetails;
import com.ibm.pcmae.cluster.beans.ClusterDefinitionMachine;
import com.ibm.pcmae.cluster.beans.ClusterDefinitionTier;
import com.ibm.pcmae.cluster.beans.Quota;

public class ListClusterDefs extends APITestAdapter {
	private static final Logger logger = Logger.getLogger(ListClusterDefs.class);

	public ListClusterDefs() {
	}

	public void run() {
		List<ClusterDefinition> defs = api.listClusterDefinitions();
		logger.info(defs.size() + " cluster definitions found");
		for (ClusterDefinition def : defs) {
			String defId = def.getId();
			logger.info("---------------------------------------------------------");
			ClusterDefinitionDetails details = api.getClusterDefinitionDetails(defId);
			logger.info("Cluster definition details for " + defId);
			logger.info(" name: " + details.getName());
			logger.info(" description: " + details.getDescription());
			logger.info(" number of tiers: " + details.getTiers().size());
			for (ClusterDefinitionTier tier : details.getTiers()) {
				logger.info("  Tier: " + tier.getName());
				ClusterDefinitionMachine machine = tier.getMachineDefinitions().get(0);
				Quota machineQuota = machine.getQuotas().getMachine();
				Quota cpuQuota = machine.getQuotas().getCpu();
				Quota memoryQuota = machine.getQuotas().getMemory();
				logger.info("    machine: " + machineQuota.getMin() + " - " + machineQuota.getMax());
				logger.info("    CPU: " + cpuQuota.getMin() + " - " + cpuQuota.getMax());
				logger.info("    memory: " + memoryQuota.getMin() + " - " + memoryQuota.getMax());
			}

		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ListClusterDefs().run();

	}

}
