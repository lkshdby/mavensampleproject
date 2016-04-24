package com.ibm.scas.analytics.provider.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ibm.scas.analytics.EngineProperties;
import com.ibm.scas.analytics.backend.PcmaeGateway;
import com.ibm.scas.analytics.persistence.PersistenceService;
import com.ibm.scas.analytics.persistence.beans.CPELocation;
import com.ibm.scas.analytics.persistence.beans.Offering;
import com.ibm.scas.analytics.persistence.beans.Plugin;
import com.ibm.scas.analytics.persistence.beans.PluginParam;
import com.ibm.scas.analytics.persistence.util.WhereClause;
import com.ibm.scas.analytics.provider.ServiceProvider;
import com.ibm.scas.analytics.provider.ServiceProviderException;
import com.ibm.scas.analytics.utils.CPEException;

/**
 * Factory for creating service provider plugins
 * 
 * @author Han Chen
 * 
 */
@Singleton
public class ServiceProviderPluginFactory {
	private static final Logger logger = Logger.getLogger(ServiceProviderPluginFactory.class);

	private static Map<String, ServiceProvider> pluginCache = new HashMap<String, ServiceProvider>();
	
	@Inject private PersistenceService persistence;
	@Inject private PcmaeGateway pcmaeGateway;

	/**
	 * Loads a service provider plugin from cache
	 * 
	 * @param className
	 * @return
	 */
	public ServiceProvider getPlugin(String offeringId) throws CPEException {
		synchronized (pluginCache) {
			ServiceProvider plugin = pluginCache.get(offeringId);
			if (plugin != null) {
				return plugin;
			}
			
			logger.debug("Loading plugin for offering: " + offeringId);
			final Offering offering = persistence.getObjectById(Offering.class, offeringId);
			final String plugindId = offering.getPlugin().getId();
			logger.debug("Offering plugin id is " + plugindId);

			Plugin details = persistence.getObjectById(Plugin.class, plugindId);
			if (details == null) {
				throw new CPEException(String.format("Failed to load plugin %s details! Check CPE DB consistency"));
			} 
			
			final String className = details.getClassName();
			final String source = details.getSource();
			logger.trace(String.format("Offering plugin %s class: %s source: %s", plugindId, className, source));

			plugin = createPlugin(source, className);
			if (plugin == null) {
				throw new CPEException(String.format("Failed to create plugin %s for offering %s! Source: %s, ClassName: %s", plugindId, offeringId, source, className));
			}
			
			final Map<String, String> params = new HashMap<String, String>();
			final List<PluginParam> pluginParams =  persistence.getObjectsBy(PluginParam.class, new WhereClause("plugin", plugindId));
			for (final PluginParam pluginParam : pluginParams) {
				params.put(pluginParam.getName(), pluginParam.getValue());
			}
			
			final String cpeLocationName = EngineProperties.getInstance().getProperty(EngineProperties.CPE_LOCATION_NAME, EngineProperties.DEFAULT_CPE_LOCATION_NAME);
			final CPELocation cpeLocation = persistence.getObjectById(CPELocation.class, cpeLocationName);
			if (cpeLocation == null) {
				throw new CPEException(String.format("Failed to find CPELOCATION record for %s", cpeLocationName));
			}
			
			try {
				plugin.init(pcmaeGateway.getClusterApi(cpeLocation.getPcmaeBackend().getId()), params);
			} catch (ServiceProviderException e) {
				throw new CPEException(String.format("Error initializing plugin %s for offering %s: %s", plugindId, offeringId, e.getLocalizedMessage()));
			}
			
			pluginCache.put(offeringId, plugin);
			
			return plugin;
		}
	}

	/**
	 * Creates a service provider plugin object from the specified class name
	 * 
	 * @param source indicates where to load the class from, null means the default classloader
	 * @param className FQ classname
	 * @return
	 */
	private ServiceProvider createPlugin(String source, String className) throws CPEException {
		ServiceProvider plugin = null;
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Creating service provider plugin from class " + className);
			}
			Class<?> clazz = null;
			if (source == null) {
				clazz = Class.forName(className);
			} else {
				URL url = new URL(source);
				URLClassLoader loader = new URLClassLoader(new URL[] {url}, ServiceProviderPluginFactory.class.getClassLoader());
				if (logger.isDebugEnabled()) {
					logger.debug(" - class loader: " + loader);
				}
				clazz = loader.loadClass(className);
			}
			Object instance = clazz.newInstance();
			if (instance instanceof ServiceProvider) {
				plugin = (ServiceProvider) instance;
			} else {
				logger.error("Class " + className + " does not implement ServiceProvider");
			}
		} catch (ClassNotFoundException e) {
			logger.error("Exception in creating service provider plugin.", e);
			throw new CPEException(e.getLocalizedMessage(), e);
		} catch (InstantiationException e) {
			logger.error("Exception in creating service provider plugin.", e);
			throw new CPEException(e.getLocalizedMessage(), e);
		} catch (IllegalAccessException e) {
			logger.error("Exception in creating service provider plugin.", e);
			throw new CPEException(e.getLocalizedMessage(), e);
		} catch (MalformedURLException e) {
			logger.error("Exception in creating service provider plugin.", e);
			throw new CPEException(e.getLocalizedMessage(), e);
		}
		return plugin;
	}

	public void invalidate() {
		synchronized (pluginCache) {
			pluginCache.clear();
			logger.info("All plugin cache cleared.");
		}
	}

	public void invalidate(String plugin) {
		synchronized (pluginCache) {
			if (pluginCache.containsKey(plugin)) {
				pluginCache.remove(plugin);
				logger.info("Plugin cache for " + plugin + " cleared.");
			}
		}
	}
}
