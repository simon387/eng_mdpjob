/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.util;

import org.apache.xml.security.utils.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;


public class EncryptionDecryptionUtil {

	private EncryptionDecryptionUtil () {
		throw new IllegalStateException ( "Utility class" );
	}

	@Deprecated
	public static String decrypt ( final String encryptedString, final byte[] sbKey ) {

		var skeySpec = new SecretKeySpec ( sbKey, "AES" );
		byte[] original;
		var encrypted = encryptedString.getBytes ();
		try {
			encrypted = Base64.decode ( new String ( encrypted ) );
			var cipher = Cipher.getInstance ( "AES" );

			cipher.init ( Cipher.DECRYPT_MODE, skeySpec );
			original = cipher.doFinal ( encrypted );

			return new String ( original );
		} catch ( Exception e ) {
			throw new RuntimeException ( "Error occurred while encrypting data", e );
		}

	}

	public static String encrypt ( final String data, final byte[] sbKey ) {
		try {
			var secretKey = new SecretKeySpec ( sbKey, "AES" );
			var cipher = Cipher.getInstance ( "AES" );
			cipher.init ( Cipher.ENCRYPT_MODE, secretKey );
			var encryptedData = cipher.doFinal ( data.getBytes ( StandardCharsets.UTF_8 ) );
			return Base64.encode ( encryptedData );
		} catch ( Exception e ) {
			throw new RuntimeException ( "Error occurred while encrypting data", e );
		}
	}

}
