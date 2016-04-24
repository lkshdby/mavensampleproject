package com.ibm.scas.analytics.beans;

import java.util.List;

/**
 * POJO representing the metered usage event in AppDirect JSON
 * 
 * @author Han Chen
 * @see Account
 * @see BillingItem
 */
public class Usage<T extends BillingItem> {
	private Account account;
	private List<T> items;
	public Account getAccount() {
		return account;
	}
	public void setAccount(Account account) {
		this.account = account;
	}
	public List<T> getItems() {
		return items;
	}
	public void setItems(List<T> items) {
		this.items = items;
	}
}
