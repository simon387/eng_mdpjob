/*
 * SPDX-FileCopyrightText: (C) Copyright 2023 Regione Piemonte
 *
 * SPDX-License-Identifier: EUPL-1.2 */

package it.csi.mdpjob.dto;

public class ConfigParam510 {

	private String limiteNumFlussiGiornaliero;

	private String ordinamentoFlussi;

	private String identificativoFlusso;

	private String identificativoIstitutoRicevente;

	private String elencoCodiciSegregazione;

	public String getLimiteNumFlussiGiornaliero () {
		return limiteNumFlussiGiornaliero;
	}

	public void setLimiteNumFlussiGiornaliero ( String v ) {
		this.limiteNumFlussiGiornaliero = v;
	}

	public String getOrdinamentoFlussi () {
		return ordinamentoFlussi;
	}

	public void setOrdinamentoFlussi ( String v ) {
		this.ordinamentoFlussi = v;
	}

	public String getIdentificativoFlusso () {
		return identificativoFlusso;
	}

	public void setIdentificativoFlusso ( String v ) {
		this.identificativoFlusso = v;
	}

	public String getIdentificativoIstitutoRicevente () {
		return identificativoIstitutoRicevente;
	}

	public void setIdentificativoIstitutoRicevente ( String v ) {
		this.identificativoIstitutoRicevente = v;
	}

	public String getElencoCodiciSegregazione () {
		return elencoCodiciSegregazione;
	}

	public void setElencoCodiciSegregazione ( String v ) {
		this.elencoCodiciSegregazione = v;
	}
}