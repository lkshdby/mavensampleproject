<%@page import="com.google.inject.Injector"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.ibm.scas.analytics.utils.PromoCode"%>
<%@page import="com.ibm.scas.analytics.persistence.PersistenceService"%>
<%@page import="com.ibm.scas.analytics.persistence.beans.Offering"%>
<%@page import="java.util.List"%>
<%@page import="com.ibm.scas.analytics.EngineProperties"%>
<%@page import="java.util.Enumeration"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en-US" xml:lang="en-US">
<%
String marketplaceUrl = EngineProperties.getInstance().getProperty(EngineProperties.MARKETPLACE_URL);
//boolean testMode = "true".equalsIgnoreCase(EngineProperties.getInstance().getProperty(EngineProperties.TEST_MODE));
String contextPath = request.getContextPath();

String pageTitle = "Admin Console";
String companyName = "IBM";
String firstName = "Administrator";
String lastName = "";

SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

String promoCode = "";
String offering = null;
String customerEmail = "";
String endDate = "";
String minEndDate = "";

boolean trialExpires = false;

if (request.getMethod().equalsIgnoreCase("POST")) {
	offering = request.getParameter("offering_id");
	customerEmail = request.getParameter("customer_email");
	endDate = request.getParameter("end_date");
	trialExpires = request.getParameter("trial_expires") != null;

	
	long expirationTime = 0;
	if (trialExpires) {
		if (endDate.length() > 0) {
			expirationTime = df.parse(endDate).getTime();
			expirationTime += 86340000;
		}
	}
//	System.out.println("offeringId: " + offering);
//	System.out.println("customerEmail: " + customerEmail);
	
	promoCode = PromoCode.generate(offering, customerEmail, expirationTime);
	expirationTime = PromoCode.extractExpiration(promoCode);
	String expStr = expirationTime == 0 ? "Never" : df.format(new Date(expirationTime));
	promoCode += " (Expiration: " + expStr + ")";
} else {
	Date now = new Date();
	Calendar cal = Calendar.getInstance();
	cal.setTime(now);
	cal.add(Calendar.MONTH, 1);
	endDate = df.format(cal.getTime());	
	
	cal.setTime(now);
	cal.add(Calendar.DAY_OF_MONTH, 1);
	minEndDate = df.format(cal.getTime());	
}

// TODO: use a controller class instead of injector/persistence service directly here
final Injector inj = (Injector) pageContext.getServletContext().getAttribute(Injector.class.getName());
final PersistenceService persistence = inj.getInstance(PersistenceService.class);
List<Offering> offerings = persistence.getAllObjects(Offering.class);
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
	 
	<title><%=pageTitle%></title>
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

<!-- 
--------------------------------------------------------------------------------------
  Main page 
--------------------------------------------------------------------------------------
-->
<div id="promo_code_container">

<form data-dojo-type="dijit/form/Form" method="post">
		<script type="dojo/method" event="onSubmit">
			return this.validate();
		</script>
<div class="ibm-columns" style="padding-top: 40px; overflow: visible">
	<div class="ibm-col-6-6">

		<span style="font-face: Helvetica Neue Light; font-size: 28px">Promo code generator</span><br/>

		<hr></hr>
		<p class="heading">To generate a promo code follow these steps:</p>
		<div style="padding-left: 30px">
		<p>1) Select a service offering<br/>2) Enter the customer's email address<br/>3) Select an end date for the trial period<br/>4) Click the "Generate"" button</p>
		</div>
		<div id="main_form" style="display:none">
		<table role="presentation">
		<tr><td><p class="parameter_name"><label for="offering_id">Offering name:</label></p></td><td>
		 <select name="offering_id" id="offering_id" data-dojo-type="dijit/form/FilteringSelect">
<%
for (Offering o : offerings) {
	String oId = o.getId();
	String oName = o.getName();
%>
		<option value="<%=oId%>" <%=(oId.equals(offering) ? "selected" : "") %>>[<%=oId%>] <%=oName%></option>
<%
}
%>
		</select></td></tr>
<%-- 		<p><span class="parameter_name">Your name:</span> <span class="parameter_value"><%= firstName %> <%= lastName %></span></p>
 --%>
		<tr><td><p class="parameter_name"><label for="customer_email">Customer email:</label></p></td><td>
 		<input style="width: 400px" name="customer_email" id="customer_email" data-dojo-type="dijit/form/ValidationTextBox"
					validator="dojox.validate.isEmailAddress" size="40" required="true" value="<%=customerEmail %>" type="text" />
 		</td></tr>
		<tr><td><p class="parameter_name"><label for="trial_expires">Trial expires?:</label></p></td><td>
 		<input name="trial_expires" type="checkbox" id="trial_expires" <%=trialExpires ? "checked" : ""%> data-dojo-type="dijit/form/CheckBox" />
 		</td></tr>
		<tr id="end_date_row" style="display:none"><td><p class="parameter_name"><label for="end_date">Trial end date:</label></p></td><td>
 		<input style="width: 200px" name="end_date" id="end_date" size="40" value="<%=endDate%>" type="text" 
 		data-dojo-type="dijit/form/DateTextBox" data-dojo-props='constraints:{min: "<%=minEndDate%>"}' required="true"/>
 		</td></tr>
 		<tr><td></td><td align="center"><input value="Generate" label="Generate" type="submit" class="ibm_submit_btn"  data-dojo-type="dijit/form/Button"/></td></tr>
<% if (promoCode.length() > 0) { %>
		<tr><td><p class="parameter_name">Promo code:</p></td><td>
 		<p class="parameter_value"><%=promoCode%></p>
 		</td></tr>
<% } %>
		</table>
		</div>
	</div>
</div>
</form>

</div>

</div>
<!-- CONTENT_BODY_END -->
</div>
<!-- CONTENT_END -->
</div>
<!-- NAVIGATION_BEGIN -->

<!-- NAVIGATION_END -->
</div>

<script>
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
require(["dojo/ready", "dojo/on", "dojo/dom", "dijit/registry", "dojo/parser", "dojox/validate/us", "dojox/validate/web", 
         "dijit/form/DateTextBox", "dijit/form/FilteringSelect", "dijit/form/ValidationTextBox",
         "dijit/form/Button", "dojo/domReady!"], function(ready, on, dom, registry, parser) {
	
	var updateEndDateRow = function() {
		var checked = registry.byId("trial_expires").get("checked");
		var endDateRow = dom.byId("end_date_row");
		endDateRow.style.display = checked ? "" : "none";
	};
	
	ready(function(){
//		parser.parse();
		var mainForm = dom.byId("main_form");
		mainForm.style.display = "block";

		registry.byId("trial_expires").on("change", updateEndDateRow);
		
		updateEndDateRow();
	});
});
</script>

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
