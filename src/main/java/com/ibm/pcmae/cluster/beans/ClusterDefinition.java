package com.ibm.pcmae.cluster.beans;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ClusterDefinition {

	protected String id;
	protected String name;
	protected String version;
	protected String description;
	protected Boolean published;
	protected String creator;
	protected ReferenceObject ownerAccount;
	protected String versionId;

	public ClusterDefinition() {
	}

	public ClusterDefinition(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	
	public ClusterDefinition(ClusterDefinition def) {
		this.id = def.id;
		this.name = def.name;
		this.version = def.version;
		this.description = def.description;
		this.published = def.published;
		this.creator = def.creator;
		this.ownerAccount = def.ownerAccount;
		this.versionId = def.versionId;
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
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getPublished() {
		return published;
	}

	public void setPublished(Boolean isPublished) {
		this.published = isPublished;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public ReferenceObject getOwnerAccount() {
		return ownerAccount;
	}

	public void setOwnerAccount(ReferenceObject owner) {
		this.ownerAccount = owner;
	}
	
	public void setVersionId(String versionId) {
		this.versionId = versionId;
	}
	
	public String getVersionId() {
		return versionId;
	}

}
