/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.util.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class ConnectionUtil {

	private static final Logger log = LoggerFactory.getLogger ( ConnectionUtil.class );

	public static String commonGetAppId ( String iuv, Connection conn ) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionManagerFactory.getInstance ().getConnection ();

			// Prima: iuv_ottici (come EstraiApplicationIdDaIuvDAO)
			stmt = conn.prepareStatement ( "SELECT application_id FROM iuv_ottici WHERE iuv_ottico = ? LIMIT 1" );
			stmt.setString ( 1, iuv );
			rs = stmt.executeQuery ();
			if ( rs.next () ) {
				var appId = rs.getString ( "application_id" );
				if ( appId != null ) {
					return appId;
				}
			}
			ConnectionManager.closeResultSet ( rs );
			ConnectionManager.closeStatement ( stmt );

			// Fallback: rpt (come EstraiApplicationIdDaRPTDAO)
			stmt = conn.prepareStatement ( "SELECT application_id FROM rpt WHERE iuv = ? LIMIT 1" );
			stmt.setString ( 1, iuv );
			rs = stmt.executeQuery ();
			if ( rs.next () ) {
				return rs.getString ( "application_id" );
			}

		} catch ( Exception e ) {
			log.error ( "recuperaApplicationId - Errore recupero applicationId per IUV {}", iuv, e );
		} finally {
			ConnectionManager.closeResultSet ( rs );
			ConnectionManager.closeStatement ( stmt );
			ConnectionManager.closeConnection ( conn );
		}
		return null;
	}

	/**
	 * Recupera application_id e cod_versamento da iuv_ottici, fallback su rpt per solo application_id.
	 * Restituisce array [applicationId, codVersamento] — codVersamento può essere null se non trovato in iuv_ottici.
	 */
	public static String[] getAppIdAndCodVersamento ( String iuv, Connection conn ) {
		if ( iuv == null ) {
			return new String[] { null, null };
		}
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement ( "SELECT application_id, cod_versamento FROM iuv_ottici WHERE iuv_ottico = ? LIMIT 1" );
			stmt.setString ( 1, iuv );
			rs = stmt.executeQuery ();
			if ( rs.next () ) {
				var appId = rs.getString ( "application_id" );
				var codVersamento = rs.getString ( "cod_versamento" );
				if ( appId != null )
					return new String[] { appId, codVersamento };
			}
			ConnectionManager.closeResultSet ( rs );
			ConnectionManager.closeStatement ( stmt );

			// Fallback su rpt: cod_versamento non disponibile
			stmt = conn.prepareStatement ( "SELECT application_id FROM rpt WHERE iuv = ? LIMIT 1" );
			stmt.setString ( 1, iuv );
			rs = stmt.executeQuery ();
			if ( rs.next () ) {
				return new String[] { rs.getString ( "application_id" ), null };
			}

		} catch ( Exception e ) {
			log.error ( "getAppIdAndCodVersamento - errore per iuv {}", iuv, e );
		} finally {
			ConnectionManager.closeResultSet ( rs );
			ConnectionManager.closeStatement ( stmt );
		}
		return new String[] { null, null };
	}

	/**
	 * Dato uno IUV e un organizationId (partita_iva):
	 * 1. Cerca in iuv_ottici filtrando per ente (partita_iva → ente_id)
	 * 2. Se non trovato, cerca in iuv_ottici_cov_secondari
	 * 3. Se non trovato, restituisce [null, null]
	 */
	public static String[] getAppIdAndCodVersamento ( String iuv, String organizationId, Connection conn ) {
		if ( iuv == null ) {
			return new String[] { null, null };
		}
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			// Passo 1: cerca in iuv_ottici filtrando per ente tramite partita_iva
			stmt = conn.prepareStatement (
							"SELECT io.application_id, io.cod_versamento " +
											"FROM iuv_ottici io " +
											"JOIN enti e ON e.ente_id = io.ente_id " +
											"WHERE io.iuv_ottico = ? " +
											"AND e.partita_iva = ? " +
											"LIMIT 1"
			);
			stmt.setString ( 1, iuv );
			stmt.setString ( 2, organizationId );
			rs = stmt.executeQuery ();
			if ( rs.next () ) {
				return new String[] {
								rs.getString ( "application_id" ),
								rs.getString ( "cod_versamento" )
				};
			}
			ConnectionManager.closeResultSet ( rs );
			ConnectionManager.closeStatement ( stmt );

			// Passo 2: cerca in iuv_ottici_cov_secondari filtrando per ente
			stmt = conn.prepareStatement (
							"SELECT cs.application_id, cs.cod_versamento " +
											"FROM iuv_ottici_cov_secondari cs " +
											"JOIN iuv_ottici io ON io.id = cs.id_iuv_ottico " +
											"JOIN enti e ON e.ente_id = cs.ente_id " +
											"WHERE io.iuv_ottico = ? " +
											"AND e.partita_iva = ? " +
											"LIMIT 1"
			);
			stmt.setString ( 1, iuv );
			stmt.setString ( 2, organizationId );
			rs = stmt.executeQuery ();
			if ( rs.next () ) {
				return new String[] {
								rs.getString ( "application_id" ),
								rs.getString ( "cod_versamento" )
				};
			}

		} catch ( Exception e ) {
			log.error ( "getAppIdAndCodVersamento - errore per iuv {} organizationId {}", iuv, organizationId, e );
		} finally {
			ConnectionManager.closeResultSet ( rs );
			ConnectionManager.closeStatement ( stmt );
		}
		return new String[] { null, null };
	}
}
