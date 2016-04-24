package com.ibm.scas.analytics.utils;

public class CPEException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8539199581210892331L;

	public CPEException(Throwable cause) {
		super(cause);
	}

	public CPEException(String localizedMessage, Throwable e) {
		super(localizedMessage, e);
	}

	public CPEException() {
		super();
	}

	public CPEException(String arg0) {
		super(arg0);
	}
}

