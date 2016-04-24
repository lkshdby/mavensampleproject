package com.ibm.scas.analytics.persistence.beans;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="SOFTLAYERLOCATIONS")
public class SoftLayerLocation {
	
	@Id
	private String name;
	private String publicUrl;
	private String privateUrl;
	
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getPublicUrl() {
		return publicUrl;
	}
	
	public void setPublicUrl(String publicUrl) {
		this.publicUrl = publicUrl;
	}
	
	public String getPrivateUrl() {
		return privateUrl;
	}
	
	public void setPrivateUrl(String privateUrl) {
		this.privateUrl = privateUrl;
	}
}
