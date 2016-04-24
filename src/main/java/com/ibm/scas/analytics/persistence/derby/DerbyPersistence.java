package com.ibm.scas.analytics.persistence.derby;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.persistence.config.PersistenceUnitProperties;

import com.google.inject.Singleton;

import com.ibm.scas.analytics.EngineProperties;
import com.ibm.scas.analytics.persistence.sql.SqlPersistence;

/**
 * A derby implementation of the PersistenceService
 * 
 * @author Han Chen
 *
 */
@Singleton
public class DerbyPersistence extends SqlPersistence {
	private static final Logger logger = Logger.getLogger(DerbyPersistence.class);
	private final String derbyDBPath;
	
	public DerbyPersistence() {
		derbyDBPath = EngineProperties.getInstance().getProperty(EngineProperties.DERBY_DB_PATH);
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
	public Map<String, String> getConnectionProperties() {
		final String uri = String.format("jdbc:derby:%s;create=true", derbyDBPath);
	
		final Map<String, String> connectionProps = new HashMap<String, String>();
	    connectionProps.put(PersistenceUnitProperties.JDBC_DRIVER, "org.apache.derby.jdbc.EmbeddedDriver");
	    connectionProps.put(PersistenceUnitProperties.JDBC_URL, uri);

        connectionProps.put("eclipselink.target-database", "DERBY");
        
        return connectionProps;

	}
	
	protected Connection getConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:derby:" + derbyDBPath);
	}
}
