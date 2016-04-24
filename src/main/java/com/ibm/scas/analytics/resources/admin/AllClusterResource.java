package com.ibm.scas.analytics.resources.admin;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.ibm.scas.analytics.beans.Cluster;
import com.ibm.scas.analytics.persistence.PersistenceException;
import com.ibm.scas.analytics.resources.BaseResource;
import com.ibm.scas.analytics.resources.exceptions.ResourceInternalErrorException;
import com.ibm.scas.analytics.utils.CPEException;

/**
 * Jersey handlers for the clusters REST API
 * 
 * @author Han Chen
 * 
 */
@Path("/allclusters")
public class AllClusterResource extends BaseResource {
	private final static Logger logger = Logger.getLogger(AllClusterResource.class);

	/**
	 * GET /clusters
	 * 
	 * @return
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response listAllClusters(@HeaderParam("api-key") String apiKey) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);

			this.validateAPIKey(null, apiKey);

			final List<Cluster> clusters = engine.listAllClusters();
		
			return createOKResponse(clusters);
		} catch (PersistenceException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e.getLocalizedMessage(), e);
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e.getLocalizedMessage(), e);
		}
		
	}
}
