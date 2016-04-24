package com.ibm.scas.analytics.resources;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ibm.scas.analytics.EngineProperties;
import com.ibm.scas.analytics.beans.TransferRequest;
import com.ibm.scas.analytics.content.DataProvider;
import com.ibm.scas.analytics.persistence.beans.SoftLayerLocation;
import com.ibm.scas.analytics.resources.exceptions.ResourceInternalErrorException;
import com.ibm.scas.analytics.utils.SSLUtils;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;

/**
 * Jersey handlers for the plugins REST API
 * 
 * @author Han Chen
 *
 */
@Path("/transfers")
@Singleton
public class TransfersResource extends BaseResource {
	private final static Logger logger = Logger.getLogger(TransfersResource.class);
	
	@Inject DataProvider dataProvider;
	
	private int transferServicePort;
	
	public TransfersResource() {
		try {
			transferServicePort = Integer.parseInt(EngineProperties.getInstance().getProperty(EngineProperties.TRANSFER_SERVICE_PORT, String.valueOf(EngineProperties.DEFAULT_TRANSFER_SERVICE_PORT)));
		} catch (NumberFormatException e) {
			transferServicePort = EngineProperties.DEFAULT_TRANSFER_SERVICE_PORT;
		}
	}

	private String generateUrl(String masterIp) {
		return "https://" + masterIp + ":" + transferServicePort + "/transfers";
	}
	
	private String generateUrl(String masterIp, String id) {
		return generateUrl(masterIp) + "/" + id;
	}
	
	
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response listTransfers(@HeaderParam("x-master-ip") String masterIp, @HeaderParam("x-username") String username, @HeaderParam("x-password") String password) {
		String url = generateUrl(masterIp);
		logger.debug("listTransfers, URL: " + url);

		ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
		config.getClasses().add(JacksonJaxbJsonProvider.class);
		Client client = Client.create(config);
		WebResource resource = client.resource(url);

		try {
			ClientResponse response = resource.header("user", username).header("password", password)
					.accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);
			int status = response.getStatus();
			Map result = response.getEntity(Map.class);
			logger.debug(" - status: " + status);
			try {
				ObjectMapper mapper = new ObjectMapper();
				mapper.setSerializationInclusion(Inclusion.NON_NULL);
				String json = mapper.writeValueAsString(result);
				logger.debug(" - result: " + json);
			} catch (Exception e) {
				logger.error(" - failed to parse result!");
			}
			return createResponse(Status.fromStatusCode(status), result);
		} catch (Throwable e) {
			logger.error("Exception in listTransfers", e);
			throw new ResourceInternalErrorException(e);
		}
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response startTransfer(@HeaderParam("x-master-ip") String masterIp, @HeaderParam("x-username") String username, @HeaderParam("x-password") String password, TransferRequest request) {
		String url = generateUrl(masterIp);
		logger.debug("startTransfer, URL: " + url);

		ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
		config.getClasses().add(JacksonJaxbJsonProvider.class);
		Client client = Client.create(config);
		WebResource resource = client.resource(url);

		try {
			String location = request.getLocation();
			if(location != null)
			{
				SoftLayerLocation slLocation = dataProvider.getSoftLayerLocationForName(location);
				String container = request.getContainer();
				String path = request.getPath();
				String transferDirection = request.getTransferDirection();
				/*if(!path.startsWith("/"))
				{
					path = "/" + path;
				}*/
				String dataInSource = "swift://" + container + ".softlayer/"+ path;
				
				logger.debug("startTransfer, transferDirection : " + transferDirection);
				
				//if from softlayer then set source and destination otherwise reverse the order
				if(transferDirection.equalsIgnoreCase("from"))
				{
					request.setSrc(dataInSource);
				}
				else
				{
					String destination = request.getDest();
					request.setSrc(destination);
					request.setDest(dataInSource);
				}
				
				request.setAuthEndPoint(slLocation.getPrivateUrl());
				
				logger.debug("startTransfer, source : " + request.getSrc() + " and destination : " + request.getDest());
				
				ClientResponse response = resource.header("user", username).header("password", password)
						.type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, request);
				int status = response.getStatus();
				Map result = response.getEntity(Map.class);
				logger.debug(" - status: " + status);
				try {
					ObjectMapper mapper = new ObjectMapper();
					mapper.setSerializationInclusion(Inclusion.NON_NULL);
					String json = mapper.writeValueAsString(result);
					logger.debug(" - result: " + json);
				} catch (Exception e) {
					logger.error(" - failed to parse result!");
				}
				return createResponse(Status.fromStatusCode(status), result);
			}
			else{
				throw new RuntimeException("Location is not provided");
			}
		} catch (Throwable e) {
			logger.error("Exception in startTransfer", e);
			throw new ResourceInternalErrorException(e);
		}
	}

	@GET
	@Path("/{id}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getTransferStatus(@HeaderParam("x-master-ip") String masterIp, @HeaderParam("x-username") String username, @HeaderParam("x-password") String password, @PathParam("id") String id) {
		String url = generateUrl(masterIp, id);
		logger.debug("getTransferStatus, URL: " + url);

		ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
		config.getClasses().add(JacksonJaxbJsonProvider.class);
		Client client = Client.create(config);
		WebResource resource = client.resource(url);

		try {
			ClientResponse response = resource.header("user", username).header("password", password)
					.accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);
			int status = response.getStatus();
			Map result = response.getEntity(Map.class);
			logger.debug(" - status: " + status);
			try {
				ObjectMapper mapper = new ObjectMapper();
				mapper.setSerializationInclusion(Inclusion.NON_NULL);
				String json = mapper.writeValueAsString(result);
				logger.debug(" - result: " + json);
			} catch (Exception e) {
				logger.error(" - failed to parse result!");
			}
			return createResponse(Status.fromStatusCode(status), result);
		} catch (Throwable e) {
			logger.error("Exception in getTransferStatus", e);
			throw new ResourceInternalErrorException(e);
		}
	}
	/**
	 * DELETE /transfers/{id}
	 * 
	 * @return
	 */
	@DELETE
	@Path("/{id}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response stopTransfer(@HeaderParam("x-master-ip") String masterIp, @HeaderParam("x-username") String username, @HeaderParam("x-password") String password, @PathParam("id") String id) {
		String url = generateUrl(masterIp, id);
		logger.debug("stopTransfer, URL: " + url);

		ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
		config.getClasses().add(JacksonJaxbJsonProvider.class);
		Client client = Client.create(config);
		WebResource resource = client.resource(url);

		try {
			ClientResponse response = resource.header("user", username).header("password", password)
					.accept(MediaType.APPLICATION_JSON_TYPE).delete(ClientResponse.class);
			int status = response.getStatus();
			Map result = response.getEntity(Map.class);
			logger.debug(" - status: " + status);
			try {
				ObjectMapper mapper = new ObjectMapper();
				mapper.setSerializationInclusion(Inclusion.NON_NULL);
				String json = mapper.writeValueAsString(result);
				logger.debug(" - result: " + json);
			} catch (Exception e) {
				logger.error(" - failed to parse result!");
			}
			return createResponse(Status.fromStatusCode(status), result);
		} catch (Throwable e) {
			logger.error("Exception in stopTransfer", e);
			throw new ResourceInternalErrorException(e);
		}
	}
}
