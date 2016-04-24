package com.ibm.scas.analytics.beans;

import java.util.Map;

/**
 * POJO representing the create cluster request coming from the dashboard UI
 * 
 * @author Han Chen
 *
 */
public class ClusterRequest {
	private String name;
	private String description;
	private int size;
//	private long launchTime;
	private Map<String, String> parameters;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
//	public long getLaunchTime() {
//		return launchTime;
//	}
//	public void setLaunchTime(long launchTime) {
//		this.launchTime = launchTime;
//	}
	public Map<String, String> getParameters() {
		return parameters;
	}
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
}
