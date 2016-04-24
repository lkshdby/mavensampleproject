package com.ibm.scas.analytics.beans;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * POJO representing the Notification in AppDirect JSON
 * <pre>
{
  "marketplace": {...}, 
  "applicationUuid": null, 
  "creator": {...}, 
  "flag": "DEVELOPMENT", 
  "returnUrl": null, 
  "type": "SUBSCRIPTION_ORDER", 
  "payload": {...}
}
</pre>
 * @author chenhan
 * @see User
 * @see Marketplace
 * @see Payload
 *
 */
public class NotificationEvent {
	private String type;
	private String applicationUuid;
	private String flag;
	private String returnUrl;
	private User creator;
	private Marketplace marketplace;
	private Payload payload;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getApplicationUuid() {
		return applicationUuid;
	}
	public void setApplicationUuid(String applicationUuid) {
		this.applicationUuid = applicationUuid;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	public String getReturnUrl() {
		return returnUrl;
	}
	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}
	public User getCreator() {
		return creator;
	}
	public void setCreator(User creator) {
		this.creator = creator;
	}
	
	public Marketplace getMarketplace() {
		return marketplace;
	}
	public void setMarketplace(Marketplace marketplace) {
		this.marketplace = marketplace;
	}
	public Payload getPayload() {
		return payload;
	}
	public void setPayload(Payload payload) {
		this.payload = payload;
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		Gson gson = new Gson();
		FileInputStream is = new FileInputStream("/Users/chenhan/Desktop/order.json");
		InputStreamReader reader = new InputStreamReader(is);
		NotificationEvent event = gson.fromJson(reader, NotificationEvent.class);
		String json = new GsonBuilder().setPrettyPrinting().create().toJson(event);
		System.out.println("Event:\n" + json);
	}
}
