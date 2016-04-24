package com.ibm.scas.analytics.beans;

/**
 * POJO representing the Notice in AppDirect JSON
 * <pre>
{
  "message": null, 
  "type": "DEACTIVATED"
}
 </pre>
 * @author Han Chen
 *
 */
public class Notice {
	private String type;
	private String message;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
