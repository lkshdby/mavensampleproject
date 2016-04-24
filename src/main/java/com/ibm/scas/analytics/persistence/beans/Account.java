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

/**
 * POJO representing the Account row in the DB schema
 * <pre>
CREATE TABLE ACCOUNTS (
  id			INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1000, INCREMENT BY 1),
  offeringId	VARCHAR(50),
  marketUrl		VARCHAR(500),
  partner		VARCHAR(500),
  expiration	BIGINT,
  quantity 		INTEGER,
  edition 		VARCHAR(100),
  state 		INTEGER,
  PRIMARY KEY (id),
  FOREIGN KEY (offeringId) REFERENCES OFFERINGS(id) ON DELETE RESTRICT
);
 </pre>
 * @author Han Chen
 *
 */
@Entity
@Table(name = "ACCOUNTS")
public class Account {
	public static final int UNKNOWN = -1;
	public static final int ACTIVE = 0;
	public static final int FREE_TRIAL = 1;
	public static final int FREE_TRIAL_EXPIRED = 2;
	public static final int SUSPENDED = 3;
	public static final int CANCELLED = 4;
	
	public static final String SYSTEM_PARTNER = "_SYSTEM_";
	public static final String SYSTEM_URL = "http://analytics.icas.ibm.com";
	
	public static final String ACCOUNT_PROPS_SHRED = "SHRED";
	public static final String ACCOUNT_PROPS_DGW = "DGW";
	
	
	@Id
	@GeneratedValue(generator="system-uuid")
	private String id;
	
	@ManyToOne
	@JoinColumn(name="offeringId")
	private Offering offering;
	
	private String marketUrl = null;
	private String partner = null;

	/**
	 * expiration == 0 means no expiry
	 */
	private long expiration = -1;
	/**
	 * quantity == 0 mean no limit
	 */
	private int quantity = -1;
	
	private String edition;
	private int state;
	
	@ElementCollection(fetch=FetchType.LAZY)
	@CollectionTable(name="ACCOUNT_PARAMS",
			   joinColumns=@JoinColumn(name="accountId"))
	@MapKeyColumn(name="name")
	@Column(name="value")
	private Map<String, String> properties = new HashMap<String, String>();
	
	public static int parseState(String state) {
		if ("ACTIVE".equals(state)) {
			return ACTIVE;
		} else if ("FREE_TRIAL".equals(state)) {
			return FREE_TRIAL;
		} else if ("FREE_TRIAL_EXPIRED".equals(state)) {
			return FREE_TRIAL_EXPIRED;
		} else if ("SUSPENDED".equals(state)) {
			return SUSPENDED;
		} else if ("CANCELLED".equals(state)) {
			return CANCELLED;
		} else {
			return UNKNOWN;
		}
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Offering getOffering() {
		return offering;
	}
	public void setOffering(Offering offering) {
		this.offering = offering;
	}
	public String getMarketUrl() {
		return marketUrl;
	}
	public void setMarketUrl(String marketUrl) {
		this.marketUrl = marketUrl;
	}
	public String getPartner() {
		return partner;
	}
	public void setPartner(String partner) {
		this.partner = partner;
	}
	public long getExpiration() {
		return expiration;
	}
	public void setExpiration(long expiration) {
		this.expiration = expiration;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public String getEdition() {
		return edition;
	}
	public void setEdition(String edition) {
		this.edition = edition;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
}
