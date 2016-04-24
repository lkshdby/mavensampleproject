package com.ibm.scas.analytics.provider.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.ibm.pcmae.ClusterAPI;
import com.ibm.pcmae.cluster.beans.ClusterDefinitionDetails;
import com.ibm.pcmae.cluster.beans.ClusterDefinitionMachine;
import com.ibm.pcmae.cluster.beans.ClusterDefinitionTier;
import com.ibm.pcmae.cluster.beans.ClusterDetails;
import com.ibm.pcmae.cluster.beans.Parameter;
import com.ibm.pcmae.cluster.beans.Quota;
import com.ibm.scas.analytics.beans.BillingItem;
import com.ibm.scas.analytics.beans.ClusterRequest;
import com.ibm.scas.analytics.persistence.beans.SoftLayerLocation;
import com.ibm.scas.analytics.beans.Usage;
import com.ibm.scas.analytics.provider.ServiceProvider;
import com.ibm.scas.analytics.provider.ServiceProviderException;

abstract public class ServiceProviderAdapter implements ServiceProvider {
	public static final String PLUGIN_PROPERTIES = "plugin.properties";
	public static final String PLUGIN_VERSION = "plugin.version";
	public static final String PLUGIN_BUILD = "plugin.build";
	public static final String UNKNOWN = "unknown";

	public static final String LARGE_EDITION_KEYWORD = "large";
	public static final int LARGE_NODE_SIZE = 5;
	public static final int DEFAULT_NODE_SIZE = 1;
	
	public static final String ACCESS_CONTROLLED_EDITIONS = "access.control";
	private Set<String> accessControlledEditions = new HashSet<String>();
	
	protected String computeTierName = null;
	
	private Properties props = null;
	
	@Override
	public void init(ClusterAPI api, Map<String, String> params) throws ServiceProviderException {
		String editionsList = getParameter(params, ACCESS_CONTROLLED_EDITIONS, null);
		if (editionsList == null) {
			return;
		}
		editionsList = editionsList.trim();
		if (editionsList.length() <= 0) {
			return;
		}
		
		String[] editions = editionsList.split(",");
		accessControlledEditions.addAll(Arrays.asList(editions));
	}

	@Override
	public String getFlexTierName() {
		return null;
	}

	@Override
	public int computeFlexTierSize(String edition, int currentSize, int newSize) {
		return 0;
	}

	@Override
	public void generateFlexUsages(String subscriberId, int currentSize, int newSize, List<Usage<? extends BillingItem>> usages) {
	}

	@Override
	public int getEditionQuota(String editionCode) {
		return 2;
	}

	abstract protected Logger getLogger();
	
	protected String getParameter(Map<String, String> params, String name, String defaultValue) {
		String value = params.get(name);
		if (value == null) {
			value = defaultValue;
		}
		return value;
	}

	protected int getParameter(Map<String, String> params, String name, int defaultValue) {
		String valueString = params.get(name);
		int value;
		if (valueString == null) {
			value = defaultValue;
		} else {
			try {
				value = Integer.parseInt(valueString);
			} catch (NumberFormatException e) {
				value = defaultValue;
			}
		}
		
		return value;
	}
	
	protected void logDefinitionDetails(ClusterDefinitionDetails defDetails) {
		Logger logger = getLogger();
		if (logger.isDebugEnabled()) {
			logger.debug("Cluster definition details for " + defDetails.getId());
			logger.debug(" name: " + defDetails.getName());
			logger.debug(" description: " + defDetails.getDescription());
			logger.debug(" number of tiers: " + defDetails.getTiers().size());

			for (ClusterDefinitionTier defTier : defDetails.getTiers()) {
				logger.debug("  Tier: " + defTier.getName());
				ClusterDefinitionMachine defMachines = defTier.getMachineDefinitions().get(0);
				Quota machineQuota = defMachines.getQuotas().getMachine();
				Quota cpuQuota = defMachines.getQuotas().getCpu();
				Quota memoryQuota = defMachines.getQuotas().getMemory();
				logger.debug("    machine: " + machineQuota.getMin() + " - " + machineQuota.getMax());
				logger.debug("    CPU: " + cpuQuota.getMin() + " - " + cpuQuota.getMax());
				logger.debug("    memory: " + memoryQuota.getMin() + " - " + memoryQuota.getMax());
			}
		}
	}
	
	/**
	 * Check if a parameter is specified in the input cluster request, if so, add it to the datatable.
	 * @param datatable
	 * @param request
	 * @param name
	 */
	protected void processRequestParameter(List<Parameter> datatable, ClusterRequest request, String name) {
		Logger logger = getLogger();
		String value = request.getParameters().get(name);
		if (value != null && value.length() > 0) {
			if (logger.isDebugEnabled()) {
				logger.debug(" - " + name + ": " + value);
			}
			datatable.add(new Parameter(name, value));
		} else {
			logger.warn(" - " + name + " not found!");
		}
	}
	
	
	protected void processDataInSource(List<Parameter> datatable, ClusterRequest request)
	{
		Logger logger = getLogger();
		String container = request.getParameters().get("DATA_IN_CONTAINER");
		String path = request.getParameters().get("DATA_IN_PATH");
		
		String dataInSource = "swift://" + container + ".softlayer/"+ path;
		logger.debug("DATA_IN_SOURCE : " + dataInSource);
		datatable.add(new Parameter("DATA_IN_SOURCE", dataInSource));
	}
	
	protected void setDataAutomationParameters(ClusterDetails details, ClusterRequest request, SoftLayerLocation slLocation)
	{
		Logger logger = getLogger();
		List<Parameter> datatable = new ArrayList<Parameter>();
		if(request.getParameters().get("DATA_IN_USERNAME") != null && request.getParameters().get("DATA_IN_APIKEY") != null)
		{
			logger.info("Setting data table...");
			
			processRequestParameter(datatable, request, "DATA_IN_USERNAME");
			processRequestParameter(datatable, request, "DATA_IN_APIKEY");
			processDataInSource(datatable, request);
			datatable.add(new Parameter("SWIFT_AUTH_ENDPOINT", slLocation.getPrivateUrl()));
			processRequestParameter(datatable, request, "DATA_IN_DESTINATION");
		}
		details.setDatatable(datatable);
	}
	
	protected String getInternalEditionCode(String edition) {
		if (edition == null) {
			return edition;
		}
		
		int index = edition.indexOf(" - ");
		if (index < 0) {
			return edition;
		} else {
			String internalEdition = edition.substring(index + 3);
			return internalEdition.trim();
		}
	}

	private Properties getProperties() {
		if (props == null) {
			props = new Properties();
			try {
				InputStream is = this.getClass().getClassLoader().getResourceAsStream(PLUGIN_PROPERTIES);
				props.load(is);
			} catch(Exception e) {
				getLogger().error("Failed to load plugin.properties!", e);
			}
		} 
		return props;
	}
	
	/**
	 * This is meant to be a heuristic. Plug-ins that use different node size and/or code scheming for edition should override this method
	 * @param editionCode
	 * @return node size
	 */
	@Override
	public int getEditionNodeSize(String editionCode) {
		return getInternalEditionCode(editionCode).indexOf(LARGE_EDITION_KEYWORD) >= 0 ? LARGE_NODE_SIZE : DEFAULT_NODE_SIZE;
	}

	@Override
	public String getVersion() {
		return getProperties().getProperty(PLUGIN_VERSION, UNKNOWN);
	}

	@Override
	public String getBuild() {
		return getProperties().getProperty(PLUGIN_BUILD, UNKNOWN);
	}

	@Override
	public boolean editionRequiresAccessCode(String editionCode) {
		return accessControlledEditions.contains(getInternalEditionCode(editionCode)) || accessControlledEditions.contains(editionCode);
	}
	
	protected abstract ClusterDefinitionDetails getClusterDefinitionForEdition(String edition);
	
	@Override
	public void populateClusterDetails(com.ibm.scas.analytics.beans.Cluster cluster) throws ServiceProviderException {
		final ClusterDefinitionDetails defDetails = getClusterDefinitionForEdition(cluster.getOwner().getAccount().getEdition());
		
		if (defDetails == null) {
			throw new ServiceProviderException(String.format("Unable to find cluster definition for edition: %s", cluster.getOwner().getAccount().getEdition()));
		}

//		boolean dataMasking = "yes".equalsIgnoreCase(request.getParameters().get("dataMasking"));
//		boolean haNameNode = "yes".equalsIgnoreCase(request.getParameters().get("haNameNode"));
		
		final StringBuilder publicIPTiers = new StringBuilder();
		com.ibm.scas.analytics.beans.ClusterTier computeTier = null;
		int machineCount = 0;
		
		final List<com.ibm.scas.analytics.beans.ClusterTier> tiers = new ArrayList<com.ibm.scas.analytics.beans.ClusterTier>(defDetails.getTiers().size());
		for (final ClusterDefinitionTier defTier : defDetails.getTiers()) {
			final ClusterDefinitionMachine defMachines = defTier.getMachineDefinitions().get(0);
			final Quota machineQuota = defMachines.getQuotas().getMachine();
			final Quota cpuQuota = defMachines.getQuotas().getCpu();
			final Quota memoryQuota = defMachines.getQuotas().getMemory();

			final com.ibm.scas.analytics.beans.ClusterTier tier = new com.ibm.scas.analytics.beans.ClusterTier();
			tier.setName(defTier.getName());
			
			if (tier.getName().equals(computeTierName)) {
				computeTier = tier;
			}

			final com.ibm.scas.analytics.beans.ClusterMachineGroup machines = new com.ibm.scas.analytics.beans.ClusterMachineGroup();
			machines.setNumberOfMachines(machineQuota.getMin());
			machines.setCpu(cpuQuota.getMin());
			machines.setMemory(memoryQuota.getMin());
			machineCount += machines.getNumberOfMachines();
			
			// master tier goes on the public internet
			// TODO: set this to false if dedicated gateway is ordered
			if (computeTierName != null && !computeTierName.equals(tier.getName())) {
				if (publicIPTiers.length() == 0) {
					publicIPTiers.append(tier.getName());
				} else {
					publicIPTiers.append(";").append(tier.getName());
				}
			}
		
			tier.setMachineGroup(machines);
			tiers.add(tier);
		}
		cluster.setClusterTiers(tiers);
		
		cluster.getProperties().put("PUBLIC_IP_TIERS", publicIPTiers.toString());

		if (computeTier != null) {
			// adjust compute tier size to reach the total cluster size
			int delta = cluster.getSize() - machineCount;
			if (delta >= 0) {
				com.ibm.scas.analytics.beans.ClusterMachineGroup machines = computeTier.getMachineGroup();
				int currentComputeSize = machines.getNumberOfMachines();
				int newComputeSize = currentComputeSize + delta;
				machines.setNumberOfMachines(newComputeSize);
			}
		}
	}
}
