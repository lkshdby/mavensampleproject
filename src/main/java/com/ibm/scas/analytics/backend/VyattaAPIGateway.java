package com.ibm.scas.analytics.backend;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.HttpRoutedConnection;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.AbstractVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.ibm.scas.analytics.EngineProperties;
import com.ibm.scas.analytics.beans.Gateway;
import com.ibm.scas.analytics.beans.GatewayMember;
import com.ibm.scas.analytics.utils.CPEException;
import com.ibm.scas.analytics.utils.CollectionsUtil;
import com.ibm.scas.analytics.utils.JsonUtil;


public class VyattaAPIGateway {
	private static Logger logger = Logger.getLogger(VyattaAPIGateway.class);

	private final DefaultHttpClient httpClient; 
	
	private final Collection<VyattaAPIGatewayMember> gatewayMembers;

	/**
	 * The insecure way of creating the gateway.  we accept the SSL certificate (whatever it is)
	 * @param ip
	 * @param username
	 * @param password
	 * @throws CPEException
	 */
	protected VyattaAPIGateway(String ip, String username, String password) throws CPEException {
		final VyattaAPIGatewayMember mem = new VyattaAPIGatewayMember();
		mem.memberIp = ip;
		mem.username = username;
		mem.password = password;
		
		final String sslCert = getSSLCertificate(ip);
		mem.sslCert = sslCert;
		this.gatewayMembers = Arrays.asList(mem);
		
		this.httpClient = new DefaultHttpClient(new PoolingClientConnectionManager());
		this.initHTTPClient();
	}
	
	// connect to Vyatta gateway member
	public VyattaAPIGateway(Collection<GatewayMember> members) throws CPEException {

		this.httpClient = new DefaultHttpClient(new PoolingClientConnectionManager());

		this.gatewayMembers = new ArrayList<VyattaAPIGatewayMember>(members.size());
		
		for (final GatewayMember gwMem : members) {
			final VyattaAPIGatewayMember mem = new VyattaAPIGatewayMember();
			mem.memberIp = gwMem.getMemberIp();
			mem.username = gwMem.getUsername();
			mem.password = gwMem.getPassword();
			mem.sslCert = gwMem.getSslCert();
			this.gatewayMembers.add(mem);
		}
		
		this.initHTTPClient();

	}
	
	private void initHTTPClient() throws CPEException {
		try {
			this.setHTTPClientParrams();
			
			final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
			ks.load(null, null);
			
			final Set<String> gatewayIPs = new HashSet<String>(this.gatewayMembers.size());
			
			for (final VyattaAPIGatewayMember member : this.gatewayMembers) {
				/* add all certificates in the map to the truststore */
				final CertificateFactory cf = CertificateFactory.getInstance("X.509");

				final Certificate realCert = cf.generateCertificate(new ByteArrayInputStream(Base64.decodeBase64(member.sslCert.getBytes())));
				ks.setCertificateEntry(String.format("vyatta-%s", member.memberIp), realCert);
				
				gatewayIPs.add(member.memberIp);
			}

			final SSLContext sc = SSLContext.getInstance("TLS");
			final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(ks);

			sc.init(null, tmf.getTrustManagers(), null);

			this.httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, new SSLSocketFactory(sc, new AbstractVerifier() {
				@Override
				public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
					if (host == null || !gatewayIPs.contains(host)) {
						// the certificate host needs to match one of our gateway IP
						throw new SSLException(String.format("Host %s does not match one of Vyatta member IPs: %s", host, gatewayIPs));
					}

					if (cns == null || cns.length != 1) {
						// must have at least one CN
						throw new SSLException(String.format("Invalid CN in Vyatta SSL certificate"));
					}

					final Set<String> cnSet = new HashSet<String>(Arrays.asList(cns));
					if (!cnSet.contains("Vyatta Web GUI")) {
						// the CN list must contain "Vyatta Web GUI"
						throw new SSLException(String.format("Invalid CNs in Vyatta SSL certificate: %s", cnSet));
					}
				}
			})));
		} catch (KeyStoreException e) {
			throw new CPEException(e.getLocalizedMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			throw new CPEException(e.getLocalizedMessage(), e);
		} catch (CertificateException e) {
			throw new CPEException(e.getLocalizedMessage(), e);
		} catch (KeyManagementException e) {
			throw new CPEException(e.getLocalizedMessage(), e);
		} catch (IOException e) {
			throw new CPEException(e.getLocalizedMessage(), e);
		}	
	}
	
	// connect to single member
	public VyattaAPIGateway(final String ip, String username, String password, final String sslCert) throws CPEException {
		final VyattaAPIGatewayMember mem = new VyattaAPIGatewayMember();
		mem.memberIp = ip;
		mem.username = username;
		mem.password = password;
		mem.sslCert = sslCert;
		
		this.gatewayMembers = Arrays.asList(mem);
		this.httpClient = new DefaultHttpClient(new PoolingClientConnectionManager());
		
		this.initHTTPClient();
	}
	
	public VyattaAPIGateway(Gateway gatewayRec) throws CPEException {
		this(gatewayRec.getGatewayMembers());
	}
	
	public static String getSSLCertificate(final String vyattaIP) throws CPEException {
		final SSLContext sslContext;
		
	    try {
	    	// create SSL context using an all-trusting trust manager
	    	sslContext = SSLContext.getInstance("TLS");
	    	sslContext.init(
    			null,  	// KeyManager[]
    			new TrustManager[] { 
	    			new X509TrustManager() {
	    				/* completely insecure trust manager.  we are trusting all server certificates in order
	    				 * to get the actual server certificate for saving
	    				 */
	    				@Override
	    				public X509Certificate[] getAcceptedIssuers() {
	    					// all certificates accepted
	    					return null;
	    				}

	    				@Override
	    				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	    					// server always trusted
	    				}

	    				@Override
	    				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	    					// client always trusted -- however we never do client certificate authentication
	    				}
	    			}
		    	}, 		// TrustManager[]
		    	null	// Random
		    );

	    	final DefaultHttpClient testHttpClient = new DefaultHttpClient();
	    	testHttpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, new SSLSocketFactory(sslContext, new AbstractVerifier() {
				@Override
				public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
					if (host == null || !host.equals(vyattaIP)) {
						// the certificate host needs to match our gateway IP
						throw new SSLException(String.format("Host %s does not match Vyatta IP %s", host, vyattaIP));
					}
					
					if (cns == null || cns.length != 1) {
						// must have one CN
						throw new SSLException(String.format("Invalid CN in Vyatta %s SSL certificate", vyattaIP));
					}
					
					final Set<String> cnSet = new HashSet<String>(Arrays.asList(cns));
					if (!cnSet.contains("Vyatta Web GUI")) {
						// the CN list must contain "Vyatta Web GUI"
						throw new SSLException(String.format("Invalid CNs in Vyatta %s SSL certificate: %s", vyattaIP, cnSet));
					}
				}
			})));
	    	
	    	testHttpClient.addRequestInterceptor(new HttpRequestInterceptor() {
				@Override
				public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
					final HttpRoutedConnection conn = (HttpRoutedConnection) context.getAttribute(ExecutionContext.HTTP_CONNECTION);
					// add the last certificate to the context -- this is the server certificate
					final Certificate[] certs = conn.getSSLSession().getPeerCertificates();
					context.setAttribute("server_certificate", certs[certs.length -1 ]);
				}
			});

	    	// open the Vyatta URL
	    	final HttpContext httpContext = new BasicHttpContext();
	    	testHttpClient.execute(new HttpGet(String.format("https://%s/rest/conf", vyattaIP)), httpContext);
	    	
	    	// grab the certificate we saved in the context
	    	final Certificate serverCert = (Certificate)httpContext.getAttribute("server_certificate");
	    	
    		return new String(Base64.encodeBase64(serverCert.getEncoded()));
	    } catch (SSLException e ) {
	    	throw new CPEException(e.getLocalizedMessage(), e);
		} catch (NoSuchAlgorithmException e1) {
	    	throw new CPEException(e1.getLocalizedMessage(), e1);
		} catch (KeyManagementException e1) {
	    	throw new CPEException(e1.getLocalizedMessage(), e1);
		} catch (ClientProtocolException e) {
	    	throw new CPEException(e.getLocalizedMessage(), e);
		} catch (IOException e) {
	    	throw new CPEException(e.getLocalizedMessage(), e);
		} catch (CertificateEncodingException e) {
	    	throw new CPEException(e.getLocalizedMessage(), e);
		}
	}
	
	/**
	 * test the username/password for all members
	 * @throws CPEException
	 */
	public void testCredentials() throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace("testCredentials()");
		}
	
		final List<String> errorMsgs = new ArrayList<String>(gatewayMembers.size());
		for (final VyattaAPIGatewayMember member : gatewayMembers) {
			final String restAPIUrl = String.format("https://%s/rest/conf", member.memberIp);
			
			final byte[] creds64bytes = Base64.encodeBase64(String.format("%s:%s", member.username, member.password).getBytes());
			final String b64creds = new String(creds64bytes);	
			final HttpGet testURL = new HttpGet(restAPIUrl);
			testURL.addHeader("Authorization", String.format("Basic %s", b64creds));

			final StringBuilder sb = new StringBuilder();
			try {
				if (logger.isDebugEnabled()) {
					logger.debug(String.format("Attempting to authenticate: %s", testURL.getURI().toString()));
				}
				
				final HttpResponse resp = httpClient.execute(testURL);

				final BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
				String line;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				reader.close();

				if (resp.getStatusLine().getStatusCode() != 200) {
					throw new CPEException(String.format("Unable to validate Vyatta account credentials on member %s: HTTP %d", member.memberIp, resp.getStatusLine().getStatusCode()));
				}
				
			} catch (ClientProtocolException e) {
				errorMsgs.add(String.format("Unable to validate Vyatta account credentials on member %s: %s", member.memberIp, e.getLocalizedMessage()));
			} catch (IOException e) {
				errorMsgs.add(String.format("Unable to validate Vyatta account credentials on member %s: %s", member.memberIp, e.getLocalizedMessage()));
			}
		}
	}
	
	/**
	 * start a session on all members
	 * @throws CPEException
	 */
	public void startSessionAllMembers() throws CPEException {
		for (final VyattaAPIGatewayMember member : gatewayMembers) {	
			try {
				this.startSession(member.memberIp);
			} catch (CPEException e) {
				// if i can't start a session on all members, just die
				throw new CPEException("Failed to start session on: " + member.memberIp + " due to: " + e.getLocalizedMessage(), e);
			}
		}
	}
	
	/**
	 * start a session on at least one member
	 * @return
	 * @throws CPEException
	 */
	public boolean startSession() throws CPEException {
		final List<String> errorMsgs = new ArrayList<String>(gatewayMembers.size());
		for (final VyattaAPIGatewayMember member : gatewayMembers) {	
			if (member.restAPIUrl != null) {
				// at least one session is active; nothing to do
				return false;
			}
		}
		
		for (final VyattaAPIGatewayMember member : gatewayMembers) {	
			try {
				this.startSession(member.memberIp);
				
				return true;
			} catch (CPEException e) {
				final String errorMsg = "Failed to start session on: " + member.memberIp + " due to: " + e.getLocalizedMessage();
				logger.warn(errorMsg, e);
				errorMsgs.add(errorMsg);
				continue;
			}
		}
		
		if (!errorMsgs.isEmpty()) {
			// was not able to create a session on either member
			throw new CPEException(String.format("Error creating session: %s", errorMsgs));
		}	
		
		return false;
	}
	
	/**
	 * start a session on a particular member
	 * @param memberIp
	 * @return
	 * @throws CPEException
	 */
	public boolean startSession(String memberIp) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace("startSession() called: " + memberIp);
		}
		
		boolean startedSession = false;
		
		// attempt to start a session on all of the gateway members. 
		final List<String> errorMsgs = new ArrayList<String>(gatewayMembers.size());
		for (final VyattaAPIGatewayMember member : gatewayMembers) {
			if (!member.memberIp.equals(memberIp)) {
				// only a particular member
				continue;
			}
			
			if (member.restAPIUrl != null) {
				// no need to start a new session if we already have one
				return false;
			}
			
			final String tmpRestAPIUrl = String.format("https://%s/rest/conf", member.memberIp);

			final HttpPost url = new HttpPost(tmpRestAPIUrl);
			final byte[] creds64bytes = Base64.encodeBase64(String.format("%s:%s", member.username, member.password).getBytes());
			final String b64creds = new String(creds64bytes);	
			url.addHeader("Authorization", String.format("Basic %s", b64creds));

			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Calling Vyatta REST API: %s %s", url.getMethod(), url.getURI().toString()));
			}

			try {
				final HttpResponse  resp = httpClient.execute(url);
				
				// drain the response buffer
				EntityUtils.consume(resp.getEntity());
				
				// Expected: HTTP 201 Created
				if (resp.getStatusLine().getStatusCode() != 201) {
					throw new CPEException(String.format("Failed to call Vyatta at %s, URL %s: Error %d", member.memberIp, url.getURI().toString(), resp.getStatusLine().getStatusCode()));
				}

				String location_headerValue = null;
				for (final Header header : resp.getAllHeaders()) {
					if (!header.getName().equals("Location")) {
						continue;
					}

					location_headerValue=header.getValue();
					break;
				}

				if (location_headerValue == null) {
					errorMsgs.add(String.format("Unable to find location header value for session in response from Vyatta member %s", member.memberIp));
					continue;
				}

				final String locArr[] = location_headerValue.split("/");
				//sessionId will be the 3rd entry in array as location header value would be 'rest/conf/09B16FE5F3090A75'
				final String sessionId = locArr[2];
				

				if (logger.isDebugEnabled()) {
					logger.debug(String.format("Vyatta session ID on %s: %s", member.memberIp, sessionId));
				}

				member.restAPIUrl = String.format("https://%s/rest/conf/%s", member.memberIp, sessionId);
				
				// i started a session on at least one of the members
				startedSession = true;
			} catch (ClientProtocolException e) {
				throw new CPEException(String.format("Unable to create Vyatta session on member %s: %s", member.memberIp, e.getLocalizedMessage()), e);
			} catch (IOException e) {
				throw new CPEException(String.format("Unable to create Vyatta session on member %s: %s", member.memberIp, e.getLocalizedMessage()), e);
			}
		}
	
		return startedSession;
	}

	/**
	 * commit changes on all active Sessions
	 * @throws CPEException
	 */
	public void commitChanges() throws CPEException {
		// commit changes on all vyatta members that have an open session
		if (logger.isTraceEnabled()) {
			logger.trace("commitChanges() call");
		}
		
		boolean hasSession = false;
		
		for (final VyattaAPIGatewayMember member : gatewayMembers) {
			if (member.restAPIUrl != null) {
				hasSession = true;
			}
		}
	
		if (!hasSession) {
			throw new CPEException(String.format("Cannot commit changes: no Vyatta session is active!"));
		}
		
		for (final VyattaAPIGatewayMember member : gatewayMembers) {
			this.commitChanges(member.memberIp);
		}
	}
	
	/**
	 * commit changes on a specific member
	 * @throws CPEException
	 */
	public void commitChanges(String memberIp) throws CPEException {
		// commit changes on all vyatta members that have an open session
		if (logger.isTraceEnabled()) {
			logger.trace("commitChanges() call");
		}
		
		boolean hasSession = false;
		
		for (final VyattaAPIGatewayMember member : gatewayMembers) {
			if (member.restAPIUrl != null) {
				hasSession = true;
			}
		}
	
		if (!hasSession) {
			throw new CPEException(String.format("Cannot commit changes: no Vyatta session is active!"));
		}
		
		for (final VyattaAPIGatewayMember member : gatewayMembers) {
			if (member.restAPIUrl == null) {
				continue;
			}
			if (!member.memberIp.equals(memberIp)) {
				continue;
			}
			
			final HttpPost url = new HttpPost(String.format("%s/commit", member.restAPIUrl));
			
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Calling Vyatta REST API: %s %s", url.getMethod(), url.getURI().toString()));
			}
			
			final byte[] creds64bytes = Base64.encodeBase64(String.format("%s:%s", member.username, member.password).getBytes());
			final String b64creds = new String(creds64bytes);	
			url.addHeader("Authorization", String.format("Basic %s", b64creds));

			try {
				final HttpResponse  resp = httpClient.execute(url);

				final StringBuilder sb = new StringBuilder();
				final BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
				String line;
				while ((line = reader.readLine()) != null) {
					sb.append(line).append(System.getProperty("line.separator"));
				}
				reader.close();		

				if (resp.getStatusLine().getStatusCode() != 200) {
					throw new CPEException(String.format("Unable to commit the changes: HTTP Response %d Response Body: %s", resp.getStatusLine().getStatusCode(), sb.toString()));
				}
				
				if (logger.isTraceEnabled()) {
					logger.trace(String.format("Success: Vyatta %s %s: Response body: %s", url.getMethod(), url.getURI().toString(), sb.toString()));
				}

			} catch (IOException e) {
				throw new CPEException("Unable to commit the changes");
			}
		}
	}
	
	/**
	 * save changes on all active open sessions
	 */
	public void saveChanges() throws CPEException{
		if (logger.isTraceEnabled()) {
			logger.debug("saveChanges() call");
		}
		
		boolean hasSession = false;
		
		for (final VyattaAPIGatewayMember member : gatewayMembers) {
			if (member.restAPIUrl != null) {
				hasSession = true;
			}
		}
	
		if (!hasSession) {
			throw new CPEException(String.format("Cannot commit changes: no Vyatta session is active!"));
		}
		
		for (final VyattaAPIGatewayMember member : gatewayMembers) {
			if (member.restAPIUrl == null) {
				// no active session
				continue;
			}
			final HttpPost url = new HttpPost(String.format("%s/save", member.restAPIUrl));
			
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Calling Vyatta REST API: %s %s", url.getMethod(), url.getURI().toString()));
			}		
			
			final byte[] creds64bytes = Base64.encodeBase64(String.format("%s:%s", member.username, member.password).getBytes());
			final String b64creds = new String(creds64bytes);	
			url.addHeader("Authorization", String.format("Basic %s", b64creds));

			try {
				final HttpResponse  resp = httpClient.execute(url);

				final StringBuilder sb = new StringBuilder();
				final BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
				String line;
				while ((line = reader.readLine()) != null) {
					sb.append(line).append(System.getProperty("line.separator"));
				}
				reader.close();
				if (resp.getStatusLine().getStatusCode() != 200) {
					throw new CPEException(String.format("Unable to save the changes on member %s: HTTP %d, Response body: %s", member.memberIp, resp.getStatusLine().getStatusCode(), sb.toString()));
				}
				
				if (logger.isTraceEnabled()) {
					logger.trace(String.format("Success: Vyatta %s %s: Response body: %s", url.getMethod(), url.getURI().toString(), sb.toString()));
				}
			} catch (IOException e) {
				throw new CPEException("Unable to save the changes on member " + member.memberIp, e);
			}
		}
	}
	
	/**
	 * destroy active sessions on specified member, if it's open
	 * @throws CPEException
	 */
	public void destroySession(String memberIp) throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace("destroySession() call");
		}
		
		// destroy all active vyatta sessions
		for (final VyattaAPIGatewayMember member : gatewayMembers) {
			if (member.restAPIUrl == null) {
				// no active session
				continue;
			}
			
			if (!member.memberIp.equals(memberIp)) {
				continue;
			}
			
			final HttpDelete url = new HttpDelete(member.restAPIUrl);
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Calling Vyatta REST API: %s %s", url.getMethod(), url.getURI().toString()));
			}
			
			final byte[] creds64bytes = Base64.encodeBase64(String.format("%s:%s", member.username, member.password).getBytes());
			final String b64creds = new String(creds64bytes);	
			url.addHeader("Authorization", String.format("Basic %s", b64creds));
	
			try {
				final HttpResponse  resp = httpClient.execute(url);

				final StringBuilder sb = new StringBuilder();
				final BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
				String line;
				while ((line = reader.readLine()) != null) {
					sb.append(line).append(System.getProperty("line.separator"));
				}
				reader.close();		

				if (resp.getStatusLine().getStatusCode() != 200) {
					throw new CPEException(String.format("Unable to destroy the session on member %s: HTTP %d, Response body: %s", member.memberIp, resp.getStatusLine().getStatusCode(), sb.toString()));
				}
				
				if (logger.isTraceEnabled()) {
					logger.trace(String.format("Success: Vyatta %s %s: Response body: %s", url.getMethod(), url.getURI().toString(), sb.toString()));
				}			

				member.restAPIUrl = null;
			} catch (IOException e) {
				throw new CPEException("Unable to destroy the session on member " + member.memberIp, e);
			}
		}
	}

	/**
	 * destroy all active sessions on all members
	 * @throws CPEException
	 */
	public void destroySession() throws CPEException {
		if (logger.isTraceEnabled()) {
			logger.trace("destroySession() call");
		}
		
		// destroy all active vyatta sessions
		for (final VyattaAPIGatewayMember member : gatewayMembers) {
			try {
				this.destroySession(member.memberIp);
			} catch (CPEException e) {
				logger.warn("Unable to destroy session on " + member.memberIp +": " + e.getLocalizedMessage(), e);
			}
		}
	}
	
	/**
	 * call a commands in operation mode (e.g. show interfaces) on one of the members
	 * @param gatewayIp
	 * @param operation
	 * @return
	 * @throws CPEException
	 */
	public String callOp(String memberIp, String operation) throws CPEException {
		// store the gateway IPs
		for (final VyattaAPIGatewayMember member : this.gatewayMembers) {
			if (!memberIp.equals(member.memberIp)) {
				continue;
			}
			
			final String operationPath = operation.replace(" ", "/");
			try {
				// for non-config mode operations, Vyatta uses the POST and then GET to run and get the output
				final HttpPost url = new HttpPost(String.format("https://%s/rest/op/%s", member.memberIp, operationPath));
				final byte[] creds64bytes = Base64.encodeBase64(String.format("%s:%s", member.username, member.password).getBytes());
				final String b64creds = new String(creds64bytes);				
				url.addHeader("Authorization", String.format("Basic %s", b64creds));		

				if (logger.isDebugEnabled()) {
					logger.debug(String.format("Calling Vyatta REST API: %s %s", url.getMethod(), url.getURI().toString()));
				}
				
				final HttpResponse resp = httpClient.execute(url);

				if (resp.getStatusLine().getStatusCode() != 201) {
					// looking for HTTP 201 CREATED
					throw new CPEException(String.format("Unable to call operation \"%s\" on gateway %s: Location not returned in header of response.", operation, member.memberIp));
				}

				String locationUrl = null;
				// the URL of the op is stored in the Location header
				for (final Header header : resp.getAllHeaders()) {
					if (!header.getName().equals("Location")) {
						continue;
					}

					locationUrl = header.getValue();
				}

				// eat the rest of the response
				EntityUtils.consume(resp.getEntity());

				if (locationUrl == null) {
					throw new CPEException(String.format("Unable to call operation \"%s\" on gateway %s: Location not returned in header of response.", operation, member.memberIp));
				}

				// now HTTP Get on the location URL
				final HttpGet get = new HttpGet(String.format("https://%s/%s", member.memberIp, locationUrl));
				get.addHeader("Authorization", String.format("Basic %s", b64creds));
				if (logger.isDebugEnabled()) {
					logger.debug(String.format("Calling Vyatta REST API: %s %s", get.getMethod(), get.getURI().toString()));
				}
	
				final HttpResponse output = httpClient.execute(get);
				final StringBuilder sb = new StringBuilder();

				final BufferedReader reader = new BufferedReader(new InputStreamReader(output.getEntity().getContent()));
				String line;
				while ((line = reader.readLine()) != null) {
					sb.append(line).append(System.getProperty("line.separator"));
				}
				reader.close();

				// eat the rest of the response
				EntityUtils.consume(resp.getEntity());

				if (output.getStatusLine().getStatusCode() != 200) {
					// looking for HTTP 200 OK
					throw new CPEException(String.format("Unable to call operation GET \"%s\" on gateway %s: HTTP response %d, body %s", operation, member.memberIp, resp.getStatusLine().getStatusCode(), sb.toString()));
				}
				
				if (logger.isTraceEnabled()) {
					logger.trace(String.format("Success: Vyatta %s %s: Response body: %s", get.getMethod(), get.getURI().toString(), sb.toString()));
				}
				

				return sb.toString();
			} catch (ClientProtocolException e) {
				final String errorMsg = String.format("Unable to call operation \"%s\" on gateway %s: %s ", operation, member.memberIp, e.getLocalizedMessage());
				throw new CPEException(errorMsg);
			} catch (IOException e) {
				final String errorMsg = String.format("Unable to call operation \"%s\" on gateway %s: %s ", operation, member.memberIp, e.getLocalizedMessage());
				throw new CPEException(errorMsg);
			}
		}

		// none of the IPs matched any members
		throw new CPEException(String.format("Unable to call operation \"%s\": no member with IP %s", operation, memberIp));
	}
	
	/**
	 * call a commands in operation mode (e.g. show interfaces) on one of the members
	 * @param gatewayIp
	 * @param operation
	 * @return
	 * @throws CPEException
	 */
	public String callOp(String operation) throws CPEException {
		final List<String> errorMsgs = new ArrayList<String>(gatewayMembers.size());
		for (final VyattaAPIGatewayMember member : this.gatewayMembers) {
			try {
				return this.callOp(member.memberIp, operation);
			} catch (CPEException e) {
				final String errorMsg = String.format("Unable to call operation \"%s\" on gateway %s: %s ", operation, member.memberIp, e.getLocalizedMessage());
				logger.warn(errorMsg);
				errorMsgs.add(errorMsg);			
				continue;
			}
		}

		throw new CPEException(String.format("Unable to call operation \"%s\": error messages=%s", operation, errorMsgs));
	}
	
	/**
	 * Return a JSON object with the configuration elements in the Vyatta.
	 * WARNING!!! this method is quite expensive.  It performs a recursive GET call on the Vyatta.  Try to keep the configuration path as close to the leaf
	 * as possible.  for example instead of "show interfaces" you could try "show interfaces ethernet <interface name>" to reduce the amount
	 * of recursion between all of the interfaces
	 * @param path
	 * @return
	 * @throws CPEException
	 */
	public JsonObject getConfigurationPath(String memberIp, String path) throws CPEException {
		VyattaAPIGatewayMember member = null;
		for (final VyattaAPIGatewayMember mem : this.gatewayMembers) {
			if (mem.memberIp.equals(memberIp)) {
				member = mem;
				break;
			}
		}
		
		if (member == null) {
			throw new CPEException("No gateway member matches IP address: " + memberIp);
		}
		
		// must have a valid session first
		if (member.restAPIUrl == null) {
			throw new CPEException("Cannot get configuration path \"" + path + "\", no configuration session is active on member IP: " + memberIp + "!");
		}
		
		final JsonObject returnObj = new JsonObject();
		
		// tokenize the command
		final String[] pathArr = path.split("\\s+");
		
		final List<String> pathList = new ArrayList<String>();
		for (final String pathElem : pathArr) {
			// URL encode each parameter -- this deals with strings like subnets with slashes in them already
			try {
				final String encodedCmdElem = URLEncoder.encode(pathElem, "UTF-8");
				pathList.add(encodedCmdElem);
			} catch (UnsupportedEncodingException e) {
				throw new CPEException(e.getLocalizedMessage(), e);
			}
		}
		
		final HttpGet url = new HttpGet(String.format("%s/%s", member.restAPIUrl, CollectionsUtil.toStringList(pathList, "/")));
		final byte[] creds64bytes = Base64.encodeBase64(String.format("%s:%s", member.username, member.password).getBytes());
		final String b64creds = new String(creds64bytes);					
		url.addHeader("Authorization", String.format("Basic %s", b64creds));

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Calling Vyatta REST API: %s %s", url.getMethod(), url.getURI().toString()));
		}

		try {
			final HttpResponse resp = httpClient.execute(url);
			final StringBuilder sb = new StringBuilder();

			final BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();

			if (resp.getStatusLine().getStatusCode() != 200) {
				throw new CPEException(String.format("Failed to call Vyatta %s %s: Error %d, Body: %s", url.getMethod(), url.getURI().toString(), resp.getStatusLine().getStatusCode(), sb.toString()));
			}
			
			final JsonParser parser = new JsonParser();
			final JsonElement jsonElem = parser.parse(sb.toString());
			
			if (logger.isTraceEnabled()) {
				logger.trace(String.format("Success: Vyatta %s %s: Response body: %s", url.getMethod(), url.getURI().toString(), new GsonBuilder().setPrettyPrinting().create().toJson(jsonElem)));
			}

			// get the "children" element
			final JsonObject jsonObj = jsonElem.getAsJsonObject();

			if (!jsonObj.has("children")) {
				return null;
			}
			
			final String nodeName = JsonUtil.getStringFromPath(jsonObj, "name");
			
			final String isMulti = JsonUtil.getStringFromPath(jsonObj, "multi");
			boolean isArray = false;
			if (isMulti != null && isMulti.equals("true")) {
				// this node is an array
				returnObj.add(nodeName, new JsonArray());
				isArray = true;
			}
			
			final String isEnd = JsonUtil.getStringFromPath(jsonObj, "end");
			boolean stopRecurse = false;
			if (isEnd != null && isEnd.equals("true")) {
				stopRecurse = true;
			}
		
			final JsonArray arr = JsonUtil.getArrayFromPath(jsonObj, "children");
			for (final JsonElement childElem : arr) {
				final String childStatus = JsonUtil.getStringFromPath(childElem, "state");
				if (childStatus == null || !childStatus.equals("active")) {
					continue;
				}
				
				final String childName = JsonUtil.getStringFromPath(childElem, "name");
				
				if (stopRecurse) {
					// end the recursion, add the value to the obj
					if (isArray) {
						returnObj.get(nodeName).getAsJsonArray().add(new JsonPrimitive(childName));				
					} else {
						returnObj.add(nodeName, new JsonPrimitive(childName));
					}
					continue;
				}
				
				// create a child object, recursively
				final JsonObject childObject = this.getConfigurationPath(memberIp, String.format("%s %s", path, childName));

				if (childObject == null) {
					continue;
				}

				if (isArray) {
					returnObj.get(nodeName).getAsJsonArray().add(childObject);
				} else {
					returnObj.add(nodeName, childObject);
				}
			}
			
			return returnObj;
		} catch (ClientProtocolException e) {
			throw new CPEException(e);
		} catch (IOException e) {
			throw new CPEException(e);
		}
	}
	
	/**
	 * Get the configuration path on one of the members
	 * -- only call this if both members will return the same configurations (i.e. config-sync'ed trees), or if you have an
	 *    open session only on one of the members.  otherwise call getConfigurationPath with the specific member IP
	 *    to target.
	 * @param path
	 * @return
	 * @throws CPEException
	 */
	public JsonObject getConfigurationPath(String path) throws CPEException {
		final List<String> errorMsgs = new ArrayList<String>(gatewayMembers.size());
		for (final VyattaAPIGatewayMember member : this.gatewayMembers) {
			try {
				if (member.restAPIUrl == null) {
					continue;
				}
				
				// get the configuration path of the first member to return successfully
				return this.getConfigurationPath(member.memberIp, path);
			} catch (CPEException e) {
				final String errorMsg = String.format("Unable to get configuration path \"%s\" on gateway %s: %s ", path, member.memberIp, e.getLocalizedMessage());
				logger.warn(errorMsg);
				errorMsgs.add(errorMsg);			
				continue;
			}
		}

		throw new CPEException(String.format("Unable to get configuration path \"%s\": error messages=%s", path, errorMsgs));
	}
	
	/**
	 * call the command on a specific gateway member
	 * @param memberIp
	 * @param command
	 * @return
	 * @throws CPEException
	 */
	public String callConfigureCommand(String memberIp, String command) throws CPEException {
		VyattaAPIGatewayMember member = null;
		for (final VyattaAPIGatewayMember mem : this.gatewayMembers) {
			if (mem.memberIp.equals(memberIp)) {
				member = mem;
				break;
			}
		}
		
		if (member == null) {
			throw new CPEException("No gateway member matches IP address: " + memberIp);
		}
		
		// must have a valid session first
		if (member.restAPIUrl == null) {
			throw new CPEException("Cannot execute command \"" + command + "\" on member " + memberIp + ", no configuration session is active!");
		}

		// tokenize the command
		final String[] commandArr = CollectionsUtil.tokenizeExcludingQuotes(command);

		final List<String> commandList = new ArrayList<String>();
		for (final String commandElem : commandArr) {
			// URL encode each parameter -- this deals with strings like subnets with slashes in them already
			try {
				String encodedCmdElem = URLEncoder.encode(commandElem, "UTF-8");
				// UTF-8 encoding converts spaces in +, convert + back into %20 since Vyatta expects those
				encodedCmdElem = encodedCmdElem.replace("+", "%20");
				commandList.add(encodedCmdElem);
			} catch (UnsupportedEncodingException e) {
				throw new CPEException(e.getLocalizedMessage(), e);
			}
		}

		final HttpPut url = new HttpPut(String.format("%s/%s", member.restAPIUrl, CollectionsUtil.toStringList(commandList, "/")));
		final byte[] creds64bytes = Base64.encodeBase64(String.format("%s:%s", member.username, member.password).getBytes());
		final String b64creds = new String(creds64bytes);						
		url.addHeader("Authorization", String.format("Basic %s", b64creds));

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Calling Vyatta REST API: %s %s", url.getMethod(), url.getURI().toString()));
		}

		try {
			final HttpResponse resp = httpClient.execute(url);
			final StringBuilder sb = new StringBuilder();

			final BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append(System.getProperty("line.separator"));
			}
			reader.close();

			if (resp.getStatusLine().getStatusCode() != 200) {
				throw new CPEException(String.format("Failed to call Vyatta %s %s: Error %d, Body: %s", url.getMethod(), url.getURI().toString(), resp.getStatusLine().getStatusCode(), sb.toString()));
			}

			if (logger.isTraceEnabled()) {
				logger.trace(String.format("Success: Vyatta %s %s: Response body: %s", url.getMethod(), url.getURI().toString(), sb.toString()));
			}

			return sb.toString();
		} catch (ClientProtocolException e) {
			throw new CPEException(e);
		} catch (IOException e) {
			throw new CPEException(e);
		}
	}
	
	/**
	 * call a configuration command on all gateway members with open session.
	 * -- only call this if you intend to run the same command on both members, or if only one of the members has an open seesion.  
	 *    for config-sync'ed trees, it's only necessary to call one of the members and the configuration will be synced during commit.
	 * @param command
	 * @return
	 * @throws CPEException if one of the members has an error while performing the configure command
	 */
	public Map<String, String> callConfigureCommand(String command) throws CPEException {
		final Map<String, String> responses = new HashMap<String, String>();
		
		for (final VyattaAPIGatewayMember member : this.gatewayMembers) {
			if (member.restAPIUrl == null) {
				// must have a valid session first
				continue;
			}
			
			try {
				final String response = this.callConfigureCommand(member.memberIp, command);
				responses.put(member.memberIp, response);
			} catch (CPEException e) {
				// the command must be successful on all members that have open session
				throw new CPEException(String.format("Unable to execute command \"%s\" on gateway %s: %s ", command, member.memberIp, e.getLocalizedMessage(), e));
			}
		}
	
		return responses;

	}
	
	/**
	 * get the private interface that has the IP address.  this is called on one of the members.
	 * 
	 * @return
	 */
	public String getPrivateInterfaceName() throws CPEException {
		final String interfaceOutput = this.callOp("show interfaces");
		final String[] lines = interfaceOutput.split(System.getProperty("line.separator"));

		for (final VyattaAPIGatewayMember member : this.gatewayMembers) {
			for (final String line : lines) {
				if (!line.contains(member.memberIp)) {
					continue;
				}

				final String[] interfaceArr = line.split("\\s+");

				// the first item in the array is the interface name
				return interfaceArr[0];
			}
		}
		
		return null;
	}
	
	/**
	 * get the public facing interface name, if there is one.  this is called on one of the members
	 * 
	 * @return
	 */
	public String getPublicInterfaceName() throws CPEException {
		for (final VyattaAPIGatewayMember member : this.gatewayMembers) {
			final String interfaceOutput = this.callOp("show interfaces");
			final String[] lines = interfaceOutput.split(System.getProperty("line.separator"));
			String privateIntf = null;
			for (final String line : lines) {
				final String[] interfaceArr = line.split("\\s+");
				if (interfaceArr.length < 3) {
					// not a valid interface line
					continue;
				}
				
				if (line.contains(member.memberIp)) {
					// skip the private interface
					privateIntf = interfaceArr[0];
					break;
				}
			}
			
			if (privateIntf == null) {
				// must have a private interface?
				continue;
			}

			for (final String line : lines) {
				final String[] interfaceArr = line.split("\\s+");
				if (interfaceArr.length < 3 || interfaceArr.length > 4) {
					// not a valid interface line
					continue;
				}
				
				if (line.contains(privateIntf)) {
					// skip the private interface and all VIFs on the private interface
					continue;
				}

				// the first item in the array is the interface name
				if (interfaceArr[0].equals("lo")) {
					// skip loopback interface
					continue;
				}
				
				// check the link state; if it's down, continue
				final String[] linkState = interfaceArr[2].split("/");
				if (linkState.length != 2 || !linkState[1].equals("u")) {
					// link is down; not a valid interface
					continue;
				}
				
				final String ipAddr = interfaceArr[1];
				if (ipAddr.equals('-')) {
					// not interested in interfaces that are not configured
					continue;
				}
				
				if (interfaceArr[0].contains(".")) {
					// if the interface contains a period, it's a VIF
					continue;
				}
				
				return interfaceArr[0];
			}
		}
		
		return null;
	}
	
	public Set<String> getMemberIPs() { 
		final Set<String> memberIPs = new HashSet<String>();
		for (final VyattaAPIGatewayMember member : this.gatewayMembers) {
			memberIPs.add(member.memberIp);
		}
		
		return memberIPs;
	}
	

	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("usage: VyattaAPIGateway <IP> <username> <password>");
			System.exit(0);
		}
		try {
			final VyattaAPIGateway realGw = new VyattaAPIGateway(args[0], args[1], args[2]);
			realGw.testCredentials();
			
			
			//realGw.addVifGateway(2290, "50.22.248.241/28", VlanNetworkSpace.PUBLIC);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private static class VyattaAPIGatewayMember {
		protected String memberIp;
		protected String username;
		protected String password;
		protected String sslCert;
		protected String restAPIUrl;
	}
	
	private void setHTTPClientParrams() {
		this.httpClient.setParams(new BasicHttpParams());
		
		final String soTimeoutStr = EngineProperties.getInstance().getProperty(EngineProperties.FIREWALL_VYATTA_HTTPCLIENT_SOCKET_TIMEOUT, EngineProperties.DEFAULT_FIREWALL_VYATTA_HTTPCLIENT_SOCKET_TIMEOUT_SECS);
		int soTimeout;
		try  {
			soTimeout = Integer.parseInt(soTimeoutStr);
		} catch (NumberFormatException e) {
			logger.warn(String.format("Failed to parse %s. Using default value of %s.", EngineProperties.FIREWALL_VYATTA_HTTPCLIENT_SOCKET_TIMEOUT, EngineProperties.DEFAULT_FIREWALL_VYATTA_HTTPCLIENT_SOCKET_TIMEOUT_SECS));
			soTimeout = Integer.parseInt(EngineProperties.DEFAULT_FIREWALL_VYATTA_HTTPCLIENT_SOCKET_TIMEOUT_SECS);
		}

		HttpConnectionParams.setSoTimeout(this.httpClient.getParams(), (soTimeout*1000));
		
		final String connectTimeoutStr = EngineProperties.getInstance().getProperty(EngineProperties.FIREWALL_VYATTA_HTTPCLIENT_CONNECTION_TIMEOUT, EngineProperties.DEFAULT_FIREWALL_VYATTA_HTTPCLIENT_CONNECTION_TIMEOUT_SECS);
		int connectTimeout;
		try  {
			connectTimeout = Integer.parseInt(connectTimeoutStr);
		} catch (NumberFormatException e) {
			logger.warn(String.format("Failed to parse %s. Using default value of %s.", EngineProperties.FIREWALL_VYATTA_HTTPCLIENT_CONNECTION_TIMEOUT, EngineProperties.DEFAULT_FIREWALL_VYATTA_HTTPCLIENT_CONNECTION_TIMEOUT_SECS));
			connectTimeout = Integer.parseInt(EngineProperties.DEFAULT_FIREWALL_VYATTA_HTTPCLIENT_CONNECTION_TIMEOUT_SECS);
		}

		HttpConnectionParams.setConnectionTimeout(this.httpClient.getParams(), (connectTimeout*1000));
	}
}
