package com.ibm.scas.analytics.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ibm.scas.analytics.reports.AccountReport;
import com.ibm.scas.analytics.utils.CPEException;

/**
 * Servlet implementation class ReportServlet
 */
// @WebServlet("/report")
@Singleton
public class ReportServlet extends HttpServlet {
	private static final Logger logger = Logger.getLogger(ReportServlet.class);
	private static final long serialVersionUID = 1L;

	private static final String ACCOUNT = "account";
	
	@Inject AccountReport accountReport;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ReportServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getPathInfo();
		String type = path.substring(1);
		String remoteHost = request.getRemoteHost();
		if (logger.isDebugEnabled()) {
			logger.debug("===Report Servlet called===");
			logger.debug("Type: " + type);
			logger.debug("Remote host: " + remoteHost);
		}

		response.setContentType("text/plain");
		PrintWriter writer = response.getWriter();
		if (!"127.0.0.1".equals(remoteHost)) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			writer.println("Only connections from localhost are allowed.");
		} else if (ACCOUNT.equals(type)) {
			try {
				accountReport.generate(writer);
			} catch (CPEException e) {
				logger.error(e);
				throw new ServletException(e);
			}
		} else {
			writer.println("Unrecognized report type: " + type);
		}
		writer.flush();
		writer.close();
	}
}
