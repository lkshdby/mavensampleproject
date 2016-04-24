package com.ibm.scas.analytics.persistence.beans;

import java.security.GeneralSecurityException;
import java.util.UUID;

import javax.persistence.Column;
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
import javax.persistence.Transient;

import com.ibm.scas.analytics.utils.AES;


/**
 * POJO representing the Subscriber row in the DB schema
 * <pre> 
CREATE TABLE SUBSCRIPTIONS (
  id			INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 10000, INCREMENT BY 1),
  apiKey		VARCHAR(50),
  type			INTEGER,
  accountId		INTEGER,
  externalId	VARCHAR(500),
  name			VARCHAR(500),
  PRIMARY KEY (id),
  FOREIGN KEY (offeringId) REFERENCES OFFERINGS(id) ON DELETE RESTRICT
);
 * </pre>
 * @author Han Chen
 *
 */
@Entity
@EntityListeners(Subscriber.SubscriberAPIKeyListener.class)
@Table(name="SUBSCRIBERS")
public class Subscriber {
	public static final int APPDIRECT = 1;
	public static final int CLOUDOE = 2;
	public static final int CLOUDOE_PULSE_DEMO = 3;
	public static final int SF = 4;
	public static final int APPDIRECT_ARCHIVED = 17;
	public static final int DEV_TEST = 314159265;
	public static final int GARBAGE_COLLECTOR = -1;
	public static final int SYSTEM = -2;
	
	@Id
	@GeneratedValue(generator="system-uuid")
	private String id;
	
	@Column(name="apiKey") // map encApiKey to encrypted field
	private String encApiKey;	// saved to DB
	
	@Transient
	private String apiKey; // filled in by user

	private int type;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="accountId")
	private Account account;
	
	private String externalId;
	private String name;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getApiKey() {
		return apiKey;
	}
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	public void generateRandomApiKey() {
		setApiKey(UUID.randomUUID().toString().replaceAll("\\-", ""));
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}

	public String getExternalId() {
		return externalId;
	}
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Account getAccount() {
		return account;
	}
	
	public void setAccount(Account account) {
		this.account = account;
	}
	
	public String getEncApiKey() {
		return encApiKey;
	}
	
	public void setEncApiKey(String encApiKey) {
		this.encApiKey = encApiKey;
	}
	
	public static class SubscriberAPIKeyListener {
		/**
	    * Decrypt password after loading.
	    */
	   @PostLoad
	   @PostUpdate
	   public void decryptPassword(Object pc) throws GeneralSecurityException {
	      if (!(pc instanceof Subscriber)) {
	         return;
	      }
	 
	      final Subscriber subscriber = (Subscriber) pc;
	 
	      if (subscriber.getEncApiKey() != null) {
	    	  subscriber.setApiKey(
				    new AES().decrypt(subscriber.getEncApiKey()));
	      }
	   }
	 
	   /**
	    * Encrypt password before persisting
	    */
	   @PrePersist
	   @PreUpdate
	   public void encryptPassword(Object pc) throws GeneralSecurityException {
	      if (!(pc instanceof Subscriber)) {
	         return;
	      }
	 
	      final Subscriber subscriber = (Subscriber) pc;
	      subscriber.setEncApiKey(null);;
	 
	      if (subscriber.getApiKey() != null) {
	         subscriber.setEncApiKey(
	            new AES().encrypt(subscriber.getApiKey()));
	      }
	   }
	}
}
