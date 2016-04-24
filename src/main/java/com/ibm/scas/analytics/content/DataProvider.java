package com.ibm.scas.analytics.content;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.google.inject.Inject;
import com.ibm.scas.analytics.backend.NotAuthorizedException;
import com.ibm.scas.analytics.backend.TenantService;
import com.ibm.scas.analytics.persistence.PersistenceException;
import com.ibm.scas.analytics.persistence.PersistenceService;
import com.ibm.scas.analytics.persistence.beans.Account;
import com.ibm.scas.analytics.persistence.beans.Cluster;
import com.ibm.scas.analytics.persistence.beans.DataCenter;
import com.ibm.scas.analytics.persistence.beans.DataTransferFieldsMap;
import com.ibm.scas.analytics.persistence.beans.FormField;
import com.ibm.scas.analytics.persistence.beans.NodeConfiguration;
import com.ibm.scas.analytics.persistence.beans.Offering;
import com.ibm.scas.analytics.persistence.beans.OfferingLocations;
import com.ibm.scas.analytics.persistence.beans.SoftLayerLocation;
import com.ibm.scas.analytics.persistence.beans.StepDetail;
import com.ibm.scas.analytics.persistence.util.WhereClause;
import com.ibm.scas.analytics.utils.CPEException;
import com.ibm.scas.analytics.utils.SLAuthenticator;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;

public class DataProvider {
	@Inject private PersistenceService persistence;
	@Inject private TenantService tenantService;
	
	private final static Logger logger = Logger.getLogger(DataProvider.class);
	
	private static Map<String, List<FormField>> pluginFormFields = new HashMap<String, List<FormField>>(); 
	private static Map<String, List<StepDetail>> pluginStepDetails = new HashMap<String, List<StepDetail>>();
	
	public List<FormField> getFormFields(String plugin, Locale locale) throws CPEException {
		List<FormField> formElements = persistence.getAllFormFields(plugin);
		if (formElements == null) {
			return Collections.emptyList();
		}
		
		//pluginFormFields.put(plugin, formElements);
		for (final FormField formField : formElements) {
			formField.localize(locale);
		}

		
		return formElements;
	}

	public List<FormField> getFormFieldsForStep(String stepId, Locale locale) throws CPEException {
		List<FormField> retElements = persistence.getFormFieldsForStep(stepId);
		
		if (retElements == null) {
			return Collections.emptyList();
		}
		
		for (final FormField formField : retElements) {
			formField.localize(locale);
		}
		
		return retElements;
	}
	
	public List<StepDetail> getStepDetails(String plugin, Locale locale) throws PersistenceException {
		List<StepDetail> stepDetails = persistence.getAllStepDetails(plugin);
		if(stepDetails == null) {
			return Collections.emptyList();
		}

		for(final StepDetail stepDetail : stepDetails) {
			stepDetail.localize(locale);
		}

		return stepDetails;
	}

	public List<String> getAllDataCenters(Locale locale)
	{
		List<DataCenter> dataCenters = null;
		List<String> retDataCenters = new ArrayList<String>();
		
		try {
			dataCenters = persistence.getAllObjects(DataCenter.class);
			
			for(DataCenter dataCenter : dataCenters)
			{
				dataCenter.localize(locale);
				retDataCenters.add(dataCenter.getName() + "~formFieldsForDataCenter/" + dataCenter.getName());
			}
		} catch (PersistenceException e) {
			logger.error(e.getLocalizedMessage(), e);
			System.exit(1);
		}
		
		return retDataCenters;
	}
	
	public List<Integer> getAccountUsage(String accountId)
	{
		List<Integer> accountUsage = new ArrayList<Integer>();
		
		try {
			Account account = persistence.getObjectById(Account.class, accountId);
			if (account.getQuantity() > 0) {
				int currentUsage = this.tenantService.getCurrentUsageForAccount(accountId);
				Integer usage = (account.getQuantity() - currentUsage) - 1;
				if(usage > 0)
				{
					for(int i=1;i<=usage;i++)
					{
						accountUsage.add(i);
					}
				}
			}
		} catch (PersistenceException e) {
			logger.error(e.getLocalizedMessage(), e);
			System.exit(1);
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			System.exit(1);
		}
		
		return accountUsage;
	}
	
	public Boolean checkClusterName(String subscriberId, String name)
	{
		Boolean isDuplicate = false;
		
		try {
			List<Cluster> clusters = persistence.getObjectsBy(Cluster.class, new WhereClause("name", name), new WhereClause("owner.id", subscriberId));
			
			if(clusters != null && clusters.size() > 0)
			{
				isDuplicate = true;
			}
		} catch (PersistenceException e) {
			logger.error(e.getLocalizedMessage(), e);
			System.exit(1);
		}
		return isDuplicate;
	}
	
	public List<FormField> getFormFieldsForDataCenter(String dataCenterName, Locale locale)
	{
		List<FormField> formFields = null;
		
		try {
			formFields = persistence.getObjectsBy(FormField.class, new WhereClause("dataCenterFields.dataCenter.name", dataCenterName));
		} catch (PersistenceException e) {
			logger.error(e.getLocalizedMessage(), e);
			return Collections.emptyList();
		}
		if(formFields != null)
		{
			//pluginFormFields.put(plugin, formElements);
			for(FormField formField : formFields)
			{
				formField.localize(locale);
			}
		}
		
		return formFields;
	}
	
	public List<String> getSoftLayerLocations()
	{
		List<SoftLayerLocation> locations = null;
		List<String> locationValues = new ArrayList<String>();
		
		try {
			locations = persistence.getAllObjects(SoftLayerLocation.class);
			
			for(SoftLayerLocation location : locations)
			{
				locationValues.add(location.getName());
			}
		} catch (PersistenceException e) {
			logger.error(e.getLocalizedMessage(), e);
			System.exit(1);
		}
		
		return locationValues;
	}
	
	public List<String> getSoftLayerContainersForLocation(String username, String apiKey, String location, Boolean isPrivateURL)
	{
		List<String> containers = new ArrayList<String>();
		
		logger.debug("Getting SoftLayer Location Info for name : " + location);
		SoftLayerLocation slLocation = getSoftLayerLocationForName(location);
		String authUrl = null;
		
		if(isPrivateURL)
		{
			authUrl = slLocation.getPrivateUrl();
		}
		else
		{
			authUrl = slLocation.getPublicUrl();
		}
		
		
		try {
			
			SLAuthenticator authenticator = new SLAuthenticator(username, apiKey, authUrl, isPrivateURL);
			logger.debug("Authenticating user for URL : " + authUrl);
			authenticator.authenticate();
			
			String authToken = authenticator.getAuthToken();
			logger.debug("Auth Token : " + authToken);
			String storageUrl = authenticator.getStorageUrl();
			logger.debug("Storage URL : " + storageUrl);
			
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet getRequest = new HttpGet(storageUrl);
			
			getRequest.addHeader("X-Auth-Token", authToken);
			getRequest.addHeader("accept", "application/json");
	 
			HttpResponse response = httpClient.execute(getRequest);
	 
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
				   + response.getStatusLine().getStatusCode());
			}
	 
			BufferedReader br = new BufferedReader(
	                         new InputStreamReader((response.getEntity().getContent())));
	 
			String output;
			StringBuilder jsonData = new StringBuilder();
			logger.debug("Returned Json Data : ");
			while ((output = br.readLine()) != null) {
				logger.debug(output);
				jsonData.append(output);
			}
	 
			httpClient.getConnectionManager().shutdown();
			
			String jsonContainers = jsonData.toString();
			if(jsonContainers.trim().length() != 0)
			{	
				ObjectMapper mapper = new ObjectMapper();
				List<SoftLayerContainer> slContainers = mapper.readValue(jsonContainers, new TypeReference<List<SoftLayerContainer>>(){});
				logger.debug("Count of Containers : " + slContainers.size());
				if(slContainers != null)
				{
					for(SoftLayerContainer container : slContainers)
					{
						containers.add(container.getName());
					}
				}
			}
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error Occurred : ", e);
			logger.error("Error Occurred : " + e.getMessage());
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error Occurred : ", e);
			logger.error("Error Occurred : " + e.getMessage());
		} catch (UniformInterfaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error Occurred : ", e);
			logger.error("Error Occurred : " + e.getMessage());
		} catch (ClientHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error Occurred : ", e);
			logger.error("Error Occurred : " + e.getMessage());
		} catch (NotAuthorizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error Occurred : ", e);
			logger.error("Error Occurred : " + e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error Occurred : ", e);
			logger.error("Error Occurred : " + e.getMessage());
		}
		catch(Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error Occurred : ", e);
			logger.error("Error Occurred : " + e.getMessage());
		}
		
		return containers;
	}
	
	public SoftLayerLocation getSoftLayerLocationForName(String location)
	{
		logger.debug("Getting SoftLayer Location Info for name : " + location);
		
		SoftLayerLocation slLocation = null;
		try {
			slLocation = persistence.getObjectById(SoftLayerLocation.class, location);
		} catch (PersistenceException e) {
			logger.error(e.getLocalizedMessage(), e);
			System.exit(1);
		}
		
		return slLocation;
	}
	
	public boolean authenticateUser(String username, String apiKey, String location, Boolean isPrivateURL)  throws NotAuthorizedException
	{
		logger.debug("Authenticating user for name : " + username);

		SoftLayerLocation slLocation = null;
		try {
			slLocation = persistence.getObjectById(SoftLayerLocation.class, location);
		} catch (PersistenceException e) {
			logger.error(e.getLocalizedMessage(), e);
			System.exit(1);
		}
		String authUrl = null;

		if (isPrivateURL) {
			authUrl = slLocation.getPrivateUrl();
		} else {
			authUrl = slLocation.getPublicUrl();
		}

		SLAuthenticator authenticator = new SLAuthenticator(username,
				apiKey, authUrl, isPrivateURL);
		logger.debug("Authenticating user for URL : " + authUrl);
		authenticator.authenticate();

		return true;
	}
	
	public List<NodeConfiguration> getNodeConfigurations(String plugin, Locale locale)
	{
		List<NodeConfiguration> nodeConfigurations = new ArrayList<NodeConfiguration>();
		
		try {
			nodeConfigurations = persistence.getObjectsBy(NodeConfiguration.class, new WhereClause("pluginId", plugin));
			NodeConfiguration headerNodeConfiguration = new NodeConfiguration();
			
			ResourceBundle rsrc = ResourceBundle.getBundle("cpe_messages", locale);
			headerNodeConfiguration.setNodeSize(rsrc.getString("hdp.header.nodeSize"));
			headerNodeConfiguration.setSpecification(rsrc.getString("hdp.header.specification"));
			headerNodeConfiguration.setDataBandwidth(rsrc.getString("hdp.header.dataBandwidth"));
			headerNodeConfiguration.setUsedFor(rsrc.getString("hdp.header.usedFor"));
			
			nodeConfigurations.add(0, headerNodeConfiguration);
			
			for(NodeConfiguration config : nodeConfigurations)
			{
				config.localize(locale);
			}
		} catch (PersistenceException e) {
			logger.error(e.getLocalizedMessage(), e);
			System.exit(1);
		}
		
		return nodeConfigurations;
	}
	
	public List<String> getCpeLocations(String offeringId) {
		List<OfferingLocations> cpeLocations = null;
		List<String> cpeLocationValues = new ArrayList<String>();
		
		try {
			Offering offering = persistence.getObjectById(Offering.class, offeringId);
			cpeLocations = persistence.getObjectsBy(OfferingLocations.class, new WhereClause("offeringId", offering));
			
			for(OfferingLocations location : cpeLocations)
			{
				cpeLocationValues.add(location.getLocationName().getName());
			}
		} catch (PersistenceException e) {
			logger.error(e.getLocalizedMessage(), e);
			System.exit(1);
		}
		
		return cpeLocationValues;
	}
	
	public void invalidate()
	{
		pluginFormFields.clear();
		pluginStepDetails.clear();
	}
	
	public List<String> getAllTransferDataCenters()
	{
		List<DataCenter> transferDataCenters;
		try {
			transferDataCenters = persistence.getAllObjects(DataCenter.class);
		} catch (PersistenceException e) {
			logger.error(e.getLocalizedMessage(), e);
			return Collections.emptyList();
		}
		
		final List<String> toReturn = new ArrayList<String>(transferDataCenters.size());
		for (final DataCenter dc : transferDataCenters) {
			toReturn.add(dc.getName());
		}
				
		return toReturn;
	}
	
	public List<FormField> getFormFieldsForTransferDataCenter(Locale locale)
	{
		List<FormField> formFields = null;
		
		try {
			List<DataTransferFieldsMap> dataTransferFieldsMaps = persistence.getObjectsBy(DataTransferFieldsMap.class);
			if(null!=dataTransferFieldsMaps && !dataTransferFieldsMaps.isEmpty())
			{
				formFields = new ArrayList<FormField>();
				for(DataTransferFieldsMap transferFields: dataTransferFieldsMaps)
				{
					transferFields.getFormField().localize(locale);
					formFields.add(transferFields.getFormField());
				}
			}
		} catch (PersistenceException e) {
			logger.error(e.getLocalizedMessage(), e);
			return Collections.emptyList();
		}
		
		return formFields;
	}
	
	public List<FormField> getVPNTunnelParamFields(String plugin, String formFieldName, Locale locale) throws CPEException {
		List<FormField> formElements = null;
		
		try {
			final List<StepDetail> stepDetailsForPlugin = persistence.getObjectsBy(StepDetail.class, new WhereClause("pluginId", plugin));
			
			final List<String> stepIdList = new ArrayList<String>();
			for (final StepDetail stepDetails : stepDetailsForPlugin) 	{
				stepIdList.add(stepDetails.getId());
			}
			
			final List<FormField> formElementsWithName = persistence.getObjectsBy(FormField.class, new WhereClause("name", formFieldName));
			
			//find step id of matching element
			String stepId = null;
			for (final FormField formField : formElementsWithName) {
				if (stepIdList.contains(formField.getStepDetail().getId())) {
					stepId = formField.getStepDetail().getId();
					break;
				}
			}
			
			if(stepId != null) 	{
				formElements = getFormFieldsForStep(stepId, locale);
			}
			
		} catch (PersistenceException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		
		return formElements;
	}
}
