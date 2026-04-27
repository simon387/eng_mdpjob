/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.dao;

import com.fasterxml.jackson.databind.JsonNode;
import it.csi.mdpjob.util.connection.ConnectionManager;
import it.csi.mdpjob.util.connection.ConnectionManagerFactory;
import it.csi.mdpjob.util.connection.ConnectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import static it.csi.mdpjob.dao.InserisciFlussoRiversamentoRestDAO.getTimestamp;


public class InserisciFlussoSingoloPagamentoRestDAO {

	private static final Logger log = LoggerFactory.getLogger ( InserisciFlussoSingoloPagamentoRestDAO.class );

	// Mapping payStatus JSON → codiceesitosingolopagamento numerico
	// come da documentazione CDU: EXECUTED->0, REVOKED->3,
	// STAND_IN->4, STAND_IN_NO_RPT->8, NO_RPT->9
	private static final java.util.Map<String, String> PAY_STATUS_MAP =
					java.util.Map.of (
									"EXECUTED", "0",
									"REVOKED", "3",
									"STAND_IN", "4",
									"STAND_IN_NO_RPT", "8",
									"NO_RPT", "9"
					);

	private final Integer idFlusso;

	private final JsonNode pagamento;

	private final String organizationId;

	public InserisciFlussoSingoloPagamentoRestDAO(Integer idFlusso, JsonNode pagamento, String organizationId) {
		this.idFlusso = idFlusso;
		this.pagamento = pagamento;
		this.organizationId = organizationId;  // <-- aggiunto
	}

	/**
	 * Inserisce il singolo pagamento in flusso_singolo_pagamento.
	 * Se payStatus = NO_RPT (codice 9), il chiamante deve gestire
	 * l'invio RT al fruitore come nel vecchio batch.
	 */
	public int executeUpdate () throws Exception {
		log.debug ( "called executeUpdate" );
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionManagerFactory.getInstance ().getConnection ();

			var iuv = pagamento.path ( "iuv" ).asText ( null );
			var iur = pagamento.path ( "iur" ).asText ( null );
			var payStatus = pagamento.path ( "payStatus" ).asText ( null );
			var codiceEsito = PAY_STATUS_MAP.getOrDefault ( payStatus, payStatus );
			var payDateStr = pagamento.path ( "payDate" ).asText ( null );
			var pay = pagamento.path ( "pay" ).asDouble ( 0 );
			var index = pagamento.path ( "index" ).asInt ( 0 );
			var dataEsito = parseTimestamp ( payDateStr );

			// MODIFICA: recupera sia applicationId che codVersamento in una sola query
			var appIdAndCov = ConnectionUtil.getAppIdAndCodVersamento ( iuv, organizationId, conn );

			var applicationId = appIdAndCov[0];
			var codVersamento = appIdAndCov[1];

			stmt = conn.prepareStatement (
							"INSERT INTO flusso_singolo_pagamento (" +
											"  id_flusso, iuv, identificativounivocoriscossione," +
											"  singoloimportopagato, codiceesitosingolopagamento," +
											"  dataesitosingolopagamento, datainserimento," +
											"  application_id, indicedatisingolopagamento, cod_versamento" +  // <-- aggiunto
											") VALUES (?,?,?,?,?,?,NOW(),?,?,?) RETURNING id"
			);
			stmt.setInt ( 1, idFlusso );
			stmt.setString ( 2, iuv );
			stmt.setString ( 3, iur );
			stmt.setBigDecimal ( 4, BigDecimal.valueOf ( pay ) );
			stmt.setString ( 5, codiceEsito );
			stmt.setTimestamp ( 6, dataEsito );
			stmt.setString ( 7, applicationId );
			stmt.setInt ( 8, index );
			stmt.setString ( 9, codVersamento );  // <-- aggiunto, può essere null

			rs = stmt.executeQuery ();
			rs.next ();
			return rs.getInt ( 1 );

		} finally {
			ConnectionManager.closeResultSet ( rs );
			ConnectionManager.closeStatement ( stmt );
			ConnectionManager.closeConnection ( conn );
		}
	}

	private Timestamp parseTimestamp ( String val ) {
		return getTimestamp ( val );
	}
}
