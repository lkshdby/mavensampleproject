package com.ibm.scas.analytics.beans;

/**
 * POJO representing the Item in AppDirect JSON
 * 
 * <pre>
 * {
 *   "unit": "USER", 
 *   "quantity": 10
 * }
 * </pre>
 * 
 * @author Han Chen
 * 
 */
public class BillingItem {
	private String unit;
	private int quantity;

	public BillingItem(String unit, int quantity) {
		super();
		this.unit = unit;
		this.quantity = quantity;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}
