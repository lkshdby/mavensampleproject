package com.ibm.pcmae.cluster.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Parameter {

	private String name;
	private String description;
	private String value;
	private String type;
	private ParameterAllowedValues allowedValues;

	public Parameter() {
	}
	
	public Parameter(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public static final List<Parameter> createParameterList(Properties props) {
		ArrayList<Parameter> list = new ArrayList<Parameter>();
		for (Object key : props.keySet()) {
			Parameter p = new Parameter(key.toString(), props.getProperty(key.toString()));
			list.add(p);
		}
		
		return list;
	}

	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public ParameterAllowedValues getAllowedValues() {
		return allowedValues;
	}

	public void setAllowedValues(ParameterAllowedValues allowedValues) {
		this.allowedValues = allowedValues;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
