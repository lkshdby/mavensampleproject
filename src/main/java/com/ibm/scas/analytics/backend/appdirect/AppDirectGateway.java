package com.ibm.scas.analytics.backend.appdirect;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.persist.Transactional;
import com.ibm.scas.analytics.EngineProperties;
import com.ibm.scas.analytics.backend.ProvisioningService;
import com.ibm.scas.analytics.backend.TenantService;
import com.ibm.scas.analytics.backend.monitoring.NagiosEventLogger;
import com.ibm.scas.analytics.beans.BillingItem;
import com.ibm.scas.analytics.beans.ClusterRequest;
import com.ibm.scas.analytics.beans.NotificationEvent;
import com.ibm.scas.analytics.beans.NotificationResult;
import com.ibm.scas.analytics.beans.User;
import com.ibm.scas.analytics.beans.utils.BeanConversionUtil;
import com.ibm.scas.analytics.persistence.PersistenceException;
import com.ibm.scas.analytics.persistence.PersistenceService;
import com.ibm.scas.analytics.persistence.beans.Account;
import com.ibm.scas.analytics.persistence.beans.CPELocation;
import com.ibm.scas.analytics.persistence.beans.Cluster;
import com.ibm.scas.analytics.persistence.beans.Offering;
import com.ibm.scas.analytics.persistence.beans.Subscriber;
import com.ibm.scas.analytics.persistence.util.WhereClause;
import com.ibm.scas.analytics.provider.ServiceProvider;
import com.ibm.scas.analytics.provider.impl.ServiceProviderPluginFactory;
import com.ibm.scas.analytics.utils.CPEException;
import com.ibm.scas.analytics.utils.InvalidPromoCodeException;
import com.ibm.scas.analytics.utils.PromoCode;
import com.ibm.scas.analytics.utils.SSLUtils;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;

/**
 * The main entry point of AppDirect event handling logic
 * 
 * @author Han Chen
 *
 */
public class AppDirectGateway {
	private static final Logger logger = Logger.getLogger(AppDirectGateway.class);
	
	private static final String ORDER = "SUBSCRIPTION_ORDER";
	private static final String CANCEL = "SUBSCRIPTION_CANCEL";
	private static final String CHANGE = "SUBSCRIPTION_CHANGE";
	private static final String NOTICE = "SUBSCRIPTION_NOTICE";

	private static final String ASSIGN = "USER_ASSIGNMENT";
	private static final String UNASSIGN = "USER_UNASSIGNMENT";
	
	private static final String ADDON_ORDER = "ADDON_ORDER";
	private static final String ADDON_CANCEL = "ADDON_CANCEL";

	private static final String CLOSED_NOTICE = "CLOSED";
	
	private static final NotificationResult UNHANDLED = new NotificationResult("true", "Unhandled event type", null);

	private final static Boolean isHub;
	private final static String myLocationName;
	private final static String adminApiKey;
	private final static String dgwAddonCode;
	
	static {
		isHub = Boolean.valueOf(EngineProperties.getInstance().getProperty(EngineProperties.CPE_LOCATION_IS_HUB, EngineProperties.DEFAULT_CPE_LOCATION_IS_HUB));
		myLocationName = EngineProperties.getInstance().getProperty(EngineProperties.CPE_LOCATION_NAME, EngineProperties.DEFAULT_CPE_LOCATION_NAME);
		adminApiKey = EngineProperties.getInstance().getProperty(EngineProperties.ADMIN_API_KEY);
		dgwAddonCode = EngineProperties.getInstance().getProperty(EngineProperties.DGW_ADDON_CODE, EngineProperties.DEFAULT_DGW_ADDON_CODE);
	}

	private final boolean isDummy;
	
	@Inject private TenantService tenantService;
	@Inject private PersistenceService persistence;
	@Inject private ProvisioningService engine;
	@Inject private ServiceProviderPluginFactory pluginFactory;
	

	/**
	 * 
	 * @param isDummy indicate if the gateway if operating in dummy mode, which is useful for AppDirect integration test
	 */
	@AssistedInject
	public AppDirectGateway(@Assisted boolean isDummy) {
		// Create a sub clsas for testing that sets isDummy to true
		this.isDummy = isDummy;
	}

	/**
	 * handle an AppDirect event
	 * 
	 * @param offeringId
	 * @param event
	 * @return a POJO representing the result that's compatible with AppDirect
	 */
	@Transactional(rollbackOn={AppDirectGatewayException.class, RuntimeException.class})
	public NotificationResult processEvent(String offeringId, NotificationEvent event) throws AppDirectGatewayException {
		NotificationResult result;

		if (logger.isDebugEnabled()) {
			String json = new GsonBuilder().setPrettyPrinting().create().toJson(event);
			logger.debug("Event:\n" + json);
		}

		String type = event.getType();
		if (logger.isInfoEnabled()) {
			User creator = event.getCreator();
			User user = event.getPayload().getUser();
			logger.info("Event type: " + type);
			logger.info("Creator: " + creator);
			logger.info("User: " + user);
		}

		if (ORDER.equals(type)) {
			result = handleSubscriptionOrder(offeringId, event);
		} else if (CANCEL.equals(type)) {
			result = handleSubscriptionCancel(offeringId, event);
		} else if (CHANGE.equals(type)) {
			result = handleSubscriptionChange(offeringId, event);
		} else if (NOTICE.equals(type)) {
			result = handleSubscriptionStatus(offeringId, event);
		} else if (ASSIGN.equals(type)) {
			result = handleUserAssignment(offeringId, event);
		} else if (UNASSIGN.equals(type)) {
			result = handleUserUnassignment(offeringId, event);
		} else if (ADDON_ORDER.equals(type)) {
			result = handleAddonOrder(offeringId, event);
		} else if (ADDON_CANCEL.equals(type)) {
			result = handleAddonCancel(offeringId, event);
		} else {
			logger.warn("Unhandled event type!");
			result = UNHANDLED;
		}

		if (logger.isDebugEnabled()) {
			String json = new GsonBuilder().setPrettyPrinting().create().toJson(result);
			logger.debug("Response:\n" + json);
		}

		return result;
	}
	
	@Transactional(rollbackOn={AppDirectGatewayException.class, RuntimeException.class})
	private NotificationResult handleAddonOrder(String offeringId, NotificationEvent event) throws AppDirectGatewayException {
		if (isDummy) {
			logger.info("Dummy event. No action is taken!");
			return new NotificationResult("true", "Dummy subscription created", "-1");
		}
		
		/*
		 * extract expiration time if configured
		 */
		long expirationTime = PromoCode.NO_EXPIRATION;
		Map<String, String> orderConfiguration = event.getPayload().getConfiguration();
		String promoCode = orderConfiguration.get("promoCode");
		if (promoCode != null) {
			try {
				expirationTime = PromoCode.extractExpiration(promoCode);
			} catch (InvalidPromoCodeException e) {
				logger.error("Invalid promo code: " + promoCode + ". This should not have happened!");
			}
		}
		
		String rcvdAddonCode = event.getPayload().getOrder().getAddonOfferingCode();
		
		if(rcvdAddonCode != null && rcvdAddonCode.equals(dgwAddonCode))
		{
			try {
				String accountId = event.getPayload().getAccount().getAccountIdentifier();
				Account account = persistence.getObjectById(Account.class, accountId);
				if(account == null)
				{
					logger.error("Specified account does not exist in CPE.");
					return new NotificationResult("false", "Failed to retrieve account", "-1");
				}
				
				Subscriber subscriber = null;
				List<Subscriber> subscribers = persistence.getObjectsBy(Subscriber.class, new WhereClause("account.id", account.getId()), new WhereClause("name", "_SYSTEM_"), new WhereClause("type", Subscriber.SYSTEM));
				if(subscribers == null || subscribers.size() == 0)
				{
					subscriber = new Subscriber();
					subscriber.generateRandomApiKey();
					subscriber.setAccount(account);
					subscriber.setType(Subscriber.SYSTEM);
					subscriber.setExternalId("");
					subscriber.setName("_SYSTEM_");
					persistence.saveObject(Subscriber.class, subscriber);
					
					subscribers = persistence.getObjectsBy(Subscriber.class, new WhereClause("account.id", account.getId()), new WhereClause("name", "_SYSTEM_"), new WhereClause("type", Subscriber.SYSTEM));
					subscriber = subscribers.get(0);
				}
				else
				{
					subscriber = subscribers.get(0);
				}
				
				Map<String, String> accountProperties = account.getProperties();
				accountProperties.put(Account.ACCOUNT_PROPS_DGW, "true");
				persistence.updateObject(Account.class, account);
				
				ClusterRequest clusterRequest = new ClusterRequest();
				clusterRequest.setName("Dedicated Gateway");
				clusterRequest.setDescription("Dedicated Gateway for Account");
				clusterRequest.setSize(1);
				Map<String, String>	clusterParams = new HashMap<String, String>();
				clusterParams.put(Cluster.CLUSTER_PROP_CLUSTERTYPE, Cluster.CLUSTERTYPE_DEDICATED_GATEWAY);
				clusterParams.put(Cluster.CLUSTER_PROP_CPELOCATIONNAME, myLocationName);
				clusterRequest.setParameters(clusterParams);
				
				engine.createCluster(subscriber.getId(), clusterRequest, Locale.ENGLISH);
				
				List<Cluster> clusters = persistence.getObjectsBy(Cluster.class, new WhereClause("name", "Dedicated Gateway"), new WhereClause("owner.id", subscriber.getId()));
				if(clusters.size() == 1)
				{
					new NagiosEventLogger().emitWarning(String.format("NAGIOSMONITOR DEDICATEDGATEWAYORDER :Dedicated Gateway has been ordered by account %s with id : %s", accountId, clusters.get(0).getId()));
					return new NotificationResult("true", "Addon Added", String.valueOf(accountId));
				}
			} catch (CPEException e) {
				throw new AppDirectGatewayException(e, "-1");
			}
		}
		
		return UNHANDLED;
	}

	@Transactional(rollbackOn={AppDirectGatewayException.class, RuntimeException.class})
	private NotificationResult handleAddonCancel(String offeringId, NotificationEvent event) throws AppDirectGatewayException {
			
		if (isDummy) {
			logger.info("Dummy event. No action is taken!");
			return new NotificationResult("true", "Dummy subscription created", "-1");
		}
		
		try {
			String accountId = event.getPayload().getAccount().getAccountIdentifier();
			Account account = persistence.getObjectById(Account.class, accountId);
			Map<String, String> accountProperties = account.getProperties();
			if(accountProperties != null && accountProperties.keySet().contains(Account.ACCOUNT_PROPS_DGW) 
					&& accountProperties.get(Account.ACCOUNT_PROPS_DGW).equals("true"))
			{
					List<Cluster> clusters = persistence.getObjectsBy(Cluster.class, new WhereClause("owner.account.id", accountId));
					if(clusters != null && clusters.size() > 1)
					{
						return new NotificationResult("false", "Cannot Cancel Addon Subscription as one or more clusters are using it.", String.valueOf(accountId));
					}
					else
					{
						account.getProperties().remove(Account.ACCOUNT_PROPS_DGW);
						persistence.updateObject(Account.class, account);
						
						Cluster dgwCluster = clusters.get(0);
						engine.deleteCluster(dgwCluster.getOwner().getId(), dgwCluster.getId(), Locale.ENGLISH);
					}
					new NagiosEventLogger().emitWarning(String.format("NAGIOSMONITOR DEDICATEDGATEWAYORDER :Dedicated Gateway has been cancelled by account %s with id : %s", accountId, clusters.get(0).getId()));
					return new NotificationResult("true", "Addon subscription has been cancelled.", String.valueOf(accountId));
			}
			
		} catch (CPEException e) {
			throw new AppDirectGatewayException(e, "-1");
		}
		
		return UNHANDLED;
	}

	/**
	 * handles subscription order event
	 * 
	 * @param offeringId
	 * @param event
	 * @return
	 */
	protected NotificationResult handleSubscriptionOrder(String offeringId, NotificationEvent event) throws AppDirectGatewayException {
		if (isDummy) {
			logger.info("Dummy event. No action is taken!");
			return new NotificationResult("true", "Dummy subscription created", "-1");
		}
		/*
		 * extract expiration time if configured
		 */
		long expirationTime = PromoCode.NO_EXPIRATION;
		Map<String, String> orderConfiguration = event.getPayload().getConfiguration();
		String promoCode = orderConfiguration.get("promoCode");
		if (promoCode != null) {
			try {
				expirationTime = PromoCode.extractExpiration(promoCode);
			} catch (InvalidPromoCodeException e) {
				logger.error("Invalid promo code: " + promoCode + ". This should not have happened!");
			}
		}

		/*
		 * extract limit if present in the order
		 */
		int quantity = extractServerQuantity(offeringId, event);

		/*
		 * extract edition code and status
		 * TODO: need a mechanism to figure out if the initial state should be FREE_TRIAL or ACTIVE
		 */
		String edition = event.getPayload().getOrder().getEditionCode();

		/*
		 * create account
		 */
		try {
			final Account account = new Account();
			final Offering offering = persistence.getObjectById(Offering.class, offeringId);
			account.setOffering(offering);
			account.setMarketUrl(event.getMarketplace().getBaseUrl());
			account.setPartner(event.getMarketplace().getPartner());
			account.setExpiration(expirationTime);
			account.setQuantity(quantity);
			account.setEdition(edition);
			//		account.setState(state);
			persistence.saveObject(Account.class, account);

			String accountId = account.getId();
			logger.debug("Account is added with Id : " + accountId);

			if (accountId == null) {
				return new NotificationResult("false", "Failed to create account", "-1");
			}
			/*
			 * create first subscriber
			 */
			User creator = event.getCreator();
			final Subscriber subscriber = new Subscriber();
			subscriber.generateRandomApiKey();
			subscriber.setAccount(account);
			subscriber.setType(Subscriber.APPDIRECT);
			subscriber.setExternalId(creator.getOpenId());
			subscriber.setName(creator.getFullName());
			persistence.saveObject(Subscriber.class, subscriber);
			String subscriberId = subscriber.getId();

			logger.debug("Subscriber is added with Id : " + subscriberId);
			if (subscriberId == null) {
				return new NotificationResult("false", "Failed to create subscriber", "-1");
			}

			// If its the hub then replicate the account and subscriber data to the spoke.
			if (isHub) {
				List<CPELocation> cpeLocations = persistence.getAllObjects(CPELocation.class);

				for (CPELocation cpeLoc : cpeLocations) {
					// Continue if cpe location is my location because we already saved account and subscribers above.
					if (cpeLoc.getName().equals(myLocationName))
						continue;

					final String spokeBaseUrl = cpeLoc.getUrl();

					final ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
					config.getClasses().add(JacksonJaxbJsonProvider.class);
					final Client client = Client.create(config);

					Gson gson = new Gson(); 
					final com.ibm.scas.analytics.beans.Account acc = BeanConversionUtil.convertToBean(account);
					final String accReq = gson.toJson(acc);
					logger.debug("Account Formatted to String : " + accReq);
					logger.debug("Spoke URL : " + spokeBaseUrl + "/accounts");
					client.resource(spokeBaseUrl + "/accounts").type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_TYPE).header("api-key", adminApiKey).post(accReq);

					final com.ibm.scas.analytics.beans.Subscriber sub = BeanConversionUtil.convertToBean(subscriber);
					final String subReq = gson.toJson(sub);
					logger.debug("Subscriber Formatted to String : " + subReq);
					logger.debug("Spoke URL : " + spokeBaseUrl + "/subscribers");
					client.resource(spokeBaseUrl + "/subscribers").type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_TYPE).header("api-key", adminApiKey).post(subReq);
				}
			}
			return new NotificationResult("true", "Subscription created", String.valueOf(accountId));
			
		} catch (CPEException e) {
			throw new AppDirectGatewayException(e, "-1");
		}
	}

	/**
	 * handles subscription cancel event
	 * 
	 * @param offeringId
	 * @param event
	 * @return
	 */
	protected NotificationResult handleSubscriptionCancel(String offeringId, NotificationEvent event) throws AppDirectGatewayException {
		if (isDummy) {
			logger.info("Dummy event. No action is taken!");
			return new NotificationResult("true", "Dummy subscription canceled", "-1");
		}
		String accountId = event.getPayload().getAccount().getAccountIdentifier();
		logger.info("Account id: " + accountId);
		try {
			this.deleteAccount(accountId);
		} catch (CPEException e) {
			throw new AppDirectGatewayException(e, String.valueOf(accountId));
		}

		return new NotificationResult("true", "Subscription canceled", String.valueOf(accountId));
	}

	/**
	 * handles subscription change event
	 * 
	 * @param offeringId
	 * @param event
	 * @return
	 */
	protected NotificationResult handleSubscriptionChange(String offeringId, NotificationEvent event) throws AppDirectGatewayException {
		if (isDummy) {
			logger.info("Dummy event. No action is taken!");
			return new NotificationResult("true", "Dummy subscription updated", "-1");
		}
		String accountId = event.getPayload().getAccount().getAccountIdentifier();
		logger.info("Account id: " + accountId);

		try {
			Account currentAccount = persistence.getObjectById(Account.class, accountId);
			String currentEdition = currentAccount.getEdition();
			int currentState = currentAccount.getState();
			int currentQuantity = currentAccount.getQuantity();
			int currentUsage = this.tenantService.getCurrentUsageForAccount(accountId);

			String newEdition = event.getPayload().getOrder().getEditionCode();
			String newStatus = event.getPayload().getAccount().getStatus();
			int newState = Account.parseState(newStatus);
			/*
			 * extract limit if present in the order
			 */
			int newQuantity = extractServerQuantity(offeringId, event);

			boolean changed = false;
			if (!newEdition.equals(currentEdition)) {
//				if (currentUsage > 0) {
//					logger.error("Cannot changed edition while there are existing clusters");
//					return new NotificationResult("false", "Cannot changed edition while there are existing clusters", String.valueOf(accountId));
//				}
				currentAccount.setEdition(newEdition);
				changed = true;
				logger.debug("Edition: " + currentEdition + " -> " + newEdition);
			}
			if (newState != currentState) {
				currentAccount.setState(newState);
				changed = true;
				logger.debug("Account state: " + currentState + " -> " + newState);
			}
			if (newQuantity < currentUsage) {
				logger.error("Cannot reduce server quantity below current usage");
				return new NotificationResult("false", "Cannot reduce server quantity below current usage", String.valueOf(accountId));
			}
			if (newQuantity != currentQuantity) {
				currentAccount.setQuantity(newQuantity);
				changed = true;
				logger.debug("Quantity: " + currentQuantity + " -> " + newQuantity);
			}

			if (changed) {
				persistence.updateObject(Account.class, currentAccount);

				// If its the hub then replicate the account and subscriber data to the spoke.
				if (isHub) {
					List<CPELocation> cpeLocations = persistence.getAllObjects(CPELocation.class);

					for (CPELocation cpeLoc : cpeLocations) {
						// Continue if cpe location is my location because we already saved account and subscribers above.
						if (cpeLoc.getName().equals(myLocationName))
							continue;

						final String spokeBaseUrl = cpeLoc.getUrl();

						final ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
						config.getClasses().add(JacksonJaxbJsonProvider.class);
						final Client client = Client.create(config);

						final com.ibm.scas.analytics.beans.Account acc = BeanConversionUtil.convertToBean(currentAccount);
						Gson gson = new Gson();
						final String accReq = gson.toJson(acc);

						client.resource(spokeBaseUrl + "/accounts/" + accountId).type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_TYPE).header("api-key", adminApiKey).put(accReq);
					}
				}

			} else {
				logger.warn("No changes are detected in subscription change event!");
			}
		} catch (CPEException e) {
			throw new AppDirectGatewayException(e, accountId);
		}

		return new NotificationResult("true", "Subscription updated", String.valueOf(accountId));
	}

	/**
	 * handles subscription status notification
	 * 
	 * @param offeringId
	 * @param event
	 * @return
	 */
	protected NotificationResult handleSubscriptionStatus(String offeringId, NotificationEvent event) throws AppDirectGatewayException {
		if (isDummy) {
			logger.info("Dummy event. No action is taken!");
			return new NotificationResult("true", "Dummy subscription notice processed", "-1");
		}
		String accountId = event.getPayload().getAccount().getAccountIdentifier();
		logger.info("Account id: " + accountId);

		String type = event.getPayload().getNotice().getType();
		if (CLOSED_NOTICE.equals(type)) {
			logger.info("Delinquent account is being closed");
			try {
				this.deleteAccount(accountId);
			} catch (CPEException e) {
				throw new AppDirectGatewayException(e, "-1");
			}

			return new NotificationResult("true", "Subscription canceled", String.valueOf(accountId));
		} 

		Account currentAccount;
		try {
			currentAccount = persistence.getObjectById(Account.class, accountId);
		} catch (PersistenceException e) {
			throw new AppDirectGatewayException(e, "-1");
		}
		
		if (currentAccount == null) {
			logger.error("Account not found!");
			return new NotificationResult("false", "ACCOUNT_NOT_FOUND", "Subscription notice not processed", String.valueOf(accountId));
		}
		
		int currentState = currentAccount.getState();
		String newStatus = event.getPayload().getAccount().getStatus();
		int newState = Account.parseState(newStatus);
		
		boolean changed = false;
		if (newState != currentState) {
			currentAccount.setState(newState);
			changed = true;
			logger.debug("Account state: " + currentState + " -> " + newState);
		}
		
		if (changed) {
			try {
				persistence.updateObject(Account.class, currentAccount);

				if (newState == Account.FREE_TRIAL_EXPIRED) {
					logger.info("Free trial has expired for account " + accountId + ". Deleting all clusters.");
					engine.deleteAllClusters(accountId);
				}

				// If its the hub then update the account in the spoke.
				if (isHub) {
					List<CPELocation> cpeLocations = persistence.getAllObjects(CPELocation.class);

					for (CPELocation cpeLoc : cpeLocations) {
						// Continue if cpe location is my location because we already saved account and subscribers above.
						if (cpeLoc.getName().equals(myLocationName))
							continue;

						final String spokeBaseUrl = cpeLoc.getUrl();

						final ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
						config.getClasses().add(JacksonJaxbJsonProvider.class);
						final Client client = Client.create(config);

						final com.ibm.scas.analytics.beans.Account acc = BeanConversionUtil.convertToBean(currentAccount);
						Gson gson = new Gson();
						final String accReq = gson.toJson(acc);

						client.resource(spokeBaseUrl + "/accounts/" + accountId).type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_TYPE).header("api-key", adminApiKey).put(accReq);
					}
				}
			} catch (CPEException e) {
				throw new AppDirectGatewayException(e, "-1");
			}
		} else {
			logger.warn("No changes are detected in subscription change event!");
		}
			
		return new NotificationResult("true", "Subscription notice processed", String.valueOf(accountId));
	}

	/**
	 * handles user assignment event
	 * 
	 * @param offeringId
	 * @param event
	 * @return
	 */
	protected NotificationResult handleUserAssignment(String offeringId, NotificationEvent event) throws AppDirectGatewayException {
		User user = event.getPayload().getUser();
		if (isDummy) {
			logger.info("Dummy event. No action is taken!");
			return new NotificationResult("true", user + " assigned", "-1");
		}
		
		String accountId = event.getPayload().getAccount().getAccountIdentifier();
		logger.info("Account id: " + accountId);
		try {
			final List<Subscriber> archived = persistence.getObjectsBy(Subscriber.class, new WhereClause("account.id", accountId), new WhereClause("externalId", user.getOpenId()), new WhereClause("type", Subscriber.APPDIRECT_ARCHIVED));
			if (archived == null || archived.isEmpty()) {
				logger.debug(" - new user. creating new subscriber...");
				Subscriber subscriber = new Subscriber();
				subscriber.generateRandomApiKey();
				final Account account = persistence.getObjectById(Account.class, accountId);
				subscriber.setAccount(account);
				subscriber.setType(Subscriber.APPDIRECT);
				subscriber.setExternalId(user.getOpenId());
				subscriber.setName(user.getFullName());
				persistence.saveObject(Subscriber.class, subscriber);
				
				// If its the hub then replicate the account and subscriber data to the spoke.
				if (isHub) {
					List<CPELocation> cpeLocations = persistence.getAllObjects(CPELocation.class);
					
					for (CPELocation cpeLoc : cpeLocations) {
						// Continue if cpe location is my location because we already saved account and subscribers above.
						if (cpeLoc.getName().equals(myLocationName))
							continue;

						final String spokeBaseUrl = cpeLoc.getUrl();
						
						final ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
						config.getClasses().add(JacksonJaxbJsonProvider.class);
						final Client client = Client.create(config);
						
						final com.ibm.scas.analytics.beans.Subscriber sub = BeanConversionUtil.convertToBean(subscriber);
						Gson gson = new Gson();
						final String subReq = gson.toJson(sub);
						
						client.resource(spokeBaseUrl + "/subscribers/").type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_TYPE).header("api-key", adminApiKey).post(subReq);
					}
				}
			} else {
				logger.debug(" - archived user. activating existing subscriber...");
				Subscriber subscriber = archived.get(0);
				subscriber.setType(Subscriber.APPDIRECT);
				persistence.updateObject(Subscriber.class, subscriber);
				
				if (isHub) {
					List<CPELocation> cpeLocations = persistence.getAllObjects(CPELocation.class);
					
					for (CPELocation cpeLoc : cpeLocations) {
						// Continue if cpe location is my location because we already saved account and subscribers above.
						if (cpeLoc.getName().equals(myLocationName))
							continue;

						final String spokeBaseUrl = cpeLoc.getUrl();
						
						final ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
						config.getClasses().add(JacksonJaxbJsonProvider.class);
						final Client client = Client.create(config);
						
						final com.ibm.scas.analytics.beans.Subscriber sub = BeanConversionUtil.convertToBean(subscriber);
						Gson gson = new Gson();
						final String subReq = gson.toJson(sub);
						client.resource(spokeBaseUrl + "/subscribers/" + subscriber.getId()).type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_TYPE).header("api-key", adminApiKey).put(subReq);
					}
				}
			}
		} catch (CPEException e) {
			throw new AppDirectGatewayException(e, String.valueOf(accountId));
		}
		
		return new NotificationResult("true", user + " assigned", String.valueOf(accountId));
	}

	/**
	 * handles user unassignment event
	 * 
	 * @param offeringId
	 * @param event
	 * @return
	 */
	protected NotificationResult handleUserUnassignment(String offeringId, NotificationEvent event) throws AppDirectGatewayException {
		User user = event.getPayload().getUser();
		if (isDummy) {
			logger.info("Dummy event. No action is taken!");
			return new NotificationResult("true", user + " unassigned", "-1");
		}
		
		String accountId = event.getPayload().getAccount().getAccountIdentifier();
		logger.info("Account id: " + accountId);
		
		try {
			final List<Subscriber> subscribers = persistence.getObjectsBy(Subscriber.class, new WhereClause("account.id", accountId), new WhereClause("externalId", user.getOpenId()), new WhereClause("type", Subscriber.APPDIRECT));
			if (subscribers == null || subscribers.isEmpty()) {
				return new NotificationResult("false", "ACCOUNT_NOT_FOUND", "Subscription notice not processed", String.valueOf(accountId));
			}
			Subscriber archived = subscribers.get(0);
			archived.setType(Subscriber.APPDIRECT_ARCHIVED);
			persistence.updateObject(Subscriber.class, archived);
			
			if (isHub) {
				List<CPELocation> cpeLocations = persistence.getAllObjects(CPELocation.class);
				
				for (CPELocation cpeLoc : cpeLocations) {
					// Continue if cpe location is my location because we already saved account and subscribers above.
					if (cpeLoc.getName().equals(myLocationName))
						continue;

					final String spokeBaseUrl = cpeLoc.getUrl();
					
					final ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
					config.getClasses().add(JacksonJaxbJsonProvider.class);
					final Client client = Client.create(config);
					
					final com.ibm.scas.analytics.beans.Subscriber sub = BeanConversionUtil.convertToBean(archived);
					Gson gson = new Gson();
					final String subReq = gson.toJson(sub);
					client.resource(spokeBaseUrl + "/subscribers/" + archived.getId()).type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_TYPE).header("api-key", adminApiKey).delete();
				}
			}
			
		} catch (CPEException e) {
			throw new AppDirectGatewayException(e, String.valueOf(accountId));
		}
//			engine.deleteSubscriber(accountId, subscriber.getId());
		return new NotificationResult("true", user + " unassigned", String.valueOf(accountId));
	}
	
	public int extractServerQuantity(String offeringId, NotificationEvent event) throws AppDirectGatewayException {
		int quantity = 0;
		List<BillingItem> items = event.getPayload().getOrder().getItems();
		if (items != null) {
			String unit = EngineProperties.getInstance().getProperty(EngineProperties.MARKETPLACE_ORDER_UNIT, EngineProperties.DEFAULT_MARKETPLACE_ORDER_UNIT);
			for (BillingItem item : items) {
				if (unit.equals(item.getUnit())) {
					quantity = item.getQuantity();
					break;
				}
			}
		}
		if (quantity <= 0) {
			ServiceProvider plugin;
			try {
				plugin = pluginFactory.getPlugin(offeringId);
	
				if (plugin == null) {
					logger.error("Failed to obtain service provider plugin");
				} else {
					String editionCode = event.getPayload().getOrder().getEditionCode();
					quantity = plugin.getEditionQuota(editionCode);
					logger.debug("Quota for \"" + editionCode + "\" edition is " + quantity);
				}
			} catch (CPEException e) {
				throw new AppDirectGatewayException(e, "-1");
			}
		}
		return quantity;
	}
	
	private void deleteAccount(String accountId) throws CPEException {
		tenantService.deleteAccount(accountId);
		
		// If its the hub then delete the account in the spoke.
		if (isHub) {
			List<CPELocation> cpeLocations = persistence.getAllObjects(CPELocation.class);
			
			for (CPELocation cpeLoc : cpeLocations) {
				// Continue if cpe location is my location because we already deleted account above.
				if (cpeLoc.getName().equals(myLocationName))
					continue;

				final String spokeBaseUrl = cpeLoc.getUrl();
				
				final ClientConfig config = SSLUtils.getTrustingSSLClientConfig();
				config.getClasses().add(JacksonJaxbJsonProvider.class);
				final Client client = Client.create(config);
				
				client.resource(spokeBaseUrl + "/accounts/" + accountId).type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_TYPE).header("api-key", adminApiKey).delete();
			}
		}
	}
}
