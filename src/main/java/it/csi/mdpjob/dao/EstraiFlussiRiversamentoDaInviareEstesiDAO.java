/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.dao;

import it.csi.mdpjob.dto.FlussoRiversamentoDB;
import it.csi.mdpjob.util.connection.ConnectionManager;
import it.csi.mdpjob.util.connection.ConnectionManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


public class EstraiFlussiRiversamentoDaInviareEstesiDAO {

	private static final Logger log = LoggerFactory.getLogger ( EstraiFlussiRiversamentoDaInviareEstesiDAO.class );

	private final String limiteNumFlussi;

	private final String ordinamento;

	private final String identificativoFlusso;

	private final String identificativoIstitutoRicevente;

	public EstraiFlussiRiversamentoDaInviareEstesiDAO ( String limiteNumFlussi, String ordinamento,
					String identificativoFlusso,
					String identificativoIstitutoRicevente ) {
		this.limiteNumFlussi = limiteNumFlussi;
		this.ordinamento = ordinamento;
		this.identificativoFlusso = identificativoFlusso;
		this.identificativoIstitutoRicevente = identificativoIstitutoRicevente;
	}

	public List<FlussoRiversamentoDB> executeQuery () throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionManagerFactory.getInstance ().getConnection ();

			StringBuilder sql = new StringBuilder (
							"SELECT id, identificativoflusso, identificativopsp, " +
											"identificativoistitutomittente, identificativoistitutoricevente, " +
											"denominazionemittente, denominazionericevente, " +
											"numerototalepagamenti, importototalepagamenti, " +
											"dataoraflusso, dataregolamento, xmlflusso, " +
											"identificativounivocoregolamento " +
											"FROM flusso_riversamento " +
											"WHERE stato_invio_flusso_esteso = 1 " +
											"AND xmlflusso IS NOT NULL AND xmlflusso <> '' "
			);

			if ( identificativoFlusso != null && !identificativoFlusso.isBlank () ) {
				sql.append ( "AND identificativoflusso = '" ).append ( identificativoFlusso ).append ( "' " );
			}
			if ( identificativoIstitutoRicevente != null && !identificativoIstitutoRicevente.isBlank () ) {
				sql.append ( "AND identificativoistitutoricevente = '" )
								.append ( identificativoIstitutoRicevente ).append ( "' " );
			}

			String ord = ( ordinamento != null && ordinamento.equalsIgnoreCase ( "asc" ) ) ? "ASC" : "DESC";
			sql.append ( "ORDER BY dataoraflusso " ).append ( ord ).append ( " " );

			if ( limiteNumFlussi != null && !limiteNumFlussi.isBlank () ) {
				try {
					int limite = Integer.parseInt ( limiteNumFlussi.trim () );
					sql.append ( "LIMIT " ).append ( limite );
				} catch ( NumberFormatException e ) {
					log.warn ( "limite.num.flussi.estesi.giornaliero non valido: {}", limiteNumFlussi );
				}
			}

			log.info ( "EstraiFlussiRiversamentoDaInviareEstesiDAO - SQL: {}", sql );
			stmt = conn.prepareStatement ( sql.toString () );
			rs = stmt.executeQuery ();

			List<FlussoRiversamentoDB> result = new ArrayList<> ();
			while ( rs.next () ) {
				FlussoRiversamentoDB f = new FlussoRiversamentoDB ();
				f.setId ( rs.getInt ( "id" ) );
				f.setIdentificativoFlusso ( rs.getString ( "identificativoflusso" ) );
				f.setIdentificativoPsp ( rs.getString ( "identificativopsp" ) );
				f.setIdentificativoIstitutoMittente ( rs.getString ( "identificativoistitutomittente" ) );
				f.setIdentificativoIstitutoRicevente ( rs.getString ( "identificativoistitutoricevente" ) );
				f.setDenominazioneMittente ( rs.getString ( "denominazionemittente" ) );
				f.setDenominazioneRicevente ( rs.getString ( "denominazionericevente" ) );
				f.setNumeroTotalePagamenti ( rs.getInt ( "numerototalepagamenti" ) );
				f.setImportoTotalePagamenti ( rs.getBigDecimal ( "importototalepagamenti" ) );
				f.setDataOraFlusso ( rs.getTimestamp ( "dataoraflusso" ) );
				f.setDataRegolamento ( rs.getTimestamp ( "dataregolamento" ) );
				f.setXmlFlusso ( rs.getString ( "xmlflusso" ) );
				f.setIdentificativoUnivocoRegolamento ( rs.getString ( "identificativounivocoregolamento" ) );
				result.add ( f );
			}
			log.info ( "Flussi estratti da inviare: {}", result.size () );
			return result;
		} finally {
			ConnectionManager.closeResultSet ( rs );
			ConnectionManager.closeStatement ( stmt );
			ConnectionManager.closeConnection ( conn );
		}
	}
}