/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;


public class FlussoRiversamentoDB {

	private Integer id;

	private String identificativoFlusso;

	private String identificativoPsp;

	private String identificativoIstitutoMittente;

	private String identificativoIstitutoRicevente;

	private String denominazioneMittente;

	private String denominazioneRicevente;

	private Integer numeroTotalePagamenti;

	private BigDecimal importoTotalePagamenti;

	private Timestamp dataOraFlusso;

	private Timestamp dataRegolamento;

	private String xmlFlusso;

	private String identificativoUnivocoRegolamento;

	public Integer getId () {
		return id;
	}

	public void setId ( Integer id ) {
		this.id = id;
	}

	public String getIdentificativoFlusso () {
		return identificativoFlusso;
	}

	public void setIdentificativoFlusso ( String identificativoFlusso ) {
		this.identificativoFlusso = identificativoFlusso;
	}

	public String getIdentificativoPsp () {
		return identificativoPsp;
	}

	public void setIdentificativoPsp ( String identificativoPsp ) {
		this.identificativoPsp = identificativoPsp;
	}

	public String getIdentificativoIstitutoMittente () {
		return identificativoIstitutoMittente;
	}

	public void setIdentificativoIstitutoMittente ( String v ) {
		this.identificativoIstitutoMittente = v;
	}

	public String getIdentificativoIstitutoRicevente () {
		return identificativoIstitutoRicevente;
	}

	public void setIdentificativoIstitutoRicevente ( String v ) {
		this.identificativoIstitutoRicevente = v;
	}

	public String getDenominazioneMittente () {
		return denominazioneMittente;
	}

	public void setDenominazioneMittente ( String denominazioneMittente ) {
		this.denominazioneMittente = denominazioneMittente;
	}

	public String getDenominazioneRicevente () {
		return denominazioneRicevente;
	}

	public void setDenominazioneRicevente ( String denominazioneRicevente ) {
		this.denominazioneRicevente = denominazioneRicevente;
	}

	public Integer getNumeroTotalePagamenti () {
		return numeroTotalePagamenti;
	}

	public void setNumeroTotalePagamenti ( Integer numeroTotalePagamenti ) {
		this.numeroTotalePagamenti = numeroTotalePagamenti;
	}

	public BigDecimal getImportoTotalePagamenti () {
		return importoTotalePagamenti;
	}

	public void setImportoTotalePagamenti ( BigDecimal importoTotalePagamenti ) {
		this.importoTotalePagamenti = importoTotalePagamenti;
	}

	public Timestamp getDataOraFlusso () {
		return dataOraFlusso;
	}

	public void setDataOraFlusso ( Timestamp dataOraFlusso ) {
		this.dataOraFlusso = dataOraFlusso;
	}

	public Timestamp getDataRegolamento () {
		return dataRegolamento;
	}

	public void setDataRegolamento ( Timestamp dataRegolamento ) {
		this.dataRegolamento = dataRegolamento;
	}

	public String getXmlFlusso () {
		return xmlFlusso;
	}

	public void setXmlFlusso ( String xmlFlusso ) {
		this.xmlFlusso = xmlFlusso;
	}

	public String getIdentificativoUnivocoRegolamento () {
		return identificativoUnivocoRegolamento;
	}

	public void setIdentificativoUnivocoRegolamento ( String v ) {
		this.identificativoUnivocoRegolamento = v;
	}
}