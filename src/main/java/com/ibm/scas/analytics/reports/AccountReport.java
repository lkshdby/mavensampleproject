package com.ibm.scas.analytics.reports;

import java.io.PrintWriter;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ibm.scas.analytics.persistence.PersistenceService;
import com.ibm.scas.analytics.persistence.beans.Account;
import com.ibm.scas.analytics.persistence.beans.Offering;
import com.ibm.scas.analytics.persistence.beans.Subscriber;
import com.ibm.scas.analytics.persistence.util.WhereClause;
import com.ibm.scas.analytics.provider.ServiceProvider;
import com.ibm.scas.analytics.provider.impl.ServiceProviderPluginFactory;
import com.ibm.scas.analytics.utils.CPEException;

@Singleton
public class AccountReport {
	private static final Logger logger = Logger.getLogger(AccountReport.class);
	
	@Inject PersistenceService persistence;
	@Inject ServiceProviderPluginFactory pluginFactory;
	
	public AccountReport() {
	}

	public void generate(PrintWriter writer) throws CPEException {
		final List<Account> accounts = persistence.getAllObjects(Account.class);
		int totalCanonicalNodes = 0;
		for (Account account : accounts) {
			if (Account.SYSTEM_PARTNER.equals(account.getPartner())) {
				continue;
			}
			int state = account.getState();
			if (state != Account.ACTIVE && state != Account.FREE_TRIAL) {
				continue;
			}
			String accountId = account.getId();
			String offeringId = account.getOffering().getId();
			String edition = account.getEdition();
			int quantity = account.getQuantity();
			
			List<Subscriber> users = persistence.getObjectsBy(Subscriber.class, new WhereClause("account.id", accountId));
			Offering offering = persistence.getObjectById(Offering.class, offeringId);
			int nodeSize = 1;
			try {
				ServiceProvider plugin = pluginFactory.getPlugin(offeringId);
				nodeSize = plugin.getEditionNodeSize(edition);
			} catch (Exception e) {
				logger.error("Exception retrieving node size for offering: " + offeringId + ", edition: " + edition, e);
			}
			int canonicalNodes = quantity * nodeSize;
			totalCanonicalNodes += canonicalNodes;
			
			writer.println("Account: " + accountId);
			writer.println(" - Offering: [" + offeringId +"] " + offering.getName());
			writer.println(" - Edition: " + edition);
			writer.print(" - Users: ");
			for (int i = 0; i < users.size(); i ++) {
				Subscriber user = users.get(i);
				if (i > 0) {
					writer.print(", ");
				}
				writer.print(user.getName());
			}
			writer.println();
			writer.println(" - Nominal nodes: " + quantity);
			writer.println(" - Canonical nodes: " + canonicalNodes);
			writer.println();
		}
		writer.println("Total canonical nodes sold: " + totalCanonicalNodes);
	}
}
