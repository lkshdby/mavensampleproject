package com.ibm.scas.analytics.test.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.google.inject.Singleton;
import com.ibm.scas.analytics.persistence.derby.DerbyPersistence;

/**
 * A derby implementation of the PersistenceService
 * 
 * @author Han Chen
 *
 */
@Singleton
public class InMemoryDerbyPersistence extends DerbyPersistence {
	private static final Logger logger = Logger.getLogger(InMemoryDerbyPersistence.class);
	private final String derbyDBPath = "memory:testdb";
	
	public InMemoryDerbyPersistence() {
		if (derbyDBPath == null) {
			logger.error("Derby DB path not configured!");
			throw new RuntimeException("Derby DB path not configured!");
		} else {
			logger.info("Derby DB path: " + derbyDBPath);
			try {
				Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
			} catch (Exception e) {
				logger.error("Exception loading derby jdbc driver", e);
				throw new RuntimeException(e);
			}
		}
	}
	
	@Override
	protected Connection getConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:derby:" + derbyDBPath + ";create=true");
	}

}
