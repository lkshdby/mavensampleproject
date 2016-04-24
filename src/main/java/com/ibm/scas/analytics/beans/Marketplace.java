package com.ibm.scas.analytics.beans;

/**
 * POJO representing the MarketPlace in AppDirect JSON
 * <pre>
{
  "partner": "ACME11", 
  "baseUrl": "https://ibmbluemix.appdirect.com"
} 
</pre>
 * @author Han Chen
 *
 */
public class Marketplace {
	private String partner;
	private String baseUrl;
	
	public String getPartner() {
		return partner;
	}
	public void setPartner(String partner) {
		this.partner = partner;
	}
	public String getBaseUrl() {
		return baseUrl;
	}
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
}
