package com.ibm.scas.analytics.resources.admin;

import javax.ws.rs.DELETE;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.ibm.scas.analytics.BaseResult;
import com.ibm.scas.analytics.content.DataProvider;
import com.ibm.scas.analytics.content.DynamicLoader;
import com.ibm.scas.analytics.provider.impl.ServiceProviderPluginFactory;
import com.ibm.scas.analytics.resources.BaseResource;

/**
 * Jersey handlers for the plugins REST API
 * 
 * @author Han Chen
 *
 */
@Path("/plugins")
public class PluginResource extends BaseResource {
	private final static Logger logger = Logger.getLogger(PluginResource.class);

	@Inject DataProvider dataProvider;
	@Inject DynamicLoader dynamicLoader;
	@Inject ServiceProviderPluginFactory pluginFactory;

	/**
	 * DELETE /plugins
	 * This API invalidates the entire plugin cache.
	 * 
	 * @return
	 */
	@DELETE
	@Produces({ MediaType.APPLICATION_JSON })
	public Response deleteAccount(@HeaderParam("api-key") String apiKey) {
		this.validateAPIKey(null, apiKey);
		
		pluginFactory.invalidate();
		dynamicLoader.invalidate();
		dataProvider.invalidate();
		
		return createOKResponse(new BaseResult("OK"));
	}
}
