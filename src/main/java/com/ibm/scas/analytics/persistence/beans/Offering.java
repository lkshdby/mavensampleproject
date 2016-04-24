package com.ibm.scas.analytics.persistence.beans;

import java.security.GeneralSecurityException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.ibm.scas.analytics.utils.AES;

/**
 * POJO representing the Offering row in the DB schema
 * 
 * <pre>
CREATE TABLE OFFERINGS (
  id			VARCHAR(50) NOT NULL,
  oauthKey		VARCHAR(250),
  oauthSecret	VARCHAR(250),
  name			VARCHAR(250),
  plugin		VARCHAR(50),
  urlPath		VARCHAR(50),
  backendId		VARCHAR(50),
  multiuser		BOOLEAN,
  PRIMARY KEY (id),
  FOREIGN KEY (backendId) REFERENCES PCMAEBACKENDS(id) ON DELETE RESTRICT,
  FOREIGN KEY (plugin) REFERENCES PLUGINS(id) ON DELETE RESTRICT
);
</pre>
 * @author Han Chen
 */
@Entity
@EntityListeners(Offering.OfferingPersistenceListener.class)
@Table(name="OFFERINGS")
public class Offering {
	@Id
	private String id;
	
	private String oauthKey;
	@Transient
	private String oauthSecret;
	
	@Column(name="oauthSecret") // map oauthSecret to encrypted field
	private String encOauthSecret;

	private String name;
	
	@ManyToOne
	@JoinColumn(name="plugin")
	private Plugin plugin;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@JoinColumn(name="urlPath", referencedColumnName="id")
	private ContentMap urlPath;
	
	private boolean multiuser;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getOauthKey() {
		return oauthKey;
	}
	public void setOauthKey(String oauthKey) {
		this.oauthKey = oauthKey;
	}
	public String getOauthSecret() {
		return oauthSecret;
	}
	public void setOauthSecret(String oauthSecret) {
		this.oauthSecret = oauthSecret;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Plugin getPlugin() {
		return plugin;
	}
	public void setPlugin(Plugin plugin) {
		this.plugin = plugin;
	}
	public ContentMap getUrlPath() {
		return urlPath;
	}
	public void setUrlPath(ContentMap urlPath) {
		this.urlPath = urlPath;
	}

	public boolean isMultiuser() {
		return multiuser;
	}
	public void setMultiuser(boolean multiuser) {
		this.multiuser = multiuser;
	}

	public String getEncOauthSecret() {
		return encOauthSecret;
	}
	
	public void setEncOauthSecret(String encOauthSecret) {
		this.encOauthSecret = encOauthSecret;
	}
	public static class OfferingPersistenceListener {
		/**
	    * Decrypt password after loading.
	    */
	   @PostLoad
	   @PostUpdate
	   public void decryptOauthSecret(Object pc) throws GeneralSecurityException {
	      if (!(pc instanceof Offering)) {
	         return;
	      }
	 
	      final Offering offering = (Offering) pc;
	      offering.setOauthSecret(null);
	 
	      if (offering.getEncOauthSecret() != null) {
	    	  offering.setOauthSecret(
				    new AES().decrypt(offering.getEncOauthSecret()));
	      }
	   }
	 
	   /**
	    * Encrypt password before persisting
	    */
	   @PrePersist
	   @PreUpdate
	   public void encryptOauthSecret(Object pc) throws GeneralSecurityException {
	      if (!(pc instanceof Offering)) {
	         return;
	      }
	 
	      final Offering offering = (Offering) pc;
	 
	      if (offering.getOauthSecret() != null) {
	         offering.setEncOauthSecret(
	            new AES().encrypt(offering.getOauthSecret()));
	      }
	   }
	}
}
