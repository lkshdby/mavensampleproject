package com.ibm.pcmae.cluster.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class User {

	private String name;
	private String role;
	private String email;
	private String businessUnit;
	private String department;
	private String location;
	private String password;
	private String action;
	private List<UserAccessRight> accounts;
	
	public User() {
	}
	
	public User(String name, String role) {
		super();
		this.name = name;
		this.role = role;
	}
		
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getBusinessUnit() {
		return businessUnit;
	}

	public void setBusinessUnit(String businessUnit) {
		this.businessUnit = businessUnit;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public List<UserAccessRight> getAccounts() {
		return accounts;
	}

	public void setAccounts(List<UserAccessRight> accounts) {
		this.accounts = accounts;
	}
}
