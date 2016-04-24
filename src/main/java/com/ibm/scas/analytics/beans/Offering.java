package com.ibm.scas.analytics.beans;

public class Offering extends BaseBean {
	
	private String oauthKey;
	private String oauthSecret;
	
	private String name;
	private boolean multiuser;

	public String getOauthKey() {
		return oauthKey;
	}
	public void setOauthKey(String oauthKey) {
		this.oauthKey = oauthKey;
	}
	public String getOauthSecret() {
		return oauthSecret;
	}
	public void setOauthSecret(String oauthSecret) {
		this.oauthSecret = oauthSecret;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public boolean isMultiuser() {
		return multiuser;
	}
	public void setMultiuser(boolean multiuser) {
		this.multiuser = multiuser;
	}
}
