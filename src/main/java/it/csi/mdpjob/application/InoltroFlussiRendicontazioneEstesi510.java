/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.application;

import it.csi.mdp.generatedvo.pagamenti.CtRichiestaPagamentoTelematico;
import it.csi.mdp.generatedvo.pagamenti.StTipoIdentificativoUnivocoPersFG;
import it.csi.mdp.generatedvo.pagamentimod3.PaGetPaymentRes;
import it.csi.mdp.generatedvo.pagamentimod3.PaSendRTReq;
import it.csi.mdp.mdpnodospcclient.integration.service.flussiesbext.*;
import it.csi.mdpjob.dao.AggiornaStatoInvioFlussoEstesoDAO;
import it.csi.mdpjob.dao.EstraiFlussiRiversamentoDaInviareEstesiDAO;
import it.csi.mdpjob.dao.InserisciLoggingFlussoEstesoDAO;
import it.csi.mdpjob.dto.ConfigParam510;
import it.csi.mdpjob.dto.DatiRichiesta;
import it.csi.mdpjob.dto.DatiRichiestaGetPayment;
import it.csi.mdpjob.dto.DatiRichiestaReceipt;
import it.csi.mdpjob.dto.FlussoRiversamentoDB;
import it.csi.mdpjob.dto.SingoloPagamentoMultiVersamentoDTO;
import it.csi.mdpjob.dto.gov.CtDatiSingoliPagamenti;
import it.csi.mdpjob.dto.gov.CtFlussoRiversamento;
import it.csi.mdpjob.util.connection.ConnectionManager;
import it.csi.mdpjob.util.connection.ConnectionManagerFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
//import org.apache.ws.security.util.Base64;
import org.apache.geronimo.mail.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class InoltroFlussiRendicontazioneEstesi510 {

	private static final Logger log = LoggerFactory.getLogger ( InoltroFlussiRendicontazioneEstesi510.class );

	private static final String ESITO_OK = "OK";

	private static final String ESITO_KO = "KO";

	private Set<String> listIuvTrattatiSpacchettamento;

	private String elencoCodiciSegregazioneDaTrattare;

	private Map<String, TipoIUV> iuvTrattatiConTipo;


	private enum TipoIUV {RPT, GETPAYMENT, RECEIPT, INTERMEDIATO, NON_INTERMEDIATO, SCONOSCIUTO}

	public void inoltraFlussi ( String urlEndpointServizio, ConfigParam510 config ) throws Exception {
		listIuvTrattatiSpacchettamento = new HashSet<> ();
		iuvTrattatiConTipo = new HashMap<> ();
		elencoCodiciSegregazioneDaTrattare = config.getElencoCodiciSegregazione ();

		if ( StringUtils.isNotEmpty ( elencoCodiciSegregazioneDaTrattare ) ) {
			log.info ( "elencoCodiciSegregazioneDaTrattare: {}", elencoCodiciSegregazioneDaTrattare );
		} else {
			log.info ( "Nessun codice segregazione configurato - verranno elaborati tutti gli IUV" );
		}

		List<FlussoRiversamentoDB> elencoFlussi = new EstraiFlussiRiversamentoDaInviareEstesiDAO (
						config.getLimiteNumFlussiGiornaliero (),
						config.getOrdinamentoFlussi (),
						config.getIdentificativoFlusso (),
						config.getIdentificativoIstitutoRicevente ()
		).executeQuery ();

		log.info ( "Flussi da elaborare: {}", elencoFlussi.size () );

		if ( elencoFlussi.isEmpty () ) {
			log.info ( "Nessun flusso da elaborare. Fine." );
			return;
		}

		ServiziRendicontazioneExt servizio = inizializzaServizioESB ( urlEndpointServizio );
		Unmarshaller unmarshallerFlusso = inizializzaUnMarshallerFlusso ();

		for ( FlussoRiversamentoDB singoloFlusso : elencoFlussi ) {
			List<String> elencoIuvNonTrovati = new ArrayList<> ();
			iuvTrattatiConTipo.clear ();
			listIuvTrattatiSpacchettamento.clear ();

			String idFlusso = singoloFlusso.getIdentificativoFlusso ();
			Timestamp dataOraInvio = new Timestamp ( System.currentTimeMillis () );
			String esito = null;
			String errori = null;
			String warning = null;
			String idMessaggio = null;

			log.info ( "Inizio elaborazione flusso: {} - PSP: {}",
							idFlusso, singoloFlusso.getIdentificativoIstitutoMittente () );

			try {
				CtFlussoRiversamento flusso = (CtFlussoRiversamento) unmarshallerFlusso
								.unmarshal ( new StringReader ( singoloFlusso.getXmlFlusso () ) );

				TrasmettiFlussoRendicontazioneExtRequestType trasmettiFlusso =
								new TrasmettiFlussoRendicontazioneExtRequestType ();

				TestataFlussoRendicontazioneExtType testata = costruisciTestata ( singoloFlusso, flusso );
				trasmettiFlusso.setTestata ( testata );
				idMessaggio = testata.getIdMessaggio ();

				Set<String> distictCodiciVersamento = new HashSet<> ();
				gestioneSingoliPagamenti ( elencoIuvNonTrovati, distictCodiciVersamento, flusso, trasmettiFlusso );

				TestataFlussoRendicontazioneExtType.ElencoCodiciVersamento elencoCV =
								new TestataFlussoRendicontazioneExtType.ElencoCodiciVersamento ();
				elencoCV.getCodiceVersamento ().addAll ( distictCodiciVersamento );
				trasmettiFlusso.getTestata ().setElencoCodiciVersamento ( elencoCV );

				trasmettiFlusso.setFlussoRiversamento ( singoloFlusso.getXmlFlusso ().getBytes () );

				log.info ( "Invio flusso {} - IUV totali: {}, non trovati: {}",
								idFlusso, trasmettiFlusso.getPagamentiIntermediati () != null ?
												trasmettiFlusso.getPagamentiIntermediati ().getPagamentoIntermediato ().size () : 0,
								elencoIuvNonTrovati.size () );

				ResponseType res = servizio.trasmettiFlussoRendicontazioneExt ( trasmettiFlusso );

				if ( "000".equalsIgnoreCase ( res.getResult ().getCodice () ) ) {
					new AggiornaStatoInvioFlussoEstesoDAO ().aggiorna (
									singoloFlusso.getId (), AggiornaStatoInvioFlussoEstesoDAO.STATO_INVIATO );
					esito = ESITO_OK;
					log.info ( "Flusso {} inviato con successo.", idFlusso );
				} else if ( StringUtils.startsWith ( res.getResult ().getCodice (), "0" ) ) {
					// warning non bloccante
					new AggiornaStatoInvioFlussoEstesoDAO ().aggiorna (
									singoloFlusso.getId (), AggiornaStatoInvioFlussoEstesoDAO.STATO_INVIATO );
					esito = ESITO_OK;
					warning = StringUtils.substring ( res.getResult ().getMessaggio (), 0, 255 );
					log.warn ( "Flusso {} inviato con warning: {}", idFlusso, warning );
				} else {
					new AggiornaStatoInvioFlussoEstesoDAO ().aggiorna (
									singoloFlusso.getId (), AggiornaStatoInvioFlussoEstesoDAO.STATO_NON_INVIATO );
					esito = ESITO_KO;
					warning = StringUtils.substring ( res.getResult ().getMessaggio (), 0, 255 );
					log.error ( "Flusso {} non inviato: {}", idFlusso, warning );
				}

			} catch ( Exception e ) {
				log.error ( "Errore elaborazione flusso {}", idFlusso, e );
				errori = StringUtils.substring ( e.getMessage (), 0, 255 );
				// NON aggiorno stato_invio_flusso_esteso in caso di eccezione
				// (rimane a 1 per poter riprocessare)
			} finally {
				if ( !elencoIuvNonTrovati.isEmpty () ) {
					String iuvNonTrovatiStr = "IUV NON TROVATI: " + elencoIuvNonTrovati;
					warning = StringUtils.substring (
									( warning != null ? warning + " - " : "" ) + iuvNonTrovatiStr, 0, 255 );
				}
				try {
					new InserisciLoggingFlussoEstesoDAO ().inserisci (
									idFlusso, singoloFlusso.getDenominazioneMittente (),
									dataOraInvio, errori, warning, esito, idMessaggio );
				} catch ( Exception ex ) {
					log.warn ( "Errore logging flusso {}", idFlusso, ex );
				}
			}
		}
	}

	private void gestioneSingoliPagamenti ( List<String> elencoIuvNonTrovati,
					Set<String> distictCodiciVersamento,
					CtFlussoRiversamento flusso,
					TrasmettiFlussoRendicontazioneExtRequestType flussoExt ) throws Exception {
		for ( CtDatiSingoliPagamenti singoloPagamento : flusso.getDatiSingoliPagamenti () ) {
			String iuv = singoloPagamento.getIdentificativoUnivocoVersamento ();
			log.info ( "Elaborazione IUV: {}", iuv );

			if ( iuvTrattatiConTipo.containsKey ( iuv ) ) {
				log.info ( "IUV {} già trattato - aggiorno testata", iuv );
				aggiornaTestata ( flussoExt, singoloPagamento, iuvTrattatiConTipo.get ( iuv ) );
				continue;
			}

			if ( !isIuvPPAY ( iuv ) ) {
				log.info ( "IUV {} non riconosciuto da PPAY", iuv );
				elaboraNonRiconosciutoPPay ( elencoIuvNonTrovati, distictCodiciVersamento,
								flussoExt, singoloPagamento );
				TipoIUV tipo = "9".equals ( singoloPagamento.getCodiceEsitoSingoloPagamento () )
								? TipoIUV.INTERMEDIATO : TipoIUV.SCONOSCIUTO;
				iuvTrattatiConTipo.put ( iuv, tipo );
				continue;
			}

			String cfEnte = flusso.getIstitutoRicevente ()
							.getIdentificativoUnivocoRicevente ().getCodiceIdentificativoUnivoco ();

			// 1. Cerca getPayment
			DatiRichiestaGetPayment getPaymentDb = cercaGetPayment ( iuv, cfEnte );
			if ( getPaymentDb != null ) {
				log.info ( "IUV {} - getPayment trovata", iuv );
				elaboraGetPayment ( distictCodiciVersamento, flusso, flussoExt, singoloPagamento, getPaymentDb );
				iuvTrattatiConTipo.put ( iuv, TipoIUV.GETPAYMENT );
				continue;
			}

			// 2. Cerca receipt
			DatiRichiestaReceipt receiptDb = cercaReceipt ( iuv, cfEnte );
			if ( receiptDb != null ) {
				log.info ( "IUV {} - receipt trovata", iuv );
				elaboraReceipt ( distictCodiciVersamento, flusso, flussoExt, singoloPagamento, receiptDb, false );
				iuvTrattatiConTipo.put ( iuv, TipoIUV.RECEIPT );
				continue;
			}

			// 3. Cerca RPT
			DatiRichiesta rptDb = cercaRpt ( iuv, cfEnte );
			if ( rptDb != null ) {
				log.info ( "IUV {} - RPT trovata", iuv );
				elaboraRPT ( distictCodiciVersamento, flussoExt, singoloPagamento, rptDb );
				iuvTrattatiConTipo.put ( iuv, TipoIUV.RPT );
				continue;
			}

			// 4. Cerca receipt sconosciuta
			DatiRichiestaReceipt receiptSconosciuta = cercaReceiptSconosciuta ( iuv );
			if ( receiptSconosciuta != null ) {
				log.info ( "IUV {} - receipt non intermediata da PPAY trovata", iuv );
				elaboraReceipt ( distictCodiciVersamento, flusso, flussoExt, singoloPagamento, receiptSconosciuta, true );
				iuvTrattatiConTipo.put ( iuv, TipoIUV.NON_INTERMEDIATO );
				continue;
			}

			// 5. Non trovato
			log.info ( "IUV {} - non riconosciuto da PPAY", iuv );
			elaboraNonRiconosciutoPPay ( elencoIuvNonTrovati, distictCodiciVersamento,
							flussoExt, singoloPagamento );
			TipoIUV tipo = "9".equals ( singoloPagamento.getCodiceEsitoSingoloPagamento () )
							? TipoIUV.INTERMEDIATO : TipoIUV.SCONOSCIUTO;
			iuvTrattatiConTipo.put ( iuv, tipo );
		}
	}

	// --- Metodi di lookup DB ---

	private DatiRichiestaGetPayment cercaGetPayment ( String iuv, String cfEnte ) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionManagerFactory.getInstance ().getConnection ();
			stmt = conn.prepareStatement (
							"SELECT g.id_getpayment, g.transaction_id, g.application_id, g.xml_getpayment " +
											"FROM mdp_getpayment g " +
											"JOIN mdp_singolo_transfer st ON st.id_getpayment = g.id_getpayment " +
											"WHERE g.creditor_referenceid = ? AND st.fiscal_codepa = ? LIMIT 1"
			);
			stmt.setString ( 1, iuv );
			stmt.setString ( 2, cfEnte );
			rs = stmt.executeQuery ();
			if ( rs.next () ) {
				DatiRichiestaGetPayment dto = new DatiRichiestaGetPayment ();
				dto.setTransactionId ( rs.getString ( "transaction_id" ) );
				dto.setApplicationId ( rs.getString ( "application_id" ) );
				dto.setRptXml ( rs.getBytes ( "xml_getpayment" ) );
				return dto;
			}
			return null;
		} finally {
			ConnectionManager.closeResultSet ( rs );
			ConnectionManager.closeStatement ( stmt );
			ConnectionManager.closeConnection ( conn );
		}
	}

	private DatiRichiestaReceipt cercaReceipt ( String iuv, String cfEnte ) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionManagerFactory.getInstance ().getConnection ();
			stmt = conn.prepareStatement (
							"SELECT r.id, r.transaction_id, r.application_id, r.xml_receipt " +
											"FROM mdp_receipt r " +
											"JOIN mdp_singolo_transfer st ON st.id_receipt = r.id " +
											"WHERE r.creditor_referenceid = ? AND st.fiscal_codepa = ? " +
											"AND r.iuv_sconosciuto IS NOT TRUE LIMIT 1"
			);
			stmt.setString ( 1, iuv );
			stmt.setString ( 2, cfEnte );
			rs = stmt.executeQuery ();
			if ( rs.next () ) {
				DatiRichiestaReceipt dto = new DatiRichiestaReceipt ();
				dto.setTransactionId ( rs.getString ( "transaction_id" ) );
				dto.setRptXml ( rs.getBytes ( "xml_receipt" ) );
				return dto;
			}
			return null;
		} finally {
			ConnectionManager.closeResultSet ( rs );
			ConnectionManager.closeStatement ( stmt );
			ConnectionManager.closeConnection ( conn );
		}
	}

	private DatiRichiesta cercaRpt ( String iuv, String cfEnte ) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionManagerFactory.getInstance ().getConnection ();
			stmt = conn.prepareStatement (
							"SELECT r.transaction_id, r.application_id, r.xml AS rpt_xml " +
											"FROM rpt r " +
											"WHERE r.iuv = ? AND r.codice_fiscale_ente = ? LIMIT 1"
			);
			stmt.setString ( 1, iuv );
			stmt.setString ( 2, cfEnte );
			rs = stmt.executeQuery ();
			if ( rs.next () ) {
				DatiRichiesta dto = new DatiRichiesta ();
				dto.setTransactionId ( rs.getString ( "transaction_id" ) );
				dto.setApplicationId ( rs.getString ( "application_id" ) );
				dto.setRptXml ( rs.getString ( "rpt_xml" ) );
				return dto;
			}
			return null;
		} finally {
			ConnectionManager.closeResultSet ( rs );
			ConnectionManager.closeStatement ( stmt );
			ConnectionManager.closeConnection ( conn );
		}
	}

	private DatiRichiestaReceipt cercaReceiptSconosciuta ( String iuv ) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionManagerFactory.getInstance ().getConnection ();
			stmt = conn.prepareStatement (
							"SELECT transaction_id, xml_receipt FROM mdp_receipt " +
											"WHERE creditor_referenceid = ? AND iuv_sconosciuto = TRUE LIMIT 1"
			);
			stmt.setString ( 1, iuv );
			rs = stmt.executeQuery ();
			if ( rs.next () ) {
				DatiRichiestaReceipt dto = new DatiRichiestaReceipt ();
				dto.setTransactionId ( rs.getString ( "transaction_id" ) );
				dto.setRptXml ( rs.getBytes ( "xml_receipt" ) );
				return dto;
			}
			return null;
		} finally {
			ConnectionManager.closeResultSet ( rs );
			ConnectionManager.closeStatement ( stmt );
			ConnectionManager.closeConnection ( conn );
		}
	}

	// --- Metodi di elaborazione (identici al vecchio) ---

	private void elaboraNonRiconosciutoPPay ( List<String> elencoIuvNonTrovati,
					Set<String> distictCodiciVersamento,
					TrasmettiFlussoRendicontazioneExtRequestType flussoExt,
					CtDatiSingoliPagamenti singoloPagamento ) throws Exception {
		String codiceEsito = singoloPagamento.getCodiceEsitoSingoloPagamento ();
		boolean isNoveOIntermediato = "9".equals ( codiceEsito );

		PagamentoIntermediatoType pagamento = new PagamentoIntermediatoType ();
		DatiSingoloPagamentoType datiSingolo = new DatiSingoloPagamentoType ();

		aggiornaTestata ( flussoExt, singoloPagamento,
						isNoveOIntermediato ? TipoIUV.INTERMEDIATO : TipoIUV.SCONOSCIUTO );

		if ( isNoveOIntermediato ) {
			if ( flussoExt.getPagamentiIntermediati () == null )
				flussoExt.setPagamentiIntermediati ( new TrasmettiFlussoRendicontazioneExtRequestType.PagamentiIntermediati () );
			datiSingolo.setTipoRicevuta ( TipoRicevuta.IUV_SENZA_RICEVUTA );
			datiSingolo.setCategoriaIUV ( CategoriaIUV.INTERM_PPAY );
			datiSingolo.setTransactionId ( "PRD00000000XXX" );

			String iuv = singoloPagamento.getIdentificativoUnivocoVersamento ();
			if ( iuv.length () > 17 ) {
				try {
					Integer.parseInt ( iuv.substring ( 13, 14 ) );
					datiSingolo.setCodiceVersamento ( iuv.substring ( 15, 19 ) );
				} catch ( NumberFormatException e ) {
					datiSingolo.setCodiceVersamento ( iuv.substring ( 13, 17 ) );
				}
			} else {
				String cov = recuperaCovDaIuvOttici ( iuv );
				if ( cov != null ) {
					datiSingolo.setCodiceVersamento ( cov );
				} else {
					elencoIuvNonTrovati.add ( iuv );
				}
			}

			datiSingolo.setDescrizioneCausaleVersamento (
							"/RFB/" + iuv + "/" + singoloPagamento.getSingoloImportoPagato () + "/TXT/Assenza RPT" );
			flussoExt.getPagamentiIntermediati ().getPagamentoIntermediato ().add ( pagamento );
		} else {
			if ( flussoExt.getPagamentiSconosciuti () == null )
				flussoExt.setPagamentiSconosciuti ( new TrasmettiFlussoRendicontazioneExtRequestType.PagamentiIntermediati () );
			datiSingolo.setTipoRicevuta ( TipoRicevuta.SCONOSCIUTO );
			datiSingolo.setCategoriaIUV ( CategoriaIUV.SCONOSCIUT_PPAY );
			datiSingolo.setTransactionId ( "----" );
			datiSingolo.setCodiceVersamento ( "----" );
			datiSingolo.setDescrizioneCausaleVersamento (
							"/RFB/" + singoloPagamento.getIdentificativoUnivocoVersamento ()
											+ "/" + singoloPagamento.getSingoloImportoPagato () + "/TXT/PagamentoSconosciuto" );
			flussoExt.getPagamentiSconosciuti ().getPagamentoIntermediato ().add ( pagamento );
		}

		datiSingolo.setDatiSpecificiRiscossione ( "9/000" );
		SoggettoType soggetto = new SoggettoType ();
		PersonaGiuridicaType pg = new PersonaGiuridicaType ();
		pg.setRagioneSociale ( isNoveOIntermediato ? "Assenza RPT" : "----" );
		soggetto.setPersonaGiuridica ( pg );
		soggetto.setIdentificativoUnivocoFiscale ( "01111111111" );
		datiSingolo.setAnagraficaPagatore ( soggetto );
		datiSingolo.setAnagraficaVersante ( soggetto );
		datiSingolo.setIndiceDatiPagamento ( 1 );
		datiSingolo.setIUR ( singoloPagamento.getIdentificativoUnivocoRiscossione () );
		datiSingolo.setIUV ( singoloPagamento.getIdentificativoUnivocoVersamento () );
		datiSingolo.setSingoloImportoPagato ( singoloPagamento.getSingoloImportoPagato () );
		datiSingolo.setCodiceEsitoPagamento ( codiceEsito );
		datiSingolo.setDataEsitoSingoloPagamento ( singoloPagamento.getDataEsitoSingoloPagamento () );
		distictCodiciVersamento.add ( datiSingolo.getCodiceVersamento () );

		PagamentoIntermediatoType.DatiSingoliPagamenti dsp = new PagamentoIntermediatoType.DatiSingoliPagamenti ();
		dsp.setDatiSingoloPagamento ( datiSingolo );
		pagamento.setDatiSingoliPagamenti ( dsp );
	}

	private void elaboraGetPayment ( Set<String> distictCodiciVersamento, CtFlussoRiversamento flusso,
					TrasmettiFlussoRendicontazioneExtRequestType flussoExt,
					CtDatiSingoliPagamenti singoloPagamento,
					DatiRichiestaGetPayment getPaymentDb ) throws Exception {
		aggiornaTestata ( flussoExt, singoloPagamento, TipoIUV.INTERMEDIATO );
		int indice = singoloPagamento.getIndiceDatiSingoloPagamento () != null
						? singoloPagamento.getIndiceDatiSingoloPagamento () - 1 : 0;

		PaGetPaymentRes richiesta = null;
		try {
			richiesta = JAXB.unmarshal (
							new ByteArrayInputStream ( Base64.decode ( new String ( getPaymentDb.getRptXml () ) ) ),
							PaGetPaymentRes.class );
		} catch ( Exception e ) {
			log.error ( "Errore unmarshal getPayment", e );
		}

		List<SingoloPagamentoMultiVersamentoDTO> pag = cercaMultiversamento (
						singoloPagamento.getIdentificativoUnivocoVersamento (),
						flusso.getIstitutoRicevente ().getIdentificativoUnivocoRicevente ().getCodiceIdentificativoUnivoco (),
						"GETPAYMENT" );

		SoggettoType pagatore = new SoggettoType ();
		if ( richiesta != null ) {
			String datiSpec = richiesta.getData ().getTransferList ().getTransfer ().get ( indice ) != null
							? richiesta.getData ().getTransferList ().getTransfer ().get ( indice ).getTransferCategory () : null;
			String causale = richiesta.getData ().getTransferList ().getTransfer ().get ( indice ).getRemittanceInformation ();
			pagatore.setEMail ( richiesta.getData ().getDebtor ().getEMail () );
			pagatore.setIdentificativoUnivocoFiscale (
							richiesta.getData ().getDebtor ().getUniqueIdentifier ().getEntityUniqueIdentifierValue () );
			if ( StTipoIdentificativoUnivocoPersFG.F.equals (
							richiesta.getData ().getDebtor ().getUniqueIdentifier ().getEntityUniqueIdentifierType () ) ) {
				PersonaFisicaType pf = new PersonaFisicaType ();
				pf.setCognome ( richiesta.getData ().getDebtor ().getFullName () );
				pagatore.setPersonaFisica ( pf );
			} else {
				PersonaGiuridicaType pg = new PersonaGiuridicaType ();
				pg.setRagioneSociale ( richiesta.getData ().getDebtor ().getFullName () );
				pagatore.setPersonaGiuridica ( pg );
			}
			if ( flussoExt.getPagamentiIntermediati () == null )
				flussoExt.setPagamentiIntermediati ( new TrasmettiFlussoRendicontazioneExtRequestType.PagamentiIntermediati () );
			flussoExt.getPagamentiIntermediati ().getPagamentoIntermediato ().addAll (
							spacchettamento ( pag, singoloPagamento, pagatore, distictCodiciVersamento,
											getPaymentDb.getTransactionId (), datiSpec, causale, TipoRicevuta.GET_PAYMENT, CategoriaIUV.INTERM_PPAY ) );
		}
	}

	private void elaboraReceipt ( Set<String> distictCodiciVersamento, CtFlussoRiversamento flusso,
					TrasmettiFlussoRendicontazioneExtRequestType flussoExt,
					CtDatiSingoliPagamenti singoloPagamento,
					DatiRichiestaReceipt receiptDb, boolean nonIntermediato ) throws Exception {
		TipoIUV tipoIUV = nonIntermediato ? TipoIUV.NON_INTERMEDIATO : TipoIUV.INTERMEDIATO;
		aggiornaTestata ( flussoExt, singoloPagamento, tipoIUV );
		int indice = singoloPagamento.getIndiceDatiSingoloPagamento () != null
						? singoloPagamento.getIndiceDatiSingoloPagamento () - 1 : 0;

		PaSendRTReq richiesta = null;
		try {
			richiesta = JAXB.unmarshal (
							new ByteArrayInputStream ( Base64.decode ( new String ( receiptDb.getRptXml () ) ) ),
							PaSendRTReq.class );
		} catch ( Exception e ) {
			log.error ( "Errore unmarshal receipt", e );
		}

		List<SingoloPagamentoMultiVersamentoDTO> pag = cercaMultiversamento (
						singoloPagamento.getIdentificativoUnivocoVersamento (),
						flusso.getIstitutoRicevente ().getIdentificativoUnivocoRicevente ().getCodiceIdentificativoUnivoco (),
						"RECEIPT" );

		if ( richiesta != null ) {
			String datiSpec = richiesta.getReceipt ().getTransferList ().getTransfer ().get ( indice ) != null
							? richiesta.getReceipt ().getTransferList ().getTransfer ().get ( indice ).getTransferCategory () : null;
			String causale = richiesta.getReceipt ().getTransferList ().getTransfer ().get ( indice ).getRemittanceInformation ();

			SoggettoType pagatore = new SoggettoType ();
			pagatore.setEMail ( richiesta.getReceipt ().getDebtor ().getEMail () );
			pagatore.setIdentificativoUnivocoFiscale (
							richiesta.getReceipt ().getDebtor ().getUniqueIdentifier ().getEntityUniqueIdentifierValue () );
			if ( StTipoIdentificativoUnivocoPersFG.F.equals (
							richiesta.getReceipt ().getDebtor ().getUniqueIdentifier ().getEntityUniqueIdentifierType () ) ) {
				PersonaFisicaType pf = new PersonaFisicaType ();
				pf.setCognome ( richiesta.getReceipt ().getDebtor ().getFullName () );
				pagatore.setPersonaFisica ( pf );
			} else {
				PersonaGiuridicaType pg = new PersonaGiuridicaType ();
				pg.setRagioneSociale ( richiesta.getReceipt ().getDebtor ().getFullName () );
				pagatore.setPersonaGiuridica ( pg );
			}

			CategoriaIUV categoriaIUV = nonIntermediato ? CategoriaIUV.NON_INTERM_PPAY : CategoriaIUV.INTERM_PPAY;
			if ( nonIntermediato ) {
				if ( flussoExt.getPagamentiNonIntermediati () == null )
					flussoExt.setPagamentiNonIntermediati ( new TrasmettiFlussoRendicontazioneExtRequestType.PagamentiIntermediati () );
				flussoExt.getPagamentiNonIntermediati ().getPagamentoIntermediato ().addAll (
								spacchettamento ( pag, singoloPagamento, pagatore, distictCodiciVersamento,
												receiptDb.getTransactionId (), datiSpec, causale, TipoRicevuta.RECEIPT, categoriaIUV ) );
			} else {
				if ( flussoExt.getPagamentiIntermediati () == null )
					flussoExt.setPagamentiIntermediati ( new TrasmettiFlussoRendicontazioneExtRequestType.PagamentiIntermediati () );
				flussoExt.getPagamentiIntermediati ().getPagamentoIntermediato ().addAll (
								spacchettamento ( pag, singoloPagamento, pagatore, distictCodiciVersamento,
												receiptDb.getTransactionId (), datiSpec, causale, TipoRicevuta.RECEIPT, categoriaIUV ) );
			}
		}
	}

	private void elaboraRPT ( Set<String> distictCodiciVersamento,
					TrasmettiFlussoRendicontazioneExtRequestType flussoExt,
					CtDatiSingoliPagamenti singoloPagamento,
					DatiRichiesta rptDb ) throws Exception {
		aggiornaTestata ( flussoExt, singoloPagamento, TipoIUV.INTERMEDIATO );
		int indice = singoloPagamento.getIndiceDatiSingoloPagamento () != null
						? singoloPagamento.getIndiceDatiSingoloPagamento () - 1 : 0;

		CtRichiestaPagamentoTelematico richiesta = null;
		try {
			richiesta = JAXB.unmarshal (
							new ByteArrayInputStream ( rptDb.getRptXml ().getBytes () ),
							CtRichiestaPagamentoTelematico.class );
		} catch ( Exception e ) {
			log.error ( "Errore unmarshal RPT", e );
		}

		List<SingoloPagamentoMultiVersamentoDTO> pag = cercaMultiversamentoRPT (
						singoloPagamento.getIdentificativoUnivocoVersamento (),
						rptDb.getTransactionId (), rptDb.getApplicationId () );

		if ( richiesta != null ) {
			String datiSpec = richiesta.getDatiVersamento ().getDatiSingoloVersamento ().get ( indice ) != null
							? richiesta.getDatiVersamento ().getDatiSingoloVersamento ().get ( indice ).getDatiSpecificiRiscossione () : null;
			String causale = richiesta.getDatiVersamento ().getDatiSingoloVersamento ().get ( indice ).getCausaleVersamento ();

			SoggettoType pagatore = new SoggettoType ();
			pagatore.setEMail ( richiesta.getSoggettoPagatore ().getEMailPagatore () );
			pagatore.setIdentificativoUnivocoFiscale (
							richiesta.getSoggettoPagatore ().getIdentificativoUnivocoPagatore ().getCodiceIdentificativoUnivoco () );
			if ( StTipoIdentificativoUnivocoPersFG.F.equals (
							richiesta.getSoggettoPagatore ().getIdentificativoUnivocoPagatore ().getTipoIdentificativoUnivoco () ) ) {
				PersonaFisicaType pf = new PersonaFisicaType ();
				pf.setCognome ( richiesta.getSoggettoPagatore ().getAnagraficaPagatore () );
				pagatore.setPersonaFisica ( pf );
			} else {
				PersonaGiuridicaType pg = new PersonaGiuridicaType ();
				pg.setRagioneSociale ( richiesta.getSoggettoPagatore ().getAnagraficaPagatore () );
				pagatore.setPersonaGiuridica ( pg );
			}
			if ( flussoExt.getPagamentiIntermediati () == null )
				flussoExt.setPagamentiIntermediati ( new TrasmettiFlussoRendicontazioneExtRequestType.PagamentiIntermediati () );
			flussoExt.getPagamentiIntermediati ().getPagamentoIntermediato ().addAll (
							spacchettamento ( pag, singoloPagamento, pagatore, distictCodiciVersamento,
											rptDb.getTransactionId (), datiSpec, causale, TipoRicevuta.RPT, CategoriaIUV.INTERM_PPAY ) );
		}
	}

	private List<SingoloPagamentoMultiVersamentoDTO> cercaMultiversamento ( String iuv, String cfEnte, String tipo ) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionManagerFactory.getInstance ().getConnection ();
			String sql;
			if ( "GETPAYMENT".equals ( tipo ) ) {
				sql = "SELECT dsv.posizione, dsv.importosingoloversamento, dsv.causaleversamento, " +
								"dsv.datispecificiriscossione, dsv.annoaccertamento, dsv.numeroaccertamento, " +
								"dsv.applicationid, em.transaction_id " +
								"FROM elemento_multiversamento em " +
								"JOIN dati_singolo_versamento dsv ON dsv.multi_id = em.id " +
								"JOIN mdp_getpayment g ON g.id_elemento_multiversamento = em.id " +
								"JOIN mdp_singolo_transfer st ON st.id_getpayment = g.id_getpayment " +
								"WHERE em.iuv = ? AND st.fiscal_codepa = ?";
			} else {
				sql = "SELECT dsv.posizione, dsv.importosingoloversamento, dsv.causaleversamento, " +
								"dsv.datispecificiriscossione, dsv.annoaccertamento, dsv.numeroaccertamento, " +
								"dsv.applicationid, em.transaction_id " +
								"FROM elemento_multiversamento em " +
								"JOIN dati_singolo_versamento dsv ON dsv.multi_id = em.id " +
								"JOIN mdp_receipt r ON r.id_elemento_multiversamento = em.id " +
								"JOIN mdp_singolo_transfer st ON st.id_receipt = r.id " +
								"WHERE em.iuv = ? AND st.fiscal_codepa = ?";
			}
			stmt = conn.prepareStatement ( sql );
			stmt.setString ( 1, iuv );
			stmt.setString ( 2, cfEnte );
			rs = stmt.executeQuery ();
			return estraiMultiversamento ( rs );
		} catch ( Exception e ) {
			log.error ( "Errore cercaMultiversamento iuv={} tipo={}", iuv, tipo, e );
			return new ArrayList<> ();
		} finally {
			ConnectionManager.closeResultSet ( rs );
			ConnectionManager.closeStatement ( stmt );
			ConnectionManager.closeConnection ( conn );
		}
	}

	private List<SingoloPagamentoMultiVersamentoDTO> cercaMultiversamentoRPT ( String iuv,
					String transactionId,
					String applicationId ) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionManagerFactory.getInstance ().getConnection ();
			stmt = conn.prepareStatement (
							"SELECT dsv.posizione, dsv.importosingoloversamento, dsv.causaleversamento, " +
											"dsv.datispecificiriscossione, dsv.annoaccertamento, dsv.numeroaccertamento, " +
											"dsv.applicationid, em.transaction_id " +
											"FROM elemento_multiversamento em " +
											"JOIN dati_singolo_versamento dsv ON dsv.multi_id = em.id " +
											"WHERE em.iuv = ? AND em.transaction_id = ? AND em.application_id = ?"
			);
			stmt.setString ( 1, iuv );
			stmt.setString ( 2, transactionId );
			stmt.setString ( 3, applicationId );
			rs = stmt.executeQuery ();
			return estraiMultiversamento ( rs );
		} catch ( Exception e ) {
			log.error ( "Errore cercaMultiversamentoRPT iuv={}", iuv, e );
			return new ArrayList<> ();
		} finally {
			ConnectionManager.closeResultSet ( rs );
			ConnectionManager.closeStatement ( stmt );
			ConnectionManager.closeConnection ( conn );
		}
	}

	private List<SingoloPagamentoMultiVersamentoDTO> estraiMultiversamento ( ResultSet rs ) throws Exception {
		List<SingoloPagamentoMultiVersamentoDTO> result = new ArrayList<> ();
		while ( rs.next () ) {
			SingoloPagamentoMultiVersamentoDTO dto = new SingoloPagamentoMultiVersamentoDTO ();
			dto.setPosizione ( rs.getInt ( "posizione" ) );
			dto.setImporto ( rs.getBigDecimal ( "importosingoloversamento" ) );
			dto.setCausale ( rs.getString ( "causaleversamento" ) );
			dto.setDatiSpecificiRiscossione ( rs.getString ( "datispecificiriscossione" ) );
			dto.setAnnoAccertamento ( rs.getInt ( "annoaccertamento" ) );
			dto.setNumeroAccertamento ( rs.getInt ( "numeroaccertamento" ) );
			dto.setApplicationId ( rs.getString ( "applicationid" ) );
			dto.setIuv ( rs.getString ( "transaction_id" ) );
			result.add ( dto );
		}
		return result;
	}

	private String recuperaCovDaIuvOttici ( String iuv ) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionManagerFactory.getInstance ().getConnection ();
			stmt = conn.prepareStatement (
							"SELECT cod_versamento FROM iuv_ottici WHERE iuv_ottico = ? LIMIT 1" );
			stmt.setString ( 1, iuv );
			rs = stmt.executeQuery ();
			if ( rs.next () )
				return rs.getString ( "cod_versamento" );
			return null;
		} catch ( Exception e ) {
			log.error ( "Errore recuperaCovDaIuvOttici iuv={}", iuv, e );
			return null;
		} finally {
			ConnectionManager.closeResultSet ( rs );
			ConnectionManager.closeStatement ( stmt );
			ConnectionManager.closeConnection ( conn );
		}
	}

	// --- Spacchettamento (identico al vecchio) ---

	private List<PagamentoIntermediatoType> spacchettamento ( List<SingoloPagamentoMultiVersamentoDTO> pag,
					CtDatiSingoliPagamenti singoloPagamento,
					SoggettoType pagatore,
					Set<String> distictCodiciVersamento,
					String transactionId, String datiSpec,
					String causale, TipoRicevuta tipoRicevuta,
					CategoriaIUV categoriaIUV ) throws Exception {
		List<PagamentoIntermediatoType> retList = new ArrayList<> ();
		String iuv = singoloPagamento.getIdentificativoUnivocoVersamento ();

		if ( pag.isEmpty () ) {
			SingoloPagamentoMultiVersamentoDTO singoloFarlocco = new SingoloPagamentoMultiVersamentoDTO ();
			singoloFarlocco.setPosizione ( singoloPagamento.getIndiceDatiSingoloPagamento () );
			singoloFarlocco.setCausale ( causale );
			singoloFarlocco.setImporto ( singoloPagamento.getSingoloImportoPagato () );
			singoloFarlocco.setIuv ( iuv );
			singoloFarlocco.setDatiSpecificiRiscossione ( datiSpec );
			pag.add ( singoloFarlocco );
		}

		if ( !listIuvTrattatiSpacchettamento.contains ( pag.get ( 0 ).getIuv () ) ) {
			for ( SingoloPagamentoMultiVersamentoDTO multi : pag ) {
				PagamentoIntermediatoType primoLivello = new PagamentoIntermediatoType ();
				PagamentoIntermediatoType.DatiSingoliPagamenti dsp =
								new PagamentoIntermediatoType.DatiSingoliPagamenti ();
				DatiSingoloPagamentoType terzo = new DatiSingoloPagamentoType ();
				dsp.setDatiSingoloPagamento ( terzo );
				primoLivello.setDatiSingoliPagamenti ( dsp );
				retList.add ( primoLivello );

				terzo.setTipoRicevuta ( tipoRicevuta );
				terzo.setCategoriaIUV ( categoriaIUV );
				terzo.setIUR ( singoloPagamento.getIdentificativoUnivocoRiscossione () );
				terzo.setIUV ( iuv );
				terzo.setTransactionId ( StringUtils.isEmpty ( transactionId ) ? "----" : transactionId );
				terzo.setSingoloImportoPagato ( multi.getImporto () );
				terzo.setAnagraficaPagatore ( pagatore );
				terzo.setAnagraficaVersante ( pagatore );
				terzo.setCodiceEsitoPagamento ( singoloPagamento.getCodiceEsitoSingoloPagamento () );
				terzo.setDataEsitoSingoloPagamento ( singoloPagamento.getDataEsitoSingoloPagamento () );
				terzo.setCodiceVersamento ( determinaCov ( distictCodiciVersamento, iuv, multi.getApplicationId () ) );

				if ( StringUtils.isBlank ( multi.getDatiSpecificiRiscossione () ) ) {
					throw new Exception ( "Dati specifici di riscossione per IUV " + iuv + " non presenti" );
				}
				terzo.setDatiSpecificiRiscossione ( multi.getDatiSpecificiRiscossione () );
				terzo.setDescrizioneCausaleVersamento ( multi.getCausale () );

				if ( multi.getPosizione () != null ) {
					terzo.setIndiceDatiPagamento ( multi.getPosizione () );
				} else {
					terzo.setIndiceDatiPagamento ( 1 );
				}
				if ( multi.getAnnoAccertamento () != null && multi.getAnnoAccertamento () > 0 )
					terzo.setAnnoAccertamento ( multi.getAnnoAccertamento () );
				if ( multi.getNumeroAccertamento () != null && multi.getNumeroAccertamento () > 0 )
					terzo.setNumeroAccertamento ( multi.getNumeroAccertamento () );
			}
			listIuvTrattatiSpacchettamento.add ( pag.get ( 0 ).getIuv () );
		}
		return retList;
	}

	private String determinaCov ( Set<String> distictCodiciVersamento, String iuv, String applicationId ) {
		String retCov = "----";
		if ( StringUtils.isNotBlank ( iuv ) ) {
			if ( iuv.length () > 17 ) {
				try {
					Integer.parseInt ( iuv.substring ( 13, 14 ) );
					retCov = iuv.substring ( 15, 19 );
				} catch ( NumberFormatException e ) {
					retCov = iuv.substring ( 13, 17 );
				}
			} else {
				retCov = recuperaCovDaIuvOtticiConApplicationId ( iuv, applicationId );
			}
		}
		distictCodiciVersamento.add ( retCov );
		return retCov;
	}

	private String recuperaCovDaIuvOtticiConApplicationId ( String iuv, String applicationId ) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionManagerFactory.getInstance ().getConnection ();
			stmt = conn.prepareStatement (
							"SELECT cod_versamento, application_id FROM iuv_ottici WHERE iuv_ottico = ? LIMIT 1" );
			stmt.setString ( 1, iuv );
			rs = stmt.executeQuery ();
			if ( rs.next () ) {
				String appId = rs.getString ( "application_id" );
				if ( appId.equals ( applicationId ) ) {
					return rs.getString ( "cod_versamento" );
				}
				ConnectionManager.closeResultSet ( rs );
				ConnectionManager.closeStatement ( stmt );
				// cerca in cov secondari
				stmt = conn.prepareStatement (
								"SELECT cs.cod_versamento FROM iuv_ottici_cov_secondari cs " +
												"JOIN iuv_ottici io ON io.id = cs.id_iuv_ottico " +
												"WHERE io.iuv_ottico = ? AND cs.application_id = ? LIMIT 1" );
				stmt.setString ( 1, iuv );
				stmt.setString ( 2, applicationId );
				rs = stmt.executeQuery ();
				if ( rs.next () )
					return rs.getString ( "cod_versamento" );
			}
		} catch ( Exception e ) {
			log.error ( "Errore determinaCov iuv={}", iuv, e );
		} finally {
			ConnectionManager.closeResultSet ( rs );
			ConnectionManager.closeStatement ( stmt );
			ConnectionManager.closeConnection ( conn );
		}
		return "----";
	}

	// --- Metodi di supporto ---

	private boolean isIuvPPAY ( String iuv ) {
		if ( StringUtils.isEmpty ( elencoCodiciSegregazioneDaTrattare ) )
			return true;
		if ( iuv.length () == 15 || iuv.startsWith ( "RF" ) )
			return true;
		if ( iuv.length () == 17 ) {
			String prefisso = iuv.substring ( 0, 2 );
			String[] codici = elencoCodiciSegregazioneDaTrattare.split ( ";" );
			return Arrays.asList ( codici ).contains ( prefisso );
		}
		return false;
	}

	private void aggiornaTestata ( TrasmettiFlussoRendicontazioneExtRequestType flussoExt,
					CtDatiSingoliPagamenti singoloPagamento, TipoIUV tipoIUV ) {
		switch ( tipoIUV ) {
		case RPT:
		case GETPAYMENT:
		case RECEIPT:
		case INTERMEDIATO:
			flussoExt.getTestata ().setImportoTotalePagamentiIntermediati (
							flussoExt.getTestata ().getImportoTotalePagamentiIntermediati ()
											.add ( singoloPagamento.getSingoloImportoPagato () ) );
			flussoExt.getTestata ().setNumeroTotalePagamentiIntermediati (
							flussoExt.getTestata ().getNumeroTotalePagamentiIntermediati ().add ( BigInteger.ONE ) );
			break;
		case NON_INTERMEDIATO:
			flussoExt.getTestata ().setImportoTotalePagamentiNonIntermediati (
							flussoExt.getTestata ().getImportoTotalePagamentiNonIntermediati ()
											.add ( singoloPagamento.getSingoloImportoPagato () ) );
			flussoExt.getTestata ().setNumeroTotalePagamentiNonIntermediati (
							flussoExt.getTestata ().getNumeroTotalePagamentiNonIntermediati ().add ( BigInteger.ONE ) );
			break;
		case SCONOSCIUTO:
			flussoExt.getTestata ().setImportoTotalePagamentiSconosciuti (
							flussoExt.getTestata ().getImportoTotalePagamentiSconosciuti ()
											.add ( singoloPagamento.getSingoloImportoPagato () ) );
			flussoExt.getTestata ().setNumeroTotalePagamentiSconosciuti (
							flussoExt.getTestata ().getNumeroTotalePagamentiSconosciuti ().add ( BigInteger.ONE ) );
			break;
		}
	}

	private TestataFlussoRendicontazioneExtType costruisciTestata ( FlussoRiversamentoDB singoloFlusso,
					CtFlussoRiversamento flusso ) {
		TestataFlussoRendicontazioneExtType testata = new TestataFlussoRendicontazioneExtType ();
		testata.setIdMessaggio ( singoloFlusso.getIdentificativoFlusso () );
		testata.setIdPSP ( singoloFlusso.getIdentificativoIstitutoMittente () );
		testata.setDataOraMessaggio ( flusso.getDataOraFlusso () );
		testata.setDataRegolamento ( flusso.getDataRegolamento () );
		testata.setDenominazioneEnte ( sanitizza ( flusso.getIstitutoRicevente () != null
						? flusso.getIstitutoRicevente ().getDenominazioneRicevente () : null ) );
		testata.setDenominazionePSP ( sanitizza ( flusso.getIstitutoMittente () != null
						? flusso.getIstitutoMittente ().getDenominazioneMittente () : null ) );
		testata.setCFEnteCreditore ( flusso.getIstitutoRicevente () != null
						? flusso.getIstitutoRicevente ().getIdentificativoUnivocoRicevente ().getCodiceIdentificativoUnivoco ()
						: "ND" );
		testata.setIdentificativoFlusso ( singoloFlusso.getIdentificativoFlusso () );
		testata.setIdentificativoUnivocoRegolamento ( singoloFlusso.getIdentificativoUnivocoRegolamento () );
		testata.setNumeroTotalePagamentiFlusso ( singoloFlusso.getNumeroTotalePagamenti () != null
						? BigInteger.valueOf ( singoloFlusso.getNumeroTotalePagamenti () ) : BigInteger.ZERO );
		testata.setImportoTotalePagamentiFlusso ( singoloFlusso.getImportoTotalePagamenti () != null
						? singoloFlusso.getImportoTotalePagamenti () : BigDecimal.ZERO );
		testata.setNumeroTotalePagamentiIntermediati ( BigInteger.ZERO );
		testata.setImportoTotalePagamentiIntermediati ( BigDecimal.ZERO );
		testata.setNumeroTotalePagamentiNonIntermediati ( BigInteger.ZERO );
		testata.setImportoTotalePagamentiNonIntermediati ( BigDecimal.ZERO );
		testata.setNumeroTotalePagamentiSconosciuti ( BigInteger.ZERO );
		testata.setImportoTotalePagamentiSconosciuti ( BigDecimal.ZERO );
		return testata;
	}

	private String sanitizza ( String input ) {
		return ( input == null || input.trim ().isEmpty () ) ? "ND" : input;
	}

	private ServiziRendicontazioneExt inizializzaServizioESB ( String urlEndpointServizio ) {
		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean ();
		factory.getInInterceptors ().add ( new LoggingInInterceptor () );
		factory.getOutInterceptors ().add ( new LoggingOutInterceptor () );
		factory.setServiceClass ( ServiziRendicontazioneExt.class );
		factory.setAddress ( urlEndpointServizio );
		return (ServiziRendicontazioneExt) factory.create ();
	}

	private Unmarshaller inizializzaUnMarshallerFlusso () throws Exception {
		JAXBContext context = JAXBContext.newInstance ( CtFlussoRiversamento.class );
		Unmarshaller unmarshaller = context.createUnmarshaller ();
		unmarshaller.setEventHandler ( new ValidationEventHandler () {

			public boolean handleEvent ( ValidationEvent event ) {
				log.warn ( "Errore validazione XML: {} {}", event.getMessage (), event.getSeverity () );
				return true;
			}
		} );
		SchemaFactory sf = SchemaFactory.newInstance ( "http://www.w3.org/2001/XMLSchema" );
		Source so = new StreamSource ( getClass ().getResourceAsStream ( "/FlussoRiversamento_1_0_4.xsd" ) );
		Schema s = sf.newSchema ( so );
		unmarshaller.setSchema ( s );
		return unmarshaller;
	}
}