package com.ibm.scas.analytics.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.scas.analytics.EngineProperties;
import com.ibm.scas.analytics.persistence.beans.SoftLayerAccount;
import com.ibm.scas.analytics.utils.CPEException;
import com.ibm.scas.analytics.utils.SoftLayerObjectFilter;

public class SoftLayerAPIGateway {
	private static Logger logger = Logger.getLogger(SoftLayerAPIGateway.class);
	
	public static final String DEFAULT_REST_API_URL = "https://api.service.softlayer.com/rest/v3.1";
	public static final String PUBLIC_REST_API_URL = "https://api.softlayer.com/rest/v3.1";
	
	private final String url;
	
	private final DefaultHttpClient httpClient; 

	public SoftLayerAPIGateway(String username, String apiKey) {
		this(DEFAULT_REST_API_URL, username, apiKey);
	}
	
	public SoftLayerAPIGateway(com.ibm.scas.analytics.beans.SoftLayerAccount acctRec) {
		this(acctRec.getUrl(), acctRec.getUsername(), acctRec.getApiKey());
	}
	
	public SoftLayerAPIGateway(SoftLayerAccount acctRec) {
		this(acctRec.getUrl(), acctRec.getUsername(), acctRec.getApiKey());
	}
	
	public SoftLayerAPIGateway(String url, String username, String apiKey) {
		this.httpClient = new DefaultHttpClient();
		
		this.setHTTPClientParrams();
		
		if (url != null) {
			this.url = url;
		} else {
			this.url = DEFAULT_REST_API_URL;
		}
		
		this.httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, apiKey));
	}
	
	public void testCredentials() throws CPEException{
		final HttpGet testURL = new HttpGet(String.format("%s/SoftLayer_Account/getCurrentUser", this.url));
		
		final StringBuilder sb = new StringBuilder();
		try {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("testCredentials(): Calling SoftLayer REST API: %s", testURL.toString()));
			}
			
			final HttpResponse resp = httpClient.execute(testURL);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
			String line;
			while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();		
			
			if (resp.getStatusLine().getStatusCode() != 200) {
				throw new CPEException(String.format("Unable to validate SoftLayer account credentials: %s", sb.toString()));
			}
			
		} catch (SocketTimeoutException e) {
			logger.error(e);
			
			throw new CPEException("Connction Timed out for URL : " + testURL, e);
		} catch (ClientProtocolException e) {
			logger.error(e);
			
			throw new CPEException("Unable to validate SoftLayer account credentials", e);
		} catch (IOException e) {
			logger.error(e);
			
			// try again next iteration
			throw new CPEException("Unable to validate SoftLayer account credentials", e);
		}	
		
	}
	
	/**
	 * Call a SoftLayer method using HTTP GET.  See <a href="http://sldn.softlayer.com/reference/softlayerapi">api reference</a>
	 * for details.
	 * @param serviceType Type of Service
	 * @param serviceName Name of Service
	 * @param softLayerId 
	 * @param objectMask list of object mask (see <a href="http://sldn.softlayer.com/article/Object-Masks">object mask</a>)
	 * @return
	 */
	public JsonObject getObjectById(String serviceType, String softLayerId, String serviceName, SoftLayerObjectFilter objectFilter,  String ... objectMask) throws CPEException {
		final StringBuilder urlBuilder = new StringBuilder();
		
		urlBuilder.append(this.url);
		urlBuilder.append("/").append(serviceType);
		urlBuilder.append("/").append(softLayerId);
		
		if (serviceName != null && serviceName.length() > 0) {
			urlBuilder.append("/").append(serviceName);
		}
		
		if (objectFilter != null || (objectMask != null && objectMask.length > 0)) {
			urlBuilder.append("?");
		}
		
		if (objectFilter != null && !objectFilter.isEmpty()) {
			if (logger.isTraceEnabled()) {
				logger.trace(String.format("Object Filter: \n%s", objectFilter.toString()));
			}
			urlBuilder.append("objectFilter=");
			try {
				urlBuilder.append(URLEncoder.encode(new Gson().toJson(objectFilter.toJsonObject()), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new CPEException(e);
			}
			if (objectMask != null && objectMask.length > 0) {
				urlBuilder.append("&");
			}
		}
		
		if (objectMask != null && objectMask.length > 0) {
			int count = 0;
			urlBuilder.append("objectMask=");
			for (final String objMask : objectMask) {
				urlBuilder.append(objMask);
				count ++;
				if (count != objectMask.length) {
					urlBuilder.append(";");
				}
			}
		}
		
		final StringBuilder sb = new StringBuilder();
		try {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("callGetObjectById(): Calling SoftLayer REST API: %s", urlBuilder.toString()));
			}
			final HttpGet getMethod = new HttpGet(urlBuilder.toString());
			final HttpResponse resp = httpClient.execute(getMethod);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
			String line;
			while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
		} catch (SocketTimeoutException e) {
			logger.error(e);
			throw new CPEException("Connction Timed out", e);
		} catch (ClientProtocolException e) {
			throw new CPEException(e);
		} catch (IOException e) {
			throw new CPEException(e);
		}
		
	
		final JsonParser p = new JsonParser();
        final JsonElement jsonElem = p.parse(sb.toString());
        if (!jsonElem.isJsonObject()) {
        	throw new CPEException(String.format("Cannot parse response from SoftLayer: object type %s, object id %s: %s", serviceType, softLayerId, ReflectionToStringBuilder.toString(sb.toString())));
        }
        
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("callGetMethod(): Response:\n%s", new GsonBuilder().setPrettyPrinting().create().toJson(jsonElem)));
		}
        
        return jsonElem.getAsJsonObject();
	}
	
	/**
	 * Call a SoftLayer method using HTTP GET.  See <a href="http://sldn.softlayer.com/reference/softlayerapi">api reference</a>
	 * for details.
	 * @param serviceType Type of Service
	 * @param serviceName Name of Service
	 * @param objectFilter ObjectFilter (see <a href="http://sldn.softlayer.com/article/Object-Filters">object filters</a>)
	 * @param objectMask list of object mask (see <a href="http://sldn.softlayer.com/article/Object-Masks">object mask</a>)
	 * @return
	 */
	public JsonElement callGetMethod(String serviceType, String serviceName, SoftLayerObjectFilter objectFilter, String ... objectMask) throws CPEException {
		final StringBuilder urlBuilder = new StringBuilder();
		
		urlBuilder.append(this.url);
		urlBuilder.append("/").append(serviceType);
		urlBuilder.append("/").append(serviceName);
		
		if (objectFilter != null || (objectMask != null && objectMask.length > 0)) {
			urlBuilder.append("?");
		}
		
		if (objectFilter != null && !objectFilter.isEmpty()) {
			if (logger.isTraceEnabled()) {
				logger.trace(String.format("Object Filter: \n%s", objectFilter.toString()));
			}
			urlBuilder.append("objectFilter=");
			try {
				urlBuilder.append(URLEncoder.encode(new Gson().toJson(objectFilter.toJsonObject()), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new CPEException(e);
			}
			if (objectMask != null && objectMask.length > 0) {
				urlBuilder.append("&");
			}
		}
		
		if (objectMask != null && objectMask.length > 0) {
			int count = 0;
			urlBuilder.append("objectMask=");
			for (final String objMask : objectMask) {
				urlBuilder.append(objMask);
				count ++;
				if (count != objectMask.length) {
					urlBuilder.append(";");
				}
			}
		}
		
		final StringBuilder sb = new StringBuilder();
		try {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("callGetMethod(): Calling SoftLayer REST API: %s", urlBuilder.toString()));
			}
			final HttpGet getMethod = new HttpGet(urlBuilder.toString());
			final HttpResponse resp = httpClient.execute(getMethod);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
			String line;
			while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
		} catch (SocketTimeoutException e) {
			logger.error(e);
			throw new CPEException("Connction Timed out ", e);
		} catch (ClientProtocolException e) {
			throw new CPEException(e);
		} catch (IOException e) {
			throw new CPEException(e);
		}
		
	
		final JsonParser p = new JsonParser();
        final JsonElement jsonResponse = p.parse(sb.toString());
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("callGetMethod(): Response:\n%s", new GsonBuilder().setPrettyPrinting().create().toJson(jsonResponse)));
		}
		
		return jsonResponse;
	}
	
	public JsonElement callGetMethod(String urlPath) throws CPEException {
		final StringBuilder sb = new StringBuilder();
		try {
			final HttpGet getMethod = new HttpGet(String.format("%s/%s", this.url, urlPath));
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("callGetMethod(): Calling SoftLayer REST API: %s/%s", this.url, urlPath));
			}
			final HttpResponse resp = httpClient.execute(getMethod);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
			String line;
			while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
		} catch (SocketTimeoutException e) {
			logger.error(e);
			
			throw new CPEException("Connction Timed out for URL", e);
		} catch (ClientProtocolException e) {
			throw new CPEException(e);
		} catch (IOException e) {
			throw new CPEException(e);
		}
		
		logger.trace(sb.toString());
		final JsonParser p = new JsonParser();
        final JsonElement jsonResponse = p.parse(sb.toString());
        
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("callGetMethod(): Response:\n%s", new GsonBuilder().setPrettyPrinting().create().toJson(jsonResponse)));
		}
		
		return jsonResponse;
	
	}
	
	public void callPutMethod(String urlPath, JsonObject parameters) throws CPEException{
		final StringBuilder urlBuilder = new StringBuilder();
		
		urlBuilder.append(this.url);
		urlBuilder.append("/").append(urlPath);
		
		final StringBuilder sb = new StringBuilder();

			if (logger.isDebugEnabled()) {
				logger.debug(String.format("callPutMethod(): Calling SoftLayer REST API: %s, request body: %s", urlBuilder.toString(), new GsonBuilder().setPrettyPrinting().create().toJson(parameters)));
			}
			
			final HttpPut putMethod = new HttpPut(urlBuilder.toString());
			try {
				putMethod.addHeader("Accept", "application/json");
				StringEntity input = new StringEntity(parameters.toString());
				putMethod.setEntity(input);
				final HttpResponse resp = httpClient.execute(putMethod);

				final BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
				String line;
				while ((line = reader.readLine()) != null) {
                	sb.append(line);
				}
            
				reader.close();
				
				if (resp.getStatusLine().getStatusCode() != 200) {
	                throw new CPEException(String.format("PUT %s failed. HTTP error code : %d, Response body: %s", putMethod.getURI().toString(), resp.getStatusLine().getStatusCode(), sb.toString()));
	            }
				
			} catch (SocketTimeoutException e) {
				logger.error(e);
				
				throw new CPEException("Connction Timed out", e);
			} catch (IOException e) {
				throw new CPEException(e);
			}

	
		final JsonParser p = new JsonParser();
        final JsonElement jsonResponse = p.parse(sb.toString());
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("callPutMethod(): Response:\n%s", new GsonBuilder().setPrettyPrinting().create().toJson(jsonResponse)));
		}
		
		
	}
	
	public void callPostMethod(String urlPath, JsonObject parameters) throws CPEException{
		final StringBuilder urlBuilder = new StringBuilder();
		
		urlBuilder.append(this.url);
		urlBuilder.append("/").append(urlPath);
		
		final StringBuilder sb = new StringBuilder();

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("callPostMethod(): Calling SoftLayer REST API: %s, request body: %s", urlBuilder.toString(), new GsonBuilder().setPrettyPrinting().create().toJson(parameters)));
		}
		
		final HttpPost postMethod = new HttpPost(urlBuilder.toString());
		try {
			postMethod.addHeader("Accept", "application/json");
			postMethod.addHeader("ContentType", "application/json");
			StringEntity input = new StringEntity(parameters.toString());
			postMethod.setEntity(input);
			final HttpResponse resp = httpClient.execute(postMethod);

			final BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}

			reader.close();

			if (resp.getStatusLine().getStatusCode() != 200) {
				throw new CPEException(String.format("POST %s failed. HTTP error code : %d, Response body: %s", postMethod.getURI().toString(), resp.getStatusLine().getStatusCode(), sb.toString()));
			}
		} catch (SocketTimeoutException e) {
			logger.error(e);
			
			throw new CPEException("Connction Timed out", e);
		} catch (IOException e) {
			throw new CPEException(e);
		}


		final JsonParser p = new JsonParser();
		final JsonElement jsonResponse = p.parse(sb.toString());
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("callPutMethod(): Response:\n%s", new GsonBuilder().setPrettyPrinting().create().toJson(jsonResponse)));
		}
	}
	
	public void deleteObjectById(String serviceType, String softLayerId) throws CPEException {
		final StringBuilder urlBuilder = new StringBuilder();
		
		urlBuilder.append(this.url);
		urlBuilder.append("/").append(serviceType);
		urlBuilder.append("/").append(softLayerId);
		
		final StringBuilder sb = new StringBuilder();
		try {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Calling SoftLayer REST API: DELETE %s", urlBuilder.toString()));
			}
			
			final HttpDelete deleteMethod = new HttpDelete(urlBuilder.toString());
			final HttpResponse resp = httpClient.execute(deleteMethod);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
			String line;
			while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            
            if (resp.getStatusLine().getStatusCode() != 200) {
            	throw new CPEException(String.format("Error calling SoftLayer DELETE on object %s ID %s: response body: %s", serviceType, softLayerId, sb.toString()));
            }
            
            if (logger.isTraceEnabled()) {
            	logger.trace(String.format("DELETE %s Response:\n%s", urlBuilder.toString(), new GsonBuilder().setPrettyPrinting().create().toJson(sb.toString())));
            }           
		} catch (SocketTimeoutException e) {
			logger.error(e);
			
			throw new CPEException("Connction Timed out", e);
		} catch (ClientProtocolException e) {
			throw new CPEException(e);
		} catch (IOException e) {
			throw new CPEException(e);
		}
	}
	
	private void setHTTPClientParrams() {
		this.httpClient.setParams(new BasicHttpParams());
		
		final String soTimeoutStr = EngineProperties.getInstance().getProperty(EngineProperties.SOFTLAYER_HTTPCLIENT_SOCKET_TIMEOUT, EngineProperties.DEFAULT_SOFTLAYER_HTTPCLIENT_SOCKET_TIMEOUT_SECS);
		int soTimeout;
		try  {
			soTimeout = Integer.parseInt(soTimeoutStr);
		} catch (NumberFormatException e) {
			logger.warn(String.format("Failed to parse %s. Using default value of %s.", EngineProperties.SOFTLAYER_HTTPCLIENT_SOCKET_TIMEOUT, EngineProperties.DEFAULT_SOFTLAYER_HTTPCLIENT_SOCKET_TIMEOUT_SECS));
			soTimeout = Integer.parseInt(EngineProperties.DEFAULT_SOFTLAYER_HTTPCLIENT_SOCKET_TIMEOUT_SECS);
		}

		HttpConnectionParams.setSoTimeout(this.httpClient.getParams(), (soTimeout*1000));
		
		final String connectTimeoutStr = EngineProperties.getInstance().getProperty(EngineProperties.SOFTLAYER_HTTPCLIENT_CONNECTION_TIMEOUT, EngineProperties.DEFAULT_SOFTLAYER_HTTPCLIENT_CONNECTION_TIMEOUT_SECS);
		int connectTimeout;
		try  {
			connectTimeout = Integer.parseInt(connectTimeoutStr);
		} catch (NumberFormatException e) {
			logger.warn(String.format("Failed to parse %s. Using default value of %s.", EngineProperties.SOFTLAYER_HTTPCLIENT_CONNECTION_TIMEOUT, EngineProperties.DEFAULT_SOFTLAYER_HTTPCLIENT_CONNECTION_TIMEOUT_SECS));
			connectTimeout = Integer.parseInt(EngineProperties.DEFAULT_SOFTLAYER_HTTPCLIENT_CONNECTION_TIMEOUT_SECS);
		}

		HttpConnectionParams.setConnectionTimeout(this.httpClient.getParams(), (connectTimeout*1000));
	}
}
