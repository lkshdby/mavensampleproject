package com.ibm.scas.analytics.resources.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.sun.jersey.api.Responses;

// throw this for HTTP 404 Not Found response
public class ResourceNotFoundException extends WebApplicationException {

	public ResourceNotFoundException() {
		super(Responses.notFound().build());
	}
	
	public ResourceNotFoundException(String message) {
		super(Response.status(Responses.NOT_FOUND).entity(message).type("text/plain").build());
	}
}
