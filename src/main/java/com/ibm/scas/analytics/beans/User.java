package com.ibm.scas.analytics.beans;

import java.util.HashMap;

/**
 * POJO representing the User in AppDirect JSON
 * <pre>
{
    "openId": "https://ibmbluemix.appdirect.com/openid/id/1913c554-e326-44f9-943b-316de17dc4b3", 
    "uuid": "1913c554-e326-44f9-943b-316de17dc4b3", 
    "language": "en", 
    "lastName": "Chen", 
    "firstName": "Han", 
    "address": null, 
    "attributes": null, 
    "email": "captainsolo@me.com"
}, 
</pre>
 * @author Han Chen
 *
 */
public class User {
	private String openId;
	private String uuid;
	private String language;
	private String lastName;
	private String firstName;
	private Address address;
	private HashMap<String, String> attributes;
	private String email;
	
	public String getOpenId() {
		return openId;
	}
	public void setOpenId(String openId) {
		this.openId = openId;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public Address getAddress() {
		return address;
	}
	public void setAddress(Address address) {
		this.address = address;
	}
	public HashMap<String, String> getAttributes() {
		return attributes;
	}
	public void setAttributes(HashMap<String, String> attributes) {
		this.attributes = attributes;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getFullName() {
		return firstName + " " + lastName;
	}

	public String toString() {
		return firstName + " " + lastName + " (" + openId + ")";
	}
}
