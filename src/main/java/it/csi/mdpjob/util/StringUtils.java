/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.util;

import org.apache.xml.security.utils.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;


public class StringUtils {

	private static final Logger log = LoggerFactory.getLogger ( StringUtils.class );

	public static byte[] getKeyDB () {
		final String methodName = "getKeyDB";
		log.info ( "{} - START", methodName );

		try ( var is = ClassLoader.getSystemResourceAsStream ( "skeydb.txt" ) ) {

			if ( is == null ) {
				throw new IllegalStateException ( "Resource skeydb.txt not found" );
			}

			var encodedBytes = is.readAllBytes ();
			var encodedString = new String ( encodedBytes, StandardCharsets.UTF_8 );

			return Base64.decode ( encodedString );

		} catch ( Exception e ) {
			log.error ( "{} - errore chiave db", methodName, e );
			return null;
		}
	}

	public static String capitalize(String s){
		return s;
	}
}
