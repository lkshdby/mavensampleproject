package com.ibm.pcmae.cluster.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ClusterDetails extends Cluster {
	public static final String STATE_NEW = "_NEW"; // internal state used by the mock API
	public static final String STATE_ACTIVE = "ACTIVE";
	public static final String STATE_CANCELED = "CANCELED";
	
	public static final String ACTION_PROVISIONING = "Provision";
	public static final String ACTION_READY = "";
	
	public static final String ACTION_CANCEL = "_Cancel"; // internal state used by the mock API
	public static final String ACTION_CANCELING = "Canceling";

	private String creator;
	private String startDate;
	private String endDate;
	private String requestDate;
	private String state;
	private String applicationAction;
	private ReferenceObject ownerAccount;
	private ReferenceObject clusterDefinition;
	private List<ClusterTier> tiers;
	private String symphonyUrl;
	private List<Parameter> properties;
	private List<Parameter> datatable;
	private List<Parameter> deploymentVariables;

	public ClusterDetails() {
		super();
	}

	public ClusterDetails(String id, String name) {
		super(id, name);
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public ReferenceObject getClusterDefinition() {
		return clusterDefinition;
	}

	public void setClusterDefinition(ReferenceObject clusterDefinition) {
		this.clusterDefinition = clusterDefinition;
	}

	public List<ClusterTier> getTiers() {
		return tiers;
	}

	public void setTiers(List<ClusterTier> tiers) {
		this.tiers = tiers;
	}

	public String getSymphonyUrl() {
		return symphonyUrl;
	}

	public void setSymphonyUrl(String symphonyUrl) {
		this.symphonyUrl = symphonyUrl;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public ReferenceObject getOwnerAccount() {
		return ownerAccount;
	}

	public void setOwnerAccount(ReferenceObject account) {
		this.ownerAccount = account;
	}

	public List<Parameter> getProperties() {
		return properties;
	}

	public void setProperties(List<Parameter> properties) {
		this.properties = properties;
	}

	public List<Parameter> getDeploymentVariables() {
		return deploymentVariables;
	}

	public void setDeploymentVariables(List<Parameter> deploymentVariables) {
		this.deploymentVariables = deploymentVariables;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public List<Parameter> getDatatable() {
		return datatable;
	}

	public void setDatatable(List<Parameter> datatable) {
		this.datatable = datatable;
	}

	public String getRequestDate() {
		return requestDate;
	}

	public void setRequestDate(String requestDate) {
		this.requestDate = requestDate;
	}
	
	public String getApplicationAction() {
		return applicationAction;
	}

	public void setApplicationAction(String applicationAction) {
		this.applicationAction = applicationAction;
	}
	
}
