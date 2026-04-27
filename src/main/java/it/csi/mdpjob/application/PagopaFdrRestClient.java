/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.csi.mdpjob.dto.FdrDettaglio;
import it.csi.mdpjob.dto.FdrItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;


public class PagopaFdrRestClient {

	private static final Logger log = LoggerFactory.getLogger ( PagopaFdrRestClient.class );

	private static final int MAX_RETRY = 5;

	private static final long RETRY_SLEEP_MS = 3000;

	private static final int PAGE_SIZE = 1000;

	private final String baseUrl;

	private final String subscriptionKey;

	private final HttpClient httpClient;

	private final ObjectMapper objectMapper;

	public PagopaFdrRestClient ( String baseUrl, String subscriptionKey ) {
		this.baseUrl = baseUrl.endsWith ( "/" ) ? baseUrl : baseUrl + "/";
		this.subscriptionKey = subscriptionKey;
		this.httpClient = HttpClient.newHttpClient ();
		this.objectMapper = new ObjectMapper ();
	}

	/**
	 * GET /organizations/{orgId}/fdrs
	 * Recupero paginato di tutti i flussi per un EC.
	 * Restituisce anche il count totale dichiarato da pagoPA per il controllo.
	 */
	public FdrsResponse getAllFdrs ( String organizationId, String pspId, String publishedGt, String flowDate ) throws Exception {
		List<FdrItem> tuttiIFlussi = new ArrayList<> ();
		var countTotaleDichiarato = 0;
		var pageNumber = 1;

		while ( true ) {
			var url = new StringBuilder ( baseUrl )
							.append ( "organizations/" ).append ( organizationId ).append ( "/fdrs" )
							.append ( "?page=" ).append ( pageNumber )
							.append ( "&size=" ).append ( PAGE_SIZE );

			if ( pspId != null && !pspId.isBlank () ) {
				url.append ( "&pspId=" ).append ( pspId );
			}
			if ( publishedGt != null && !publishedGt.isBlank () ) {
				url.append ( "&publishedGt=" ).append ( publishedGt );
			}
			if ( flowDate != null && !flowDate.isBlank () ) {
				url.append ( "&flowDate=" ).append ( flowDate );
			}

			var response = chiamaConRetry ( url.toString () );

			var data = response.path ( "data" );
			var count = response.path ( "count" ).asInt ( 0 );
			countTotaleDichiarato += count;

			if ( data.isArray () ) {
				for ( var node : data ) {
					var item = new FdrItem ();
					item.setFdr ( node.path ( "fdr" ).asText ( null ) );
					item.setPspId ( node.path ( "pspId" ).asText ( null ) );
					item.setRevision ( node.path ( "revision" ).asInt ( 1 ) );
					item.setPublished ( node.path ( "published" ).asText ( null ) );
					item.setFlowDate ( node.path ( "flowDate" ).asText ( null ) );
					tuttiIFlussi.add ( item );
				}
			}

			// Fine paginazione: count < pageSize = ultima pagina
			//if ( count < PAGE_SIZE ) { questo non dovrebbe servire
			//	break;
			//}

			var totPage = response.path ( "metadata" ).path ( "totPage" ).asInt ( 1 );
			if ( pageNumber >= totPage ) {
				break;
			}

			pageNumber++;
		}

		return new FdrsResponse ( tuttiIFlussi, countTotaleDichiarato );
	}

	/**
	 * GET /organizations/{orgId}/fdrs/{fdr}/revisions/{rev}/psps/{pspId}
	 * Dettaglio testata del singolo flusso.
	 */
	public FdrDettaglio getSingleFdr ( String organizationId, String fdr, Integer revision, String pspId ) throws Exception {
		var url = baseUrl
						+ "organizations/" + organizationId
						+ "/fdrs/" + fdr
						+ "/revisions/" + revision
						+ "/psps/" + pspId;

		var response = chiamaConRetry ( url );

		var dettaglio = new FdrDettaglio ();
		dettaglio.setStatus ( response.path ( "status" ).asText ( null ) );
		dettaglio.setRevision ( response.path ( "revision" ).asInt () );
		dettaglio.setFdr ( response.path ( "fdr" ).asText ( null ) );
		dettaglio.setTotPayments ( response.path ( "totPayments" ).asInt ( 0 ) );
		dettaglio.setSumPayments ( response.path ( "sumPayments" ).asDouble ( 0 ) );
		dettaglio.setComputedTotPayments ( response.path ( "computedTotPayments" ).asInt ( 0 ) );
		dettaglio.setComputedSumPayments ( response.path ( "computedSumPayments" ).asDouble ( 0 ) );
		dettaglio.setRawJson ( response );
		return dettaglio;
	}

	/**
	 * GET /organizations/{orgId}/fdrs/{fdr}/revisions/{rev}/psps/{pspId}/payments
	 * Recupero paginato di tutti i pagamenti del flusso.
	 * Restituisce i pagamenti e il JSON aggregato per jsonflusso.
	 */
	public PaymentsResponse getAllPayments ( String organizationId, String fdr, Integer revision, String pspId ) throws Exception {
		List<JsonNode> tuttiIPagamenti = new ArrayList<> ();
		var countTotaleDichiarato = 0;
		var pageNumber = 1;

		while ( true ) {
			var url = baseUrl
							+ "organizations/" + organizationId
							+ "/fdrs/" + fdr
							+ "/revisions/" + revision
							+ "/psps/" + pspId
							+ "/payments"
							+ "?page=" + pageNumber
							+ "&size=" + PAGE_SIZE;

			var response = chiamaConRetry ( url );

			var data = response.path ( "data" );
			var count = response.path ( "count" ).asInt ( 0 );
			countTotaleDichiarato += count;

			if ( data.isArray () ) {
				for ( var node : data ) {
					tuttiIPagamenti.add ( node );
				}
			}

			//if ( count < PAGE_SIZE ) { questo non dovrebbe servire
			//	break;
			//}

			var totPage = response.path ( "metadata" ).path ( "totPage" ).asInt ( 1 );
			if ( pageNumber >= totPage ) {
				break;
			}

			pageNumber++;
		}

		return new PaymentsResponse ( tuttiIPagamenti, countTotaleDichiarato );
	}

	private JsonNode chiamaConRetry ( String url ) throws Exception {
		Exception ultimaEccezione = null;
		for ( var i = 1; i <= MAX_RETRY; i++ ) {
			try {
				var request = HttpRequest.newBuilder ()
								.uri ( URI.create ( url ) )
								.header ( "Ocp-Apim-Subscription-Key", subscriptionKey )
								.GET ()
								.build ();

				log.info ( "HTTP {} {} - headers: {}", request.method (), request.uri (), request.headers ().map () ); // solo per locale, poi togliere

				var response = httpClient.send ( request, HttpResponse.BodyHandlers.ofString () );

				log.info ( "HTTP response {} - body: {}", response.statusCode (), response.body () ); // solo per locale, poi togliere

				if ( response.statusCode () == 200 ) {
					return objectMapper.readTree ( response.body () );
				}

				// Errori 4xx: permanenti, inutile ritentare
				int statusCode = response.statusCode ();
				if ( statusCode >= 400 && statusCode < 500 ) {
					String motivo = descriviErrore4xx ( statusCode, response.body () );
					throw new NonRetryableException ( "HTTP " + statusCode + " per URL: " + url + " - " + motivo + " - body: " + response.body () );
				}

				throw new Exception ( "HTTP " + statusCode + " per URL: " + url + " - body: " + response.body () );

			} catch ( NonRetryableException e ) {
				// Errore permanente (4xx): non ritentare, rilancia subito
				log.warn ( "Errore non recuperabile (nessun retry): {}", e.getMessage () );
				throw e;
			} catch ( Exception e ) {
				ultimaEccezione = e;
				log.error ( "Tentativo {}/" + MAX_RETRY + " fallito per: {} - {}", i, url, e.getMessage () );

				if ( i < MAX_RETRY ) {
					Thread.sleep ( RETRY_SLEEP_MS );
				}
			}
		}
		throw new Exception ( "Tutti i " + MAX_RETRY + " tentativi falliti per: " + url, ultimaEccezione );
	}

	/**
	 * Descrive in italiano il motivo di un errore 4xx per un log più parlante.
	 */
	private String descriviErrore4xx ( int statusCode, String body ) {
		if ( statusCode == 403 ) {
			return "Accesso negato dall'API gateway (ente non autorizzato per questa subscription key o non presente in ambiente PagoPA UAT)";
		}
		if ( statusCode == 400 ) {
			if ( body != null && body.contains ( "FDR-2008" ) ) {
				return "Ente non registrato nell'ambiente PagoPA (creditor institution invalid or unknown - FDR-2008): verificare che l'organizationId sia onboardato in UAT";
			}
			return "Richiesta non valida (400 Bad Request)";
		}
		if ( statusCode == 404 ) {
			return "Risorsa non trovata (404 Not Found)";
		}
		return "Errore client HTTP " + statusCode;
	}

	/** Eccezione che segnala un errore 4xx non ritentabile. */
	private static class NonRetryableException extends Exception {
		private static final long serialVersionUID = 1L;

		public NonRetryableException ( String message ) {
			super ( message );
		}
	}

	// --- Classi interne per le response aggregate ---


	public record FdrsResponse(List<FdrItem> flussi, int countTotaleDichiarato) {

	}


	public record PaymentsResponse(List<JsonNode> pagamenti, int countTotaleDichiarato) {

	}
}
