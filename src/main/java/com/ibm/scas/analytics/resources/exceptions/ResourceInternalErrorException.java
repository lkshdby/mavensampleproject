package com.ibm.scas.analytics.resources.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

// Throw this exception for HTTP 500 response
public class ResourceInternalErrorException extends WebApplicationException {
	public ResourceInternalErrorException() {
		super(Response.status(Status.INTERNAL_SERVER_ERROR).build());
	}
	
	public ResourceInternalErrorException(String message) {
		super(Response.status(Status.INTERNAL_SERVER_ERROR).entity(message).build());
	}
	
	public ResourceInternalErrorException(String message, Throwable t) {
		super(Response.status(Status.INTERNAL_SERVER_ERROR).entity(message).build());
	}
	
	public ResourceInternalErrorException(Throwable t) {
		super(Response.status(Status.INTERNAL_SERVER_ERROR).entity(t).build());
	}
}
