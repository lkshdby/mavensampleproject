package com.ibm.pcmae.test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.ibm.pcmae.cluster.beans.ClusterDefinitionDetails;
import com.ibm.pcmae.cluster.beans.ClusterDefinitionMachine;
import com.ibm.pcmae.cluster.beans.ClusterDefinitionTier;
import com.ibm.pcmae.cluster.beans.ClusterDetails;
import com.ibm.pcmae.cluster.beans.ClusterMachineGroup;
import com.ibm.pcmae.cluster.beans.ClusterTier;
import com.ibm.pcmae.cluster.beans.Message;
import com.ibm.pcmae.cluster.beans.Parameter;
import com.ibm.pcmae.cluster.beans.Quota;
import com.ibm.pcmae.cluster.beans.ReferenceObject;

public class CreateCluster extends APITestAdapter {
	private static final Logger logger = Logger.getLogger(CreateCluster.class);

	private String clusterDefId = "2c9e8790-443807bb-0144-46f624df-2e4a";
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
	
	public CreateCluster() {
	}
	
	private ClusterDetails generateRequest(String defId) {
		ClusterDetails details = new ClusterDetails();

		details.setName("Test-" + System.currentTimeMillis() + ": Cluster created by API");
		details.setDescription("A test cluster created by the cluster provisioning engine");
		details.setClusterDefinition(new ReferenceObject(defId));
		
//		details.setStartDate("2013-12-01T17:00:00EDT");
//		details.setEndDate("2014-12-01T17:00:00EDT");
		Date startDate = new Date();
		Date endDate = new Date(startDate.getTime() + 86400000L * 30);
		details.setStartDate(df.format(startDate));
//		details.setEndDate(df.format(endDate));
//		details.setEndDate("No Expiry");

		
		logger.info("---------------------------------------------------------");
		ClusterDefinitionDetails defDetails = api.getClusterDefinitionDetails(defId);
		logger.info("Cluster definition details for " + defId);
		logger.info(" name: " + defDetails.getName());
		logger.info(" description: " + defDetails.getDescription());
		logger.info(" number of tiers: " + defDetails.getTiers().size());

		List<ClusterTier> tiers = new ArrayList<ClusterTier>(defDetails.getTiers().size());
		for (ClusterDefinitionTier defTier : defDetails.getTiers()) {
			logger.info("  Tier: " + defTier.getName());
			ClusterDefinitionMachine defMachines = defTier.getMachineDefinitions().get(0);
			Quota machineQuota = defMachines.getQuotas().getMachine();
			Quota cpuQuota = defMachines.getQuotas().getCpu();
			Quota memoryQuota = defMachines.getQuotas().getMemory();
			logger.info("    machine: " + machineQuota.getMin() + " - " + machineQuota.getMax());
			logger.info("    CPU: " + cpuQuota.getMin() + " - " + cpuQuota.getMax());
			logger.info("    memory: " + memoryQuota.getMin() + " - " + memoryQuota.getMax());
			
			ClusterTier tier = new ClusterTier();
			tier.setId(defTier.getId());
			tier.setName(defTier.getName());
			
			ClusterMachineGroup machines = new ClusterMachineGroup();
			machines.setDefinition(new ReferenceObject(defMachines.getId()));
			machines.setNumberOfMachines(machineQuota.getMin());
			machines.setCpu(cpuQuota.getMin());
			machines.setMemory(memoryQuota.getMin());
			
			tier.setMachines(Arrays.asList(machines));
			tiers.add(tier);
		}
//		"datatable" : [ {
//			"name" : "DATA_IN_USERNAME",
//			"value" : "IBMOS278059-1:ssotest1"
//			}, {
//			"name" : "DATA_IN_APIKEY",
//			"value" : "f91b005f21104e3e4c828ca0582367522b26f8823c70b0bfcbd41847fdce5bf0"
//			}, {
//			"name" : "DATA_IN_SOURCE",
//			"value" : "swift://pulse.softlayer/NASDAQ_prices_cleaned.csv"
//			}, {
//			"name" : "DATA_IN_DESTINATION",
//			"value" : "/Raw_Data"
//			} ]

		details.setDatatable(Arrays.asList(
//				new Parameter("DATA_IN_USERNAME", "IBMOS278059-1:ssotest1"),
//				new Parameter("DATA_IN_APIKEY", "f91b005f21104e3e4c828ca0582367522b26f8823c70b0bfcbd41847fdce5bf0"),
				new Parameter("DATA_IN_SOURCE", "swift://pulse.softlayer/NASDAQ_prices_cleaned.csv"),
				new Parameter("DATA_IN_DESTINATION", "/Raw_Data")
			));

		details.setTiers(tiers);

		return details;
	}

	public void run() {
		ClusterDetails details = generateRequest(clusterDefId);
		try {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Inclusion.NON_NULL);
		String json = mapper.writeValueAsString(details);
		logger.info("POST body: " + json);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Message message = api.createCluster(details);
		logger.info("result: " + message.getType());
		logger.info("message: " + message.getMessage());
		logger.info("Cluster id: " + message.getId());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		Logger.getRootLogger().setLevel(Level.TRACE);
//		System.out.println(CreateCluster.df.format(new Date()));
		new CreateCluster().run();
	}
}
