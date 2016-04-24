/*
 *  
 * Licensed Materials - Property of IBM 
 *  
 * Restricted Materials of IBM 
 *  
 * (C) COPYRIGHT International Business Machines Corp. 2014 
 * All Rights Reserved 
 *  
 * US Government Users Restricted Rights - Use, duplication or 
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp. 
 *
 */

package com.ibm.scas.analytics.utils;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.JsonMappingException;
/*
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonNode;
*/
import org.codehaus.jackson.map.ObjectMapper;

import com.ibm.scas.analytics.backend.NotAuthorizedException;
import com.ibm.scas.analytics.beans.Storage;
import com.ibm.scas.analytics.beans.StorageAuthenticationWrapper;

public class SLAuthenticator {
	private static Logger logger = Logger.getLogger(SLAuthenticator.class);

	String username;
	String apikey;
	String authUrl;
	boolean usePrivate;
	String authToken;
	String storageUrl; 

	public SLAuthenticator(String username, String apikey, String authUrl, boolean usePrivate) {
		this.username = username;
		this.apikey = apikey;
		this.authUrl = authUrl;
		this.usePrivate = usePrivate;
 		this.authToken = "";
 		this.storageUrl = "";
	}

	public String getAuthToken() {
		return authToken; 
	}

	public String getStorageUrl() {
		return storageUrl; 
	}

	protected void parseResponseBody(String body) {
		// parse the json response to get the storage url
		ObjectMapper jsonMapper = new ObjectMapper();
		jsonMapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    	try {
			final Storage storage = jsonMapper.readValue(body,
        		StorageAuthenticationWrapper.class).getStorage();
			if (storage != null) {
        		storageUrl = usePrivate ? storage.getPrivateURL() : storage.getPublicURL();
        		if (storageUrl.equals("")) {
        			throw new RuntimeException("The storage url (endpoint) in the authentication " +
        				"response from SoftLayer was not valid - it was the empty string");
        		}
        	}
    	} catch (JsonGenerationException e) {
    		System.err.println("Error processing this auth response body: " + body);
      		throw new RuntimeException("Json generation error", e);
    	} catch (JsonMappingException e) {
   			System.err.println("Error processing this auth response body: " + body);
      		throw new RuntimeException("Json mapping error", e);
    	} catch (IOException e) {
	   		System.err.println("Error processing this auth response body: " + body);
      		throw new RuntimeException("IO Exception", e);
    	}
	}
 
	public void authenticate() throws NotAuthorizedException{
	  	try {
 
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet getRequest = new HttpGet( authUrl );
			getRequest.addHeader("X-Auth-User", username );
			getRequest.addHeader("X-Auth-Key", apikey );
			getRequest.addHeader("Content-type", "application/json");
			
			logger.debug("Authenticating - Username : " + username + ", apiKey: " + apikey);
			HttpResponse response = httpClient.execute(getRequest);
			
			logger.debug("Authentication Response Code : " + response.getStatusLine().getStatusCode());
 
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
				throw new NotAuthorizedException("Failed authentication to SoftLayer with HTTP error code : "
			   	+ response.getStatusLine().getStatusCode());
			}else if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new RuntimeException("Failed authentication to SoftLayer with HTTP error code : "
			   	+ response.getStatusLine().getStatusCode());
			}

			// get the auth token from the headers
			Header token = response.getFirstHeader("X-Auth-Token");
			logger.debug("authToken : " + token);
        	if (token != null) {
        		authToken = token.getValue();
        		if (authToken.equals("")) {
        			throw new NotAuthorizedException("X-Auth-Token header in the authentication " +
        				"response from SoftLayer was not valid - it was the empty string");
        		}
        	}
        	else {
        		throw new NotAuthorizedException("Authentication response from SoftLayer did not " + 
        			"contain an X-Auth-Token header.");
	    	}

        	parseResponseBody(EntityUtils.toString(response.getEntity()));

        	httpClient.getConnectionManager().shutdown();
 
	  	} catch (ClientProtocolException e) {
 
			e.printStackTrace();
 
	  	} catch (IOException e) {
 
			e.printStackTrace();
	  	}
    	return;
  	}
}