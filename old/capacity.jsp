<%@page import="com.google.inject.Injector"%>
<%@page import="com.ibm.scas.analytics.beans.NotificationEvent"%>
<%@page import="com.google.gson.Gson"%>
<%@page import="com.ibm.scas.analytics.utils.OauthAdapter"%>
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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en-US" xml:lang="en-US">
<%
	boolean testMode = "true".equalsIgnoreCase(EngineProperties.getInstance().getProperty(EngineProperties.TEST_MODE));
	String marketplaceUrl = EngineProperties.getInstance().getProperty(EngineProperties.MARKETPLACE_URL);
	
	if (testMode) {
		session.setAttribute("offeringId", "bi-qse");
		
		session.setAttribute("companyName", "Acme");
		session.setAttribute("firstName", "John");
		session.setAttribute("lastName", "Doe");
		session.setAttribute("email", "john.doe@acme.com");

		session.setAttribute("availableNodes", 4);
	}

	String offeringId = (String) session.getAttribute("offeringId");
	String companyName = (String) session.getAttribute("companyName");
	String firstName = (String) session.getAttribute("firstName");
	String lastName = (String) session.getAttribute("lastName");
	String email = (String) session.getAttribute("email");

	Integer availableNodes = (Integer) session.getAttribute("availableNodes");
	
	String contextPath = request.getContextPath();
	
	final Injector inj = (Injector) pageContext.getServletContext().getAttribute(Injector.class.getName());
	final PersistenceService persistence = inj.getInstance(PersistenceService.class);
	Offering offering = persistence.getObjectById(Offering.class, offeringId);
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

	String method = request.getMethod().toUpperCase();
	if ("POST".equals(method)) {
		OauthAdapter adapter = new OauthAdapter(offering.getOauthKey(), offering.getOauthSecret());

		String returlUrl = (String) session.getAttribute("returnUrl");
		String resultParams = "&success=false&errorCode=OPERATION_CANCELLED&message=Insufficient+Capacity";
		String signedUrl = adapter.signUrl(returlUrl + resultParams);

		response.sendRedirect(signedUrl);
	}

%>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<link rel="schema.DC" href="http://purl.org/DC/elements/1.0/"/>
	<link rel="SHORTCUT ICON" href="http://www.ibm.com/favicon.ico"/>

	<meta name="DC.Rights" content="? Copyright IBM Corp. 2011" />
	<meta name="Keywords" content="Click to accept" />
	<meta name="DC.Date" scheme="iso8601" content="2011-07-05" />
	<meta name="Source" content="v17 Template Generator, Template 17.02" />
	<meta name="Security" content="Public" />
	<meta name="Abstract" content="Click to accept" />
	<meta name="IBM.Effective" scheme="W3CDTF" content="2011-08-05" />
	<meta name="DC.Subject" scheme="IBM_SubjectTaxonomy" content="IBM_SubjectTaxonomy" />
	<meta name="Owner" content="REPLACE_ME@us.ibm.com" />
	<meta name="DC.Language" scheme="rfc1766" content="en-US" />
	<meta name="IBM.Country" content="US" />
	<meta name="Robots" content="index,follow" />
	<meta name="DC.Type" scheme="IBM_ContentClassTaxonomy" content="CT101IBM_SubjectTaxonomy" />
	<meta name="Description" content="ICAS Analytics Platform" />
	 
	<title><%=offeringName%></title>
	<link href="//1.www.s81c.com/common/v17/css/www.css" rel="stylesheet" title="www" type="text/css" />
	<link rel="stylesheet" href="<%=contextPath%>/theme/icas-analytics.css" title="www" type="text/css" />
	<link rel="stylesheet" href="//ajax.googleapis.com/ajax/libs/dojo/1.9.1/dijit/themes/claro/claro.css" media="screen"/>
	<link rel="stylesheet" href="//ajax.googleapis.com/ajax/libs/dojo/1.9.1/dojox/grid/resources/Grid.css" />
	<link rel="stylesheet" href="//ajax.googleapis.com/ajax/libs/dojo/1.9.1/dojox/grid/resources/claroGrid.css" />
<!-- 
 	<script src="//1.www.s81c.com/common/js/dojo/www.js" type="text/javascript">//</script>
 -->
</head>

<body id="ibm-com" class="claro">
<div id="ibm-top" class="ibm-application">

<!-- MASTHEAD_BEGIN -->
<div id="ibm-masthead">
	<div id="ibm-mast-options">
	<ul>
		<li id="ibm-geo"><a
			href="http://www.ibm.com/planetwide/select/selector.html"><span
				class="ibm-access">Select a country/region: </span>United States</a></li>
		<li id="ibm-sso" role="presentation" class=""><span dojoattachpoint="containerNode" widgetid="ibmweb_dynnav_greeting_0">
				<p dojoattachpoint="welcomeMessageNode" id="ibm-welcome-msg">Welcome [<%= companyName %>] <%= firstName %> <%= lastName %>
				</p> 
		</span></li>
	</ul>
	</div>
	<div id="ibm-universal-nav">
		<ul id="ibm-unav-links">
			<li id="ibm-home"><a href="http://www.ibm.com/us/en/"
				tabindex="0">IBM®</a></li>
		</ul>
		<div class="ibm-masthead-tools">
			<div id="ibm-mast-options"></div>
		</div>
		<div id="ibm-secondhead" class="ibm-alternate-masthead" role="banner">
			<div id="ibm-leadspace-body">
				<ul id="ibm-outlinks">
					<li><a id="ibm-cloud-link"
						href="http://www.ibm.com/cloud/marketplace">IBM Cloud</a></li>
					<li><a id="ibm-marketplace-link"
						href="<%=marketplaceUrl%>">marketplace</a></li>
				</ul>
			</div>
		</div>
	</div>
</div>

<!-- MASTHEAD_END -->

<div id="ibm-pcon">
  
<!-- CONTENT_BEGIN -->
<div id="ibm-content">
<div id="ibm-content-body">
<div id="ibm-content-main" role="main">

<div id="main_page">
<!-- BEGIN CPE CONTENT -->
<div class="ibm-columns" style="padding-top: 40px; overflow: visible">
	<div class="ibm-col-6-6">
		<div id="cpe-dashboard">
			<div data-dojo-type="platform/capacity" data-dojo-props="offeringName: '<%=offeringName%>', subOfferingName: '<%=subOfferingName%>', availableNodes: <%=availableNodes%>, contextUrl: '<%=contextPath%>/dynamic/<%=offering.getUrlPath()%>'"></div>
		</div>
	</div>
</div>
<!-- BEGIN CPE CONTENT -->
</div>

<!-- Main dojo entry point -->
<script>
<%-- These are global variables that will be referenced by other modules --%>
/*	icasConfig = {
		multiuser: <%= offering.isMultiuser() %>,
		urlPath: "<%= offeringId %>",
		contextPath: "<%= contextPath %>"
	};
*/
    dojoConfig= {
        has: {
            "dojo-firebug": true
        },
        packages: [
            { name: "platform", location: "<%=contextPath%>/js/platform" }
        ],
        parseOnLoad: true,
        async: true
    };
</script>
<script src="//ajax.googleapis.com/ajax/libs/dojo/1.9.1/dojo/dojo.js"></script>
<script>
	require(
		["dojo/parser", "platform/capacity", "dojo/domReady!"],
		function(parser){
			parser.parse();
		}
	);
</script>

<div id="build-info">
<p>Platform version: <%= cpeVersion %> / <%= cpeBuild%> | Plugin version: <%= pluginVersion %> / <%= pluginBuild%></p>
</div>

</div>
<!-- CONTENT_BODY_END -->
</div>
<!-- CONTENT_END -->
</div>
<!-- NAVIGATION_BEGIN -->

<!-- NAVIGATION_END -->
</div>

<!-- FOOTER_BEGIN -->
<div id="ibm-footer-module"></div>
<div id="ibm-footer">
	<h2 class="ibm-access">Footer links</h2>
	<ul>
		<li><a href="http://www.ibm.com/contact/us/en/">Contact</a></li>
		<li><a href="http://www.ibm.com/privacy/us/en/">Privacy</a></li>
		<li><a href="http://www.ibm.com/legal/us/en/">Terms of use</a></li>
		<li><a href="http://www.ibm.com/accessibility/us/en/">Accessibility</a></li>
	</ul>
</div>
<!-- FOOTER_END -->

</div>
</body>
</html>
