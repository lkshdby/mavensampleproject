package com.ibm.pcmae.test;

import com.ibm.pcmae.ClusterAPI;
import com.ibm.pcmae.client.PassthruClusterAPI;

public class APITestAdapter {
	protected ClusterAPI api;

	public APITestAdapter() {
		String url = "https://mapreduce.beta.manage.softlayer.com:8443/pcmwebapi-1.0/rest";
		api = new PassthruClusterAPI(url);
		api.setCredentials("2c9e8790-424e0880-0142-d853be03-3fff", "cluster-provisioning-engine", "scas2analytics");
	}
}
