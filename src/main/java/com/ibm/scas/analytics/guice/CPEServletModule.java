package com.ibm.scas.analytics.guice;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import com.ibm.scas.analytics.servlet.CPEPersistFilter;
import com.ibm.scas.analytics.servlet.ContentProxyServlet;
import com.ibm.scas.analytics.servlet.LifeCycleServlet;
import com.ibm.scas.analytics.servlet.LoginServlet;
import com.ibm.scas.analytics.servlet.ReportServlet;
import com.ibm.scas.analytics.servlet.ValidateServlet;
import com.ibm.scas.analytics.utils.IdPropertyFilter;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class CPEServletModule extends ServletModule {
	final Logger logger = Logger.getLogger(CPEServletModule.class);
	
	@Provides 
	@Singleton
	ObjectMapper objectMapper() {
	    final ObjectMapper mapper = new ObjectMapper();
	    final FilterProvider filters = new SimpleFilterProvider().addFilter("idFilter", 
	    		new IdPropertyFilter());
	    mapper.setFilters(filters);
	    return mapper;
	}

	
	@Provides
	@Singleton
	public JacksonJsonProvider jacksonJsonProvider(ObjectMapper mapper) {
		return new JacksonJsonProvider(mapper);
	}
	
	@Override
	protected void configureServlets() {
		filter("/*").through(CPEPersistFilter.class);
		
	
		/* Run all servlets through the PersistFilter which injects the EntityManager 
		 * and starts the UnitOfWork */
		install(new JerseyServletModule() {
			@Override
			protected void configureServlets() {
				
				bind(GuiceContainer.class);
				PackagesResourceConfig resourceConfig = new PackagesResourceConfig(
					"com.ibm.scas.analytics.resources", 
					"com.ibm.scas.analytics.resources.admin");
				
				for (Class<?> resource : resourceConfig.getClasses()) {
					logger.debug("Binding " + resource.getCanonicalName() + " ...");
					bind(resource);
				}
				
				final Map<String, String> params = new HashMap<String, String>();
				params.put(PackagesResourceConfig.PROPERTY_PACKAGES, 
						"com.ibm.scas.analytics.resources;com.ibm.scas.analytics.resources.admin");
				//params.put("com.sun.jersey.api.json.POJOMappingFeature", "true");
				//params.put("com.sun.jersey.config.feature.Trace", "true");
			    //params.put("com.sun.jersey.spi.container.ContainerRequestFilters", "com.sun.jersey.api.container.filter.LoggingFilter");
		        serve("/rest/*").with(GuiceContainer.class, params);
			}
			
		});

		/*
		  <servlet>
		    <servlet-name>lifecycle-servlet</servlet-name>
		    <servlet-class>com.ibm.scas.analytics.servlet.LifeCycleServlet</servlet-class>
		  </servlet>
		  <servlet-mapping>
		    <servlet-name>lifecycle-servlet</servlet-name>
		    <url-pattern>/lifecycle/*</url-pattern>
		  </servlet-mapping>
		 */
        bind(LifeCycleServlet.class);
		serve("/lifecycle/*").with(LifeCycleServlet.class);
		/*
		  <servlet>
		    <servlet-name>login-servlet</servlet-name>
		    <servlet-class>com.ibm.scas.analytics.servlet.LoginServlet</servlet-class>
		  </servlet>
		  <servlet-mapping>
		    <servlet-name>login-servlet</servlet-name>
		    <url-pattern>/login/*</url-pattern>
		  </servlet-mapping>
		 */
		bind(LoginServlet.class);
		serve("/login/*").with(LoginServlet.class);
		/*
		  <servlet>
		    <servlet-name>validate-servlet</servlet-name>
		    <servlet-class>com.ibm.scas.analytics.servlet.ValidateServlet</servlet-class>
		  </servlet>
		  <servlet-mapping>
		    <servlet-name>validate-servlet</servlet-name>
		    <url-pattern>/validate/*</url-pattern>
		  </servlet-mapping>
		 */
		bind(ValidateServlet.class);
		serve("/validate/*").with(ValidateServlet.class);
		/*
		  <servlet>
		    <servlet-name>report-servlet</servlet-name>
		    <servlet-class>com.ibm.scas.analytics.servlet.ReportServlet</servlet-class>
		  </servlet>
		  <servlet-mapping>
		    <servlet-name>report-servlet</servlet-name>
		    <url-pattern>/report/*</url-pattern>
		  </servlet-mapping>
		 */
		bind(ReportServlet.class);
		serve("/report/*").with(ReportServlet.class);
		/*
		  <servlet>
		  	<description>
		  	</description>
		  	<display-name>ContentProxyServlet</display-name>
		  	<servlet-name>ContentProxyServlet</servlet-name>
		  	<servlet-class>com.ibm.scas.analytics.servlet.ContentProxyServlet</servlet-class>
		  </servlet>
		  <servlet-mapping>
		  	<servlet-name>ContentProxyServlet</servlet-name>
		  	<url-pattern>/dynamic/*</url-pattern>
		  </servlet-mapping>
		 */
		bind(ContentProxyServlet.class);
		serve("/dynamic/*").with(ContentProxyServlet.class);
	}
}