/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.dao;

import it.csi.mdpjob.dto.ConfigParam;
import it.csi.mdpjob.util.connection.ConnectionManager;
import it.csi.mdpjob.util.connection.ConnectionManagerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;


public class LeggiConfigParamDAO {

	public ConfigParam leggiParametri () throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionManagerFactory.getInstance ().getConnection ();
			//noinspection Annotator
			stmt = conn.prepareStatement (
							"SELECT key, value FROM config " +
											"WHERE key IN ('organizationID.flusso110','pspID.flusso110'," +
											"'publishedGt.flusso110','flowDate.flusso110','numGiorniPublished.flusso110')"
			);
			rs = stmt.executeQuery ();

			Map<String, String> valori = new HashMap<> ();
			while ( rs.next () ) {
				valori.put ( rs.getString ( "key" ), rs.getString ( "value" ) );
			}

			var param = new ConfigParam ();
			// Valorizza solo se non blank — se tutti null siamo in modalità standard
			var orgId = valori.get ( "organizationID.flusso110" );
			if ( orgId != null && !orgId.isBlank () ) {
				param.setOrganizationId ( orgId );
			}

			var pspId = valori.get ( "pspID.flusso110" );
			if ( pspId != null && !pspId.isBlank () ) {
				param.setPspId ( pspId );
			}

			var publishedGt = valori.get ( "publishedGt.flusso110" );
			if ( publishedGt != null && !publishedGt.isBlank () ) {
				param.setPublishedGt ( publishedGt );
			}

			var flowDate = valori.get ( "flowDate.flusso110" );
			if ( flowDate != null && !flowDate.isBlank () ) {
				param.setFlowDate ( flowDate );
			}

			var numGiorni = valori.get ( "numGiorniPublished.flusso110" );
			if ( numGiorni != null && !numGiorni.isBlank () ) {
				param.setNumGiorniPublished ( Integer.parseInt ( numGiorni ) );
			}
			return param;

		} finally {
			ConnectionManager.closeResultSet ( rs );
			ConnectionManager.closeStatement ( stmt );
			ConnectionManager.closeConnection ( conn );
		}
	}
}