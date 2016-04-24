package com.ibm.pcmae.cluster.beans;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "account")
public class AccountBean {
	protected String id;
	protected String name;
	protected String description;
	protected String state;
	protected ReferenceObject parentAccount;
	protected List<User> users;
	protected Map<String, AccountQuotaBean> quotas;
	protected List<ReferenceObject> subAccounts; 

	public AccountBean() {
	}

	public AccountBean(String id, String name, String descr, String state, List<User> users) {
		super();
		this.id = id;
		this.name = name;
		this.description = descr;
		this.state = state;
		this.users = users;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String descr) {
		this.description = descr;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public Map<String, AccountQuotaBean> getQuotas() {
		return quotas;
	}

	public void setQuotas(Map<String, AccountQuotaBean> quotas) {
		this.quotas = quotas;
	}
	
	public ReferenceObject getParentAccount() {
		return parentAccount;
	}

	public void setParentAccount(ReferenceObject parentAccount) {
		this.parentAccount = parentAccount;
	}
	
	public List<ReferenceObject> getSubAccounts() {
		return subAccounts;
	}

	public void setSubAccounts(List<ReferenceObject> subAccounts) {
		this.subAccounts = subAccounts;
	}
}
