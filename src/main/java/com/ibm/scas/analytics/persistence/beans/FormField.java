package com.ibm.scas.analytics.persistence.beans;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name="FORMFIELDS")
public class FormField {
	@Id
	@GeneratedValue(generator="system-uuid")
	private String id = null;
	private String stepId = null;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY, cascade={CascadeType.REMOVE})
	@JoinColumn(name="stepId", nullable=false, updatable=false, insertable = false)
	private StepDetail stepDetail = null;
	
	private String name = null;
	private String label = null;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY, cascade={CascadeType.REMOVE})
	@JoinColumn(name="typeId", nullable=false, updatable=true)
	private ControlType controlType = null;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY, cascade={CascadeType.REMOVE})
	@JoinColumn(name="id", nullable=false, updatable=false, insertable = false, referencedColumnName = "formFieldId")
	private DatacenterFieldsMap dataCenterFields = null;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY, cascade={CascadeType.REMOVE})
	@JoinColumn(name="id", nullable=false, updatable=false, insertable = false, referencedColumnName = "formFieldId")
	private DataTransferFieldsMap dataTransferFields = null;
	
	@Transient
	private String type = null;
	private Integer orderIndex = null;
	private String value = null;
	private String attachedRESTEvent = null; 
	private String description = null;
	private String helpDescription = null;
	private String defaultValue = null;
	private Integer isMandetory = null;
	private Integer isEnabled = null;
	private Integer isOnDemand = null;
	private Integer isHelpEnabled = null;
	private Integer maximumValue = null;
	private Integer minimumValue = null;
	
	public String getStepId() {
		return stepDetail.getId();
	}
	
	public void setStepId(String stepId) {
		this.stepId = stepId;
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
	
	public ControlType getControlType() {
		return controlType;
	}
	
	public void setControlType(ControlType controlType) {
		this.controlType = controlType;
	}
	
	public String getType() {
		return controlType.getName();
	}
	
	public void setStepDetail(StepDetail stepDetail) {
		this.stepDetail = stepDetail;
	}
	
	public StepDetail getStepDetail() {
		return stepDetail;
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
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public Integer getIsEnabled() {
		return isEnabled;
	}
	
	public void setIsEnabled(Integer isEnabled) {
		this.isEnabled = isEnabled;
	}
	
	public Integer getIsOnDemand() {
		return isOnDemand;
	}
	
	public void setIsOnDemand(Integer isOnDemand) {
		this.isOnDemand = isOnDemand;
	}
	
	public String getHelpDescription() {
		return helpDescription;
	}
	
	public void setHelpDescription(String helpDescription) {
		this.helpDescription = helpDescription;
	}
	
	public Integer getIsHelpEnabled() {
		return isHelpEnabled;
	}
	
	public void setIsHelpEnabled(Integer isHelpEnabled) {
		this.isHelpEnabled = isHelpEnabled;
	}
	
	public void localize(Locale locale)
	{
		ResourceBundle rsrc = ResourceBundle.getBundle("cpe_messages", locale);
		String offeringSpecificPreKeyText = stepDetail.getPluginId() + "." + name;
		String generalPreKeyText = "hdp." + name;
		//handle transfer data fields
		if(stepDetail.getStepNumber() == 0)
		{
			offeringSpecificPreKeyText = "TRANSFER.FROM." + offeringSpecificPreKeyText ;
			generalPreKeyText = "TRANSFER.FROM." + generalPreKeyText ;
		}
		else if(stepDetail.getStepNumber() == -1)
		{
			offeringSpecificPreKeyText = "TRANSFER.TO." + offeringSpecificPreKeyText ;
			generalPreKeyText = "TRANSFER.TO." + generalPreKeyText ;
		}
		
		label =  (String) getLocalizedValue(rsrc, offeringSpecificPreKeyText, generalPreKeyText, label);
		description =  (String) getLocalizedValue(rsrc, offeringSpecificPreKeyText + ".description", generalPreKeyText + ".description",description);
		helpDescription =  (String) getLocalizedValue(rsrc, offeringSpecificPreKeyText + ".helpDescription", generalPreKeyText + ".helpDescription",helpDescription);
		value = (String) getLocalizedValue(rsrc, offeringSpecificPreKeyText + ".value", generalPreKeyText + ".value",value);
		defaultValue = (String) getLocalizedValue(rsrc, offeringSpecificPreKeyText + ".defaultValue", generalPreKeyText + ".defaultValue",defaultValue);
	}

	private Object getLocalizedValue(ResourceBundle rsrc, String offeringKey, String generalKey, Object originalFieldValue) {
		try {
			return rsrc.getString(offeringKey);
		} catch (MissingResourceException offeringMissingResourceException) {
			try {
				return rsrc.getString(generalKey);
			} catch (MissingResourceException generalMissingResourceException) {
				return originalFieldValue;
			}
		}
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
