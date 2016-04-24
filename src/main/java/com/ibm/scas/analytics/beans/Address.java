package com.ibm.scas.analytics.beans;

/**
 * POJO representing the Address object in AppDirect JSON
 * 
 *<pre>
{
  "city": "Somerville", 
  "fax": null, 
  "zip": "02411", 
  "phoneExtension": null, 
  "firstName": null, 
  "companyName": null, 
  "lastName": null, 
  "street2": null, 
  "pobox": null, 
  "faxExtension": null, 
  "phone": "5105551234", 
  "state": "MA", 
  "street1": "50 Main St", 
  "salutation": null, 
  "country": "US", 
  "pozip": null
}, 
 </pre>
 * 
 * @author Han Chen
 */
public class Address {
	private String salutation;
	private String firstName;
	private String lastName;
	private String companyName;
	private String phone;
	private String phoneExtension;
	private String fax;
	private String faxExtension;
	private String street1;
	private String street2;
	private String city;
	private String state;
	private String zip;
	private String country;
	private String pobox;
	private String pozip;
	
	public Address() {
	}

	public String getSalutation() {
		return salutation;
	}

	public void setSalutation(String salutation) {
		this.salutation = salutation;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPhoneExtension() {
		return phoneExtension;
	}

	public void setPhoneExtension(String phoneExtension) {
		this.phoneExtension = phoneExtension;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getFaxExtension() {
		return faxExtension;
	}

	public void setFaxExtension(String faxExtension) {
		this.faxExtension = faxExtension;
	}

	public String getStreet1() {
		return street1;
	}

	public void setStreet1(String street1) {
		this.street1 = street1;
	}

	public String getStreet2() {
		return street2;
	}

	public void setStreet2(String street2) {
		this.street2 = street2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getPobox() {
		return pobox;
	}

	public void setPobox(String pobox) {
		this.pobox = pobox;
	}

	public String getPozip() {
		return pozip;
	}

	public void setPozip(String pozip) {
		this.pozip = pozip;
	}

}
