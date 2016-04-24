package com.ibm.scas.analytics.resources.admin;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.google.inject.persist.Transactional;
import com.ibm.scas.analytics.BaseResult;
import com.ibm.scas.analytics.beans.Account;
import com.ibm.scas.analytics.beans.Subscriber;
import com.ibm.scas.analytics.beans.Subscriber.SubscriberType;
import com.ibm.scas.analytics.resources.BaseResource;
import com.ibm.scas.analytics.resources.exceptions.ResourceBadRequestException;
import com.ibm.scas.analytics.resources.exceptions.ResourceInternalErrorException;
import com.ibm.scas.analytics.resources.exceptions.ResourceNotFoundException;
import com.ibm.scas.analytics.utils.CPEException;
import com.ibm.scas.analytics.utils.CPEParamException;

/**
 * Jersey handlers for the clusters REST API
 * 
 * @author Han Chen
 *
 */
@Path("/subscribers")
public class SubscriberResource extends BaseResource {
	private final static Logger logger = Logger.getLogger(SubscriberResource.class);

	/**
	 * GET /subscribers
	 * 
	 * @return
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response listSubscribers(@HeaderParam("api-key") String apiKey, @QueryParam("accountId") String accountId, @QueryParam("externalId") String externalId, @QueryParam("type") String type) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			
			this.validateAPIKey(null, apiKey);
			
			SubscriberType subType = null;
			if (type != null) {
				subType = SubscriberType.valueOf(type);
				if (subType == null) {
					throw new ResourceBadRequestException(String.format("Unknown subscriber type: %s", type));
				}
			}
			
			final List<Subscriber> toReturn = tenantService.getSubscribers(accountId, externalId, subType);
			return createOKResponse(toReturn);
		} catch (CPEException e) {
			logger.error(e);
			throw new ResourceInternalErrorException(e);
		}
	}

	/**
	 * POST /subscribers
	 * 
	 * @return
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({ MediaType.APPLICATION_JSON })
	@Transactional
	public Response createSubscriber(@HeaderParam("api-key") String apiKey, Subscriber subscriber) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			
			this.validateAPIKey(null, apiKey);
			
			final String subscriberId = tenantService.createSubscriber(subscriber);
			
			return createOKResponse(new BaseResult("OK", "" + subscriberId));
		} catch (CPEParamException e) {
			throw new ResourceBadRequestException(e.getLocalizedMessage());
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e.getLocalizedMessage());		
		}
	}

	/**
	 * GET /subscribers/{subscriberId}
	 * 
	 * @return
	 */
	@GET
	@Path("/{subscriberId}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getSubscriberDetails(@PathParam("subscriberId") String subscriberId, @HeaderParam("api-key") String apiKey) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			subscriberId = (subscriberId == null ? getStringAttrValueFromSession("subscriberId") : subscriberId);

			this.validateAPIKey(null, apiKey);
			
			if (subscriberId == null) {
				throw new ResourceBadRequestException("Invalid or missing subscriber ID");
			}
			
			final Subscriber subscriber = tenantService.getSubscriberById(subscriberId);
			if (subscriber == null) {
				throw new ResourceNotFoundException();
			}
			return createOKResponse(subscriber);
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e.getLocalizedMessage());
		}
	}

	/**
	 * put /subscribers/{subscriberId}
	 * 
	 * @return
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({ MediaType.APPLICATION_JSON })
	@Transactional
	public Response modifySubscriber(@HeaderParam("api-key") String apiKey, Subscriber subscriber) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			
			this.validateAPIKey(null, apiKey);
			
			if (subscriber.getAccount() == null) {
				throw new ResourceBadRequestException("Missing field: subscriber.account");
			}
				
			if (subscriber.getAccount().getAccountIdentifier() == null) {
				throw new ResourceBadRequestException("Missing field: subscriber.account.accountIdentifier");
			}
			
			// find the account
			final String accountId = subscriber.getAccount().getAccountIdentifier();
			final Account account = tenantService.getAccountById(accountId);
			if (account == null) {
				throw new ResourceNotFoundException(String.format("Account %s not found", subscriber.getAccount().getAccountIdentifier()));
			}
			
			tenantService.modifySubscriber(subscriber);
		
			return createOKResponse(new BaseResult("OK"));
		} catch (CPEParamException e) {
			throw new ResourceBadRequestException(e.getLocalizedMessage());		
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e.getLocalizedMessage());		
		}
	}

	/**
	 * DELETE /subscribers/{subscriberId}
	 * 
	 * @param id
	 * @return
	 */
	@DELETE
	@Path("/{subscriberId}")
	@Produces({ MediaType.APPLICATION_JSON })
	@Transactional
	public Response deleteSubscriber(@PathParam("subscriberId") String subscriberId, @HeaderParam("api-key") String apiKey) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			subscriberId = (subscriberId == null ? getStringAttrValueFromSession("subscriberId") : subscriberId);
			
			if (subscriberId == null) {
				throw new ResourceBadRequestException("Invalid or missing subscriber ID");
			}

			this.validateAPIKey(null, apiKey);
			
			final com.ibm.scas.analytics.persistence.beans.Subscriber subscriber = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Subscriber.class, subscriberId);
			if (subscriber == null) {
				throw new ResourceNotFoundException("Subscriber not found");
			}

			final String accountId = subscriber.getAccount().getId();
			
			tenantService.deleteSubscriber(accountId, subscriberId);

			return createOKResponse(new BaseResult("OK"));
		} catch (CPEException e) {
			logger.error(e);
			throw new ResourceInternalErrorException(e);
		}
	}
}
