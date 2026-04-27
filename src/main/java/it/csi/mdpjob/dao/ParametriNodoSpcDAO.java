/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.dao;

import it.csi.mdpjob.util.connection.ConnectionManager;
import it.csi.mdpjob.util.connection.ConnectionManagerFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.xml.security.utils.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;


public class ParametriNodoSpcDAO {

	private static final Logger log = LoggerFactory.getLogger ( ParametriNodoSpcDAO.class );

	private final byte[] sKey;

	public ParametriNodoSpcDAO ( byte[] sKey ) {
		this.sKey = sKey;
	}

	/**
	 * Estrae l'elenco degli application custom field per un'application
	 */
	public Map<String, String> getMappaApplicationCustomFieldsEnabled ( String applicationId ) throws Exception {
		var METHOD_NAME = "getMappaApplicationCustomFieldsEnabled";

		log.info ( "{} - {}", METHOD_NAME, super.getClass ().getSimpleName () );

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		Map<String, String> mappaAppCustomFields = new HashMap<> ();

		try {
			log.info ( "{} - Ottengo la connessione", METHOD_NAME );
			conn = ConnectionManagerFactory.getInstance ().getConnection ();

			log.info ( "{} - Connessione ottenuta, creo lo statement applicazioni", METHOD_NAME );
			stmt = conn.prepareStatement ( "select a.fieldname, a.fieldvalue "
							+ "from applicationcustomfields a, applicationdetail ad,  gateway g "
							+ "where a.applicationid = ad.applicationid  "
							+ "and a.gateway_id=ad.gatewayid "
							+ "and ad.gatewayid = g.gateway_id "
							+ "and g.flag_nodo is true "
							+ "and ad.enabled = '1' "
							+ "and a.fieldvalue is not null "
							+ "and a.applicationid = ? " );

			stmt.setString ( 1, applicationId );

			log.info ( "{} - Statement ottenuto, eseguo la query applicazioni", METHOD_NAME );
			rs = stmt.executeQuery ();

			while ( rs.next () ) {
				mappaAppCustomFields.put ( StringUtils.trimToEmpty ( rs.getString ( "fieldname" ) ), decifraFieldValue ( rs.getString ( "fieldvalue" ) ) );
			}

			log.info ( "{} - parametri popolati, fine elaborazione", METHOD_NAME );
			return mappaAppCustomFields;

		} catch ( Exception e ) {
			log.error ( "{}{}", METHOD_NAME, e.getClass (), e );
			throw e;
		} finally {
			ConnectionManager.closeResultSet ( rs );
			ConnectionManager.closeStatement ( stmt );
			ConnectionManager.closeConnection ( conn );
		}
	}

	@Deprecated
	private String decifraFieldValue ( String fieldValueCifrato ) {
		String fieldValueDecifrato = null;
		if ( sKey != null && fieldValueCifrato != null ) {
			var skeySpec = new SecretKeySpec ( sKey, "AES" );
			byte[] original;
			var encrypted = fieldValueCifrato.getBytes ();
			try {
				encrypted = Base64.decode ( new String ( encrypted ) );
				var cipher = Cipher.getInstance ( "AES" );

				cipher.init ( Cipher.DECRYPT_MODE, skeySpec );
				original = cipher.doFinal ( encrypted );

				fieldValueDecifrato = new String ( original );
			} catch ( Exception e ) {
				log.error ( e.getMessage (), e );
			}
		}
		return fieldValueDecifrato;
	}
}
