package com.ibm.scas.analytics.persistence.beans;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

/**
 * 
 * POJO representing the Cluster row in the DB schema
 *
 * <pre>
CREATE TABLE CLUSTERS (
  id			INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 100000, INCREMENT BY 1),
  owner			INTEGER,
  name			VARCHAR(250),
  description	VARCHAR(250),
  clusterId		VARCHAR(250),
  size			INTEGER,
  state			INTEGER,
  launchTime	BIGINT,
  terminateTime	BIGINT,
  PRIMARY KEY (id),
  FOREIGN KEY (owner) REFERENCES SUBSCRIBERS(id) ON DELETE CASCADE
);
</pre>
 * @author Han Chen
 *
 */
@Entity
@Table(name = "CLUSTERS")
public class Cluster {
	public static final int INUSE = 1;
	public static final int DELETING = 2;
	public static final int DELETED = 3;
	
	public static final String CLUSTERTYPE_DEDICATED_GATEWAY = "DedicatedGateway";
	
	public static final String CLUSTER_PROP_CLUSTERTYPE = "ClusterType";
	public static final String CLUSTER_PROP_GATEWAYID = "GatewayId";
	public static final String CLUSTER_PROP_CPELOCATIONNAME = "cpeLocationName";
	
	@Id
	@GeneratedValue(generator="system-uuid")
	private String id;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@JoinColumn(name="owner", nullable=false)
	private Subscriber owner;
	
	private String name;
	private String description;
	private String clusterId;
	private int size;
	private int state;
	private long launchTime;
	private long terminateTime;
	private String currentStep;

	@ElementCollection(fetch=FetchType.LAZY)
	@CollectionTable(name="CLUSTER_PARAMS",
			   joinColumns=@JoinColumn(name="clusterId"))
	@MapKeyColumn(name="name")
	@Column(name="value")
	private Map<String, String> clusterParams = new HashMap<String, String>();

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Subscriber getOwner() {
		return owner;
	}
	public void setOwner(Subscriber owner) {
		this.owner = owner;
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
	public String getClusterId() {
		return clusterId;
	}
	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}
	
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public long getLaunchTime() {
		return launchTime;
	}
	public void setLaunchTime(long launchTime) {
		this.launchTime = launchTime;
	}
	public long getTerminateTime() {
		return terminateTime;
	}
	public void setTerminateTime(long terminateTime) {
		this.terminateTime = terminateTime;
	}
	public String getCurrentStep() {
		return currentStep;
	}
	public void setCurrentStep(String currentStep) {
		this.currentStep = currentStep;
	}
	public Map<String, String> getClusterParams() {
		return clusterParams;
	}
	public void setClusterParams(Map<String, String> clusterParams) {
		this.clusterParams = clusterParams;
	}
}
