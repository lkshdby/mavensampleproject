package com.ibm.scas.analytics.resources;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;

import com.google.gson.Gson;
import com.google.inject.persist.Transactional;
import com.ibm.scas.analytics.BaseResult;
import com.ibm.scas.analytics.backend.NotAuthorizedException;
import com.ibm.scas.analytics.beans.Cluster;
import com.ibm.scas.analytics.beans.Gateway;
import com.ibm.scas.analytics.beans.Subscriber;
import com.ibm.scas.analytics.beans.VPNTunnel;
import com.ibm.scas.analytics.persistence.beans.CPELocation;
import com.ibm.scas.analytics.resources.exceptions.ResourceInternalErrorException;
import com.ibm.scas.analytics.resources.exceptions.ResourceNotFoundException;
import com.ibm.scas.analytics.resources.exceptions.ResourceUnauthorizedException;
import com.ibm.scas.analytics.utils.CPEException;
import com.ibm.scas.analytics.utils.SSLUtils;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;

@Path("/tunnels")
public class VPNTunnelResource extends BaseResource {
	private final static Logger logger = Logger.getLogger(ClusterResource.class);

	/**
	 * GET /tunnels
	 * 
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response listTunnels(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey, @HeaderParam("cpe-location") String cpeLocation) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			subscriberId = (subscriberId == null ? getStringAttrValueFromSession("subscriberId") : subscriberId);
	
			this.validateAPIKey(subscriberId, apiKey);
	
			final List<Cluster> clusters = new ArrayList<Cluster>();
	
			if (cpeLocation == null || cpeLocation.equals(myLocationName)) {
				if (subscriberId == null) {
					clusters.addAll(engine.listAllClusters());
				} else {
					clusters.addAll(engine.listClustersInAccount(subscriberId));
				}
			}
			
			if (isHub) {
				final List<CPELocation> cpeLocations = persistence.getAllObjects(CPELocation.class);
				
				for (CPELocation c : cpeLocations) {
					// Continue if cpe location is my location because we already retrieved my clusters above.
					if (c.getName().equals(myLocationName))
						continue;
					
					// If cpe location passed in header is null then we want to call all the spokes
					// and get their clusters.
					// If cpe location passed in header is not null then only call the spoke
					// for the passed in cpe location.
					if (cpeLocation == null || cpeLocation.equals(c.getName())) {
						final String spokeUrl = c.getUrl() + "/tunnels";
						
						final ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
						config.getClasses().add(JacksonJaxbJsonProvider.class);
						final Client client = Client.create(config);
						final WebResource resource = client.resource(spokeUrl);
						
						final List<Cluster> spokeClusters = resource.accept(MediaType.APPLICATION_JSON_TYPE).header("subscriber-id", subscriberId).header("api-key", adminApiKey).get(new GenericType<List<Cluster>>() {});
						
						clusters.addAll(spokeClusters);
					}
				}
			}
			
			final List<VPNTunnel> tunnels = new ArrayList<VPNTunnel>();

			for (Cluster cluster : clusters) {
				final VPNTunnel tunnel = cluster.getVpnTunnel();
				if (tunnel == null) {
					// no tunnel in this record
					continue;
				}
				
				tunnels.add(firewallService.getVPNTunnel(tunnel.getId(), true));
			}
			
			return createOKResponse(tunnels);
		} catch (NotAuthorizedException e) {
			throw new ResourceUnauthorizedException(e.getLocalizedMessage());						
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e);
		}
	}
	
	/**
	 * GET /tunnels/{tunnelId}
	 * 
	 * @return
	 */
	@GET
	@Path("/{tunnelId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTunnelDetails(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey, @HeaderParam("cpe-location") String cpeLocation, @PathParam("tunnelId") String tunnelId) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			subscriberId = (subscriberId == null ? getStringAttrValueFromSession("subscriberId") : subscriberId);
	
			this.validateAPIKey(subscriberId, apiKey);
	
			VPNTunnel tunnel = null;
	
			if (cpeLocation == null || cpeLocation.equals(myLocationName)) {
				tunnel = this.firewallService.getVPNTunnel(tunnelId, true);

				if (tunnel == null)
					throw new ResourceNotFoundException();
				
				final Gateway gateway = this.networkDetailProvider.getGatewayById(tunnel.getGatewayId());
				String isDedicatedGatewayStr = gateway.getAccount().getProperties().get("dgw");
				
				final Subscriber subscriber = tenantService.getSubscriberById(subscriberId);
				
				if (subscriber != null && !subscriber.getAccount().getId().equals(gateway.getAccount().getId())) {
					throw new ResourceUnauthorizedException();
				} else if(isDedicatedGatewayStr != null && isDedicatedGatewayStr.equalsIgnoreCase("true")){
					throw new ResourceInternalErrorException("VPN Tunnel is not associated with dedicated gateway!");
				}
			} else {
				if (isHub) {
					final CPELocation c = persistence.getObjectById(CPELocation.class, cpeLocation);
					final String spokeUrl = c.getUrl() + "/tunnels/" + tunnelId;
					
					final ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
					config.getClasses().add(JacksonJaxbJsonProvider.class);
					final Client client = Client.create(config);
					final WebResource resource = client.resource(spokeUrl);
					
					tunnel = resource.accept(MediaType.APPLICATION_JSON_TYPE).header("subscriber-id", subscriberId).header("api-key", adminApiKey).get(new GenericType<VPNTunnel>() {});
				}
			}
			
			return createOKResponse(tunnel);
		} catch (NotAuthorizedException e) {
			throw new ResourceUnauthorizedException(e.getLocalizedMessage());						
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e);
		}
	}
	
	/**
	 * PUT /tunnels/{tunnelId}
	 * 
	 * @return
	 */
	@PUT
	@Path("/{tunnelId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Response modifyTunnel(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey, @HeaderParam("cpe-location") String cpeLocation, @PathParam("tunnelId") String tunnelId, VPNTunnel tunnel) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			subscriberId = (subscriberId == null ? getStringAttrValueFromSession("subscriberId") : subscriberId);
	
			this.validateAPIKey(subscriberId, apiKey);
			boolean success = false;
			
			if (cpeLocation == null || cpeLocation.equals(myLocationName)) {
				final VPNTunnel existingTunnel = firewallService.getVPNTunnel(tunnelId, true);
				if (existingTunnel == null) {
					throw new ResourceNotFoundException();
				}
				
				final String gatewayId = existingTunnel.getGatewayId();
				final Gateway gateway = this.networkDetailProvider.getGatewayById(gatewayId);
				String isDedicatedGatewayStr = gateway.getAccount().getProperties().get("dgw");
				final Subscriber subscriber = tenantService.getSubscriberById(subscriberId);
				
				if (subscriber != null && !subscriber.getAccount().getId().equals(gateway.getAccount().getId())) {
					throw new ResourceUnauthorizedException();
				} else if(isDedicatedGatewayStr != null && isDedicatedGatewayStr.equalsIgnoreCase("true")){
					throw new ResourceInternalErrorException("VPN Tunnel is not associated with dedicated gateway!");
				}

				this.firewallService.updateVPNTunnel(tunnelId, tunnel);
				
				success = true;
			} else {
				if (isHub) {
					final CPELocation c = persistence.getObjectById(CPELocation.class, cpeLocation);
					final String spokeUrl = c.getUrl() + "/tunnels/" + tunnelId;
					
					final ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
					config.getClasses().add(JacksonJaxbJsonProvider.class);
					final Client client = Client.create(config);
					final WebResource resource = client.resource(spokeUrl);
					
					final Gson gson = new Gson();
					final String tunnelReq = gson.toJson(tunnel);
					final String resp = resource.accept(MediaType.APPLICATION_JSON_TYPE).header("subscriber-id", subscriberId).header("api-key", adminApiKey).put(String.class, tunnelReq);

					success = true;
				}
			}
			

			if (success) {
				return createOKResponse(new BaseResult("OK", "Cluster modified"));
			} else {
				throw new ResourceInternalErrorException("Failed to modify cluster");
			}
		} catch (NotAuthorizedException e) {
			throw new ResourceUnauthorizedException(e.getLocalizedMessage());						
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e);
		}
	}
}
