package com.ibm.scas.analytics.persistence.beans;

import java.security.GeneralSecurityException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.ibm.scas.analytics.utils.AES;

/**
 * POJO representing the SoftlayerAccount row in the DB schema
 * 
 * <pre>
CREATE TABLE SOFTLAYERACCOUNTS (
  id 			VARCHAR(36) NOT NULL,
  username		VARCHAR(64),
  apiKey 		VARCHAR(64),
  PRIMARY KEY (id)
) ENGINE=INNODB;
</pre>
 * @author Alkesh Dagade
 */
@Entity
@EntityListeners(SoftLayerAccount.SoftlayerAccountListener.class)
@Table(name="SOFTLAYERACCOUNTS")
public class SoftLayerAccount {
	
	@Id
	@GeneratedValue(generator="system-uuid")
	private String id;
	
	private String url;
	private String username;
	
	@Transient
	private String apiKey;	// not saved to db
	
	@Column(name="apiKey") // map APIKey to encrypted field
	private String encApiKey;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	
	public String getEncApiKey() {
		return encApiKey;
	}

	public void setEncApiKey(String encApiKey) {
		this.encApiKey = encApiKey;
	}

	public static class SoftlayerAccountListener {
		/**
	    * Decrypt APiKey after loading.
	    */
	   @PostLoad
	   @PostUpdate
	   public void decryptApiKey(Object pc) throws GeneralSecurityException {
	      if (!(pc instanceof SoftLayerAccount)) {
	         return;
	      }
	 
	      final SoftLayerAccount softlayerAccount = (SoftLayerAccount) pc;
	 
	      if (softlayerAccount.getEncApiKey() != null) {
	    	  softlayerAccount.setApiKey(
				    new AES().decrypt(softlayerAccount.getEncApiKey()));
	      }
	   }
	 
	   /**
	    * Encrypt APiKey before persisting
	    */
	   @PrePersist
	   @PreUpdate
	   public void encryptApiKey(Object pc) throws GeneralSecurityException {
	      if (!(pc instanceof SoftLayerAccount)) {
	         return;
	      }
	 
	      final SoftLayerAccount softlayerAccount = (SoftLayerAccount) pc;
	      softlayerAccount.setEncApiKey(null);;
	 
	      if (softlayerAccount.getApiKey() != null) {
	         softlayerAccount.setEncApiKey(
	            new AES().encrypt(softlayerAccount.getApiKey()));
	      }
	   }
	}
}
