package com.ibm.scas.analytics.beans;

/**
 * POJO representing the Company in AppDirect JSON
 * <pre>
{
  "website": "us.ibm.com", 
  "uuid": "a05aac3e-ef76-47ef-9a3e-7697cb76e679", 
  "country": "US", 
  "phoneNumber": "914 426 1860", 
  "email": null, 
  "name": "IBM"
} 
 </pre>
 * @author Han Chen
 *
 */
public class Company {
	private String name;
	private String uuid;
	private String country;
	private String phoneNumber;
	private String website;
	private String email;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getWebsite() {
		return website;
	}
	public void setWebsite(String website) {
		this.website = website;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
}
