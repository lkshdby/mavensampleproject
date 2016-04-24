package com.ibm.scas.analytics.beans;

public class FormField {
	
	private Integer stepId = null;
	private Integer stepNo = null;
	private String name = null;
	private String label = null;
	private String type = null;
	private Integer orderIndex = null;
	private String value = null;
	private String attachedRESTEvent = null; 
	private String description = null;
	private String helpDescription = null;
	private String defaultValue = null;
	private Integer isMandetory = null;
	private Integer isHelpEnabled = null;
	private Integer maximumValue = null;
	private Integer minimumValue = null;
	
	public Integer getStepId() {
		return stepId;
	}
	
	public void setStepId(Integer stepId) {
		this.stepId = stepId;
	}
	
	public Integer getStepNo() {
		return stepNo;
	}
	
	public void setStepNo(Integer stepNo) {
		this.stepNo = stepNo;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public Integer getOrderIndex() {
		return orderIndex;
	}
	
	public void setOrderIndex(Integer orderIndex) {
		this.orderIndex = orderIndex;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getAttachedRESTEvent() {
		return attachedRESTEvent;
	}
	
	public void setAttachedRESTEvent(String attachedRESTEvent) {
		this.attachedRESTEvent = attachedRESTEvent;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDefaultValue() {
		return defaultValue;
	}
	
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public Integer getIsMandetory() {
		return isMandetory;
	}
	
	public void setIsMandetory(Integer isMandetory) {
		this.isMandetory = isMandetory;
	}
	
	public Integer getIsHelpEnabled() {
		return isHelpEnabled;
	}
	
	public void setIsHelpEnabled(Integer isHelpEnabled) {
		this.isHelpEnabled = isHelpEnabled;
	}
	
	public String getHelpDescription() {
		return helpDescription;
	}
	
	public void setHelpDescription(String helpDescription) {
		this.helpDescription = helpDescription;
	}

	public Integer getMaximumValue() {
		return maximumValue;
	}

	public void setMaximumValue(Integer maximumValue) {
		this.maximumValue = maximumValue;
	}

	public Integer getMinimumValue() {
		return minimumValue;
	}

	public void setMinimumValue(Integer minimumValue) {
		this.minimumValue = minimumValue;
	}
}
