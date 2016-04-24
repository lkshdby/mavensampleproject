package com.ibm.pcmae.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.ibm.pcmae.ClusterAPI;
import com.ibm.pcmae.cluster.beans.Cluster;
import com.ibm.pcmae.cluster.beans.ClusterDefinition;
import com.ibm.pcmae.cluster.beans.ClusterDefinitionDetails;
import com.ibm.pcmae.cluster.beans.ClusterDetails;
import com.ibm.pcmae.cluster.beans.ClusterMachine;
import com.ibm.pcmae.cluster.beans.ClusterTier;
import com.ibm.pcmae.cluster.beans.Message;
import com.ibm.pcmae.cluster.beans.Parameter;
import com.ibm.pcmae.cluster.beans.ReferenceObject;
import com.ibm.scas.analytics.utils.SSLUtils;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;

public class PassthruClusterAPI implements ClusterAPI {
	private static final Logger logger = Logger.getLogger(PassthruClusterAPI.class);
	protected String baseUrl;
	protected String account;
	protected String username;
	protected String password;
	
	private static Map<String, ClusterDefinitionDetails> clusterDefCache = new HashMap<String, ClusterDefinitionDetails>();
	

	public PassthruClusterAPI(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	@Provider
	public static class IgnoreNullJsonProvider extends JacksonJsonProvider {
		@Override
		public ObjectMapper locateMapper(Class<?> arg0, MediaType arg1) {
			ObjectMapper mapper = super.locateMapper(arg0, arg1);
			mapper.setSerializationInclusion(Inclusion.NON_NULL);
			logger.debug("locateMapper called: " + arg0.getCanonicalName());
			return mapper;
		}
	}

	@Override
	public void setCredentials(String account, String username, String password) {
		if (logger.isTraceEnabled()) {
			logger.trace("Setting logon credentials ");
			logger.trace(" account: " + account);
			logger.trace(" username: " + username);
			logger.trace(" password: " + password);
		}
		this.account = account;
		this.username = username;
		this.password = password;
	}

	@Override
	public List<ClusterDefinition> listClusterDefinitions() {
		String url = baseUrl + "/cluster-definitions";
		if (logger.isDebugEnabled()) {	
			logger.debug("listClusterDefinitions, URL: " + url);
		}
		//ClientConfig config = new DefaultClientConfig();
		ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
		config.getClasses().add(JacksonJaxbJsonProvider.class);
		Client client = Client.create(config);
		WebResource resource = client.resource(url);
		
		try {
			List<ClusterDefinition> defs = resource.accept(MediaType.APPLICATION_JSON_TYPE).header("user", username).header("password", password)
					.get(new GenericType<List<ClusterDefinition>>() {});
			return defs;
		} catch (Throwable e) {
			logger.error("Exception listing cluster definition", e);
			return Collections.emptyList();
		}
	}
	@Override
	public ClusterDefinitionDetails getClusterDefinitionDetails(String id) {
		String url = baseUrl + "/cluster-definitions/" + id;
		if (logger.isDebugEnabled()) {
			logger.debug("getClusterDefinitionDetails, URL: " + url);
		}

		synchronized (clusterDefCache) {
			ClusterDefinitionDetails def = clusterDefCache.get(url);
			if (def == null) {
				logger.info("Retrieving cluster definition details from PCM-AE. ID: " + id);

				//ClientConfig config = new DefaultClientConfig();
				ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
				config.getClasses().add(JacksonJaxbJsonProvider.class);
//				config.getClasses().add(IgnoreNullJsonProvider.class);

				Client client = Client.create(config);
				WebResource resource = client.resource(url);
				
				try {
					def = resource.accept(MediaType.APPLICATION_JSON_TYPE).header("user", username).header("password", password)
							.get(ClusterDefinitionDetails.class);
					
					clusterDefCache.put(url, def);
					logger.info(" - cluster definition cached");
				} catch (Throwable e) {
					e.printStackTrace(System.out);
					logger.error(" - failed to retrieve cluster definition from PCM-AE.");
					def = null;
				}
			} else {
				if (logger.isDebugEnabled()) {	
					logger.info(" - loaded from cache.");
				}
			}
			return def;
		}
	}
	@Override
	public List<Cluster> listClusters() {
		String url = baseUrl + "/clusters";
		if (logger.isDebugEnabled()) {
			logger.debug("listClusters, URL: " + url);
		}
		//ClientConfig config = new DefaultClientConfig();
		ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
		config.getClasses().add(JacksonJaxbJsonProvider.class);
		Client client = Client.create(config);
		WebResource resource = client.resource(url);
		
		try {
			List<Cluster> clusters = resource.accept(MediaType.APPLICATION_JSON_TYPE).header("user", username).header("password", password)
					.get(new GenericType<List<Cluster>>() {});
			return clusters;
		} catch (Throwable e) {
			logger.error("Exception listing cluster", e);
			return Collections.emptyList();
		}
	}
	@Override
	public ClusterDetails getClusterDetails(String id) {
		String url = baseUrl + "/clusters/" + id;
		if (logger.isDebugEnabled()) {
			logger.debug("getClusterDetails, URL: " + url);
		}
		//ClientConfig config = new DefaultClientConfig();
		ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
		config.getClasses().add(JacksonJaxbJsonProvider.class);
		Client client = Client.create(config);
		WebResource resource = client.resource(url);
		
		try {
			ClusterDetails cluster = resource.accept(MediaType.APPLICATION_JSON_TYPE).header("user", username).header("password", password)
					.get(ClusterDetails.class);
			return cluster;
		} catch (Throwable e) {
//			e.printStackTrace();
			// preserve HTTP 404 errors
			if (e instanceof UniformInterfaceException) {
				// get the error code
				final UniformInterfaceException uie = (UniformInterfaceException) e;
				if (uie.getResponse().getClientResponseStatus().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
					throw new ClusterNotFoundException(e);
				}
			}
			return null;
		}
	}
	@Override
	public List<Parameter> getClusterParameters(String id, String query) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public List<ClusterMachine> getClusterMachines(String id, String tierName) {
		String url = null;
		if (tierName != null)
			url = baseUrl + "/clusters/" + id + "/machines?tierName=" + tierName;
		url = baseUrl + "/clusters/" + id + "/machines";
		if (logger.isDebugEnabled()) {
			logger.debug("getClusterMachines, URL: " + url);
		}
		ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
		config.getClasses().add(JacksonJaxbJsonProvider.class);
		Client client = Client.create(config);
		WebResource resource = client.resource(url);
		try {
			List<ClusterMachine> clusterMachines = resource.accept(MediaType.APPLICATION_JSON_TYPE).header("user", username).header("password", password)
					.get(new GenericType<List<ClusterMachine>>() {});
			return clusterMachines;
		} catch (Throwable e) {
			logger.debug("Exception listing cluster machine details ", e);
			return Collections.emptyList();
		}
	}
	@Override
	public Message createCluster(ClusterDetails details) {
		ReferenceObject ownerAccount = details.getOwnerAccount();
		if (ownerAccount == null) {
			ownerAccount = new ReferenceObject();
			details.setOwnerAccount(ownerAccount);
		}
		if (ownerAccount.getId() == null) {
			ownerAccount.setId(account);
		}
		String url = baseUrl + "/clusters";
		if (logger.isDebugEnabled()) {
			logger.debug("createCluster, URL: " + url);
		}
		//ClientConfig config = new DefaultClientConfig();
		ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
		config.getClasses().add(JacksonJaxbJsonProvider.class);
//		config.getClasses().add(IgnoreNullJsonProvider.class);
		Client client = Client.create(config);
		WebResource resource = client.resource(url);
		
		try {
//			ObjectMapper mapper = new ObjectMapper();
//			mapper.setSerializationInclusion(Inclusion.NON_NULL);
//			String json = mapper.writeValueAsString(details);
//			logger.debug("POST body: " + json);
			
			Message msg = resource.type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).header("user", username).header("password", password)
					.post(Message.class, details);
//					.post(Message.class, json);
			return msg;
		} catch (Throwable e) {
			logger.error("Exception creating cluster", e);
			return new Message(e.getMessage(), Message.ERROR);
		}
	}
	@Override
	public Message cancelCluster(String id) {
		ClusterDetails details = new ClusterDetails();
		details.setId(id);
		details.setState("CANCELED");
		
		String url = baseUrl + "/clusters/" + id;
		if (logger.isDebugEnabled()) {	
			logger.debug("cancelCluster, URL: " + url);
		}
		//ClientConfig config = new DefaultClientConfig();
		ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
		config.getClasses().add(JacksonJaxbJsonProvider.class);
//		config.getClasses().add(IgnoreNullJsonProvider.class);
		Client client = Client.create(config);
		WebResource resource = client.resource(url);
		
		try {
			Message msg = resource.type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).header("user", username).header("password", password)
					.put(Message.class, details);
			return msg;
		} catch (Throwable e) {
			logger.error("Exception canceling cluster", e);
			return new Message(e.getMessage(), Message.ERROR);
		}
	}
	@Override
	public Message removeCluster(String id) {
		ClusterDetails details = new ClusterDetails();
		details.setId(id);
		details.setState("REMOVED");
		
		String url = baseUrl + "/clusters/" + id;
		if (logger.isDebugEnabled()) {	
			logger.debug("removeCluster, URL: " + url);
		}
		//ClientConfig config = new DefaultClientConfig();
		ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
		config.getClasses().add(JacksonJaxbJsonProvider.class);
		Client client = Client.create(config);
		WebResource resource = client.resource(url);
		
		try {
			Message msg = resource.type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).header("user", username).header("password", password)
					.put(Message.class, details);
			return msg;
		} catch (Throwable e) {
			logger.error("Exception removing cluster", e);
			return new Message(e.getMessage(), Message.ERROR);
		}
	}

	@Override
	public Message flexUpCluster(String id, String tierName, int size) {
		String url = baseUrl + "/clusters/" + id;
		if (logger.isDebugEnabled()) {
			logger.debug("flexUpCluster, URL: " + url);
		}
		//ClientConfig config = new DefaultClientConfig();
		ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
		config.getClasses().add(JacksonJaxbJsonProvider.class);
		Client client = Client.create(config);
		WebResource resource = client.resource(url);
		
		try {
			if (logger.isDebugEnabled()) {
				logger.debug(" - retrieving cluster details...");
			}
			ClusterDetails cluster = resource.accept(MediaType.APPLICATION_JSON_TYPE).header("user", username).header("password", password)
					.get(ClusterDetails.class);

			if (logger.isDebugEnabled()) {
				logger.debug(" - locating requested tier [" + tierName + "]...");
			}
			ClusterTier tier = null;
			for (ClusterTier t : cluster.getTiers()) {
				if (t.getName().equals(tierName)) {
					tier = t;
					break;
				}
			}
			if (tier == null) {
				logger.error("Cluter tier \"" + tierName + "\" not found!");
				return new Message("Tier not found", Message.ERROR);
			} 
			int currentSize = tier.getMachines().get(0).getNumberOfMachines();
			if (currentSize >= size) {
				logger.error("New size (" + size + ") must be greater than current size (" + currentSize + ")!");
				return new Message("Illegal tier size", Message.ERROR);
			}

			if (logger.isDebugEnabled()) {
				logger.debug(" - flexing up...");
			}
			ClusterDetails details = new ClusterDetails();
			details.setId(id);
			details.setTiers(Arrays.asList(tier));
			tier.getMachines().get(0).setNumberOfMachines(size);
			
			Message msg = resource.type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).header("user", username).header("password", password)
					.put(Message.class, details);
			return msg;
		} catch (Throwable e) {
			logger.error("Exception flexing up cluster", e);
			return new Message(e.getMessage(), Message.ERROR);
		}
	}
}
