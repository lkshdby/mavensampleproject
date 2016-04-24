package com.ibm.scas.analytics.backend.appdirect;

import com.ibm.scas.analytics.utils.CPEException;

public class AppDirectGatewayException extends CPEException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -571980069705834659L;
	final String id;

	public AppDirectGatewayException(Throwable cause, String id) {
		super(cause);
		
		this.id = id;
	}
	
	public String getId() {
		return this.id;
	}

}
