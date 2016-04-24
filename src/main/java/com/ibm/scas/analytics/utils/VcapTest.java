package com.ibm.scas.analytics.utils;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ibm.scas.analytics.persistence.mysql.MySqlCredentials;
import com.ibm.scas.analytics.persistence.mysql.MySqlServiceConfig;

public class VcapTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String filename = "/Users/chenhan/tmp/vcap.json";
		FileInputStream is = new FileInputStream(filename);
		Gson gson = new Gson();
		Map<String, MySqlServiceConfig[]> map = gson.fromJson(new InputStreamReader(is), new TypeToken<Map<String, MySqlServiceConfig[]>>() {}.getType());

		MySqlCredentials creds = map.get("mysql-5.5")[0].getCredentials();
		String dbName = creds.getName();
		String hostname = creds.getHostname();
		int port = creds.getPort();
		System.out.println("db name: " + dbName);
		System.out.println("hostname: " + hostname);
		System.out.println("port: " + port);
	}

}
