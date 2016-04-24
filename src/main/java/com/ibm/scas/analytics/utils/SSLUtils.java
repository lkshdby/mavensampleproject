package com.ibm.scas.analytics.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

public class SSLUtils {
	private static final Logger logger = Logger.getLogger(SSLUtils.class);
	
	public static X509TrustManager getDummyTrustManager() {
		return new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		};
	}
	
	public static ClientConfig getTrustingSSLClientConfig() {
		TrustManager[] certs = new TrustManager[] { getDummyTrustManager() };
		SSLContext ctx = null;
		try {
			try {
				// IBM JDK
				ctx = SSLContext.getInstance("SSL_TLS");
			} catch (NoSuchAlgorithmException e) {
			}
			if (ctx == null) {
				// Sun JDK
				ctx = SSLContext.getInstance("TLS");
			}
			ctx.init(null, certs, new SecureRandom());
		} catch (java.security.GeneralSecurityException ex) {
			logger.error("Exception in configureClient", ex);
		}
		HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());

		ClientConfig config = new DefaultClientConfig();
		try {
			config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			}, ctx));
		} catch (Exception e) {
			logger.error("Problem putting HTTPS properties: " + e.getMessage());
		}
		return config;
	}

	public static HostnameVerifier getAllHostVerifier() {
		return new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
	}

	public static SSLContext disableSSLCertValidation() throws Exception {

		// Create a trust manager that does not validate certificate chains
		TrustManager[] tma = new TrustManager[] { getDummyTrustManager() };

		// Install the all-trusting trust manager
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, tma, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(getAllHostVerifier());
		return sc;
	}
}