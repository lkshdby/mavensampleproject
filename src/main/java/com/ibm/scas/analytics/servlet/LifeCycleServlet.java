package com.ibm.scas.analytics.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ibm.scas.analytics.backend.ProvisioningService;
import com.ibm.scas.analytics.backend.appdirect.AppDirectGateway;
import com.ibm.scas.analytics.backend.appdirect.AppDirectGatewayException;
import com.ibm.scas.analytics.backend.appdirect.AppDirectGatewayFactory;
import com.ibm.scas.analytics.beans.NotificationEvent;
import com.ibm.scas.analytics.beans.NotificationResult;
import com.ibm.scas.analytics.persistence.PersistenceService;
import com.ibm.scas.analytics.persistence.beans.Offering;
import com.ibm.scas.analytics.provider.ServiceProvider;
import com.ibm.scas.analytics.provider.impl.ServiceProviderPluginFactory;
import com.ibm.scas.analytics.utils.OauthAdapter;

/**
 * Servlet implementing the AppDirect lifecycle callbacks
 * 
 * @author Han Chen
 */
//@WebServlet("/lifecycle/*")
@Singleton
public class LifeCycleServlet extends HttpServlet {
	private static Logger logger = Logger.getLogger(LifeCycleServlet.class);
	private static final long serialVersionUID = 1L;

	private static final String VERIFY_AND_CREATE = "verify";
	private static final String CREATE = "create";
	
	@Inject private PersistenceService persistenceService;
	@Inject private ServiceProviderPluginFactory pluginFactory;
	@Inject private AppDirectGatewayFactory appDirectGatewayFactory;
	@Inject private ProvisioningService engine;
	
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public LifeCycleServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		handler(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		handler(request, response);
	}

	protected void handler(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
		try {
			String path = req.getPathInfo();
			int index = path.indexOf("/", 1);
			String action = null;
			String offeringId = null;
			if (index > 0) {
				action = path.substring(1, index);
				offeringId = path.substring(index + 1);
			}
			if (logger.isInfoEnabled()) {
				logger.info("===Lifecycle Servlet called===");
				logger.info("Action: " + action);
				logger.info("Offering: " + offeringId);
			}
			NotificationResult result;
			if (action == null) {
				logger.error("Malformed servlet path. Ignored.");
				// FIXME: return a proper error
				result = new NotificationResult("true", "Success", "dummy-account-id");
			} else if (offeringId.startsWith("dummy")) {
				logger.info("Dummy offering Id encountered. Ignored.");
				result = new NotificationResult("true", "Action " + action + " for dummy offering is successful", "dummy-account-id");
			} else {
				if (logger.isDebugEnabled()) {
					String url = req.getRequestURL().toString();
					String method = req.getMethod();
					String query = req.getQueryString();
					logger.debug("URL: " + url);
					logger.debug("Method: " + method);
					logger.debug("Path: " + path);
					logger.debug("Query: " + query);

					logger.debug("Parameters");
					Map<String, String[]> paramMap = req.getParameterMap();
					for (String key : paramMap.keySet()) {
						String[] values = paramMap.get(key);
						for (String v : values) {
							logger.debug(" - " + key + ": " + v);
						}
					}
					
					logger.debug("Headers");
					Enumeration<String> headers = req.getHeaderNames();
					while (headers.hasMoreElements()) {
						String header = headers.nextElement();
						String value = req.getHeader(header);
						logger.debug(" - " + header + ": " + value);
					}
				}

				/*
				 * retrieve event from AppDirect
				 */
				String eventUrl = req.getParameter("url");
				Offering offering = persistenceService.getObjectById(Offering.class, offeringId);
				OauthAdapter adapter = new OauthAdapter(offering.getOauthKey(), offering.getOauthSecret());
				String eventJson = adapter.doGet(eventUrl);
				if (logger.isDebugEnabled()) {
					logger.debug("Event retrieved, " + eventJson.length() + " bytes\n" + eventJson);
				}

				Gson gson = new Gson();
				NotificationEvent event = gson.fromJson(eventJson, NotificationEvent.class);

				String token = eventUrl.substring(eventUrl.lastIndexOf("/") + 1);
				boolean isDummy = token.startsWith("dummy");
				final AppDirectGateway appDirectGateway = appDirectGatewayFactory.create(isDummy);
				
				if (CREATE.equals(action) && event.getReturnUrl() != null && !isDummy) {
					if (logger.isDebugEnabled()) {
						logger.debug("Interactive subscription creation");
					}
					result = null;

					/*
					 * interactive subscription creation process
					 */
					ServiceProvider plugin = pluginFactory.getPlugin(offeringId);
					String editionCode = event.getPayload().getOrder().getEditionCode();
					boolean accessControlRequired = plugin.editionRequiresAccessCode(editionCode);
					if (accessControlRequired) {
						/*
						 * access control is required for the selected edition.
						 * we assume that when access code is validated there are enough capacity, so we
						 * won't check for capacity again
						 */
						logger.info("Access control is required for edition '" + editionCode + "'. redirecting...");
						
						HttpSession session = req.getSession();

						session.setAttribute("offeringId", offeringId);
						session.setAttribute("companyName", event.getPayload().getCompany().getName());
						session.setAttribute("firstName", event.getCreator().getFirstName());
						session.setAttribute("lastName", event.getCreator().getLastName());
						session.setAttribute("email", event.getCreator().getEmail());
						session.setAttribute("eventJson", eventJson);
						
						resp.sendRedirect(req.getContextPath() + "/access-control.jsp");
					} else {
						/*
						 * check capacity
						 * all capacities below are calculated in canonical node size.
						 */
						int availableCapacity = engine.getAvailableCapacity();
						int nodeSize = plugin.getEditionNodeSize(editionCode);
						int requestedCapacity = appDirectGateway.extractServerQuantity(offeringId, event) * nodeSize;
						boolean hasCapacity = requestedCapacity <= availableCapacity;
						if (logger.isDebugEnabled()) {
							logger.debug("System capacity check. Requested: " + requestedCapacity + ", Available: " + availableCapacity + " ==> " + (hasCapacity ? "Allow" : "Deny"));
						}
						if (hasCapacity) {
							NotificationResult res = appDirectGateway.processEvent(offeringId, event);
							String resultParams = "&success=" + res.getSuccess() + "&accountIdentifier=" + res.getAccountIdentifier();
							String signedUrl = adapter.signUrl(event.getReturnUrl() + resultParams);
							resp.sendRedirect(signedUrl);
						} else {
							HttpSession session = req.getSession();
							session.setAttribute("offeringId", offeringId);
							session.setAttribute("companyName", event.getPayload().getCompany().getName());
							session.setAttribute("firstName", event.getCreator().getFirstName());
							session.setAttribute("lastName", event.getCreator().getLastName());
							session.setAttribute("email", event.getCreator().getEmail());
							session.setAttribute("returnUrl", event.getReturnUrl());
							session.setAttribute("availableNodes", availableCapacity / nodeSize);
							
							resp.sendRedirect(req.getContextPath() + "/capacity.jsp");
						}
					}
				} else {
					/*
					 * fire event off to the processor
					 */
					result = appDirectGateway.processEvent(offeringId, event);
				}
			}

			if (result != null) {
				resp.setContentType("application/json");
				PrintWriter writer = resp.getWriter();
				Gson gson = new Gson();
				writer.println(gson.toJson(result));
				writer.flush();
				writer.close();
			}
		} catch (AppDirectGatewayException e) {
			// generate an error message, write a response to the response URL
			final NotificationResult result = new NotificationResult("false", e.getCause().getLocalizedMessage(), e.getId());
			resp.setContentType("application/json");
			PrintWriter writer;
			try {
				writer = resp.getWriter();
				Gson gson = new Gson();
				writer.println(gson.toJson(result));
				writer.flush();
				writer.close();			
			} catch (IOException e1) {
				logger.error(e1.getLocalizedMessage(), e);
			}

			logger.error(e.getLocalizedMessage(), e);
		} catch (Exception e) {
			logger.error("Exception handling servlet request", e);
		}
	}
}
