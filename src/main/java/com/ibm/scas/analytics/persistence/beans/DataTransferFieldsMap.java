package com.ibm.scas.analytics.persistence.beans;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.ibm.scas.analytics.persistence.beans.FormField;

@Entity
@Table(name="DATATRANSFERFIELDSMAP")
public class DataTransferFieldsMap {
	@Id
	private String id = null;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY, cascade={CascadeType.REMOVE})
	@JoinColumn(name="dataCenterId", nullable=true, updatable=true)
	private DataCenter dataCenter = null;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY, cascade={CascadeType.REMOVE})
	@JoinColumn(name="formFieldId", nullable=true, updatable=true)
	private FormField formField = null;
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
	public DataCenter getDataCenter() {
		return dataCenter;
	}
	
	public void setDataCenter(DataCenter dataCenter) {
		this.dataCenter = dataCenter;
	}
	
	public FormField getFormField() {
		return formField;
	}
	
	public void setFormFieldId(FormField formField) {
		this.formField = formField;
	}
}


