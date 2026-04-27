/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.dao;

import it.csi.mdpjob.dto.FdrDettaglio;
import it.csi.mdpjob.dto.FdrItem;
import it.csi.mdpjob.util.connection.ConnectionManager;
import it.csi.mdpjob.util.connection.ConnectionManagerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;


public class InserisciFlussoRiversamentoRestDAO {

	private static final String VERSIONE_OGGETTO_DEFAULT = "1.0";

	private final FdrItem fdrItem;

	private final FdrDettaglio dettaglio;

	private final String jsonFlusso;

	public InserisciFlussoRiversamentoRestDAO ( FdrItem fdrItem, FdrDettaglio dettaglio, String jsonFlusso ) {
		this.fdrItem = fdrItem;
		this.dettaglio = dettaglio;
		this.jsonFlusso = jsonFlusso;
	}

	/**
	 * Inserisce il flusso in flusso_riversamento valorizzando jsonflusso.
	 * Se il flusso esiste già (acquisito in XML dal batch 110) aggiorna solo jsonflusso.
	 * Restituisce l'id del record inserito/aggiornato.
	 */
	public Integer executeUpdate () throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionManagerFactory.getInstance ().getConnection ();

			// Verifica se esiste già (potrebbe esserci in XML dal vecchio batch)
			stmt = conn.prepareStatement (
							"SELECT id FROM flusso_riversamento " +
											"WHERE identificativoflusso = ? LIMIT 1"
			);
			stmt.setString ( 1, fdrItem.getFdr () );
			rs = stmt.executeQuery ();

			if ( rs.next () ) {
				// Esiste già in XML: aggiorna solo jsonflusso
				var id = rs.getInt ( "id" );
				ConnectionManager.closeResultSet ( rs );
				ConnectionManager.closeStatement ( stmt );

				stmt = conn.prepareStatement (
								"UPDATE flusso_riversamento " +
												"SET jsonflusso = ?, datamodifica = NOW() " +
												"WHERE id = ?"
				);
				stmt.setString ( 1, jsonFlusso );
				stmt.setInt ( 2, id );
				stmt.executeUpdate ();
				return id;
			}

			ConnectionManager.closeResultSet ( rs );
			ConnectionManager.closeStatement ( stmt );

			// Nuovo flusso: recupera nextval dalla sequenza esistente
			stmt = conn.prepareStatement (
							"SELECT nextval('flusso_riversamento_id_seq')"
			);
			rs = stmt.executeQuery ();
			rs.next ();
			var newId = rs.getInt ( 1 );
			ConnectionManager.closeResultSet ( rs );
			ConnectionManager.closeStatement ( stmt );

			// Mappa i campi JSON → colonne DB
			// sender.pspId       → identificativopsp
			// sender.id          → identificativoistitutomittente
			// sender.pspName     → denominazionemittente
			// receiver.id        → identificativoistitutoricevente
			// receiver.orgName   → denominazionericevente
			// regulation         → identificativounivocoregolamento
			// regulationDate     → dataregolamento
			// fdrDate            → dataoraflusso
			// totPayments        → numerototalepagamenti
			// sumPayments        → importototalepagamenti
			// bicCodePouringBank → codicebicbancadiriversamento

			var raw = dettaglio.getRawJson ();
			var pspId = raw.path ( "sender" ).path ( "pspId" ).asText ( null );
			var senderId = raw.path ( "sender" ).path ( "id" ).asText ( null );
			var pspName = raw.path ( "sender" ).path ( "pspName" ).asText ( null );
			var receiverId = raw.path ( "receiver" ).path ( "id" ).asText ( null );
			var receiverName = raw.path ( "receiver" ).path ( "organizationName" ).asText ( null );
			var regulation = raw.path ( "regulation" ).asText ( null );
			var regulationDateStr = raw.path ( "regulationDate" ).asText ( null );
			var fdrDateStr = raw.path ( "fdrDate" ).asText ( null );
			var bicCode = raw.path ( "bicCodePouringBank" ).asText ( null );

			var dataOraFlusso = parseTimestamp ( fdrDateStr );
			var dataRegolamento = parseTimestamp ( regulationDateStr );

			stmt = conn.prepareStatement (
							"INSERT INTO flusso_riversamento (" +
											"  id, identificativopsp, identificativoflusso, versioneoggetto," +
											"  identificativounivocoregolamento," +
											"  identificativoistitutomittente, identificativoistitutoricevente," +
											"  numerototalepagamenti, importototalepagamenti," +
											"  dataoraflusso, dataregolamento, datainserimento," +
											"  jsonflusso, denominazionemittente, denominazionericevente," +
											"  codicebicbancadiriversamento," +
											"  stato_invio_flusso_base, stato_invio_flusso_esteso" +
											") VALUES (?,?,?,?,?,?,?,?,?,?,?,NOW(),?,?,?,?,1,1)"
			);
			stmt.setInt ( 1, newId );
			stmt.setString ( 2, pspId );
			stmt.setString ( 3, fdrItem.getFdr () );
			stmt.setString ( 4, VERSIONE_OGGETTO_DEFAULT );
			stmt.setString ( 5, regulation );
			stmt.setString ( 6, senderId );
			stmt.setString ( 7, receiverId );
			stmt.setInt ( 8, dettaglio.getTotPayments () != null ? dettaglio.getTotPayments () : 0 );
			stmt.setBigDecimal ( 9, dettaglio.getSumPayments () != null
							? java.math.BigDecimal.valueOf ( dettaglio.getSumPayments () ) : null );
			stmt.setTimestamp ( 10, dataOraFlusso );
			stmt.setTimestamp ( 11, dataRegolamento );
			stmt.setString ( 12, jsonFlusso );
			stmt.setString ( 13, pspName );
			stmt.setString ( 14, receiverName );
			stmt.setString ( 15, bicCode );
			stmt.executeUpdate ();
			return newId;

		} finally {
			ConnectionManager.closeResultSet ( rs );
			ConnectionManager.closeStatement ( stmt );
			ConnectionManager.closeConnection ( conn );
		}
	}

	private Timestamp parseTimestamp ( String val ) {
		return getTimestamp ( val );
	}

	static Timestamp getTimestamp ( String val ) {
		if ( val == null || val.isBlank () ) {
			return null;
		}
		try {
			// Formato pagoPA: yyyy-MM-dd'T'HH:mm:ssZ o con millis
			return Timestamp.from ( java.time.OffsetDateTime.parse ( val ).toInstant () );
		} catch ( Exception e ) {
			try {
				return Timestamp.valueOf ( val.replace ( "T", " " ).substring ( 0, 19 ) );
			} catch ( Exception e2 ) {
				return null;
			}
		}
	}
}