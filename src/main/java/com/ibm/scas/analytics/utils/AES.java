package com.ibm.scas.analytics.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class AES {
	private static final byte[] defaultKey = { 39, -7, 70, 30, 25, -50, 52, -120, -99, 53, -54, -2, -74, 27, 60, -93 };

	private byte[] rawKey;
	private SecretKeySpec aesKey = null;
	private Cipher aesCipher = null;
	
	public AES() throws GeneralSecurityException {
		try {
			rawKey = defaultKey;
			aesKey = new SecretKeySpec(rawKey, "AES"); //$NON-NLS-1$
			aesCipher = Cipher.getInstance("AES"); //$NON-NLS-1$
		} catch (Exception e) {
			aesKey = null;
			aesCipher = null;
			throw new GeneralSecurityException(e);
		}
	}

	synchronized public String encrypt(String plainText) throws GeneralSecurityException {
		if (aesKey == null) {
			throw new GeneralSecurityException();
		}
		try {
			aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
		} catch (InvalidKeyException e) {
			throw new GeneralSecurityException(e);
		}
		byte[] plainTxtBytes = plainText.getBytes();
		byte[] cipherBytes = aesCipher.doFinal(plainTxtBytes);
		cipherBytes = Base64.encodeBase64(cipherBytes);
		String strCipherText = new String(cipherBytes);
		// The following is only available in commons-code 1.4 and the build
		// seems to pick up 1.3
		// String strCipherText = Base64.encodeBase64String(cipherBytes);
		return strCipherText;
	}

	synchronized public String decrypt(String cipherText) throws GeneralSecurityException {
		if (aesKey == null) {
			throw new GeneralSecurityException();
		}
		aesCipher.init(Cipher.DECRYPT_MODE, aesKey, aesCipher.getParameters());
		byte[] cipherTxtBytes;
		cipherTxtBytes = Base64.decodeBase64(cipherText.getBytes());
		byte[] decodedBytes = aesCipher.doFinal(cipherTxtBytes);
		return new String(decodedBytes);
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			System.out.println("AES encrypt|decrypt <string>"); //$NON-NLS-1$
			return;
		}
		AES myaes = new AES();
//		System.out.println("running AES with: " + args[0] + " " + args[1]); //$NON-NLS-1$ //$NON-NLS-2$
		String input;
		if (args.length >= 2) {
			input = args[1];
		} else {
			InputStream is = System.in;
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			byte[] buf = new byte[256];
			int len;
			while ((len = is.read(buf)) > 0) {
				os.write(buf, 0, len);
			}
			input = os.toString();
		}
		if (args[0].equalsIgnoreCase("encrypt")) { //$NON-NLS-1$
			System.out.print(myaes.encrypt(input));
		} else {
			System.out.print(myaes.decrypt(input));
		}
	}
}
