package com.ibm.scas.analytics.persistence.beans;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "DATACENTERS")
public class DataCenter {
	
	@Id
	@GeneratedValue(generator="system-uuid")
	private String id = null;
	private String name = null;
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void localize(Locale locale)
	{
		ResourceBundle rsrc = ResourceBundle.getBundle("cpe_messages", locale);
		name =  (String) getLocalizedValue(rsrc, "dataCenter." + id + ".name", name);
	}

	private Object getLocalizedValue(ResourceBundle rsrc, String resourceKey, Object originalFieldValue) {
		try {
			return rsrc.getString(resourceKey);
		} catch (MissingResourceException e) {
			return originalFieldValue;
		}
	}
}
