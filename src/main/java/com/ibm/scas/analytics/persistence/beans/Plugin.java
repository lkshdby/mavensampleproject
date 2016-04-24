package com.ibm.scas.analytics.persistence.beans;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * POJO representing the Plugin row in the DB schema
 * 
 * <pre>
CREATE TABLE PLUGINS (
  id 			VARCHAR(50),
  className 	VARCHAR(250),
  source 		VARCHAR(250),
  PRIMARY KEY (id)
);
</pre>
 * @author Han Chen
 */
@Entity
@Table(name="PLUGINS")
public class Plugin {
	
	@Id
	private String id;
	private String className;
	private String source;
	
	public Plugin() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
}
