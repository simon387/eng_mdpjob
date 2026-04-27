/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.csi.mdpjob.dao.InserisciFlussoRiversamentoRestDAO;
import it.csi.mdpjob.dao.InserisciFlussoSingoloPagamentoRestDAO;
import it.csi.mdpjob.dao.InserisciTracciaturAcquisizioneFlussoDAO;
import it.csi.mdpjob.dao.InserisciTracciaturaSingolaAcquisizioneDAO;
import it.csi.mdpjob.dao.VerificaPresenzaFlussoJsonDAO;
import it.csi.mdpjob.dto.FdrDettaglio;
import it.csi.mdpjob.dto.FdrItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class ElaborazioneFlussiRendicontazioneRest {

	private static final Logger log = LoggerFactory.getLogger ( ElaborazioneFlussiRendicontazioneRest.class );
	private static final String LOG_ESITO_FLUSSO =
					"Esito flusso - orgId={} fdr={} rev={} pspId={} idTracciatura={} idSingola={} stato={} esito={} motivo={}";

	private final ObjectMapper objectMapper = new ObjectMapper ();

	public boolean elaboraPerEnte ( String organizationId,
					PagopaFdrRestClient client,
					String pspIdFiltro,
					String publishedGt,
					String flowDate,
					byte[] key ) {  // <-- aggiunto

		log.info ( "Inizio elaborazione per ente: {}", organizationId );
		var errore = false;

		try {
			// PASSO 3-4: chiama GET /fdrs con paginazione
			var fdrsResponse = client.getAllFdrs ( organizationId, pspIdFiltro, publishedGt, flowDate );

			var flussi = fdrsResponse.flussi ();
			var countDichiarato = fdrsResponse.countTotaleDichiarato ();

			// PASSO 5: CONTROLLO numero flussi ricevuti
			if ( flussi.size () != countDichiarato ) {
				log.info ( "WARNING: flussi ricevuti ({}) != count dichiarato da pagoPA ({}) per ente {}", flussi.size (), countDichiarato, organizationId );
			}

			// PASSO 6: elabora ogni singolo flusso
			for ( var fdrItem : flussi ) {
				try {
					errore = errore || elaboraSingoloFlusso ( organizationId, fdrItem, client, key );
				} catch ( Exception e ) {
					log.error ( "Errore su flusso {}", fdrItem.getFdr (), e );
					errore = true;
				}
			}

		} catch ( Exception e ) {
			log.error ( "Errore generale per ente {}", organizationId, e );
			errore = true;
		}

		log.info ( "Fine elaborazione per ente: {}", organizationId );
		return errore;
	}

	private boolean elaboraSingoloFlusso ( String organizationId, FdrItem fdrItem, PagopaFdrRestClient client, byte[] key ) throws Exception {
		var METHOD = "elaboraSingoloFlusso";
		var fdr = fdrItem.getFdr ();
		var revision = fdrItem.getRevision ();
		var pspId = fdrItem.getPspId ();

		log.info ( "Elaborazione flusso: {} rev: {} psp: {}", fdr, revision, pspId );

		// PASSO 6: CONTROLLO presenza flusso in MDP
		var giaPresenteConJson = new VerificaPresenzaFlussoJsonDAO ().isGiaPresenteConJson ( fdr );

		if ( giaPresenteConJson ) {
			log.info ( "{} - Flusso {} rev {} gia' presente con jsonflusso, salto.", METHOD, fdr, revision );
			logEsitoFlusso ( organizationId, fdr, revision, pspId, null, null, "SKIPPED", "SKIPPED", "gia_presente_con_jsonflusso" );
			return false;
		}

		// PASSO 7: insert tracciatura con stato DA_ELABORARE
		var tracciatureDAO = new InserisciTracciaturAcquisizioneFlussoDAO ();
		var idTracciatura = tracciatureDAO.inserisci ( fdr, revision, organizationId, pspId, InserisciTracciaturAcquisizioneFlussoDAO.STATO_DA_ELABORARE );

		// PASSO 8: GET dettaglio flusso (testata)
		var singolaDAO = new InserisciTracciaturaSingolaAcquisizioneDAO ();
		var idSingola = singolaDAO.inserisci ( idTracciatura, null );

		FdrDettaglio dettaglio;
		try {
			dettaglio = client.getSingleFdr ( organizationId, fdr, revision, pspId );
		} catch ( Exception e ) {
			log.error ( "Errore GET dettaglio flusso {}", fdr, e );
			var nota = "Errore chiamata getSingleFdr: " + e.getMessage ();
			tracciatureDAO.aggiornaStato ( idTracciatura, InserisciTracciaturAcquisizioneFlussoDAO.STATO_SCARTATO, nota );
			singolaDAO.aggiorna ( idSingola, "KO", null, nota );
			logEsitoFlusso ( organizationId, fdr, revision, pspId, idTracciatura, idSingola,
							InserisciTracciaturAcquisizioneFlussoDAO.STATO_SCARTATO, "KO", nota );
			return true;
		}

		// PASSO 9: CONTROLLO metadati flusso
		if ( !"PUBLISHED".equals ( dettaglio.getStatus () ) ) {
			var nota = "Flusso non in stato PUBLISHED: " + dettaglio.getStatus ();
			log.info ( "{} - flusso: {}", nota, fdr );
			tracciatureDAO.aggiornaStato ( idTracciatura, InserisciTracciaturAcquisizioneFlussoDAO.STATO_SCARTATO, nota );
			singolaDAO.aggiorna ( idSingola, "KO", null, nota );
			logEsitoFlusso ( organizationId, fdr, revision, pspId, idTracciatura, idSingola,
							InserisciTracciaturAcquisizioneFlussoDAO.STATO_SCARTATO, "KO", nota );
			return false;
		}

		if ( !dettaglio.getComputedTotPayments ().equals ( dettaglio.getTotPayments () ) || Double.compare ( dettaglio.getComputedSumPayments (),
						dettaglio.getSumPayments () ) != 0 ) {
			var nota = "Flusso non arrivato completo: " +
							"computedTot=" + dettaglio.getComputedTotPayments () +
							" totPayments=" + dettaglio.getTotPayments () +
							" computedSum=" + dettaglio.getComputedSumPayments () +
							" sumPayments=" + dettaglio.getSumPayments ();
			log.info ( "{} - flusso: {}", nota, fdr );
			tracciatureDAO.aggiornaStato ( idTracciatura, InserisciTracciaturAcquisizioneFlussoDAO.STATO_SCARTATO, nota );
			singolaDAO.aggiorna ( idSingola, "KO", null, nota );
			logEsitoFlusso ( organizationId, fdr, revision, pspId, idTracciatura, idSingola,
							InserisciTracciaturAcquisizioneFlussoDAO.STATO_SCARTATO, "KO", nota );
			return false;
		}

		// PASSO 12-13: GET pagamenti con paginazione
		PagopaFdrRestClient.PaymentsResponse paymentsResponse;
		try {
			paymentsResponse = client.getAllPayments ( organizationId, fdr, revision, pspId );
		} catch ( Exception e ) {
			log.error ( "Errore GET pagamenti flusso {}", fdr, e );
			var nota = "Errore chiamata getAllPayments: " + e.getMessage ();
			tracciatureDAO.aggiornaStato ( idTracciatura, InserisciTracciaturAcquisizioneFlussoDAO.STATO_SCARTATO, nota );
			singolaDAO.aggiorna ( idSingola, "KO", null, nota );
			logEsitoFlusso ( organizationId, fdr, revision, pspId, idTracciatura, idSingola,
							InserisciTracciaturAcquisizioneFlussoDAO.STATO_SCARTATO, "KO", nota );
			return true;
		}

		singolaDAO.aggiornaNumPagamenti ( idSingola, paymentsResponse.countTotaleDichiarato () );

		// PASSO 14: costruisci JSON_TESTATA + JSON_PAYMENTS
		var jsonFlusso = costruisciJsonFlusso ( dettaglio.getRawJson (), paymentsResponse.pagamenti () );

		// PASSO 15: verifica somma importi pagamenti
		var sommaPagamenti = paymentsResponse.pagamenti ().stream ()
						.mapToDouble ( p -> p.path ( "pay" ).asDouble ( 0 ) )
						.sum ();

		// Confronto con tolleranza per double
		if ( Math.abs ( sommaPagamenti - dettaglio.getSumPayments () ) > 0.001 ) {
			var nota = "Somma importi pagamenti non coerente: " +
							"calcolata=" + sommaPagamenti +
							" sumPayments=" + dettaglio.getSumPayments ();
			log.info ( "{} - flusso: {}", nota, fdr );
			tracciatureDAO.aggiornaStato ( idTracciatura, InserisciTracciaturAcquisizioneFlussoDAO.STATO_SCARTATO, nota );
			singolaDAO.aggiorna ( idSingola, "KO", jsonFlusso, nota );
			logEsitoFlusso ( organizationId, fdr, revision, pspId, idTracciatura, idSingola,
							InserisciTracciaturAcquisizioneFlussoDAO.STATO_SCARTATO, "KO", nota );
			return false;
		}

		// PASSO 16-17: salva in flusso_riversamento e flusso_singolo_pagamento
		try {
			salvaFlussoEPagamenti ( fdrItem, dettaglio, jsonFlusso, paymentsResponse.pagamenti (), key, organizationId );
		} catch ( Exception e ) {
			var nota = "Errore salvataggio flusso/pagamenti: " + e.getMessage ();
			tracciatureDAO.aggiornaStato ( idTracciatura, InserisciTracciaturAcquisizioneFlussoDAO.STATO_SCARTATO, nota );
			singolaDAO.aggiorna ( idSingola, "KO", jsonFlusso, nota );
			logEsitoFlusso ( organizationId, fdr, revision, pspId, idTracciatura, idSingola,
							InserisciTracciaturAcquisizioneFlussoDAO.STATO_SCARTATO, "KO", nota );
			throw e;
		}

		singolaDAO.aggiorna ( idSingola, "OK", null, null );
		tracciatureDAO.aggiornaStato ( idTracciatura, InserisciTracciaturAcquisizioneFlussoDAO.STATO_ELABORATO, null );
		logEsitoFlusso ( organizationId, fdr, revision, pspId, idTracciatura, idSingola,
						InserisciTracciaturAcquisizioneFlussoDAO.STATO_ELABORATO, "OK", "elaborazione_completata" );

		log.info ( "Flusso {} elaborato con successo.", fdr );
		return false;
	}

	private void logEsitoFlusso ( String organizationId,
							 String fdr,
							 Integer revision,
							 String pspId,
							 Integer idTracciatura,
							 Integer idSingola,
							 String stato,
							 String esito,
							 String motivo ) {
		log.info ( LOG_ESITO_FLUSSO,
					organizationId,
					fdr,
					revision,
					pspId,
					idTracciatura,
					idSingola,
					stato,
					esito,
					motivo );
	}

	/**
	 * Costruisce il JSON finale: { "header": <testata>, "data": [<pagamenti>] }
	 * come da §File JSON flusso BASE del CDU.
	 */
	private String costruisciJsonFlusso ( JsonNode testata, List<JsonNode> pagamenti ) throws Exception {
		var root = objectMapper.createObjectNode ();
		root.set ( "header", testata );
		root.set ( "data", objectMapper.valueToTree ( pagamenti ) );
		return objectMapper.writeValueAsString ( root );
	}

	private void salvaFlussoEPagamenti ( FdrItem fdrItem, FdrDettaglio dettaglio,
					String jsonFlusso, List<JsonNode> pagamenti, byte[] key, String organizationId ) throws Exception {

		var idFlusso = new InserisciFlussoRiversamentoRestDAO (
						fdrItem, dettaglio, jsonFlusso ).executeUpdate ();

		var pspId = asNullableText ( dettaglio.getRawJson ()
						.path ( "sender" )
						.path ( "pspId" ) );
		var pspName = asNullableText ( dettaglio.getRawJson ()
						.path ( "sender" )
						.path ( "pspName" ) );

		var rtService = new InviaRTAlFruitoreService ( key );

		for ( var pagamento : pagamenti ) {
			var idPagamento = new InserisciFlussoSingoloPagamentoRestDAO ( idFlusso, pagamento, organizationId ).executeUpdate ();

			// Gestione NO_RPT: invio RT al fruitore come da logica vecchio batch
			if ( "NO_RPT".equals ( asNullableText ( pagamento.path ( "payStatus" ) ) ) ) {
				log.info ( "salvaFlussoEPagamenti - Pagamento NO_RPT rilevato per IUV: {} - avvio invio RT fruitore", pagamento.path ( "iuv" ).asText () );
				rtService.gestisciNoRpt ( idPagamento, pagamento, pspId, pspName );
			}
		}
	}

	private String asNullableText ( JsonNode node ) {
		return ( node == null || node.isNull () || node.isMissingNode () )
						? null
						: node.asText ();
	}
}