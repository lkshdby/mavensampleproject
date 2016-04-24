package com.ibm.scas.analytics.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.ibm.scas.analytics.BaseResult;
import com.ibm.scas.analytics.backend.NotAuthorizedException;
import com.ibm.scas.analytics.backend.ProvisioningException;
import com.ibm.scas.analytics.backend.SoftLayerOrderProvider;
import com.ibm.scas.analytics.beans.Cluster;
import com.ibm.scas.analytics.beans.ClusterRequest;
import com.ibm.scas.analytics.beans.IPAddress;
import com.ibm.scas.analytics.beans.SoftLayerOrder;
import com.ibm.scas.analytics.beans.VPNTunnel;
import com.ibm.scas.analytics.persistence.beans.CPELocation;
import com.ibm.scas.analytics.resources.exceptions.ResourceBadRequestException;
import com.ibm.scas.analytics.resources.exceptions.ResourceInternalErrorException;
import com.ibm.scas.analytics.resources.exceptions.ResourceNotFoundException;
import com.ibm.scas.analytics.resources.exceptions.ResourceUnauthorizedException;
import com.ibm.scas.analytics.utils.CPEException;
import com.ibm.scas.analytics.utils.CPEParamException;
import com.ibm.scas.analytics.utils.ReadWriteLockTable;
import com.ibm.scas.analytics.utils.SSLUtils;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;

/**
 * Jersey handlers for the clusters REST API
 * 
 * @author Han Chen
 *
 */
@Path("/clusters")
public class ClusterResource extends BaseResource {
	private final static Logger logger = Logger.getLogger(ClusterResource.class);
	@Inject private ReadWriteLockTable<String> locktable;
	@Inject private SoftLayerOrderProvider orderProvider;
	
	public void afterInject() {
		// called after Guice instantiates me and injects my dependencies
		
	}
	
	/**
	 * GET /clusters
	 * 
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response listClustersInAccount(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey, @HeaderParam("cpe-location") String cpeLocation, @HeaderParam("dedicated-gateway") Boolean forDedicatedGateway) {
		try {
			logger.trace("listClustersInAccount: subscriberId : " + subscriberId + " , cpeLocation : " + cpeLocation);
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			subscriberId = (subscriberId == null ? getStringAttrValueFromSession("subscriberId") : subscriberId);

			this.validateAPIKey(subscriberId, apiKey);
			
			List<Cluster> clusters = new ArrayList<Cluster>();

			if (cpeLocation == null || cpeLocation.equals(myLocationName)) {
				if (subscriberId == null) {
					logger.trace("Listing All Clusters ......");
					clusters.addAll(engine.listAllClusters());
				} else {
					logger.trace("Listing All Clusters for SubscriberId : " + subscriberId);
					clusters.addAll(engine.listClustersInAccount(subscriberId));
				}
			}

			//create seperate list of dedicated gateway if call is for dedicated gateway.
			if(forDedicatedGateway!=null && forDedicatedGateway)
			{
				logger.debug("Getting List of Dedicated Gateways.......");
				List<Cluster> dedicatedGatewayCluster = new ArrayList<Cluster>();
				for(Cluster cluster : clusters)
				{
					String clusterType = cluster.getClusterParams().get(com.ibm.scas.analytics.persistence.beans.Cluster.CLUSTER_PROP_CLUSTERTYPE);

					if(com.ibm.scas.analytics.persistence.beans.Cluster.CLUSTERTYPE_DEDICATED_GATEWAY.equals(clusterType))
					{
						dedicatedGatewayCluster.add(cluster);
						logger.trace(String.format("Found dedicated gateway cluster %s", cluster.getId()));
					}
				}
				
				clusters = dedicatedGatewayCluster;
				logger.trace("# of Dedicated Gateways found : "+ clusters.size());
			}
			else
			{
				logger.debug("Getting List of Clusters.......");
				List<Cluster> noDedicatedGatewayCluster = new ArrayList<Cluster>();
				for(Cluster cluster : clusters)
				{
					String clusterType = cluster.getClusterParams().get(com.ibm.scas.analytics.persistence.beans.Cluster.CLUSTER_PROP_CLUSTERTYPE);
					if(!com.ibm.scas.analytics.persistence.beans.Cluster.CLUSTERTYPE_DEDICATED_GATEWAY.equals(clusterType))
					{
						noDedicatedGatewayCluster.add(cluster);
					}
				}
				
				clusters = noDedicatedGatewayCluster;
				logger.debug("No. of Clusters found : "+ clusters.size());
			}
			
			if (isHub) {
				List<CPELocation> cpeLocations = persistence.getAllObjects(CPELocation.class);
				
				for (CPELocation c : cpeLocations) {
					// Continue if cpe location is my location because we already retrieved my clusters above.
					if (c.getName().equals(myLocationName))
						continue;
					
					// If cpe location passed in header is null then we want to call all the spokes
					// and get their clusters.
					// If cpe location passed in header is not null then only call the spoke
					// for the passed in cpe location.
					if (cpeLocation == null || cpeLocation.equals(c.getName())) {
						final String spokeUrl = c.getUrl() + "/clusters";
						
						final ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
						config.getClasses().add(JacksonJaxbJsonProvider.class);
						final Client client = Client.create(config);
						final WebResource resource = client.resource(spokeUrl);
						
						logger.trace("Calling Spoke URL : " + spokeUrl);
						final List<Cluster> spokeClusters = resource.accept(MediaType.APPLICATION_JSON_TYPE).header("subscriber-id", subscriberId).header("api-key", adminApiKey).get(new GenericType<List<Cluster>>() {});
						
						for(Cluster cluster : spokeClusters)
						{
							logger.trace("Received Cluster : " + new Gson().toJson(cluster));
						}
						clusters.addAll(spokeClusters);
					}
				}
			}

			return createOKResponse(clusters);
		} catch (NotAuthorizedException e) {
			throw new ResourceUnauthorizedException(e.getLocalizedMessage());						
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e);
		}
	}

	/**
	 * POST /clusters
	 * 
	 * @return
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Response createCluster(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey, ClusterRequest cluster) {
		try {
			logger.trace("createCluster: subscriberId : " + subscriberId);
			
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			subscriberId = (subscriberId == null ? getStringAttrValueFromSession("subscriberId") : subscriberId);

			this.validateAPIKey(subscriberId, apiKey);

			/*
			 * Get CPE Location from cluster request
			 * If CPE Location is for Spoke CPE then call Spoke CPE to create cluster.
			 * Otherwise create cluster in Hub. 
			 */
			final String cpeLocationName = cluster.getParameters().get(com.ibm.scas.analytics.persistence.beans.Cluster.CLUSTER_PROP_CPELOCATIONNAME);
			
			if (cpeLocationName == null) {
				throw new CPEParamException("parameters.cpeLocationName");
			}
			
			final CPELocation cpeLocation = persistence.getObjectById(CPELocation.class, cpeLocationName);
			if (cpeLocation == null) {
				throw new CPEParamException("CPE location with name " + cpeLocationName + " not found!");
			}
			
			final String clusterId;
			
			if (cpeLocationName.equals(myLocationName)) {
				clusterId = engine.createCluster(subscriberId, cluster, request.getLocale());
			} else {
				final String spokeUrl = cpeLocation.getUrl() + "/clusters";
				
				final ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
				config.getClasses().add(JacksonJaxbJsonProvider.class);
				final Client client = Client.create(config);
				final WebResource resource = client.resource(spokeUrl);
				
				final Gson gson = new Gson();
				final String req = gson.toJson(cluster);
				
				logger.trace("Calling Spoke URL : " + spokeUrl);
				logger.trace("Post Cluster Object : " + req);
				clusterId = resource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).header("subscriber-id", subscriberId).header("api-key", adminApiKey).post(String.class, req);
			}
			return createOKResponse(new BaseResult("OK", "" + clusterId));
		} catch (ProvisioningException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e.getLocalizedMessage());
		} catch (CPEParamException e) {
			throw new ResourceBadRequestException(e.getLocalizedMessage(), e);
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e.getLocalizedMessage());
		}
	}

	/**
	 * GET /clusters/{clusterId}
	 * 
	 * @param clusterId
	 * @return
	 */
	@GET
	@Path("/{clusterId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClusterDetails(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey, @HeaderParam("cpe-location") String cpeLocation, @PathParam("clusterId") String clusterId) {
		try {
			logger.trace("getClusterDetails: subscriberId : " + subscriberId + " , cpeLocation : " + cpeLocation + " , clusterId : " + clusterId);
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			subscriberId = (subscriberId == null ? getStringAttrValueFromSession("subscriberId") : subscriberId);

			this.validateAPIKey(subscriberId, apiKey);
			
			/*
			 * If Hub then check CPELocationId in header.
			 * If CPELocationId is hub then return cluster details, otherwise forward to Spoke.
			 * If spoke then get cluster details.
			 */
			final Cluster cluster;
			
			if (cpeLocation == null || cpeLocation.equals(myLocationName)) {
				if (subscriberId != null && !engine.subscriberHasAccessToCluster(subscriberId, clusterId)) {
					throw new ResourceUnauthorizedException();
				}
				
				cluster = engine.getClusterDetails(clusterId);
				if (cluster == null) {
					throw new ResourceNotFoundException();
				}
				
				if (cluster.getVpnTunnel() != null) {
					// fill in the vpn tunnel object using firewall service
					cluster.setVpnTunnel(firewallService.getVPNTunnel(cluster.getVpnTunnel().getId(), false));
				}
				
			} else {
				final CPELocation c = persistence.getObjectById(CPELocation.class, cpeLocation);
				final String spokeUrl = c.getUrl() + "/clusters/" + clusterId;
				
				final ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
				config.getClasses().add(JacksonJaxbJsonProvider.class);
				
				final Client client = Client.create(config);
				final WebResource resource = client.resource(spokeUrl);
				
				logger.trace("Calling Spoke URL : " + spokeUrl);
				cluster = resource.accept(MediaType.APPLICATION_JSON_TYPE).header("subscriber-id", subscriberId).header("api-key", adminApiKey).get(new GenericType<Cluster>() {});
				logger.trace("Received Cluster : " + new Gson().toJson(cluster));
			}
			
			return createOKResponse(cluster);
		} catch (NotAuthorizedException e) {
			throw new ResourceUnauthorizedException(e.getLocalizedMessage());				
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e);
		}
	}

	/**
	 * PUT /clusters/{clusterId}
	 * 
	 * @return
	 */
	@PUT
	@Path("/{clusterId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response modifyCluster(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey, @HeaderParam("cpe-location") String cpeLocation, @PathParam("clusterId") String clusterId, ClusterRequest cluster) {
		try {
			logger.trace("modifyCluster: subscriberId : " + subscriberId + " , cpeLocation : " + cpeLocation + " , clusterId : " + clusterId);
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			subscriberId = (subscriberId == null ? getStringAttrValueFromSession("subscriberId") : subscriberId);

			this.validateAPIKey(subscriberId, apiKey);
			
			/*
			 * If Hub then check CPELocationId in header.
			 * If CPELocationId is hub then modify cluster, otherwise forward to Spoke.
			 * If spoke then modify cluster.
			 */
			
			final Cluster existingCluster;
			boolean success = false;
			
			if (cpeLocation == null || cpeLocation.equals(myLocationName)) {
				if (subscriberId != null && !engine.subscriberHasAccessToCluster(subscriberId, clusterId)) {
					logger.error("Subscriber does not have access to cluster!");
					throw new ResourceUnauthorizedException();
				}
				
				existingCluster = engine.getClusterDetails(clusterId);
				
				if (existingCluster == null) {
					throw new ResourceInternalErrorException("Cluster not found!");
				}
				
				final Lock lock = this.locktable.getLockWithAdd(clusterId).writeLock();
				
				try {
					logger.debug("Locking on cluster " + clusterId + " ...");
					lock.lock();
					logger.debug("Lock acquired on cluster " + clusterId);
					
					success = this.performModify(subscriberId, clusterId, cluster);
				} finally {
					lock.unlock();
					logger.debug("Lock released for cluster " + clusterId + " ...");
				}
			} else {
				final CPELocation c = persistence.getObjectById(CPELocation.class, cpeLocation);
				final String spokeUrl = c.getUrl() + "/clusters/" + clusterId;
				
				final ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
				config.getClasses().add(JacksonJaxbJsonProvider.class);
				
				final Client client = Client.create(config);
				final WebResource resource = client.resource(spokeUrl);
				
				final Gson gson = new Gson();
				final String req = gson.toJson(cluster);
				
				logger.debug("Calling Spoke URL : " + spokeUrl);
				final String resp = resource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).header("subscriber-id", subscriberId).header("api-key", adminApiKey).put(String.class, req);
				success = true;
			}

			if (success) {
				return createOKResponse(new BaseResult("OK", "Cluster modified"));
			} else {
				throw new ResourceInternalErrorException("Failed to modify cluster");
			}
		} catch (ProvisioningException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e);
		} catch (NotAuthorizedException e) {
			throw new ResourceUnauthorizedException(e.getLocalizedMessage());		
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e);
		} catch (UniformInterfaceException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e);
		} catch (ClientHandlerException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e);
		}
	}
	
	/**
	 * DELETE /clusters/{clusterId}
	 * 
	 * @param id
	 * @return
	 */
	@DELETE
	@Path("/{clusterId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteCluster(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey, @HeaderParam("cpe-location") String cpeLocation, @PathParam("clusterId") String clusterId) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			subscriberId = (subscriberId == null ? getStringAttrValueFromSession("subscriberId") : subscriberId);

			this.validateAPIKey(subscriberId, apiKey);

			/*
			 * If Hub then check CPELocationId in header.
			 * If CPELocationId is hub then delete cluster, otherwise forward to Spoke.
			 * If spoke then delete cluster.
			 */
			if (cpeLocation == null || cpeLocation.equals(myLocationName)) {
				if (subscriberId != null && !engine.subscriberHasAccessToCluster(subscriberId, clusterId)) {
					throw new ResourceUnauthorizedException();
				}
				
				final Cluster cluster = engine.getClusterDetails(clusterId);
				if (cluster == null) {
					throw new ResourceNotFoundException();
				}
				
				final Lock lock = this.locktable.getLockWithAdd(clusterId).writeLock();
				
				try {
					logger.debug("Locking on cluster " + clusterId + " ...");
					lock.lock();
					logger.debug("Lock acquired on cluster " + clusterId);
					
					this.performDelete(subscriberId, clusterId);
				} finally {
					lock.unlock();
					logger.debug("Lock released for cluster " + clusterId + " ...");
				}
			} else {
				final CPELocation c = persistence.getObjectById(CPELocation.class, cpeLocation);
				final String spokeUrl = c.getUrl() + "/clusters/" + clusterId;
				
				final ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
				config.getClasses().add(JacksonJaxbJsonProvider.class);
				
				final Client client = Client.create(config);
				final WebResource resource = client.resource(spokeUrl);
				
				resource.accept(MediaType.APPLICATION_JSON_TYPE).header("api-key", adminApiKey).delete();
			}
			return createOKResponse(new BaseResult("OK"));
		} catch (NotAuthorizedException e) {
			throw new ResourceUnauthorizedException(e.getLocalizedMessage());
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e);
		}
	}
	
	/**
	 * GET /clusters/{clusterId}/getIPAddrs
	 * 
	 * @param clusterId
	 * @return
	 */
	@GET
	@Path("/{clusterId}/getIPAddrs")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getClusterIPAddrs(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey, @HeaderParam("cpe-location") String cpeLocation, @PathParam("clusterId") String clusterId, @HeaderParam("clusterTier") String clusterTierName) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			subscriberId = (subscriberId == null ? getStringAttrValueFromSession("subscriberId") : subscriberId);

			this.validateAPIKey(subscriberId, apiKey);
			
			/*
			 * If Hub then check CPELocationId in header.
			 * If CPELocationId is hub then return cluster details, otherwise forward to Spoke.
			 * If spoke then get cluster details.
			 */
			
			final List<IPAddress> ipAddresses;
			if (cpeLocation == null || cpeLocation.equals(myLocationName)) {
				final Cluster cluster = engine.getClusterDetails(clusterId);
				if (cluster == null) {
					throw new ResourceNotFoundException();
				}
	
				// local database
				if (subscriberId != null && !engine.subscriberHasAccessToCluster(subscriberId, clusterId)) {
					throw new ResourceUnauthorizedException();
				}
			
				ipAddresses = networkDetailProvider.getIPAddressByCluster(clusterId, clusterTierName);
			} else {
				final CPELocation c = persistence.getObjectById(CPELocation.class, cpeLocation);
				final String spokeUrl = c.getUrl() + "/clusters/" + clusterId + "/getIPAddrs";
				
				final ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
				config.getClasses().add(JacksonJaxbJsonProvider.class);
				
				final Client client = Client.create(config);
				final WebResource resource = client.resource(spokeUrl);
				
				ipAddresses = resource.accept(MediaType.APPLICATION_JSON_TYPE).header("subscriber-id", subscriberId).header("api-key", adminApiKey).header("clusterTier", clusterTierName).get(new GenericType<List<IPAddress>>() {});
			}
			
			return createOKResponse(ipAddresses);
		} catch (NotAuthorizedException e) {
			throw new ResourceUnauthorizedException(e.getLocalizedMessage());						
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e);
		}
	}
	
	/**
	 * GET /clusters/{clusterId}/getVPNTunnel
	 * 
	 * @param clusterId
	 * @return
	 */
	@GET
	@Path("/{clusterId}/getVPNTunnel")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getClusterVPNTunnel(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey, @HeaderParam("cpe-location") String cpeLocation, @PathParam("clusterId") String clusterId) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			subscriberId = (subscriberId == null ? getStringAttrValueFromSession("subscriberId") : subscriberId);

			this.validateAPIKey(subscriberId, apiKey);
			
			/*
			 * If Hub then check CPELocationId in header.
			 * If CPELocationId is hub then return cluster details, otherwise forward to Spoke.
			 * If spoke then get cluster details.
			 */
			
			final VPNTunnel clusterVPNTunnel;
			if (cpeLocation == null || cpeLocation.equals(myLocationName)) {
				final Cluster cluster = engine.getClusterDetails(clusterId);
				if (cluster == null) {
					throw new ResourceNotFoundException();
				}
	
				// local database
				if (subscriberId != null && !engine.subscriberHasAccessToCluster(subscriberId, clusterId)) {
					throw new ResourceUnauthorizedException();
				}
				
				if (cluster.getVpnTunnel() == null) {
					return null;
				}
				
				clusterVPNTunnel = firewallService.getVPNTunnel(cluster.getVpnTunnel().getId(), false);
			} else {
				final CPELocation c = persistence.getObjectById(CPELocation.class, cpeLocation);
				final String spokeUrl = c.getUrl() + "/clusters/" + clusterId + "/getVPNTunnel";
				
				final ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
				config.getClasses().add(JacksonJaxbJsonProvider.class);
				
				final Client client = Client.create(config);
				final WebResource resource = client.resource(spokeUrl);
				
				clusterVPNTunnel = resource.accept(MediaType.APPLICATION_JSON_TYPE).header("subscriber-id", subscriberId).header("api-key", adminApiKey).get(new GenericType<VPNTunnel>() {});
			}
			
			return createOKResponse(clusterVPNTunnel);
		} catch (NotAuthorizedException e) {
			throw new ResourceUnauthorizedException(e.getLocalizedMessage());						
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e);
		}
	}


	/**
	 * POST /clusters/{clusterId}/addOrder
	 * 
	 * @param clusterId
	 * @return
	 */
	@POST
	@Path("/{clusterId}/addOrder")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Response addOrder(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey, @PathParam("clusterId") String clusterId, SoftLayerOrder order) {
		try {
		    logger.trace("addOrder: subscriberId : " + subscriberId + ", clusterId : " + clusterId);
		    apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			subscriberId = (subscriberId == null ? getStringAttrValueFromSession("subscriberId") : subscriberId);
			this.validateAPIKey(subscriberId, apiKey);
						
			boolean success = false;
			
			if (subscriberId != null && !engine.subscriberHasAccessToCluster(subscriberId, clusterId)) {
				logger.error("Subscriber does not have access to cluster!");
				throw new ResourceUnauthorizedException();
			}
				
			final com.ibm.scas.analytics.persistence.beans.Cluster existingCluster = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Cluster.class, clusterId);
							
			if (existingCluster == null) {
					throw new ResourceNotFoundException();
			}
				
			if(order.getSoftLayerId() == null)
				throw new ResourceBadRequestException("Missing parameter: softLayerId");
			
			if(order.getSoftLayerAccount() == null || order.getSoftLayerAccount().getId() == null)
				throw new ResourceBadRequestException("Missing parameter: softLayerAccount.id");
			
			final Lock lock = this.locktable.getLockWithAdd(clusterId).writeLock();
			
			try {
				logger.debug("Locking on cluster " + clusterId + " ...");
				lock.lock();
				logger.debug("Lock acquired on cluster " + clusterId);
				
				success = engine.addSoftlayerOrder(subscriberId, clusterId, order);
			} finally {
				lock.unlock();
				logger.debug("Lock released for cluster " + clusterId + " ...");
			}
			
			if (success) {
				return createOKResponse(new BaseResult("OK", "Cluster modified with softlayer order"));
			} else {
				throw new ResourceInternalErrorException("Failed to modify cluster softlayer order");
			}
		}catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e);
		}
		
	}

	/**
	 * GET /clusters/{clusterId}/orders
	 * 
	 * @param clusterId
	 * @return
	 */
	@GET
	@Path("/{clusterId}/orders")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClusterOrders(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey, @PathParam("clusterId") String clusterId) {
		try {
			logger.trace("getClusterOrders: subscriberId : " + subscriberId + " , clusterId : " + clusterId);
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);
			subscriberId = (subscriberId == null ? getStringAttrValueFromSession("subscriberId") : subscriberId);

			this.validateAPIKey(subscriberId, apiKey);
			
			final com.ibm.scas.analytics.persistence.beans.Cluster existingCluster = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Cluster.class, clusterId);
			
			if (existingCluster == null) {
					throw new ResourceNotFoundException();
			}
			if (subscriberId != null && !engine.subscriberHasAccessToCluster(subscriberId, clusterId)) {
					throw new ResourceUnauthorizedException();
			}
							
			List<SoftLayerOrder> orders = orderProvider.getOrdersForCluster(clusterId);
									
			return createOKResponse(orders);
		} catch (NotAuthorizedException e) {
			throw new ResourceUnauthorizedException(e.getLocalizedMessage());				
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e);
		}
	}	
	@Transactional
	void performDelete(String subscriberId, String clusterId) throws CPEException {
		engine.deleteCluster(subscriberId, clusterId, request.getLocale());
	}
	
	@Transactional
	boolean performModify(String subscriberId, String clusterId, ClusterRequest cluster) throws CPEException {
		return engine.modifyCluster(subscriberId, clusterId, cluster, request.getLocale());
	}
}