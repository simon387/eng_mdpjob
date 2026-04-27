/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob;

import it.csi.mdpjob.util.EncryptionDecryptionUtil;
import it.csi.mdpjob.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MainCriptaturaSubscriptionKey {

	private static final Logger log = LoggerFactory.getLogger ( MainCriptaturaSubscriptionKey.class );

	public static void main ( String[] args ) {
		var key = StringUtils.getKeyDB ();
		var keyCifrata = EncryptionDecryptionUtil.encrypt ( "tua_subscription_key_in_chiaro", key );
		log.info ( "Key cifrata: {}", keyCifrata );
	}
	/*
	 * dopo aver eseguito questo main, esegui:
	 *
	 UPDATE subscription_config
	 SET key_primaria = '<output>'
	 WHERE codice = 'FDR-ORG';
	 */
}
