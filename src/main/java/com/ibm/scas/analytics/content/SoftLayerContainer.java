package com.ibm.scas.analytics.content;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SoftLayerContainer {
	private Integer count = null;
	private Long bytes = null;
	private String name = null;
	
	public Integer getCount() {
		return count;
	}
	
	public void setCount(Integer count) {
		this.count = count;
	}
	
	public Long getBytes() {
		return bytes;
	}
	
	public void setBytes(Long bytes) {
		this.bytes = bytes;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
