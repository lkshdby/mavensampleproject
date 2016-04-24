package com.ibm.scas.analytics.persistence.beans;

import java.security.GeneralSecurityException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.ibm.scas.analytics.utils.AES;

/**
 * POJO representing the PcmaeBackend row in the DB schema
 * 
 * <pre>
CREATE TABLE PCMAEBACKENDS (
  id 			VARCHAR(50),
  url 			VARCHAR(250),
  account 		VARCHAR(50),
  username 		VARCHAR(50),
  password 		VARCHAR(250),
  PRIMARY KEY(id)
);
</pre>
 * @author Han Chen
 */
@Entity
@EntityListeners(PcmaeBackend.PcmaeBackendPasswordListener.class)
@Table(name="PCMAEBACKENDS")
public class PcmaeBackend {
	
	@Id
	private String id;
	private String url;
	private String account;
	private String username;
	
	@Transient
	private String password;	// not saved to db
	
	@Column(name="password") // map password to encrypted field
	private String encPassword;

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

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

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

	public static class PcmaeBackendPasswordListener {
		/**
	    * Decrypt password after loading.
	    */
	   @PostLoad
	   @PostUpdate
	   public void decryptPassword(Object pc) throws GeneralSecurityException {
	      if (!(pc instanceof PcmaeBackend)) {
	         return;
	      }
	 
	      final PcmaeBackend pcmaeBackend = (PcmaeBackend) pc;
	 
	      if (pcmaeBackend.getEncPassword() != null) {
	    	  pcmaeBackend.setPassword(
				    new AES().decrypt(pcmaeBackend.getEncPassword()));
	      }
	   }
	 
	   /**
	    * Encrypt password before persisting
	    */
	   @PrePersist
	   @PreUpdate
	   public void encryptPassword(Object pc) throws GeneralSecurityException {
	      if (!(pc instanceof PcmaeBackend)) {
	         return;
	      }
	 
	      final PcmaeBackend pcmaeBackend = (PcmaeBackend) pc;
	      pcmaeBackend.setEncPassword(null);;
	 
	      if (pcmaeBackend.getPassword() != null) {
	         pcmaeBackend.setEncPassword(
	            new AES().encrypt(pcmaeBackend.getPassword()));
	      }
	   }
	}
}
