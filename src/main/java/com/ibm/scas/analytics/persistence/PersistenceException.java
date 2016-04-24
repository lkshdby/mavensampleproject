package com.ibm.scas.analytics.persistence;

import com.ibm.scas.analytics.utils.CPEException;


public class PersistenceException extends CPEException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4773442762642046905L;

	
	public PersistenceException(Throwable cause) {
		super(cause);
	}

	public PersistenceException(String localizedMessage, Throwable e) {
		super(localizedMessage, e);
	}
}
