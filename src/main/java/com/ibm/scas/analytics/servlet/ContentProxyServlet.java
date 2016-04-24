package com.ibm.scas.analytics.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ibm.scas.analytics.content.DynamicLoader;
import com.ibm.scas.analytics.utils.CPEException;

/**
 * Servlet implementation class ContentProxyServlet
 */
@Singleton
public class ContentProxyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ContentProxyServlet.class);
	
	@Inject DynamicLoader dynamicLoader;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ContentProxyServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			String path = request.getPathInfo();
			int index = path.indexOf("/", 1);
			if (index <= 0) {
				logger.warn("Illegal dynamic content path: " + path + ". Ignored.");
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return;
			} 
			
			String offeringId = path.substring(1, index);
			String relativePath = path.substring(index + 1);

			if (logger.isDebugEnabled()) {
				logger.debug("Offering: " + offeringId);
				logger.debug("Relative path: " + relativePath);
			}
			
			dynamicLoader.render(request, response, offeringId, relativePath);
		} catch (CPEException e) {
			logger.error(e.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
	}

}
