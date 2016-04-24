package com.ibm.scas.analytics.persistence.beans;

/**
 * 
 * POJO representing the enhanced account details used in the admin view
 *
 * @author Han Chen
 *
 */
public class AllClusterDetails extends Cluster {
	private String offeringId;
	private String offeringName;
	private int accountId;
	private String ownerName;
	
	public String getOfferingId() {
		return offeringId;
	}
	public void setOfferingId(String offeringId) {
		this.offeringId = offeringId;
	}
	public String getOfferingName() {
		return offeringName;
	}
	public void setOfferingName(String offeringName) {
		this.offeringName = offeringName;
	}
	public int getAccountId() {
		return accountId;
	}
	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}
	public String getOwnerName() {
		return ownerName;
	}
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}
	
}
