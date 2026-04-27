/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.dao;

import it.csi.mdpjob.util.connection.ConnectionManager;
import it.csi.mdpjob.util.connection.ConnectionManagerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;


public class InserisciLoggingFlussoEstesoDAO {

	public void inserisci ( String idFlusso, String istitutoMittente, Timestamp dataOraInvio,
					String errori, String warning, String esito, String idMessaggio ) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = ConnectionManagerFactory.getInstance ().getConnection ();
			stmt = conn.prepareStatement (
							"INSERT INTO logging_flusso " +
											"(id_flusso, istituto_mittente, tipo_flusso, data_ora_invio, errori, warning, esito, id_messaggio) " +
											"VALUES (?, ?, 'ESTESO', ?, ?, ?, ?, ?)"
			);
			stmt.setString ( 1, idFlusso );
			stmt.setString ( 2, istitutoMittente );
			stmt.setTimestamp ( 3, dataOraInvio );
			stmt.setString ( 4, errori != null ? errori.substring ( 0, Math.min ( errori.length (), 256 ) ) : null );
			stmt.setString ( 5, warning != null ? warning.substring ( 0, Math.min ( warning.length (), 256 ) ) : null );
			stmt.setString ( 6, esito );
			stmt.setString ( 7, idMessaggio );
			stmt.executeUpdate ();
		} finally {
			ConnectionManager.closeStatement ( stmt );
			ConnectionManager.closeConnection ( conn );
		}
	}
}