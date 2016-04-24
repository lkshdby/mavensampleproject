package com.ibm.scas.analytics.persistence.beans;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * 
 * POJO representing the IP Address row in the DB schema
 *
 * <pre>
CREATE TABLE IPADDRESS (
  id			VARCHAR(36),
  ipAddress 	BIGINTEGER NOT NULL,
  subnetId		VARCHAR(36) NOT NULL,
  hostname		VARCHAR(64),
  clusterId		INTEGER
  tierName		VARCHAR(50),
  PRIMARY KEY (id),
  FOREIGN KEY (subnetId) REFERENCES SUBNETS(id) ON DELETE CASCADE
  FOREIGN KEY (clusterId) REFERENCES CLUSTERS(id) ON DELETE CASCADE
);

</pre>
*/
@Entity
@Table(name="IPADDRESS")
public class IPAddress {

	@Id
	@GeneratedValue(generator="system-uuid")
	private String id;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="subnetId")
	private Subnet subnet;
	
	private long ipAddress; // long representation of IP address
	private String hostname;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="clusterId")
	private Cluster cluster;
	
	private String tierName;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getIpAddress() {
		return ipAddress;
	}
	
	public void setIpAddress(long ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	
	public String getTierName() {
		return tierName;
	}
	
	public void setTierName(String tierName) {
		this.tierName = tierName;
	}

	public Subnet getSubnet() {
		return subnet;
	}
	

	public void setSubnet(Subnet subnet) {
		this.subnet = subnet;
	}
	

	public Cluster getCluster() {
		return cluster;
	}
	

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}
	
	
}
