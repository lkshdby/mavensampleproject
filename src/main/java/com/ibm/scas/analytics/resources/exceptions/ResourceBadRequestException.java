package com.ibm.scas.analytics.resources.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

// Throw this exception for HTTP 400 response
public class ResourceBadRequestException extends WebApplicationException {
	public ResourceBadRequestException() {
		super(Response.status(Status.BAD_REQUEST).build());
	}
	
	public ResourceBadRequestException(String message) {
		super(Response.status(Status.BAD_REQUEST).entity(message).build());
	}
	
	public ResourceBadRequestException(String message, Throwable t) {
		super(Response.status(Status.BAD_REQUEST).entity(message).build());
	}
	
	public ResourceBadRequestException(Throwable t) {
		super(Response.status(Status.BAD_REQUEST).entity(t).build());
	}
}
