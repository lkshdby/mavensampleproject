package com.ibm.pcmae.test;

import org.apache.log4j.Logger;

import com.ibm.pcmae.cluster.beans.ClusterDetails;
import com.ibm.pcmae.cluster.beans.Message;

public class CancelCluster extends APITestAdapter {
	private static final Logger logger = Logger.getLogger(CancelCluster.class);

	public CancelCluster() {
	}

	public void run() {
		String id = "2c9e8790-424e0880-0142-f7f513a4-4ffc";

		logger.info("------------------------------------------------------");
		ClusterDetails details = api.getClusterDetails(id);
		if (details == null) {
			logger.info("Not found!");
			return;
		}
		logger.info("Cluster details for " + id);
		logger.info(" name: " + details.getName());
		logger.info(" description: " + details.getDescription());
		logger.info(" state: " + details.getState());
		logger.info(" application action: " + details.getApplicationAction());
		
		logger.info("------------------------------------------------------");
		Message message = api.cancelCluster(id);
		logger.info("result: " + message.getType());
		logger.info("message: " + message.getMessage());
		
		logger.info("------------------------------------------------------");
		details = api.getClusterDetails(id);
		if (details == null) {
			logger.info("Not found!");
			return;
		}
		logger.info("Cluster details for " + id);
		logger.info(" state: " + details.getState());
		logger.info(" application action: " + details.getApplicationAction());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new CancelCluster().run();
	}

}
