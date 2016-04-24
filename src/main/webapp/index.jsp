<%@page import="com.google.inject.Injector"%>
<%@page import="com.ibm.scas.analytics.provider.impl.ServiceProviderPluginFactory"%>
<%@page import="com.ibm.scas.analytics.provider.ServiceProvider"%>
<%@page import="com.ibm.scas.analytics.persistence.beans.Offering"%>
<%@page import="com.ibm.scas.analytics.persistence.beans.AllClusterDetails"%>
<%@page import="java.util.List"%>
<%@page import="com.ibm.scas.analytics.persistence.beans.Account"%>
<%@page import="com.ibm.scas.analytics.persistence.PersistenceService"%>
<%@page import="com.ibm.scas.analytics.EngineProperties"%>
<%@page import="java.util.Enumeration"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<%
	boolean testMode = "true".equalsIgnoreCase(EngineProperties.getInstance().getProperty(EngineProperties.TEST_MODE));
	String marketplaceUrl = EngineProperties.getInstance().getProperty(EngineProperties.MARKETPLACE_URL);
	String isDGWActionAllowed = EngineProperties.getInstance().getProperty(EngineProperties.DGW_ACTIONS_ALLOWED, EngineProperties.DEFAULT_DGW_ACTIONS_ALLOWED);
	
	boolean loggedIn = session.getAttribute("loggedIn") != null;
	if (!loggedIn && !testMode) {
		session.setAttribute("errorMsg", "Please log in from <a href='" + marketplaceUrl + "'>IBM Cloud marketplace</a>.");
		response.sendRedirect("error.jsp");
		return;
	} 

	if (testMode) {
		String testTarget = "biginsights";

		String testAccountId;
		String testSubscriberId;
		String testApiKey;
		
		if ("biginsights".equals(testTarget)) {
			testAccountId = "2000";
			testSubscriberId = "20000";
			testApiKey = "1234567890";
		} else if ("streams".equals(testTarget)) {
			testAccountId = "2001";
			testSubscriberId = "20001";
			testApiKey = "2345678901";
		} else if ("cloudera".equals(testTarget)) {
			testAccountId = "2002";
			testSubscriberId = "20002";
			testApiKey = "3456789012";
		} else if ("bluacc".equals(testTarget)) {
			testAccountId = "2003";
			testSubscriberId = "20003";
			testApiKey = "4567890123";
		} else if ("hortonworks".equals(testTarget)) {
			testAccountId = "2004";
			testSubscriberId = "20004";
			testApiKey = "5678901234";
		} else if ("han-test".equals(testTarget)) {
			testAccountId = "2005";
			testSubscriberId = "20005";
			testApiKey = "6789012345";
		} else if ("bi-qse".equals(testTarget)) {
			testAccountId = "2006";
			testSubscriberId = "20006";
			testApiKey = "7890123456";
		} else if ("streams-dev".equals(testTarget)) {
			testAccountId = "2007";
			testSubscriberId = "20007";
			testApiKey = "9012345678";
		} else if ("monitoring".equals(testTarget)) {
			testAccountId = "2008";
			testSubscriberId = "20008";
			testApiKey = "0123456789";
		} else if ("ibm10cent".equals(testTarget)) {
			testAccountId = "2009";
			testSubscriberId = "20009";
			testApiKey = "1234567890";
		} else {
			// default to streams
			testAccountId = "2001";
			testSubscriberId = "20001";
			testApiKey = "2345678901";
		}

		session.setAttribute("companyName", "Acme");
		session.setAttribute("firstName", "John");
		session.setAttribute("lastName", "Doe");
		session.setAttribute("email", "john.doe@acme.com");

		session.setAttribute("accountId", testAccountId);
		session.setAttribute("subscriberId", testSubscriberId);
		session.setAttribute("apiKey", testApiKey);
	}
	
	String companyName = (String) session.getAttribute("companyName");
	String firstName = (String) session.getAttribute("firstName");
	String lastName = (String) session.getAttribute("lastName");
	String email = (String) session.getAttribute("email");

	String accountId = (String) session.getAttribute("accountId");
	String subscriberId = (String) session.getAttribute("subscriberId");
	String apiKey = (String) session.getAttribute("apiKey");
	
	String contextPath = request.getContextPath();

	final Injector inj = (Injector) pageContext.getServletContext().getAttribute(Injector.class.getName());
	PersistenceService persistence = inj.getInstance(PersistenceService.class);
	Account account = persistence.getObjectById(Account.class, accountId);
	marketplaceUrl = account.getMarketUrl();
	Offering offering = account.getOffering();
	String offeringId = offering.getId();
	String offeringName = offering.getName();
	String subOfferingName = "";
	{
		int index = offeringName.indexOf('|');
		if (index > 0) {
			subOfferingName = offeringName.substring(index + 1);
			offeringName = offeringName.substring(0, index);
		}
	}
	if (testMode) {
		offeringName += " (Local test mode)";
	}
	
	String cpeVersion = EngineProperties.getInstance().getProperty(EngineProperties.CPE_VERSION);
	String cpeBuild = EngineProperties.getInstance().getProperty(EngineProperties.CPE_BUILD);
	
	ServiceProvider plugin = inj.getInstance(ServiceProviderPluginFactory.class).getPlugin(offeringId);
	String pluginVersion = plugin.getVersion();
	String pluginBuild = plugin.getBuild();
	
	session.setAttribute("offeringName", offering.getName());
	session.setAttribute("plugin", offering.getPlugin().getId());
%>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title><%=offeringName%></title>
	<meta name="description" content="Explore hundreds of IBM and Business Partner services from across the cloud spectrum."/>
	<meta name="keywords" content="Cloud marketplace Explore hundreds of IBM and Business Partner services from across the cloud spectrum."/>
	<meta name="pageName" content="HomePage"/>
	<link rel="SHORTCUT ICON" href="theme/images/icon-favicon.ico">

	<!--Include stylesheets -->
	<link href="//1.www.s81c.com/common/v17/css/www.css" rel="stylesheet" title="www" type="text/css" />
	<link rel="stylesheet" href="<%=contextPath%>/theme/css/cloud-marketplace.css" type="text/css" />		    

	<link rel="stylesheet" href="<%=contextPath%>/theme/icas-analytics.css" title="www" type="text/css" />
	<link rel="stylesheet" href="//ajax.googleapis.com/ajax/libs/dojo/1.9.1/dijit/themes/claro/claro.css" media="screen"/>
	<link rel="stylesheet" href="//ajax.googleapis.com/ajax/libs/dojo/1.9.1/dojox/grid/resources/Grid.css" />
	<link rel="stylesheet" href="//ajax.googleapis.com/ajax/libs/dojo/1.9.1/dojox/grid/resources/claroGrid.css" />
	
	<fmt:setLocale value="<%=request.getLocale()%>"/>
</head>

<body id="ibm-cloud-marketplace" class="claro">

	<div id="marketplace-page-wrapper">		
		<header id="header-cloud-marketplace">
			<div class="primary-nav-container bg-default-blue">
				<nav class="desktop">						
					<div class="ibm-logo-container fleft">
						<a href="http://www.ibm.com/marketplace/cloud/us/en-us/" class="ibm-logo">
							<img class="hamburger-icon desktop" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAGQAAABkAQMAAABKLAcXAAAABlBMVEX///////9VfPVsAAAAAXRSTlMAQObYZgAAABxJREFUeAFjGNZgFNj/BwNyeUPAD6M+GnpgFAAAc4Rpyh7s7lcAAAAASUVORK5CYII=" alt="IBM Cloud marketplace" />
						</a>
					</div>
					<!-- ibm-logo-container -->	

					<div class="ibm-cloud-title fleft">
						<h1><a href="http://www.ibm.com/cloud-computing/us/en/"><fmt:message key="cpe.dashboard.ibm"/> <span><fmt:message key="cpe.dashboard.cloud"/></span></a></h1>
					</div>
					<!-- ibm-cloud-title -->

					<ul class="primary-nav min-td-spce">							
						<li>
							<span class="parent">
								<a href="http://www.ibm.com/cloud-computing/us/en/"><fmt:message key="cpe.dashboard.about"/></a>
							</span>
							<ul>
								<li><a href="http://www.ibm.com/cloud-computing/us/en/what-is-cloud-computing.html"><fmt:message key="cpe.dashboard.what"/></a></li>
								<li><a href="http://www.ibm.com/cloud-computing/us/en/why-cloud.html"><fmt:message key="cpe.dashboard.why"/></a></li>								
							</ul>
						</li>
						<li><a href="<%=marketplaceUrl%>"><fmt:message key="cpe.dashboard.marketplace"/></a></li>
						<li><a href="http://www.ibm.com/cloud-computing/us/en/partner-landing.html"><fmt:message key="cpe.dashboard.partners"/></a></li>
						<li><a href="http://www.ibm.com/cloud-computing/us/en/cloud-solutions/"><fmt:message key="cpe.dashboard.solutions"/></a></li>
						<li><a href="https://developer.ibm.com/marketplace/"><fmt:message key="cpe.dashboard.community"/></a></li>		
						<li><a href="#" class="active"><fmt:message key="cpe.dashboard.analytics"/></a></li>

						<li>
							<span class="parent"><%= firstName %> <%= lastName %><span class="user-icon"></span></span>
							<ul>
								<li><a href="logout.jsp" ><fmt:message key="cpe.dashboard.logout"/></a></li>
							</ul>
						</li>						
					</ul>
					<!-- page navigation links -->
				</nav>
				<!-- nav bar -->
			</div>
			<!-- marketplace-global-nav -->
		</header>	
		<!-- page header -->

		<section id="page-content" class="cloud-default-layout">
			<div id="welcome-page" class="wrapper">

<!-- BEGIN CPE CONTENT -->
<div class="ibm-columns" style="padding-top: 40px; overflow: visible">
	<div class="ibm-col-6-6">
		<div id="cpe-dashboard">
			<div data-dojo-type="platform/dashboard" data-dojo-props="offeringName: '<%=offeringName%>', subOfferingName: '<%=subOfferingName%>', contextUrl: '<%=contextPath%>/dynamic/<%=offering.getId()%>'"></div>
		</div>
	</div>
</div>
<!-- END CPE CONTENT -->
</div>
		</section>
<!-- Main dojo entry point -->
<script>
<%-- These are global variables that will be referenced by other modules --%>
	icasConfig = {
		multiuser: <%= offering.isMultiuser() %>,
		urlPath: "<%= offeringId %>",
		accountQuota: <%= account.getQuantity() %>,
		subscriberId: "<%= subscriberId %>",
		accountId: "<%= accountId %>",
		apiKey: "<%= apiKey %>",
		contextPath: "<%= contextPath %>",
		offeringName: "<%= offering.getName() %>",
		offeringId: "<%= offering.getId() %>",
		plugin: "<%= offering.getPlugin().getId() %>", 
		isDGWActionAllowed: "<%=isDGWActionAllowed %>"
	};

    dojoConfig= {
        has: {
            "dojo-firebug": true
        },
        packages: [
            { name: "platform", location: "<%=contextPath%>/js/platform" },
            { name: "offering", location: "<%=contextPath%>/dynamic/<%= offeringId %>/js" },
            { name: "resource", location: "<%=contextPath%>/js/platform/bundles" },
            { name: "offeringResource", location: "<%=contextPath%>/dynamic/<%= offeringId %>/js/bundles" }
        ],
        parseOnLoad: true,
        async: true,
        locale: '<%=request.getLocale()%>'
    };
</script>

<link href="<%=contextPath%>/css/smart_wizard_vertical.css" rel="stylesheet"	type="text/css">
<link href="<%=contextPath%>/css/jquery-ui.css" rel="stylesheet"	type="text/css">
<script type="text/javascript" src="<%=contextPath%>/js/jquery/jquery-2.0.0.js"></script>
<script type="text/javascript" src="<%=contextPath%>/js/jquery/jquery.cookie.js"></script>
<script type="text/javascript" src="<%=contextPath%>/js/jquery/jquery-ui.js"></script>
<script type="text/javascript" src="<%=contextPath%>/js/jquery/jquery.smartWizard.js"></script>
<script type="text/javascript" src="<%=contextPath%>/js/jquery/populate-smartwizard-plugin.js"></script>

<script src="//ajax.googleapis.com/ajax/libs/dojo/1.9.1/dojo/dojo.js"></script>

<script>
	require(
		["dojo/parser", "platform/dashboard", "dojo/domReady!"],
		function(parser){
			parser.parse();
		}
	);
</script>

<div id="build-info">
<p>Platform version: <%= cpeVersion %> / <%= cpeBuild%> | Plugin version: <%= pluginVersion %> / <%= pluginBuild%></p>
</div>

	</div>
	<!-- marketplace-page-wrapper -->

	<footer id="cloud-marketplace-footer" class="footer">
		<div class="container info-bar">
			<ul class="contacts fleft">
				<li>
					<a href="https://www14.software.ibm.com/webapp/iwm/web/signup.do?source=gtsmail&lang=en_US&S_TACT=602AX26W" class="contact-link email" title="Email">&nbsp;</a>
					<span class="contact-link tele" title="Telephone">&nbsp;</span>
				</li>					
				<li class="contact-dt">
					US &amp; Canada: 1-877-999-7115 (Priority code 103HA09T)
				</li>
			</ul>
			<!-- contact details -->

			<ul class="social fright">
				<span class="visit-us">Visit us</span>
				<li><a class="social-link tw" href="https://twitter.com/ibmcloud/" title="Twitter" target="_blank"></a></li>
				<li><a class="social-link yt" href="http://www.youtube.com/user/ibmcloud/" title="Youtube" target="_blank"></a> </li>	
				<li><a class="social-link in" href="https://www.linkedin.com/groups/IBM-Cloud-Computing-2002441" title="LinkedIn" target="_blank"></a> </li>	
				<li><a class="social-link fb" href="https://www.facebook.com/IBMCloud" title="Facebook" target="_blank"></a> </li>	
				<li><a class="social-link wp" href="http://thoughtsoncloud.com/" title="Wordpress" target="_blank"></a> </li>	
				<li><a class="social-link gp last" href="https://plus.google.com/+ibmcloud/posts" title="Google+" target="_blank"></a></li>
			</ul>
			<!-- social media links -->
		</div>
		<!-- info-bar -->

		<hr/>
		<div class="container">
			<ul class="footer-links">
				<li>
					<a href="http://www.ibm.com/us/en/" class="footer-link ibm-logo first">
						<img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMgAAABaCAAAAADYnUYKAAAAAnRSTlMA/1uRIrUAAAV0SURBVHgB7dr9T1NXGAfw/U9PJDPGbYps0ixLTMZeTBbnFI0D5qgviMpQRwtMugUTX1BrVutgJSG+IDocM3OKUcEYIc4pohatirGbhVYoQr4L2sI55Tn33l7Gupnz+a29z729Xzm539tbX8MrQgfRQXQQHUQH0UF0kFc0yFdk6iKAYrJmzifbTg1A1khKn0LBQ0pz+SAVZKoTQAlZl11zG6ImUusHbyEp5fBBKsnUZQCbKC21Y5h0hNQCYHWRmoMPstna0vqC0jOvAxMCpJYHVjmpzeaD7HzbYSw3txvAN+oxTu4sx00ktb6jnnOEwVmx0KGycAkfZOzZiLF4HABG1WO8x2EkjQ2pDx6LgxMdHlEZfq575P8RJBw0NQwgFrRjGOMGg0b6mYUVNBKaVo+cIjuK+B6RzP4LqfJITdkj28hUB4BWsuWeeRDyIcVVMjR/WoXYRrZ8zxeiZBFSlJGauhDLrBXiCbJlFV+IsjOQ9JOxOXyQHW/mmMjuAvDbvBwb5q0GgJNvGQ69UQXJ4deNj7mYDzISeWYsFh0DMDoQe5a+gRhe7Gs4FHkKyYDxGQ0O6ULMEB1kZ37hpIL8i+A9KlglzK3cMAzRhfwC6SinIWpYVshZfhwTuld+zo2sKghbDvIhSQLg3SRZFKIAyQ5AtNa8rEtI4b7lIIUkOQ7eHZIsiEHUTLJfIXKRwu9IGJ5LCg8tBymwFSTbOMgta5291fxrcchykI+sLa2etJbWc4jcs7JYs96N4yWnaiLL+l+ks1USAi8qj50ehSjUKrkEydN7Id6dZOC++yGFUd0jmaaD1BU5BUUd4PU7i53W5O9FikbpI4qFV0U7AKC5SNhaLM06/7Qc5AMbhWisAynWkNpjANmUkIFCNNCKVC65z78kwSHgOgkci/+hQmyebpBaSJhH/ydy5G+8tSRw19oNYvNeS8WFqVwkCXmkV2NZJLhabXdptXn9ggO94EUO+vzmvC1ghG8FJ929jfjk67s9sRHv5JF9fnSK5+PzR3WPZJoOEqiSdIMXrrLkOBg/yTNBPNlaxaq4AhypkkQsB1lEknrwbpA1dZhqNUnOYogUrgNLSHIvY4W4z/Ty2wkEiLUcQEnGC1H9T1ExJchgFnEaAKy3GySPJD9O/16rDSmKSXIOwAbiPACw1O7Sat3lFezqAe+pd5/Xmu+OIUX7TmHz/roHAEJ79ntT7W4CgNPi8F6vLsSM00Ga3B6B+xp4Yc92Yax6dxyia5UeQdUBSH52CRtrqvsw7pTLI6lxRTCuWXx/uycy01etKEQNJAuaXbWYH8PmjADTuWoVzsSTxhbjQuRuKegoAL4QM/jst85CED9JZg9NM8j7qeXKu2G4tOpJtsJoaZ1NNBNJyoHpLa1ARaXA1Q3ek0q3OFYbh6jL5ZaOUg/RSfEj3K4gXmqsEN9NnvFhabgyonskM3SQiz6/sYO+fgBB4zHlvoMAenyKjY+QENzrT/A1IemMvBszIQVZR6bOAzhG9sQA/EAK7Uh4SBPWIGkZKWTiP9UUG/6c1omkr5mrbAnxcjMR5ITFIH9QwlKYBnHYXlrtAI6SHQvGAOAQKZzDhPemPvj+LL2l1bJpi7HysrsArm0uV48o9+zCuMubFZvvYMKFF6dR5hrFhMaNWzhl3+oe+VfpIJ0BU48B9AXS5K2LIaE3oNAPwS+BgD8EQXuAd4wPsmamrlp74kjwk8JZCLakfudY+p8oxOW9SDDsEUEfkROAjR6ZwSB5TUC6QfAxtQCYyUI8n97Sml/SBolRIQpcNAjAdiEedW40VlpyG0D3utKN5kpLaxouRZGiY20pO7y+F6IrtZDU82e2rlr3CEcH0UF0EB1EB9FBXgF/Aw1Wo7IO5udsAAAAAElFTkSuQmCC" alt="IBM">
					</a>
				</li>
				<li><a class="footer-link" href="http://www.ibm.com/contact/us/en/?lnk=flg-cont-usen"><fmt:message key="cpe.dashboard.contact"/></a></li>
				<li><a class="footer-link" href="http://www.ibm.com/privacy/us/en/?lnk=flg-priv-usen"><fmt:message key="cpe.dashboard.privacy"/></a></li>
				<li><a class="footer-link" href="http://www.ibm.com/legal/us/en/?lnk=flg-tous-usen"><fmt:message key="cpe.dashboard.terms"/></a></li>
				<li><a class="footer-link" href="http://www.ibm.com/accessibility/us/en/?lnk=flg-acce-usen"><fmt:message key="cpe.dashboard.accessibility"/></a></li>
				<li><a class="footer-link" id="ibm-cookie-preferences-link" href="javascript: void(0);"><fmt:message key="cpe.dashboard.cookie"/></a></li>
			</ul>
			<div class="clearfix"></div>
		</div>
		<!-- footer links -->

	</footer>
	<!-- page footer -->
	<!-- Error box -->
	<div id="dialogPane" class="dialogPane"></div>
	<div id="dialogBox" class="msgBox">
		<div class="msgBoxInnerDiv">
			<div class="title">
				<span class="titleMsg"></span>
				<img class="titleImage" src="<%=contextPath%>/images/error.png" style="display: none;"></img>
			</div>
			<div class="content">
			</div>
			<div class="close">
			   	<a href="#" class="yes" style="display: none;">Yes</a>
				<a href="#" class="no" style="display: none;">No</a>
			</div>
		</div>
	</div>
</body>
</html>
