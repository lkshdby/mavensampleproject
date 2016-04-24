package com.ibm.scas.analytics.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.Statement;
import java.util.UUID;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.ibm.scas.analytics.EngineProperties;
import com.ibm.scas.analytics.backend.TenantService;
import com.ibm.scas.analytics.beans.Account;
import com.ibm.scas.analytics.beans.Subscriber;
import com.ibm.scas.analytics.guice.CPEAppModule;
import com.ibm.scas.analytics.persistence.PersistenceService;
import com.ibm.scas.analytics.persistence.beans.CPELocation;
import com.ibm.scas.analytics.persistence.beans.PcmaeBackend;
import com.ibm.scas.analytics.utils.CPEException;
import com.ibm.scas.analytics.persistence.beans.Offering;

import junit.framework.TestCase;

public abstract class BaseTestCase extends TestCase {
	protected PersistenceService service;
	protected TenantService tenantService;
	protected Injector injector;
	
	protected String dummyAccountId;
	protected String dummySubscriberId;
	protected String dummyOfferingId;
	
	protected void addDummyAccount() throws CPEException {
		// create an offering
		final Offering offeringRec = new Offering();
		offeringRec.setId("biginsights");
		offeringRec.setName("biginsights");
		offeringRec.setOauthKey("asdf");
		offeringRec.setOauthSecret("abcd");
		offeringRec.setPlugin(null);
		offeringRec.setUrlPath(null);
		service.saveObject(Offering.class, offeringRec);
		dummyOfferingId = offeringRec.getId();

		// create an account
		final Account account = new Account();
		account.setOffering(new com.ibm.scas.analytics.beans.Offering());
		account.getOffering().setId(dummyOfferingId);
		account.setStatus(Account.ACTIVE);
		account.setQuantity(-1);
		account.setEdition("bi30_small");
		account.setMarketUrl(null);
		
		dummyAccountId = tenantService.createAccount(account);

		// create a subscriber
		final Subscriber subscriber = new Subscriber();
		subscriber.setName("testSubscriber");
		subscriber.setApiKey(UUID.randomUUID().toString().replace("-", ""));
		subscriber.setExternalId("externalId");
		subscriber.setAccount(new Account());
		subscriber.getAccount().setAccountIdentifier(dummyAccountId);

		dummySubscriberId = tenantService.createSubscriber(subscriber);
	}
	
	@Override
	public void setUp() throws Exception {
		Logger.getRootLogger().addAppender(new ConsoleAppender());
		Logger.getLogger("com.ibm").setLevel(Level.DEBUG);
		
		Logger.getRootLogger().info("Running test setup ...");
		
		createDerbyDB();
		
		try {
			executeSQLScript("create_db.sql");
			
			injector = Guice.createInjector(new CPEAppModule());
			injector.getInstance(PersistService.class).start();
			
			service = injector.getInstance(PersistenceService.class);
			tenantService = injector.getInstance(TenantService.class);
			
			// add my location record
			service.beginTransaction();
			final PcmaeBackend pcmae = new PcmaeBackend();
			pcmae.setId(UUID.randomUUID().toString());
			pcmae.setUrl("https://mapreduce.beta.manage.softlayer.com:8443/pcmwebapi-1.0/rest");
			pcmae.setAccount(UUID.randomUUID().toString());
			pcmae.setUsername("cluster-provisioning-engine");
			pcmae.setPassword(UUID.randomUUID().toString());
			service.saveObject(PcmaeBackend.class, pcmae);

			final CPELocation location = new CPELocation();
			location.setName(EngineProperties.getInstance().getProperty(EngineProperties.CPE_LOCATION_NAME, EngineProperties.DEFAULT_CPE_LOCATION_NAME));
			location.setPcmaeBackend(pcmae);
			service.saveObject(CPELocation.class, location);
			service.commitTransaction();



		} catch (Exception e) {
			dropDerbyDB();
			throw e;
		}
	}
	
	@Override
	public void tearDown() throws Exception {
		Logger.getRootLogger().info("Running test shutdown ...");
		injector.getInstance(PersistService.class).stop();
		
		dropDerbyDB();
	}
	
	protected Connection getConnection() throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
		return DriverManager.getConnection("jdbc:derby:" + EngineProperties.getInstance().getProperty(EngineProperties.DERBY_DB_PATH) + ";create=true");
	}

	protected void createDerbyDB() throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
		Connection connection = null;
		try {
			// connect using drop=true to get rid of the db
			connection = DriverManager.getConnection("jdbc:derby:" + EngineProperties.getInstance().getProperty(EngineProperties.DERBY_DB_PATH) + ";create=true");
		} catch (SQLNonTransientConnectionException e) {
			// drop derby db throws this exception for some reason, but it's ok if the SQLSTATE matches
			if (!e.getSQLState().equals("08006")) {
				throw e;
			}
			
			Logger.getRootLogger().info(e.getLocalizedMessage());
		} finally {
			if (connection != null) {
				connection.close();
			}		
		}
	}
	
	
	protected void dropDerbyDB() throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
		Connection connection = null;
		try {
			// connect using drop=true to get rid of the db
			connection = DriverManager.getConnection("jdbc:derby:" + EngineProperties.getInstance().getProperty(EngineProperties.DERBY_DB_PATH) + ";drop=true");
		} catch (SQLNonTransientConnectionException e) {
			// drop derby db throws this exception for some reason, but it's ok if the SQLSTATE matches
			if (!e.getSQLState().equals("08006")) {
				throw e;
			}
			
			Logger.getRootLogger().info(e.getLocalizedMessage());
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}
	
	protected void executeSQLScript(String fileName) throws Exception {
		/* load the DDL */
		BufferedReader reader = null;
		try {
			final InputStream inStream = this.getClass().getClassLoader().getResourceAsStream(fileName);
			reader = new BufferedReader(new InputStreamReader(inStream));

			int lineNum = 0;
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				lineNum++;
				if (line.trim().length() == 0) {
					continue;
				}
				
				sb.append(line.trim());
				if (line.trim().charAt(line.trim().length() - 1) != ';') {
					continue;
				}
				
				Connection connection = null;
				Statement statement = null;
				try {
					connection = this.getConnection();
					statement = connection.createStatement();
					Logger.getRootLogger().debug("Running SQL: " + sb.toString());
					statement.executeUpdate(sb.toString().replaceAll(";", ""));

					connection.commit();
				} catch (SQLException e) {
					if (!e.getSQLState().equals("X0Y32")) {
						throw e;
					}
					
					// ignore this as table already exists
					Logger.getRootLogger().warn(e.getLocalizedMessage());
				} catch (Exception e) {
					Logger.getRootLogger().error("Error in " + fileName + " line: " + lineNum);
					Logger.getRootLogger().error(e);
					throw e;
				} finally {
					if (statement != null) {
						statement.close();
					}

					if (connection != null) {
						connection.close();
					}
				}
				
				sb = new StringBuilder();
			}
		} finally {
			if (reader != null) {
				reader.close();
			}

		}	
	}
}
