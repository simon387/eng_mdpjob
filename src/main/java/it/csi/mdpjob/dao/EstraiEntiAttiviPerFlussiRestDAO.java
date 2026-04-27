/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.dao;

import it.csi.mdpjob.util.connection.ConnectionManager;
import it.csi.mdpjob.util.connection.ConnectionManagerFactory;
import org.apache.xml.security.utils.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class EstraiEntiAttiviPerFlussiRestDAO {

	private final byte[] sKey;

	public EstraiEntiAttiviPerFlussiRestDAO ( byte[] sKey ) {
		this.sKey = sKey;
	}

	/**
	 * Restituisce la lista dei codici fiscale Ente (identificativoDominio)
	 * per cui il batch deve richiedere i flussi a pagoPA.
	 * Logica: solo enti con passwordDominioNodoSpc != NULL (= ente non dismesso).
	 * Se organizationIdFiltro != null, restituisce solo quell'ente (scenario alternativo).
	 */
	public List<String> findEntiAttivi ( String organizationIdFiltro ) throws Exception {
		Connection conn = null;
		PreparedStatement selApp = null;
		PreparedStatement selFields = null;
		ResultSet rsApp = null;
		ResultSet rsFields = null;

		try {
			conn = ConnectionManagerFactory.getInstance ().getConnection ();

			// Passo 1: lista APPLICATION_ID con identificativointermediarioPA
			selApp = conn.prepareStatement (
							"SELECT DISTINCT applicationid FROM applicationcustomfields " +
											"WHERE fieldname = 'identificativointermediarioPA'"
			);
			rsApp = selApp.executeQuery ();
			List<String> listAppId = new ArrayList<> ();
			while ( rsApp.next () ) {
				listAppId.add ( rsApp.getString ( 1 ) );
			}

			if ( listAppId.isEmpty () ) {
				return Collections.emptyList ();
			}

			var inClause = new StringBuilder ();
			for ( var i = 0; i < listAppId.size (); i++ ) {
				inClause.append ( "'" ).append ( listAppId.get ( i ) ).append ( "'" );
				if ( i < listAppId.size () - 1 ) {
					inClause.append ( "," );
				}
			}

			// Passo 2: recupera identificativoDominio e passwordDominioNodoSpc
			var sql = "SELECT DISTINCT a.applicationid, a.fieldname, a.fieldvalue " +
							"FROM applicationcustomfields a, applicationdetail ad, gateway g " +
							"WHERE a.applicationid = ad.applicationid " +
							"AND a.gateway_id = ad.gatewayid " +
							"AND ad.gatewayid = g.gateway_id " +
							"AND g.flag_nodo IS TRUE " +
							"AND ad.enabled = '1' " +
							"AND a.applicationid IN (" + inClause + ") " +
							"AND TRIM(fieldname) IN ('identificativoDominio','passwordDominioNodoSpc') " +
							"ORDER BY 1, 2";

			selFields = conn.prepareStatement ( sql );
			rsFields = selFields.executeQuery ();

			// Raggruppa per applicationId
			Map<String, Map<String, String>> mappa = new LinkedHashMap<> ();
			while ( rsFields.next () ) {
				var appId = rsFields.getString ( "applicationid" );
				var fieldName = rsFields.getString ( "fieldname" ).trim ();
				var fieldValue = decifra ( rsFields.getString ( "fieldvalue" ) );
				mappa.computeIfAbsent ( appId, k -> new HashMap<> () ).put ( fieldName, fieldValue );
			}

			// Passo 3: solo enti con passwordDominioNodoSpc != null
			List<String> result = new ArrayList<> ();
			for ( var campi : mappa.values () ) {
				var dominio = campi.get ( "identificativoDominio" );
				var password = campi.get ( "passwordDominioNodoSpc" );
				if ( dominio != null && password != null && !password.isBlank () ) {
					// Scenario alternativo: filtro su singolo ente
					if ( organizationIdFiltro == null || organizationIdFiltro.equals ( dominio ) ) {
						if ( !result.contains ( dominio ) ) {
							result.add ( dominio );
						}
					}
				}
			}
			return result;

		} finally {
			ConnectionManager.closeResultSet ( rsApp );
			ConnectionManager.closeResultSet ( rsFields );
			ConnectionManager.closeStatement ( selApp );
			ConnectionManager.closeStatement ( selFields );
			ConnectionManager.closeConnection ( conn );
		}
	}

	private String decifra ( String cifrato ) {
		if ( sKey == null || cifrato == null )
			return cifrato;
		try {
			var skeySpec = new SecretKeySpec ( sKey, "AES" );
			byte[] encrypted = Base64.decode ( cifrato );
			var cipher = Cipher.getInstance ( "AES" );
			cipher.init ( Cipher.DECRYPT_MODE, skeySpec );
			return new String ( cipher.doFinal ( encrypted ) );
		} catch ( Exception e ) {
			return cifrato;
		}
	}
}
