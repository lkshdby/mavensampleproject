package com.ibm.pcmae.cluster.beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AccountQuotaBean {
	private String description;
	private int effectiveQuota;
	private int quota;
	private int reserved;
	private int inUse;
	
	public AccountQuotaBean() {
		
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getEffectiveQuota() {
		return effectiveQuota;
	}

	public void setEffectiveQuota(int effectiveQuota) {
		this.effectiveQuota = effectiveQuota;
	}

	public int getQuota() {
		return quota;
	}

	public void setQuota(int quota) {
		this.quota = quota;
	}

	public int getReserved() {
		return reserved;
	}

	public void setReserved(int reserved) {
		this.reserved = reserved;
	}

	public int getInUse() {
		return inUse;
	}

	public void setInUse(int inUse) {
		this.inUse = inUse;
	}
}
