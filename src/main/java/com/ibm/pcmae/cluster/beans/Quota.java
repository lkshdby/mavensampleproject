package com.ibm.pcmae.cluster.beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Quota {
	private String id;
	private int max;
	private int min;

	public Quota() {

	}

	public Quota(int min, int max) {
		super();
		this.max = max;
		this.min = min;
	}

	public Quota(String id, int min, int max) {
		super();
		this.id = id;
		this.max = max;
		this.min = min;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}
}
