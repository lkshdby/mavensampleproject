package com.ibm.scas.analytics.reports;

import com.google.inject.Provider;

public class AccountReportProvider implements Provider<AccountReport> {
	// Guice uses this class to generate AccountReport, with dependencies injected

	@Override
	public AccountReport get() {
		return new AccountReport();
	}

}
