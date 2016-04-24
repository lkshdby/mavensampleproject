package com.ibm.scas.analytics.test;

import java.util.List;

import org.apache.log4j.Logger;

import com.ibm.scas.analytics.backend.SoftLayerAPIGateway;
import com.ibm.scas.analytics.backend.SoftLayerAccountProvider;
import com.ibm.scas.analytics.beans.SoftLayerAccount;

public class TestSoftLayerAccountService extends BaseTestCase {
	private Logger logger = Logger.getLogger(TestSoftLayerAccountService.class);

	public void testAddGoodSoftLayerAccount() throws Exception {
		logger.info("****** testAddGoodSoftLayerAccount()");
		final SoftLayerAccountProvider acctProvider = injector.getInstance(SoftLayerAccountProvider.class);
		
		// add my SL credentials, these actually work
		final SoftLayerAccount slAccount = new SoftLayerAccount();
		slAccount.setUrl(SoftLayerAPIGateway.PUBLIC_REST_API_URL);
		slAccount.setUsername("ldubey.3@us.ibm.com");
		slAccount.setApiKey("b5e5d43e39099f08347bf12b3e68da7671345741ca6aee2b779d760d126ff9c0");
		
		final String acctId = acctProvider.createSoftLayerAccount(slAccount);
		
		assertTrue("Account was created!", true);
		
		final SoftLayerAccount myAcct = acctProvider.getSoftLayerAccountById(acctId);
		assertTrue("Account ID matches", myAcct.getId().equals(acctId));
		assertTrue("Account URL matches", myAcct.getUrl().equals(slAccount.getUrl()));
		assertTrue("Account username matches", myAcct.getUsername().equals(slAccount.getUsername()));
		assertTrue("Account api key matches", myAcct.getApiKey().equals(slAccount.getApiKey()));
		
		// now delete!
		acctProvider.deleteSoftLayerAccount(acctId);
		
		final List<SoftLayerAccount> allAccounts = acctProvider.getSoftLayerAccountDetails();
		for (final SoftLayerAccount acct : allAccounts) {
			assertTrue("Account " + acct.getId() + " is not deleted account " + acctId, !acct.getId().equals(acctId));
		}
	}
	
//	public void testAddBadSoftLayerAccounts() throws Exception {
//		logger.info("****** testAddBadSoftLayerAccount()");
//		final SoftLayerAccountProvider acctProvider = injector.getInstance(SoftLayerAccountProvider.class);
//		
//		// add SL credentials with bad URL, these shouldn't work
//		final SoftLayerAccount slAccount = new SoftLayerAccount();
//		slAccount.setUrl("http://microsoft.com/some/bad/path");
//		slAccount.setUsername("jeffkwong@ca.ibm.com");
//		slAccount.setApiKey("63eaacd492cec6e8631d432389c8c505de4790efcaa8e1f5a36da917f46f0036");
//		
//		try {
//			acctProvider.createSoftLayerAccount(slAccount);
//			assertTrue("Bad URL was added!", false);
//		} catch (CPEException e) {
//			assertTrue("Bad URL was rejected", true);
//		}
//		
//		// now try a bad API key
//		slAccount.setUrl(SoftLayerAPIGateway.PUBLIC_REST_API_URL);
//		slAccount.setApiKey("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
//		try {
//			acctProvider.createSoftLayerAccount(slAccount);
//			assertTrue("Bad api key was added!", false);
//		} catch (CPEException e) {
//			assertTrue("Bad URL api key rejected", true);
//		}
//
//	}

}
