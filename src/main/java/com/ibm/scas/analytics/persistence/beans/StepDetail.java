package com.ibm.scas.analytics.persistence.beans;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="STEPDETAILS")
public class StepDetail {
	@Id
	@GeneratedValue(generator="system-uuid")
	private String id = null;
	private Integer stepNumber = null;
	private String pluginId = null;
	private String name = null;
	private String description = null;
	private String formTitle = null;
	private String formDescription = null;
	private Integer isEnabled = null;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getPluginId() {
		return pluginId;
	}
	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getFormTitle() {
		return formTitle;
	}
	
	public void setFormTitle(String formTitle) {
		this.formTitle = formTitle;
	}
	
	public String getFormDescription() {
		return formDescription;
	}
	
	public void setFormDescription(String formDescription) {
		this.formDescription = formDescription;
	}
	
	public Integer getIsEnabled() {
		return isEnabled;
	}
	
	public void setIsEnabled(Integer isEnabled) {
		this.isEnabled = isEnabled;
	}
	
	public Integer getStepNumber() {
		return stepNumber;
	}
	
	public void setStepNumber(Integer stepNumber) {
		this.stepNumber = stepNumber;
	}
	
	public void localize(Locale locale)
	{
		ResourceBundle rsrc = ResourceBundle.getBundle("cpe_messages", locale);
		String fieldKey = description.toLowerCase().replace(" ", "");
		String offeringSpecificPreKeyText =  pluginId + "." + fieldKey + ".";
		String generalPreKeyText = "hdp." + fieldKey + ".";
		
		description =  (String) getLocalizedValue(rsrc, offeringSpecificPreKeyText + "description", generalPreKeyText + "description",  description);
		formTitle =  (String) getLocalizedValue(rsrc, offeringSpecificPreKeyText + "formTitle", generalPreKeyText + "formTitle", formTitle);
		formDescription =  (String) getLocalizedValue(rsrc, offeringSpecificPreKeyText + "formDescription", generalPreKeyText + "formDescription", formDescription);
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
	
}
