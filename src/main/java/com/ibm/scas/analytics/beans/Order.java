package com.ibm.scas.analytics.beans;

import java.util.List;

/**
 * POJO representing the Order in AppDirect JSON
 * <pre>
{
  "pricingDuration": "MONTHLY", 
  "maxUsers": 10, 
  "addonOfferingCode": null, 
  "editionCode": "BASIC", 
  "items": [
    {
      "unit": "USER", 
      "quantity": 10
    }, 
    {
      "unit": "MEGABYTE", 
      "quantity": 15
    }
  ]
}
 </pre>
 * @author Han Chen
 * @see BillingItem
 *
 */
public class Order {
	private String editionCode;
	private String addonOfferingCode;
	private String pricingDuration;
	private int maxUsers; 
	private List<BillingItem> items; 
	public String getEditionCode() {
		return editionCode;
	}
	public void setEditionCode(String editionCode) {
		this.editionCode = editionCode;
	}
	public String getAddonOfferingCode() {
		return addonOfferingCode;
	}
	public void setAddonOfferingCode(String addonOfferingCode) {
		this.addonOfferingCode = addonOfferingCode;
	}
	public String getPricingDuration() {
		return pricingDuration;
	}
	public void setPricingDuration(String pricingDuration) {
		this.pricingDuration = pricingDuration;
	}
	public int getMaxUsers() {
		return maxUsers;
	}
	public void setMaxUsers(int maxUsers) {
		this.maxUsers = maxUsers;
	}
	public List<BillingItem> getItems() {
		return items;
	}
	public void setItems(List<BillingItem> items) {
		this.items = items;
	}
}
