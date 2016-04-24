package com.ibm.pcmae.cluster.beans;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This bean class represents the rights a user
 * has within a given account. 
 * 
 * @author Marcos Dias de Assuncao
 */

@XmlRootElement
public class UserAccessRight {
	private String accountId;
	private String accountName;
	private String userRole;
	
	public UserAccessRight() { }
	
	public UserAccessRight(String accId, String accName, String userRole) { 
		this.accountId = accId;
		this.accountName = accName;
		this.userRole = userRole;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getUserRole() {
		return userRole;
	}

	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}
}
