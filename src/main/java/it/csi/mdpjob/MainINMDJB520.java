/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob;

import it.csi.mdpjob.application.InoltroFlussiRendicontazioneEstesi510;
import it.csi.mdpjob.dao.LeggiConfigParam510DAO;
import it.csi.mdpjob.dto.ConfigParam510;
import it.csi.mdpjob.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


public class MainINMDJB520 {

	private static final Logger log = LoggerFactory.getLogger ( MainINMDJB520.class );

	public static void main ( String[] args ) {
		log.info ( "INMDJB510 - Inizio elaborazione" );
		var time = System.nanoTime ();
		try {
			cicloElabora ( args );
		} catch ( Exception e ) {
			log.error ( "INMDJB510 - Errore fatale", e );
			System.exit ( -1 );
		} finally {
			time = System.nanoTime () - time;
			var ms = TimeUnit.MILLISECONDS.convert ( time, TimeUnit.NANOSECONDS ) % 1000;
			var sec = TimeUnit.SECONDS.convert ( time, TimeUnit.NANOSECONDS );
			log.info ( "Elapsed {} ns: {}", time,
							String.format ( "%02d:%02d:%02d.%03d", ( sec / 3600 ) % 60, ( sec / 60 ) % 60, sec % 60, ms ) );
		}
		log.info ( "INMDJB510 - Fine elaborazione." );
		System.exit ( 0 );
	}

	private static void cicloElabora ( String[] args ) throws Exception {
		byte[] key = StringUtils.getKeyDB ();

		// Leggi parametri da config
		ConfigParam510 config = new LeggiConfigParam510DAO ().leggiParametri ();
		log.info ( "Parametri 510: limiteNumFlussi={} ordinamento={} idFlusso={} idIstitutoRicevente={}",
						config.getLimiteNumFlussiGiornaliero (), config.getOrdinamentoFlussi (),
						config.getIdentificativoFlusso (), config.getIdentificativoIstitutoRicevente () );

		// URL endpoint MODRIC da args[0] (come nel vecchio batch)
		if ( args == null || args.length < 1 ) {
			log.error ( "Parametro mancante: URL endpoint servizio MODRIC (args[0])" );
			System.exit ( -1 );
		}
		String urlEndpointServizio = args[0];
		log.info ( "URL endpoint MODRIC: {}", urlEndpointServizio );

		new InoltroFlussiRendicontazioneEstesi510 ().inoltraFlussi ( urlEndpointServizio, config );
	}
}
