package com.ibm.scas.analytics.persistence.beans;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@EntityListeners(SoftLayerAccount.SoftlayerAccountListener.class)
@Table(name="SOFTLAYERORDERS")
public class SoftLayerOrder {
	@Id
	@GeneratedValue(generator="system-uuid")
	private String id;

	private String softLayerId;
	
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "clusterId")
	private Cluster cluster;
	
	private String tierName;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="softLayerAccountId")
	private SoftLayerAccount softLayerAccount;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSoftLayerId() {
		return softLayerId;
	}

	public void setSoftLayerId(String softLayerId) {
		this.softLayerId = softLayerId;
	}

	public Cluster getCluster() {
		return cluster;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	public String getTierName() {
		return tierName;
	}

	public void setTierName(String tierName) {
		this.tierName = tierName;
	}

	public SoftLayerAccount getSoftLayerAccount() {
		return softLayerAccount;
	}

	public void setSoftLayerAccount(SoftLayerAccount softLayerAccount) {
		this.softLayerAccount = softLayerAccount;
	}	
}
