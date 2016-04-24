package com.ibm.scas.analytics.backend;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ibm.scas.analytics.beans.SoftLayerAccount;
import com.ibm.scas.analytics.persistence.PersistenceService;
import com.ibm.scas.analytics.utils.CPEException;

@Singleton
public class SoftLayerAccountProvider {
	private final static Logger logger = Logger.getLogger(SoftLayerAccountProvider.class);
	
	private PersistenceService persistence;
	
	@Inject 
	SoftLayerAccountProvider(PersistenceService persistence) {
		this.persistence = persistence;
	}
	
	/**
	 * Method to add new SoftlayerAccount
	 * @param softlayerAccountReq
	 * @return
	 * @throws CPEException
	 */
	public String createSoftLayerAccount(SoftLayerAccount softlayerAccountReq) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("%s.createSoftLayerAccount(): url: %s, username: %s", this.getClass().getSimpleName(), softlayerAccountReq.getUrl(), softlayerAccountReq.getUsername()));
		}
	
		// test the SoftLayer account
		final SoftLayerAPIGateway slg = new SoftLayerAPIGateway(
				softlayerAccountReq.getUrl() != null ? softlayerAccountReq.getUrl() : SoftLayerAPIGateway.DEFAULT_REST_API_URL, 
				softlayerAccountReq.getUsername(), 
				softlayerAccountReq.getApiKey());
		
		slg.testCredentials();

		// Set field values
		final com.ibm.scas.analytics.persistence.beans.SoftLayerAccount softlayerAccount = new com.ibm.scas.analytics.persistence.beans.SoftLayerAccount();
		try {
			BeanUtils.copyProperties(softlayerAccount, softlayerAccountReq);
		} catch (IllegalAccessException e) {
			throw new CPEException(e);
		} catch (InvocationTargetException e) {
			throw new CPEException(e);
		}
		
		softlayerAccount.setUrl(softlayerAccountReq.getUrl());
		softlayerAccount.setUsername(softlayerAccountReq.getUsername());
		softlayerAccount.setApiKey(softlayerAccountReq.getApiKey());
		
		persistence.saveObject(com.ibm.scas.analytics.persistence.beans.SoftLayerAccount.class, softlayerAccount);
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("%s.createSoftLayerAccount(): SoftLayer Account %s created successfully", this.getClass().getSimpleName(), softlayerAccountReq.getUsername()));
		}
		
		return softlayerAccount.getId();
		
	}
	
	/**
	 * Method to get list of all SoftlayerAccounts
	 * @return
	 * @throws CPEException
	 */
	public List<SoftLayerAccount> getSoftLayerAccountDetails() throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("%s.getSoftLayerAccountDetails()", this.getClass().getSimpleName()));
		}
		final List<com.ibm.scas.analytics.persistence.beans.SoftLayerAccount> accounts = persistence.getAllObjects(com.ibm.scas.analytics.persistence.beans.SoftLayerAccount.class);
		
		final List<SoftLayerAccount> toReturn = new ArrayList<SoftLayerAccount>(accounts.size());
		for (final com.ibm.scas.analytics.persistence.beans.SoftLayerAccount account : accounts) {
			final SoftLayerAccount acct = new SoftLayerAccount();
			try {
				BeanUtils.copyProperties(acct, account);
			} catch (IllegalAccessException e) {
				throw new CPEException(e);
			} catch (InvocationTargetException e) {
				throw new CPEException(e);
			}
			toReturn.add(acct);
		}
		
		return toReturn;
	}
	
	/**
	 * Method to get list of all SoftlayerAccounts
	 * @return
	 * @throws CPEException
	 */
	public SoftLayerAccount getSoftLayerAccountById(String id) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("%s.getSoftLayerAccountById(): id %s", this.getClass().getSimpleName(), id));
		}
		final com.ibm.scas.analytics.persistence.beans.SoftLayerAccount account = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.SoftLayerAccount.class, id);
		
		final SoftLayerAccount acct = new SoftLayerAccount();
		try {
			BeanUtils.copyProperties(acct, account);
		} catch (IllegalAccessException e) {
			throw new CPEException(e);
		} catch (InvocationTargetException e) {
			throw new CPEException(e);
		}
		
		return acct;
	}
	
	
	/**
	 * Method to delete SoftlayerAccount
	 * @param softlayerAccountReq
	 * @return
	 * @throws CPEException
	 */
	public boolean deleteSoftLayerAccount(String id) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("%s.deleteSoftLayerAccount(): id: %s", this.getClass().getSimpleName(), id));
		}
	
		persistence.deleteObjectById(com.ibm.scas.analytics.persistence.beans.SoftLayerAccount.class, id);
		
		return true;
		
	}

}
