package com.ibm.pcmae.cluster.beans;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Bean class that represents a layer in a tier of a given cluster 
 * 
 * @author Marcos Dias de Assuncao
 */

@XmlRootElement
public class ClusterTierLayer {
	private ReferenceObject definition;
	private String action;
	private String state;
	
	public ClusterTierLayer() { }
	
	public ClusterTierLayer(ReferenceObject definition) {
		this.definition = definition;
	}
	
	public ReferenceObject getDefinition() {
		return definition;
	}

	public void setDefinition(ReferenceObject definition) {
		this.definition = definition;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
}
