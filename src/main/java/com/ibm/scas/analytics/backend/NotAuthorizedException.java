package com.ibm.scas.analytics.backend;

import com.ibm.scas.analytics.utils.CPEException;

public class NotAuthorizedException extends CPEException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NotAuthorizedException() {
		super();
	}

	public NotAuthorizedException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public NotAuthorizedException(String arg0) {
		super(arg0);
	}

	public NotAuthorizedException(Throwable arg0) {
		super(arg0);
	}
}
