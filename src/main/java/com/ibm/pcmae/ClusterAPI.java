package com.ibm.pcmae;

import java.util.List;

import com.ibm.pcmae.cluster.beans.Cluster;
import com.ibm.pcmae.cluster.beans.ClusterDefinition;
import com.ibm.pcmae.cluster.beans.ClusterDefinitionDetails;
import com.ibm.pcmae.cluster.beans.ClusterDetails;
import com.ibm.pcmae.cluster.beans.ClusterMachine;
import com.ibm.pcmae.cluster.beans.Message;
import com.ibm.pcmae.cluster.beans.Parameter;

public interface ClusterAPI {
	/*
	 * credential management
	 */
	void setCredentials(String account, String username, String password);
	
	/*
	 * cluster definitions
	 */
	List<ClusterDefinition> listClusterDefinitions();
	ClusterDefinitionDetails getClusterDefinitionDetails(String id);
	
	/*
	 * cluster
	 */
	List<Cluster> listClusters();
	ClusterDetails getClusterDetails(String id);
	List<Parameter> getClusterParameters(String id, String query);
	List<ClusterMachine> getClusterMachines(String id, String tierName);
	
	Message createCluster(ClusterDetails details);
	
	Message cancelCluster(String id);
	Message removeCluster(String id);
	
	Message flexUpCluster(String id, String tierName, int size);
}
