package com.ibm.scas.analytics.resources.admin;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.ibm.scas.analytics.BaseResult;
import com.ibm.scas.analytics.backend.SoftLayerAccountProvider;
import com.ibm.scas.analytics.beans.SoftLayerAccount;
import com.ibm.scas.analytics.resources.BaseResource;
import com.ibm.scas.analytics.resources.exceptions.ResourceBadRequestException;
import com.ibm.scas.analytics.resources.exceptions.ResourceInternalErrorException;
import com.ibm.scas.analytics.utils.CPEException;

@Path("/softlayeraccount")
public class SoftLayerAccountResource extends BaseResource {

	@Context
	UriInfo uriInfo;
	@Context
	HttpServletRequest request;
	@Context
	ServletContext context;
	
	@Inject
	SoftLayerAccountProvider slAccountProvider;
	
	@Inject ObjectMapper objectMapper;

	private final static Logger logger = Logger.getLogger(SoftLayerAccountResource.class);
	
	/**
	 * POST /softlayeraccount
	 * 
	 * @return
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({ MediaType.APPLICATION_JSON })
	@Transactional
	public Response createSoftlayerAccount(@HeaderParam("api-key") String apiKey, SoftLayerAccount softlayerAccount) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			
			this.validateAPIKey(null, apiKey);
			
		    final String newId = slAccountProvider.createSoftLayerAccount(softlayerAccount);
		    
			return createOKResponse(new BaseResult("OK", newId));
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(String.format("Failed to add SoftLayer Account: %s", e.getLocalizedMessage()), e);
		} catch (RuntimeException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceBadRequestException(e);
		}
	}
	
	/**
	 * GET /softlayeraccount
	 * 
	 * @return
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getSoftLayerAccountDetails(@HeaderParam("api-key") String apiKey) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			
			this.validateAPIKey(null, apiKey);
			
			final List<SoftLayerAccount> softlayerAccounts = slAccountProvider.getSoftLayerAccountDetails();
			return createOKResponse(softlayerAccounts);
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e);
		}
	}		
	
	/**
	 * GET /softlayeraccount/{accountId}
	 * 
	 * @return
	 */
	@GET
	@Path("/{accountId}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getSoftLayerAccountDetailsById(@HeaderParam("api-key") String apiKey, @PathParam("accountId") String id) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			
			this.validateAPIKey(null, apiKey);
			
			SoftLayerAccount softlayerAccount = slAccountProvider.getSoftLayerAccountById(id);
			return createOKResponse(softlayerAccount);
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e);
		}
	}		

	/**
	 * DELETE /softlayeraccount/{accountId}
	 * 
	 * @return
	 */
	@Path("/{accountId}")
	@DELETE
	@Produces({ MediaType.APPLICATION_JSON })
	@Transactional
	public Response deleteSoftlayerAccount(@HeaderParam("api-key") String apiKey, @PathParam("accountId") String id) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			
			this.validateAPIKey(null, apiKey);
			
		    slAccountProvider.deleteSoftLayerAccount(id);
			return createOKResponse(new BaseResult("OK", "SoftLayer Account deleted"));
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException("Failed to delete SoftLayer Account", e);
		} catch (RuntimeException e) {
			logger.error(e.getLocalizedMessage(), e);
			return createResponse(Status.BAD_REQUEST, e.getMessage());
		}
	}
	
}
