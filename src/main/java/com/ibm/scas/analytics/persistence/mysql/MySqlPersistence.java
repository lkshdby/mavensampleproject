package com.ibm.scas.analytics.persistence.mysql;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Singleton;

import com.ibm.scas.analytics.EngineProperties;
import com.ibm.scas.analytics.persistence.sql.SqlPersistence;
import com.ibm.scas.analytics.utils.AES;

/**
 * A MySQL implementation of the PersistenceService
 * 
 * @author Han Chen
 *
 */
@Singleton
public class MySqlPersistence extends SqlPersistence {
	private static final Logger logger = Logger.getLogger(MySqlPersistence.class);
	private String mySqlUri;
	
	private final String dbName;
	private final String hostname;
	private final String username; 
	private final String password;
	private final String uri;
	private final int port;
	
	public MySqlPersistence() {
		String vcapServices = System.getenv("VCAP_SERVICES");
		String theDbName = null;
		String theHostname = null;
		String theUsername = null;
		String thePassword = null;
		String theUri = null;
		int thePort = EngineProperties.DEFAULT_MYSQL_PORT;

		if (vcapServices != null) {
			// we are running inside Bluemix
			logger.info("VCAP_SERVICES: " + vcapServices);
			try {
				Gson gson = new Gson();
				Map<String, MySqlServiceConfig[]> map = gson.fromJson(vcapServices, new TypeToken<Map<String, MySqlServiceConfig[]>>() {}.getType());

				MySqlCredentials creds = map.get("mysql-5.5")[0].getCredentials();
				theDbName = creds.getName();
				theHostname = creds.getHostname();
				thePort = creds.getPort();
				theUsername = creds.getUsername();
				thePassword = creds.getPassword();
				
				if (theDbName != null && theHostname != null && theUsername != null && thePassword != null) {
					theUri = "mysql://" + theHostname + ":" + thePort + "/" + theDbName + "?user=" + theUsername + "&password=" + thePassword;
				} else {
					logger.error("Failed to retrieve required MySQL credentials.");
				}
			} catch(Throwable e) {
				logger.error("Failed to parse VCAP_SERVICES");
			}
			
			if (theUri == null) {
				logger.warn("Falling back on engine.properties ...");
			}
		}
		
		if (theUri == null) {
			theDbName = EngineProperties.getInstance().getProperty(EngineProperties.MYSQL_DB, EngineProperties.DEFAULT_MYSQL_DB);
			theHostname = EngineProperties.getInstance().getProperty(EngineProperties.MYSQL_HOST, EngineProperties.DEFAULT_MYSQL_HOST);
			try {
				thePort = Integer.parseInt(EngineProperties.getInstance().getProperty(EngineProperties.MYSQL_PORT, String.valueOf(EngineProperties.DEFAULT_MYSQL_PORT)));
			} catch (NumberFormatException e) {
				thePort = EngineProperties.DEFAULT_MYSQL_PORT;
			}
			theUsername = EngineProperties.getInstance().getProperty(EngineProperties.MYSQL_USER, EngineProperties.DEFAULT_MYSQL_USER);
			thePassword = EngineProperties.getInstance().getProperty(EngineProperties.MYSQL_PASSWORD);
			if (thePassword != null) {
				try {
					thePassword = new AES().decrypt(thePassword);
				} catch (GeneralSecurityException e) {
					logger.error("Failed to decrypte MySQL password: " + thePassword);
					thePassword = null;
				}
			}
			if (thePassword == null) {
				thePassword = EngineProperties.DEFAULT_MYSQL_PASSWORD;
			}
		}
		
		dbName = theDbName;
		hostname = theHostname;
		username = theUsername;
		password = thePassword;
		port = thePort;
		
		if (dbName != null && hostname != null && username != null && password != null) {
			logger.info("Database name: " + dbName);
			logger.info("Host name: " + hostname);
			logger.info("Port: " + port);
			logger.info("Username: " + username);
			logger.debug("Password: " + password);

			theUri = "mysql://" + hostname + ":" + port + "/" + dbName + "?user=" + username + "&password=" + password;
		} else {
			logger.error("Failed to retrieve required MySQL credentials.");
		}
		uri = theUri;
		mySqlUri = uri;		

		if (mySqlUri == null) {
			logger.error("MySQL URI not configured!");
		} else {
			logger.info("MySQL URI: " + mySqlUri);
			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
			} catch (Exception e) {
				logger.error("Exception loading mysql jdbc driver", e);
			}
		}
	}

	@Override
	public Map<String,String> getConnectionProperties() {
	
		final Map<String, String> connectionProps = new HashMap<String, String>();
		connectionProps.put("javax.persistence.jdbc.driver", "com.mysql.jdbc.Driver");
	    connectionProps.put("javax.persistence.jdbc.url", String.format("jdbc:%s", uri));
	    connectionProps.put("javax.persistence.jdbc.user", username);
	    connectionProps.put("javax.persistence.jdbc.password", password);
	    
	    return connectionProps;
	}
	
	@Override
	protected Connection getConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:" + mySqlUri);
	}
	
}
