package com.ibm.pcmae.cluster.beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Cluster {

	protected String id;
	protected String name;
	protected String version;
	protected String description;
	
	public Cluster() {
	}

	public Cluster(Cluster c) {
		this.id = c.id;
		this.name = c.name;
		this.version = c.version;
		this.description = c.description;
	}
	
	public Cluster(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


}
