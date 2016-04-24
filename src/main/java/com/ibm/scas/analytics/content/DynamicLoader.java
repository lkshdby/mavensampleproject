package com.ibm.scas.analytics.content;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.net.URLCodec;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ibm.scas.analytics.persistence.PersistenceException;
import com.ibm.scas.analytics.persistence.PersistenceService;
import com.ibm.scas.analytics.persistence.beans.ContentMap;
import com.ibm.scas.analytics.persistence.beans.Offering;
import com.ibm.scas.analytics.utils.CPEException;

@Singleton
public class DynamicLoader {
	private static final Logger logger = Logger.getLogger(DynamicLoader.class);

	private static Map<String, String> contentUrlMap = new HashMap<String, String>();
	@Inject private PersistenceService persistence;

	private final static String BUILTIN_CONTENT_PATH = "/plugins";

	public String getContentUrl(String id) throws CPEException{
		synchronized (contentUrlMap) {
			String url = contentUrlMap.get(id);
			if (url == null) {
				ContentMap contentMap;
				try {
					Offering offering = persistence.getObjectById(Offering.class, id);
					if(offering == null)
					{
						logger.error(String.format("Offering with id '%s' does not exist.", id));
						throw new CPEException(String.format("Offering with id '%s' does not exist.", id));
					}
					contentMap = persistence.getObjectById(ContentMap.class, offering.getUrlPath().getId());

					if (contentMap == null) {
						logger.warn(String.format("No content map available in database for id: %s", id));
						return "";
					}
				} catch (PersistenceException e) {
					logger.warn(String.format("Error retrieving content map for id %s: %s", id, e), e);
					return "";
				}			
				
				contentUrlMap.put(id, contentMap.getUrl());
				
				url = contentMap.getUrl();
			}
			return url;
		}
	}

	public void invalidate() {
		synchronized (contentUrlMap) {
			contentUrlMap.clear();
			logger.info("Content URL map cleared.");
		}
	}

	public void render(HttpServletRequest req, HttpServletResponse resp, String offeringId, String path) throws CPEException {
		String targetUrl = getContentUrl(offeringId);
		if (targetUrl.length() == 0) {
			// redirect to built-in content
			String context = req.getContextPath();
			String redirectUrl = context + BUILTIN_CONTENT_PATH + "/" + offeringId + "/" + path;
			resp.setStatus(HttpServletResponse.SC_FOUND);
			resp.setHeader("Location", redirectUrl);
		} else {

			try {
				URLCodec urlCodec = new URLCodec();
				StringBuilder sb = new StringBuilder();
				sb.append(targetUrl);
				String[] segments = path.split("/");
				for (String s : segments) {
					sb.append('/').append(urlCodec.encode(s).replaceAll("\\+", "%20"));
//					sb.append('/').append();
				}
				String redirectUrl = sb.toString();
				if (logger.isDebugEnabled()) {
					logger.debug("Loading from remote URL: " + redirectUrl);
				}
				HttpGet get = new HttpGet(redirectUrl);
				
				String acceptHeader = req.getHeader("Accept");
				if (acceptHeader != null) {
					get.setHeader("Accept", acceptHeader);
				}

				HttpClient httpClient = new DefaultHttpClient();
				HttpResponse response = httpClient.execute(get);

				int status = response.getStatusLine().getStatusCode();
				resp.setStatus(status);

				if (redirectUrl.endsWith(".html")) {
					// TODO: this is a temporary fix for SL object storage
					resp.setHeader("Content-Type", "text/html");
				} else if (redirectUrl.endsWith(".css")) {
					resp.setHeader("Content-Type", "text/css");
				} else {
					Header[] contentTypeHeaders = response.getHeaders("Content-Type");
					if (contentTypeHeaders != null) {
						for (Header h : contentTypeHeaders) {
							resp.setHeader(h.getName(), h.getValue());
							if (logger.isTraceEnabled()) {
								logger.trace(" - " + h.getName() + ": " + h.getValue());
							}
						}
					}
				}
				Header[] contentLengthHeaders = response.getHeaders("Content-Length");
				if (contentLengthHeaders != null) {
					for (Header h : contentLengthHeaders) {
						resp.setHeader(h.getName(), h.getValue());
						if (logger.isTraceEnabled()) {
							logger.trace(" - " + h.getName() + ": " + h.getValue());
						}
					}
				}

				InputStream is = response.getEntity().getContent();
				OutputStream os = resp.getOutputStream();
				byte[] buf = new byte[512];
				int len;
				while ((len = is.read(buf)) > 0) {
					os.write(buf, 0, len);
				}
				os.flush();
				os.close();
				is.close();
			} catch (Throwable e) {
				logger.error("Exception rendering request", e);
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}

	}
}
