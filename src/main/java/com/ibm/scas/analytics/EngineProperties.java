package com.ibm.scas.analytics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.log4j.Logger;

import com.ibm.scas.analytics.utils.AES;

/**
 * Properties loader for the cluster provisioning engine
 * 
 * @author Han Chen
 * 
 */
public class EngineProperties extends Properties implements FileAlterationListener {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(EngineProperties.class);

	public static final String TOMCAT_PROPERTIES_FILENAME = "engine.properties";
	public static final String OE_PROPERTIES_FILENAME = "engine-oe.properties";
	public static final String PROPERTIES_FILENAME;

	public static final String EXTERNAL_PROPERTIES = "external.properties";
	public static final String DEFAULT_EXTERNAL_PROPERTIES = "/opt/ibm/cpe/cpe.properties";

	public static final String CPE_VERSION = "cpe.version";
	public static final String CPE_BUILD = "cpe.build";
	
	public static final String CPE_LOCATION_IS_HUB = "cpe.location.isHub";
	public static final String DEFAULT_CPE_LOCATION_IS_HUB = "true";
	public static final String CPE_LOCATION_NAME = "cpe.location.name";
	public static final String DEFAULT_CPE_LOCATION_NAME = "wdc01";
	
	public static final String DERBY_DB_PATH = "derby.db.path";
//	public static final String MYSQL_URI = "mysql.uri";
	public static final String MYSQL_HOST = "mysql.host";
	public static final String DEFAULT_MYSQL_HOST = "localhost";
	public static final String MYSQL_PORT = "mysql.port";
	public static final int DEFAULT_MYSQL_PORT = 3306;
	public static final String MYSQL_DB = "mysql.db";
	public static final String DEFAULT_MYSQL_DB = "cpedb";
	public static final String MYSQL_USER = "mysql.user";
	public static final String DEFAULT_MYSQL_USER = "cpeadmin";
	public static final String MYSQL_PASSWORD = "mysql.password";
	public static final String DEFAULT_MYSQL_PASSWORD = "cpeadmin";
	public static final String PERSISTENCE_PROVIDER = "persistence.provider";
	public static final String PERSISTENCE_DDL_GENERATION = "persistence.ddl-generation";
	public static final String PERSISTENCE_LOGGING_LEVEL = "persistence.logging.level";
	public static final String PERSISTENCE_ENABLE_DB_CACHE = "persistence.enableCache";
	public static final String DEFAULT_PERSISTENCE_PROVIDER = "com.ibm.scas.analytics.persistence.derby.DerbyPersistence";
	public static final String PROVISIONING_PROVIDER = "provisioning.provider";
	public static final String DEFAULT_PROVISIONING_PROVIDER = "com.ibm.scas.analytics.backend.impl.mock.MockProvisioningService";
	public static final String NETWORK_PROVIDER = "network.provider";
	public static final String DEFAULT_NETWORK_PROVIDER = "com.ibm.scas.analytics.backend.impl.mock.MockNetworkService";
	public static final String FIREWALL_PROVIDER = "firewall.provider";
	public static final String DEFAULT_FIREWALL_PROVIDER = "com.ibm.scas.analytics.backend.impl.mock.MockFirewallService";
	public static final String FIREWALL_VYATTA_CONFIG_SYNC = "firewall.provider.vyatta.config-sync";
	public static final String FIREWALL_VYATTA_CONFIG_SYNC_USERS_PREFIX = "firewall.provider.vyatta.config-sync.users";
	public static final String FIREWALL_VYATTA_CONFIG_SYNC_USER_PREFIX = "firewall.provider.vyatta.config-sync.username";
	public static final String FIREWALL_VYATTA_CONFIG_SYNC_PASSWD_PREFIX = "firewall.provider.vyatta.config-sync.password";
	public static final String FIREWALL_VYATTA_VRRP_AH_PASSWD_PREFIX = "firewall.provider.vyatta.vrrp.ah.password";
	public static final String FIREWALL_VYATTA_VRRP_AH_PASSWD_RANDOM_PREFIX = "firewall.provider.vyatta.vrrp.ah.password.randomize";
	
	public static final String FIREWALL_VYATTA_HTTPCLIENT_SOCKET_TIMEOUT = "firewall.provider.vyatta.httpClient.socketTimeout";
	public static final String DEFAULT_FIREWALL_VYATTA_HTTPCLIENT_SOCKET_TIMEOUT_SECS = "10";
	public static final String FIREWALL_VYATTA_HTTPCLIENT_CONNECTION_TIMEOUT = "firewall.provider.vyatta.httpClient.connectimeout";
	public static final String DEFAULT_FIREWALL_VYATTA_HTTPCLIENT_CONNECTION_TIMEOUT_SECS = "10";

	public static final String SOFTLAYER_HTTPCLIENT_SOCKET_TIMEOUT = "softlayer.httpClient.socketTimeout";
	public static final String DEFAULT_SOFTLAYER_HTTPCLIENT_SOCKET_TIMEOUT_SECS = "10";
	public static final String SOFTLAYER_HTTPCLIENT_CONNECTION_TIMEOUT = "softlayer.httpClient.connectimeout";
	public static final String DEFAULT_SOFTLAYER_HTTPCLIENT_CONNECTION_TIMEOUT_SECS = "10";
	
	public static final String DGW_ADDON_CODE = "dedicated.gateway.addon.code";
	public static final String DEFAULT_DGW_ADDON_CODE = "dedicated_gateway";
	public static final String DGW_ACTIONS_ALLOWED = "dedicated.gateway.actions.allowed";
	public static final String DEFAULT_DGW_ACTIONS_ALLOWED = "true";

	public static final String PCMAE_DB = "pcmae.db";
	public static final String PCMAE_USER = "pcmae.user";
	public static final String PCMAE_PASSWORD = "pcmae.password";
	public static final String DEFAULT_PCMAE_DB = "pcmae";
	public static final String DEFAULT_PCMAE_USER = "pcmae";
	public static final String DEFAULT_PCMAE_PASSWORD = "pcmae";
	
	public static final String NODES_PER_HYPERVISOR = "nodes.per.hypervisor";
	public static final int DEFAULT_NODES_PER_HYPERVISOR = 5;
	
	public static final String IGNORE_SSL_ERROR = "ignore.ssl.error";
	
	public static final String TEST_MODE = "test.mode";
	public static final String PROVISION_MAGIC_PASSWORD = "provision.magic.password";

	public static final String ADMIN_API_KEY = "admin.api.key";
	public static final String PROMO_CODE_KEY = "promo.code.key";
	public static final String MARKETPLACE_URL = "marketplace.url";

	public static final String TRANSFER_SERVICE_PORT = "transfer.service.port";
	public static final int DEFAULT_TRANSFER_SERVICE_PORT = 8444;

	public static final String NAGIOS_LOG_FILE = "nagios.log.file";
	public static final String DEFAULT_NAGIOS_LOG_FILE = "/opt/platform/icm/log/cpe_passive_check.log";
	
	public static final String AUDIT_LOG_FILE = "audit.log.file";
	public static final String DEFAULT_AUDIT_LOG_FILE = "/opt/platform/gui/logs/audit.log";
	
	public static final String MARKETPLACE_ORDER_UNIT = "marketplace.order.unit";
	public static final String DEFAULT_MARKETPLACE_ORDER_UNIT = "SERVER";

	public static final String GC_INTERVAL = "gc.interval";
	public static final String DEFAULT_GC_INTERVAL_SECS = "15";
	
	public static final String SOFTLAYER_POLL_INTERVAL = "sl.poll.interval";
	public static final String DEFAULT_SOFTLAYER_POLL_INTERVAL_SECS = "30";
	
	public static final String CLUSTER_BUILDER_INTERVAL = "cluster.builder.interval";
	public static final String DEFAULT_CLUSTER_BUILDER_INTERVAL_SECS = "15";
	/*
	 * PULSE demo
	 */
	public static final String CLUSTER_POOL = "cluster.pool";

	private static final EngineProperties props;

	private final FileAlterationMonitor fileMonitor;

	static {
		if (System.getenv("VCAP_APPLICATION") != null) {
			/*
			 * Assume that the application is running inside CloudOE. FIXME:
			 * this assumption may lead to false positive. Consider a more
			 * reliable test.
			 */
			PROPERTIES_FILENAME = OE_PROPERTIES_FILENAME;
		} else {
			PROPERTIES_FILENAME = TOMCAT_PROPERTIES_FILENAME;
		}
		logger.info("Using engine properties file: " + PROPERTIES_FILENAME);

		props = new EngineProperties();
	}
	
	private EngineProperties() {
		fileMonitor = new FileAlterationMonitor();
		
		// monitor engine.properties for changes
		final File propsFilePath = new File(getClass().getClassLoader().getResource("").getPath());
		final FileAlterationObserver obs = new FileAlterationObserver(propsFilePath, FileFilterUtils.nameFileFilter(PROPERTIES_FILENAME));
		obs.addListener(this);
		
		fileMonitor.addObserver(obs);
		
		try {
			fileMonitor.start();
		} catch (Exception e) {
			logger.error(e);
		}
		
		this.loadPropertiesFile();
	}
	
	private void loadPropertiesFile() {
		this.clear();
		// load engine.properties
		try {
			InputStream is = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILENAME);
			if (is == null) {
				logger.warn("Properties file not found " + PROPERTIES_FILENAME);
			} else {
				this.load(is);
			}
		} catch (IOException e) {
			logger.error("Failed to load from properties file: " + PROPERTIES_FILENAME, e);
		}

		// monitor the external properties file too
		String externalProperties = getProperty(EXTERNAL_PROPERTIES, DEFAULT_EXTERNAL_PROPERTIES);
		final File externalPropsFile = new File(externalProperties);
	
		for (final FileAlterationObserver oldObs : fileMonitor.getObservers()) {
			if (oldObs.getDirectory().equals(externalPropsFile.getParent())) {
				continue;
			}
			if (oldObs.getFileFilter().accept(externalPropsFile)) {
				continue;
			}
				
			final FileAlterationObserver obs = new FileAlterationObserver(externalPropsFile.getParent(), FileFilterUtils.nameFileFilter(externalPropsFile.getName()));
			obs.addListener(this);
			
			fileMonitor.addObserver(obs);
			break;
		}
			
		// also load it now
		this.loadExternalPropertiesFile(externalPropsFile);
		
		try {
			logger.debug("Retrieving PCM-AE PerfConfig...");
			Process p = Runtime.getRuntime().exec("/opt/platform/jre/linux-x86_64/bin/java -cp /opt/platform/gui/3.0/tomcat/webappspub/cpe/WEB-INF/classes/:/opt/platform/perf/1.2/lib/perf_commons.jar:/opt/platform/virtualization/4.1/lib/external/dom4j-1.6.1.jar:/opt/platform/perf/1.2/lib/log4j-1.2.15.jar:/opt/platform/perf/1.2/lib/jaxen-1.1.1.jar:/opt/platform/perf/1.2/lib/commons-dbcp-1.3.jar:/opt/platform/perf/1.2/lib/commons-pool-1.5.4.jar:/opt/platform/perf/1.2/lib/commons-lang-2.1.jar PerfConfig");
			p.waitFor();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = stdout.readLine()) != null) {
				String[] tokens = line.split(":");
				if (tokens.length != 2) {
					continue;
				}
				String key = tokens[0].trim();
				String value = tokens[1].trim();
				if ("user".equals(key)) {
					this.setProperty(PCMAE_USER, value);
				} else if ("password".equals(key)) {
					try {
						String encryptedPassword = new AES().encrypt(value);
						this.setProperty(PCMAE_PASSWORD, encryptedPassword); 
					} catch (GeneralSecurityException e) {
						logger.error("Exception encrypting PCM-AE password.", e);
					}
				} else if ("dbName".equals(key)) {
					this.setProperty(PCMAE_DB, value);
				}
			}
			if (!this.containsKey(PCMAE_DB)) {
				logger.error("PCM-AE DB Name not found!");
			}
			if (!this.containsKey(PCMAE_USER)) {
				logger.error("PCM-AE User not found!");
			}
			if (!this.containsKey(PCMAE_PASSWORD)) {
				logger.error("PCM-AE Password not found!");
			}
		} catch (IOException e) {
			logger.error("Failed to decode PCM-AE PerfConfig." + e);
		} catch (InterruptedException e) {
			logger.error("Failed to decode PCM-AE PerfConfig." + e);
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("All properties");
			for (String key : stringPropertyNames()) {
				String value = getProperty(key);
	
				logger.debug(" - " + key + ": " + value);
			}
		}
	}

	private void loadExternalPropertiesFile(File externalProperties) {
		if (!externalProperties.exists()) {
			logger.warn("External properties file \"" + externalProperties + "\" not found.");
			return;
		}
		try {
			logger.info("Loading external properties file \"" + externalProperties.getAbsolutePath() + "\"...");
			InputStream is = new FileInputStream(externalProperties);
			Properties props = new Properties();
			props.load(is);

			for (String key : props.stringPropertyNames()) {
				String value = props.getProperty(key);

				logger.debug(" - " + key + ": " + value);
				this.setProperty(key, value);
			}
		} catch (IOException e) {
			logger.error("Failed to load from external properties file: " + externalProperties, e);
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("All properties");
			for (String key : stringPropertyNames()) {
				String value = getProperty(key);

				logger.debug(" - " + key + ": " + value);
			}
		}
	}

	public static EngineProperties getInstance() {
		return props;
	}

	public static void main(String[] args) {
		System.out.println(UUID.randomUUID().toString().replaceAll("\\-", ""));
	}

	@Override
	public void onDirectoryChange(File arg0) {
	}

	@Override
	public void onDirectoryCreate(File arg0) {
	}

	@Override
	public void onDirectoryDelete(File arg0) {
	}

	@Override
	public void onFileChange(File arg0) {
		logger.info(String.format("File change detected on %s, reloading ...", arg0.getAbsolutePath()));
		if (arg0.getName().equals(PROPERTIES_FILENAME)) {
		    this.loadPropertiesFile();
		    return;		
		}
		this.loadExternalPropertiesFile(arg0);
	}

	@Override
	public void onFileCreate(File arg0) {
		logger.info(String.format("File create detected on %s, reloading ...", arg0.getAbsolutePath()));
		if (arg0.getName().equals(PROPERTIES_FILENAME)) {
		    this.loadPropertiesFile();
		    return;
		}
		this.loadExternalPropertiesFile(arg0);
	}

	@Override
	public void onFileDelete(File arg0) {
		logger.info(String.format("File delete detected on %s, reloading ...", arg0.getAbsolutePath()));
		this.loadPropertiesFile();
	}

	@Override
	public void onStart(FileAlterationObserver arg0) {
	}

	@Override
	public void onStop(FileAlterationObserver arg0) {
	}
}
