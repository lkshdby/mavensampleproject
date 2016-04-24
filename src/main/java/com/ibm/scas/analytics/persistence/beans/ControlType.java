package com.ibm.scas.analytics.persistence.beans;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="CONTROLTYPES")
public class ControlType {

	@Id
	@GeneratedValue(generator="system-uuid")
	private Integer id = null;
	private String name = null;
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	
	public String getName() {
		return name;
	}
	
	
	public void setName(String name) {
		this.name = name;
	}
	
		
}
