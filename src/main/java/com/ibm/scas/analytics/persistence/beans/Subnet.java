package com.ibm.scas.analytics.persistence.beans;


import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * 
 * POJO representing subnet row in the DB schema
 *
 * <pre>
CREATE TABLE SUBNETS (
  id			VARCHAR(36) NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 100000, INCREMENT BY 1),
  softLayerId	VARCHAR(36),
  vlanId		VARCHAR(36) NOT NULL,
  networkAddr	VARCHAR(15),
  gatewayAddr	VARCHAR(15),
  broadcastAddr	VARCHAR(15),
  cidr			INTEGER,
  PRIMARY KEY (id),
  UNIQUE KEY vlan_subnet (vlanId, networkAddr, cidr),
  FOREIGN KEY (vlanId) REFERENCES VLANS(softLayerId) ON DELETE CASCADE
);

</pre>
*/
@Entity
@Table(name = "SUBNETS")
public class Subnet  implements Serializable {
	private static final long serialVersionUID = -6217763113458413948L;
	
	@Id
	@GeneratedValue(generator="system-uuid")
	private String id;
	private String softLayerId;
	
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "vlanId")
	private Vlan vlan;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="softLayerAccountId")
	private SoftLayerAccount softLayerAccount;	
	
	private String networkAddr;
	private int cidr;
	
	// optional fields
	private String broadcastAddr;
	private String gatewayAddr;
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

	public Vlan getVlan() {
		return vlan;
	}

	public void setVlan(Vlan vlan) {
		this.vlan = vlan;
	}
	
	public String getNetworkAddr() {
		return networkAddr;
	}
	
	public void setNetworkAddr(String networkAddr) {
		this.networkAddr = networkAddr;
	}
	
	public String getBroadcastAddr() {
		return broadcastAddr;
	}
	
	public void setBroadcastAddr(String broadcastAddr) {
		this.broadcastAddr = broadcastAddr;
	}
	
	public String getGatewayAddr() {
		return gatewayAddr;
	}
	
	public void setGatewayAddr(String gatewayAddr) {
		this.gatewayAddr = gatewayAddr;
	}
	
	public int getCidr() {
		return cidr;
	}
	
	public void setCidr(int cidr) {
		this.cidr = cidr;
	}

	public SoftLayerAccount getSoftLayerAccount() {
		return softLayerAccount;
	}

	public void setSoftLayerAccount(SoftLayerAccount softLayerAccount) {
		this.softLayerAccount = softLayerAccount;
	}		
	
}
