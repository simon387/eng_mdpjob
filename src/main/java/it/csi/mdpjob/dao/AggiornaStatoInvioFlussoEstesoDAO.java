/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.dao;

import it.csi.mdpjob.util.connection.ConnectionManager;
import it.csi.mdpjob.util.connection.ConnectionManagerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;


public class AggiornaStatoInvioFlussoEstesoDAO {

	public static final int STATO_INVIATO = 4;

	public static final int STATO_NON_INVIATO = 5;

	public void aggiorna ( Integer idFlusso, int stato ) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = ConnectionManagerFactory.getInstance ().getConnection ();
			stmt = conn.prepareStatement (
							"UPDATE flusso_riversamento SET stato_invio_flusso_esteso = ? WHERE id = ?"
			);
			stmt.setInt ( 1, stato );
			stmt.setInt ( 2, idFlusso );
			stmt.executeUpdate ();
		} finally {
			ConnectionManager.closeStatement ( stmt );
			ConnectionManager.closeConnection ( conn );
		}
	}
}