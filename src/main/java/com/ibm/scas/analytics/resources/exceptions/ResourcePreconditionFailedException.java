package com.ibm.scas.analytics.resources.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

// Throw this for HTTP 412 Precondition Failed response
public class ResourcePreconditionFailedException extends WebApplicationException {
	public ResourcePreconditionFailedException() {
		super(Response.status(Status.PRECONDITION_FAILED).build());
	}
	
	public ResourcePreconditionFailedException(String message) {
		super(Response.status(Status.PRECONDITION_FAILED).entity(message).build());
	}
}
