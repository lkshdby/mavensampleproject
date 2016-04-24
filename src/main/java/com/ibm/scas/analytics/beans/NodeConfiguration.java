package com.ibm.scas.analytics.beans;

public class NodeConfiguration {
	private String id = null;
	
	private String pluginId = null;
	
	private String nodeSize = null;
	private String specification = null;
	private String dataBandwidth = null;
	private String usedFor = null;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getPluginId() {
		return pluginId;
	}
	
	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}
	
	public String getNodeSize() {
		return nodeSize;
	}
	
	public void setNodeSize(String nodeSize) {
		this.nodeSize = nodeSize;
	}
	
	public String getSpecification() {
		return specification;
	}
	
	public void setSpecification(String specification) {
		this.specification = specification;
	}
	
	public String getDataBandwidth() {
		return dataBandwidth;
	}
	
	public void setDataBandwidth(String dataBandwidth) {
		this.dataBandwidth = dataBandwidth;
	}
	
	public String getUsedFor() {
		return usedFor;
	}
	
	public void setUsedFor(String usedFor) {
		this.usedFor = usedFor;
	}
	
}
