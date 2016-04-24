package com.ibm.scas.analytics.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.inject.Singleton;
import com.ibm.scas.analytics.utils.ExpiredPromoCodeException;
import com.ibm.scas.analytics.utils.InvalidPromoCodeException;
import com.ibm.scas.analytics.utils.PromoCode;

/**
 * Servlet implementation class VerifyServlet
 */
// @WebServlet("/validate")
@Singleton
public class ValidateServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ValidateServlet.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ValidateServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.info("POST validate");

		String path = request.getPathInfo();
		String offeringId = path.substring(1);
		String email = request.getParameter("email");
		String promoCode = request.getParameter("promoCode");
		if (logger.isDebugEnabled()) {
			logger.debug("===Validate Servlet called===");
			logger.debug("Offering: " + offeringId);
			logger.debug("Email: " + email);
			logger.debug("Promo code: " + promoCode);
		}

		String failure = null;
		try {
			PromoCode.validate(offeringId, email, promoCode);
			logger.info("-- VALIDATED PROMO CODE --");
		} catch (InvalidPromoCodeException e) {
			logger.info("-- INVALID PROMO CODE --");
			failure = "{\"result\":[{\"field\":\"promoCode\",\"title\":\"Invalid promo code\",\"description\":\"Please contact IBM Cloud Analytics support and request a promo code\",\"level\":\"ERROR\"}]}";
		} catch (ExpiredPromoCodeException e) {
			logger.info("-- EXPIRED PROMO CODE --");
			failure = "{\"result\":[{\"field\":\"promoCode\",\"title\":\"Promo code has expired\",\"description\":\"Please contact IBM Cloud Analytics support and request a promo code\",\"level\":\"ERROR\"}]}";
		}

		if (failure != null) {
			response.setContentType("application/json");
			PrintWriter writer = response.getWriter();
			writer.print(failure);
			writer.flush();
			writer.close();
		}
	}
}
