package com.ibm.scas.analytics.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;

import com.ibm.scas.analytics.EngineProperties;
import com.ibm.scas.analytics.persistence.PersistenceException;
import com.ibm.scas.analytics.persistence.beans.Account;
import com.ibm.scas.analytics.persistence.beans.CPELocation;
import com.ibm.scas.analytics.persistence.beans.ContentMap;
import com.ibm.scas.analytics.persistence.beans.Gateway;
import com.ibm.scas.analytics.persistence.beans.Offering;
import com.ibm.scas.analytics.persistence.beans.PcmaeBackend;
import com.ibm.scas.analytics.persistence.beans.Plugin;
import com.ibm.scas.analytics.persistence.beans.SoftLayerAccount;
import com.ibm.scas.analytics.persistence.beans.Subscriber;
import com.ibm.scas.analytics.persistence.beans.Vlan;
import com.ibm.scas.analytics.persistence.util.WhereClause;
import com.ibm.scas.analytics.persistence.util.WhereInClause;
import com.ibm.scas.analytics.utils.AES;
import com.ibm.scas.analytics.utils.CPEException;

public class TestPersistenceService extends BaseTestCase {
	Logger logger = Logger.getLogger(TestPersistenceService.class);
	@Override
	public void setUp() throws Exception {
		super.setUp();
		executeSQLScript("populate_fake_data.sql");
	}

	private List<PcmaeBackend> createPCMAEBackends() throws CPEException {
		final List<PcmaeBackend> allBackends = new ArrayList<PcmaeBackend>();
		for (int i = 0; i < 5; i++) {
			final PcmaeBackend pcmae = new PcmaeBackend();
			pcmae.setId(UUID.randomUUID().toString());
			pcmae.setUrl("https://mapreduce.beta.manage.softlayer.com:8443/pcmwebapi-1.0/rest");
			pcmae.setAccount(UUID.randomUUID().toString());
			pcmae.setUsername("cluster-provisioning-engine");
			pcmae.setPassword(UUID.randomUUID().toString());
			allBackends.add(pcmae);
		}
		
		return allBackends;
	}
	
	private Account createNewAccount() throws Exception {
		Logger.getRootLogger().info("Create new test Account record");
		final List<Offering> allOfferings = service.getAllObjects(Offering.class);
		
		// get the first offering to create an account with
		final Account account = new Account();
		account.setOffering(allOfferings.get(0));
		account.setMarketUrl("https://my-url.com");
		account.setExpiration(12345);
		account.setPartner("blahblah partner");
		account.setQuantity(1);
		account.setState(Account.ACTIVE);
		
        //create new account for update
		service.beginTransaction();
		service.saveObject(Account.class, account);
		service.commitTransaction();
		
		return account;
	}
	
	private Plugin createNewPlugin() throws Exception {
		Logger.getRootLogger().info("Create new test plugin record");
		final Plugin plugin = new Plugin();
		plugin.setId("hortonworks2");
		plugin.setClassName("com.ibm.icas.hortonworks.ServiceProviderPlugin");
		plugin.setSource("file:///RTC_Workspace/hortonworks-plugin/target/classes/");
		
		service.beginTransaction();
		service.saveObject(Plugin.class, plugin);
		service.commitTransaction();
		
		return plugin;
	}
	
	private Offering createNewOffering() throws Exception {
		Logger.getRootLogger().info("Create new test offering");
		final Offering offering = new Offering();
		
		offering.setId("hortonworks-sandbox");
		offering.setOauthKey("ibm-blu-acceleration-for-cloud-7560");
		offering.setOauthSecret("yqRBDBPixp9umw8RpcJlAr7pQGBUWA6T7CwihKzYZpA=");
		offering.setName("hortonworks-test");
		
		Plugin plugin = service.getObjectById(Plugin.class, "hortonworks");
		offering.setPlugin(plugin);
		
		ContentMap urlPath = service.getObjectById(ContentMap.class, "hortonworks");
		offering.setUrlPath(urlPath);
		
		offering.setMultiuser(true);
		
		service.beginTransaction();
		service.saveObject(Offering.class, offering);
		service.commitTransaction();
		
		return offering;
	}	
	
	private Subscriber createNewSubscriber() throws Exception {
		Logger.getRootLogger().info("Create new test subscriber");
		final Subscriber subscriber = new Subscriber();
		subscriber.setId("12345");
		subscriber.setApiKey("API123");
		subscriber.setType(1);
		
		Account account = createNewAccount();
		subscriber.setAccount(account);
		
		subscriber.setExternalId("");
		subscriber.setName("Garbage Collector");
		
		service.beginTransaction();
		service.saveObject(Subscriber.class, subscriber);
		service.commitTransaction();		
		
		return subscriber;
	}	
	
	public void testCreatePCMAEBackend() throws Exception {
		logger.info("****** testCreatePCMAEBackend()");
		Logger.getRootLogger().debug(String.format("%s.testCreatePCMAEBackend()", this.getClass().getSimpleName()));
		service.beginTransaction();
		
		final List<PcmaeBackend> testPcmaes = this.createPCMAEBackends();
		Logger.getRootLogger().debug("Inserting " + testPcmaes.size() + " PCMAE records ...");
		for (final PcmaeBackend testPcmae : testPcmaes) {
			service.saveObject(PcmaeBackend.class, testPcmae);
		}
		service.commitTransaction();
		
		Logger.getRootLogger().debug("TestPCMAEs: " + testPcmaes.size());
		for (final PcmaeBackend testPcmae : testPcmaes) {
			Logger.getRootLogger().debug(ReflectionToStringBuilder.toString(testPcmae));
		}
		
		Logger.getRootLogger().debug("Get all PCM-AE records");
		final List<PcmaeBackend> allPCMAE = service.getAllObjects(PcmaeBackend.class);
		Logger.getRootLogger().debug("Records returned: " + allPCMAE.size());
		for (final PcmaeBackend testPcmae : allPCMAE) {
			Logger.getRootLogger().debug(ReflectionToStringBuilder.toString(testPcmae));
		}
		for (final PcmaeBackend testPcmae : testPcmaes) {
			// ensure testPcmae that we inserted are in the list of all PCMAE
			assertTrue(allPCMAE.contains(testPcmae));
		}
		
		/* try to get a pcmae by Id */
		final String pcmaeId = testPcmaes.get(0).getId();
		Logger.getRootLogger().debug("Get PCMAE by ID: " + pcmaeId);
		final PcmaeBackend pcmae = service.getObjectById(PcmaeBackend.class, pcmaeId);
		Logger.getRootLogger().debug("Record returned: " + ReflectionToStringBuilder.toString(pcmae));
		
		assertTrue(pcmae.getId().equals(pcmaeId));
		
		
		/* make sure the password is encrypted; use direct sql */
		Logger.getRootLogger().debug("Get PCMAE password by ID: " + pcmaeId);
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			connection = getConnection();
			statement = connection.prepareStatement ("SELECT password FROM PCMAEBACKENDS WHERE id=?");
			statement.setString(1, pcmaeId);

			resultSet = statement.executeQuery();
			if (resultSet != null && resultSet.next()) {
				String password = resultSet.getString(1);
				
				Logger.getRootLogger().debug("Encrypted Password: " + password);
				assertTrue(!password.equals(pcmae.getPassword()));
				assertTrue(password.equals(pcmae.getEncPassword()));
				
				Logger.getRootLogger().debug("Decrypted Password: " + new AES().decrypt(password));
				assertTrue(new AES().decrypt(password).equals(pcmae.getPassword()));
				
				
			} else {
				Logger.getRootLogger().error("SQL query failed.");
			}

		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
			if (connection != null) {
				connection.close();
			}
		}
		
	}
	
	public void testSelectWhereIn() throws Exception {
		logger.info("****** testSelectWhereIn");
		
		service.beginTransaction();

		final CPELocation myLocation = service.getObjectById(CPELocation.class, EngineProperties.getInstance().getProperty(EngineProperties.CPE_LOCATION_NAME, EngineProperties.DEFAULT_CPE_LOCATION_NAME));

		// add SoftLayerAccount
		final SoftLayerAccount slAcct = new SoftLayerAccount();
		slAcct.setUrl("https://api.service.softlayer.com/rest/v3.1");
		slAcct.setUsername("jkwong@ca.ibm.com");
		slAcct.setApiKey(UUID.randomUUID().toString().replace("-", ""));

		service.saveObject(SoftLayerAccount.class, slAcct);

		// add 5 random vlans
		final Random rnd = new Random();

		final List<String> vlanIds = new ArrayList<String>();

		for (int i = 0; i < 5; i++) {
			final Vlan vlan = new Vlan();
			vlan.setSoftLayerId(String.valueOf(rnd.nextInt()));
			vlan.setSoftLayerAccount(slAcct);
			vlan.setLocation(myLocation);

			service.saveObject(Vlan.class, vlan);

			vlanIds.add(vlan.getId());
		}

		service.commitTransaction();

		// Now select by 3 of the VLANs
		for (int i = 1; i < vlanIds.size(); i++) {
			final List<String> subList = vlanIds.subList(0, i);
			final List<String> setDiff = vlanIds.subList(i, vlanIds.size());
			logger.info(String.format("query vlans where id in: %s, set diff: %s", subList, setDiff));
			final List<Vlan> queryVlan = service.getObjectsBy(Vlan.class, new WhereInClause("id", subList));

			for (final Vlan qV : queryVlan) {
				// make sure that it's in the subList
				assertTrue(subList.contains(qV.getId()));
				assertTrue(!setDiff.contains(qV.getId()));
			}
		}
	}
	
	public void testSelectWhereNull() throws Exception {
		logger.info("****** testSelectWhereNull");
		final CPELocation myLocation = service.getObjectById(CPELocation.class, EngineProperties.getInstance().getProperty(EngineProperties.CPE_LOCATION_NAME, EngineProperties.DEFAULT_CPE_LOCATION_NAME));
		service.beginTransaction();
		// add SoftLayerAccount
		final SoftLayerAccount slAcct = new SoftLayerAccount();
		slAcct.setUrl("https://api.service.softlayer.com/rest/v3.1");
		slAcct.setUsername("jkwong@ca.ibm.com");
		slAcct.setApiKey(UUID.randomUUID().toString().replace("-", ""));

		service.saveObject(SoftLayerAccount.class, slAcct);

		final Random rnd = new Random();
		// add a gateway
		final Gateway gateway = new Gateway();
		gateway.setSoftLayerAccount(slAcct);
		gateway.setSoftLayerId(String.valueOf(rnd.nextInt()));
		gateway.setType("SHARED");
		gateway.setLocation(myLocation);
		service.saveObject(Gateway.class, gateway);

		// add 3 floating VLANs
		for (int i = 0; i < 3; i++) {
			final Vlan vlan = new Vlan();
			vlan.setSoftLayerId(String.valueOf(rnd.nextInt()));
			vlan.setSoftLayerAccount(slAcct);
			vlan.setLocation(myLocation);

			service.saveObject(Vlan.class, vlan);
		}

		// add some vlans with gateway
		for (int i = 0; i < 3; i++) {
			final Vlan vlan = new Vlan();
			vlan.setSoftLayerId(String.valueOf(rnd.nextInt()));
			vlan.setSoftLayerAccount(slAcct);
			vlan.setGateway(gateway);
			vlan.setLocation(myLocation);

			service.saveObject(Vlan.class, vlan);
		}

		service.commitTransaction();

		// Now select gateway null
		final List<Vlan> queryVlan = service.getObjectsBy(Vlan.class, new WhereClause("gateway", null));

		for (final Vlan qV : queryVlan) {
			assertTrue(qV.getGateway() == null);
		}
	}
	
	public void testCreateAccount() throws Exception {
		System.out.println(String.format("%s.testCreateAccount()", this.getClass().getSimpleName()));
		
		service.beginTransaction();
		final List<Offering> allOfferings = service.getAllObjects(Offering.class);
		
		// get the first offering to create an account with
		final Account account = new Account();
		account.setOffering(allOfferings.get(0));
		account.setMarketUrl("https://my-url.com");
		account.setExpiration(12345);
		account.setPartner("blahblah partner");
		account.setQuantity(1);
		account.setState(Account.ACTIVE);
		
		assertTrue(!service.isPersisted(Account.class, account));
		
		service.saveObject(Account.class, account);
		
		assertTrue(service.isPersisted(Account.class, account));

		/* check the record is not added yet, since we did not commit, even though the entity is managed */
		Logger.getRootLogger().debug("Get Accounts by ID: " + account.getId());
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			connection = getConnection();
			statement = connection.prepareStatement ("SELECT count(*) FROM ACCOUNTS WHERE id=?");
			statement.setString(1, account.getId());

			resultSet = statement.executeQuery();
			if (resultSet != null && resultSet.next()) {
				int count = resultSet.getInt(1);
				
				assertTrue(count == 0);
			} else {
				Logger.getRootLogger().error("SQL query failed.");
			}

		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
			if (connection != null) {
				connection.close();
			}
		}
		
		
		Logger.getRootLogger().info("Before commit transaction, account: " + ReflectionToStringBuilder.toString(account));
		
		service.commitTransaction();
		
		Logger.getRootLogger().info("After commit transaction, account: " + ReflectionToStringBuilder.toString(account));

		/* check the record fields, use direct sql */
		Logger.getRootLogger().debug("Get Account by ID: " + account.getId());
		try {
			connection = getConnection();
			statement = connection.prepareStatement ("SELECT id, offeringId FROM ACCOUNTS WHERE id=?");
			statement.setString(1, account.getId());

			resultSet = statement.executeQuery();
			if (resultSet != null && resultSet.next()) {
				String accountId = resultSet.getString(1);
				String offeringId = resultSet.getString(2);
				
				assertTrue(accountId.equals(account.getId()));
				assertTrue(offeringId.equals(account.getOffering().getId()));
				
				Logger.getRootLogger().info("Account ID: " + accountId);
				Logger.getRootLogger().info("Offering ID: " + offeringId);
				
			} else {
				Logger.getRootLogger().error("SQL query failed.");
			}

		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
			if (connection != null) {
				connection.close();
			}
		}
		
		
	}
	
	
	
	public void testUpdateAccount() throws Exception {
		System.out.println(String.format("%s.testUpdateAccount()", this.getClass().getSimpleName()));
		Logger.getRootLogger().info(String.format("%s.testUpdateAccount()", this.getClass().getSimpleName()));
		
        final Account newAccount = createNewAccount();
        
		//update account
		service.beginTransaction();
		// get the first Account by Id
		final Account account = service.getObjectById(Account.class, newAccount.getId());
		int quantityOriginal = account.getQuantity();
		Logger.getRootLogger().debug("Read Quantity field value before Update : " + quantityOriginal);
		
		Logger.getRootLogger().debug("Update quantity field value for Account id: " + newAccount.getId());
		
		//update quantity
		int quantityNew = 3;
		account.setQuantity(quantityNew);
		
		service.saveObject(Account.class, account);
		
		service.commitTransaction();
		
		Logger.getRootLogger().info("After commit transaction, account: " + ReflectionToStringBuilder.toString(account));

		/* check the record fields if updated */
		final Account accountAfterCommit = service.getObjectById(Account.class, newAccount.getId());
		assertEquals("Check if quanity field value is updated",accountAfterCommit.getQuantity(), quantityNew);
	}
	
	public void testDeleteAccount() throws Exception {
		System.out.println(String.format("%s.testDeleteAccount()", this.getClass().getSimpleName()));
		Logger.getRootLogger().info(String.format("%s.testDeleteAccount()", this.getClass().getSimpleName()));
        final Account newAccount = createNewAccount();
        
        Logger.getRootLogger().info("Delete account wuth account Id: " + newAccount.getId());
        
		//Delete account
		service.beginTransaction();		
		service.deleteObject(Account.class, newAccount);
		service.commitTransaction();
		
		/* check the record fields if deleted or not */
		final Account accountAfterCommit = service.getObjectById(Account.class, newAccount.getId());
		assertNull("Check if account is deleted",accountAfterCommit);
	}	
	
	public void testCreatePlugin() throws Exception {
		System.out.println(String.format("%s.testCreatePlugin()", this.getClass().getSimpleName()));
		Logger.getRootLogger().info(String.format("%s.testCreatePlugin()", this.getClass().getSimpleName()));
		
		List<Plugin> plugins = service.getAllObjects(Plugin.class);
		
		int pluginRecords=plugins.size();
		plugins=null;
		
		final Plugin plugin = createNewPlugin();		
		plugins = service.getAllObjects(Plugin.class);
		assertEquals("Check if new plugin record is created", pluginRecords+1,plugins.size());
		Plugin pluginRecord = service.getObjectById(Plugin.class, plugin.getId());
		
		assertNotNull("Check if newly created Plugin record is retrived using persistence method",pluginRecord);
		assertEquals("Check if Id is correct", plugin.getId(),pluginRecord.getId());
		assertEquals("Check if Class Name is correct", plugin.getClassName(),pluginRecord.getClassName());
		assertEquals("Check if Source is correct", plugin.getSource(),pluginRecord.getSource());
		
		/* check the record fields, use direct sql */
		Logger.getRootLogger().debug("Get Plugin by ID using direct SQL: " + plugin.getId());
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;		
		try {
			connection = getConnection();
			statement = connection.prepareStatement ("SELECT id, className, source  FROM PLUGINS WHERE id=?");
			statement.setString(1, plugin.getId());
			
			resultSet = statement.executeQuery();
			if (resultSet != null && resultSet.next()) {
				String pluginId = resultSet.getString(1);
				String className = resultSet.getString(2);
				String source = resultSet.getString(3);
				
				assertEquals("Check if Id is correct", plugin.getId(),pluginId);
				assertEquals("Check if Class Name is correct", plugin.getClassName(),className);
				assertEquals("Check if Source is correct", plugin.getSource(),source);
				
			} else {
				Logger.getRootLogger().error("SQL query failed.");
			}

		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
			if (connection != null) {
				connection.close();
			}
		}		
	}
	
	
	public void testUpdatePlugin() throws Exception {
		System.out.println(String.format("%s.testUpdatePlugin()", this.getClass().getSimpleName()));
		Logger.getRootLogger().info(String.format("%s.testUpdatePlugin()", this.getClass().getSimpleName()));
		
        final Plugin newPlugin = createNewPlugin();
		
        //create new Plugin for update
		service.beginTransaction();
		service.saveObject(Plugin.class, newPlugin);
		service.commitTransaction();
		
		//update Plugin
		service.beginTransaction();
		
		// get the first Plugin by Id
		final Plugin plugin = service.getObjectById(Plugin.class, newPlugin.getId());
		String classNameBeforeUpdate = plugin.getClassName();
		Logger.getRootLogger().debug("Read Plugin Class name before Update : " + classNameBeforeUpdate);
		
		//update class name
		String classNameForUpdate = "New Class Name";
		plugin.setClassName(classNameForUpdate);
		
		service.saveObject(Plugin.class, plugin);
		
		service.commitTransaction();
		
		Logger.getRootLogger().info("After commit transaction, Plugin: " + ReflectionToStringBuilder.toString(plugin));

		/* check the record fields if updated */
		final Plugin pluginAfterCommit = service.getObjectById(Plugin.class, newPlugin.getId());
		Logger.getRootLogger().debug("Read Plugin Class name after Update : " + pluginAfterCommit.getClassName());
		assertTrue(pluginAfterCommit.getClassName() == classNameForUpdate);
	}
	
	public void testDeletePlugin() throws Exception {
		System.out.println(String.format("%s.testDeletePlugin()", this.getClass().getSimpleName()));
		Logger.getRootLogger().info(String.format("%s.testDeletePlugin()", this.getClass().getSimpleName()));
		
		final Plugin newPlugin = createNewPlugin();
		
        //create new Plugin for Delete
		service.beginTransaction();
		service.saveObject(Plugin.class, newPlugin);
		service.commitTransaction();
		
		//Delete Plugin
		service.beginTransaction();
		
		// get the first Plugin by Id
		final Plugin plugin = service.getObjectById(Plugin.class, newPlugin.getId());
		
		service.deleteObject(Plugin.class, plugin);
		
		service.commitTransaction();
		
		Logger.getRootLogger().info("After commit transaction, Plugin : " + ReflectionToStringBuilder.toString(plugin));

		/* check the record fields if deleted or not */
		final Plugin pluginAfterCommit = service.getObjectById(Plugin.class, newPlugin.getId());
		assertNull(pluginAfterCommit);
	}
	
	public void testCreateOffering() throws Exception {
		System.out.println(String.format("%s.testCreateOffering()", this.getClass().getSimpleName()));
		Logger.getRootLogger().info(String.format("%s.testCreateOffering()", this.getClass().getSimpleName()));
		
		List <Offering> offerings=service.getAllObjects(Offering.class);
		int preCount=offerings.size();
		offerings=null;
		
        final Offering offering = createNewOffering();

        offerings=service.getAllObjects(Offering.class);
        
		assertEquals("Check if new Offering record is created", preCount+1,offerings.size());
		Offering offeringRecord = service.getObjectById(Offering.class, offering.getId());
		
		assertNotNull("Check if newly created Plugin record is retrived",offeringRecord);
		assertEquals("Check if Id is correct", offering.getId(),offeringRecord.getId());
		assertEquals("Check if Name is correct", offering.getName(),offeringRecord.getName());
		assertEquals("Check if Oauth Key is correct", offering.getOauthKey(),offeringRecord.getOauthKey());
		assertEquals("Check if Oauth Secret is correct", offering.getEncOauthSecret(),offeringRecord.getEncOauthSecret());
	
		/* check the record fields, use direct sql */
		Logger.getRootLogger().debug("Get OFFERINGS by ID using direct SQL: " + offering.getId());
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;		
		try {
			connection = getConnection();
			statement = connection.prepareStatement ("SELECT id, oauthKey, oauthSecret, name, plugin, urlPath  FROM OFFERINGS WHERE id=?");
			statement.setString(1, offering.getId());
			
			resultSet = statement.executeQuery();
			if (resultSet != null && resultSet.next()) {
				String offeringId = resultSet.getString(1);
				String oauthKey = resultSet.getString(2);
				String oauthSecret = resultSet.getString(3);
				String name = resultSet.getString(4);
				String plugin = resultSet.getString(5);
				String urlPath = resultSet.getString(6);
				
				assertEquals("Check if Id is correct", offering.getId(),offeringId);
				assertEquals("Check if Oauth Key is correct", offering.getOauthKey(),oauthKey);
				assertEquals("Check if Oauth Secret is correct", offering.getEncOauthSecret(),oauthSecret);
				assertEquals("Check if Name is correct", offering.getName(),name);
				assertEquals("Check if Plugin Id is correct", offering.getPlugin().getId(),plugin);
				assertEquals("Check if URL Path id is correct", offering.getUrlPath().getId(),urlPath);
				
			} else {
				Logger.getRootLogger().error("SQL query failed.");
			}

		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
			if (connection != null) {
				connection.close();
			}
		}	
	}
	
	public void testUpdateOffering() throws Exception {
		System.out.println(String.format("%s.testUpdateOffering()", this.getClass().getSimpleName()));
		Logger.getRootLogger().info(String.format("%s.testUpdateOffering()", this.getClass().getSimpleName()));
		
        final Offering newOffering = createNewOffering();
        
		String nameBeforeUpdate = newOffering.getName();
		Logger.getRootLogger().debug("Read Offering name before Update : " + nameBeforeUpdate);
		
		//update name
		String nameForUpdate = "New Name";
		newOffering.setName(nameForUpdate);
		
		service.beginTransaction();
		service.saveObject(Offering.class, newOffering);
		service.commitTransaction();
		
		Logger.getRootLogger().info("After commit transaction, Offering: " + ReflectionToStringBuilder.toString(newOffering));

		/* check the record fields if updated */
		final Offering offeringAfterCommit = service.getObjectById(Offering.class, newOffering.getId());
		Logger.getRootLogger().debug("Read Offering name after Update : " + offeringAfterCommit.getName());
		assertEquals("Check if Name field value is set correctly after update operation",nameForUpdate,offeringAfterCommit.getName());
	}
	
	public void testDeleteOffering() throws Exception {
		System.out.println(String.format("%s.testDeleteOffering()", this.getClass().getSimpleName()));
		Logger.getRootLogger().info(String.format("%s.testDeleteOffering()", this.getClass().getSimpleName()));
		
		final Offering newOffering = createNewOffering();		
		
		//Delete Offering
		service.beginTransaction();
		service.deleteObject(Offering.class, newOffering);
		service.commitTransaction();
		
		/* check the record fields if deleted or not */
		final Offering offeringAfterCommit = service.getObjectById(Offering.class, newOffering.getId());
		assertNull("Check if offering is deleted",offeringAfterCommit);
	}
	
	public void testCreateSubscriber() throws Exception {
		System.out.println(String.format("%s.testCreateSubscriber()", this.getClass().getSimpleName()));
		Logger.getRootLogger().info(String.format("%s.testCreateSubscriber()", this.getClass().getSimpleName()));
		
		List <Subscriber> subscribers=service.getAllObjects(Subscriber.class);
		int preCount=subscribers.size();
		subscribers=null;
		
        final Subscriber subscriber = createNewSubscriber();

        subscribers=service.getAllObjects(Subscriber.class);
        
		assertEquals("Check if new Subscriber record is created", preCount+1,subscribers.size());
		Subscriber subscriberRecord = service.getObjectById(Subscriber.class, subscriber.getId());
		
		assertNotNull("Check if newly created Plugin record is retrived",subscriberRecord);
		assertEquals("Check if Id is correct", subscriber.getId(),subscriberRecord.getId());
		assertEquals("Check if Name is correct", subscriber.getName(),subscriberRecord.getName());
		assertEquals("Check if API Key is correct", subscriber.getApiKey(),subscriberRecord.getApiKey());
		assertEquals("Check if Type is correct", subscriber.getType(),subscriberRecord.getType());
		
		/* check the record fields, use direct sql */
		Logger.getRootLogger().debug("Get Subscriber by ID using direct SQL: " + subscriber.getId());
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;		
		try {
			connection = getConnection();
			statement = connection.prepareStatement ("SELECT id, name, apiKey, type, accountId, externalId  FROM SUBSCRIBERS WHERE id=?");
			statement.setString(1, subscriber.getId());
			
			resultSet = statement.executeQuery();
			if (resultSet != null && resultSet.next()) {
				String subscriberId = resultSet.getString(1);
				String name = resultSet.getString(2);
				String apiKey = resultSet.getString(3);
				int type = resultSet.getInt(4);
				String accountId = resultSet.getString(5);
				String externalId = resultSet.getString(6);
				
				assertEquals("Check if Id is correct", subscriber.getId(),subscriberId);
				assertEquals("Check if Name is correct", subscriber.getName(),name);
				assertEquals("Check if API Key is correct", subscriber.getEncApiKey(),apiKey);
				assertEquals("Check if Type is correct", subscriber.getType(),type);
				assertEquals("Check if Account Id is correct", subscriber.getAccount().getId(),accountId);
				assertEquals("Check if URL Path id is correct", subscriber.getExternalId(),externalId);
				
			} else {
				Logger.getRootLogger().error("SQL query failed.");
			}

		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
			if (connection != null) {
				connection.close();
			}
		}			
	}
	
	public void testUpdateSubscriber() throws Exception {
		System.out.println(String.format("%s.testUpdateSubscriber()", this.getClass().getSimpleName()));
		Logger.getRootLogger().info(String.format("%s.testUpdateSubscriber()", this.getClass().getSimpleName()));
		
        final Subscriber newSubscriber = createNewSubscriber();
		
		String nameBeforeUpdate = newSubscriber.getName();
		Logger.getRootLogger().debug("Read Subscriber name before Update : " + nameBeforeUpdate);
		
		//update name
		String nameForUpdate = "New Subscriber Name";
		newSubscriber.setName(nameForUpdate);

		service.beginTransaction();
		service.saveObject(Subscriber.class, newSubscriber);
		service.commitTransaction();
		
		Logger.getRootLogger().info("After commit transaction, Subscriber: " + ReflectionToStringBuilder.toString(newSubscriber));

		/* check the record fields if updated */
		final Subscriber subscriberAfterCommit = service.getObjectById(Subscriber.class, newSubscriber.getId());
		Logger.getRootLogger().debug("Read Subscriber name after Update : " + subscriberAfterCommit.getName());
		assertEquals("Check if Name field value is as expected after update operation",nameForUpdate,subscriberAfterCommit.getName());
	}
	
	public void testDeleteSubscriber() throws Exception {
		System.out.println(String.format("%s.testDeleteSubscriber()", this.getClass().getSimpleName()));
		Logger.getRootLogger().info(String.format("%s.testDeleteSubscriber()", this.getClass().getSimpleName()));
		
		final Subscriber newSubscriber = createNewSubscriber();
		
		service.beginTransaction();
		service.deleteObject(Subscriber.class, newSubscriber);
		service.commitTransaction();
		
		/* check the record fields if deleted or not */
		final Subscriber subscriberAfterCommit = service.getObjectById(Subscriber.class, newSubscriber.getId());
		assertNull("Check if subscriber is deleted",subscriberAfterCommit);
	}	
	
	public void testNegativeForPlugin() throws Exception {	
		System.out.println(String.format("%s.testNegativeForPlugin()", this.getClass().getSimpleName()));
		Logger.getRootLogger().info(String.format("%s.testNegativeForPlugin()", this.getClass().getSimpleName()));
		
	    final Plugin plugin = new Plugin();
	    plugin.setClassName("com.ibm.icas.biqse.ServiceProviderPlugin");
	    plugin.setSource("file:///D:/cpe_workspace/bi-qse-plugin/target/classes/");
	    plugin.setId("bi-qse");
	    
	    Logger.getRootLogger().info("Test if Plugin table doesn't accept duplicate id ");
	    try{
	           service.beginTransaction();
	           service.saveObject(Plugin.class, plugin);
	           service.commitTransaction();
	
	    }catch (PersistenceException e){
	           System.out.println(e.getMessage());
	    } catch (Exception e){
	           System.out.println(e.getMessage());
	           assertTrue("Check if new duplicate plugin record is not created", e.getMessage().contains("java.sql.SQLIntegrityConstraintViolationException"));
	           
	    }
	    
	    Logger.getRootLogger().info("Test if pcmaeBackendId field value doesn't get set with non existing id");
	    
	    final Plugin newPlugin = createNewPlugin();
	    
	    try{
		    service.beginTransaction();
		    service.saveObject(Plugin.class, newPlugin);
		    service.commitTransaction();
	    }catch (PersistenceException e){
	           System.out.println(e.getMessage());
	    } catch (Exception e){
	    	System.out.println(e.getMessage());
	           assertTrue("Check if PcmaeBackend value doesn't get set with non existing id in the Plugin table", e.getMessage().
	        		   contains("a relationship that was not marked cascade PERSIST"));
	           
	    }
	    
	}
	
	public void testNegativeForOfferings() throws Exception {	
		System.out.println(String.format("%s.testNegativeForOfferings()", this.getClass().getSimpleName()));
		Logger.getRootLogger().info(String.format("%s.testNegativeForOfferings()", this.getClass().getSimpleName()));
		
		Logger.getRootLogger().info("Delete record from Plugin table which is associated with Offerings");
		Offering offering = createNewOffering();
	    
	    try{
		    service.beginTransaction();
		    service.deleteObjectById(Plugin.class, offering.getPlugin().getId());
		    service.commitTransaction();
	    }catch (PersistenceException e){
	           System.out.println(e.getMessage());
	    } catch (Exception e){
	    	System.out.println("Error Message is:"+e.getMessage());
	    	assertTrue("Check if Plugin record doesn't get deleted if it is associated with Offering table", e.getMessage().
	    			contains("DELETE on table 'PLUGINS' caused a violation of foreign key constraint"));
	    }		
    
	    Logger.getRootLogger().info("Test if Plugin field value doesn't get set with non existing id in Offerings table");
		    
	    Plugin plugin = new Plugin();
	    plugin.setId("NON_EXISTING_324334#@!$Id");
	    offering.setPlugin(plugin);
	    
	    try{
		    service.beginTransaction();
		    service.saveObject(Offering.class, offering);
		    service.commitTransaction();
	    }catch (PersistenceException e){
	           System.out.println(e.getMessage());
	    } catch (Exception e){
	    	System.out.println(e.getMessage());
	    	assertTrue("Check if Plugin field value doesn't get set with non existing id in the Offering table", e.getMessage().
	    			contains("a relationship that was not marked cascade PERSIST"));
	           
	    }
	    
	}
	
	
	public void testNegativeForSubscribers() throws Exception {	
		System.out.println(String.format("%s.testNegativeForSubscribers()", this.getClass().getSimpleName()));
		Logger.getRootLogger().info(String.format("%s.testNegativeForSubscribers()", this.getClass().getSimpleName()));
		
		Subscriber subscriber = createNewSubscriber();
    
	    Logger.getRootLogger().info("Test if AccountId field value doesn't get set with non existing id in Subscriber table");
		    
	    Account account = new Account();
	    account.setId("NON_EXISTING_324334#@!$Id");
	    subscriber.setAccount(account);
	    
	    try{
		    service.beginTransaction();
		    service.saveObject(Subscriber.class, subscriber);
		    service.commitTransaction();
	    }catch (PersistenceException e){
	           System.out.println(e.getMessage());
	    } catch (Exception e){
	    	System.out.println(e.getMessage());
	    	assertTrue("Check if AccountId field value doesn't get set with non existing id in the Subscriber table", e.getMessage().
	    			contains("a relationship that was not marked cascade PERSIST"));
	           
	    }
	    
	    
	    Logger.getRootLogger().info("Test if Subscriber record is getting created without setting any value in AccountId field");
	    subscriber=null;
	    subscriber = new Subscriber();
		subscriber.setApiKey("API123");
		subscriber.setType(1);
		subscriber.setName("Garbage Collector");
		
		service.beginTransaction();
		service.saveObject(Subscriber.class, subscriber);
		service.commitTransaction();
		
		assertTrue("Check if Subscriber record is created without setting any value in AccountId field",subscriber.getId()!=null);	    
	    
	}
	
	public void testNegativeForAccount() throws Exception {	
		System.out.println(String.format("%s.testNegativeForAccount()", this.getClass().getSimpleName()));
		Logger.getRootLogger().info(String.format("%s.testNegativeForAccount()", this.getClass().getSimpleName()));
		
		Account account = createNewAccount();
    
	    Logger.getRootLogger().info("Test if OfferingId field value doesn't get set with non existing id in Account table");
		    
	    Offering offering = new Offering();
	    offering.setId("NON_EXISTING_324334#@!$Id");
	    account.setOffering(offering);
	    
	    try{
		    service.beginTransaction();
		    service.saveObject(Account.class, account);
		    service.commitTransaction();
	    }catch (PersistenceException e){
	           System.out.println(e.getMessage());
	    } catch (Exception e){
	    	System.out.println(e.getMessage());
	    	assertTrue("Check if OfferingId field value doesn't get set with non existing id in the Account table", e.getMessage().
	    			contains("a relationship that was not marked cascade PERSIST"));
	           
	    }
	    
	    Logger.getRootLogger().info("Test if Account record is getting created without setting any value in OfferingId field");
	    account=null;
	    account = new Account();
		account.setMarketUrl("https://my-url.com");
		account.setExpiration(12345);
		account.setPartner("blahblah partner");
		account.setQuantity(1);
		account.setState(Account.ACTIVE);
		
        //create new account for update
		service.beginTransaction();
		service.saveObject(Account.class, account);
		service.commitTransaction();
		
		assertTrue("Check if Account record is created without setting any value in OfferingId field",account.getId()!=null);
	    
	}	
	
}
