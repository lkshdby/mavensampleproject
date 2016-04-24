package com.ibm.scas.analytics.persistence.beans;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "NODECONFIGURATIONS")
public class NodeConfiguration {
	@Id
	@GeneratedValue(generator="system-uuid")
	private String id = null;
	
	private String pluginId = null;
	
	private String nodeSize = null;
	private String specification = null;
	private String dataBandwidth = null;
	private String usedFor = null;
	
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
	
	public String getNodeSize() {
		return nodeSize;
	}
	
	public void setNodeSize(String nodeSize) {
		this.nodeSize = nodeSize;
	}
	
	public String getSpecification() {
		return specification;
	}
	
	public void setSpecification(String specification) {
		this.specification = specification;
	}
	
	public String getDataBandwidth() {
		return dataBandwidth;
	}
	
	public void setDataBandwidth(String dataBandwidth) {
		this.dataBandwidth = dataBandwidth;
	}
	
	public String getUsedFor() {
		return usedFor;
	}
	
	public void setUsedFor(String usedFor) {
		this.usedFor = usedFor;
	}
	
	public void localize(Locale locale)
	{
		ResourceBundle rsrc = ResourceBundle.getBundle("cpe_messages", locale);
		
		String offeringSpecificPreKeyText = pluginId + "." + nodeSize.toLowerCase() + ".";
		String generalPreKeyText = "hdp." + nodeSize.toLowerCase() + ".";
		
		nodeSize =  (String) getLocalizedValue(rsrc, offeringSpecificPreKeyText + "nodeSize", generalPreKeyText + "nodeSize", nodeSize);
		specification =  (String) getLocalizedValue(rsrc, offeringSpecificPreKeyText + "specification", generalPreKeyText + "specification", specification);
		dataBandwidth =  (String) getLocalizedValue(rsrc, offeringSpecificPreKeyText + "dataBandwidth", generalPreKeyText + "dataBandwidth", dataBandwidth);
		usedFor = (String) getLocalizedValue(rsrc, offeringSpecificPreKeyText + "usedFor", generalPreKeyText + "usedFor", usedFor);
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
