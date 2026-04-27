/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.dao;

import it.csi.mdpjob.dto.ConfigParam510;
import it.csi.mdpjob.util.connection.ConnectionManager;
import it.csi.mdpjob.util.connection.ConnectionManagerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;


public class LeggiConfigParam510DAO {

	public ConfigParam510 leggiParametri () throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionManagerFactory.getInstance ().getConnection ();
			stmt = conn.prepareStatement (
							"SELECT key, value FROM config WHERE key IN (" +
											"'limite.num.flussi.estesi.giornaliero'," +
											"'ordinamento.estrazione_flussi.510'," +
											"'identificativo.flusso.510'," +
											"'identificativo.istituto.ricevente.510'," +
											"'elenco.codici.segregazione.da.trattare')"
			);
			rs = stmt.executeQuery ();
			Map<String, String> valori = new HashMap<> ();
			while ( rs.next () ) {
				valori.put ( rs.getString ( "key" ), rs.getString ( "value" ) );
			}
			ConfigParam510 param = new ConfigParam510 ();
			param.setLimiteNumFlussiGiornaliero ( valori.get ( "limite.num.flussi.estesi.giornaliero" ) );
			param.setOrdinamentoFlussi ( valori.get ( "ordinamento.estrazione_flussi.510" ) );
			param.setIdentificativoFlusso ( valori.get ( "identificativo.flusso.510" ) );
			param.setIdentificativoIstitutoRicevente ( valori.get ( "identificativo.istituto.ricevente.510" ) );
			param.setElencoCodiciSegregazione ( valori.get ( "elenco.codici.segregazione.da.trattare" ) );
			return param;
		} finally {
			ConnectionManager.closeResultSet ( rs );
			ConnectionManager.closeStatement ( stmt );
			ConnectionManager.closeConnection ( conn );
		}
	}
}