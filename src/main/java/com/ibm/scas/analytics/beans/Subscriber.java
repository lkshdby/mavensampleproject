package com.ibm.scas.analytics.beans;

public class Subscriber extends BaseBean {
	public enum SubscriberType {
		APPDIRECT { @Override public String toString() { return "APPDIRECT"; } },
		CLOUDOE { @Override public String toString() { return "CLOUDOE"; } },
		CLOUDOE_PULSE_DEMO { @Override public String toString() { return "CLOUDOE_PULSE_DEMO"; } },
		SF { @Override public String toString() { return "SF"; } },
		APPDIRECT_ARCHIVED { @Override public String toString() { return "APPDIRECT_ARCHIVED"; } },
		DEV_TEST { @Override public String toString() { return "DEV_TEST"; } },
		GARBAGE_COLLECTOR { @Override public String toString() { return "GARBAGE_COLLECTOR"; } }, 
		SYSTEM { @Override public String toString() { return "SYSTEM"; } }, 
	}
	
	private String apiKey; // filled in by user

	private SubscriberType type;
	
	private Account account;
	
	private String externalId;
	private String name;
	

	public String getApiKey() {
		return apiKey;
	}
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public SubscriberType getType() {
		return type;
	}
	public void setType(SubscriberType type) {
		this.type = type;
	}

	public String getExternalId() {
		return externalId;
	}
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Account getAccount() {
		return account;
	}
	
	public void setAccount(Account account) {
		this.account = account;
	}
}
