package com.ibm.pcmae.cluster.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ClusterScriptLayer extends ClusterTierLayer {
	private List<Parameter> environment;
	private List<ClusterScriptLayerExecution> executions;
	
	public ClusterScriptLayer() { }
	
	public List<Parameter> getEnvironment() {
		return environment;
	}

	public void setEnvironment(List<Parameter> environment) {
		this.environment = environment;
	}

	public List<ClusterScriptLayerExecution> getExecutions() {
		return executions;
	}

	public void setExecutions(List<ClusterScriptLayerExecution> executions) {
		this.executions = executions;
	}
}
