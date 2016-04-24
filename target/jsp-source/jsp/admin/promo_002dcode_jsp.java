/*
 * Generated by the Jasper component of Apache Tomcat
 * Version: JspCServletContext/1.0
 * Generated at: 2016-04-24 02:14:09 UTC
 * Note: The last modified time of this file was set to
 *       the last modified time of the source file after
 *       generation to assist with modification tracking.
 */
package jsp.admin;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import com.google.inject.Injector;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import com.ibm.scas.analytics.utils.PromoCode;
import com.ibm.scas.analytics.persistence.PersistenceService;
import com.ibm.scas.analytics.persistence.beans.Offering;
import java.util.List;
import com.ibm.scas.analytics.EngineProperties;
import java.util.Enumeration;

public final class promo_002dcode_jsp extends org.apache.jasper.runtime.HttpJspBase
    implements org.apache.jasper.runtime.JspSourceDependent {

  private static final javax.servlet.jsp.JspFactory _jspxFactory =
          javax.servlet.jsp.JspFactory.getDefaultFactory();

  private static java.util.Map<java.lang.String,java.lang.Long> _jspx_dependants;

  private javax.el.ExpressionFactory _el_expressionfactory;
  private org.apache.tomcat.InstanceManager _jsp_instancemanager;

  public java.util.Map<java.lang.String,java.lang.Long> getDependants() {
    return _jspx_dependants;
  }

  public void _jspInit() {
    _el_expressionfactory = _jspxFactory.getJspApplicationContext(getServletConfig().getServletContext()).getExpressionFactory();
    _jsp_instancemanager = org.apache.jasper.runtime.InstanceManagerFactory.getInstanceManager(getServletConfig());
  }

  public void _jspDestroy() {
  }

  public void _jspService(final javax.servlet.http.HttpServletRequest request, final javax.servlet.http.HttpServletResponse response)
        throws java.io.IOException, javax.servlet.ServletException {

    final javax.servlet.jsp.PageContext pageContext;
    javax.servlet.http.HttpSession session = null;
    final javax.servlet.ServletContext application;
    final javax.servlet.ServletConfig config;
    javax.servlet.jsp.JspWriter out = null;
    final java.lang.Object page = this;
    javax.servlet.jsp.JspWriter _jspx_out = null;
    javax.servlet.jsp.PageContext _jspx_page_context = null;


    try {
      response.setContentType("text/html; charset=ISO-8859-1");
      pageContext = _jspxFactory.getPageContext(this, request, response,
      			null, true, 8192, true);
      _jspx_page_context = pageContext;
      application = pageContext.getServletContext();
      config = pageContext.getServletConfig();
      session = pageContext.getSession();
      out = pageContext.getOut();
      _jspx_out = out;

      out.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\r\n<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en-US\" xml:lang=\"en-US\">\r\n");

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

      out.write("<head>\r\n\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\r\n\t<link rel=\"schema.DC\" href=\"http://purl.org/DC/elements/1.0/\"/>\r\n\t<link rel=\"SHORTCUT ICON\" href=\"http://www.ibm.com/favicon.ico\"/>\r\n\r\n\t<meta name=\"DC.Rights\" content=\"? Copyright IBM Corp. 2011\" />\r\n\t<meta name=\"Keywords\" content=\"Click to accept\" />\r\n\t<meta name=\"DC.Date\" scheme=\"iso8601\" content=\"2011-07-05\" />\r\n\t<meta name=\"Source\" content=\"v17 Template Generator, Template 17.02\" />\r\n\t<meta name=\"Security\" content=\"Public\" />\r\n\t<meta name=\"Abstract\" content=\"Click to accept\" />\r\n\t<meta name=\"IBM.Effective\" scheme=\"W3CDTF\" content=\"2011-08-05\" />\r\n\t<meta name=\"DC.Subject\" scheme=\"IBM_SubjectTaxonomy\" content=\"IBM_SubjectTaxonomy\" />\r\n\t<meta name=\"Owner\" content=\"REPLACE_ME@us.ibm.com\" />\r\n\t<meta name=\"DC.Language\" scheme=\"rfc1766\" content=\"en-US\" />\r\n\t<meta name=\"IBM.Country\" content=\"US\" />\r\n\t<meta name=\"Robots\" content=\"index,follow\" />\r\n\t<meta name=\"DC.Type\" scheme=\"IBM_ContentClassTaxonomy\" content=\"CT101IBM_SubjectTaxonomy\" />\r\n");
      out.write("\t<meta name=\"Description\" content=\"ICAS Analytics Platform\" />\r\n\t \r\n\t<title>");
      out.print(pageTitle);
      out.write("</title>\r\n\t<link href=\"//1.www.s81c.com/common/v17/css/www.css\" rel=\"stylesheet\" title=\"www\" type=\"text/css\" />\r\n\t<link rel=\"stylesheet\" href=\"");
      out.print(contextPath);
      out.write("/theme/icas-analytics.css\" title=\"www\" type=\"text/css\" />\r\n\t<link rel=\"stylesheet\" href=\"//ajax.googleapis.com/ajax/libs/dojo/1.9.1/dijit/themes/claro/claro.css\" media=\"screen\"/>\r\n\t<link rel=\"stylesheet\" href=\"//ajax.googleapis.com/ajax/libs/dojo/1.9.1/dojox/grid/resources/Grid.css\" />\r\n\t<link rel=\"stylesheet\" href=\"//ajax.googleapis.com/ajax/libs/dojo/1.9.1/dojox/grid/resources/claroGrid.css\" />\r\n\r\n<!-- \r\n \t<script src=\"//1.www.s81c.com/common/js/dojo/www.js\" type=\"text/javascript\">//</script>\r\n -->\r\n</head>\r\n\r\n<body id=\"ibm-com\" class=\"claro\">\r\n<div id=\"ibm-top\" class=\"ibm-application\">\r\n\r\n<!-- MASTHEAD_BEGIN -->\r\n<div id=\"ibm-masthead\">\r\n\t<div id=\"ibm-mast-options\">\r\n\t<ul>\r\n\t\t<li id=\"ibm-geo\"><a\r\n\t\t\thref=\"http://www.ibm.com/planetwide/select/selector.html\"><span\r\n\t\t\t\tclass=\"ibm-access\">Select a country/region: </span>United States</a></li>\r\n\t\t<li id=\"ibm-sso\" role=\"presentation\" class=\"\"><span dojoattachpoint=\"containerNode\" widgetid=\"ibmweb_dynnav_greeting_0\">\r\n\t\t\t\t<p dojoattachpoint=\"welcomeMessageNode\" id=\"ibm-welcome-msg\">Welcome [");
      out.print( companyName );
      out.write(']');
      out.write(' ');
      out.print( firstName );
      out.print( lastName );
      out.write("</p> \r\n\t\t</span></li>\r\n\t</ul>\r\n\t</div>\r\n\t<div id=\"ibm-universal-nav\">\r\n\t\t<ul id=\"ibm-unav-links\">\r\n\t\t\t<li id=\"ibm-home\"><a href=\"http://www.ibm.com/us/en/\"\r\n\t\t\t\ttabindex=\"0\">IBM®</a></li>\r\n\t\t</ul>\r\n\t\t<div class=\"ibm-masthead-tools\">\r\n\t\t\t<div id=\"ibm-mast-options\"></div>\r\n\t\t</div>\r\n\r\n\t\t<div id=\"ibm-secondhead\" class=\"ibm-alternate-masthead\" role=\"banner\">\r\n\t\t\t<div id=\"ibm-leadspace-body\">\r\n\t\t\t\t<ul id=\"ibm-outlinks\">\r\n\t\t\t\t\t<li><a id=\"ibm-cloud-link\"\r\n\t\t\t\t\t\thref=\"http://www.ibm.com/cloud/marketplace\">IBM Cloud</a></li>\r\n\t\t\t\t\t<li><a id=\"ibm-marketplace-link\"\r\n\t\t\t\t\t\thref=\"");
      out.print(marketplaceUrl);
      out.write("\">marketplace</a></li>\r\n\t\t\t\t</ul>\r\n\t\t\t</div>\r\n\t\t</div>\r\n\t</div>\r\n</div>\r\n\r\n<!-- MASTHEAD_END -->\r\n\r\n<div id=\"ibm-pcon\">\r\n  \r\n<!-- CONTENT_BEGIN -->\r\n<div id=\"ibm-content\">\r\n<div id=\"ibm-content-body\">\r\n<div id=\"ibm-content-main\" role=\"main\">\r\n\r\n<!-- \r\n--------------------------------------------------------------------------------------\r\n  Main page \r\n--------------------------------------------------------------------------------------\r\n-->\r\n<div id=\"promo_code_container\">\r\n\r\n<form data-dojo-type=\"dijit/form/Form\" method=\"post\">\r\n\t\t<script type=\"dojo/method\" event=\"onSubmit\">\r\n\t\t\treturn this.validate();\r\n\t\t</script>\r\n<div class=\"ibm-columns\" style=\"padding-top: 40px; overflow: visible\">\r\n\t<div class=\"ibm-col-6-6\">\r\n\r\n\t\t<span style=\"font-face: Helvetica Neue Light; font-size: 28px\">Promo code generator</span><br/>\r\n\r\n\t\t<hr></hr>\r\n\t\t<p class=\"heading\">To generate a promo code follow these steps:</p>\r\n\t\t<div style=\"padding-left: 30px\">\r\n\t\t<p>1) Select a service offering<br/>2) Enter the customer's email address<br/>3) Select an end date for the trial period<br/>4) Click the \"Generate\"\" button</p>\r\n");
      out.write("\t\t</div>\r\n\t\t<div id=\"main_form\" style=\"display:none\">\r\n\t\t<table role=\"presentation\">\r\n\t\t<tr><td><p class=\"parameter_name\"><label for=\"offering_id\">Offering name:</label></p></td><td>\r\n\t\t <select name=\"offering_id\" id=\"offering_id\" data-dojo-type=\"dijit/form/FilteringSelect\">\r\n");

for (Offering o : offerings) {
	String oId = o.getId();
	String oName = o.getName();

      out.write("<option value=\"");
      out.print(oId);
      out.write('"');
      out.write(' ');
      out.print((oId.equals(offering) ? "selected" : "") );
      out.write('>');
      out.write('[');
      out.print(oId);
      out.write(']');
      out.write(' ');
      out.print(oName);
      out.write("</option>\r\n");

}

      out.write("</select></td></tr>\r\n");
      out.write("<tr><td><p class=\"parameter_name\"><label for=\"customer_email\">Customer email:</label></p></td><td>\r\n \t\t<input style=\"width: 400px\" name=\"customer_email\" id=\"customer_email\" data-dojo-type=\"dijit/form/ValidationTextBox\"\r\n\t\t\t\t\tvalidator=\"dojox.validate.isEmailAddress\" size=\"40\" required=\"true\" value=\"");
      out.print(customerEmail );
      out.write("\" type=\"text\" />\r\n \t\t</td></tr>\r\n\t\t<tr><td><p class=\"parameter_name\"><label for=\"trial_expires\">Trial expires?:</label></p></td><td>\r\n \t\t<input name=\"trial_expires\" type=\"checkbox\" id=\"trial_expires\" ");
      out.print(trialExpires ? "checked" : "");
      out.write(" data-dojo-type=\"dijit/form/CheckBox\" />\r\n \t\t</td></tr>\r\n\t\t<tr id=\"end_date_row\" style=\"display:none\"><td><p class=\"parameter_name\"><label for=\"end_date\">Trial end date:</label></p></td><td>\r\n \t\t<input style=\"width: 200px\" name=\"end_date\" id=\"end_date\" size=\"40\" value=\"");
      out.print(endDate);
      out.write("\" type=\"text\" \r\n \t\tdata-dojo-type=\"dijit/form/DateTextBox\" data-dojo-props='constraints:{min: \"");
      out.print(minEndDate);
      out.write("\"}' required=\"true\"/>\r\n \t\t</td></tr>\r\n \t\t<tr><td></td><td align=\"center\"><input value=\"Generate\" label=\"Generate\" type=\"submit\" class=\"ibm_submit_btn\"  data-dojo-type=\"dijit/form/Button\"/></td></tr>\r\n");
 if (promoCode.length() > 0) { 
      out.write("<tr><td><p class=\"parameter_name\">Promo code:</p></td><td>\r\n \t\t<p class=\"parameter_value\">");
      out.print(promoCode);
      out.write("</p>\r\n \t\t</td></tr>\r\n");
 } 
      out.write("</table>\r\n\t\t</div>\r\n\t</div>\r\n</div>\r\n</form>\r\n\r\n</div>\r\n\r\n</div>\r\n<!-- CONTENT_BODY_END -->\r\n</div>\r\n<!-- CONTENT_END -->\r\n</div>\r\n<!-- NAVIGATION_BEGIN -->\r\n\r\n<!-- NAVIGATION_END -->\r\n</div>\r\n\r\n<script>\r\n    dojoConfig= {\r\n        has: {\r\n            \"dojo-firebug\": true\r\n        },\r\n        packages: [\r\n            { name: \"platform\", location: \"");
      out.print(contextPath);
      out.write("/js/platform\" }\r\n        ],\r\n        parseOnLoad: true,\r\n        async: true\r\n    };\r\n</script>\r\n<script src=\"//ajax.googleapis.com/ajax/libs/dojo/1.9.1/dojo/dojo.js\"></script>\r\n<script>\r\nrequire([\"dojo/ready\", \"dojo/on\", \"dojo/dom\", \"dijit/registry\", \"dojo/parser\", \"dojox/validate/us\", \"dojox/validate/web\", \r\n         \"dijit/form/DateTextBox\", \"dijit/form/FilteringSelect\", \"dijit/form/ValidationTextBox\",\r\n         \"dijit/form/Button\", \"dojo/domReady!\"], function(ready, on, dom, registry, parser) {\r\n\t\r\n\tvar updateEndDateRow = function() {\r\n\t\tvar checked = registry.byId(\"trial_expires\").get(\"checked\");\r\n\t\tvar endDateRow = dom.byId(\"end_date_row\");\r\n\t\tendDateRow.style.display = checked ? \"\" : \"none\";\r\n\t};\r\n\t\r\n\tready(function(){\r\n//\t\tparser.parse();\r\n\t\tvar mainForm = dom.byId(\"main_form\");\r\n\t\tmainForm.style.display = \"block\";\r\n\r\n\t\tregistry.byId(\"trial_expires\").on(\"change\", updateEndDateRow);\r\n\t\t\r\n\t\tupdateEndDateRow();\r\n\t});\r\n});\r\n</script>\r\n\r\n<!-- FOOTER_BEGIN -->\r\n<div id=\"ibm-footer-module\"></div>\r\n<div id=\"ibm-footer\">\r\n");
      out.write("\t<h2 class=\"ibm-access\">Footer links</h2>\r\n\t<ul>\r\n\t\t<li><a href=\"http://www.ibm.com/contact/us/en/\">Contact</a></li>\r\n\t\t<li><a href=\"http://www.ibm.com/privacy/us/en/\">Privacy</a></li>\r\n\t\t<li><a href=\"http://www.ibm.com/legal/us/en/\">Terms of use</a></li>\r\n\t\t<li><a href=\"http://www.ibm.com/accessibility/us/en/\">Accessibility</a></li>\r\n\t</ul>\r\n</div>\r\n<!-- FOOTER_END -->\r\n\r\n</div>\r\n</body>\r\n</html>\r\n");
    } catch (java.lang.Throwable t) {
      if (!(t instanceof javax.servlet.jsp.SkipPageException)){
        out = _jspx_out;
        if (out != null && out.getBufferSize() != 0)
          try { out.clearBuffer(); } catch (java.io.IOException e) {}
        if (_jspx_page_context != null) _jspx_page_context.handlePageException(t);
        else throw new ServletException(t);
      }
    } finally {
      _jspxFactory.releasePageContext(_jspx_page_context);
    }
  }
}