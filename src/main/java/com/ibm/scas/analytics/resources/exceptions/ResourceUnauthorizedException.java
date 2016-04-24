package com.ibm.scas.analytics.resources.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

// throw this exception for HTTP 401 response
public class ResourceUnauthorizedException extends WebApplicationException {
	public ResourceUnauthorizedException() {
		super(Response.status(Status.UNAUTHORIZED).build());
	}
	
	public ResourceUnauthorizedException(String message) {
		super(Response.status(Status.UNAUTHORIZED).entity(message).build());
	}
	
}
