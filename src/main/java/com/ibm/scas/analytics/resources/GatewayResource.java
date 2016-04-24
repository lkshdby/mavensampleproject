package com.ibm.scas.analytics.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.google.inject.persist.Transactional;
import com.ibm.scas.analytics.BaseResult;
import com.ibm.scas.analytics.backend.NotAuthorizedException;
import com.ibm.scas.analytics.beans.Account;
import com.ibm.scas.analytics.beans.Gateway;
import com.ibm.scas.analytics.beans.Gateway.GatewayType;
import com.ibm.scas.analytics.beans.GatewayMember;
import com.ibm.scas.analytics.beans.Subscriber;
import com.ibm.scas.analytics.beans.VPNTunnel;
import com.ibm.scas.analytics.resources.exceptions.ResourceBadRequestException;
import com.ibm.scas.analytics.resources.exceptions.ResourceInternalErrorException;
import com.ibm.scas.analytics.resources.exceptions.ResourceNotFoundException;
import com.ibm.scas.analytics.resources.exceptions.ResourcePreconditionFailedException;
import com.ibm.scas.analytics.resources.exceptions.ResourceUnauthorizedException;
import com.ibm.scas.analytics.utils.CPEException;
import com.ibm.scas.analytics.utils.CPEParamException;

@Path("/gateway")
public class GatewayResource extends BaseResource {

	@Context
	UriInfo uriInfo;
	@Context
	HttpServletRequest request;
	@Context
	ServletContext context;

	private final static Logger logger = Logger.getLogger(GatewayResource.class);

	/* ADMIN ONLY - START */
	
	@Path("/{gatewayId}/getSSLCert")
	/**
	 * GET /gateway
	 * Get the SSL Certificates for gateway in database
	 * 
	 * @return
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getGatewaySSLCert(@HeaderParam("api-key") String apiKey, @PathParam("gatewayId") String gatewayId) throws Exception {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			
			this.validateAPIKey(null, apiKey);
			
			final Gateway gateway = networkDetailProvider.getGatewayById(gatewayId);
			if (gateway == null) {
				throw new ResourceNotFoundException(gatewayId);
			}
			
			final Map<String,String> sslCert = networkDetailProvider.getGatewaySSLCertFromDB(gatewayId);
			
			return createOKResponse(new BaseResult("OK", sslCert));
		} catch (NotAuthorizedException e) {
			throw new ResourceUnauthorizedException();
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			String msg = e.getMessage();
			if ((msg.toLowerCase().contains("gateway")) &&  
				(msg.toLowerCase().contains("not found"))) {
				throw new ResourceNotFoundException(msg);				
			}
			throw new ResourceInternalErrorException(e);
		}
	}
	
	/**
	 * POST /gateway
	 * 
	 * @return
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({ MediaType.APPLICATION_JSON })
	@Transactional
	public Response createGateway(@HeaderParam("api-key") String apiKey, Gateway gateway) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			
			this.validateAPIKey(null, apiKey);
			
			final String gatewayId = networkDetailProvider.addGateway(gateway);
			return createOKResponse(new BaseResult("OK", gatewayId));
		} catch (NotAuthorizedException e) {
			throw new ResourceUnauthorizedException();
		} catch (CPEParamException e) {
			throw new ResourceBadRequestException(e.getLocalizedMessage(), e);
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			String msg = e.getMessage();
			if (msg.toLowerCase().contains("could not find location record")) {
				throw new ResourceNotFoundException(msg);
			}
			else if ((msg.toLowerCase().contains("subscriber with id")) && 
					(msg.toLowerCase().contains("was not found")))	{
				throw new ResourceUnauthorizedException(msg);
			}			
			else if (msg.toLowerCase().contains("could not find gateway")) {
				throw new ResourceNotFoundException(msg);
			}
			else if (msg.toLowerCase().contains("already exists")) {
				throw new ResourcePreconditionFailedException(msg);
			}
			else if (msg.toLowerCase().contains("unable to authenticate any of the credentials stored on softLayer with vyatta")) {
				throw new ResourceUnauthorizedException(msg);				
			}
			else if (msg.toLowerCase().contains("unable to authenticate credentials with vyatta")) {
				throw new ResourceUnauthorizedException(msg);								
			}
			else if ((msg.toLowerCase().contains("cannot assign")) && 
					(msg.toLowerCase().contains("gateway to a subscriber")))	{
				throw new ResourcePreconditionFailedException(msg);
			}
			throw new ResourceInternalErrorException("Failed to add Gateway", e);
		} catch (RuntimeException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException("Failed to add Gateway", e);
		}
	}
	
	
	/**
	 * DELETE /gateway/{gatewayId}
	 * 
	 * @param id
	 * @return
	 */
	@DELETE
	@Path("/{gatewayId}")
	@Produces({ MediaType.APPLICATION_JSON })
	@Transactional
	public Response deleteGateway(@HeaderParam("api-key") String apiKey, @PathParam("gatewayId") String gatewayId) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			
			this.validateAPIKey(null, apiKey);
			
			networkDetailProvider.deleteGateway(gatewayId);
			return createOKResponse(new BaseResult("OK"));
		} catch (NotAuthorizedException e) {
			throw new ResourceUnauthorizedException();
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			String msg = e.getMessage();
			if ((msg.toLowerCase().contains("cannot delete gateway")) && 
				(msg.toLowerCase().contains("while there are attached vlans"))) {
				throw new ResourcePreconditionFailedException(msg);				
			}
			else if ((msg.toLowerCase().contains("cannot delete gateway")) && 
					(msg.toLowerCase().contains("because associated vlan"))) {
				throw new ResourcePreconditionFailedException(msg);				
			}
			return createResponse(Status.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}
	
	@Path("/{gatewayId}/testCredentials")
	/**
	 * @PUT /gateway/{gatewayId}
	 * 
	 * @return
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({ MediaType.APPLICATION_JSON })
	@Transactional
	public Response testGatewayUserPassword(@HeaderParam("api-key") String apiKey, @PathParam("gatewayId") String gatewayId) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			
			this.validateAPIKey(null, apiKey);
			
			final Gateway gateway = networkDetailProvider.getGatewayById(gatewayId);
			
			if (gateway == null) {
				throw new ResourceNotFoundException();
			}
			
			networkDetailProvider.testGatewayUserPassword(gatewayId);
			return createOKResponse(new BaseResult("OK", ""));
		} catch (CPEParamException e) {
			throw new ResourceBadRequestException(e.getLocalizedMessage(), e);		
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			String msg = e.getMessage();
			if (msg.toLowerCase().contains("cannot find gateway with id")) {
				throw new ResourceNotFoundException(msg);				
			}
			else if (msg.toLowerCase().contains("unable to authenticate credentials with vyatta")) {
				throw new ResourceUnauthorizedException(msg);				
			}
			throw new ResourceInternalErrorException(e.getLocalizedMessage(), e);
		}
	}	
	
	@Path("/{gatewayId}/updateCredentials")
	/**
	 * @PUT /gateway/{gatewayId}/updateCredentials
	 * 
	 * @return
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({ MediaType.APPLICATION_JSON })
	@Transactional
	public Response modifyGatewayUserPassword(@HeaderParam("api-key") String apiKey, @PathParam("gatewayId") String gatewayId, Gateway gatewayReq) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			
			this.validateAPIKey(null, apiKey);
			
			final Gateway gateway = networkDetailProvider.getGatewayById(gatewayId);
			if (gateway == null) {
				throw new ResourceNotFoundException(gatewayId);
			}

			if (gatewayReq != null) {
				// same user/password for both members
				if (gatewayReq.getUsername() != null) {
					if (gatewayReq.getPassword() == null) {
						throw new ResourceBadRequestException(String.format("Missing parameter: password"));
					}		
				}

				// if username/password is passed in
				if (gatewayReq.getGatewayMembers() != null && !gatewayReq.getGatewayMembers().isEmpty()) {
					// setting user/password for one or both members -- all members passed in must have a username/password set
					for (final GatewayMember member : gatewayReq.getGatewayMembers()) {
						if (member.getMemberIp() == null) {
							throw new ResourceBadRequestException(String.format("Missing parameter: member.memberIp"));
						}				
						if (member.getUsername() == null) {
							throw new ResourceBadRequestException(String.format("Missing parameter: member.username"));
						}
						if (member.getPassword() == null) {
							throw new ResourceBadRequestException(String.format("Missing parameter: member.password"));
						}				
					}
				}
			}
			
			// else: no username/password is passed in, go into the SL API to test the password combination there
			networkDetailProvider.updateGatewayUserPassword(gatewayId, gatewayReq);
			
			return createOKResponse(new BaseResult("OK", String.format("Gateway %s (%s) username and password updated", gateway.getId(), gateway.getName())));
		} catch (CPEParamException e) {
			throw new ResourceBadRequestException(e.getLocalizedMessage(), e);		
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e.getLocalizedMessage(), e);
		}
	}	
	
	@Path("/{gatewayId}/assign")
	/**
	 * @PUT /gateway/{gatewayId}
	 * 
	 * @return
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({ MediaType.APPLICATION_JSON })
	@Transactional
	public Response modifyGatewaySubscriber(@HeaderParam("api-key") String apiKey, @PathParam("gatewayId") String gatewayId, Gateway gatewayReq) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			
			this.validateAPIKey(null, apiKey);
			
			final Gateway gateway = networkDetailProvider.getGatewayById(gatewayId);
			if (gateway == null) {
				throw new ResourceNotFoundException(gatewayId);
			}
			
			if (gatewayReq.getAccount() == null || gatewayReq.getAccount().getId() == null) {
				throw new ResourceBadRequestException("Missing parameter: account.id");
			}
			
			final Account account = tenantService.getAccountById(gatewayReq.getAccount().getId());
			if (account == null) {
				throw new ResourceBadRequestException(String.format("Account with id %s was not found in the database", gatewayReq.getAccount().getId()));
			}
			
			// update the subscriber
			networkDetailProvider.updateGatewayAccount(gatewayId, gatewayReq.getAccount().getId());

			return createOKResponse(new BaseResult("OK", String.format("Gateway %s (%s) assigned to subscriber %s", gateway.getId(), gateway.getName(), account.getId())));
		} catch (CPEParamException e) {
			throw new ResourceBadRequestException(e.getLocalizedMessage(), e);
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			String msg = e.getMessage();
			if ((msg.toLowerCase().contains("gateway")) &&  
				(msg.toLowerCase().contains("does not exist"))) {
				throw new ResourceNotFoundException(msg);				
			}
			else if ((msg.toLowerCase().contains("gateway")) &&  
					(msg.toLowerCase().contains("is not of type dedicated"))) {
				throw new ResourcePreconditionFailedException(msg);				
			}
			else if ((msg.toLowerCase().contains("Cannot modify gateway")) && 
					(msg.toLowerCase().contains("because associated vlan"))) {
				throw new ResourcePreconditionFailedException(msg);				
			}
			throw new ResourceInternalErrorException(e.getLocalizedMessage(), e);
		}
	}	
	
	@Path("/{gatewayId}/unassign")
	/**
	 * @PUT /gateway/{gatewayId}
	 * 
	 * @return
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({ MediaType.APPLICATION_JSON })
	@Transactional
	public Response unassignGateway(@HeaderParam("api-key") String apiKey, @PathParam("gatewayId") String gatewayId) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			
			this.validateAPIKey(null, apiKey);
			
			final Gateway gateway = networkDetailProvider.getGatewayById(gatewayId);
			if (gateway == null) {
				throw new ResourceNotFoundException(gatewayId);
			}
			
			networkDetailProvider.unassignGateway(gatewayId);
		
			return createOKResponse(new BaseResult("OK", String.format("Gateway %s (%s) unassigned from account %s", gateway.getId(), gateway.getName(), gateway.getAccount().getId())));
		
		} catch (CPEParamException e) {
			throw new ResourceBadRequestException(e.getLocalizedMessage(), e);
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e.getLocalizedMessage(), e);
		}
	}
	
	@Path("/{gatewayId}/updateSSLCert")
	/**
	 * PUT /gateway/{gatewayId}/updateSSLCert
	 * Update the SSL Certificates for gateway in database
	 * 
	 * @return
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON })
	@Transactional
	public Response updateGatewaySSLCert(@HeaderParam("api-key") String apiKey, @PathParam("gatewayId") String gatewayId) throws Exception {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			
			this.validateAPIKey(null, apiKey);
			
			final Gateway gateway = networkDetailProvider.getGatewayById(gatewayId);
			if (gateway == null) {
				throw new ResourceNotFoundException(gatewayId);
			}
			
			networkDetailProvider.updateGatewaySSLCert(gatewayId);
			
			return createOKResponse(new BaseResult("OK"));
		} catch (NotAuthorizedException e) {
			throw new ResourceUnauthorizedException();
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e.getLocalizedMessage(), e);
		}
	}
	
	/* ADMIN ONLY - END */

	/**
	 * GET /gateway
	 * 
	 * @return
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getGatewayDetails(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			
			this.validateAPIKey(subscriberId, apiKey);
			
			List<Gateway> gateways = new ArrayList<Gateway>(); 
			
			if (subscriberId == null) {
				//Listing all gateways as user is Admin
				logger.trace("Listing All Gateways");
				gateways = networkDetailProvider.getAllGateways();
			} else {
				Subscriber subscriber = tenantService.getSubscriberById(subscriberId);
				if(subscriber == null)
				{
					throw new ResourceUnauthorizedException();
				}
				
				String accountId = subscriber.getAccount().getId();
				
				logger.trace("Listing All Gateways for accountId : " + accountId);
				gateways = networkDetailProvider.getAccountGateways(accountId);
			}
			
			for (final Gateway gateway : gateways) {
				// do this here instead of in the service class to avoid infinite loop
				gateway.setVpnTunnels(firewallService.getVPNTunnels(gateway.getId()));
			}
			return createOKResponse(gateways);
		} catch (NotAuthorizedException e) {
			throw new ResourceUnauthorizedException();
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			return createResponse(Status.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}
	
	@Path("/{gatewayId}")
	/**
	 * GET /gateway
	 * 
	 * @return
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getGatewayDetail(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey, @PathParam("gatewayId") String gatewayId) throws Exception {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			
			this.validateAPIKey(subscriberId, apiKey);
			
			final Gateway gateway = networkDetailProvider.getGatewayById(gatewayId);
			if (gateway == null) {
				throw new ResourceNotFoundException(gatewayId);
			}
			
			isAuthorizedForGateway(subscriberId, gateway);
			
			// do this here instead of in the service class to avoid infinite loop
			gateway.setVpnTunnels(firewallService.getVPNTunnels(gateway.getId()));
			
			return createOKResponse(gateway);
			
		} catch (NotAuthorizedException e) {
			throw new ResourceUnauthorizedException();
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			String msg = e.getMessage();
			if (msg.toLowerCase().contains("cannot retrieve list of gateways from softLayer account with id")) {
				throw new ResourceUnauthorizedException(msg);				
			}
			throw new ResourceInternalErrorException(e);
		}
	}	
	
	@Path("/{gatewayId}/addVPNTunnel")
	/**
	 * GET /gateway
	 * 
	 * @return
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Response addVPNTunnel(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey, @PathParam("gatewayId") String gatewayId, VPNTunnel tunnel) throws Exception {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			
			this.validateAPIKey(subscriberId, apiKey);
			
			final Gateway gateway = networkDetailProvider.getGatewayById(gatewayId);
			
			if (gateway == null) {
				throw new ResourceNotFoundException(gatewayId);
			}
			
			isAuthorizedForGateway(subscriberId, gateway);
			
			firewallService.addVPNTunnelToGateway(gatewayId, tunnel);

			return createOKResponse("OK");
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			String msg = e.getMessage();
			if (msg.toLowerCase().contains("cannot retrieve list of gateways from softLayer account with id")) {
				throw new ResourceUnauthorizedException(msg);				
			}
			throw new ResourceInternalErrorException(e);
		}
	}	
	
	@Path("/{gatewayId}/applyDefaultFirewall")
	/**
	 * PUT /gateway/{gatewayId}/applyDefaultFirewall
	 * Apply default firewall rules to the gateway.
	 * 
	 * @return
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON })
	@Transactional
	public Response applyDefaultFirewall(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey, @PathParam("gatewayId") String gatewayId) throws Exception {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			
			this.validateAPIKey(null, apiKey);
			
			final Gateway gateway = networkDetailProvider.getGatewayById(gatewayId);
			if (gateway == null) {
				throw new ResourceNotFoundException(gatewayId);
			}
			
			if (gateway.getType() == GatewayType.MANAGEMENT) {
				throw new ResourceBadRequestException(String.format("Cannot apply default firewall rules to management gateway %s", gatewayId));
			}
			
			isAuthorizedForGateway(subscriberId, gateway);
			
			firewallService.addDefaultFirewallRules(gateway);
			
			return createOKResponse(new BaseResult("OK"));
		} catch (NotAuthorizedException e) {
			throw new ResourceUnauthorizedException();
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e.getLocalizedMessage(), e);
		}
	}
	
	@Path("/{gatewayId}/reloadOS")
	/**
	 * PUT /gateway/{gatewayId}/reloadOS
	 * Reloads the OS on the gateway.
	 * 
	 * @return
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON })
	@Transactional
	public Response reloadOS(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey, @PathParam("gatewayId") String gatewayId) throws Exception {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			
			this.validateAPIKey(null, apiKey);
			
			final Gateway gateway = networkDetailProvider.getGatewayById(gatewayId);
			if (gateway == null) {
				throw new ResourceNotFoundException(gatewayId);
			}
			
			if (gateway.getType() == GatewayType.MANAGEMENT || gateway.getType() == GatewayType.SHARED) {
				throw new ResourceBadRequestException(String.format("Cannot reload OS of gateway %s because it is of type %s", gatewayId, gateway.getType().name()));
			}
			
			isAuthorizedForGateway(subscriberId, gateway);
			
			networkDetailProvider.osReload(gatewayId);
			
			return createOKResponse(new BaseResult("OK"));
		} catch (NotAuthorizedException e) {
			throw new ResourceUnauthorizedException();
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e.getLocalizedMessage(), e);
		}
	}
	
	// Caller should call validateApiKey before call this method.
	private void isAuthorizedForGateway(String subscriberId, Gateway gateway) throws ResourceUnauthorizedException, CPEException {
		// if subscriber Id is null then it is admin case.
		if (subscriberId == null)
			return;

		Subscriber subscriber = tenantService.getSubscriberById(subscriberId);
		if(subscriber == null) {
			throw new ResourceUnauthorizedException();
		}
		
		// if gateway does not belong to the subscriber's account then throw an exception;
		if(!subscriber.getAccount().getId().equals(gateway.getAccount().getId())) {
			throw new ResourceUnauthorizedException();
		}
		
		return;
	}
}