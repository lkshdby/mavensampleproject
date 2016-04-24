package com.ibm.scas.analytics.backend.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.ibm.scas.analytics.backend.NetworkService;
import com.ibm.scas.analytics.backend.ProvisioningService;
import com.ibm.scas.analytics.backend.TenantService;
import com.ibm.scas.analytics.beans.Account;
import com.ibm.scas.analytics.beans.Cluster;
import com.ibm.scas.analytics.beans.Gateway;
import com.ibm.scas.analytics.beans.Offering;
import com.ibm.scas.analytics.beans.Subscriber;
import com.ibm.scas.analytics.beans.Subscriber.SubscriberType;
import com.ibm.scas.analytics.beans.Vlan;
import com.ibm.scas.analytics.beans.utils.BeanConversionUtil;
import com.ibm.scas.analytics.persistence.PersistenceService;
import com.ibm.scas.analytics.persistence.util.WhereClause;
import com.ibm.scas.analytics.utils.CPEException;
import com.ibm.scas.analytics.utils.CPEParamException;

public class TenantServiceImpl implements TenantService {
	private final static Logger logger = Logger.getLogger(TenantServiceImpl.class);
	
	@Inject NetworkService networkService;
	@Inject PersistenceService persistence;
	@Inject ProvisioningService engine;
	
	@Override
	public List<Account> getAllAccounts() throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("getAllAccounts()"));
		}
		
		List<com.ibm.scas.analytics.persistence.beans.Account> accountRecs = Collections.emptyList();

		accountRecs = persistence.getAllObjects(com.ibm.scas.analytics.persistence.beans.Account.class);

		final List<Account> accounts = new ArrayList<Account>(accountRecs.size());
		for (final com.ibm.scas.analytics.persistence.beans.Account accountRec : accountRecs) {
			accounts.add(BeanConversionUtil.convertToBean(accountRec));
		}
		
		return accounts;
	}

	@Override
	public List<Account> getAccountsForOffering(String offeringId) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("getAccountsForOffering(): offeringId=%s", offeringId));
		}
		
		List<com.ibm.scas.analytics.persistence.beans.Account> accountRecs = Collections.emptyList();
		if (offeringId != null) {
			accountRecs = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Account.class, new WhereClause("offering.id",  "offeringId"));
		}

		final List<Account> accounts = new ArrayList<Account>(accountRecs.size());
		for (final com.ibm.scas.analytics.persistence.beans.Account accountRec : accountRecs) {
			accounts.add(BeanConversionUtil.convertToBean(accountRec));
		}
		
		return accounts;
	}

	@Override
	public Account getAccountById(String accountId) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("getAccountById(): %s", accountId));
		}
		
		final com.ibm.scas.analytics.persistence.beans.Account account = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Account.class, accountId);
		if (account == null) {
			return null;
		}
		
		return BeanConversionUtil.convertToBean(account);
	}

	@Override
	public List<Subscriber> getSubscribers(String accountId, String externalId, SubscriberType type) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("getSubscribers(): accountId=%s, externalId=%s, type=%s", accountId, externalId, type != null ? type.toString() : "null"));
		}		
		List<com.ibm.scas.analytics.persistence.beans.Subscriber> subscribers = Collections.emptyList();
		final List<WhereClause> whereClauses = new ArrayList<WhereClause>(3);
		if (accountId != null) {
			whereClauses.add(new WhereClause("account.id", accountId));
		}
		if (externalId != null) {
			whereClauses.add(new WhereClause("externalId", externalId));
		}
		
		if (type != null) {
			whereClauses.add(new WhereClause("type", BeanConversionUtil.subscriberTypeToInt(type)));
		}

		subscribers = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Subscriber.class, whereClauses.toArray(new WhereClause[] {}));

		final List <Subscriber> toReturn = new ArrayList<Subscriber>(subscribers.size());
		for (final com.ibm.scas.analytics.persistence.beans.Subscriber subscriberRec : subscribers) {
			toReturn.add(BeanConversionUtil.convertToBean(subscriberRec));
		}
		
		return toReturn;
	}

	@Override
	public Subscriber getSubscriberById(String subscriberId) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("getSubscriberById(): %s", subscriberId));
		}	
		final com.ibm.scas.analytics.persistence.beans.Subscriber subscriber = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Subscriber.class, subscriberId);

		if (subscriber == null) {
			return null;
		}

		return BeanConversionUtil.convertToBean(subscriber);
	}

	@Override
	public String createAccount(Account account) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("createAccount(): %s", ReflectionToStringBuilder.toString(account)));
		}
		if (account.getOffering() == null) {
			throw new CPEParamException("account.offering not set");
		}
		if (account.getOffering().getId() == null) {
			throw new CPEParamException("account.offering.id not set");
		}	
		final String offeringId = account.getOffering().getId();
		final com.ibm.scas.analytics.persistence.beans.Offering offering = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Offering.class, offeringId);
		if (offering == null) {
			throw new CPEParamException(String.format("Offering %s not found", account.getOffering().getId()));
		}
			
		final com.ibm.scas.analytics.persistence.beans.Account accountRec = BeanConversionUtil.convertToRecord(account);
		accountRec.setOffering(offering);
	
		persistence.saveObject(com.ibm.scas.analytics.persistence.beans.Account.class, accountRec);
		
		logger.info(String.format("Created new Account %s for offering with id %s", accountRec.getId(), offeringId));
	
		return accountRec.getId();
	}

	@Override
	public String createSubscriber(Subscriber subscriber) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("createSubscriber(): %s", ReflectionToStringBuilder.toString(subscriber)));
		}
		
		if (subscriber.getName() == null) {
			throw new CPEParamException("Missing field: subscriber.name");
		}
		
		if (subscriber.getAccount() == null) {
			throw new CPEParamException("Missing field: subscriber.account");
		}

		if (subscriber.getAccount().getAccountIdentifier() == null) {
			throw new CPEParamException("Missing field: subscriber.account.accountIdentifier");
		}
		
		if (subscriber.getType() == null) {
			logger.warn(String.format("subscriber.type not set, defaulting to %s", SubscriberType.APPDIRECT));
			subscriber.setType(SubscriberType.APPDIRECT);
		}
		
		if (subscriber.getType() == SubscriberType.APPDIRECT &&
			subscriber.getExternalId() == null) {
			throw new CPEParamException("Missing field: Subscriber type is APPDIRECT but subscriber.externalId is not set");
		}

		// find the account
		final String accountId = subscriber.getAccount().getAccountIdentifier();
		final com.ibm.scas.analytics.persistence.beans.Account account = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Account.class, accountId);
		if (account == null) {
			throw new CPEParamException(String.format("Account %s not found", subscriber.getAccount().getAccountIdentifier()));
		}

		final com.ibm.scas.analytics.persistence.beans.Subscriber subscriberRec = BeanConversionUtil.convertToRecord(subscriber);
		if (subscriber.getApiKey() == null) {
			subscriberRec.generateRandomApiKey();
		}
		subscriberRec.setAccount(account);

		persistence.saveObject(com.ibm.scas.analytics.persistence.beans.Subscriber.class, subscriberRec);
		
		logger.info(String.format("createSubscriber(): Created new subscriber %s for account with id %s", subscriberRec.getId(), accountId));

		return subscriberRec.getId();

	}

	@Override
	public void deleteAccount(String accountId) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("deleteAccount(): %s", accountId));
		}	
		// caller opens transaction
		logger.info("deleteAccount called, account: " + accountId);
		List<com.ibm.scas.analytics.persistence.beans.Subscriber> subscribers = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Subscriber.class, new WhereClause("account.id", accountId));
		for (com.ibm.scas.analytics.persistence.beans.Subscriber subscriber : subscribers) {
			String subscriberId = subscriber.getId();
			deleteSubscriber(accountId, subscriberId);
		}
		
		final Collection<Gateway> gateways = networkService.getAccountGateways(accountId);
		for (final Gateway subscriberGateway : gateways) {
			networkService.unassignGateway(subscriberGateway.getId());
		}
		
		persistence.deleteObjectById(com.ibm.scas.analytics.persistence.beans.Account.class, accountId);;
	}

	@Override
	public void deleteSubscriber(String accountId, String subscriberId) throws CPEException {
		// note: caller starts the transaction
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("deleteSubscriber(): accountId=%s, subscriberId=%s", accountId, subscriberId));
		}		
		
		final List<Cluster> clusters = engine.listClusters(subscriberId);
		for (final Cluster cluster: clusters) {
			engine.deleteCluster(subscriberId, cluster.getId(), Locale.ENGLISH);
		}
		
		final Collection<Vlan> vlans = networkService.getSubscriberVlans(subscriberId);
		for (final Vlan subscriberVlan : vlans) {
			networkService.unassignVlan(subscriberVlan.getId(), false);
		}
		
		persistence.deleteObjectById(com.ibm.scas.analytics.persistence.beans.Subscriber.class, subscriberId);
	}

	@Override
	public Offering getOffering(String offeringId) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("getOffering(): %s", offeringId));
		}		
		
		final com.ibm.scas.analytics.persistence.beans.Offering offering = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Offering.class, offeringId);
		if (offering == null) {
			return null;
		}
		
		return BeanConversionUtil.convertToBean(offering);
	}

	@Override
	public void modifyAccount(String accountId, Account updatedAccount) throws CPEException {
		if (updatedAccount == null || updatedAccount.getAccountIdentifier() == null || !updatedAccount.getAccountIdentifier().equals(accountId)) {
			throw new CPEParamException("The account id does not match the account being updated");
		}
			
		final com.ibm.scas.analytics.persistence.beans.Account accountRec = BeanConversionUtil.convertToRecord(updatedAccount);
		accountRec.setId(accountId);
			
		persistence.updateObject(com.ibm.scas.analytics.persistence.beans.Account.class, accountRec);
	}

	@Override
	public void modifySubscriber(Subscriber subscriber) throws CPEException {
		final com.ibm.scas.analytics.persistence.beans.Subscriber subscriberRec = BeanConversionUtil.convertToRecord(subscriber);
		
		if (subscriber.getAccount() != null && subscriber.getAccount().getAccountIdentifier() != null) {
			final com.ibm.scas.analytics.persistence.beans.Account accountRec = persistence.getObjectById(com.ibm.scas.analytics.persistence.beans.Account.class, subscriber.getAccount().getAccountIdentifier());
			if (accountRec == null) {
				throw new CPEParamException(String.format("Account does not exist: %s", subscriber.getAccount().getAccountIdentifier()));
			}
			subscriberRec.setAccount(accountRec);
		}
		
		persistence.updateObject(com.ibm.scas.analytics.persistence.beans.Subscriber.class, subscriberRec);
	}

	@Override
	public int getCurrentUsageForAccount(String accountId) throws CPEException {
		List<com.ibm.scas.analytics.persistence.beans.Cluster> clusters = persistence.getObjectsBy(com.ibm.scas.analytics.persistence.beans.Cluster.class, new WhereClause("owner.account.id", accountId), new WhereClause("owner.type", com.ibm.scas.analytics.persistence.beans.Subscriber.SYSTEM, false));
		int currentUsage = 0;
		if (clusters == null)
			return currentUsage;
		
		for (com.ibm.scas.analytics.persistence.beans.Cluster c : clusters) {
			currentUsage += c.getSize();
		}
		
		return currentUsage;
	}
}
