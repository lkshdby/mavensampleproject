package com.ibm.scas.analytics.test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.ibm.scas.analytics.persistence.PersistenceException;
import com.ibm.scas.analytics.persistence.beans.Offering;
import com.ibm.scas.analytics.utils.ExpiredPromoCodeException;
import com.ibm.scas.analytics.utils.InvalidPromoCodeException;
import com.ibm.scas.analytics.utils.PromoCode;

public class TestPromoCode extends BaseTestCase {
	private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		executeSQLScript("populate_fake_data.sql");
	}
	
	public void testExpiredPromoCode() {
		String offeringId = "biginsights";
		String email = "Kumar.Amit@bcg.com";
//		long expirationTime = 0;
//		long expirationTime = System.currentTimeMillis() - 3600000L;
		long expirationTime = System.currentTimeMillis() - 3600000L;
		
//		String promoCode = "5a5c72a8147b8e90f69b49f1938b31e3285de9fc886ad85e";
//		try {
//			validate(offeringId, email, promoCode);
//		} catch (InvalidPromoCodeException e) {
//			e.printStackTrace();
//		} catch (ExpiredPromoCodeException e) {
//			e.printStackTrace();
//		}

		for (int i = 0; i < 100; i++) {
			String promoCode = PromoCode.generate(offeringId, email, expirationTime);
			String exp = "";
			try {
				long e = PromoCode.extractExpiration(promoCode);
				if (e == 0) {
					exp = "NEVER";
				} else {
					exp = df.format(new Date(e));
				}
				PromoCode.validate(offeringId, email, promoCode);
				Logger.getRootLogger().info(promoCode + ": " + exp + " -> valid");
				
				assertTrue(false);
			} catch (InvalidPromoCodeException e) {
				Logger.getRootLogger().info(promoCode + ": " + exp + " -> invalid");
				assertTrue(false);
			} catch (ExpiredPromoCodeException e) {
				Logger.getRootLogger().info(promoCode + ": " + exp + " -> expired");
			}
		}
	}

	/**
	 * @param args
	 * @throws PersistenceException 
	 */
	public void testNoExpirePromoCode() throws PersistenceException {
		final List<Offering> offering = service.getAllObjects(Offering.class);
		
		String offeringId = offering.get(0).getId();
		String email = "jkwong@ca.ibm.com";
		long expirationTime = PromoCode.NO_EXPIRATION;
		String promoCode = PromoCode.generate(offeringId, email, expirationTime);
		try {
			Logger.getRootLogger().info("Offering Id: " + offeringId);
			Logger.getRootLogger().info("email: " + email);
			Logger.getRootLogger().info("Promo code: " + promoCode);
			long exp = PromoCode.extractExpiration(promoCode);
			if (exp == 0) {
				System.out.println(" - expiration: NEVER");
			} else {
				System.out.println(" - expiration: " + df.format(new Date(exp)));
			}
			assertTrue(exp == 0);
		} catch(InvalidPromoCodeException e) {
			System.err.println("Internal error! Failed to validate promo code.");
		}

	}
}