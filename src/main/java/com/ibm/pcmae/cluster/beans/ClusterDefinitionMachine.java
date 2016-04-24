package com.ibm.pcmae.cluster.beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ClusterDefinitionMachine {
	private String id;
	private String name;
	private String description;
	private Quotas quotas;

	public ClusterDefinitionMachine() {
		
	}
	
	public ClusterDefinitionMachine(String id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Quotas getQuotas() {
		return quotas;
	}

	public void setQuotas(Quotas quotas) {
		this.quotas = quotas;
	}
}
