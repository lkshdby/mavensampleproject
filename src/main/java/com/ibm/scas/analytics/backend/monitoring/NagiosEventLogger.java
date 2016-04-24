package com.ibm.scas.analytics.backend.monitoring;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import com.ibm.scas.analytics.EngineProperties;

public class NagiosEventLogger {
	private static final Logger logger = Logger.getLogger(NagiosEventLogger.class);
	
	private static String logFile;
	private static String hostname;
	private static String serviceName = "CPECluster";
	
	private final static int OK = 0;
	private final static int WARNING = 1;
	private final static int CRITICAL = 2;
	private final static int UNKNOWN = 3;

	static {
		logFile = EngineProperties.getInstance().getProperty(EngineProperties.NAGIOS_LOG_FILE, EngineProperties.DEFAULT_NAGIOS_LOG_FILE);
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			logger.error("Failed to determine hostname, using a hardcoded one instead.", e);
			hostname = "uswdc01hpc00011ianra.analytics.ibmcloud.com";
		}
	}
	
	public NagiosEventLogger() {
	}

	public void emitOK(String message) {
		emitEvent(OK, message);
	}

	public void emitWarning(String message) {
		emitEvent(WARNING, message);
	}

	public void emitCritical(String message) {
		emitEvent(CRITICAL, message);
	}

	public void emitUnknown(String message) {
		emitEvent(UNKNOWN, message);
	}

	protected void emitEvent(int code, String message) {
		try {
			long now = System.currentTimeMillis() / 1000;
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));
			StringBuffer sb = new StringBuffer();
			sb.append(now).append('\t');
			sb.append(hostname).append("\t");
			sb.append(serviceName).append("\t");
			sb.append(code).append("\t");
			sb.append(message);
			
			out.println(sb.toString());
			out.close();
		} catch (IOException e) {
			logger.error("Cannot write to Nagios event log", e);
		}
	}
}
