package com.ibm.scas.analytics.guice;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.logging.SessionLog;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.matcher.Matchers;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.ibm.scas.analytics.EngineProperties;
import com.ibm.scas.analytics.backend.FirewallService;
import com.ibm.scas.analytics.backend.NetworkService;
import com.ibm.scas.analytics.backend.ProvisioningService;
import com.ibm.scas.analytics.backend.SoftLayerAccountProvider;
import com.ibm.scas.analytics.backend.SoftLayerOrderProvider;
import com.ibm.scas.analytics.backend.TenantService;
import com.ibm.scas.analytics.backend.appdirect.AppDirectGatewayFactory;
import com.ibm.scas.analytics.backend.impl.ClusterBuilder;
import com.ibm.scas.analytics.backend.impl.ClusterGarbageCollector;
import com.ibm.scas.analytics.backend.impl.SoftLayerAPIPollerThread;
import com.ibm.scas.analytics.backend.impl.TenantServiceImpl;
import com.ibm.scas.analytics.content.DataProvider;
import com.ibm.scas.analytics.content.DynamicLoader;
import com.ibm.scas.analytics.persistence.PersistenceService;
import com.ibm.scas.analytics.persistence.util.EntityIdGenerator;
import com.ibm.scas.analytics.provider.impl.ServiceProviderPluginFactory;
import com.ibm.scas.analytics.reports.AccountReport;
import com.ibm.scas.analytics.utils.ReadWriteLockTable;

public class CPEAppModule extends AbstractModule {
	final Logger logger = Logger.getLogger(CPEAppModule.class);
	
	@Provides 
	@Singleton
	ScheduledExecutorService getThreadPool() {
		return Executors.newScheduledThreadPool(30, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				// name the thread the same as the class
				final Thread t = new Thread(r);
				t.setName(r.getClass().getSimpleName() + "-" + t.getId());
				return t;
			}
		});
	}

	private JpaPersistModule getJPAPersistModule(Class<? extends PersistenceService> persistenceCls) {
		/* Allow Guice to manage the persistence layer, but add our connection properties */
		PersistenceService pServiceInst = null;
		
		try {
			pServiceInst = persistenceCls.newInstance();
		} catch (InstantiationException e) {
			logger.error("Exception in creating persistence service.", e);
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			logger.error("Exception in creating persistence service.", e);
			throw new RuntimeException(e);
		}
		
		final Map<String,String> connectionProps = pServiceInst.getConnectionProperties();
		final String ddlGenerationProp = EngineProperties.getInstance().getProperty(EngineProperties.PERSISTENCE_DDL_GENERATION, PersistenceUnitProperties.NONE);
		final String logLevelProp = EngineProperties.getInstance().getProperty(EngineProperties.PERSISTENCE_LOGGING_LEVEL, SessionLog.INFO_LABEL);
		final String enableDBCache = EngineProperties.getInstance().getProperty(EngineProperties.PERSISTENCE_ENABLE_DB_CACHE, Boolean.FALSE.toString());
		connectionProps.put(PersistenceUnitProperties.DDL_GENERATION, ddlGenerationProp);
		connectionProps.put(PersistenceUnitProperties.LOGGING_LEVEL, logLevelProp);
		connectionProps.put(PersistenceUnitProperties.WEAVING, "static");
		connectionProps.put(PersistenceUnitProperties.SESSION_CUSTOMIZER, EntityIdGenerator.class.getCanonicalName());
		connectionProps.put(PersistenceUnitProperties.CACHE_SHARED_DEFAULT, enableDBCache);
		final JpaPersistModule cpePersistModule = new JpaPersistModule("cpe").properties(connectionProps);
		return cpePersistModule;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void configure() {
		/* use Guice to construct all factory objects and bind them here. */
		
		// always call afterInject() after instantiation
		bindListener(Matchers.any(), new TypeListener() {
		    @Override
		    public <I> void hear(final TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter) {
		        typeEncounter.register(new InjectionListener<I>() {
		            @Override
		            public void afterInjection(I i) {
		            	final Method m;
		            	try {
		            		m = i.getClass().getDeclaredMethod("afterInject");
						} catch (NoSuchMethodException e) {
							// no "afterInject" method, oh well
							return;
						} catch (SecurityException e) {
							// no "afterInject" method, oh well
							return;
						}
						
						try {
		            		if (m == null) {
		            			return;
		            		}

		            		m.setAccessible(true);
		            		m.invoke(i);
				        } catch (IllegalAccessException e) {
							// This shouldn't happen
							logger.error(e.getLocalizedMessage(), e);
							throw new RuntimeException(e);
						} catch (IllegalArgumentException e) {
							// This shouldn't happen, there should be no arguments
							logger.error(e.getLocalizedMessage(), e);
							throw e;
						} catch (InvocationTargetException e) {
							// This shouldn't happen
							logger.error(e.getLocalizedMessage(), e);
							throw new RuntimeException(e);
						}
		            }
		        });
		    }
		});
		
		/* Persistence service implementation from engine.properties */
		final String persistenceProviderCls = EngineProperties.getInstance().getProperty(EngineProperties.PERSISTENCE_PROVIDER, EngineProperties.DEFAULT_PERSISTENCE_PROVIDER);
		try {
			final Class<?> clazz = (Class<?>) Class.forName(persistenceProviderCls);
			if (!PersistenceService.class.isAssignableFrom(clazz)) {
				throw new RuntimeException("Class " + clazz + " does not implement PersistenceService");
			}
			
			install(getJPAPersistModule((Class<? extends PersistenceService>) clazz));
			bind(PersistenceService.class).to((Class<? extends PersistenceService>) clazz).in(Singleton.class);;
		} catch (ClassNotFoundException e) {
			// Should not happen, deadly exception!
			throw new RuntimeException(e);
		}
		
		/* Provisioning provider implementation from engine.properties */
		final String provisioningProviderCls = EngineProperties.getInstance().getProperty(EngineProperties.PROVISIONING_PROVIDER, EngineProperties.DEFAULT_PROVISIONING_PROVIDER);
		try {
			final Class<?> clazz = (Class<?>) Class.forName(provisioningProviderCls);
			if (!ProvisioningService.class.isAssignableFrom(clazz)) {
				throw new RuntimeException("Class " + clazz + " does not implement ProvisioningService");
			}
			bind(ProvisioningService.class).to((Class<? extends ProvisioningService>) clazz).in(Singleton.class);;
		} catch (ClassNotFoundException e) {
			// Should not happen, deadly exception!
			throw new RuntimeException(e);
		}

		/* Network provider implementation from engine.properties */
		final String networkProviderCls = EngineProperties.getInstance().getProperty(EngineProperties.NETWORK_PROVIDER, EngineProperties.DEFAULT_NETWORK_PROVIDER);
		try {
			final Class<?> clazz = (Class<?>) Class.forName(networkProviderCls);
			if (!NetworkService.class.isAssignableFrom(clazz)) {
				throw new RuntimeException("Class " + clazz + " does not implement NetworkService");
			}
			bind(NetworkService.class).to((Class<? extends NetworkService>) clazz).in(Singleton.class);;
		} catch (ClassNotFoundException e) {
			// Should not happen, deadly exception!
			throw new RuntimeException(e);
		}
		
		/* Firewall provider implementation from engine.properties */
		final String firewallProviderCls = EngineProperties.getInstance().getProperty(EngineProperties.FIREWALL_PROVIDER, EngineProperties.DEFAULT_FIREWALL_PROVIDER);
		try {
			final Class<?> clazz = (Class<?>) Class.forName(firewallProviderCls);
			if (!FirewallService.class.isAssignableFrom(clazz)) {
				throw new RuntimeException("Class " + clazz + " does not implement FirewallService");
			}
			bind(FirewallService.class).to((Class<? extends FirewallService>) clazz).in(Singleton.class);;
		} catch (ClassNotFoundException e) {
			// Should not happen, deadly exception!
			throw new RuntimeException(e);
		}	
		
		/* bind any other service classes here  -- Guice instantiated with dependencies injected */
		bind(ServiceProviderPluginFactory.class);
		bind(DynamicLoader.class);
		// TODO: DataProvider has some static fields that might be Session Scoped
		bind(DataProvider.class).in(Singleton.class);
		// use this class to generate AccountReport, with dependencies injected
		//bind(AccountReport.class).toProvider(AccountReportProvider.class);
		bind(AccountReport.class);
		// factory class for AppDirectGateway
		install(new FactoryModuleBuilder()
			.build(AppDirectGatewayFactory.class));
		bind(TenantService.class).to(TenantServiceImpl.class);
		bind(SoftLayerAccountProvider.class);
		bind(SoftLayerOrderProvider.class);
		bind(ClusterBuilder.class);
		bind(ClusterGarbageCollector.class);
		bind(SoftLayerAPIPollerThread.class);
		bind(new TypeLiteral<ReadWriteLockTable<String>>(){});
		
	}
}
