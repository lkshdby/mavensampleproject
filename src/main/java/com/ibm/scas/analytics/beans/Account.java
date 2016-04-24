package com.ibm.scas.analytics.beans;

import java.util.Map;

/**
 * POJO representing the Account object in AppDirect JSON
 * <pre>
{
  "status": "ACTIVE", 
  "accountIdentifier": "dummy-account-id"
}
</pre>
 * @author Han Chen
 *
 */
public class Account extends BaseBean{
	public static final String UNKNOWN = "UNKNOWN";
	public static final String ACTIVE = "ACTIVE";
	public static final String FREE_TRIAL = "FREE_TRIAL";
	public static final String FREE_TRIAL_EXPIRED = "FREE_TRIAL_EXPIRED";
	public static final String SUSPENDED = "SUSPENDED";
	public static final String CANCELLED = "CANCELLED";
	
	private String accountIdentifier;
	private String status;
	
	private Offering offering;
	
	private String marketUrl = null;
	private String partner = null;

	private long expiration = -1;
	private int quantity = -1;
	
	private String edition;
	
	private Map<String, String> properties;			// for internal usage
	
	public String getAccountIdentifier() {
		return accountIdentifier;
	}
	public void setAccountIdentifier(String accountIdentifier) {
		this.accountIdentifier = accountIdentifier;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public Offering getOffering() {
		return offering;
	}
	public void setOffering(Offering offering) {
		this.offering = offering;
	}
	public String getMarketUrl() {
		return marketUrl;
	}
	public void setMarketUrl(String marketUrl) {
		this.marketUrl = marketUrl;
	}
	public String getPartner() {
		return partner;
	}
	public void setPartner(String partner) {
		this.partner = partner;
	}
	public long getExpiration() {
		return expiration;
	}
	public void setExpiration(long expiration) {
		this.expiration = expiration;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public String getEdition() {
		return edition;
	}
	public void setEdition(String edition) {
		this.edition = edition;
	}
	public Map<String, String> getProperties() {
		return properties;
	}
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	
	@Override
	public String getId() {
		return this.accountIdentifier;
	}
	
	@Override
	public void setId(String id) {
		this.accountIdentifier = id;
	}
}
