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


public class InserisciTracciaturAcquisizioneFlussoDAO {

	public static final String STATO_DA_ELABORARE = "DA_ELABORARE";

	public static final String STATO_ELABORATO = "ELABORATO";

	public static final String STATO_SCARTATO = "SCARTATO";

	public Integer inserisci ( String identificativoFlusso, Integer revision, String organizationId, String pspId, String stato ) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionManagerFactory.getInstance ().getConnection ();

			// Recupera nextval dalla sequenza
			stmt = conn.prepareStatement (
							"SELECT nextval('tracciatura_acquisizione_flusso_id_seq')"
			);
			rs = stmt.executeQuery ();
			rs.next ();
			int id = rs.getInt ( 1 );
			ConnectionManager.closeResultSet ( rs );
			ConnectionManager.closeStatement ( stmt );

			stmt = conn.prepareStatement (
							"INSERT INTO tracciatura_acquisizione_flusso " +
											"(id, identificativo_flusso, revision, organization_id, psp_id, " +
											" data_inizio_elaborazione, stato) " +
											"VALUES (?, ?, ?, ?, ?, NOW(), ?)"
			);
			stmt.setInt ( 1, id );
			stmt.setString ( 2, identificativoFlusso );
			stmt.setInt ( 3, revision );
			stmt.setString ( 4, organizationId );
			stmt.setString ( 5, pspId );
			stmt.setString ( 6, stato );
			stmt.executeUpdate ();
			return id;

		} finally {
			ConnectionManager.closeResultSet ( rs );
			ConnectionManager.closeStatement ( stmt );
			ConnectionManager.closeConnection ( conn );
		}
	}

	public void aggiornaStato ( Integer id, String stato, String note ) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = ConnectionManagerFactory.getInstance ().getConnection ();
			stmt = conn.prepareStatement (
							"UPDATE tracciatura_acquisizione_flusso " +
											"SET stato = ?, data_fine_elaborazione = NOW(), note = ? " +
											"WHERE id = ?"
			);
			stmt.setString ( 1, stato );
			stmt.setString ( 2, note );
			stmt.setInt ( 3, id );
			stmt.executeUpdate ();
		} finally {
			ConnectionManager.closeStatement ( stmt );
			ConnectionManager.closeConnection ( conn );
		}
	}
}
