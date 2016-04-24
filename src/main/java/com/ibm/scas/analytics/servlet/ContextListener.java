package com.ibm.scas.analytics.servlet;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.google.inject.servlet.GuiceServletContextListener;
import com.ibm.scas.analytics.EngineProperties;
import com.ibm.scas.analytics.backend.impl.ClusterBuilder;
import com.ibm.scas.analytics.backend.impl.ClusterGarbageCollector;
import com.ibm.scas.analytics.backend.impl.SoftLayerAPIPollerThread;
import com.ibm.scas.analytics.guice.CPEAppModule;
import com.ibm.scas.analytics.guice.CPEServletModule;
import com.ibm.scas.analytics.provider.impl.ServiceProviderPluginFactory;

/**
 * Application Lifecycle Listener implementation class ContextListener
 * 
 */
public class ContextListener extends GuiceServletContextListener {
	private static final Logger logger = Logger.getLogger(ContextListener.class);

	private ScheduledExecutorService scheduler = null;

	/**
	 * Default constructor.
	 */
	public ContextListener() {
		super();
	}
	
	@Override
	protected Injector getInjector() {
	    final Injector injector = Guice.createInjector(
	    		new CPEAppModule(), 
	    		new CPEServletModule()); 
		
		return injector;
	}

	/**
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent event) {
		super.contextInitialized(event);
		
		boolean testMode = "true".equalsIgnoreCase(EngineProperties.getInstance().getProperty(EngineProperties.TEST_MODE));
		if (testMode) {
			logger.info("Test mode. Setting loggers under com.ibm to DEBUG");
			Logger.getLogger("com.ibm").setLevel(Level.DEBUG);
		}
		
		logger.info("Servlet context initialized!");
		final Injector injector = (Injector)event.getServletContext().getAttribute(Injector.class.getName());

		// initialize the persistence service here 
		final PersistService persistService = injector.getInstance(PersistService.class);
		logger.info("Initializing PersistService ...");
		persistService.start();
		
		scheduler = injector.getInstance(ScheduledExecutorService.class);
		
		// log4j monitor (don't use configureAndWatch) -- reload log4j properties every 15 seconds
		scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				URL propertiesURL = null;
				String log4jPropertiesFile = System.getProperty("log4j.configuration");
				
				if (log4jPropertiesFile != null) {
					try {
						propertiesURL = new URL(log4jPropertiesFile);
					} catch (MalformedURLException e) {
						// bad URL
					}
				}
	
				if (propertiesURL == null) {
					// try to find log4j.properties in the classpath
					propertiesURL = this.getClass().getClassLoader().getResource("log4j.properties");
				}
			
				if (propertiesURL != null && propertiesURL.getProtocol().equals("file")) {
					PropertyConfigurator.configure(propertiesURL.getFile());
				}
			}
		}, 0, 15, TimeUnit.SECONDS);
		
	
		final ClusterGarbageCollector cgc = injector.getInstance(ClusterGarbageCollector.class);
		if (cgc.isRunning()) {
			logger.warn("ClusterGarbageCollector thread is running already.  Not scheduling a new ClusterGarbageCollector thread.");
		} else {
			final String gcIntervalStr = EngineProperties.getInstance().getProperty(EngineProperties.GC_INTERVAL, EngineProperties.DEFAULT_GC_INTERVAL_SECS);
			int gcInterval;
			try {
				gcInterval = Integer.parseInt(gcIntervalStr);
			} catch (NumberFormatException e) {
				logger.warn("Failed to parse cluster GC interval: " + gcIntervalStr + ". Using default value of " + EngineProperties.DEFAULT_GC_INTERVAL_SECS + ".");
				gcInterval = Integer.parseInt(EngineProperties.DEFAULT_GC_INTERVAL_SECS);
			}

			logger.info("Scheduling garbage collection at " + gcInterval + " second interval...");
			scheduler.scheduleAtFixedRate(cgc, gcInterval, gcInterval, TimeUnit.SECONDS);
		}
		
		final SoftLayerAPIPollerThread slAPIThread = injector.getInstance(SoftLayerAPIPollerThread.class);
		if (slAPIThread.isRunning()) {
			logger.warn("SoftLayer poller thread is running already.  Not scheduling a new SoftLayer poller thread.");
		} else {
			int pollInterval;
			final String pollIntervalStr = EngineProperties.getInstance().getProperty(EngineProperties.SOFTLAYER_POLL_INTERVAL, 
					EngineProperties.DEFAULT_SOFTLAYER_POLL_INTERVAL_SECS);
			try {
				pollInterval = Integer.parseInt(pollIntervalStr);
			} catch (NumberFormatException e) {
				logger.warn(String.format("Failed to parse SoftLayer polling interval: \"%s\". Using default value of %s.", pollIntervalStr, EngineProperties.DEFAULT_SOFTLAYER_POLL_INTERVAL_SECS));
				pollInterval = Integer.parseInt(EngineProperties.DEFAULT_SOFTLAYER_POLL_INTERVAL_SECS);
			}	

			logger.info("Scheduling SoftLayer polling at " + pollInterval + " second interval...");
			scheduler.scheduleAtFixedRate(slAPIThread, 0, pollInterval, TimeUnit.SECONDS);
		}
		
		final ClusterBuilder cb = injector.getInstance(ClusterBuilder.class);
		if (cb.isRunning()) {
			logger.warn("ClusterBuilder is running already.  Not scheduling a new ClusterBuilder thread.");
		} else {
			final String cbIntervalStr = EngineProperties.getInstance().getProperty(EngineProperties.CLUSTER_BUILDER_INTERVAL, EngineProperties.DEFAULT_CLUSTER_BUILDER_INTERVAL_SECS);
			int cbInterval;
			try  {
				cbInterval = Integer.parseInt(cbIntervalStr);
			} catch (NumberFormatException e) {
				logger.warn("Failed to parse cluster ClusterBuilder interval: " + cbIntervalStr + ". Using default value of " + EngineProperties.DEFAULT_CLUSTER_BUILDER_INTERVAL_SECS + ".");
				cbInterval = Integer.parseInt(EngineProperties.DEFAULT_CLUSTER_BUILDER_INTERVAL_SECS);
			}
			
			logger.info("Scheduling Cluster builder at " + cbInterval + " second interval...");
			scheduler.scheduleAtFixedRate(cb, 0, cbInterval, TimeUnit.SECONDS);	
		}
	}

	/**
	 * @see ServletContextListener#contextDestroyed(ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent event) {
		logger.info("Servlet context destroyed");
		final Injector injector = (Injector)event.getServletContext().getAttribute(Injector.class.getName());
		
		scheduler = injector.getInstance(ScheduledExecutorService.class);
		if (scheduler != null) {
			final List<Runnable> runningThreads = scheduler.shutdownNow();
			logger.info(String.format("Shutting down thread pool: %d threads were awaiting execution", runningThreads.size()));
			try {
				// wait for termination
				scheduler.awaitTermination(10000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				logger.warn(e.getLocalizedMessage(), e);
			}
			
			scheduler = null;
		}
		
		// also shut down the persistence factory to clean up
		// managed entities and db connection
		//getInjector().getInstance(PersistenceInitializer.class).shutdown();
		final PersistService persistService = injector.getInstance(PersistService.class);
		persistService.stop();	
		
		// get rid of all cached plugins
		final ServiceProviderPluginFactory pluginFactory = injector.getInstance(ServiceProviderPluginFactory.class);
		pluginFactory.invalidate();
		
		super.contextDestroyed(event);
	}

}
