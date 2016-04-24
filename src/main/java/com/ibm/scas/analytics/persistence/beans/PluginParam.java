package com.ibm.scas.analytics.persistence.beans;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@IdClass(value=PluginParam.PluginParamId.class)
@Table(name = "PLUGINPARAMS")
public class PluginParam {

	@Id
    private String plugin;
	
	@Id
    private String name;
	
	private String value;

    public String getPlugin() {
		return plugin;
	}
    
    public void setPlugin(String plugin) {
		this.plugin = plugin;
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

    static class PluginParamId implements Serializable {
    	private String plugin;
        private String name;

        public String getPlugin() {
			return plugin;
		}
        
        public void setPlugin(String plugin) {
			this.plugin = plugin;
		}
        
        public String getName() {
			return name;
		}
        
        public void setName(String name) {
			this.name = name;
		}
	}
}
