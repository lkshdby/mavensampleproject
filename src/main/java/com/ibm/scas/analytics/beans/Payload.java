package com.ibm.scas.analytics.beans;

import java.util.HashMap;

/**
 * POJO representing the Payload in AppDirect JSON
 * <pre>
{
	"account": {...}, 
	"company": null, 
	"notice": null, 
	"addonInstance": null, 
	"addonBinding": null, 
	"user": {...}, 
	"configuration": {}, 
	"order": null
}
 </pre>
 * @author Han Chen
 * @see Account
 * @see Company
 * @see Notice
 * @see AddonBinding
 * @see AddonInstance
 * @see User
 * @see Order
 *
 */
public class Payload {
	private Account account;
	private Company company;
	private Notice notice;
	private AddonInstance addonInstance;
	private AddonBinding addonBinding;
	private User user;
	private HashMap<String, String> configuration;
	private Order order;
	
	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public Notice getNotice() {
		return notice;
	}

	public void setNotice(Notice notice) {
		this.notice = notice;
	}

	public AddonInstance getAddonInstance() {
		return addonInstance;
	}

	public void setAddonInstance(AddonInstance addonInstance) {
		this.addonInstance = addonInstance;
	}

	public AddonBinding getAddonBinding() {
		return addonBinding;
	}

	public void setAddonBinding(AddonBinding addonBinding) {
		this.addonBinding = addonBinding;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public HashMap<String, String> getConfiguration() {
		return configuration;
	}

	public void setConfiguration(HashMap<String, String> configuration) {
		this.configuration = configuration;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}
}
