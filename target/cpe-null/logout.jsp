<%@page import="com.google.inject.Injector"%>
<%@page import="org.apache.log4j.Logger"%>
<%@page import="com.ibm.scas.analytics.persistence.beans.Subscriber"%>
<%@page import="com.ibm.scas.analytics.persistence.beans.Account"%>
<%@page import="com.ibm.scas.analytics.persistence.PersistenceService"%>
<%@page import="com.ibm.scas.analytics.EngineProperties"%>
<%@page import="java.util.Enumeration"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE HTML>
<html lang="en">
<%
	Logger logger = Logger.getLogger("com.ibm.scas.analytics.JSP.logout");
	
	String accountId = (String) session.getAttribute("accountId");
	String subscriberId = (String) session.getAttribute("subscriberId");
	
	// TODO: use a controller class instead of injector/persistence service directly here
	final Injector inj = (Injector) pageContext.getServletContext().getAttribute(Injector.class.getName());
	PersistenceService persistence = inj.getInstance(PersistenceService.class);
	Subscriber subscriber = (Subscriber) persistence.getObjectById(Subscriber.class, subscriberId);
	Account account = (Account) persistence.getObjectById(Account.class, accountId);

	String signoutUrl = account.getMarketUrl() + "/applogout?openid=" + subscriber.getExternalId();
	logger.debug("Sign out URL: " + signoutUrl);

	session.invalidate();
 	response.sendRedirect(signoutUrl);
%>
<head>
</head>
<body>
</body>
</html>
