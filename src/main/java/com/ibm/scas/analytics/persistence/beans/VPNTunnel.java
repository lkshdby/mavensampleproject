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

/*
 * POJO representing VPN Tunnels
 * CREATE TABLE VPNTUNNELS (
 * 	id     		VARCHAR(36) NOT NULL,
 *  custIpAddr	VARCHAR(36) NOT NULL,
 * 	gatewayId	VARCHAR(36),
 * 	clusterId	VARCHAR(36) NOT NULL,
 * 	PRIMARY KEY (id),
 * 	FOREIGN KEY (gatewayId) REFERENCES GATEWAYS(id) ON DELETE RESTRICT,
 * 	FOREIGN KEY (clusterId) REFERENCES CLUSTERS(id) ON DELETE CASCADE
 * );
 * 
 * CREATE TABLE VPNTUNNEL_PARAMS (
 * 	vpnTunnelId	VARCHAR(36) NOT NULL,
 * 	name		VARCHAR(50) NOT NULL,
 * 	value		VARCHAR(255),
 * 	FOREIGN KEY (vpnTunnelId) REFERENCES VPNTUNNELS(id) on DELETE CASCADE
 * );
 * 
 * CREATE TABLE VPNTUNNEL_SUBNET (
 * 	vpnTunnelId	VARCHAR(36) NOT NULL,
 * 	subnetId	VARCHAR(36) NOT NULL,
 * 	FOREIGN KEY (vpnTunnelId) REFERENCES VPNTUNNELS(id) on DELETE CASCADE,
 * 	FOREIGN KEY (subnetId) REFERENCES SUBNETS(id) on DELETE CASCADE
 * );
 */

@Entity
@Table (name="VPNTUNNELS")
public class VPNTunnel {
	@Id
	@GeneratedValue(generator="system-uuid")
	private String id;
	
	private String custIpAddr;
	
	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "gatewayId")
	private Gateway gateway;
	
	@ElementCollection(fetch=FetchType.LAZY)
	@CollectionTable(name="VPNTUNNEL_PARAMS",
			   joinColumns=@JoinColumn(name="vpnTunnelId"))
	@MapKeyColumn(name="name")
	@Column(name="value")
	// map of vpn tunnel params to gateway members
	private Map<String, String> params = new HashMap<String, String>();

	public Gateway getGateway() {
		return gateway;
	}

	public void setGateway(Gateway gateway) {
		this.gateway = gateway;
	}

	public Map<String, String> getParams() {
		return params;
	}
	
	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	public String getId() {
		return id;
	}
	

	public void setId(String id) {
		this.id = id;
	}

	public String getCustIpAddr() {
		return custIpAddr;
	}

	public void setCustIpAddr(String custIpAddr) {
		this.custIpAddr = custIpAddr;
	}
	
	
	
}
