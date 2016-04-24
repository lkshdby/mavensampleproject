package com.ibm.scas.analytics.persistence.mysql;

public class MySqlServiceConfig {
	private String name;
	private String label;
	private String plan;
	private MySqlCredentials credentials;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getPlan() {
		return plan;
	}
	public void setPlan(String plan) {
		this.plan = plan;
	}
	public MySqlCredentials getCredentials() {
		return credentials;
	}
	public void setCredentials(MySqlCredentials credentials) {
		this.credentials = credentials;
	}
}
