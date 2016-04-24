package com.ibm.scas.analytics.resources;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.ibm.scas.analytics.backend.NotAuthorizedException;
import com.ibm.scas.analytics.content.DataProvider;
import com.ibm.scas.analytics.persistence.beans.FormField;
import com.ibm.scas.analytics.persistence.beans.NodeConfiguration;
import com.ibm.scas.analytics.persistence.beans.StepDetail;
import com.ibm.scas.analytics.resources.exceptions.ResourceInternalErrorException;
import com.ibm.scas.analytics.resources.exceptions.ResourceUnauthorizedException;
import com.ibm.scas.analytics.utils.CPEException;

/**
 * Jersey handlers for the Wizards REST API
 * 
 * @author Yogesh Shardul
 *
 */
@Path("/wizard")
public class WizardFieldsResource extends BaseResource {

	private final static Logger logger = Logger.getLogger(WizardFieldsResource.class);
	
	@Inject DataProvider dataProvider;
	
	/**
	 * GET /stepDetails
	 * 
	 * @return
	 */
	@GET
	@Path("/stepDetails")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response listStepDetails(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey) {
		
		this.validateAPIKey(subscriberId, apiKey);

		String plugin = getStringAttrValueFromSession("plugin");
		
		//FIXME : Assigned biginsights to plugin for Testing purpose. Remove it afterwards
		if(plugin == null)
		{
			plugin = "biginsights";
		}
		try {
			final List<StepDetail> wizardSteps = dataProvider.getStepDetails(plugin, request.getLocale());
			return createOKResponse(wizardSteps);
		} catch (CPEException e) {
			throw new ResourceInternalErrorException(e.getLocalizedMessage(), e);
		}
	}
	
	/**
	 * GET /formFields
	 * 
	 * @return
	 */
	@GET
	@Path("/formFields")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response listFormFields(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey) {
		
		this.validateAPIKey(subscriberId, apiKey);

		String plugin = getStringAttrValueFromSession("plugin");
		
		//FIXME : Assigned biginsights to plugin for Testing purpose. Remove it afterwards
		if (plugin == null) {
			plugin = "biginsights";
		}			
		
		try {
			final List<FormField> formElements = dataProvider.getFormFields(plugin, request.getLocale());
			return createOKResponse(formElements);
		} catch (CPEException e) {
			throw new ResourceInternalErrorException(e.getLocalizedMessage(), e);
		}
	}
	
	/**
	 * GET /formFields
	 * 
	 * @return
	 */
	@GET
	@Path("/formFields/{stepId}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response listFormFieldsForStep(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey, @PathParam("stepId") String stepId) {

		this.validateAPIKey(subscriberId, apiKey);
		try {
			final List<FormField> formElements = dataProvider.getFormFieldsForStep(stepId, request.getLocale());

			return createOKResponse(formElements);
		} catch (CPEException e) {
			throw new ResourceInternalErrorException(e.getLocalizedMessage(), e);
		}
	}
	
	/**
	 * GET /formFieldsForDataCenter/{dataCenterName}
	 * 
	 * @return
	 */
	@GET
	@Path("/formFieldsForDataCenter/{dataCenterName}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response listFormFieldsForDataCenter(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey, @PathParam("dataCenterName") String dataCenterName) {

		this.validateAPIKey(subscriberId, apiKey);

		List<FormField> formElements = dataProvider.getFormFieldsForDataCenter(dataCenterName, request.getLocale());
		
		return createOKResponse(formElements);
	}
	
	/**
	 * GET /dataCenters
	 * 
	 * @return
	 */
	@GET
	@Path("/dataCenters")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getDataCenters(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey) {

		this.validateAPIKey(subscriberId, apiKey);
		
		List<String> dataCenters = null;
		dataCenters = dataProvider.getAllDataCenters(request.getLocale());
		
		return createOKResponse(dataCenters);
	}
	
	/**
	 * GET /softLayerLocations
	 * 
	 * @return
	 */
	@GET
	@Path("/softLayerLocations")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getSoftLayerLocations(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey) {
		
		this.validateAPIKey(subscriberId, apiKey);
		
		List<String> locationValues = null;
		locationValues = dataProvider.getSoftLayerLocations();
		
		return createOKResponse(locationValues);
	}
	
	/**
	 * GET /getContainersInLocation/{location}
	 * 
	 * @return
	 */
	@GET
	@Path("/getContainersInLocation")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getContainersInLocation(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey,  @HeaderParam("authUser") String authUser, @HeaderParam("authKey") String authKey, @HeaderParam("location") String location, @HeaderParam("isPrivateURL") Boolean isPrivateURL) {
		
		this.validateAPIKey(subscriberId, apiKey);
		
		logger.debug("authUser : " + authUser);
		logger.debug("authKey : " + authKey);
		logger.debug("location : " + location);
		logger.debug("isPrivateURL : " + isPrivateURL);
		
		List<String> locations;
		locations = dataProvider.getSoftLayerContainersForLocation(authUser, authKey, location, isPrivateURL);
		return createOKResponse(locations);
	}
	
	
	/**
	 * GET /nodes
	 * 
	 * @return
	 */
	@GET
	@Path("/nodes")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getNodes(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey) {
		
		this.validateAPIKey(subscriberId, apiKey);
		
		HttpSession session = request.getSession();
		String accountId = (String) session.getAttribute("accountId");
		
		List<Integer> accountUsage = dataProvider.getAccountUsage(accountId);
		
		return createOKResponse(accountUsage);
	}
	
	/**
	 * GET /checkClusterName
	 * 
	 * @return
	 */
	@GET
	@Path("/checkClusterName/{clusterName}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response checkClusterName(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey, @PathParam("clusterName") String clusterName) {
		
		this.validateAPIKey(subscriberId, apiKey);
		
		Boolean isDuplicate = dataProvider.checkClusterName(subscriberId, clusterName);
		
		return createOKResponse(isDuplicate);
	}
	
	/**
	 * GET /authenticateUser
	 * 
	 * Method to authenticate specified user for the provided API key and location details.
	 * @return Response
	 */
	@GET
	@Path("/authenticateUser")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response authenticateUser(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey, @HeaderParam("authUser") String authUser, @HeaderParam("authKey") String authKey, @HeaderParam("location") String location, @HeaderParam("isPrivateURL") Boolean isPrivateURL) {
		// Authenticate user against what? Is this SoftLayer authentication?
		this.validateAPIKey(subscriberId, apiKey);
		
		try {
			Boolean authenticationStatus = dataProvider.authenticateUser(authUser, authKey, location, isPrivateURL);
			return createOKResponse(authenticationStatus);
		} catch (NotAuthorizedException e) {
			e.printStackTrace();
			throw new ResourceUnauthorizedException("Entered User name and API key are not valid. Please enter valid crendentials and try again.");
		}
	}
	
	/**
	 * GET /checkClusterName
	 * 
	 * @return
	 */
	@GET
	@Path("/nodeConfigurations")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getNodeConfigurations(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey, @PathParam("clusterName") String clusterName) {

		this.validateAPIKey(subscriberId, apiKey);
		
		String plugin = this.getStringAttrValueFromSession("plugin");
		
		List<NodeConfiguration> nodeConfigurations = dataProvider.getNodeConfigurations(plugin, request.getLocale());
		return createOKResponse(nodeConfigurations);
	}

	/**
	 * GET /cpeLocations
	 * 
	 * @return
	 */
	@GET
	@Path("/cpeLocations")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getCpeLocations(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey, @HeaderParam("offering-id") String offeringId) {
		this.validateAPIKey(subscriberId, apiKey);
		List<String> cpeLocationValues = null;
		cpeLocationValues = dataProvider.getCpeLocations(offeringId);
		
		return createOKResponse(cpeLocationValues);
	}
	
	
	/**
	 * GET /transferDirections
	 * 
	 * @return
	 */
	@GET
	@Path("/transferDataCenters")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getTransferDataCenters(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey) {
		this.validateAPIKey(subscriberId, apiKey);
		List<String> transferDataCenters = null;
		List<FormField> formElements = dataProvider.getFormFieldsForTransferDataCenter(request.getLocale());
		
		return createOKResponse(formElements);
	}
	
	/**
	 * GET /authenticationMethods
	 * 
	 * @return
	 */
	@GET
	@Path("/authenticationMethods")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAuthenticationMethods(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey, @HeaderParam("offering-id") String offeringId) {
		this.validateAPIKey(subscriberId, apiKey);
		List<String> authenticationMethods = null;
		authenticationMethods = new ArrayList<String>();
		
		//TODO - hard coded authentication method, need to change once more methods required in future.
		authenticationMethods.add("Pre-Shared Key");
		
		return createOKResponse(authenticationMethods);
	}
	
	/**
	 * GET /vpnTunnelParamFields
	 * 
	 * @return
	 */
	@GET
	@Path("/vpnTunnelParamFields")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getVPNTunnelParamFields(@HeaderParam("subscriber-id") String subscriberId, @HeaderParam("api-key") String apiKey, @HeaderParam("offering-id") String offeringId) {
		this.validateAPIKey(subscriberId, apiKey);
		String plugin = this.getStringAttrValueFromSession("plugin");
		
		try {
			final List<FormField> formElements = dataProvider.getVPNTunnelParamFields(plugin, "dedicatedVPN", request.getLocale());
			return createOKResponse(formElements);
		} catch (CPEException e) {
			throw new ResourceInternalErrorException(e.getLocalizedMessage(), e);
		}
	}
}
