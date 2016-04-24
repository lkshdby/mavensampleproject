package com.ibm.pcmae.cluster.beans;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ClusterDefinitionDetails extends ClusterDefinition {

	private String framework;
	// the framework type, hadoop, mongoDB, etc, is implied by the cluster definition.
	// However, we may want to surface that information via the API. 
	// This is needed so that the modeling component can figure out, for a given 
	// framework type, what available cluster definitions can be used for optimization.
	
	private List<ClusterDefinitionTier> tiers;
	private Map<String,Parameter> properties;
	private List<Parameter> datatable;
	private List<Parameter> deploymentVariables;
	private List<ReferenceObject> publishingList; // accounts granted access to the definition

	public ClusterDefinitionDetails() {
		super();
	}
	
	public String getFramework() {
		return framework;
	}

	public void setFramework(String framework) {
		this.framework = framework;
	}

	public List<ClusterDefinitionTier> getTiers() {
		return tiers;
	}

	public void setTiers(List<ClusterDefinitionTier> tiers) {
		this.tiers = tiers;
	}

	public Map<String,Parameter> getProperties() {
		return properties;
	}

	public void setParameters(Map<String,Parameter> props) {
		this.properties = props;
	}

	public List<Parameter> getDatatable() {
		return datatable;
	}

	public void setDatatable(List<Parameter> datatable) {
		this.datatable = datatable;
	}

	public List<Parameter> getDeploymentVariables() {
		return deploymentVariables;
	}

	public void setDeploymentVariables(List<Parameter> deploymentVariables) {
		this.deploymentVariables = deploymentVariables;
	}

	public List<ReferenceObject> getPublishingList() {
		return publishingList;
	}

	public void setPublishingList(List<ReferenceObject> publishingList) {
		this.publishingList = publishingList;
	}

}
