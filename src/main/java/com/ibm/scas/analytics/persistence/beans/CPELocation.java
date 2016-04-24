package com.ibm.scas.analytics.persistence.beans;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="CPELOCATIONS")
public class CPELocation {
	@Id
	private String name;
	private String url;
	
	@ManyToOne
	@JoinColumn(name="pcmaeBackendId")
	private PcmaeBackend pcmaeBackend;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setPcmaeBackend(PcmaeBackend pcmaeBackend) {
		this.pcmaeBackend = pcmaeBackend;
	}
	
	public PcmaeBackend getPcmaeBackend() {
		return pcmaeBackend;
	}
}
