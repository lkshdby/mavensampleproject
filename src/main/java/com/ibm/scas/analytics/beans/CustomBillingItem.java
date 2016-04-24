package com.ibm.scas.analytics.beans;

/**
 * POJO representing the customer billing Item in AppDirect JSON
 * 
 * <pre>
 * {
 *   "quantity": 10
 *   "price": 0.15
 *   "description": "foo"
 * }
 * </pre>
 * 
 * @author Han Chen
 * 
 */
public class CustomBillingItem extends BillingItem {
	private double price;
	private String description;

	public CustomBillingItem(int quantity, double price, String description) {
		super(null, quantity);
		this.price = price;
		this.description = description;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
