package com.ibm.scas.analytics.persistence.beans;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="OFFERINGLOCATIONS")
public class OfferingLocations {
	@Id
	private String id = null;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY, cascade={CascadeType.REMOVE})
	@JoinColumn(name="offeringId", nullable=true, updatable=true)
	private Offering offeringId = null;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY, cascade={CascadeType.REMOVE})
	@JoinColumn(name="cpeLocationName", nullable=true, updatable=true)
	private CPELocation locationName = null;
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}

	public Offering getOfferingId() {
		return offeringId;
	}

	public void setOfferingId(Offering offeringId) {
		this.offeringId = offeringId;
	}

	public CPELocation getLocationName() {
		return locationName;
	}

	public void setLocationName(CPELocation locationName) {
		this.locationName = locationName;
	}
	
}


