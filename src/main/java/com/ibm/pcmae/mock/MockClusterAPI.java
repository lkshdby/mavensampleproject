package com.ibm.pcmae.mock;

import static com.ibm.pcmae.cluster.beans.ClusterDetails.ACTION_CANCEL;
import static com.ibm.pcmae.cluster.beans.ClusterDetails.ACTION_CANCELING;
import static com.ibm.pcmae.cluster.beans.ClusterDetails.ACTION_PROVISIONING;
import static com.ibm.pcmae.cluster.beans.ClusterDetails.ACTION_READY;
import static com.ibm.pcmae.cluster.beans.ClusterDetails.STATE_ACTIVE;
import static com.ibm.pcmae.cluster.beans.ClusterDetails.STATE_CANCELED;
import static com.ibm.pcmae.cluster.beans.ClusterDetails.STATE_NEW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.ibm.pcmae.ClusterAPI;
import com.ibm.pcmae.cluster.beans.Cluster;
import com.ibm.pcmae.cluster.beans.ClusterDefinition;
import com.ibm.pcmae.cluster.beans.ClusterDefinitionDetails;
import com.ibm.pcmae.cluster.beans.ClusterDefinitionMachine;
import com.ibm.pcmae.cluster.beans.ClusterDefinitionTier;
import com.ibm.pcmae.cluster.beans.ClusterDetails;
import com.ibm.pcmae.cluster.beans.ClusterMachine;
import com.ibm.pcmae.cluster.beans.Message;
import com.ibm.pcmae.cluster.beans.Parameter;
import com.ibm.pcmae.cluster.beans.Quota;
import com.ibm.pcmae.cluster.beans.Quotas;

public class MockClusterAPI implements ClusterAPI {
	
	private static final Map<String, ClusterDefinitionDetails> allDefs = new HashMap<String, ClusterDefinitionDetails>();
	private static final Map<String, ClusterDetails> allClusters = new HashMap<String, ClusterDetails>();
	
	static {
		for (int i = 1; i <= 4; i ++) {
			String id = "00000000-00000000-0000-00000000-000" + i;
			String name = "Mock cluster definition (" + (5 * i) + " nodes)";
			ClusterDefinitionDetails def = new ClusterDefinitionDetails();
			def.setName(name);
			def.setId(id);
			def.setDescription(name);
			
			List<ClusterDefinitionTier> tiers = new ArrayList<ClusterDefinitionTier>();
			def.setTiers(tiers);
			
			ClusterDefinitionTier masterTier = new ClusterDefinitionTier("0000-master-0000", "Master", "master tier");
			ClusterDefinitionTier computeTier = new ClusterDefinitionTier("0000-compute-0000", "ComputeNodes", "compute tier");
			tiers.add(masterTier);
			tiers.add(computeTier);
			
			ClusterDefinitionMachine masterMachine = new ClusterDefinitionMachine("1111-master-1111", "Master", "master node");
			ClusterDefinitionMachine computeMachine = new ClusterDefinitionMachine("1111-compute-1111", "Compute Nodes", "compute nodes");
			masterTier.setMachineDefinitions(Arrays.asList(masterMachine));
			computeTier.setMachineDefinitions(Arrays.asList(computeMachine));
			
			Quotas masterQuotas = new Quotas();
			Quotas computeQuotas = new Quotas();
			masterMachine.setQuotas(masterQuotas);
			computeMachine.setQuotas(computeQuotas);
			
			masterQuotas.setMachine(new Quota(1, 1));
			masterQuotas.setCpu(new Quota(4, 4));
			masterQuotas.setStorage(new Quota(0, 0));
			masterQuotas.setMemory(new Quota(19456, 19456));
			
			computeQuotas.setMachine(new Quota(i * 5 - 1, i * 5 - 1));
			computeQuotas.setCpu(new Quota(4, 4));
			computeQuotas.setStorage(new Quota(0, 0));
			computeQuotas.setMemory(new Quota(19456, 19456));
			allDefs.put(id, def);
		}
	}
	
	@Override
	public void setCredentials(String account, String username, String password) {
	}

	@Override
	public List<ClusterDefinition> listClusterDefinitions() {
		synchronized(allDefs) {
			List<ClusterDefinition> defs = new ArrayList<ClusterDefinition>(allDefs.size());
			for (ClusterDefinitionDetails details : allDefs.values()) {
				defs.add(new ClusterDefinition(details));
			}
			return defs;
		}
	}

	@Override
	public ClusterDefinitionDetails getClusterDefinitionDetails(String id) {
		synchronized(allDefs) {
			return allDefs.get(id);
		}
	}

	@Override
	public List<Cluster> listClusters() {
		synchronized(allClusters) {
			List<Cluster> clusters = new ArrayList<Cluster>(allClusters.size());
			for (ClusterDetails details : allClusters.values()) {
				clusters.add(new Cluster(details));
			}
			return clusters;
		}
	}

	@Override
	public ClusterDetails getClusterDetails(String id) {
		synchronized(allClusters) {
			ClusterDetails details = allClusters.get(id);
			String state = details.getState();
			String action = details.getApplicationAction();
			if (STATE_NEW.equals(state)) {
				details.setState(STATE_ACTIVE);
				details.setApplicationAction(ACTION_PROVISIONING);
			} else if (STATE_ACTIVE.equals(state) && ACTION_PROVISIONING.equals(action)) {
				details.setApplicationAction(ACTION_READY);
			} else if (STATE_ACTIVE.equals(state) && ACTION_CANCEL.equals(action)) {
				details.setApplicationAction(ACTION_CANCELING);
			} else if (STATE_ACTIVE.equals(state) && ACTION_CANCELING.equals(action)) {
				details.setState(STATE_CANCELED);
				details.setApplicationAction(ACTION_READY);
			} 
			return details;
		}
	}

	@Override
	public List<Parameter> getClusterParameters(String id, String query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ClusterMachine> getClusterMachines(String id, String tierName) {
		// won't be needing this for now
		return Collections.emptyList();
	}

	@Override
	public Message createCluster(ClusterDetails details) {
		String id = UUID.randomUUID().toString();

		details.setId(id);
		details.setState(STATE_NEW);
		synchronized(allClusters) {
			allClusters.put(id, details);
		}
		return new Message(id, "clusters/" + id, "cluster created", Message.SUCCESS);
	}

	@Override
	public Message cancelCluster(String id) {
		synchronized(allClusters) {
			ClusterDetails details = allClusters.get(id);
			String state = details.getState();
//			String action = details.getApplicationAction();
			if (STATE_NEW.equals(state) || STATE_CANCELED.equals(state)) {
				return new Message("Wrong state", Message.ERROR);
			}
			
			details.setApplicationAction(ClusterDetails.ACTION_CANCEL);
			return new Message("OK", Message.SUCCESS);
		}
	}

	@Override
	public Message removeCluster(String id) {
		synchronized(allClusters) {
			ClusterDetails details = allClusters.get(id);
			String state = details.getState();
//			String action = details.getApplicationAction();
			if (STATE_NEW.equals(state) || STATE_ACTIVE.equals(state)) {
				return new Message("Wrong state", Message.ERROR);
			}
			allClusters.remove(id);
			return new Message("OK", Message.SUCCESS);
		}
	}

	@Override
	public Message flexUpCluster(String id, String tierName, int size) {
		return new Message("OK", Message.SUCCESS);
	}
}
