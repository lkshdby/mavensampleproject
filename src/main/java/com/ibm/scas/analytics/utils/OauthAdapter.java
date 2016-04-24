package com.ibm.scas.analytics.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.ibm.scas.analytics.EngineProperties;

/**
 * A simple OAuth wrapper using the Apache Http client and SignPost library
 * 
 * @author Han Chen
 *
 */
public class OauthAdapter {
	private static final Logger logger = Logger.getLogger(OauthAdapter.class);
	private OAuthConsumer consumer;

	protected HttpClient getHttpClient() throws Exception {
		HttpClient defaultClient = new DefaultHttpClient();

		boolean ignoreSslError = Boolean.parseBoolean(EngineProperties.getInstance().getProperty(EngineProperties.IGNORE_SSL_ERROR, "false"));
		if (ignoreSslError) {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, new TrustManager[] { SSLUtils.getDummyTrustManager() }, null);
			SSLSocketFactory ssf = new SSLSocketFactory(sc);
			ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			ClientConnectionManager ccm = defaultClient.getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
			sr.register(new Scheme("https", ssf, 443));
			return new DefaultHttpClient(ccm, defaultClient.getParams());
		} else {
			return defaultClient;
		}
	}

	public OauthAdapter(String consumerKey, String consumerSecret) {
		consumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
	};

	private String readResponse(HttpResponse response) throws IllegalStateException, IOException {
		InputStream is = response.getEntity().getContent();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] buf = new byte[512];
		int len;
		while ((len = is.read(buf)) > 0) {
			os.write(buf, 0, len);
		}
		String result = new String(os.toByteArray());
		logger.debug("Response: " + result);
		return result;
	}

	public String doGet(String url) {
		if (logger.isDebugEnabled()) {
			logger.debug("GET, url: " + url);
		}
		try {
			HttpGet get = new HttpGet(url);
			get.setHeader("Accept", "application/json");
			consumer.sign(get);

			HttpClient httpClient = getHttpClient();
			HttpResponse response = httpClient.execute(get);

			return readResponse(response);
		} catch (Exception e) {
			logger.error("Failed to GET URL using OAuth: " + url, e);
			return null;
		}
	}

	public String doPost(String url, String body) {
		if (logger.isDebugEnabled()) {
			logger.debug("POST, url: " + url);
		}
		try {
			HttpPost post = new HttpPost(url);
			post.setHeader("Accept", "application/json");
			post.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
			consumer.sign(post);

			HttpClient httpClient = getHttpClient();
			HttpResponse response = httpClient.execute(post);

			return readResponse(response);
		} catch (Exception e) {
			logger.error("Failed to POST to URL using OAuth: " + url, e);
			return null;
		}
	}
	
	public String signUrl(String url) {
		if (logger.isDebugEnabled()) {
			logger.debug("Sign URL: " + url);
		}
		try {
			String signedUrl = consumer.sign(url);
			if (logger.isDebugEnabled()) {
				logger.debug("Signed URL: " + signedUrl);
			}
			return signedUrl;
		} catch (Exception e) {
			logger.error("Failed to sign URL using OAuth: " + url, e);
			return null;
		}
	}
	
	public static void main(String[] args) throws ClientProtocolException, IOException {
		Logger.getLogger("org.apache.http").setLevel(Level.DEBUG);
		
//		OauthAdapter adapter = new OauthAdapter("hans-test-7851", "8ap8yvwfNijVfR35");
//		OauthAdapter adapter = new OauthAdapter("test-3", "aXNZZ8kR2K05g262");
//		OauthAdapter adapter = new OauthAdapter("ibm-infosphere-streams-7172", "llwJXFjIntF9x8it");
		OauthAdapter adapter = new OauthAdapter("han-test-811", "tVvnU2PNzCBbZcOt");
		String url = "https://test1.marketplace-test.ibmcloud.com/finishprocure?token=e9281f9c-4f4d-479b-aaa6-0e5fdf36612d";
		url += "&success=false&errorCode=OPERATION_CANCELED&message=Invalid+Promo+Code";
//		url += "&success=false&errorCode=UNAUTHORIZED&message=Invalid+Promo+Code";
//		url += "&success=true&errorCode=UNAUTHORIZED&message=failed&accountIdentifier=0";
//		url += "&success=true&accountIdentifier=1&message=FINE";
		
		
		String signedUrl = adapter.signUrl(url);
//		logger.info("Signed URL: " + signedUrl);
		
		HttpGet get = new HttpGet(signedUrl);
		HttpClient httpClient = new DefaultHttpClient();
		String auth = new String(Base64.encodeBase64("ibmtest1:Marketplace#3!".getBytes()));
		get.setHeader("Authorization", "Basic " + auth);
		HttpParams params = new BasicHttpParams();
		params.setParameter("http.protocol.handle-redirects",false);
		get.setParams(params);
		
		HttpResponse response = httpClient.execute(get);

		String result = adapter.readResponse(response);
//		logger.info("Result: " + result);
//		System.out.println(signedUrl);
	}
}