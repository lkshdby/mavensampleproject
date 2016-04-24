package com.ibm.pcmae.cluster.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ClusterScriptLayerExecution {
	private String hostname;
	private List<Parameter> output;
	
	public ClusterScriptLayerExecution() {
		
	}
	
	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public List<Parameter> getOutput() {
		return output;
	}

	public void setOutput(List<Parameter> output) {
		this.output = output;
	}
}
