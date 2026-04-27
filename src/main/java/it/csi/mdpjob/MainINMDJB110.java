/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob;

import it.csi.mdpjob.application.ElaborazioneFlussiRendicontazioneRest;
import it.csi.mdpjob.application.PagopaFdrRestClient;
import it.csi.mdpjob.dao.EstraiEntiAttiviPerFlussiRestDAO;
import it.csi.mdpjob.dao.LeggiConfigParamDAO;
import it.csi.mdpjob.dao.SubscriptionConfDAO;
import it.csi.mdpjob.dto.ConfigParam;
import it.csi.mdpjob.enumeration.SubscriptionConfigCodeEnum;
import it.csi.mdpjob.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import static it.csi.mdpjob.util.EncryptionDecryptionUtil.decrypt;


public class MainINMDJB110 {

	private static final Logger log = LoggerFactory.getLogger ( MainINMDJB110.class );

	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern ( "yyyy-MM-dd'T'HH:mm:ss" );

	// per ora nessun parametro è necessario
	public static void main ( String[] args ) {
		log.info ( "inizio elaborazione" );
		var time = System.nanoTime ();
		try {
			cicloElabora ();
		} catch ( Exception e ) {
			log.error ( "Errore fatale", e );
			System.exit ( -1 );
		} finally {
			time = System.nanoTime () - time;
			var ms = TimeUnit.MILLISECONDS.convert ( time, TimeUnit.NANOSECONDS ) % 1000;
			var sec = TimeUnit.SECONDS.convert ( time, TimeUnit.NANOSECONDS );
			log.info ( "Elapsed {} ns: {} hh:mm:ss", time,
							String.format ( "%02d:%02d:%02d.%03d", ( sec / 3600 ) % 60, ( sec / 60 ) % 60, sec % 60, ms ) );
		}
		log.info ( "Fine elaborazione." );
		System.exit ( 0 );
	}

	private static void cicloElabora () throws Exception {
		var key = StringUtils.getKeyDB ();

		// PASSO 1a: leggi parametri da tabella config
		var configParam = new LeggiConfigParamDAO ().leggiParametri ();
		log.info ( "cicloElabora - Parametri config letti: organizationId={} pspId={} publishedGt={} flowDate={} numGiorniPublished={}",
						configParam.getOrganizationId (), configParam.getPspId (), configParam.getPublishedGt (), configParam.getFlowDate (),
						configParam.getNumGiorniPublished () );

		// PASSO 1b: leggi Ocp-Apim-Subscription-Key da subscription_config
		var subscriptionConfDAO = new SubscriptionConfDAO ( SubscriptionConfigCodeEnum.FDR_ORG );
		var subscriptionConfig = subscriptionConfDAO.executeQuery ();

		if ( subscriptionConfig == null ||
						( subscriptionConfig.getKeyPrimaria () == null && subscriptionConfig.getKeySecondaria () == null ) ) {
			log.error ( "cicloElabora - Subscription key FDR-ORG non configurata. Elaborazione interrotta." );
			log.info ( "ERRORE: Subscription key FDR-ORG non configurata." );
			System.exit ( -1 );
		}

		var cryptedKey = subscriptionConfig.getKeyPrimaria () != null
						? subscriptionConfig.getKeyPrimaria ()
						: subscriptionConfig.getKeySecondaria ();
		var subscriptionKey = decrypt ( cryptedKey, key );

		var baseUrl = subscriptionConfig.getUrl ();
		if ( baseUrl == null || baseUrl.isBlank () ) {
			log.error ( "cicloElabora - URL FDR-ORG non configurato in subscription_config." );
			System.exit ( -1 );
		}

		// Calcolo publishedGt effettivo: se numGiorniPublished valorizzato,
		// sottrai i giorni alla data di pubblicazione
		var publishedGtEffettivo = calcolaPublishedGt ( configParam );

		// PASSO 2: recupera lista enti (scenario base o alternativo)
		var enti = new EstraiEntiAttiviPerFlussiRestDAO ( key )
						.findEntiAttivi ( configParam.getOrganizationId () ); // null = tutti

		log.info ( "Enti da elaborare: {}", enti.size () );
		log.info ( "cicloElabora - Enti da elaborare: {}", enti.size () );

		var client = new PagopaFdrRestClient ( baseUrl, subscriptionKey );
		var elaborazione = new ElaborazioneFlussiRendicontazioneRest ();

		var erroreGlobale = false;

		// PASSO 3: per ogni ente richiedi i flussi
		for ( var organizationId : enti ) {
			log.info ( "Elaborazione ente: {}", organizationId );
			try {
				var errore = elaborazione.elaboraPerEnte (
								organizationId,
								client,
								configParam.getPspId (),
								publishedGtEffettivo,
								configParam.getFlowDate (),
								key
				);
				if ( errore ) {
					log.error ( "cicloElabora - Errori riscontrati per ente: {}", organizationId );
					erroreGlobale = true;
				}
			} catch ( Exception e ) {
				log.error ( "cicloElabora - Errore elaborazione ente {}", organizationId );
				erroreGlobale = true;
			}
		}

		if ( erroreGlobale ) {
			log.error ( "cicloElabora - Elaborazione terminata con errori." );
			System.exit ( -1 );
		}
	}

	/**
	 * Se numGiorniPublished è valorizzato, calcola publishedGt sottraendo
	 * i giorni alla data odierna. Altrimenti usa publishedGt da config.
	 */
	private static String calcolaPublishedGt ( ConfigParam param ) {
		if ( param.getNumGiorniPublished () != null ) {
			var dataCalcolata = LocalDateTime.now ().minusDays ( param.getNumGiorniPublished () );
			var calcolata = dataCalcolata.format ( FMT );
			log.info ( "calcolaPublishedGt - publishedGt calcolato da numGiorniPublished: {}", calcolata );
			return calcolata;
		}
		return param.getPublishedGt (); // può essere null = nessun filtro
	}
}