package com.ibm.scas.analytics.persistence.beans;

import java.security.GeneralSecurityException;
import java.util.Collection;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import com.ibm.scas.analytics.utils.AES;

/**
 * 
 * POJO representing the Gateway row in the DB schema
 *
 * <pre>
CREATE TABLE GATEWAYS (
  id			VARCHAR(36) NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 100000, INCREMENT BY 1),
  softLayerId	VARCHAR(36),

  type			VARCHAR(10),
  sslCert		VARCHAR(1024) NOT NULL,
  softLayerAccountId  VARCHAR(36) NOT NULL,
  accountId		VARCHAR(36),
  PRIMARY KEY (id),
  FOREIGN KEY (subscriberId) REFERENCES SUBSCRIBERS(id) ON DELETE CASCADE,
  FOREIGN KEY (softLayerAccountId) REFERENCES SOFTLAYERACCOUNTS(id) ON DELETE RESTRICT
);


</pre>
*/
@XmlRootElement
@Entity
@EntityListeners(Gateway.GatewayPasswordListener.class)
@Table (name="GATEWAYS")
public class Gateway {
	@Id
	@GeneratedValue(generator="system-uuid")
	private String id;
	private String softLayerId;

	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@JoinColumn(name="accountId", nullable=true, updatable=true)
	private Account account;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="softLayerAccountId", nullable=false, updatable=true)
	private SoftLayerAccount softLayerAccount;	
	
	private String type;
	
	// collection of gateway members
	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(name="GATEWAY_SSL_CERTS",
					 joinColumns=@JoinColumn(name="gatewayId")
	)
	private Collection<GatewayMember> gatewayMembers;

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


	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public Account getAccount() {
		return account;
	}
	
	public void setAccount(Account account) {
		this.account = account;
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

	public Collection<GatewayMember> getGatewayMembers() {
		return gatewayMembers;
	}

	public void setGatewayMembers(Collection<GatewayMember> members) {
		this.gatewayMembers = members;
	}
	
	public static class GatewayPasswordListener {
		/**
		 * Decrypt password after loading.
		 */
		@PostLoad
		@PostUpdate
		public void decryptPassword(Object pc) throws GeneralSecurityException {
			if (!(pc instanceof Gateway)) {
				return;
			}

			final Gateway gateway = (Gateway) pc;
			if (gateway.getGatewayMembers() == null) {
				return;
			}
			
			for (final GatewayMember gatewayMember : gateway.getGatewayMembers()) {
				if (gatewayMember.getEncPassword() == null) {
					continue;
				}
				gatewayMember.setPassword(
						new AES().decrypt(gatewayMember.getEncPassword()));
			}
		}

		/**
		 * Encrypt password before persisting
		 */
		@PrePersist
		@PreUpdate
		public void encryptPassword(Object pc) throws GeneralSecurityException {
			if (!(pc instanceof Gateway)) {
				return;
			}

			final Gateway gateway = (Gateway) pc;
			if (gateway.getGatewayMembers() == null) {
				return;
			}
			
			for (final GatewayMember gatewayMember : gateway.getGatewayMembers()) {
				if (gatewayMember.getPassword() == null) {
					gatewayMember.setEncPassword(null);
					continue;
				}

				gatewayMember.setEncPassword(
						new AES().encrypt(gatewayMember.getPassword()));
			}
		}
	}
}
