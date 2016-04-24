package com.ibm.scas.analytics.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.ibm.scas.analytics.EngineProperties;

public class PromoCode {
	private static final Logger logger = Logger.getLogger(PromoCode.class);

	private static final String MAC_NAME = "HmacSHA1";
	private static final int NONCE_BYTES = 4;
	private static final int EXPIRATION_BYTES = 4;
	private static final int HASH_TRUNCATE_BYTES = 8;
	
	/*
	 * promo code format NNNNNNNN-EEEEEEEE-HHHHHHHHHHHHHHHH
	 */
	private static final int PROMO_CODE_LENGTH = 2 * NONCE_BYTES + 1 + 2 * EXPIRATION_BYTES + 1 + 2 * HASH_TRUNCATE_BYTES;
	
	public static final long NO_EXPIRATION = 0L;
	
	private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final Random random = new Random(System.nanoTime());
	private static final String macKey = EngineProperties.getInstance().getProperty(EngineProperties.PROMO_CODE_KEY);
	
	private static byte[] maskBytes(byte[] nonce, byte[] input) throws NoSuchAlgorithmException, InvalidKeyException {
		SecretKey key = new SecretKeySpec(macKey.getBytes(), MAC_NAME);
		Mac mac = Mac.getInstance(MAC_NAME);
		mac.init(key);

		byte[] hash = mac.doFinal(nonce);
		byte[] output = new byte[input.length];
		for (int i = 0; i < input.length; i ++) {
			output[i] = (byte)(input[i] ^ hash[i]);
		}
		return output;
	}
	
	private static String sign(byte[] nonce, String offeringId, String email) throws NoSuchAlgorithmException, InvalidKeyException {
		SecretKey key = new SecretKeySpec(macKey.getBytes(), MAC_NAME);
		Mac mac = Mac.getInstance(MAC_NAME);
		mac.init(key);

		mac.update(nonce);
		byte[] body = (offeringId + ":" + email).getBytes();
		byte[] hash = mac.doFinal(body);
		return new String(Hex.encodeHex(hash)).substring(0, HASH_TRUNCATE_BYTES * 2);
	}

	
	private static byte[] createNonce(int len) {
		byte[] nonce = new byte[len];
		random.nextBytes(nonce);
		return nonce;
	}

	private static byte[] createNonce() {
		return createNonce(NONCE_BYTES);
	}

	private static byte[] longToBytes(long v, int len) {
		byte[] bytes = new byte[len];
		for (int i = 0; i < len; i ++) {
			bytes[i] = (byte) (v & 0xFF);
			v >>= 8;
		}
		return bytes;
	}
	
	private static long bytesToLong(byte[] bytes) {
		long v = 0;
		for (int i = bytes.length - 1; i >= 0; i --) {
			v = (v << 8) | ((long)bytes[i] & 0xFF);
		}
		return v;
	}
	
	public static String generate(String offeringId, String email) {
		return generate(offeringId, email, NO_EXPIRATION);
	}
	
	public static String generate(String offeringId, String email, long expirationTime) {
		try {
			if (logger.isDebugEnabled()) {
				String expStr = expirationTime == NO_EXPIRATION ? "NEVER" : df.format(new Date(expirationTime));
				logger.debug("Generating promo code, offering id: " + offeringId + ", email: " + email + ", expiration: " + expStr);
			}
			byte[] nonce = createNonce();
			byte[] exp = longToBytes(expirationTime / 60000L, EXPIRATION_BYTES); // reduce to minute resolution to fit in 4 bytes safely
			
			StringBuilder sb = new StringBuilder();
			sb.append(Hex.encodeHex(nonce));
			sb.append('-');
			sb.append(Hex.encodeHex(maskBytes(nonce, exp))); 
			sb.append('-');
			sb.append(sign(nonce, offeringId, email));
			
			String promoCode = sb.toString().toUpperCase();
			if (logger.isDebugEnabled()) {
				logger.debug(" - Promo code: " + promoCode);
			}
			return promoCode;
		} catch (Exception e) {
			logger.error("Failed to generate promo code", e);
			return "ERROR";
		}
	}

	public static void validate(String offeringId, String email, String promoCode) throws InvalidPromoCodeException, ExpiredPromoCodeException {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Validating promo code, offering id: " + offeringId + ", email: " + email + ", promo code: " + promoCode);
			}
			
			if (promoCode.length() != PROMO_CODE_LENGTH) {
				logger.info("Invalid promo code length.");
				throw new InvalidPromoCodeException();
			}
			
			/*
			 * segment the promo code into its components
			 */
			promoCode = promoCode.toLowerCase();
			String[] segments = promoCode.split("-");
			if (segments.length != 3) {
				throw new InvalidPromoCodeException();
			}
			String nonceSegment = segments[0];
			String expirationSegment = segments[1];
			String hashSegment = segments[2];
			if (nonceSegment.length() != 2 * NONCE_BYTES) {
				throw new InvalidPromoCodeException();
			}
			if (expirationSegment.length() != 2 * EXPIRATION_BYTES) {
				throw new InvalidPromoCodeException();
			}
			if (hashSegment.length() != 2 * HASH_TRUNCATE_BYTES) {
				throw new InvalidPromoCodeException();
			}
			
			/* 
			 * decode and verify
			 */
			byte[] nonce = Hex.decodeHex(nonceSegment.toCharArray());
			byte[] expBytes = maskBytes(nonce, Hex.decodeHex(expirationSegment.toCharArray()));
			long exp = bytesToLong(expBytes) * 60000L;
			boolean expired = exp > 0 && exp < System.currentTimeMillis();
			if (logger.isDebugEnabled()) {
				logger.debug("exp: " + exp);
				if (exp == 0) {
					logger.debug(" - expiration: NEVER");
				} else {
					logger.debug(" - expiration: " + df.format(new Date(exp)) + ", " + (expired ? " Expired" : " Valid"));
				}
			}
			if (expired) {
				throw new ExpiredPromoCodeException();
			}
			
			String hash = sign(nonce, offeringId, email);
			boolean valid =  hash.equals(hashSegment);
			if (logger.isDebugEnabled()) {
				logger.debug(" - " + (valid ? "Valid" : "Invalid"));
			}
			if (!valid) {
				throw new InvalidPromoCodeException();
			}
		} catch (DecoderException e) {
			logger.error("Failed to validate promo code: " + promoCode, e);
			throw new InvalidPromoCodeException(e);
		} catch (InvalidKeyException e) {
			logger.error("Failed to validate promo code: " + promoCode, e);
			throw new InvalidPromoCodeException(e);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Failed to validate promo code: " + promoCode, e);
			throw new InvalidPromoCodeException(e);
		}
	}
	
	public static long extractExpiration(String promoCode) throws InvalidPromoCodeException {
		try {
			if (promoCode.length() != PROMO_CODE_LENGTH) {
				logger.info("Invalid promo code length.");
				throw new InvalidPromoCodeException();
			}
			
			/*
			 * segment the promo code into its components
			 */
			promoCode = promoCode.toLowerCase();
			String[] segments = promoCode.split("-");
			if (segments.length != 3) {
				throw new InvalidPromoCodeException();
			}
			String nonceSegment = segments[0];
			String expirationSegment = segments[1];
			String hashSegment = segments[2];
			if (nonceSegment.length() != 2 * NONCE_BYTES) {
				throw new InvalidPromoCodeException();
			}
			if (expirationSegment.length() != 2 * EXPIRATION_BYTES) {
				throw new InvalidPromoCodeException();
			}
			if (hashSegment.length() != 2 * HASH_TRUNCATE_BYTES) {
				throw new InvalidPromoCodeException();
			}
			
			/* 
			 * decode and verify
			 */
			byte[] nonce = Hex.decodeHex(nonceSegment.toCharArray());
			byte[] expBytes = maskBytes(nonce, Hex.decodeHex(expirationSegment.toCharArray()));
			long exp = bytesToLong(expBytes) * 60000L;
			return exp;
		} catch (DecoderException e) {
			logger.error("Failed to validate promo code: " + promoCode, e);
			throw new InvalidPromoCodeException(e);
		} catch (InvalidKeyException e) {
			logger.error("Failed to validate promo code: " + promoCode, e);
			throw new InvalidPromoCodeException(e);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Failed to validate promo code: " + promoCode, e);
			throw new InvalidPromoCodeException(e);
		}
	}
	

	private void test() {
		String offeringId = "biginsights";
		String email = "Kumar.Amit@bcg.com";
//		long expirationTime = 0;
//		long expirationTime = System.currentTimeMillis() - 3600000L;
		long expirationTime = System.currentTimeMillis() + 3600000L;
		
//		String promoCode = "5a5c72a8147b8e90f69b49f1938b31e3285de9fc886ad85e";
//		try {
//			validate(offeringId, email, promoCode);
//		} catch (InvalidPromoCodeException e) {
//			e.printStackTrace();
//		} catch (ExpiredPromoCodeException e) {
//			e.printStackTrace();
//		}

		for (int i = 0; i < 100; i++) {
			String promoCode = generate(offeringId, email, expirationTime);
			String exp = "";
			try {
				long e = extractExpiration(promoCode);
				if (e == 0) {
					exp = "NEVER";
				} else {
					exp = df.format(new Date(e));
				}
				validate(offeringId, email, promoCode);
				logger.info(promoCode + ": " + exp + " -> valid");
			} catch (InvalidPromoCodeException e) {
				logger.info(promoCode + ": " + exp + " -> invalid");
			} catch (ExpiredPromoCodeException e) {
				logger.info(promoCode + ": " + exp + " -> expired");
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logger.getRootLogger().setLevel(Level.WARN);
		Logger.getLogger(PromoCode.class).setLevel(Level.WARN);
		Logger.getLogger(EngineProperties.class).setLevel(Level.WARN);
		
		if (args.length < 2) {
			System.out.println("Usage: PromoCode <offeringId> <email> [<expiration>]");
			System.out.println("    <offeringId>: CPE offering ID of the service");
			System.out.println("    <email>: customer email");
			System.out.println("    <expiration>: expiration date in the format of yyyy-MM-dd");
			
			return;
		}
		
		String offeringId = args[0];
		String email = args[1];
		long expirationTime = NO_EXPIRATION;
		if (args.length > 2) {
			try {
				Date expDate = new SimpleDateFormat("yyyy-MM-dd").parse(args[2]);
				expirationTime = expDate.getTime() + 23 * 3600000L + 59 * 60000L;
			} catch (ParseException e) {
				System.err.println("Illegal expiration date: " + args[2]);
				return;
			}
		}
		String promoCode = generate(offeringId, email, expirationTime);
		try {
			System.out.println("Offering Id: " + offeringId);
			System.out.println("email: " + email);
			System.out.println("Promo code: " + promoCode);
			long exp = extractExpiration(promoCode);
			if (exp == 0) {
				System.out.println(" - expiration: NEVER");
			} else {
				System.out.println(" - expiration: " + df.format(new Date(exp)));
			}
		} catch(InvalidPromoCodeException e) {
			System.err.println("Internal error! Failed to validate promo code.");
		}

	}
}
