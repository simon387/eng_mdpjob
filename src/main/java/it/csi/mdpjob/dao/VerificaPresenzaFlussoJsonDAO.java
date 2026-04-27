/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.dao;

import it.csi.mdpjob.util.connection.ConnectionManager;
import it.csi.mdpjob.util.connection.ConnectionManagerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class VerificaPresenzaFlussoJsonDAO {

	/**
	 * Verifica se il flusso identificato da fdr+revision è già presente
	 * con jsonflusso valorizzato. Se sì, non va ri-acquisito.
	 */
	public boolean isGiaPresenteConJson ( String fdr ) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionManagerFactory.getInstance ().getConnection ();
			stmt = conn.prepareStatement (
							"SELECT 1 FROM flusso_riversamento " +
											"WHERE identificativoflusso = ? " +
											"AND jsonflusso IS NOT NULL AND jsonflusso <> '' " +
											"LIMIT 1"
			);
			stmt.setString ( 1, fdr );
			rs = stmt.executeQuery ();
			return rs.next ();
		} finally {
			ConnectionManager.closeResultSet ( rs );
			ConnectionManager.closeStatement ( stmt );
			ConnectionManager.closeConnection ( conn );
		}
	}
}
