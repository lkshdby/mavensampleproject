package com.ibm.scas.analytics.persistence.beans;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Table;
import javax.persistence.Transient;

/*
CREATE TABLE GATEWAY_SSL_CERTS (
  gatewayId    VARCHAR(36) NOT NULL,
  memberIp     VARCHAR(36) NOT NULL,
  username		VARCHAR(25),
  password		VARCHAR(50),
  sslCert      VARCHAR(1024) NOT NULL,
  FOREIGN KEY (gatewayId) REFERENCES GATEWAYS(id) ON DELETE CASCADE
);
*/

@Embeddable
@Table (name="GATEWAY_SSL_CERTS")
public class GatewayMember {
	
	private String memberIp;
	private String username;
	
	@Transient
	private String password;
	
	@Column(name="password") // map password to encrypted field
	private String encPassword;
	
	private String sslCert;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEncPassword() {
		return encPassword;
	}

	public void setEncPassword(String encPassword) {
		this.encPassword = encPassword;
	}

	public String getMemberIp() {
		return memberIp;
	}

	public void setMemberIp(String memberIp) {
		this.memberIp = memberIp;
	}

	public String getSslCert() {
		return sslCert;
	}

	public void setSslCert(String sslCert) {
		this.sslCert = sslCert;
	}
	

}
