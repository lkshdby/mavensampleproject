package com.ibm.scas.analytics.backend.impl;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.ibm.scas.analytics.EngineProperties;
import com.ibm.scas.analytics.utils.AES;

public class PcmaeDatabaseConnector {
	private static final Logger logger = Logger.getLogger(PcmaeDatabaseConnector.class);
	private static final String mySqlUri;
	private static int nodesPerHypervisor;
	
	static {
		String uri = null;
		
		String dbName = EngineProperties.getInstance().getProperty(EngineProperties.PCMAE_DB, EngineProperties.DEFAULT_PCMAE_DB);
		String hostname = EngineProperties.getInstance().getProperty(EngineProperties.MYSQL_HOST, EngineProperties.DEFAULT_MYSQL_HOST);
		int port;
		try {
			port = Integer.parseInt(EngineProperties.getInstance().getProperty(EngineProperties.MYSQL_PORT, String.valueOf(EngineProperties.DEFAULT_MYSQL_PORT)));
		} catch (NumberFormatException e) {
			port = EngineProperties.DEFAULT_MYSQL_PORT;
		}
		String username = EngineProperties.getInstance().getProperty(EngineProperties.PCMAE_USER, EngineProperties.DEFAULT_PCMAE_USER);
		String password = EngineProperties.getInstance().getProperty(EngineProperties.PCMAE_PASSWORD);
		if (password != null) {
			try {
				password = new AES().decrypt(password);
			} catch (GeneralSecurityException e) {
				logger.error("Failed to decrypte MySQL password: " + password, e);
				password = null;
			}
		}
		if (password == null) {
			password = EngineProperties.DEFAULT_PCMAE_PASSWORD;
		}
		
		if (dbName != null && hostname != null && username != null && password != null) {
			logger.info("Database name: " + dbName);
			logger.info("Host name: " + hostname);
			logger.info("Port: " + port);
			logger.info("Username: " + username);
			logger.debug("Password: " + password);
			
			uri = "mysql://" + hostname + ":" + port + "/" + dbName + "?user=" + username + "&password=" + password;
		} else {
			logger.error("Failed to retrieve required MySQL credentials.");
		}
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
		
		try {
			nodesPerHypervisor = Integer.parseInt(EngineProperties.getInstance().getProperty(EngineProperties.NODES_PER_HYPERVISOR, String.valueOf(EngineProperties.DEFAULT_NODES_PER_HYPERVISOR)));
		} catch (NumberFormatException e) {
			port = EngineProperties.DEFAULT_NODES_PER_HYPERVISOR;
		}
	}

	protected Connection getConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:" + mySqlUri);
	}

	public int getPhysicalHostCount() {
		logger.debug("getPhysicalHostCount called");
		
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		
		try {
			connection = getConnection();
			statement = connection.prepareStatement("SELECT COUNT(*) from PHYSICALHOST");
			
			int count = 0;
			
			resultSet = statement.executeQuery();
			if (resultSet != null && resultSet.next()) {
				count = resultSet.getInt(1);
			} 
			
			return count;
		} catch (Exception e) {
			logger.error("Exception in getPhysicalHostCount", e);
			return 0;
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch(SQLException e) {
				logger.error("Exception in getPhysicalHostCount", e);
			}
		}
	}
	
	public int getTotalCapacity() {
		return getPhysicalHostCount() * nodesPerHypervisor;
	}
}
