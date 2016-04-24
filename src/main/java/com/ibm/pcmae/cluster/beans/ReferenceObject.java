package com.ibm.pcmae.cluster.beans;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Some objects returned by the RMI API contain references to definition
 * objects. For instance, a Machine in a cluster has a reference to its
 * definition. This class is used to store details about the reference 
 * 
 * @author Marcos Dias de Assuncao
 */

@XmlRootElement
public class ReferenceObject {
	private String id;
	private String name;
	private String description;
	
	public ReferenceObject() {
		
	}
	
	public ReferenceObject(String id) {
		super();
		this.id = id;
	}

	public ReferenceObject(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public ReferenceObject(String id, String name, String descr) {
		this(id, name);
		this.description = descr;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
}
