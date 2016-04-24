package com.ibm.scas.analytics.resources.admin;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.ibm.scas.analytics.backend.NotAuthorizedException;
import com.ibm.scas.analytics.beans.IPAddress;
import com.ibm.scas.analytics.beans.Subnet;
import com.ibm.scas.analytics.resources.BaseResource;
import com.ibm.scas.analytics.resources.exceptions.ResourceNotFoundException;
import com.ibm.scas.analytics.resources.exceptions.ResourceUnauthorizedException;
import com.ibm.scas.analytics.utils.CPEException;

@Path("/subnet")
public class SubnetResource extends BaseResource {
	@Context
	UriInfo uriInfo;
	@Context
	HttpServletRequest request;
	@Context
	ServletContext context;

	
	private final static Logger logger = Logger.getLogger(SubnetResource.class);
	
	/**
	 * GET /subnet
	 * 
	 * @return
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getSubnetDetails(@HeaderParam("api-key") String apiKey) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			
			this.validateAPIKey(null, apiKey);
			
			List<Subnet> subnets = networkDetailProvider.getAllSubnets();
			return createOKResponse(subnets);
		} catch (NotAuthorizedException e) {
			throw new ResourceUnauthorizedException();
		} catch (CPEException e) {
			logger.error(e);
			return createResponse(Status.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}
	
	/**
	 * GET /subnet/{subnetid}
	 * 
	 * @return
	 */
	@GET
	@Path("/{subnetid}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getSubnetDetailsById(@HeaderParam("api-key") String apiKey, @PathParam("subnetid") String subnetid) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			
			this.validateAPIKey(null, apiKey);
			
			Subnet subnet = networkDetailProvider.getSubnetById(subnetid);
			if (subnet == null) {
				throw new ResourceNotFoundException(subnetid);
			}
			return createOKResponse(subnet);
		} catch (NotAuthorizedException e) {
			throw new ResourceUnauthorizedException();
		} catch (CPEException e) {
			logger.error(e);
			return createResponse(Status.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}
	
	/**
	 * GET /subnet/{subnetid}/getIPAddrs
	 * 
	 * @return
	 */
	@GET
	@Path("/{subnetid}/getIPAddrs")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getIPAddrsForSubnet(@HeaderParam("api-key") String apiKey, @PathParam("subnetid") String subnetid) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			
			this.validateAPIKey(null, apiKey);
			
			final List<IPAddress> ipAddrs = networkDetailProvider.getIPAddressBySubnet(subnetid);
			return createOKResponse(ipAddrs);
		} catch (NotAuthorizedException e) {
			throw new ResourceUnauthorizedException();
		} catch (CPEException e) {
			logger.error(e);
			return createResponse(Status.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}
}
