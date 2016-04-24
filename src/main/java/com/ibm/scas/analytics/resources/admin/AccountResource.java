package com.ibm.scas.analytics.resources.admin;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.google.inject.persist.Transactional;
import com.ibm.scas.analytics.BaseResult;
import com.ibm.scas.analytics.beans.Account;
import com.ibm.scas.analytics.beans.Offering;
import com.ibm.scas.analytics.resources.BaseResource;
import com.ibm.scas.analytics.resources.exceptions.ResourceBadRequestException;
import com.ibm.scas.analytics.resources.exceptions.ResourceInternalErrorException;
import com.ibm.scas.analytics.resources.exceptions.ResourceNotFoundException;
import com.ibm.scas.analytics.utils.CPEException;
import com.ibm.scas.analytics.utils.CPEParamException;

/**
 * Jersey handlers for the clusters REST API
 * 
 * @author Han Chen
 *
 */
@Path("/accounts")
public class AccountResource extends BaseResource {
	private final static Logger logger = Logger.getLogger(AccountResource.class);
	
	/**
	 * GET /accounts
	 * 
	 * @return
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response listAccounts(@QueryParam("offeringId") String offeringId, @HeaderParam("api-key") String apiKey) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);

			this.validateAPIKey(null, apiKey);
			
			List<Account> accounts;
			if (offeringId != null) {
				final Offering offering = tenantService.getOffering(offeringId);
				if (offering == null) {
					throw new ResourceNotFoundException(String.format("Cannot find offering with id: %s", offeringId));
				}
				accounts = tenantService.getAccountsForOffering(offeringId);
			} else {
				accounts = tenantService.getAllAccounts();
			}

			return createOKResponse(accounts);
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e.getLocalizedMessage());
		}

	}

	/**
	 * POST /accounts
	 * 
	 * @return
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({ MediaType.APPLICATION_JSON })
	@Transactional
	public Response createAccount(@HeaderParam("api-key") String apiKey, Account account) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);

			this.validateAPIKey(null, apiKey);

			final String accountId = tenantService.createAccount(account);
			return createOKResponse(new BaseResult("OK", accountId));	
		} catch (CPEParamException e) {
			logger.error("CPEParamException : " + e.getLocalizedMessage());
			throw new ResourceBadRequestException(e.getLocalizedMessage());
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e.getLocalizedMessage());
		}
	}

	/**
	 * GET /accounts/{accountId}
	 * 
	 * @return
	 */
	@GET
	@Path("/{accountId}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAccountDetails(@PathParam("accountId") String accountId, @HeaderParam("api-key") String apiKey) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);

			this.validateAPIKey(null, apiKey);

			final Account account = tenantService.getAccountById(accountId);
			if (account == null) {
				throw new ResourceNotFoundException("Account not found");
			}
			return createOKResponse(account);
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e.getLocalizedMessage());
		}
	}

	/**
	 * put /accounts/{accountId}
	 * 
	 * @return
	 */
	@PUT
	@Path("/{accountId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({ MediaType.APPLICATION_JSON })
	@Transactional
	public Response modifyAccount(@HeaderParam("api-key") String apiKey, @PathParam("accountId") String accountId, Account updatedAccount) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);

			this.validateAPIKey(null, apiKey);

			final com.ibm.scas.analytics.persistence.beans.Account account = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Account.class, accountId);
			if (account == null) {
				throw new ResourceNotFoundException("Account not found");
			}
			
			tenantService.modifyAccount(accountId, updatedAccount);
			
			if (updatedAccount.getStatus().equals(Account.FREE_TRIAL_EXPIRED)) {
				logger.info("Free trial has expired for account " + accountId + ". Deleting all clusters.");
				engine.deleteAllClusters(accountId);
			}
			
			return createOKResponse("OK");
		} catch (CPEParamException e) {
			throw new ResourceBadRequestException(e.getLocalizedMessage());
		} catch (CPEException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ResourceInternalErrorException(e.getLocalizedMessage());
		}
	}
	
	/**
	 * DELETE /accounts/{accountId}
	 * 
	 * @return
	 */
	@DELETE
	@Path("/{accountId}")
	@Produces({ MediaType.APPLICATION_JSON })
	@Transactional
	public Response deleteAccount(@PathParam("accountId") String accountId, @HeaderParam("api-key") String apiKey) {
		try {
			apiKey = (apiKey == null ? getStringAttrValueFromSession("apiKey") : apiKey);

			this.validateAPIKey(null, apiKey);
			tenantService.deleteAccount(accountId);
			return createOKResponse(new BaseResult("OK"));
		} catch (CPEException e) {
			logger.error(e);
			throw new ResourceInternalErrorException(e);
		}
	}
}
