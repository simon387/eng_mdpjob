/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.application;

import com.fasterxml.jackson.databind.JsonNode;
import it.csi.mdp.clientmod3.EsitoRiceviEsito;
import it.csi.mdp.clientmod3.ParametriRiceviEsito;
import it.csi.mdp.clientmod3.Serviziorissrvspc;
import it.csi.mdpjob.dao.ParametriNodoSpcDAO;
import it.csi.mdpjob.util.connection.ConnectionManager;
import it.csi.mdpjob.util.connection.ConnectionManagerFactory;
import it.csi.mdpjob.util.mail.MailData;
import it.csi.mdpjob.util.mail.MailUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.internet.AddressException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.GregorianCalendar;

import static it.csi.mdpjob.util.connection.ConnectionUtil.commonGetAppId;


/**
 * Gestisce l'invio della RT al fruitore per i pagamenti NO_RPT (codice 9).
 * Duplica la logica di InvocaServizioFlussiRendicontazione.inviaRTAlFruitore
 * adattata al formato JSON del nuovo batch INMDJB110.
 */
public class InviaRTAlFruitoreService {

	private static final Logger log = LoggerFactory.getLogger ( InviaRTAlFruitoreService.class );

	// Costanti replicate da CostantiNodoSpc (non accessibile in questo progetto)
	private static final String APP_PARAM_ENDPOINT_FRUITORE = "endpointServiziNodo";

	private static final String APP_PARAM_PASSPHRASE_FRUITORE = "passphrase";

	private final SecureRandom random = new SecureRandom ();

	private final byte[] key;

	public InviaRTAlFruitoreService ( byte[] key ) {
		this.key = key;
	}

	/**
	 * Punto di ingresso principale: gestisce invio RT e aggiorna i campi
	 * esito/msg/data sul singolo pagamento già inserito in DB.
	 * Chiamare dopo l'insert in flusso_singolo_pagamento per payStatus=NO_RPT.
	 *
	 * @param idSingoloPagamento id del record in flusso_singolo_pagamento da aggiornare
	 * @param pagamento          JsonNode del pagamento dalla response pagoPA
	 * @param pspId              identificativo PSP dal flusso (sender.pspId)
	 * @param pspName            nome PSP dal flusso (sender.pspName) = istitutoMittente
	 */
	public void gestisciNoRpt ( Integer idSingoloPagamento, JsonNode pagamento, String pspId, String pspName ) {
		var METHOD = "gestisciNoRpt";
		var iuv = pagamento.path ( "iuv" ).asText ( null );
		var iur = pagamento.path ( "iur" ).asText ( null );
		var payDateStr = pagamento.path ( "payDate" ).asText ( null );
		var pay = pagamento.path ( "pay" ).asDouble ( 0 );

		try {
			var esito = inviaRTAlFruitore ( iuv, iur, payDateStr, pay, pspId, pspName );

			if ( esito != null ) {
				var esitoStr = esito.getEsito ();
				var codiceErrore = esito.getCodiceErrore ();
				var messaggioErrore = esito.getMessaggioErrore ();

				var msg = "";
				if ( codiceErrore != null ) {
					msg = msg.concat ( codiceErrore );
				}
				if ( codiceErrore != null && messaggioErrore != null ) {
					msg = msg.concat ( " - " );
				}
				if ( messaggioErrore != null ) {
					msg = msg.concat ( messaggioErrore );
				}

				aggiornaEsitoInvioFruitore ( idSingoloPagamento, esitoStr, org.apache.commons.lang.StringUtils.substring ( msg, 0, 250 ) );
			}

		} catch ( Exception e ) {
			log.error ( "{} - Errore invio RT al fruitore per pagamento NO_RPT con IUV {}", METHOD, iuv );

			// Notifica asincrona via mail (come nel vecchio batch)
			avviaNotificaMailFallimento ( iuv, iur, ExceptionUtils.getStackTrace ( e ) );

			aggiornaEsitoInvioFruitore ( idSingoloPagamento, "KO",
							org.apache.commons.lang.StringUtils.substring ( "Errore invio RT fruitore NO_RPT - " + e.getMessage (), 0, 250 ) );
		}

		// In ogni caso aggiorna data_ultimo_invio_a_fruitore
		aggiornaDataInvioFruitore ( idSingoloPagamento );
	}

	/**
	 * Logica core: replica esatta di inviaRTAlFruitore del vecchio batch,
	 * adattata per ricevere i dati già estratti dal JSON invece di CtDatiSingoliPagamenti.
	 */
	private EsitoRiceviEsito inviaRTAlFruitore ( String iuv, String iur,
					String payDateStr, double pay,
					String pspId, String istitutoMittente )
					throws Exception {

		var sdf = new SimpleDateFormat ( "ddMMyyyy-hh:mm:ss:ms" );
		var timestamp = sdf.format ( new Date () );

		// Recupera applicationId da iuv_ottici (con fallback su rpt)
		var applicationId = recuperaApplicationId ( iuv );
		log.info ( "inviaRTAlFruitore - APPLICATION ID: {}", applicationId );

		if ( applicationId == null ) {
			log.info ( "inviaRTAlFruitore - Impossibile inviare RT per IUV {}: applicationId non trovato.", iuv );
			return null;
		}

		// Recupera transactionId da transazione_iuv
		var transactionId = recuperaTransactionId ( iuv );
		log.info ( "inviaRTAlFruitore - TRANSACTION ID: {}", transactionId );

		// Recupera parametri applicazione (endpoint fruitore, passphrase)
		var mappaParams = new ParametriNodoSpcDAO ( key ).getMappaApplicationCustomFieldsEnabled ( applicationId );

		var endpointFruitore = mappaParams.get ( APP_PARAM_ENDPOINT_FRUITORE );
		var passphrase = mappaParams.get ( APP_PARAM_PASSPHRASE_FRUITORE );

		if ( endpointFruitore == null || endpointFruitore.isBlank () ) {
			log.info ( "inviaRTAlFruitore - Endpoint fruitore non configurato per applicationId {}", applicationId );
			return null;
		}

		// Costruisce il client SOAP verso il fruitore
		var factory = new JaxWsProxyFactoryBean ();
		factory.getInInterceptors ().add ( new LoggingInInterceptor () );
		factory.getOutInterceptors ().add ( new LoggingOutInterceptor () );
		factory.setServiceClass ( Serviziorissrvspc.class );
		factory.setAddress ( endpointFruitore );
		var iPagNodo = (Serviziorissrvspc) factory.create ();

		// Genera stringa random 35 chars con prefisso MDP (identico al vecchio batch)
		var stringa35Random = new BigInteger ( 160, random ).toString ( 32 );
		if ( stringa35Random.length () > 32 ) {
			stringa35Random = stringa35Random.substring ( 32 );
		}
		stringa35Random = "MDP" + stringa35Random;

		// Converte payDate da stringa ISO a XMLGregorianCalendar
		var payDateXml = parseToXmlGregorianCalendar ( payDateStr );

		// Compone i parametri della chiamata riceviEsito
		var parametriRiceviRT = new ParametriRiceviEsito ();
		parametriRiceviRT.setApplicationId ( applicationId );
		parametriRiceviRT.setCodEsitoPagamento ( "9" ); // NO_RPT = codice 9
		parametriRiceviRT.setDataOraMsgRicevuta ( payDateXml );
		parametriRiceviRT.setDataEsitoSingoloPagamento ( payDateXml );
		parametriRiceviRT.setDescEsitoPagamento ( "Pagamento eseguito in assenza di RT" );
		parametriRiceviRT.setIuv ( iuv );
		parametriRiceviRT.setMac ( generaMac ( passphrase, applicationId, iuv, timestamp, stringa35Random ) );
		parametriRiceviRT.setIdMsgRicevuta ( stringa35Random );
		parametriRiceviRT.setTimestamp ( timestamp );
		parametriRiceviRT.setIdentificativoUnivocoRiscossione ( iur );
		parametriRiceviRT.setImportoPagato ( BigDecimal.valueOf ( pay ) );
		parametriRiceviRT.setRtPresente ( false );
		parametriRiceviRT.setIdentificativoPSP ( pspId );
		parametriRiceviRT.setDenominazionePSP ( istitutoMittente );

		return iPagNodo.riceviEsito ( parametriRiceviRT );
	}

	/**
	 * Replica esatta di generaMacVersamento del vecchio batch.
	 */
	private String generaMac ( String passphrase, String applicationId, String iuv, String timestamp, String stringa35Random ) {
		var sToDigest = passphrase + "%%%%" + applicationId + iuv + stringa35Random + timestamp + "%%%%" + passphrase;
		log.info ( "generaMac - Stringa da firmare: {}", sToDigest );
		var bMac = DigestUtils.sha256 ( sToDigest.getBytes () );
		var mac = Base64.encodeBase64String ( bMac );
		return mac.substring ( 0, 35 );
	}

	/**
	 * Recupera applicationId da iuv_ottici, fallback su rpt.
	 * Replica la logica di EstraiApplicationIdDaIuvDAO + EstraiApplicationIdDaRPTDAO.
	 */
	private String recuperaApplicationId ( String iuv ) {
		return commonGetAppId (iuv, null );
	}

	/**
	 * Recupera transactionId da transazione_iuv.
	 * Replica la logica di EstraiTransactionIdDaIuvDAO.
	 */
	private String recuperaTransactionId ( String iuv ) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionManagerFactory.getInstance ().getConnection ();
			stmt = conn.prepareStatement ( "SELECT transaction_id FROM transazione_iuv WHERE iuv = ? LIMIT 1" );
			stmt.setString ( 1, iuv );
			rs = stmt.executeQuery ();
			if ( rs.next () ) {
				return rs.getString ( "transaction_id" );
			}
		} catch ( Exception e ) {
			log.error ( "recuperaTransactionId - Errore recupero transactionId per IUV {}", iuv, e );
		} finally {
			ConnectionManager.closeResultSet ( rs );
			ConnectionManager.closeStatement ( stmt );
			ConnectionManager.closeConnection ( conn );
		}
		return null;
	}

	/**
	 * Converte una stringa ISO 8601 (formato pagoPA) in XMLGregorianCalendar.
	 */
	private XMLGregorianCalendar parseToXmlGregorianCalendar ( String dateStr ) {
		if ( dateStr == null || dateStr.isBlank () ) {
			return null;
		}
		try {
			var odt = OffsetDateTime.parse ( dateStr );
			var gc = GregorianCalendar.from ( odt.toZonedDateTime () );
			return DatatypeFactory.newInstance ().newXMLGregorianCalendar ( gc );
		} catch ( Exception e ) {
			log.error ( "parseToXmlGregorianCalendar - Impossibile parsare data: {} - {}", dateStr, e.getMessage () );
			return null;
		}
	}

	// --- Aggiornamenti DB su flusso_singolo_pagamento ---
	private void aggiornaEsitoInvioFruitore ( Integer id, String esito, String msg ) {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = ConnectionManagerFactory.getInstance ().getConnection ();
			stmt = conn.prepareStatement (
							"UPDATE flusso_singolo_pagamento " +
											"SET esito_ultimo_invio_a_fruitore = ?, " +
											"    msg_ultimo_esito_invio_a_fruitore = ? " +
											"WHERE id = ?"
			);
			stmt.setString ( 1, esito );
			stmt.setString ( 2, msg );
			stmt.setInt ( 3, id );
			stmt.executeUpdate ();
		} catch ( Exception e ) {
			log.error ( "aggiornaEsitoInvioFruitore - Errore aggiornamento esito per id {}", id, e );
		} finally {
			ConnectionManager.closeStatement ( stmt );
			ConnectionManager.closeConnection ( conn );
		}
	}

	private void aggiornaDataInvioFruitore ( Integer id ) {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = ConnectionManagerFactory.getInstance ().getConnection ();
			stmt = conn.prepareStatement (
							"UPDATE flusso_singolo_pagamento " +
											"SET data_ultimo_invio_a_fruitore = NOW() " +
											"WHERE id = ?"
			);
			stmt.setInt ( 1, id );
			stmt.executeUpdate ();
		} catch ( Exception e ) {
			log.error ( "aggiornaDataInvioFruitore - Errore aggiornamento data per id {}", id, e );
		} finally {
			ConnectionManager.closeStatement ( stmt );
			ConnectionManager.closeConnection ( conn );
		}
	}

	// --- Notifica mail asincrona in caso di fallimento ---
	private void avviaNotificaMailFallimento ( String iuv, String iur, String stackTrace ) {
		var t = new Thread ( new NotificaFallitoInvioRTThread ( iuv, iur, stackTrace, key ) );
		t.start ();
	}

	static class NotificaFallitoInvioRTThread implements Runnable {

		private final String iuv;

		private final String iur;

		private final String stackTrace;

		private final byte[] key;

		NotificaFallitoInvioRTThread ( String iuv, String iur, String stackTrace, byte[] key ) {
			this.iuv = iuv;
			this.iur = iur;
			this.stackTrace = stackTrace;
			this.key = key;
		}

		@Override
		public void run () {
			try {
				var applicationId = recuperaAppId ( iuv );
				var transactionId = recuperaTransId ( iuv );
				var endPoint = applicationId != null
								? new ParametriNodoSpcDAO ( key )
								  .getMappaApplicationCustomFieldsEnabled ( applicationId )
								  .get ( APP_PARAM_ENDPOINT_FRUITORE )
								: "N/D";

				var mail = getMailData ( transactionId, applicationId, endPoint );
				MailUtil.inviaMail ( mail );
			} catch ( Exception e ) {
				log.error ( "NotificaFallitoInvioRTThread - Errore invio mail notifica fallimento", e );
			}
		}

		private MailData getMailData ( String transactionId, String applicationId, String endPoint ) throws AddressException {
			var mail = new MailData ();
			mail.setTo ( MailUtil.getDestinatarioNotificaFallitoInvioRTFruitore () );
			mail.setSubject ( "Errore notifica RT (flusso singolo riversamento NO_RPT)" );
			mail.setText (
							"Errore durante l'invio della RT per pagamento NO_RPT\n\n" +
											"TRANSACTION ID: '" + transactionId + "'\n" +
											"APPLICATION ID: '" + applicationId + "'\n" +
											"IUV: '" + iuv + "'\n" +
											"IUR: '" + iur + "'\n\n" +
											"END POINT: '" + endPoint + "'\n\n" +
											"Dettagli errore:\n" + stackTrace
			);
			return mail;
		}

		private String recuperaAppId ( String iuv ) {
			Connection conn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				conn = ConnectionManagerFactory.getInstance ().getConnection ();
				stmt = conn.prepareStatement ( "SELECT application_id FROM iuv_ottici WHERE iuv_ottico = ? LIMIT 1" );
				stmt.setString ( 1, iuv );
				rs = stmt.executeQuery ();
				if ( rs.next () )
					return rs.getString ( "application_id" );
			} catch ( Exception e ) {
				log.error ( "recuperaAppId - Errore", e );
			} finally {
				ConnectionManager.closeResultSet ( rs );
				ConnectionManager.closeStatement ( stmt );
				ConnectionManager.closeConnection ( conn );
			}
			return null;
		}

		private String recuperaTransId ( String iuv ) {
			Connection conn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				conn = ConnectionManagerFactory.getInstance ().getConnection ();
				stmt = conn.prepareStatement ( "SELECT transaction_id FROM transazione_iuv WHERE iuv = ? LIMIT 1" );
				stmt.setString ( 1, iuv );
				rs = stmt.executeQuery ();
				if ( rs.next () ) {
					return rs.getString ( "transaction_id" );
				}
			} catch ( Exception e ) {
				log.error ( "recuperaTransId - Errore", e );
			} finally {
				ConnectionManager.closeResultSet ( rs );
				ConnectionManager.closeStatement ( stmt );
				ConnectionManager.closeConnection ( conn );
			}
			return null;
		}
	}
}