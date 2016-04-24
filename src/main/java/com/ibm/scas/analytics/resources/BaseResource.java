package com.ibm.scas.analytics.resources;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.ibm.scas.analytics.EngineProperties;
import com.ibm.scas.analytics.backend.FirewallService;
import com.ibm.scas.analytics.backend.NetworkService;
import com.ibm.scas.analytics.backend.ProvisioningService;
import com.ibm.scas.analytics.backend.TenantService;
import com.ibm.scas.analytics.persistence.PersistenceException;
import com.ibm.scas.analytics.persistence.PersistenceService;
import com.ibm.scas.analytics.persistence.beans.Subscriber;
import com.ibm.scas.analytics.resources.exceptions.ResourceInternalErrorException;
import com.ibm.scas.analytics.resources.exceptions.ResourceUnauthorizedException;

/**
 * base jersey handler class with helper functions
 * @author Han Chen
 *
 */
public class BaseResource {
	private final static Logger logger = Logger.getLogger(BaseResource.class);

	@Context
	protected UriInfo uriInfo;
	@Context
	protected HttpServletRequest request;
	@Context
	protected ServletContext context;
	
	@Inject
	protected PersistenceService persistence;
	@Inject
	protected ProvisioningService engine;
	@Inject
	protected NetworkService networkDetailProvider;
	@Inject
	protected TenantService tenantService;
	@Inject
	protected FirewallService firewallService;

	protected final static String adminApiKey = EngineProperties.getInstance().getProperty(EngineProperties.ADMIN_API_KEY);
	protected final static Boolean isHub = Boolean.valueOf(EngineProperties.getInstance().getProperty(EngineProperties.CPE_LOCATION_IS_HUB, EngineProperties.DEFAULT_CPE_LOCATION_IS_HUB));
	protected final static String myLocationName = EngineProperties.getInstance().getProperty(EngineProperties.CPE_LOCATION_NAME, EngineProperties.DEFAULT_CPE_LOCATION_NAME);
	
	
	protected <T> Response createResponse(Status status, T result) {
//		GenericEntity<T> entity = new GenericEntity<T>(result) {};

		return Response.status(status).entity(result).build();
	}
	
	protected <T> Response createNotFoundResponse(T result) {
		return createResponse(Status.NOT_FOUND, result);
	}
	
	protected <T> Response createInternalErrorResponse(T result) {
		return createResponse(Status.INTERNAL_SERVER_ERROR, result);
	}

	protected <T> Response createOKResponse(T result) {
		return createResponse(Status.OK, result);
	}
	
	protected <T> Response createCreatedResponse(T result) {
		return createResponse(Status.CREATED, result);
	}
	
	protected <T> Response createNotAuthorizedResponse(T result) {
		return createResponse(Status.UNAUTHORIZED, result);
	}
	
	protected <T> Response createErrorResponse(T result) {
		return createResponse(Status.PRECONDITION_FAILED, result);
	}
	
	/*
	 * This function validates the CPE API key for 2 cases:
	 *   
	 * Case 1: Admin or Hub is calling the REST API directly.
	 *   Case 1.1: Admin uses curl command (for example) and calls REST API directly.
	 *   
	 *   Case 1.2: Hub calls REST API of Spoke.
	 *   Here, only Hub CPE has a public IP address, so the flow is CPE UI calls HUB REST API,
	 *   Hub CPE validates the apiKey and subscriber id are in the session (case 1), then forwards the request
	 *   to Spoke CPE passing the Admin apiKey. On the spoke CPE side there is no valid http session or subscriber
	 *   details in the DB so the Spoke CPE will check if the passed in apiKey is the adminApiKey.
	 *   
	 * Case 2: Client or CPE UI calls REST API
	 *   Case 2.1: In case of CPE UI, the API KEY and Subscriber ID should be retrieved from the session
	 *   and passed into this function.
	 * 
	 *   Case 2.2: In the future we may expose our API key to customers so they can write 
	 *   clients against our REST API. In this case the subscriberId and apiKey will be passed in
	 *   the http request header and we validate that this combo exists in the DB.
	 */
	protected final void validateAPIKey(String subscriberId, String apiKey) throws ResourceUnauthorizedException, ResourceInternalErrorException {
		if (apiKey == null) {
			throw new ResourceUnauthorizedException();
		}
		
		// This is admin calling REST API directly so return right away, we trust admin.
		if (apiKey.equals(adminApiKey)) {
			return;
		}

		if (subscriberId == null) {
			throw new ResourceUnauthorizedException();
		}

		// Now validate that the api key exists in the db for the subscriber
		try {
			Subscriber subscriber = persistence.getObjectById(Subscriber.class, subscriberId);

			if (subscriber == null) 
				throw new ResourceUnauthorizedException();
			
			if (!subscriber.getApiKey().equals(apiKey)) {
				throw new ResourceUnauthorizedException();
			}
		} catch (PersistenceException e) {
			logger.error(e);
			throw new ResourceInternalErrorException(e);
		}
	}
	
	// Returns the value of the attribute from HTTP Session
	protected String getStringAttrValueFromSession(String attrName) {
		return (String) request.getSession().getAttribute(attrName);
	}
	
	protected Integer getIntAttrValueFromSession(HttpSession session, Integer attrVar, String attrName) {
		return (Integer) request.getSession().getAttribute(attrName);
	}
}
