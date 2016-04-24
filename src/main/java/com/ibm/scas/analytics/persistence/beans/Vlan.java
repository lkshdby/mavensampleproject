package com.ibm.scas.analytics.persistence.beans;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * POJO representing vlan row in the DB schema
 *
 * <pre>
CREATE TABLE VLANS (
  id			VARCHAR (36) NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 100000, INCREMENT BY 1),
  softLayerId	VARCHAR (36),
  subscriberId	VARCHAR (36),
  gatewayId		VARCHAR (36),
  PRIMARY KEY (id),
  FOREIGN KEY (gatewayId) REFERENCES GATEWAYS(id),
  FOREIGN KEY (clusterId) REFERENCES CLUSTERS(id),
);
</pre>
*/
@XmlRootElement
@Entity
@Table(name="VLANS")
public class Vlan {
	@Id
	@GeneratedValue(generator="system-uuid")
	private String id;
	private String softLayerId;

	@ManyToOne(fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="clusterId")
	private Cluster cluster;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="gatewayId")
	private Gateway gateway;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="softLayerAccountId")
	private SoftLayerAccount softLayerAccount;	
	
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="cpeLocationName", nullable=false, updatable=false)
	private CPELocation location;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSoftLayerId() {
		return softLayerId;
	}
	
	public void setSoftLayerId(String softLayerId) {
		this.softLayerId = softLayerId;
	}

    public Cluster getCluster() {
		return cluster;
	}
	
	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	public Gateway getGateway() {
		return gateway;
	}
	
	public void setGateway(Gateway gateway) {
		this.gateway = gateway;
	}
	
	public SoftLayerAccount getSoftLayerAccount() {
		return softLayerAccount;
	}

	public void setSoftLayerAccount(SoftLayerAccount softLayerAccount) {
		this.softLayerAccount = softLayerAccount;
	}	

	public CPELocation getLocation() {
		return location;
	}
	
	public void setLocation(CPELocation location) {
		this.location = location;
	}
}
