package com.ibm.scas.analytics.servlet;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.log4j.Logger;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;
import org.openid4java.consumer.InMemoryNonceVerifier;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.Discovery;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.discovery.html.HtmlResolver;
import org.openid4java.discovery.yadis.YadisResolver;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.server.RealmVerifierFactory;
import org.openid4java.util.HttpFetcherFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ibm.scas.analytics.EngineProperties;
import com.ibm.scas.analytics.backend.audit.AuditEventLogger;
import com.ibm.scas.analytics.persistence.PersistenceException;
import com.ibm.scas.analytics.persistence.PersistenceService;
import com.ibm.scas.analytics.persistence.beans.Offering;
import com.ibm.scas.analytics.persistence.beans.Subscriber;
import com.ibm.scas.analytics.persistence.util.WhereClause;
import com.ibm.scas.analytics.utils.SSLUtils;

/**
 * Servlet implementing the AppDirect login handler
 * 
 * @author Han Chen
 * 
 */
// @WebServlet("/login/*")

@Singleton
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(LoginServlet.class);
	private ConsumerManager manager;
	
	@Inject PersistenceService persistence;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public LoginServlet() {
		super();
	}

	private ConsumerManager createTrustingConsumerManager() throws Exception {
		// Install the all-trusting trust manager SSL Context
		SSLContext sc = SSLUtils.disableSSLCertValidation();

		HttpFetcherFactory hff = new HttpFetcherFactory(sc, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		YadisResolver yr = new YadisResolver(hff);
		RealmVerifierFactory rvf = new RealmVerifierFactory(yr);
		Discovery d = new Discovery(new HtmlResolver(hff), yr, Discovery.getXriResolver());

		ConsumerManager manager = new ConsumerManager(rvf, d, hff);
		manager.setAssociations(new InMemoryConsumerAssociationStore());
		manager.setNonceVerifier(new InMemoryNonceVerifier(5000));
		return manager;
	}

	protected ConsumerManager getManager() {
		if (manager == null) {
			try {
				boolean ignoreSslError = Boolean.parseBoolean(EngineProperties.getInstance().getProperty(EngineProperties.IGNORE_SSL_ERROR, "false"));
				if (ignoreSslError) {
					manager = createTrustingConsumerManager();
				} else {
					manager = new ConsumerManager();
				}
				// Han 3/28/2014: increasing nonce age to overcome DEA clock
				// sync
				// problem
				manager.setMaxNonceAge(300);
			} catch (Exception e) {
				logger.error("Failed to create openId ConsumerManager", e);
			}
		}

		return manager;
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
		if (logger.isInfoEnabled()) {
			logger.info("===Login Servlet called===");
		}
		String path = req.getPathInfo();
		String offeringId;
		boolean isReturnUrl;
		{
			int index = path.indexOf("/", 1);
			if (index >= 0) {
				/*
				 * path is /{offering}/openid
				 */
				offeringId = path.substring(1, index);
				isReturnUrl = false;
			} else {
				/*
				 * path is /{offering}
				 */
				offeringId = path.substring(1);
				isReturnUrl = true;
			}
		}
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
		// extract the receiving URL from the HTTP request
		String receivingURL = req.getRequestURL().toString();
		String forwardedProto = req.getHeader("x-forwarded-proto");
		if (forwardedProto != null) {
			int index = receivingURL.indexOf(":");
			receivingURL = forwardedProto + receivingURL.substring(index);
		}
		// URL base
		String baseUrl;
		{
			int index = receivingURL.indexOf("://");
			index = receivingURL.indexOf("/", index + 3);
			if (index > 0) {
				baseUrl = receivingURL.substring(0, index);
			} else {
				baseUrl = receivingURL;
			}
		}
		String errorRedirect = baseUrl + req.getContextPath() + "/error.jsp";
		String loginRedirect = baseUrl + req.getContextPath() + "/index.jsp";

		try {
			Offering offering = persistence.getObjectById(Offering.class, offeringId);
			if (offering == null) {
				logger.info("Offering not found. Redirecting to error page.");
				req.getSession().setAttribute("errorMsg", "Unrecognized offering id: " + offeringId);
				resp.sendRedirect(errorRedirect);

			} else if (!isReturnUrl) {
				logger.info("Authenticating...");

				int index = receivingURL.lastIndexOf("/");
				String returnUrl = receivingURL.substring(0, index);

				if (logger.isInfoEnabled()) {
					logger.info("Return URL: " + returnUrl);
				}
				String openId = req.getParameter("openid");
				String accountId = req.getParameter("accountId");
				if (logger.isInfoEnabled()) {
					logger.info("openId: " + openId);
					logger.info("accountId: " + accountId);
				}
				req.getSession().setAttribute("accountId", accountId);
				authRequest(returnUrl, openId, req, resp, errorRedirect);
			} else {
				logger.info("Verifying response...");
				verifyResponse(offering, req, resp, loginRedirect, errorRedirect);
			}

		} catch (Throwable e) {
			logger.error("Exception in Login servlet", e);
			e.printStackTrace(System.out);
			req.getSession().setAttribute("errorMsg", "An exception occurred during login. Please retry. If the problem persists, please contact support.");
			try {
				resp.sendRedirect(errorRedirect);
			} catch (IOException e1) {
				throw new ServletException(e1);
			}
		}
	}

	private void authRequest(String returnUrl, String openId, HttpServletRequest httpReq, HttpServletResponse resp, String errorRedirect) throws IOException,
			ServletException {
		try {
			// perform discovery on the user-supplied identifier
			List discoveries = getManager().discover(openId);

			// attempt to associate with the OpenID provider
			// and retrieve one service endpoint for authentication
			DiscoveryInformation discovered = getManager().associate(discoveries);

			// store the discovery information in the user's session
			httpReq.getSession().setAttribute("openid-disc", discovered);
			httpReq.getSession().setAttribute("openId", openId);

			// obtain a AuthRequest message to be sent to the OpenID provider
			AuthRequest authReq = getManager().authenticate(discovered, returnUrl);

			// fetch user information from AppDirect
			FetchRequest fetch = FetchRequest.createFetchRequest();
			fetch.addAttribute("email", "http://axschema.org/contact/email", true);
			fetch.addAttribute("firstName", "http://axschema.org/namePerson/first", true);
			fetch.addAttribute("lastName", "http://axschema.org/namePerson/last", true);
			fetch.addAttribute("companyName", "http://axschema.org/company/name", true);

			authReq.addExtension(fetch);

			// TODO: URL based method has a limit of about 2KB in parameter
			// length. Consider using form-based method
			logger.info("Requesting authentication using URL encoded parameters");
			resp.sendRedirect(authReq.getDestinationUrl(true));
		} catch (OpenIDException e) {
			logger.error("Exception while authenticating", e);
			e.printStackTrace(System.out);
			logger.info("Redirecting to error page, assuming login failed.");
			resp.sendRedirect(errorRedirect);
		}
	}

	public void verifyResponse(Offering offering, HttpServletRequest httpReq, HttpServletResponse resp, String loginRedirect, String errorRedirect)
			throws ServletException, IOException {
		try {
			HttpSession session = httpReq.getSession();

			// extract the parameters from the authentication response
			// (which comes in as a HTTP request from the OpenID provider)
			ParameterList response = new ParameterList(httpReq.getParameterMap());

			// retrieve the previously stored discovery information
			DiscoveryInformation discovered = (DiscoveryInformation) session.getAttribute("openid-disc");

			// extract the receiving URL from the HTTP request
			String receivingURL = httpReq.getRequestURL().toString();
			String forwardedProto = httpReq.getHeader("x-forwarded-proto");
			if (forwardedProto != null) {
				int index = receivingURL.indexOf(":");
				receivingURL = forwardedProto + receivingURL.substring(index);
			}
			String queryString = httpReq.getQueryString();
			if (queryString != null && queryString.length() > 0) {
				receivingURL += "?" + queryString;
			}

			// verify the response; ConsumerManager needs to be the same
			// (static) instance used to place the authentication request
			VerificationResult verification = getManager().verify(receivingURL.toString(), response, discovered);

			// examine the verification result and extract the verified
			// identifier
			Identifier verified = verification.getVerifiedId();

			if (verified != null) {
				logger.info("Login is successful.");
				AuthSuccess authSuccess = (AuthSuccess) verification.getAuthResponse();

				if (!authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
					logger.info("Failed to retrieve user information. Redirecting to error page.");
					session.setAttribute("errorMsg", "Cannot retrieve user information from AppDirect login service.");
					resp.sendRedirect(errorRedirect);
				} else {
					FetchResponse fetchResp = (FetchResponse) authSuccess.getExtension(AxMessage.OPENID_NS_AX);

					String email = fetchResp.getAttributeValue("email");
					String firstName = fetchResp.getAttributeValue("firstName");
					String lastName = fetchResp.getAttributeValue("lastName");
					String companyName = fetchResp.getAttributeValue("companyName");

					session.setAttribute("companyName", companyName);
					session.setAttribute("firstName", firstName);
					session.setAttribute("lastName", lastName);
					session.setAttribute("email", email);

					session.setAttribute("loggedIn", true);

					String openId = (String) session.getAttribute("openId");
					String accountId = (String) session.getAttribute("accountId");
					final List<Subscriber> subscribers = persistence.getObjectsBy(Subscriber.class, new WhereClause("account.id", accountId), new WhereClause("externalId", openId), new WhereClause("type", Subscriber.APPDIRECT));
					if (subscribers == null || subscribers.isEmpty()) {
						logger.info("Subscriber not found. Redirecting to error page.");
						session.setAttribute("errorMsg", "You are not assigned to this service.");
						resp.sendRedirect(errorRedirect);
					} else {
						final Subscriber subscriber = subscribers.get(0);
						if (logger.isDebugEnabled()) {
							logger.debug("Offering: " + offering.getName());
							logger.debug("Subsriber: " + firstName + " " + lastName + " (" + openId + ")");
							logger.debug("Subscriber id: " + subscriber.getId());
						}

						// session.setAttribute("pageTitle",
						// offering.getName());
						// session.setAttribute("offeringId", offering.getId());
						// session.setAttribute("offeringName",
						// offering.getName());
						// session.setAttribute("urlPath",
						// offering.getUrlPath());
						session.setAttribute("subscriberId", subscriber.getId());
						session.setAttribute("apiKey", subscriber.getApiKey());
						
						String remoteHost = httpReq.getRemoteHost();
						new AuditEventLogger().recordLogin(remoteHost, companyName, firstName, lastName, openId, offering.getId(), accountId);
						resp.sendRedirect(loginRedirect);
					}
				}
			} else {
				logger.info("Login failed. Redirecting to error page.");
				session.setAttribute("errorMsg", "Log in failed.");
				resp.sendRedirect(errorRedirect);
			}
		} catch (OpenIDException e) {
			logger.error("Exception encounter verifying login response.", e);
			e.printStackTrace(System.out);
			logger.info("Redirecting to error page, assuming login failed.");
			resp.sendRedirect(errorRedirect);
		} catch (PersistenceException e) {
			logger.error("Exception encounter verifying login response.", e);
			logger.info("Redirecting to error page, assuming login failed.");
			resp.sendRedirect(errorRedirect);
		}
	}
}
