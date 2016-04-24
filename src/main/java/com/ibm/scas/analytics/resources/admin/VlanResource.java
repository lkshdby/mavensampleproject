package com.ibm.scas.analytics.resources.admin;

import java.util.List;

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
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.ibm.scas.analytics.BaseResult;
import com.ibm.scas.analytics.backend.NetworkService;
import com.ibm.scas.analytics.backend.NotAuthorizedException;
import com.ibm.scas.analytics.backend.monitoring.NagiosEventLogger;
import com.ibm.scas.analytics.beans.Gateway;
import com.ibm.scas.analytics.beans.Vlan;
import com.ibm.scas.analytics.resources.BaseResource;
import com.ibm.scas.analytics.resources.exceptions.ResourceBadRequestException;
import com.ibm.scas.analytics.resources.exceptions.ResourceInternalErrorException;
import com.ibm.scas.analytics.resources.exceptions.ResourceNotFoundException;
import com.ibm.scas.analytics.resources.exceptions.ResourceUnauthorizedException;
import com.ibm.scas.analytics.resources.exceptions.ResourcePreconditionFailedException;
import com.ibm.scas.analytics.utils.CPEException;
import com.ibm.scas.analytics.utils.CPEParamException;

@Path("/vlan")
public class VlanResource extends BaseResource {
	@Context
	UriInfo uriInfo;
	@Context
	HttpServletRequest request;
	@Context
	ServletContext context;

	@Inject NetworkService networkDetailProvider;
	
	private final static Logger logger = Logger
			.getLogger(VlanResource.class);
	/**
	 * GET /vlan
	 * Get all the vlan details
	 * @return
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAllVlanDetails(@HeaderParam("api-key") String apiKey) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			
			this.validateAPIKey(null, apiKey);
			
			final List<Vlan> vlans = networkDetailProvider.getAllVlans();
			return createOKResponse(vlans);
		} catch (NotAuthorizedException e) {
			throw new ResourceUnauthorizedException();
		} catch (CPEException e) {
			throw new ResourceInternalErrorException(e.getLocalizedMessage(), e);
		}
	}
	
	/**
	 * POST /vlan
	 * 
	 * @return
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Response createVlan(@HeaderParam("api-key") String apiKey, Vlan vlan) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			
			this.validateAPIKey(null, apiKey);
			
			final String newVlanId = networkDetailProvider.addVlan(vlan);
			return createOKResponse(new BaseResult("OK", newVlanId));
		} catch (NotAuthorizedException e) {
			throw new ResourceUnauthorizedException();
		} catch (CPEParamException e) {
			throw new ResourceBadRequestException(e.getLocalizedMessage());
		} catch (CPEException e) {
			String msg = e.getMessage();
			if (msg.toLowerCase().contains("could not find location record")) {
				throw new ResourceNotFoundException(msg);
			}
			else if ((msg.toLowerCase().contains("softLayer account with id")) && 
					(msg.toLowerCase().contains("was not found")))	{
				throw new ResourcePreconditionFailedException(msg);
			}
			else if (msg.toLowerCase().contains("could not find vlan")) {
				throw new ResourceNotFoundException(msg);				
			}
			else if (msg.toLowerCase().contains("vlan is already associated with gateway")) {
				throw new ResourcePreconditionFailedException(msg);	
			}
			throw new ResourceInternalErrorException(e.getLocalizedMessage(), e);
		}catch (RuntimeException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e.getLocalizedMessage(), e);
		}
	}
	
	
	/**
	 * DELETE /vlan/{vlanId}
	 * 
	 * @param id
	 * @return
	 */
	@DELETE
	@Path("/{vlanId}")
	@Produces({ MediaType.APPLICATION_JSON })
	@Transactional
	public Response deleteVlan(@HeaderParam("api-key") String apiKey, @PathParam("vlanId") String vlanId) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			
			this.validateAPIKey(null, apiKey);
			
			networkDetailProvider.deleteVlan(vlanId);
			return createOKResponse(new BaseResult("OK"));
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			String msg = e.getMessage();
			if (msg.toLowerCase().contains("does not exist in the database")) {
				throw new ResourceNotFoundException(msg);				
			} 
			else if ((msg.toLowerCase().contains("is assigned")) && 
					(msg.toLowerCase().contains("unassign it first"))) {
				throw new ResourcePreconditionFailedException(msg);
			} 
			else if (msg.toLowerCase().contains("cannot remove vlan")) {
				throw new ResourcePreconditionFailedException(msg);				
			}
			throw new ResourceInternalErrorException(e.getLocalizedMessage(), e);
		}
	}

	@Path("/{vlanid}/detach")
	/**
	 * @PUT /vlan/{vlanid}/detach
	 * 
	 * @return
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({ MediaType.APPLICATION_JSON })
	@Transactional
	public Response detachVlanFromGateway(@HeaderParam("api-key") String apiKey, @PathParam("vlanid") String vlanid) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			
			this.validateAPIKey(null, apiKey);
			
			final Vlan vlan = networkDetailProvider.getVlanById(vlanid);
			if (vlan == null) {
				throw new ResourceNotFoundException();
			}
			
			if (vlan.getGateway() == null) {
				throw new ResourceBadRequestException(String.format("Vlan %s (%s.%s) is not attached to a gateway.", vlan.getId(), vlan.getPrimaryRouter(), vlan.getVlanNumber()));
			}
			
			final Gateway gateway = networkDetailProvider.getGatewayById(vlan.getGateway().getId());
			if (gateway == null) {
				throw new ResourceBadRequestException(String.format("Could not find gateway %s that Vlan %s (%s.%s) is attached to.", vlan.getGateway().getId(), vlan.getId(), vlan.getPrimaryRouter(), vlan.getVlanNumber()));
			}
			
			// delete the VIF on the Vyatta
			try {
				firewallService.removeVif(gateway, vlan);
			} catch (CPEException e) {
				new NagiosEventLogger().emitCritical("NAGIOSMONITOR VYATTACONFIG :"+e.getLocalizedMessage());
				throw e;
			}

			String emessage = String.format("Removed VLAN configuration for %s (%s.%s) from gateway %s (%s).", vlan.getId(), vlan.getPrimaryRouter(), vlan.getVlanNumber(), gateway.getId(), gateway.getName()); 
			logger.info(emessage);
			new NagiosEventLogger().emitOK("NAGIOSMONITOR VYATTACONFIG :"+emessage);
			
			networkDetailProvider.detachVlanFromGateway(vlanid, false);
			return createOKResponse(new BaseResult("OK", String.format("Vlan %s (%s.%s) was removed from gateway %s (%s).", vlanid, vlan.getPrimaryRouter(), vlan.getVlanNumber(), vlan.getGateway().getId(), gateway.getName())));

		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			String msg = e.getMessage();
			if (msg.toLowerCase().contains("does not exist in the database")) {
				throw new ResourceNotFoundException(msg);				
			} 
			else if (msg.toLowerCase().contains("is used by")) {
				throw new ResourcePreconditionFailedException(msg);
			}
			throw new ResourceInternalErrorException(e.getLocalizedMessage(), e);
		}
	}
	
	@Path("/{vlanid}/untrunk")
	/**
	 * @PUT /vlan/{vlanid}/untrunk
	 * 
	 * @return
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({ MediaType.APPLICATION_JSON })
	@Transactional
	public Response untrunkVlanFromAllHosts(@HeaderParam("api-key") String apiKey, @PathParam("vlanid") String vlanid) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			
			this.validateAPIKey(null, apiKey);
			
			final Vlan vlan = networkDetailProvider.getVlanById(vlanid);
			if (vlan == null) {
				throw new ResourceNotFoundException();
			}
		
			networkDetailProvider.removeNetworkVlanTrunks(vlanid, false);
			
			return createOKResponse(new BaseResult("OK", String.format("Vlan %s (%s.%s) was from hosts: %s.", vlanid, vlan.getPrimaryRouter(), vlan.getVlanNumber(), vlan.getVlanTrunks())));

		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			String msg = e.getMessage();
			if (msg.toLowerCase().contains("cannot find vlan with id")) {
				throw new ResourceNotFoundException(msg);								
			}
			throw new ResourceInternalErrorException(e.getLocalizedMessage(), e);
		}
	}
	
	/**
	 * GET /vlan/{vlanid}
	 * 
	 * @return
	 */
	@GET
	@Path("/{vlanid}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getVlanDetailsById(@HeaderParam("api-key") String apiKey, @PathParam("vlanid") String vlanid) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			
			this.validateAPIKey(null, apiKey);
			
			Vlan vlan = networkDetailProvider.getVlanById(vlanid);
			if (vlan == null) {
				throw new ResourceNotFoundException(vlanid);
			}
			return createOKResponse(vlan);
		} catch (CPEException e) {
			throw new ResourceInternalErrorException(e.getLocalizedMessage(), e);
		}
	}
}
