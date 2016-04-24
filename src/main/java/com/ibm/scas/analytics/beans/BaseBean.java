package com.ibm.scas.analytics.beans;

import org.codehaus.jackson.map.annotate.JsonFilter;

@JsonFilter("idFilter")
public class BaseBean {

	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	
}
