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
import java.sql.Types;


public class InserisciTracciaturaSingolaAcquisizioneDAO {

	public Integer inserisci ( Integer idFlusso, Integer numPagamenti ) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionManagerFactory.getInstance ().getConnection ();
			stmt = conn.prepareStatement (
							"SELECT nextval('tracciatura_singola_acquisizione_id_seq')"
			);
			rs = stmt.executeQuery ();
			rs.next ();
			var id = rs.getInt ( 1 );
			ConnectionManager.closeResultSet ( rs );
			ConnectionManager.closeStatement ( stmt );

			stmt = conn.prepareStatement (
							"INSERT INTO tracciatura_singola_acquisizione " +
											"(id, id_flusso, num_pagamenti, data_inizio) " +
											"VALUES (?, ?, ?, NOW())"
			);
			stmt.setInt ( 1, id );
			stmt.setInt ( 2, idFlusso );
			if ( numPagamenti != null ) {
				stmt.setInt ( 3, numPagamenti );
			} else {
				stmt.setNull ( 3, Types.INTEGER );
			}
			stmt.executeUpdate ();
			return id;
		} finally {
			ConnectionManager.closeResultSet ( rs );
			ConnectionManager.closeStatement ( stmt );
			ConnectionManager.closeConnection ( conn );
		}
	}

	public void aggiorna ( Integer id, String esito, String response, String descrizione ) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = ConnectionManagerFactory.getInstance ().getConnection ();
			stmt = conn.prepareStatement (
							"UPDATE tracciatura_singola_acquisizione " +
											"SET esito = ?, data_fine = NOW(), response = ?, descrizione = ? " +
											"WHERE id = ?"
			);
			stmt.setString ( 1, esito );
			stmt.setString ( 2, response );
			stmt.setString ( 3, descrizione != null
							? descrizione.substring ( 0, Math.min ( descrizione.length (), 500 ) )
							: null );
			stmt.setInt ( 4, id );
			stmt.executeUpdate ();
		} finally {
			ConnectionManager.closeStatement ( stmt );
			ConnectionManager.closeConnection ( conn );
		}
	}
}
