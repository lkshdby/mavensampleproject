package com.ibm.scas.analytics.beans;

import org.codehaus.jackson.annotate.JsonIgnore;

public class GatewayMember {
	private String memberIp;
	private String username;
	private String password;
	private String sslCert;
	
	private int priority;
	
	public String getMemberIp() {
		return memberIp;
	}
	public void setMemberIp(String memberIp) {
		this.memberIp = memberIp;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	@JsonIgnore
	public String getSslCert() {
		return sslCert;
	}
	public void setSslCert(String sslCert) {
		this.sslCert = sslCert;
	}
}
