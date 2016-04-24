package com.ibm.scas.analytics.backend.audit;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.ibm.scas.analytics.EngineProperties;

public class AuditEventLogger {
	private static final Logger logger = Logger.getLogger(AuditEventLogger.class);
	
	private static String logFile;

	private static final SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");

	private static final String LOGIN = "login";
	
	static {
		logFile = EngineProperties.getInstance().getProperty(EngineProperties.AUDIT_LOG_FILE, EngineProperties.DEFAULT_AUDIT_LOG_FILE);
	}

	public void recordLogin(String remoteHost, String companyName, String firstName, String lastName, String openId, String offeringId, String accountId) {
		StringBuilder sb = new StringBuilder();
		sb.append(remoteHost).append(',');
		sb.append(offeringId).append(',');
		sb.append(accountId).append(',');
		sb.append(companyName).append(',');
		sb.append(lastName).append(',');
		sb.append(firstName).append(',');
		sb.append(openId);
		
		writeAuditEvent(LOGIN, sb.toString());
	}
	
	protected void writeAuditEvent(String type, String message) {
		try {
			String time = isoDateFormat.format(new Date());
			StringBuilder sb = new StringBuilder();
			sb.append(time).append(',');
			sb.append(type).append(',');
			sb.append(message);
			
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));
			out.println(sb.toString());
			out.close();
		} catch (IOException e) {
			logger.error("Cannot write to audit event log", e);
		}
	}
}
