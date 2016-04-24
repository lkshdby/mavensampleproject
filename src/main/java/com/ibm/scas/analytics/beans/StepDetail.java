package com.ibm.scas.analytics.beans;

public class StepDetail {

	private Integer id = null;
	private String pluginId = null;
	private String name = null;
	private String description = null;
	private String formTitle = null;
	private String formDescription = null;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
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
}
