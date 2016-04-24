package com.ibm.scas.analytics.backend;

import java.util.List;

import com.ibm.scas.analytics.beans.Account;
import com.ibm.scas.analytics.beans.Offering;
import com.ibm.scas.analytics.beans.Subscriber;
import com.ibm.scas.analytics.beans.Subscriber.SubscriberType;
import com.ibm.scas.analytics.utils.CPEException;

/**
 * Managed Accounts and Subscribers
 * @author jkwong
 *
 */
public interface TenantService {
	
	public int getCurrentUsageForAccount(String accountId) throws CPEException;
	
	/**
	 * get all accounts
	 * @return
	 * @throws CPEException
	 */
	List<Account> getAllAccounts() throws CPEException;
	
	/**
	 * get all accounts for a particular offering
	 * @return
	 * @throws CPEException
	 */
	List<Account> getAccountsForOffering(String offeringId) throws CPEException;
	
	/**
	 * Get an offering by ID
	 * @param offeringId
	 * @return
	 * @throws CPEException
	 */
	Offering getOffering(String offeringId) throws CPEException;
	
	/**
	 * Get an account by ID
	 * @param accountId
	 * @return
	 * @throws CPEException
	 */
	Account getAccountById(String accountId) throws CPEException;
	
	/**
	 * Get all subscribers
	 * @return
	 * @throws CPEException
	 */
	List<Subscriber> getSubscribers(String accountId, String externalId, SubscriberType type) throws CPEException;
	
	/**
	 * Get a subscriber by ID
	 * @param subscriberId
	 * @return
	 * @throws CPEException
	 */
	Subscriber getSubscriberById(String subscriberId) throws CPEException;
	
	/**
	 * Create an account
	 * @param account
	 * @return accountID
	 * @throws CPEException
	 */
	String createAccount(Account account) throws CPEException;

	/**
	 * Create an account
	 * @param account
	 * @throws CPEException
	 */
	void modifyAccount(String accountId, Account account) throws CPEException;
	
	/**
	 * Create a subscriber
	 * @param subscriber
	 * @return a subscriber
	 * @throws CPEException
	 */
	String createSubscriber(Subscriber subscriber) throws CPEException;
	
	/**
	 * Modify a subscriber
	 * @param subscriber
	 * @throws CPEException
	 */
	void modifySubscriber(Subscriber subscriber) throws CPEException;
	
	
	/**
	 * delete an account
	 * @param accountId
	 * @throws CPEException
	 */
	void deleteAccount(String accountId) throws CPEException;
	
	/**
	 * delete a subscriber
	 * @param accountId
	 * @param subscriberId
	 * @throws CPEException
	 */
	void deleteSubscriber(String accountId, String subscriberId) throws CPEException;
}
